
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Kaptain Kurtis, combination of multiple previous AI's
 *
 * @author Andrew Boreen, Richard Schibly
 */
public class KaptainKurtis implements Captain {

    private HashMap<Integer, Integer> shipLengths;
    private Random generator;
    private Fleet myFleet;
    private Map<String, Opponent> Opponents;
    private String currentOpponentName = "TheKrispyKreme";
    private int turn;
    private ArrayList<Integer> opponentsRemainingShips;
    private Coordinate lastAttack;
    private Grid misses;
    private Grid alreadyAttacked;
    private HashMap<Integer, ArrayList<Coordinate>> opponentsDamagedShips;
    //KURTIS
    private int matchNumber = 0;
    private boolean[][] myShips = new boolean[10][10];
    private Random rGen;
    private int[] shipLength = {2, 3, 3, 4, 5};
    private int[][] theirShots = new int[10][10];

    private static class Segment {

        public int start;
        public int length = 0;

        Segment(int start) {
            this.start = start;
        }
    }

    private static class Opponent {

        public int gamesPlayed = 0;
        public int gamesWon = 0;
        public boolean useAltStrategy = false;
        public ArrayList<Coordinate> altStratCoordPriorities;
        public Grid fleetLayouts;
        public Grid aimLayout;
        public Grid myPersona;
        public Grid myFleetPlacement;
        public int[] survivors;
        public String OpponentName;

        Opponent(String name) {
            OpponentName = name;
            altStratCoordPriorities = new ArrayList<Coordinate>();
            fleetLayouts = new Grid();
            aimLayout = new Grid();
            myPersona = new Grid();
            myFleetPlacement = new Grid();
            survivors = new int[]{0, 0, 0, 0, 0};
        }
    }

    public static class Grid {

        private int[][] values;

        Grid() {
            values = new int[10][10];
            this.zeroGrid();
        }

        Grid(int[][] vals) {
            this.values = vals;
        }

        public int getValue(Coordinate coord) {
            return values[coord.getX()][coord.getY()];
        }

        public int getValue(int x, int y) {
            return values[x][y];
        }

        public void setValue(Coordinate coord, int value) {
            this.values[coord.getX()][coord.getY()] = value;
        }

        public void addValue(Coordinate coord, int value) {
            this.values[coord.getX()][coord.getY()] += value;
        }

        public void increment(Coordinate coord) {
            this.values[coord.getX()][coord.getY()]++;
        }

        public int[][] getArray() {
            return values;
        }

