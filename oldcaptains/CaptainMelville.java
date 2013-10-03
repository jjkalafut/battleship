
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;

/**
 * Captain Melville There is a wisdom that is woe; but there is a woe that is
 * madness. Have a Super-Fantastic Day!!!
 */
public class CaptainMelville implements Captain {

    boolean LOG = false;

    int id(int x, int y) {
        return y * 10 + x;
    }

    int id(Coordinate c) {
        return id(c.getX(), c.getY());
    }

    int getX(int id) {
        return id % 10;
    }

    int getY(int id) {
        return id / 10;
    }

    class ValueComparator implements Comparator<Integer> {

        Map<Integer, Long> base;
        boolean lowToHigh;

        public ValueComparator(Map<Integer, Long> base, boolean lowToHigh) {
            this.base = base;
            this.lowToHigh = lowToHigh;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(Integer a, Integer b) {
            long compare = lowToHigh ? base.get(a) - base.get(b) : base.get(b) - base.get(a);
            if (compare > 0) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private class Opponent {

        int wins;
        int losses;
        double mavg;
        boolean sampling;
        HashMap<Integer, Long> attackHistory;
        HashMap<Integer, Long> placementHistory;

        public Opponent() {
            wins = 0;
            losses = 0;
            mavg = 0.0;
            sampling = true;
            attackHistory = new HashMap<Integer, Long>();
            placementHistory = new HashMap<Integer, Long>();
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    attackHistory.put(id(x, y), (long) 0);
                    placementHistory.put(id(x, y), (long) 0);
                }
            }
        }

        public void recordPlacement(Coordinate l) {
            long v = placementHistory.get(id(l));
            v++;
            placementHistory.put(id(l), v);
        }

        public void recordAttack(Coordinate l) {
            long v = attackHistory.get(id(l));
            v++;
            attackHistory.put(id(l), v);
        }

        public void result(int result) {
            if (result == WON) {
                wins++;
            } else {
                losses++;
            }
            mavg = ((wins + losses) == 1) ? result : mavg + ((result - mavg) / (wins + losses));
        }
    }
    Random generator;
    Fleet myFleet;
    HashMap<String, Opponent> data = new HashMap<String, Opponent>();
    ArrayList<Integer> myAttacks = new ArrayList<Integer>();
    ArrayList<Integer> diagAttacks = new ArrayList<Integer>();
    Opponent currentOpponent;
    Coordinate lastAttack;
    int lastHitShip;
    Stack<Integer> attackStack = new Stack<Integer>();
    Object[] enemyShips = new Object[5];
    int[] diag1 = {44, 55, 33, 66, 22, 77, 11, 88, 99, 00};
    int[] diag2 = {45, 54, 36, 63, 27, 72, 18, 81, 90, 9};
    int[] diag3 = {41, 61, 32, 52, 30, 50};
    int[] diag4 = {38, 58, 47, 67, 49, 69};
    int[] diag5 = {13, 15, 24, 26, 3, 6};
    int[] diag6 = {84, 86, 73, 75, 93, 95};
    int[] diag3b = {42, 51, 60, 52, 41, 30};
    int[] diag4b = {47, 58, 69, 57, 48, 39};
    int[] diag5b = {25, 14, 3, 24, 15, 6};
    int[] diag6b = {75, 84, 93, 74, 85, 96};
    long adaptiveAttackCount = 0;

    // Called before each game to reset your ship locations.
    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
        generator = new Random();
        myFleet = new Fleet();

        myAttacks.clear();
        attackStack.clear();
        diagAttacks.clear();

        for (int i = 0; i < diag1.length; i++) {
            diagAttacks.add(diag1[i]);
            diagAttacks.add(diag2[i]);
        }

        if (generator.nextBoolean()) {
            for (int i = 0; i < diag3.length; i++) {
                diagAttacks.add(diag3[i]);
                diagAttacks.add(diag4[i]);
                diagAttacks.add(diag5[i]);
                diagAttacks.add(diag6[i]);
            }
        } else {
            for (int i = 0; i < diag3.length; i++) {
                diagAttacks.add(diag3b[i]);
                diagAttacks.add(diag4b[i]);
                diagAttacks.add(diag5b[i]);
                diagAttacks.add(diag6b[i]);
            }
        }

        for (int i = 0; i < 5; i++) {
            enemyShips[i] = new ArrayList<Coordinate>();
        }

