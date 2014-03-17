
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

class enemyShip {

    int name = 0;
    int orientation = 0;
    int length = 0;
    ArrayList<Coordinate> hits = new ArrayList<Coordinate>(5);
}

class shotName {

    int hitsOnOpp = 0;
    String name = "";
}

class OppAttacks {

    int index;
    Coordinate coord;
}

public class HMS_Dreadnought implements Captain, Constants {

    Random generator;
    Coordinate myAtk;
    Fleet myFleet;
    int numGames;
    int round;
    int[][] oppAtks;
    int[][] hitsOnOpp;
    int[][] oppAtksRound;
    boolean[][] myAtks;
    ArrayList<enemyShip> targets;
    int x1, y1;
    List<Coordinate> futureAttacks;
    List<Coordinate> futureAttacks2;
    String currOpp = "";

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
        generator = new Random();

        myFleet = new Fleet();

        futureAttacks = new ArrayList<Coordinate>();

        futureAttacks2 = new ArrayList<Coordinate>();

        x1 = 0;

        y1 = 0;

        if (opponent.equals(currOpp)) {
            ++numGames;

            round = 0;

        } else {
            currOpp = opponent;

            oppAtks = new int[10][10];

            hitsOnOpp = new int[10][10];

            oppAtksRound = new int[10][10];

            numGames = 0;
        }

        //every other
        for (int y = 0; y < 10; ++y) {

            for (int x = 0; x < 10; x += 2) {

                if (y % 2 == 0 && x % 2 == 0) {

                    ++x;

                }

                if (x > 9) {

                    x = 9;

                }

                futureAttacks.add(new Coordinate(x, y));

            }

        }

        //Collections.shuffle((List<Coordinate>) futureAttacks2);
        Collections.shuffle((List<Coordinate>) futureAttacks);



        if (numGames > 10) {



            sortFutureAttacks();

        }

        if (numGames % 5 == 0) {
            List<Coordinate> templist = new ArrayList<Coordinate>(100);

            templist.addAll(futureAttacks);

            futureAttacks.removeAll(templist);

            futureAttacks.add(new Coordinate(0, 0));

            futureAttacks.add(new Coordinate(9, 0));

            futureAttacks.add(new Coordinate(0, 9));

            futureAttacks.add(new Coordinate(9, 9));

            futureAttacks.addAll(templist);

        }

        //futureAttacks.addAll(futureAttacks2);


        myAtks = new boolean[10][10];

        targets = new ArrayList<enemyShip>(5);

        //myAtk = new Coordinate(generator.nextInt(10),generator.nextInt(10));

        // Each type of ship must be placed on the board.  Note that the .place method return whether it was
        // possible to put a ship at the indicated position.  If the coordinates were not on the board or if
        // it overlapped with a ship you already tried to place it will return false.