        public void zeroGrid() {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    values[i][j] = 0;
                }
            }
        }

        public void addGrid(Grid a) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    values[i][j] += a.getValue(new Coordinate(i, j));
                }
            }
        }

        public void multiplyGrid(Grid a) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    values[i][j] += a.getValue(new Coordinate(i, j));
                }
            }
        }

        public void cancelGridSquares(Grid a) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (a.getValue(new Coordinate(i, j)) != 0) {
                        values[i][j] = 0;
                    }
                }
            }
        }

        public int getMax() {
            int r = 0;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (values[i][j] > r) {
                        r = values[i][j];
                    }
                }
            }
            return r;
        }

        public int getMin() {
            int r = this.values[0][0];
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (this.values[i][j] < r) {
                        r = this.values[i][j];
                    }
                }
            }
            return r;
        }

        public Grid getNormalizedGrid() {
            return this.getNormalizedGrid(0, 100);
        }

        public Grid getNormalizedGrid(int rangeMin, int rangeMax) {
            float max = (float) getMax();
            float min = (float) getMin();
            Grid r = new Grid();

            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    r.setValue(
                            new Coordinate(i, j),
                            rangeMin
                            + Math.round((values[i][j] - min)
                            / (max - min)
                            * (rangeMax - rangeMin)));
                }
            }
            return r;
        }
    }

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {

        currentOpponentName = opponent;
        generator = new Random();
        myFleet = new Fleet();
        turn = 0;
        misses = new Grid();
        alreadyAttacked = new Grid();
        opponentsRemainingShips = new ArrayList<Integer>();
        opponentsRemainingShips.add(PATROL_BOAT);
        opponentsRemainingShips.add(DESTROYER);
        opponentsRemainingShips.add(SUBMARINE);
        opponentsRemainingShips.add(BATTLESHIP);
        opponentsRemainingShips.add(AIRCRAFT_CARRIER);
        opponentsDamagedShips = new HashMap<Integer, ArrayList<Coordinate>>();

        if (Opponents == null) {
            Opponents = new HashMap<String, Opponent>();
        }
        if (!Opponents.containsKey(opponent)) {
            Opponents.put(opponent, new Opponent(opponent));
        }

        Opponent o = Opponents.get(currentOpponentName);
        Grid persona = new Grid();
        persona.addGrid(o.myPersona);

        // determine if we need to use the alternate strategy
        // the rules for doing so are somewhat arbitrary

        if (o.gamesPlayed == 1000) {
            // check if opponent is winning the match
            if ((double) o.gamesWon / 1000 > 0.5) {
                int sum = 0;
                for (int n : o.survivors) {
                    sum += n;
                }
                // if they won the majority of their games with less than one
                // ship remaining
                if (((double) sum - o.gamesWon) / o.gamesWon < 0.2) {
                    // and if that ship was the patrol boat
                    if ((double) o.survivors[PATROL_BOAT] / o.gamesWon > 0.9) {
                        // we assume they are actively hiding their patrol boat,
                        // therefore
                        // use alternate strategy for when only a patrol boat
                        // remains
                        o.useAltStrategy = true;
                    }

                }
            }
        }

        if (shipLengths == null) {
            shipLengths = new HashMap<Integer, Integer>();
            shipLengths.put(PATROL_BOAT, PATROL_BOAT_LENGTH);
            shipLengths.put(DESTROYER, DESTROYER_LENGTH);
            shipLengths.put(SUBMARINE, SUBMARINE_LENGTH);
            shipLengths.put(BATTLESHIP, BATTLESHIP_LENGTH);
            shipLengths.put(AIRCRAFT_CARRIER, AIRCRAFT_CARRIER_LENGTH);
        }

        this.rGen = new Random();
        for (int shipType = 0; shipType < 5; shipType++) {
            boolean placed = false;
            // if we don't have enough data on the opponent, randomly distribute
            // the ships
            if (this.matchNumber <= (numMatches / 100)) {
                while (!placed) {
                    int baseCoord = this.rGen.nextInt(100);
                    placed = true;
                    if (!this.myFleet.placeShip(new Coordinate(baseCoord % 10,
                            baseCoord / 10 - this.shipLength[shipType]),
                            VERTICAL, shipType)) {
                        if (!this.myFleet.placeShip(new Coordinate(baseCoord
                                % 10 - this.shipLength[shipType],
                                baseCoord / 10), HORIZONTAL, shipType)) {
                            placed = false;
                        }
                    }
                }
            } // else pot the ships where they have shot least
            else {
                int[] placement = leastShotPlace(this.shipLength[shipType]);
                this.myFleet.placeShip(placement[0], placement[1],
                        placement[2], shipType);
            }
        }

    }

    private int[] leastShotPlace(int shipLen) {
        int bestRect = -1;
        Coordinate rectCoord = null;
        int orientation = 1;
        // try best verticle rectangle
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j <= (10 - shipLen); j++) {
                int testRect = 0;
                boolean testOk = true;
                for (int k = shipLen - 1; k >= 0; k--) {

                    if (this.myShips[i][j + k]) {
                        testOk = false;
                        continue;
                    }
                    testRect = +this.theirShots[i][j + k];
                }
                if ((bestRect == -1 || testRect < bestRect) && testOk) {
                    bestRect = testRect;
                    rectCoord = new Coordinate(i, j);
                }
            }
        }
        // try best horizontal rectangle
        for (int i = 0; i <= (10 - shipLen); i++) {
            for (int j = 0; j < 10; j++) {
                int testRect = 0;
                boolean testOk = true;
                for (int k = shipLen - 1; k >= 0; k--) {
                    if (this.myShips[i + k][j]) {
                        testOk = false;
                        continue;
                    }
                    testRect = +this.theirShots[i + k][j];
                }
                if (testRect < bestRect && testOk) {
                    bestRect = testRect;
                    rectCoord = new Coordinate(i, j);
                    orientation = 0;
                }
            }
        }

        int[] ret = {rectCoord.getX(), rectCoord.getY(), orientation};
        // System.out.println(rectCoord.getX()+" "+rectCoord.getY()+" - "+orientation);
        if (orientation == 0) {
            for (int k = shipLen - 1; k >= 0; k--) {
                this.myShips[rectCoord.getX() + k][rectCoord.getY()] = true;
            }
        } else {
            for (int k = shipLen - 1; k >= 0; k--) {
                this.myShips[rectCoord.getX()][rectCoord.getY() + k] = true;
            }

        }
        return ret;
    }

    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    @Override
    public Coordinate makeAttack() {
        // determine the most common common squares by taking into account all
        // possible permutations
        Grid distribution = calculatePermutations();
        Coordinate thisAttack = null;

        if (opponentsDamagedShips.isEmpty()) {
            // there are no damaged ships, so we need to hunt for them
            // check if we should use alternate strategy
            if (opponentsRemainingShips.size() == 1
                    && opponentsRemainingShips.get(0) == PATROL_BOAT
                    && Opponents.get(currentOpponentName).useAltStrategy) {

                for (Coordinate c : Opponents.get(currentOpponentName).altStratCoordPriorities) {
                    if (thisAttack == null && distribution.getValue(c) != 0) {
                        thisAttack = c;
                    }
                }

            }
            if (thisAttack == null) {
                // get all the squares with the highest value
                ArrayList<Coordinate> targets = new ArrayList<Coordinate>();
                int max = distribution.getMax();

                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (distribution.getValue(i, j) == max) {
                            targets.add(new Coordinate(i, j));
                        }
                    }
                }
                // target a random coordinate if there is more than one
                thisAttack = targets.get(generator.nextInt(targets.size()));
            }
        } else {
            // we have found a ship! lets attack it
            // make a list of all adjacent squares to the ones i've already hit
            ArrayList<Coordinate> targets = new ArrayList<Coordinate>();
            for (ArrayList<Coordinate> coords : opponentsDamagedShips.values()) {
                for (Coordinate coord : coords) {

                    if (coord.getY() - 1 >= 0) {
                        Coordinate c = new Coordinate(coord.getX(),
                                coord.getY() - 1);
                        if (alreadyAttacked.getValue(c) == 0) {
                            targets.add(c);
                        }
                    }
                    if (coord.getY() + 1 < 10) {
                        Coordinate c = new Coordinate(coord.getX(),
                                coord.getY() + 1);
                        if (alreadyAttacked.getValue(c) == 0) {
                            targets.add(c);
                        }
                    }
                    if (coord.getX() - 1 >= 0) {
                        Coordinate c = new Coordinate(coord.getX() - 1,
                                coord.getY());
                        if (alreadyAttacked.getValue(c) == 0) {
                            targets.add(c);
                        }
                    }
                    if (coord.getX() + 1 < 10) {
                        Coordinate c = new Coordinate(coord.getX() + 1,
                                coord.getY());
                        if (alreadyAttacked.getValue(c) == 0) {
                            targets.add(c);
                        }
                    }
                }
            }
            for (ArrayList<Coordinate> coords : opponentsDamagedShips.values()) {
                // prioritize coords that form a straight line
                if (coords.size() >= 2) {
                    if (coords.get(0).getX() == coords.get(1).getX()) {
                        for (Coordinate coord : targets) {
                            if (coord.getX() == coords.get(0).getX()) {
                                distribution.setValue(coord,
                                        distribution.getValue(coord) + 100);
                            }
                        }
                    } else if (coords.get(0).getY() == coords.get(1).getY()) {
                        for (Coordinate coord : targets) {
                            if (coord.getY() == coords.get(0).getY()) {
                                distribution.setValue(coord,
                                        distribution.getValue(coord) + 100);
                            }
                        }
                    }
                }
            }
            // target the most probable square from that list
            Coordinate mostProbable = targets.get(0);
            int max = distribution.getValue(mostProbable);
            for (Coordinate coord : targets) {
                if (distribution.getValue(coord) > max) {
                    mostProbable = coord;
                    max = distribution.getValue(coord);
                }
            }
            thisAttack = mostProbable;
        }
        alreadyAttacked.increment(thisAttack);
        Opponents.get(currentOpponentName).myPersona.increment(thisAttack);

        lastAttack = thisAttack;
        return lastAttack;
    }

    @Override
    public void resultOfAttack(int result) {

        if (result == MISS) {
            misses.increment(lastAttack);
        }
        if (result / HIT_MODIFIER > 0) { // it is either a hit or a sunk ship
            Opponents.get(currentOpponentName).fleetLayouts
                    .increment(lastAttack);
            int shipId = result % HIT_MODIFIER;
            if (!opponentsDamagedShips.containsKey(shipId)) {
                opponentsDamagedShips.put(shipId, new ArrayList<Coordinate>());
            }
            opponentsDamagedShips.get(shipId).add(lastAttack);

            if (result / SUNK_MODIFIER > 0) {
                // if we sunk a ship, remove it from the hashmaps and set all
                // the
                // squares the ship occupied to misses for the permutation
                // calculation
                opponentsRemainingShips.remove(new Integer(shipId));
                misses.increment(lastAttack);
                for (Coordinate c : opponentsDamagedShips.get(shipId)) {
                    misses.increment(c);
                }
                opponentsDamagedShips.remove(shipId);
            }
        }
    }

    @Override
    public void opponentAttack(Coordinate coord) {
        this.theirShots[coord.getX()][coord.getY()]++;

        turn++;
        // keep track of what my opponent knows about me
        if (myFleet.isShipAt(coord)) {
            Opponents.get(currentOpponentName).myFleetPlacement
                    .increment(coord);
        }
        int v = Opponents.get(currentOpponentName).aimLayout.getValue(coord);
        Opponents.get(currentOpponentName).aimLayout.setValue(coord,
                (60 - turn >= 0) ? v + ((60 - turn)) : v);

    }

    @Override
    public void resultOfGame(int result) {
        if (result == LOST) {
            Opponents.get(currentOpponentName).gamesWon++;
        } else {
            // we won, so remove the last ship from the list of remaining ships
            opponentsRemainingShips.clear();
        }
        // if we lost(there's remaining ships), collect data about how what
        // ships evaded us
        for (int shipId : opponentsRemainingShips) {
            Opponents.get(currentOpponentName).survivors[shipId]++;
        }
        Opponents.get(currentOpponentName).gamesPlayed++;
    }

    private Grid calculatePermutations() {
        // the core of my strategy

        // we start with a grid of zeroes
        int[][] vals = new int[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                vals[i][j] = 0;
            }
        }

        // iterate through each column (for vertical ships)
        for (int column = 0; column < 10; column++) {
            // divide each column into segments of empty squares
            ArrayList<Segment> vsegs = new ArrayList<Segment>();
            for (int row = 0; row < 10; row++) {
                if (misses.getValue(new Coordinate(column, row)) == 0) {
                    Segment seg = new Segment(row);
                    for (seg.length = 0; row < 10
                            && misses.getValue(new Coordinate(column, row)) == 0; row++) {
                        seg.length++;
                    }
                    vsegs.add(seg);
                }
            }
            // with each segment for this column, calculate the permutations for
            // each ship

            for (Segment seg : vsegs) {
                for (int shipId : opponentsRemainingShips) {
                    int shipLength = shipLengths.get(new Integer(shipId));
                    int[] segData = new int[seg.length];
                    if (seg.length >= shipLength) {
                        for (int i = 0; i < seg.length; i++) {
                            segData[i] = shipLength;
                        }
                        for (int i = 0; i < shipLength - 1; i++) {
                            segData[i] -= (shipLength - (i + 1));
                            segData[(seg.length - 1) - i] -= (shipLength - i - 1);
                        }
                    }
                    // add the segment's permutation data to the total
                    for (int i = 0; i < seg.length; i++) {
                        vals[column][seg.start + i] += segData[i];
                    }
                }
            }
        }
        // then iterate through each row (for horizontal ships)
        for (int row = 0; row < 10; row++) {
            // divide each row into segments of empty squares
            ArrayList<Segment> hsegs = new ArrayList<Segment>();
            for (int column = 0; column < 10; column++) {
                if (misses.getValue(new Coordinate(column, row)) == 0) {
                    Segment seg = new Segment(column);
                    for (seg.length = 0; column < 10
                            && misses.getValue(new Coordinate(column, row)) == 0; column++) {
                        seg.length++;
                    }
                    hsegs.add(seg);
                }
            }

            // with each segment for this row, calculate the permutations for
            // each ship
            for (Segment seg : hsegs) {
                for (int shipId : opponentsRemainingShips) {
                    int shipLength = shipLengths.get(new Integer(shipId));
                    int[] segData = new int[seg.length];
                    if (seg.length >= shipLength) {
                        for (int i = 0; i < seg.length; i++) {
                            segData[i] = shipLength;
                        }
                        for (int i = 0; i < shipLength - 1; i++) {
                            segData[i] -= (shipLength - (i + 1));
                            segData[(seg.length - 1) - i] -= (shipLength - i - 1);
                        }
                    }
                    // add the segment's permutation data to the total
                    for (int i = 0; i < seg.length; i++) {
                        vals[seg.start + i][row] += segData[i];
                    }
                }
            }
        }
        return new Grid(vals);
    }
}
