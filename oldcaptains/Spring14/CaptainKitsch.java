
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;

import javax.swing.JOptionPane;

/**
 * Captain Kitchen Sink is an adaptive captain that can adjust his offensive and
 * defensive strategies according to what is working best against a given enemy.
 * He records what attacks have resulted in hits and which ones have missed and
 * tries to favor the hits and avoid the misses later on. This is designed to
 * exploit and AIs that have a 'pattern' to their ship placement. Extra weight
 * is given to the patrol boat if it is ever found.
 *
 * In an attempt to hide the patrol boat it is always placed first and randomly.
 * The rest of the ships are placed according to one of 5 different strategies.
 * Initially all 5 are used to see which one is most effective and then the best
 * defensive strategy is chosen and used for the remainder of matches with that
 * opponent.
 */
public class CaptainKitsch implements Captain {

    /**
     * *********************** Members *********************
     */
    // A random number generator for making bad decisions
    protected Random generator;
    // The locations of my ships
    protected Fleet myFleet;
    protected EnemyShip[] mEnemyFleet;
    // The last coordinate attacked
    protected Coordinate mLastAttack;
    protected int mLastSearch;
    // Keeping track of our search progress
    protected int mSearchSpot;
    protected String mCurEnemy;
    // Patterns and decisions associated with the current enemy
    protected ArrayList<Coordinate> mBackupSearchPattern;
    protected HashMap< String, ArrayList<PriorityCoordinate>> mOpponentPlacement;
    protected HashMap< String, ArrayList<NeighborCoordinate>> mOpponentAttacks;
    protected HashMap< String, ArrayList<Integer>> mDefensePattern;
    protected HashMap< String, Integer> mMatchCount;
    protected HashMap< String, Integer> mDefensePick;
    // How long will the learning phase be?
    protected long OFFENSIVE_WARMUP_MATCHES;
    protected long DEFENSIVE_WARMUP_MATCHES;
    protected int mCurDefense;
    protected static final long MERCY_RULE = 30;
    protected static final int DEFENSE_COUNT = 6;
    protected static final int ENABLED_DEFENSES = 3;
    // Knowledge of current layout
    protected boolean[] mAttempted;