        if (numGames > 10) {


            ArrayList<OppAttacks> paddleboatx = new ArrayList<OppAttacks>(100);

            ArrayList<OppAttacks> paddleboaty = new ArrayList<OppAttacks>(100);

            ArrayList<OppAttacks> length3x = new ArrayList<OppAttacks>(100);

            ArrayList<OppAttacks> length3y = new ArrayList<OppAttacks>(100);

            ArrayList<OppAttacks> battleshipx = new ArrayList<OppAttacks>(100);

            ArrayList<OppAttacks> battleshipy = new ArrayList<OppAttacks>(100);

            ArrayList<OppAttacks> aircraftx = new ArrayList<OppAttacks>(100);

            ArrayList<OppAttacks> aircrafty = new ArrayList<OppAttacks>(100);


            for (int y = 0; y < 10; ++y) {

                for (int x = 0; x < 10; ++x) {

                    //paddle boat
                    if (x < 9) {
                        OppAttacks temp = new OppAttacks();

                        temp.index = oppAtks[x][y] + oppAtks[x + 1][y];

                        temp.coord = new Coordinate(x, y);

                        paddleboatx.add(temp);

                    }

                    if (y < 9) {
                        OppAttacks temp = new OppAttacks();

                        temp.index = oppAtks[x][y] + oppAtks[x][y + 1];

                        temp.coord = new Coordinate(x, y);

                        paddleboaty.add(temp);

                    }

                    //3 length boats
                    if (x < 8) {
                        OppAttacks temp = new OppAttacks();

                        temp.index = oppAtks[x][y] + oppAtks[x + 1][y] + oppAtks[x + 2][y];

                        temp.coord = new Coordinate(x, y);

                        length3x.add(temp);

                    }

                    if (y < 8) {
                        OppAttacks temp = new OppAttacks();

                        temp.index = oppAtks[x][y] + oppAtks[x][y + 1] + oppAtks[x][y + 2];

                        temp.coord = new Coordinate(x, y);

                        length3y.add(temp);

                    }

                    if (x < 7) {
                        OppAttacks temp = new OppAttacks();

                        temp.index = oppAtks[x][y] + oppAtks[x + 1][y] + oppAtks[x + 2][y] + oppAtks[x + 3][y];

                        temp.coord = new Coordinate(x, y);

                        battleshipx.add(temp);

                    }

                    if (y < 7) {
                        OppAttacks temp = new OppAttacks();

                        temp.index = oppAtks[x][y] + oppAtks[x][y + 1] + oppAtks[x][y + 2] + oppAtks[x][y + 3];

                        temp.coord = new Coordinate(x, y);

                        battleshipy.add(temp);

                    }

                    if (x < 6) {
                        OppAttacks temp = new OppAttacks();

                        temp.index = oppAtks[x][y] + oppAtks[x + 1][y] + oppAtks[x + 2][y] + oppAtks[x + 3][y] + oppAtks[x + 4][y];

                        temp.coord = new Coordinate(x, y);

                        aircraftx.add(temp);

                    }

                    if (y < 6) {
                        OppAttacks temp = new OppAttacks();

                        temp.index = oppAtks[x][y] + oppAtks[x][y + 1] + oppAtks[x][y + 2] + oppAtks[x][y + 3] + oppAtks[x][y + 4];

                        temp.coord = new Coordinate(x, y);

                        aircrafty.add(temp);

                    }

                }

            }

            Collections.sort(paddleboatx, new Comparator<OppAttacks>() {
                @Override
                public int compare(OppAttacks p1, OppAttacks p2) {
                    return p1.index - p2.index;
                }
            });

            Collections.sort(paddleboaty, new Comparator<OppAttacks>() {
                @Override
                public int compare(OppAttacks p1, OppAttacks p2) {
                    return p1.index - p2.index;
                }
            });

            Collections.sort(length3x, new Comparator<OppAttacks>() {
                @Override
                public int compare(OppAttacks p1, OppAttacks p2) {
                    return p1.index - p2.index;
                }
            });

            Collections.sort(length3y, new Comparator<OppAttacks>() {
                @Override
                public int compare(OppAttacks p1, OppAttacks p2) {
                    return p1.index - p2.index;
                }
            });

            Collections.sort(battleshipx, new Comparator<OppAttacks>() {
                @Override
                public int compare(OppAttacks p1, OppAttacks p2) {
                    return p1.index - p2.index;
                }
            });

            Collections.sort(battleshipy, new Comparator<OppAttacks>() {
                @Override
                public int compare(OppAttacks p1, OppAttacks p2) {
                    return p1.index - p2.index;
                }
            });

            Collections.sort(aircraftx, new Comparator<OppAttacks>() {
                @Override
                public int compare(OppAttacks p1, OppAttacks p2) {
                    return p1.index - p2.index;
                }
            });

            Collections.sort(aircrafty, new Comparator<OppAttacks>() {
                @Override
                public int compare(OppAttacks p1, OppAttacks p2) {
                    return p1.index - p2.index;
                }
            });

            //patrol boat placement
            if (paddleboatx.get(0).index > paddleboaty.get(0).index) {
                myFleet.placeShip(paddleboatx.get(0).coord, HORIZONTAL, PATROL_BOAT);
            } else {
                myFleet.placeShip(paddleboaty.get(0).coord, VERTICAL, PATROL_BOAT);
            }

            //destoyer placement
            for (int i = 0; i < length3x.size(); ++i) {
                if (length3x.get(i).index > length3y.get(i).index) {

                    if (!myFleet.placeShip(length3x.get(i).coord, HORIZONTAL, DESTROYER)) {
                        continue;
                    }

                } else {
                    if (!myFleet.placeShip(length3y.get(i).coord, VERTICAL, DESTROYER)) {
                        continue;
                    }
                }
            }

            //submarine placement
            for (int i = 0; i < length3x.size(); ++i) {
                if (length3x.get(i).index > length3y.get(i).index) {

                    if (!myFleet.placeShip(length3x.get(i).coord, HORIZONTAL, SUBMARINE)) {
                        continue;
                    }

                } else {
                    if (!myFleet.placeShip(length3x.get(i).coord, HORIZONTAL, SUBMARINE)) {
                        continue;
                    }
                }
            }

            //battleship placement
            for (int i = 0; i < battleshipx.size(); ++i) {
                if (battleshipx.get(i).index > battleshipy.get(i).index) {

                    if (!myFleet.placeShip(battleshipx.get(i).coord, HORIZONTAL, BATTLESHIP)) {
                        continue;
                    }

                } else {
                    if (!myFleet.placeShip(battleshipy.get(i).coord, HORIZONTAL, BATTLESHIP)) {
                        continue;
                    }
                }
            }

            //aircraft placement
            for (int i = 0; i < aircraftx.size(); ++i) {
                if (battleshipx.get(i).index > battleshipy.get(i).index) {

                    if (!myFleet.placeShip(aircraftx.get(i).coord, HORIZONTAL, AIRCRAFT_CARRIER)) {
                        continue;
                    }

                } else {
                    if (!myFleet.placeShip(aircrafty.get(i).coord, HORIZONTAL, AIRCRAFT_CARRIER)) {
                        continue;
                    }
                }
            }




        } else {
            //until have enough data place ships randomly
            while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), PATROL_BOAT)) {
            }
            while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), DESTROYER)) {
            }
            while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), SUBMARINE)) {
            }
            while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), BATTLESHIP)) {
            }
            while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), AIRCRAFT_CARRIER)) {
            }
        }
    }

    /**
     * Return my fleet that was constructed in initialize. This is a method you
     * probably don't need to change.
     *
     * @return A valid fleet representing my ship placements for this round.
     */
    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    /**
     * Roll the dice and pick a square to attack. This is not very smart. At the
     * very lest you want to keep track of where you have already attacked so
     * you don't attack a square twice. You probably also want to remember what
     * the most recent attack was so you can use it in resultOfAttack().
     *
     * @return A valid coordinate on the playing field.
     */
    @Override
    public Coordinate makeAttack() {

        if (targets.size() == 0) {

            do {

                if (futureAttacks.isEmpty()) {

                    x1 = generator.nextInt(10);

                    y1 = generator.nextInt(10);

                }

                x1 = futureAttacks.get(0).getX();

                y1 = futureAttacks.get(0).getY();

                futureAttacks.remove(0);

            } while (myAtks[x1][y1]);

            myAtk = new Coordinate(x1, y1);

        } else {

            //ContinueAttacking() will change myAtk
            ContinueAttacking();

        }

        while (myAtks[myAtk.getX()][myAtk.getY()] || myAtk.getX() > 9 || myAtk.getY() > 9 || myAtk.getX() < 0 || myAtk.getY() < 0) {
            myAtk = new Coordinate(generator.nextInt(10), generator.nextInt(10));
        }

        myAtks[myAtk.getX()][myAtk.getY()] = true;

        return myAtk;
        //return new Coordinate(generator.nextInt(10), generator.nextInt(10));
    }

    public void sortFutureAttacks() {

        Collections.sort(futureAttacks, new Comparator<Coordinate>() {
            @Override
            public int compare(Coordinate p1, Coordinate p2) {
                return hitsOnOpp[p2.getX()][p2.getY()] - hitsOnOpp[p1.getX()][p1.getY()];
            }
        });

    }

    public void ContinueAttacking() {



        //do code for attacking the battleship or air craft carrier
        //length = 4 or 5
        if (targets.get(0).orientation == 0) {

            getSecondHit();

        } else if (targets.get(0).orientation == 1) {

            Coordinate smallestHit = new Coordinate(0, 11);
            //x = index; y = number
            Coordinate largestHit = new Coordinate(0, -1);

            for (int i = 0; i < targets.get(0).hits.size(); ++i) {

                if (targets.get(0).hits.get(i).getX() < smallestHit.getY()) {

                    smallestHit = new Coordinate(i, targets.get(0).hits.get(i).getX());

                }

                if (targets.get(0).hits.get(i).getX() > largestHit.getY()) {

                    largestHit = new Coordinate(i, targets.get(0).hits.get(i).getX());

                }

            }
            // check left 1 from farthest left already hit point spot if you have already attacked there attack 1 right from farthest right hit
            if (targets.get(0).hits.get(smallestHit.getX()).getX() - 1 > -1) {
                if (!myAtks[targets.get(0).hits.get(smallestHit.getX()).getX() - 1][targets.get(0).hits.get(smallestHit.getX()).getY()]) {
                    myAtk = new Coordinate(targets.get(0).hits.get(smallestHit.getX()).getX() - 1, targets.get(0).hits.get(smallestHit.getX()).getY());
                    return;
                } else {

                    myAtk = new Coordinate(targets.get(0).hits.get(largestHit.getX()).getX() + 1, targets.get(0).hits.get(largestHit.getX()).getY());
                    return;
                }
            } else {
                myAtk = new Coordinate(targets.get(0).hits.get(largestHit.getX()).getX() + 1, targets.get(0).hits.get(largestHit.getX()).getY());
                return;
            }


        } //if vertical
        else if (targets.get(0).orientation == 2) {

            Coordinate smallestHit = new Coordinate(0, 11);
            //x = index; y = number
            Coordinate largestHit = new Coordinate(0, -1);

            for (int i = 0; i < targets.get(0).hits.size(); ++i) {

                if (targets.get(0).hits.get(i).getY() < smallestHit.getY()) {

                    smallestHit = new Coordinate(i, targets.get(0).hits.get(i).getY());

                }
                if (targets.get(0).hits.get(i).getY() > largestHit.getY()) {

                    largestHit = new Coordinate(i, targets.get(0).hits.get(i).getY());

                }

            }
            if (targets.get(0).hits.get(smallestHit.getX()).getY() - 1 > -1) {
                if (!myAtks[targets.get(0).hits.get(smallestHit.getX()).getX()][targets.get(0).hits.get(smallestHit.getX()).getY() - 1]) {
                    myAtk = new Coordinate(targets.get(0).hits.get(smallestHit.getX()).getX(), targets.get(0).hits.get(smallestHit.getX()).getY() - 1);
                    return;
                } else {

                    myAtk = new Coordinate(targets.get(0).hits.get(largestHit.getX()).getX(), targets.get(0).hits.get(largestHit.getX()).getY() + 1);
                    return;

                }
            } else {

                myAtk = new Coordinate(targets.get(0).hits.get(largestHit.getX()).getX(), targets.get(0).hits.get(largestHit.getX()).getY() + 1);
                return;

            }

        }

    }

    void getSecondHit() {

        ArrayList<shotName> temp = new ArrayList<shotName>(4);

        //left
        if (targets.get(0).hits.get(0).getX() - 1 > -1) {
            shotName shot = new shotName();

            shot.hitsOnOpp = hitsOnOpp[targets.get(0).hits.get(0).getX() - 1][targets.get(0).hits.get(0).getY()];

            shot.name = "l";

            temp.add(shot);
        }
        //top
        if (targets.get(0).hits.get(0).getY() - 1 > -1) {
            shotName shot = new shotName();

            shot.hitsOnOpp = hitsOnOpp[targets.get(0).hits.get(0).getX()][targets.get(0).hits.get(0).getY() - 1];

            shot.name = "t";

            temp.add(shot);
        }
        //right
        if (targets.get(0).hits.get(0).getX() + 1 < 10) {
            shotName shot = new shotName();

            shot.hitsOnOpp = hitsOnOpp[targets.get(0).hits.get(0).getX() + 1][targets.get(0).hits.get(0).getY()];

            shot.name = "r";

            temp.add(shot);
        }
        //bot
        if (targets.get(0).hits.get(0).getY() + 1 < 10) {

            shotName shot = new shotName();

            shot.hitsOnOpp = hitsOnOpp[targets.get(0).hits.get(0).getX()][targets.get(0).hits.get(0).getY() + 1];

            shot.name = "b";

            temp.add(shot);
        }

        Collections.sort(temp, new Comparator<shotName>() {
            @Override
            public int compare(shotName p1, shotName p2) {
                return p2.hitsOnOpp - p1.hitsOnOpp;
            }
        });

        for (int i = 0; i < temp.size(); ++i) {
            if (temp.get(i).name == "l") {
                if (!myAtks[targets.get(0).hits.get(0).getX() - 1][targets.get(0).hits.get(0).getY()]) {

                    myAtk = new Coordinate(targets.get(0).hits.get(0).getX() - 1, targets.get(0).hits.get(0).getY());
                    return;

                }
            }

            if (temp.get(i).name == "t") {
                if (!myAtks[targets.get(0).hits.get(0).getX()][targets.get(0).hits.get(0).getY() - 1]) {

                    myAtk = new Coordinate(targets.get(0).hits.get(0).getX(), targets.get(0).hits.get(0).getY() - 1);
                    return;
                }
            }

            if (temp.get(i).name == "r") {
                if (!myAtks[targets.get(0).hits.get(0).getX() + 1][targets.get(0).hits.get(0).getY()]) {

                    myAtk = new Coordinate(targets.get(0).hits.get(0).getX() + 1, targets.get(0).hits.get(0).getY());
                    return;
                }
            }

            if (temp.get(i).name == "b") {
                if (!myAtks[targets.get(0).hits.get(0).getX()][targets.get(0).hits.get(0).getY() + 1]) {

                    myAtk = new Coordinate(targets.get(0).hits.get(0).getX(), targets.get(0).hits.get(0).getY() + 1);
                    return;
                }
            }

        }

    }

    @Override
    public void resultOfAttack(int result) {
        // Add code here to process the success/failure of attacks
        if (result == HIT_PATROL_BOAT) {

            //hit something to ++hitsOnOpp on that position

            ++hitsOnOpp[myAtk.getX()][myAtk.getY()];

            //hit patrol boat

            enemyShip temp = new enemyShip();

            temp.hits.add(myAtk);

            temp.name = HIT_PATROL_BOAT;

            temp.length = 1;

            targets.add(temp);

        }

        if (result == SUNK_PATROL_BOAT) {
            ++hitsOnOpp[myAtk.getX()][myAtk.getY()];
            for (int i = 0; i < targets.size(); ++i) {

                if (targets.get(i).name == HIT_PATROL_BOAT) {

                    targets.remove(i);

                }

            }

        }

        if (result == HIT_DESTROYER) {

            ++hitsOnOpp[myAtk.getX()][myAtk.getY()];

            //hit destroyer
            boolean contained = false;

            for (int i = 0; i < targets.size(); ++i) {

                if (targets.get(i).name == HIT_DESTROYER) {
                    contained = true;

                    targets.get(i).hits.add(myAtk);

                    --targets.get(i).length;

                    if (targets.get(i).hits.get(0).getX() == targets.get(i).hits.get(1).getX()) {

                        targets.get(i).orientation = 2;

                    } else {

                        targets.get(i).orientation = 1;

                    }

                    break;
                }

            }

            if (!contained) {

                enemyShip temp = new enemyShip();

                temp.hits.add(myAtk);

                temp.name = HIT_DESTROYER;

                temp.length = 1;

                targets.add(temp);

            }


        }

        if (result == SUNK_DESTROYER) {

            ++hitsOnOpp[myAtk.getX()][myAtk.getY()];

            for (int i = 0; i < targets.size(); ++i) {

                if (targets.get(i).name == HIT_DESTROYER) {

                    targets.remove(i);

                }

            }

        }

        if (result == HIT_SUBMARINE) {

            ++hitsOnOpp[myAtk.getX()][myAtk.getY()];

            //hit destroyer
            boolean contained = false;

            for (int i = 0; i < targets.size(); ++i) {

                if (targets.get(i).name == HIT_SUBMARINE) {
                    contained = true;

                    targets.get(i).hits.add(myAtk);

                    --targets.get(i).length;

                    if (targets.get(i).hits.get(0).getX() == targets.get(i).hits.get(1).getX()) {

                        targets.get(i).orientation = 2;

                    } else {

                        targets.get(i).orientation = 1;

                    }

                    break;
                }

            }

            if (!contained) {

                enemyShip temp = new enemyShip();

                temp.hits.add(myAtk);

                temp.name = HIT_SUBMARINE;

                temp.length = 1;

                targets.add(temp);

            }


        }

        if (result == SUNK_SUBMARINE) {

            ++hitsOnOpp[myAtk.getX()][myAtk.getY()];

            for (int i = 0; i < targets.size(); ++i) {

                if (targets.get(i).name == HIT_SUBMARINE) {

                    targets.remove(i);

                }

            }

        }

        if (result == HIT_BATTLESHIP) {

            ++hitsOnOpp[myAtk.getX()][myAtk.getY()];

            //hit destroyer
            boolean contained = false;

            for (int i = 0; i < targets.size(); ++i) {

                if (targets.get(i).name == HIT_BATTLESHIP) {
                    contained = true;

                    targets.get(i).hits.add(myAtk);

                    --targets.get(i).length;

                    if (targets.get(i).hits.get(0).getX() == targets.get(i).hits.get(1).getX()) {

                        targets.get(i).orientation = 2;

                    } else {

                        targets.get(i).orientation = 1;

                    }

                    break;
                }

            }

            if (!contained) {

                enemyShip temp = new enemyShip();

                temp.hits.add(myAtk);

                temp.name = HIT_BATTLESHIP;

                temp.length = 1;

                targets.add(temp);

            }


        }

        if (result == SUNK_BATTLESHIP) {

            ++hitsOnOpp[myAtk.getX()][myAtk.getY()];

            for (int i = 0; i < targets.size(); ++i) {

                if (targets.get(i).name == HIT_BATTLESHIP) {

                    targets.remove(i);

                }

            }

        }

        if (result == HIT_AIRCRAFT_CARRIER) {

            ++hitsOnOpp[myAtk.getX()][myAtk.getY()];

            //hit destroyer
            boolean contained = false;

            for (int i = 0; i < targets.size(); ++i) {

                if (targets.get(i).name == HIT_AIRCRAFT_CARRIER) {
                    contained = true;

                    targets.get(i).hits.add(myAtk);

                    --targets.get(i).length;

                    if (targets.get(i).hits.get(0).getX() == targets.get(i).hits.get(1).getX()) {

                        targets.get(i).orientation = 2;

                    } else {

                        targets.get(i).orientation = 1;

                    }

                    break;
                }

            }

            if (!contained) {

                enemyShip temp = new enemyShip();

                temp.hits.add(myAtk);

                temp.name = HIT_AIRCRAFT_CARRIER;

                temp.length = 1;

                targets.add(temp);

            }


        }

        if (result == SUNK_AIRCRAFT_CARRIER) {

            ++hitsOnOpp[myAtk.getX()][myAtk.getY()];

            for (int i = 0; i < targets.size(); ++i) {

                if (targets.get(i).name == HIT_AIRCRAFT_CARRIER) {

                    targets.remove(i);

                }

            }

        }

    }

    @Override
    public void opponentAttack(Coordinate coord) {
        // Add code here to process or record opponent attacks
        ++oppAtks[coord.getX()][coord.getY()];

        if (oppAtksRound[coord.getX()][coord.getY()] == 0) {
            oppAtksRound[coord.getX()][coord.getY()] = 100 - round;
        } else {
            oppAtksRound[coord.getX()][coord.getY()] += 100 - round;

            oppAtksRound[coord.getX()][coord.getY()] /= 2;
        }
    }

    @Override
    public void resultOfGame(int result) {
        // Add code here to process the result of a game
    }
}