        lastHitShip = -1;

        currentOpponent = data.get(opponent);
        if (currentOpponent == null) {
            currentOpponent = new Opponent();
            data.put(opponent, currentOpponent);
        }

        if (currentOpponent.wins + currentOpponent.losses > 100) {
            currentOpponent.sampling = false;
        }

        // Sampling, place in random
        if (currentOpponent.sampling) {
            // hide the patrolboat on the outer ring first.
            int x = 0;
            int y = 0;
            do {
                do {
                    x = generator.nextInt(10);
                } while (x % 10 < 8 && x % 10 > 1);
                do {
                    y = generator.nextInt(10);
                } while (y / 10 < 8 && y / 10 > 1);
            } while (x == 9 && y == 9);
            while (!myFleet.placeShip(x, y, generator.nextInt(2), PATROL_BOAT));
            for (int i = 1; i < 5; i++) {
                while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), i)) ;
            }
        } else {
            // Place the 2-3 length boats in other locations that are attacked the least by this opponent
            TreeMap<Integer, Long> sortedMap;
            sortedMap = scorePlacements(PATROL_BOAT);
            placeShip(sortedMap, PATROL_BOAT);
            sortedMap = scorePlacements(DESTROYER);
            placeShip(sortedMap, DESTROYER);
            sortedMap = scorePlacements(SUBMARINE);
            placeShip(sortedMap, SUBMARINE);

            // Fubar the big ones
            for (int i = 3; i < 5; i++) {
                while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), i)) ;
            }

        }

    }

    private TreeMap<Integer, Long> scorePlacements(int model) {
        TreeMap<Integer, Long> map = new TreeMap<Integer, Long>();
        Ship ship;
        for (int i = 0; i < 100; i++) {
            // vertical
            Coordinate c = new Coordinate(getX(i), getY(i));
            long score = 0;
            ship = new Ship(c, HORIZONTAL, model);
            if (ship.isValid()) {
                for (int j = 0; j < ship.length; j++) {
                    int id = id(c) + j;
                    score += currentOpponent.attackHistory.get(id);
                }
                map.put(i, score / ship.length); // average score across occuping spots
            } else {
                map.put(i, Long.MAX_VALUE);
            }

            ship = new Ship(c, VERTICAL, model);
            if (ship.isValid()) {
                for (int j = 0; j < ship.length; j++) {
                    int id = id(c) + j * 10;
                    score += currentOpponent.attackHistory.get(id);
                }
                map.put(i + 100, score / ship.length); // average score across occuping spots
            } else {
                map.put(i + 100, Long.MAX_VALUE);
            }

        }

        ValueComparator bvc = new ValueComparator(map, false);
        TreeMap<Integer, Long> sortedMap = new TreeMap<Integer, Long>(bvc);
        sortedMap.putAll(map);
        return sortedMap;
    }

    private void placeShip(TreeMap<Integer, Long> sortedMap, int model) {
        for (Map.Entry<Integer, Long> entry : sortedMap.entrySet()) {
            Integer key = entry.getKey();
            int direction = HORIZONTAL;
            if (key >= 100) {
                direction = VERTICAL;
                key -= 100;
            }
            int x = getX(key);
            int y = getY(key);

            // make sure the patrol boat remains on the outer rim
            if (model == PATROL_BOAT) {
                if (x % 10 < 8 && x % 10 > 1) {
                    continue;
                }
                if (y / 10 < 8 && y / 10 > 1) {
                    continue;
                }
            }
            if (myFleet.placeShip(x, y, direction, model)) {
                break;
            }
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

        // general searching...
        for (int s = 0; s < 5; s++) {
            if (enemyShips[s] != null) {
                ArrayList<Coordinate> hitShip = (ArrayList<Coordinate>) enemyShips[s];
                if (hitShip.size() > 1) {

                    // two hits, draw a line and figure out the other spots
                    Log("Multiple hits on ship " + s + ", finisher attack: " + hitShip);
                    Coordinate hit1 = hitShip.get(0);
                    Coordinate hit2 = hitShip.get(1);
                    attackStack.clear();
                    if (HORIZONTAL == compareHits(hit1, hit2)) {
                        int left = hit1.getX();
                        for (Coordinate h : hitShip) {
                            if (h.getX() < left) {
                                left = h.getX();
                            }
                        }
                        left--;
                        int right = hit1.getX();
                        for (Coordinate h : hitShip) {
                            if (h.getX() > right) {
                                right = h.getX();
                            }
                        }
                        right++;

                        for (int x = left; x <= right; x++) {
                            attackStack.push(id(x, hit1.getY()));
                        }
                    } else {
                        int up = hit1.getY();
                        for (Coordinate h : hitShip) {
                            if (h.getY() < up) {
                                up = h.getY();
                            }
                        }
                        up--;
                        int down = hit1.getY();
                        for (Coordinate h : hitShip) {
                            if (h.getY() > down) {
                                down = h.getY();
                            }
                        }
                        down++;
                        for (int y = up; y <= down; y++) {
                            attackStack.push(id(hit1.getX(), y));
                        }
                    }
                    while (!tryAttack(attackStack.pop()));

                    if (lastAttack != null) {
                        return lastAttack;
                    }

                } else if (hitShip.size() > 0) {
                    // if you just got a hit, localize the search
                    Log("1 hit on ship " + s + ":" + hitShip);
                    int k = id(hitShip.get(0));
                    int[] dir = {-10, -1, 1, 10}; // try left/right/top/bottom
                    for (int x : dir) {
                        attackStack.push(k + x);
                    }
                    while (!tryAttack(attackStack.pop()));

                    if (lastAttack != null) {
                        return lastAttack;
                    }
                }

            }
        }

        // Wait for many more matches before attempting adaptive attack patterns
        if (currentOpponent.wins + currentOpponent.losses > 5000) {
            Log("Adaptive Attacking");
            // calculate the average
            long total = 0;
            for (Integer k : currentOpponent.placementHistory.keySet()) {
                total += currentOpponent.placementHistory.get(k);
            }
            double avg = 1.0 * total / currentOpponent.placementHistory.size();

            ValueComparator bvc = new ValueComparator(currentOpponent.placementHistory, true);
            TreeMap<Integer, Long> sortedMap = new TreeMap<Integer, Long>(bvc);
            sortedMap.putAll(currentOpponent.placementHistory);
            for (Map.Entry<Integer, Long> entry : sortedMap.entrySet()) {
                Integer key = entry.getKey();
                long value = entry.getValue();
                if (Math.abs(value - avg) > 0.66 * avg) {  // roughly an incidence of 1 stddev
                    if (tryAttack(key)) {
                        return lastAttack;
                    }
                } else {
                    break; // these are sorted, we can stop looping
                }
            }
        }

        // attack the diagonals
        while (diagAttacks.size() > 0) {
            int key = diagAttacks.remove(0);
            if (tryAttack(key)) {
                return lastAttack;
            }
        }


        // randomly select the balance, shouldn't really get here often
        while (!tryAttack(generator.nextInt(100)));
        return lastAttack;
    }

    public int compareHits(Coordinate hit1, Coordinate hit2) {
        if (hit1.getX() == hit2.getX()) {
            return VERTICAL;
        } else {
            return HORIZONTAL;
        }
    }

    public boolean tryAttack(int key) {
        Coordinate attack = null;
        if (key >= 0 && key < 100 && !myAttacks.contains(key)) {
            Log("Attack: " + key);
            int x = getX(key);
            int y = getY(key);
            lastAttack = new Coordinate(x, y);
            myAttacks.add(id(x, y));
            return true;
        }
        return false;
    }

    // Informs you of the result of your most recent attack
    @Override
    public void resultOfAttack(int result) {
        if (result != MISS && result != DEFEATED) {
            int hitShip = result % HIT_MODIFIER;
            Log("Hit ship" + hitShip + ", result:" + result);
            if (result / SUNK_MODIFIER >= 1) {
                // sunk a ship
                currentOpponent.recordPlacement(lastAttack);
                enemyShips[hitShip] = null;
            } else if (result / HIT_MODIFIER >= 1) {
                ArrayList<Coordinate> hitList = (ArrayList<Coordinate>) enemyShips[hitShip];
                hitList.add(lastAttack);
                currentOpponent.recordPlacement(lastAttack);
            }
        }
    }

    // Informs you of the position of an attack against you.
    @Override
    public void opponentAttack(Coordinate coord) {
        currentOpponent.recordAttack(coord);
    }

    // Informs you of the result of the game.
    @Override
    public void resultOfGame(int result) {
        currentOpponent.result(result);
    }

    public void Log(String s) {
        if (LOG) {
            System.out.println(s);
        }
    }
}
