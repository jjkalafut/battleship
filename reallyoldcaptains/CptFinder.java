
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class CptFinder implements Captain {

    protected Random generator;
    protected Fleet myFleet;
    /*^*VARIABLES*^*/
    private int[][][] shipMap = new int[6][10][10];
    private int[][] knownBoard = new int[10][10];
    private Coordinate locAttacked;
    private final int UNKNOWN = -999;
    private int damageCounters[] = {5, 4, 3, 3, 2};
    /*^*^*^*^*^*^*^*/

    /**
     * Get ready for the match
     *
     * @param numMatches The number matches you will be engaging in with this
     * opponent.
     * @param numCaptains The number of opponents you will be facing during the
     * current set of battles.
     * @param opponent The name of your opponent for this match
     */
    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
        generator = new Random();
        myFleet = new Fleet();
        for (int i = 0; i < knownBoard.length; i++) {
            for (int j = 0; j < knownBoard[i].length; j++) {
                knownBoard[i][j] = UNKNOWN;
            }
        }

        generateShipMap();

        placeShips(); //this captain uses the placement algorithm from MalcolmReynolds
    }

    /**
     * Send my fleet to the system running the program
     *
     * @return A valid fleet representing my ship placements for this round.
     */
    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    /**
     * Make an attack
     *
     * @return A valid coordinate on the playing field.
     */
    @Override
    public Coordinate makeAttack() {
        for (int x = 0; x < knownBoard.length; x++) {
            for (int y = 0; y < knownBoard[x].length; y++) {
                if (true || knownBoard[x][y] != UNKNOWN) {
                    for (int type = PATROL_BOAT; type <= AIRCRAFT_CARRIER; type++) {
                        shipMap[type][x][y] = 0;
                    }
                }
            }
        }
        shipMap[5][0][0] = 5;
        sumShipMap();
        Vector<Coordinate> listOfHighest = new Vector<Coordinate>();
        listOfHighest.add(new Coordinate(0, 0));
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (true || (x - y) % 2 == 0) {
                    //look at the total ship map and find the highest
                    if (shipMap[5][x][y] == shipMap[5][listOfHighest.firstElement().getX()][listOfHighest.firstElement().getY()]) {
                        listOfHighest.add(new Coordinate(x, y));
                    } else if (shipMap[5][x][y] > shipMap[5][listOfHighest.firstElement().getX()][listOfHighest.firstElement().getY()]) {
                        listOfHighest.clear();
                        listOfHighest.add(new Coordinate(x, y));
                    }
                }
            }
        }
        //pick one randomly
        locAttacked = listOfHighest.get(generator.nextInt(listOfHighest.size()));
        //if(knownBoard[locAttacked.getX()][locAttacked.getY()] != UNKNOWN)
        //		System.err.println("ATTACKING KNOWN LOCATION!!!\n");
        return locAttacked;
    }

    private void printTotalProbabilityMap() {
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                System.out.print(knownBoard[x][y]);
                System.out.print('\t');
            }
            System.out.print("\n\n");
        }
    }

    /**
     * @param result A code from Constants that tells me all about the results
     * of my last attack.
     */
    @Override
    public void resultOfAttack(int result) {
        int locX = locAttacked.getX(), locY = locAttacked.getY();
        if (result == MISS) {
            knownBoard[locX][locY] = result;
            //generateShipMap();
            result = -1;
        }
        if (result / HIT_MODIFIER == 1) { // A ship was hit!
            result -= HIT_MODIFIER;
            int shipSize = getShipSize(result);
            knownBoard[locX][locY] = result; // update knownMap

            // zero the hit ship's map
            for (int i = 0; i < shipMap[result].length; i++) {
                for (int j = 0; j < shipMap[result][i].length; j++) {
                    if (knownBoard[i][j] != UNKNOWN) {
                        shipMap[result][i][j] = 0;
                    }
                }
            }
            sumShipMap();
            // calculate all possible new locations
            if (damageCounters[result] == shipSize) {
                for (int offset = -shipSize; offset < shipSize; offset++) {
                    int currentX = locX + offset;
                    int currentY = locY + offset;
                    if (currentX < 0 || currentX > 9 || currentY < 0 || currentY > 9) {
                        continue;
                    }
                    if (knownBoard[currentX][locY] == UNKNOWN) {
                        shipMap[result][currentX][locY] = shipSize - offset;
                    }
                    if (knownBoard[locX][currentY] == UNKNOWN) {
                        shipMap[result][locX][currentY] = shipSize - offset;
                    }
                }
            } else {
                // find other hit(s) starting from farthest from hit location
                for (int offset = shipSize; offset < 0; offset--) {
                    int xhigh = locX + offset, xlow = locX - offset,
                            yhigh = locY + offset, ylow = locY - offset;
                    // update maps for the given hit locations
                    if (knownBoard[xhigh][locY] == result) {
                        switch (shipSize) {
                            case 2:
                                System.err.println("PATROL BOAT WAS SUNK BUT THE HIT MOD WAS APPLIED");
                                break;
                            case 3: //size 3 ship
                                switch (offset) {
                                    case 1: //hit location is adjacent
                                        shipMap[result][locX - 1][locY] = 1;
                                        shipMap[result][locX + 2][locY] = 1;
                                        break;
                                    case 2: //hit location is separated by 1
                                        shipMap[result][locX + 1][locY] = 1;
                                        break;
                                }
                                break;
                            case 4:  //size 4 ship
                                switch (offset) {
                                    case 1: //hit location is adjacent
                                        shipMap[result][locX - 2][locY] = 1;
                                        shipMap[result][locX - 1][locY] = 2;
                                        shipMap[result][locX + 2][locY] = 2;
                                        shipMap[result][locX + 3][locY] = 2;
                                        break;
                                    case 2: //hit location is separated by 1
                                        shipMap[result][locX - 1][locY] = 1;
                                        shipMap[result][locX + 1][locY] = 2;
                                        shipMap[result][locX + 3][locY] = 1;
                                        break;
                                    case 3: //hit location is separated by 2
                                        shipMap[result][locX + 1][locY] = 1;
                                        shipMap[result][locX + 2][locY] = 2;
                                        break;
                                }
                                break;
                            case 5:
                                switch (offset) {
                                    case 1: //hit location is adjacent
                                        shipMap[result][locX - 3][locY] = 1;
                                        shipMap[result][locX - 2][locY] = 2;
                                        shipMap[result][locX - 1][locY] = 3;
                                        shipMap[result][locX + 2][locY] = 3;
                                        shipMap[result][locX + 3][locY] = 2;
                                        shipMap[result][locX + 4][locY] = 1;
                                        break;
                                    case 2: //hit location is separated by 1
                                        shipMap[result][locX - 2][locY] = 1;
                                        shipMap[result][locX - 1][locY] = 2;
                                        shipMap[result][locX + 1][locY] = 4;
                                        shipMap[result][locX + 3][locY] = 2;
                                        shipMap[result][locX + 4][locY] = 1;
                                        break;
                                    case 3: //hit location is separated by 2
                                        shipMap[result][locX - 1][locY] = 1;
                                        shipMap[result][locX + 1][locY] = 2;
                                        shipMap[result][locX + 2][locY] = 2;
                                        shipMap[result][locX + 4][locY] = 1;
                                        break;
                                    case 4: //hit location is separated by 3
                                        shipMap[result][locX + 1][locY] = 1;
                                        shipMap[result][locX + 2][locY] = 1;
                                        shipMap[result][locX + 3][locY] = 1;
                                        break;
                                }
                                break;
                        }
                    } else if (knownBoard[xlow][locY] == result) {
                        switch (shipSize) {
                            case 2:
                                System.err.println("PATROL BOAT WAS SUNK BUT THE HIT MOD WAS APPLIED");
                                break;
                            case 3: //size 3 ship
                                switch (offset) {
                                    case 1: //hit location is adjacent
                                        shipMap[result][locX + 1][locY] = 1;
                                        shipMap[result][locX - 2][locY] = 1;
                                        break;
                                    case 2: //hit location is separated by 1
                                        shipMap[result][locX - 1][locY] = 1;
                                        break;
                                }
                                break;
                            case 4:  //size 4 ship
                                switch (offset) {
                                    case 1: //hit location is adjacent
                                        shipMap[result][locX + 2][locY] = 1;
                                        shipMap[result][locX + 1][locY] = 2;
                                        shipMap[result][locX - 2][locY] = 2;
                                        shipMap[result][locX - 3][locY] = 2;
                                        break;
                                    case 2: //hit location is separated by 1
                                        shipMap[result][locX + 1][locY] = 1;
                                        shipMap[result][locX - 1][locY] = 2;
                                        shipMap[result][locX - 3][locY] = 1;
                                        break;
                                    case 3: //hit location is separated by 2
                                        shipMap[result][locX - 1][locY] = 1;
                                        shipMap[result][locX - 2][locY] = 2;
                                        break;
                                }
                                break;
                            case 5:
                                switch (offset) {
                                    case 1: //hit location is adjacent
                                        shipMap[result][locX + 3][locY] = 1;
                                        shipMap[result][locX + 2][locY] = 2;
                                        shipMap[result][locX + 1][locY] = 3;
                                        shipMap[result][locX - 2][locY] = 3;
                                        shipMap[result][locX - 3][locY] = 2;
                                        shipMap[result][locX - 4][locY] = 1;
                                        break;
                                    case 2: //hit location is separated by 1
                                        shipMap[result][locX + 2][locY] = 1;
                                        shipMap[result][locX + 1][locY] = 2;
                                        shipMap[result][locX - 1][locY] = 4;
                                        shipMap[result][locX - 3][locY] = 2;
                                        shipMap[result][locX - 4][locY] = 1;
                                        break;
                                    case 3: //hit location is separated by 2
                                        shipMap[result][locX + 1][locY] = 1;
                                        shipMap[result][locX - 1][locY] = 2;
                                        shipMap[result][locX - 2][locY] = 2;
                                        shipMap[result][locX - 4][locY] = 1;
                                        break;
                                    case 4: //hit location is separated by 3
                                        shipMap[result][locX - 1][locY] = 1;
                                        shipMap[result][locX - 2][locY] = 1;
                                        shipMap[result][locX - 3][locY] = 1;
                                        break;
                                }
                                break;
                        }
                    } else if (knownBoard[locX][yhigh] == result) {
                        switch (shipSize) {
                            case 2:
                                System.err.println("PATROL BOAT WAS SUNK BUT THE HIT MOD WAS APPLIED");
                                break;
                            case 3: //size 3 ship
                                switch (offset) {
                                    case 1: //hit location is adjacent
                                        shipMap[result][locX][locY - 1] = 1;
                                        shipMap[result][locX][locY + 2] = 1;
                                        break;
                                    case 2: //hit location is separated by 1
                                        shipMap[result][locX][locY + 1] = 1;
                                        break;
                                }
                                break;
                            case 4:  //size 4 ship
                                switch (offset) {
                                    case 1: //hit location is adjacent
                                        shipMap[result][locX][locY - 2] = 1;
                                        shipMap[result][locX][locY - 1] = 2;
                                        shipMap[result][locX][locY + 2] = 2;
                                        shipMap[result][locX][locY + 3] = 2;
                                        break;
                                    case 2: //hit location is separated by 1
                                        shipMap[result][locX][locY - 1] = 1;
                                        shipMap[result][locX][locY + 1] = 2;
                                        shipMap[result][locX][locY + 3] = 1;
                                        break;
                                    case 3: //hit location is separated by 2
                                        shipMap[result][locX][locY + 1] = 1;
                                        shipMap[result][locX][locY + 2] = 2;
                                        break;
                                }
                                break;
                            case 5:
                                switch (offset) {
                                    case 1: //hit location is adjacent
                                        shipMap[result][locX][locY - 3] = 1;
                                        shipMap[result][locX][locY - 2] = 2;
                                        shipMap[result][locX][locY - 1] = 3;
                                        shipMap[result][locX][locY + 2] = 3;
                                        shipMap[result][locX][locY + 3] = 2;
                                        shipMap[result][locX][locY + 4] = 1;
                                        break;
                                    case 2: //hit location is separated by 1
                                        shipMap[result][locX][locY - 2] = 1;
                                        shipMap[result][locX][locY - 1] = 2;
                                        shipMap[result][locX][locY + 1] = 4;
                                        shipMap[result][locX][locY + 3] = 2;
                                        shipMap[result][locX][locY + 4] = 1;
                                        break;
                                    case 3: //hit location is separated by 2
                                        shipMap[result][locX][locY - 1] = 1;
                                        shipMap[result][locX][locY + 1] = 2;
                                        shipMap[result][locX][locY + 2] = 2;
                                        shipMap[result][locX][locY + 4] = 1;
                                        break;
                                    case 4: //hit location is separated by 3
                                        shipMap[result][locX][locY + 1] = 1;
                                        shipMap[result][locX][locY + 2] = 1;
                                        shipMap[result][locX][locY + 3] = 1;
                                        break;
                                }
                                break;
                        }
                    } else if (knownBoard[locX][ylow] == result) {
                        switch (shipSize) {
                            case 2:
                                System.err.println("PATROL BOAT WAS SUNK BUT THE HIT MOD WAS APPLIED");
                                break;
                            case 3: //size 3 ship
                                switch (offset) {
                                    case 1: //hit location is adjacent
                                        shipMap[result][locX][locY + 1] = 1;
                                        shipMap[result][locX][locY - 2] = 1;
                                        break;
                                    case 2: //hit location is separated by 1
                                        shipMap[result][locX][locY - 1] = 1;
                                        break;
                                }
                                break;
                            case 4:  //size 4 ship
                                switch (offset) {
                                    case 1: //hit location is adjacent
                                        shipMap[result][locX][locY + 2] = 1;
                                        shipMap[result][locX][locY + 1] = 2;
                                        shipMap[result][locX][locY - 2] = 2;
                                        shipMap[result][locX][locY - 3] = 2;
                                        break;
                                    case 2: //hit location is separated by 1
                                        shipMap[result][locX][locY + 1] = 1;
                                        shipMap[result][locX][locY - 1] = 2;
                                        shipMap[result][locX][locY - 3] = 1;
                                        break;
                                    case 3: //hit location is separated by 2
                                        shipMap[result][locX][locY - 1] = 1;
                                        shipMap[result][locX][locY - 2] = 2;
                                        break;
                                }
                                break;
                            case 5:
                                switch (offset) {
                                    case 1: //hit location is adjacent
                                        shipMap[result][locX][locY + 3] = 1;
                                        shipMap[result][locX][locY + 2] = 2;
                                        shipMap[result][locX][locY + 1] = 3;
                                        shipMap[result][locX][locY - 2] = 3;
                                        shipMap[result][locX][locY - 3] = 2;
                                        shipMap[result][locX][locY - 4] = 1;
                                        break;
                                    case 2: //hit location is separated by 1
                                        shipMap[result][locX][locY + 2] = 1;
                                        shipMap[result][locX][locY + 1] = 2;
                                        shipMap[result][locX][locY - 1] = 4;
                                        shipMap[result][locX][locY - 3] = 2;
                                        shipMap[result][locX][locY - 4] = 1;
                                        break;
                                    case 3: //hit location is separated by 2
                                        shipMap[result][locX][locY + 1] = 1;
                                        shipMap[result][locX][locY - 1] = 2;
                                        shipMap[result][locX][locY - 2] = 2;
                                        shipMap[result][locX][locY - 4] = 1;
                                        break;
                                    case 4: //hit location is separated by 3
                                        shipMap[result][locX][locY - 1] = 1;
                                        shipMap[result][locX][locY - 2] = 1;
                                        shipMap[result][locX][locY - 3] = 1;
                                        break;
                                }
                                break;
                        }
                    }
                }
                for (int x = 0; x < knownBoard.length; x++) {
                    for (int y = 0; y < knownBoard[x].length; y++) {
                        if (knownBoard[x][y] != UNKNOWN) {
                            shipMap[result][x][y] = 0;
                        }
                    }
                }
            }
            /* calculate all possible new locations
             * 		keep any hit locations at zero!
             * 			use knownMap
             *
             * update all other ships by decrementing the hit locations
             */
        }
        if (result / SUNK_MODIFIER == 1) { // Sunk a ship!
            result -= SUNK_MODIFIER;
            knownBoard[locX][locY] = result;
            for (int i = 0; i < shipMap[result].length; i++) {
                for (int j = 0; j < shipMap[result][i].length; j++) {
                    shipMap[result][i][j] = 0;
                }
            }
        }
        recalculateShipMap(new Coordinate(locX, locY), result);
        sumShipMap();
        //printTotalProbabilityMap(); // Commented out by Seth Dutter
    }

    /**
     * recalculate a ship map at all locations affected by the location given
     * ONLY CALL THIS IF THE COORDINATE IS NOT UNKNOWN!!!
     *
     * @param coord a Coordinate that was attacked
     * @param type the type of ship to not change (-1 to change all ships)
     */
    private void recalculateShipMap(Coordinate coord, int typeNoTouch) {
        for (int i = 1; typeNoTouch != AIRCRAFT_CARRIER && i < AIRCRAFT_CARRIER_LENGTH; i++) {
            getPossiblePositions(new Coordinate(coord.getX() + i, coord.getY()), AIRCRAFT_CARRIER);
            getPossiblePositions(new Coordinate(coord.getX() - i, coord.getY()), AIRCRAFT_CARRIER);
            getPossiblePositions(new Coordinate(coord.getX(), coord.getY() + i), AIRCRAFT_CARRIER);
            getPossiblePositions(new Coordinate(coord.getX(), coord.getY() - i), AIRCRAFT_CARRIER);
        }
        for (int i = 1; typeNoTouch != BATTLESHIP && i < BATTLESHIP_LENGTH; i++) {
            getPossiblePositions(new Coordinate(coord.getX() + i, coord.getY()), BATTLESHIP);
            getPossiblePositions(new Coordinate(coord.getX() - i, coord.getY()), BATTLESHIP);
            getPossiblePositions(new Coordinate(coord.getX(), coord.getY() + i), BATTLESHIP);
            getPossiblePositions(new Coordinate(coord.getX(), coord.getY() - i), BATTLESHIP);
        }
        for (int i = 1; typeNoTouch != SUBMARINE && i < SUBMARINE_LENGTH; i++) {
            getPossiblePositions(new Coordinate(coord.getX() + i, coord.getY()), SUBMARINE);
            getPossiblePositions(new Coordinate(coord.getX() - i, coord.getY()), SUBMARINE);
            getPossiblePositions(new Coordinate(coord.getX(), coord.getY() + i), SUBMARINE);
            getPossiblePositions(new Coordinate(coord.getX(), coord.getY() - i), SUBMARINE);
        }
        for (int i = 1; typeNoTouch != DESTROYER && i < DESTROYER_LENGTH; i++) {
            getPossiblePositions(new Coordinate(coord.getX() + i, coord.getY()), DESTROYER);
            getPossiblePositions(new Coordinate(coord.getX() - i, coord.getY()), DESTROYER);
            getPossiblePositions(new Coordinate(coord.getX(), coord.getY() + i), DESTROYER);
            getPossiblePositions(new Coordinate(coord.getX(), coord.getY() - i), DESTROYER);
        }
        for (int i = 1; typeNoTouch != PATROL_BOAT && i < PATROL_BOAT_LENGTH; i++) {
            getPossiblePositions(new Coordinate(coord.getX() + i, coord.getY()), PATROL_BOAT);
            getPossiblePositions(new Coordinate(coord.getX() - i, coord.getY()), PATROL_BOAT);
            getPossiblePositions(new Coordinate(coord.getX(), coord.getY() + i), PATROL_BOAT);
            getPossiblePositions(new Coordinate(coord.getX(), coord.getY() - i), PATROL_BOAT);
        }
    }

    private void getPossiblePositions(Coordinate coord, int type) {
        if (coord.getX() < 0 || coord.getX() > 9 || coord.getY() < 0 || coord.getY() > 9) {
            return;
        }
        if (shipMap[type][coord.getX()][coord.getY()] != 0) {
            int shipSize = getShipSize(type);
            shipMap[type][coord.getX()][coord.getY()] = 0;
            for (int offset = 0; offset < shipSize; offset++) {
                for (int orent = HORIZONTAL; orent <= VERTICAL; orent++) {
                    Ship testShip = new Ship(new Coordinate(coord.getX() - offset, coord.getY()), orent, type);
                    if (testShip.isValid()) {
                        boolean testShipHit = false;
                        switch (orent) {
                            case HORIZONTAL:
                                testShipHit = false;
                                for (int i = 0; i < shipSize; i++) {
                                    try {
                                        if (shipMap[type][coord.getX() - offset + i][coord.getY()] == 0) {
                                            testShipHit = true;
                                        }
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        continue;
                                    }
                                }
                                if (!testShipHit) {
                                    shipMap[type][coord.getX()][coord.getY()]++;
                                }
                                break;
                            case VERTICAL:
                                testShipHit = false;
                                for (int i = 0; i < shipSize; i++) {
                                    try {
                                        if (shipMap[type][coord.getX()][coord.getY() - offset + i] == 0) {
                                            testShipHit = true;
                                        }
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        continue;
                                    }
                                }
                                if (!testShipHit) {
                                    shipMap[type][coord.getX()][coord.getY()]++;
                                }
                                break;
                        }
                    }
                }
            }
        }
    }

    /**
     * @param coord The spot on the board where your opponent just attacked.
     */
    @Override
    public void opponentAttack(Coordinate coord) {
        // Add code here to process or record opponent attacks
    }

    /**
     *
     * @param result A code from Constants that will equal WON or LOST.
     */
    @Override
    public void resultOfGame(int result) {
        // Add code here to process the result of a game
    }

    private void placeShips() {
        HashMap<Ship, int[]> ships = new HashMap<>();
        int[] shipInfo;
        for (int i = 4; i > -1; i--) {
            while (ships.size() < (5 - i) * 4) {
                shipInfo = new int[3];
                shipInfo[0] = generator.nextInt(10);
                shipInfo[1] = generator.nextInt(10);
                shipInfo[2] = generator.nextInt(2);
                Ship testShip = new Ship(new Coordinate(shipInfo[0],
                        shipInfo[1]), shipInfo[2], i);
                while (!testShip.isValid()) {
                    shipInfo[0] = generator.nextInt(10);
                    shipInfo[1] = generator.nextInt(10);
                    shipInfo[2] = generator.nextInt(2);
                    testShip = new Ship(
                            new Coordinate(shipInfo[0], shipInfo[1]),
                            shipInfo[2], i);
                }
                boolean doesIntersect = false;
                for (Ship s : ships.keySet()) {
                    if (s.intersectsShip(testShip)) {
                        doesIntersect = true;
                    }
                }
                if (!doesIntersect) {
                    ships.put(testShip, shipInfo);
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            shipInfo = getRandomShip(ships, i);
            myFleet.placeShip(shipInfo[0], shipInfo[1], shipInfo[2], i);
        }
    }

    private int[] getRandomShip(HashMap<Ship, int[]> ships, int model) {
        int random = generator.nextInt(ships.size());
        int counter = 1;
        for (Ship s : ships.keySet()) {
            if (random == counter && s.getModel() == model) {
                return ships.get(s);
            }
            counter++;
            if (counter > random) {
                return getRandomShip(ships, model);
            }
        }
        return new int[3];
    }

    private void generateShipMap() {
        for (int i = 0; i < shipMap.length; i++) {
            for (int j = 0; j < shipMap[i].length; j++) {
                for (int k = 0; k < shipMap[i][j].length; k++) {
                    shipMap[i][j][k] = 0;
                }
            }
        }
        Ship tempShip;
        for (int type = PATROL_BOAT; type <= AIRCRAFT_CARRIER; type++) {
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    tempShip = new Ship(new Coordinate(x, y), VERTICAL, type);
                    if (tempShip.isValid()) {
                        boolean isValid = true;
                        for (int i = 0; i < getShipSize(type); i++) {
                            if (knownBoard[x][y + i] != UNKNOWN) {
                                isValid = false;
                                i += 10; // end the looping
                            }
                        }
                        if (isValid) {
                            for (int i = 0; i < getShipSize(type); i++) {
                                shipMap[type][x][y + i]++;
                            }
                        }
                    }
                    tempShip = new Ship(new Coordinate(x, y), HORIZONTAL, type);
                    if (tempShip.isValid()) {
                        boolean isValid = true;
                        for (int i = 0; i < getShipSize(type); i++) {
                            if (knownBoard[x + i][y] != UNKNOWN) {
                                isValid = false;
                                i += 10; // end the looping
                            }
                        }
                        if (isValid) {
                            for (int i = 0; i < getShipSize(type); i++) {
                                shipMap[type][x + i][y]++;
                            }
                        }
                    }

                }
            }
        }
        sumShipMap();
    }

    private void sumShipMap() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                shipMap[5][i][j] = shipMap[0][i][j]
                        + shipMap[1][i][j]
                        + shipMap[2][i][j]
                        + shipMap[3][i][j]
                        + shipMap[4][i][j];
            }
        }
    }

    private int getShipSize(int type) {
        switch (type) {
            case AIRCRAFT_CARRIER:
                return AIRCRAFT_CARRIER_LENGTH;
            case BATTLESHIP:
                return BATTLESHIP_LENGTH;
            case DESTROYER:
                return DESTROYER_LENGTH;
            case PATROL_BOAT:
                return PATROL_BOAT_LENGTH;
            case SUBMARINE:
                return SUBMARINE_LENGTH;
            default:
                return 0;
        }
    }
}