    public CaptainKitsch() {
        mOpponentPlacement = new HashMap<String, ArrayList<PriorityCoordinate>>();
        mOpponentAttacks = new HashMap<String, ArrayList<NeighborCoordinate>>();
        mDefensePattern = new HashMap<String, ArrayList<Integer>>();
        mMatchCount = new HashMap<String, Integer>();
        mDefensePick = new HashMap<String, Integer>();

        // Initial search pattern (in case we need more than 52)
        mBackupSearchPattern = new ArrayList<Coordinate>(48);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 5; j++) {
                if ((i == 0 && j == 0) || (i == 9 && j == 9)) {
                    continue;
                }
                if (i % 2 == 0) {
                    mBackupSearchPattern.add(new Coordinate(i, j * 2));
                } else {
                    mBackupSearchPattern.add(new Coordinate(i, j * 2 + 1));
                }
            }
        }
    }

    /**
     * *********************** Main Captain Interface Methods *********************
     */
    // Called before each game to reset your ship locations.
    @Override
    public void initialize(int pNumMatches, int pNumCaptains, String pOpponent) {

        // How long should we gather stats?
        OFFENSIVE_WARMUP_MATCHES = Math.round(0.12 * pNumMatches);
        DEFENSIVE_WARMUP_MATCHES = Math.round(0.25 * pNumMatches);

        // Add a search list for this opponent if one doesn't already exist
        if (!mOpponentPlacement.containsKey(pOpponent)) {
            ArrayList<PriorityCoordinate> newList = new ArrayList<PriorityCoordinate>(52);
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 5; j++) {
                    if (i % 2 == 0) {
                        newList.add(new PriorityCoordinate(i, j * 2 + 1));
                    } else {
                        newList.add(new PriorityCoordinate(i, j * 2));
                    }
                }
            }
            newList.add(new PriorityCoordinate(0, 0));
            newList.add(new PriorityCoordinate(9, 9));
            mOpponentPlacement.put(pOpponent, newList);

            // Add a attack pattern to track where this opponent attacks
            ArrayList<NeighborCoordinate> newList2 = new ArrayList<NeighborCoordinate>(100);
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    newList2.add(new NeighborCoordinate(i, j));
                }
            }

            // Set the neighbors
            int index = 0;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    index = j + i * 10;
                    if (i < 9) {
                        newList2.get(index).vertNeighbor = newList2.get(index + 10);
                    }
                    if (j < 9) {
                        newList2.get(index).horizNeighbor = newList2.get(index + 1);
                    }
                }
            }
            mOpponentAttacks.put(pOpponent, newList2);

            // Create the remaining opponent specific stats
            ArrayList<Integer> newDefenseList = new ArrayList<Integer>(DEFENSE_COUNT);
            newDefenseList.addAll(Collections.nCopies(DEFENSE_COUNT, 0));
            mDefensePattern.put(pOpponent, newDefenseList);

            mMatchCount.put(pOpponent, 0);
            mDefensePick.put(pOpponent, -1);
        }

        // Randomize the attack pattern and start over
        mCurEnemy = pOpponent;
        mSearchSpot = 0;

        // While we're warming up, randomize our search for more equal sampling
        if (mMatchCount.get(mCurEnemy) < OFFENSIVE_WARMUP_MATCHES) {
            Collections.shuffle(mOpponentPlacement.get(mCurEnemy));
        } else {
            // Update all the priorities
            ArrayList<PriorityCoordinate> searchList = mOpponentPlacement.get(mCurEnemy);
            for (int i = 0; i < searchList.size(); i++) {
                searchList.get(i).updatePriority();
            }

            // Sort by priorities
            Collections.sort(searchList);
        }

        // Prepare to face this particular opponent
        generator = new Random();
        myFleet = new Fleet();

        mEnemyFleet = new EnemyShip[5];
        for (int i = 0; i < 5; i++) {
            mEnemyFleet[i] = new EnemyShip(i);
        }

        // Remember our previous attempts (for this game only)
        mAttempted = new boolean[100];
        for (int i = 0; i < 100; i++) {
            mAttempted[i] = false;
        }

        // During warmup (-1 for curDefense), keep trying different strategies
        pickDefensiveStrategy();
        if (mCurDefense == -1) {
            mCurDefense = mMatchCount.get(mCurEnemy) % ENABLED_DEFENSES;
        }

        // Place the patrol boat
        positionPatrolBoat();

        // Place the rest of the ships
        switch (mCurDefense) {
            case 0:
                positionShipsAtEdge();
                break;
            case 1:
                positionShipsOuterHalf();
                break;
            case 2:
                positionShipsClustered();
                break;
            case 3:
                positionShipsParallelRandom();
                break;
            case 4:
                positionShipsFullRandom();
                break;
            case 5:
                positionShipsIdeal();
                break;
        }
    }

    protected void positionPatrolBoat() {
//    	if(mMatchCount.get(mCurEnemy) > DEFENSIVE_WARMUP_MATCHES) {
        // Random placement of patrol boat.  We should be able to do better but for now, this works best.
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), PATROL_BOAT));
//    	} else {
//    		// Get the attack list
//	    	ArrayList<NeighborCoordinate> mHuntList = mOpponentAttacks.get(mCurEnemy);
//	    	NeighborCoordinate bestCoord = new NeighborCoordinate(0, 0);
//	    	int orient = HORIZONTAL;
//
//	    	// Find the least attacked pair of horizontal squares
//	    	long minAttacks = Long.MAX_VALUE;
//	    	NeighborCoordinate.SORT_TYPE = HORIZONTAL;
//	    	for (NeighborCoordinate coord : mHuntList) {
//				coord.updatePriority();
//				if(coord.priority < minAttacks) {
//					minAttacks = coord.priority;
//					bestCoord = coord;
//				}
//			}
//	    	
//	    	// See if there's a pair of vertical squares that's better
//	    	NeighborCoordinate.SORT_TYPE = VERTICAL;
//	    	for (NeighborCoordinate coord : mHuntList) {
//				coord.updatePriority();
//				if(coord.priority < minAttacks) {
//					minAttacks = coord.priority;
//					bestCoord = coord;
//					orient = VERTICAL;
//				}
//			}
//	    	
//
//	    	// Place ship at best spot
//	    	if(!myFleet.placeShip(bestCoord, orient, PATROL_BOAT))
//	    	{
//	    		// If that fails, just do it random
//		        while(!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), PATROL_BOAT));
//	    	}
//    	}
    }

    /**
     * Look at the opponents attack pattern and place ships where he attacks the
     * least
     */
    protected void positionShipsIdeal() {
        // Sort attack list
        ArrayList<NeighborCoordinate> mHuntList = mOpponentAttacks.get(mCurEnemy);
        NeighborCoordinate.SORT_TYPE = 3; // No neighbor
        for (NeighborCoordinate coord : mHuntList) {
            coord.updatePriority();
        }
        Collections.sort(mHuntList);

        // Move through the list in reverse
        int attackIndex = mHuntList.size() - 1;
        for (int shipType = 4; shipType > 0; shipType--) {
            while (!myFleet.placeShip(mHuntList.get(attackIndex), generator.nextInt(2), shipType)) {
                attackIndex--;
            }
            attackIndex = mHuntList.size() - 1; // Start over
        }
    }

    /**
     * Place all the ships around the outermost edge of the board where they
     * would be least likely to be found in a perfectly random distribution.
     * This is a common strategy when human's play each other and can really
     * mess up a captain that doesn't place enough weight in your previously
     * chosen positions. It is often picked against weaker AIs.
     */
    protected void positionShipsAtEdge() {

        for (int shipType = 4; shipType > 0; shipType--) {
            int X = -1, Y = -1, index = 0, direction = 0;
            do {
                index = generator.nextInt(36);
                switch (index / 9) {
                    case 0:
                        X = (index % 9);
                        Y = 0;
                        direction = HORIZONTAL;
                        break;

                    case 1:
                        X = (index % 9);
                        Y = 9;
                        direction = HORIZONTAL;
                        break;

                    case 2:
                        Y = (index % 9);
                        X = 0;
                        direction = VERTICAL;
                        break;

                    case 3:
                        Y = (index % 9);
                        X = 9;
                        direction = VERTICAL;
                        break;
                }
            } while (!myFleet.placeShip(X, Y, direction, shipType));
        }
    }

    /**
     * Avoids the inner 16 squares and tries to pack all the ships in the otter
     * half of the board. This intentionally avoids what would normally be the
     * most likely squares in a perfectly random distribution. Opponents that
     * are watching you distribution will pick up on this and learn to never
     * attack the center squares. This strategy was inspired by
     * CaptainWellEducatedGuess.
     */
    protected void positionShipsOuterHalf() {

        int X = -1, Y = -1, pick = 0, index = 0, direction = 0;
        int radius = 0, low = 0, high = 0;
        boolean badSpot = false;
        int lLength = 2;

        Coordinate botLeft = new Coordinate(2, 2);
        Coordinate topRight = new Coordinate(7, 7);

        for (int shipType = 4; shipType > 0; shipType--) {
            switch (shipType) {
                case PATROL_BOAT:
                    lLength = PATROL_BOAT_LENGTH;
                    break;
                case DESTROYER:
                    lLength = DESTROYER_LENGTH;
                    break;
                case SUBMARINE:
                    lLength = SUBMARINE_LENGTH;
                    break;
                case BATTLESHIP:
                    lLength = BATTLESHIP_LENGTH;
                    break;
                default:
                    lLength = AIRCRAFT_CARRIER_LENGTH;
                    break;
            }

            pick = generator.nextInt(100);
            if (pick < 33) {
                radius = 5;
                low = 2;
                high = 7;
            } else if (pick < 66) {
                radius = 7;
                low = 1;
                high = 8;
            } else {
                radius = 9;
                low = 0;
                high = 9;
            }

            do {
                index = generator.nextInt(radius * 4);
                switch (index / radius) {
                    case 0:
                        X = low + (index % radius);
                        Y = low;
                        direction = (lLength > 3 ? HORIZONTAL : generator.nextInt(2));
                        break;

                    case 1:
                        X = low + (index % radius);
                        Y = high;
                        direction = (lLength > 3 ? HORIZONTAL : generator.nextInt(2));
                        break;

                    case 2:
                        Y = low + (index % radius);
                        X = low;
                        direction = (lLength > 3 ? VERTICAL : generator.nextInt(2));
                        break;

                    case 3:
                        Y = low + (index % radius);
                        X = high;
                        direction = (lLength > 3 ? VERTICAL : generator.nextInt(2));
                        break;
                }

                // Check for ships invading the central 16 squares	        	
                badSpot = false;
                Coordinate location = new Coordinate(X, Y);
                Coordinate farEnd = new Coordinate(location.getX(), location.getY() + lLength - 1);
                if (direction == HORIZONTAL) {
                    farEnd = new Coordinate(location.getX() + lLength - 1, location.getY());
                }

                if (location.greaterThan(botLeft) && location.lessThan(topRight)) {
                    badSpot = true;
                } else if (farEnd.greaterThan(botLeft) && farEnd.lessThan(topRight)) {
                    badSpot = true;
                }

            } while (badSpot || !myFleet.placeShip(X, Y, direction, shipType));
        }
    }

    /**
     * Try to cluster all the ships tightly together. This is a common strategy
     * for a human player against a human opponent. We are predisposed to assume
     * the ships will be spread out. Most AI captains will not have this
     * predisposition but it still tends to get picked against some weaker
     * opponents.
     */
    protected void positionShipsClustered() {

        // Pick a random 5x5 square
        Coordinate botLeft = new Coordinate(generator.nextInt(5), generator.nextInt(5));
        Coordinate topRight = new Coordinate(botLeft.getX() + 5, botLeft.getY() + 5);

        // Pack all 5 ships into that square
        Coordinate near, far;
        boolean badSpot = false;
        int lLength = 0, direction = HORIZONTAL;
        for (int shipType = 4; shipType > 0; shipType--) {
            switch (shipType) {
                case PATROL_BOAT:
                    lLength = PATROL_BOAT_LENGTH;
                    break;
                case DESTROYER:
                    lLength = DESTROYER_LENGTH;
                    break;
                case SUBMARINE:
                    lLength = SUBMARINE_LENGTH;
                    break;
                case BATTLESHIP:
                    lLength = BATTLESHIP_LENGTH;
                    break;
                default:
                    lLength = AIRCRAFT_CARRIER_LENGTH;
                    break;
            }

            do {
                near = new Coordinate(botLeft.getX() + generator.nextInt(5), botLeft.getY() + generator.nextInt(5));
                direction = generator.nextInt(2);

                // Ensure ship is inside the 6x6 square
                badSpot = false;
                if (direction == VERTICAL) {
                    far = new Coordinate(near.getX(), near.getY() + lLength - 1);
                } else {
                    far = new Coordinate(near.getX() + lLength - 1, near.getY());
                }

                if (near.lessThan(botLeft) || near.greaterThan(topRight)) {
                    badSpot = true;
                } else if (far.lessThan(botLeft) || far.greaterThan(topRight)) {
                    badSpot = true;
                }

            } while (badSpot || !myFleet.placeShip(near.getX(), near.getY(), direction, shipType));

        }

    }

    /**
     * A slightly unusual variation of the purely random approach of Captain
     * Loco. Here ships are placed randomly but they are either all horizontal
     * or all vertical. This was inspired by the very successful captain MrTwo.
     */
    protected void positionShipsParallelRandom() {
        int shipOrient = generator.nextInt(2);
        for (int i = 4; i > 0; i--) {
            while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), shipOrient, i));
        }
    }

    /**
     * This is the same approach as Captain Loco. It is rarely picked as the
     * best but against a highly adaptive opponent that is watching your
     * distribution it might be the "least awful" choice. For us, when every
     * option is losing against this enemy we default to this strategy.
     */
    protected void positionShipsFullRandom() {
        for (int i = 4; i > 0; i--) {
            while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), i));
        }
    }

    // Passes your ship locations to the main program.
    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    // Makes an attack on the opponent
    @Override
    public Coordinate makeAttack() {

        // This will be our chosen attack coordinate
        Coordinate lCoord = null;
        mLastSearch = -1;

        /* Below, any time we make a choice we validate it with the isValidNextAttack() method.
         * This method will check the coordinate to make sure it is in bounds (between [0 9] in
         * each dimension) and that we haven't attacked there before.
         */

        // Have we found the Patrol Boat?  (Ignore other ships until we have)
        if (mEnemyFleet[PATROL_BOAT].isFound()) {

            // Check if we've found any that are not yet sunk
            for (int i = 0; i < 5; i++) {
                // If we've found a ship, hunt it until it's sunk!
                if (!mEnemyFleet[i].isSunk() && mEnemyFleet[i].isFound()) {

                    // Ask for next hunting coordinate until we get a valid one.
                    // All 'hunting' logic is inside the EnemyShip object.
                    int count = 0;
                    do {
                        lCoord = mEnemyFleet[i].hunt();
                        count++;
                    } while (!isValidNextAttack(lCoord) && count < 10);

                    // If for some reason we didn't get a valid coordinate then just skip this ship
                    if (!isValidNextAttack(lCoord)) {
                        System.out.println("Incomplete hunt abandoned.\n");
                        lCoord = null;
                    }
                }

                // Stop looping once we have a ship to attack
                if (lCoord != null) {
                    break;
                }
            }
        }

        // If we aren't hunting, we are searching, follow the prescribed pattern
        while (lCoord == null) {

            Coordinate lCandidate = null;
            ArrayList<PriorityCoordinate> lCurList = mOpponentPlacement.get(mCurEnemy);
            if (mSearchSpot < 52) {
                while (mSearchSpot < lCurList.size() && !isValidNextAttack(lCandidate)) {
                    lCandidate = lCurList.get(51 - mSearchSpot);
                    mSearchSpot++;
                }
            } else if (mSearchSpot >= 52 && mSearchSpot < 100) {
                while (mSearchSpot - 52 < mBackupSearchPattern.size() && !isValidNextAttack(lCandidate)) {
                    lCandidate = mBackupSearchPattern.get(mSearchSpot - 52);
                    mSearchSpot++;
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "CRAP!  You ran out of search spaces.  That shouldn't happen!",
                        "Ruh Rooh!", JOptionPane.WARNING_MESSAGE);
            }

            lCoord = lCandidate;
            mLastSearch = mSearchSpot - 1;
        }

        // Remember the attack coordinates so we can refer to them later (in resultOfAttack())
        mLastAttack = lCoord;

        // Note that this square has now been attacked (so we don't attack it again later)
        mAttempted[lCoord.getY() * 10 + lCoord.getX()] = true;

        // Return the attack coordinates
        return lCoord;
    }

    protected void pickDefensiveStrategy() {

        // Have we settled on a strategy for this one?
        if (mDefensePick.get(mCurEnemy) != -1) {
            mCurDefense = mDefensePick.get(mCurEnemy);
            return;
        }

        // Check if it is time to settle on one
        ArrayList<Integer> myDefList = mDefensePattern.get(mCurEnemy);
        int maxVal = Collections.max(myDefList);

        mCurDefense = -1;
        if (maxVal > MERCY_RULE || mMatchCount.get(mCurEnemy) > DEFENSIVE_WARMUP_MATCHES) {
            if (maxVal < 0) {
                mCurDefense = DEFENSE_COUNT - 1;
            } else {
                for (int i = 0; i < myDefList.size(); i++) {
                    if (myDefList.get(i) == maxVal) {
                        mCurDefense = i;
                        break;
                    }
                }
            }
        }

        // If we did pick a strategy, remember it!
        if (mCurDefense != -1) {
            mDefensePick.put(mCurEnemy, mCurDefense);

            // Optional output so we know which strategies are getting picked
//			String defString = "";
//			switch(mCurDefense)
//			{
//	        	case 0: defString = "edge pack"; break;
//	        	case 1: defString = "outer half"; break;
//	        	case 2: defString = "clustered"; break;
//	        	case 3: defString = "parallel random"; break;
//	        	case 4: defString = "captain loco"; break;
//			}
//			
//			System.out.printf("Picked %s strategy for %s after round %d.\n",
//					defString, mCurEnemy, mMatchCount.get(mCurEnemy));
        }
    }

    // Informs you of the result of your most recent attack
    @Override
    public void resultOfAttack(int result) {

        // Check the 'result code' to see if we have a hit or a miss
        if (isHit(result)) {

            // Record a hit at that position 
            int index = mOpponentPlacement.get(mCurEnemy).indexOf(mLastAttack);
            if (index != -1) {
                // Weight the patrol boat higher
                if (whichShip(result) == PATROL_BOAT) {
                    mOpponentPlacement.get(mCurEnemy).get(index).hits += 100;
                } else {
                    mOpponentPlacement.get(mCurEnemy).get(index).hits++;
                }
            }

            // Update our enemy fleet tracker
            switch (whichShip(result)) {
                case PATROL_BOAT:
                    mEnemyFleet[0].addHit(mLastAttack);
                    break;
                case DESTROYER:
                    mEnemyFleet[1].addHit(mLastAttack);
                    break;
                case SUBMARINE:
                    mEnemyFleet[2].addHit(mLastAttack);
                    break;
                case BATTLESHIP:
                    mEnemyFleet[3].addHit(mLastAttack);
                    break;
                case AIRCRAFT_CARRIER:
                    mEnemyFleet[4].addHit(mLastAttack);
                    break;
                default:
                    break;
            }

        } else {
            // Record a miss at that position
            int index = mOpponentPlacement.get(mCurEnemy).indexOf(mLastAttack);
            if (index != -1) {
                mOpponentPlacement.get(mCurEnemy).get(index).misses++;
            }
        }
    }

    // Informs you of the position of an attack against you.
    @Override
    public void opponentAttack(Coordinate pCoord) {
        // Make a note that the enemy likes to 'hunt' that square
        int index = mOpponentAttacks.get(mCurEnemy).indexOf(pCoord);
        if (index != -1) {
            mOpponentAttacks.get(mCurEnemy).get(index).priority++;
        }
    }

    // Informs you of the result of the game.
    @Override
    public void resultOfGame(int result) {

        // Increase the match count
        mMatchCount.put(mCurEnemy, mMatchCount.get(mCurEnemy) + 1);

        // Keep track of wins/losses during warmup
        if (mMatchCount.get(mCurEnemy) < DEFENSIVE_WARMUP_MATCHES) {

            // Record a win or loss
            ArrayList<Integer> myDefList = mDefensePattern.get(mCurEnemy);
            if (result == WON) {
                myDefList.set(mCurDefense, myDefList.get(mCurDefense) + 1);
            } else {
                myDefList.set(mCurDefense, myDefList.get(mCurDefense) - 1);
            }

        }
    }

    /**
     * *********************** Helper Methods *********************
     */
    // Easy way to check if the next attack is reasonable (coordinates in range and not one we've already attacked)
    private boolean isWithinBounds(Coordinate pCoord) {
        if (pCoord == null) {
            return false;
        }
        if (pCoord.getX() < 0 || pCoord.getX() >= 10 || pCoord.getY() < 0 || pCoord.getY() >= 10) {
            return false;
        }
        return true;
    }

    // Easy way to check if the next attack is reasonable (coordinates in range and not one we've already attacked)
    private boolean isValidNextAttack(Coordinate pCoord) {
        if (pCoord == null || !isWithinBounds(pCoord)) {
            return false;
        }
        int index = pCoord.getY() * 10 + pCoord.getX();
        return !mAttempted[index];
    }

    // Shortcuts so I don't have to remember how to process the 'result' code
    private static boolean isHit(int pResultCode) {
        return (pResultCode % HIT_MODIFIER != pResultCode);
    }

    private static boolean isSunk(int pResultCode) {
        return (pResultCode % SUNK_MODIFIER != pResultCode);
    }

    private static int whichShip(int pResultCode) {
        return (pResultCode % 10);
    }

    private class PriorityCoordinate extends Coordinate implements Comparable<PriorityCoordinate> {

        public long hits;
        public long misses;
        public long priority;

        public PriorityCoordinate(int x, int y, long priority) {
            super(x, y);
            this.priority = priority;
            hits = misses = 0;
        }

        public PriorityCoordinate(int x, int y) {
            super(x, y);
            priority = 0;
            hits = misses = 0;
        }

        public void updatePriority() {
            priority = hits - misses;
        }

        ;

		@Override
        public int compareTo(PriorityCoordinate o) {
            if (priority < o.priority) {
                return -1;
            }
            if (priority == o.priority) {
                return 0;
            }
            return 1;
        }
    }

    static private class NeighborCoordinate extends Coordinate implements Comparable<NeighborCoordinate> {

        public static int SORT_TYPE = HORIZONTAL;
        public long attacks;
        public long priority;
        NeighborCoordinate vertNeighbor, horizNeighbor;

        public NeighborCoordinate(int x, int y) {
            super(x, y);
            attacks = 0;
            priority = 0;
            vertNeighbor = null;
            horizNeighbor = null;
        }

        public void updatePriority() {
            if (SORT_TYPE == VERTICAL) {
                priority = (vertNeighbor == null) ? Long.MAX_VALUE : (attacks + vertNeighbor.attacks);
            } else if (SORT_TYPE == HORIZONTAL) {
                priority = (horizNeighbor == null) ? Long.MAX_VALUE : (attacks + horizNeighbor.attacks);
            } else {
                priority = attacks;
            }
        }

        @Override
        public int compareTo(NeighborCoordinate o) {
            if (priority < o.priority) {
                return -1;
            }
            if (priority == o.priority) {
                return 0;
            }
            return 1;
        }
    }

    /**
     * EnemyShip is a class for tracking an opponent's fleet. It records how
     * many times a ship has been hit and where those hits occurred. Using this
     * information it is able to guide a 'hunt' for the rest of a ship. The
     * function 'hunt' will give you a new suggested coordinate to attack based
     * on what is known about that ship so far. In general is should be used as
     * follows: <ul> <li>Create a separate EnemyShip for each type of ship (5 in
     * total)</li> <li>When you get a hit, record it in the appropriate
     * EnemyShip</li> <li>Check each EnemyShip for ones that are 'found' and ask
     * it to 'hunt' them</li> <li>Be sure to 'verify' the coordinates it returns
     * from hunt()</li> <ul> <li>The coordinates may be outside the playing
     * field</li> <li>It may suggest coordinates that you've already
     * attacked</li> <li>Just keep asking for coordinates until you get ones
     * that make sense</li> </ul> <li>As you record more hits it will change
     * it's hunting strategy to match what is known about that ship</li> <li>Of
     * course, once a ship isSunk() you can stop hunting it.</li> </ul>
     */
    private class EnemyShip {

        EnemyShip(int pModel) {
            mModel = pModel;
            mHuntPhase = ENEMY_HUNT_HIDDEN;

            // Store the length of the ship based on it's model
            switch (pModel) {
                case PATROL_BOAT:
                    mLength = PATROL_BOAT_LENGTH;
                    break;
                case DESTROYER:
                    mLength = DESTROYER_LENGTH;
                    break;
                case SUBMARINE:
                    mLength = SUBMARINE_LENGTH;
                    break;
                case BATTLESHIP:
                    mLength = BATTLESHIP_LENGTH;
                    break;
                default:
                    mLength = AIRCRAFT_CARRIER_LENGTH;
                    break;
            }

            mHits = new ArrayList<Coordinate>();
            mHits.ensureCapacity(mLength);
        }

        // Any time we get a hit, add it here
        public void addHit(Coordinate pCoord) {
            // Add the hit to the list
            mHits.add(pCoord);

            // Update hunt phase
            if (mHits.size() < 2) {
                mHuntPhase = ENEMY_HUNT_FOUND;
            } else if (mHits.size() == 2) {
                mHuntPhase = ENEMY_HUNT_ORIENTED;

                switch (getOrientation()) {
                    case VERTICAL:
                        if (mHits.get(0).getY() < mHits.get(1).getY()) {
                            mEndA = mHits.get(0);
                            mEndB = mHits.get(1);
                        } else {
                            mEndA = mHits.get(1);
                            mEndB = mHits.get(0);
                        }
                        break;

                    case HORIZONTAL:
                        if (mHits.get(0).getX() < mHits.get(1).getX()) {
                            mEndA = mHits.get(0);
                            mEndB = mHits.get(1);
                        } else {
                            mEndA = mHits.get(1);
                            mEndB = mHits.get(0);
                        }
                        break;
                }
            }
        }

        // If we have two or more hits we can guess the orientation
        public int getOrientation() {
            if (mHits.size() >= 2) {
                if (mHits.get(0).getX() == mHits.get(1).getX()) {
                    return VERTICAL;
                } else if (mHits.get(0).getY() == mHits.get(1).getY()) {
                    return HORIZONTAL;
                } else {
                    System.out.println("What the, this ship is neither vertical or horizontal!?!?!?");
                }
            }

            return -1;
        }

        // These are functions that universally encode the logic for "hunting" a found ship
        public Coordinate hunt() {
            Coordinate lNext = new Coordinate(-1, -1);
            switch (mHuntPhase - (mHuntPhase % 10)) {
                // You shuldn't be using this function until you've hit the ship at least once
                case ENEMY_HUNT_HIDDEN:
                    break;

                // We've got 1 hit but don't know the orientation yet
                case ENEMY_HUNT_FOUND: {
                    // Attack around the hit to find orientation
                    do {
                        switch (mHuntPhase % 10) {
                            // Attack N, E, S and W
                            case 0:
                                lNext = new Coordinate(mHits.get(0).getX(), mHits.get(0).getY() + 1);
                                break;
                            case 1:
                                lNext = new Coordinate(mHits.get(0).getX() + 1, mHits.get(0).getY());
                                break;
                            case 2:
                                lNext = new Coordinate(mHits.get(0).getX() - 1, mHits.get(0).getY());
                                break;
                            case 3:
                                lNext = new Coordinate(mHits.get(0).getX(), mHits.get(0).getY() - 1);
                                break;
                        }

                        // Move to next point in hunt phase
                        mHuntPhase++;
                    } while (!isWithinBounds(lNext) && mHuntPhase < 14);
                }
                break;

                // We know the orientation now so widen our hunt
                case ENEMY_HUNT_ORIENTED:
                default: {
                    // Expand out based on orientation
                    if (getOrientation() == HORIZONTAL) {
                        if (!isPreviousHit(mEndA)) {
                            lNext = new Coordinate(mEndB.getX() - 1, mEndB.getY());
                            mEndB = lNext;
                        } else if (!isPreviousHit(mEndB)) {
                            lNext = new Coordinate(mEndA.getX() + 1, mEndA.getY());
                            mEndA = lNext;
                        } else {
                            if (mHuntPhase % 2 == 0) {
                                lNext = new Coordinate(mEndA.getX() + 1, mEndA.getY());
                                mEndA = lNext;
                            } else {
                                lNext = new Coordinate(mEndB.getX() - 1, mEndB.getY());
                                mEndB = lNext;
                            }
                        }
                    } else {
                        if (!isPreviousHit(mEndA)) {
                            lNext = new Coordinate(mEndB.getX(), mEndB.getY() - 1);
                            mEndB = lNext;
                        } else if (!isPreviousHit(mEndB)) {
                            lNext = new Coordinate(mEndA.getX(), mEndA.getY() + 1);
                            mEndA = lNext;
                        } else {
                            if (mHuntPhase % 2 == 0) {
                                lNext = new Coordinate(mEndA.getX(), mEndA.getY() + 1);
                                mEndA = lNext;
                            } else {
                                lNext = new Coordinate(mEndB.getX(), mEndB.getY() - 1);
                                mEndB = lNext;
                            }
                        }
                    }

                    // Move to next point for hunting
                    mHuntPhase++;
                }
                break;
            }

            return lNext;
        }

        public boolean isPreviousHit(Coordinate pTest) {
            if (!isWithinBounds(pTest)) {
                return false;
            }
            for (Coordinate pCur : mHits) {
                if (pTest.equals(pCur)) {
                    return true;
                }
            }
            return false;
        }

        // Have we found this ship yet and better yet, have we sunk it?
        public boolean isFound() {
            return (mHits.size() > 0);
        }

        public boolean isSunk() {
            return (mHits.size() == mLength);
        }
        // Store the length and current number of hits
        protected int mLength;
        protected int mModel;
        // Different phases of hunting for a ship
        final static int ENEMY_HUNT_HIDDEN = 0;			// Initial state, ship is completely hidden
        final static int ENEMY_HUNT_FOUND = 10;			// First phase, we have a single hit (orientation is unknown)
        final static int ENEMY_HUNT_ORIENTED = 20;		// Second phase, we have more than 1 hit (orientation is known)
        protected int mHuntPhase;
        protected ArrayList<Coordinate> mHits;
        protected Coordinate mEndA, mEndB;
    }
}
