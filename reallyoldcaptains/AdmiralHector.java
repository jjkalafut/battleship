
import java.util.Random;

/**
 *
 * @author Eric Bonsness Built from CaptainLoco
 */
public class AdmiralHector implements Captain {

    protected Random generator = new Random(System.currentTimeMillis());
    protected Random generator2 = new Random(System.nanoTime());
    int[][] attackPanel = new int[10][10];
    int recX;
    int recY;
    int recResult;
    int phitX = 35;
    int phitY = 35;
    int dhitX = 35;
    int dhitY = 35;
    int shitX = 35;
    int shitY = 35;
    int bhitX = 35;
    int bhitY = 35;
    int ahitX = 35;
    int ahitY = 35;
    boolean patHit = false;
    boolean desHit = false;
    boolean subHit = false;
    boolean batHit = false;
    boolean airHit = false;
    boolean desSunk = false;
    boolean subSunk = false;
    boolean batSunk = false;
    boolean airSunk = false;
    boolean schemeOne = false;
    boolean Yconstd = false;
    boolean Xconstd = false;
    boolean Yconsts = false;
    boolean Xconsts = false;
    boolean Yconstb = false;
    boolean Xconstb = false;
    boolean Yconsta = false;
    boolean Xconsta = false;
    protected Fleet myFleet;

    /**
     *
     * @param numMatches	The number matches you will be engaging in with this
     * opponent.
     * @param numCaptains	The number of opponents you will be facing during the
     * current set of battles.
     * @param opponent	The name of your opponent for this match
     */
    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
        myFleet = new Fleet();
        boolean patrolSuccess = false;
        boolean destroyerSuccess = false;
        boolean subSuccess = false;
        boolean battleSuccess = false;
        boolean aircraftSuccess = false;

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                attackPanel[i][j] = -2;
            }
        }
        if (generator2.nextInt(100) % 2 == 0) {
            schemeOne = true;
            for (int i = 2; i < 10; i = i + 3) {
                for (int j = 0; j < 10; j = j + 3) {
                    attackPanel[i][j] = -1;
                }
            }
            for (int i = 1; i < 10; i = i + 3) {
                for (int j = 1; j < 10; j = j + 3) {
                    attackPanel[i][j] = -1;
                }
            }
            for (int i = 0; i < 10; i = i + 3) {
                for (int j = 2; j < 10; j = j + 3) {
                    attackPanel[i][j] = -1;
                }
            }
        } else {
            for (int i = 1; i < 10; i = i + 3) {
                for (int j = 0; j < 10; j = j + 3) {
                    attackPanel[i][j] = -1;
                }
            }
            for (int i = 0; i < 10; i = i + 3) {
                for (int j = 1; j < 10; j = j + 3) {
                    attackPanel[i][j] = -1;
                }
            }
            for (int i = 2; i < 10; i = i + 3) {
                for (int j = 2; j < 10; j = j + 3) {
                    attackPanel[i][j] = -1;
                }
            }
        }
        int failCount = 5;
        int xpos = -1;
        int ypos = -1;
        while (!patrolSuccess) {
            if (failCount >= 2) {
                xpos = generator2.nextInt(10);
                ypos = generator.nextInt(10);
                failCount = 0;
            }
            patrolSuccess = myFleet.placeShip(xpos, ypos, generator.nextInt(2), PATROL_BOAT);
            failCount++;
        }
        xpos = generator.nextInt(10);
        ypos = generator2.nextInt(10);
        while (!destroyerSuccess) {
            if (failCount >= 2) {
                xpos = generator2.nextInt(10);
                ypos = generator.nextInt(10);
                failCount = 0;
            }
            destroyerSuccess = myFleet.placeShip(xpos, ypos, generator.nextInt(2), DESTROYER);
            failCount++;
        }
        xpos = generator.nextInt(10);
        ypos = generator2.nextInt(10);
        while (!subSuccess) {
            if (failCount >= 2) {
                xpos = generator2.nextInt(10);
                ypos = generator.nextInt(10);
                failCount = 0;
            }
            subSuccess = myFleet.placeShip(xpos, ypos, generator.nextInt(2), SUBMARINE);
            failCount++;
        }
        xpos = generator.nextInt(10);
        ypos = generator2.nextInt(10);
        while (!battleSuccess) {
            if (failCount >= 2) {
                xpos = generator2.nextInt(10);
                ypos = generator.nextInt(10);
                failCount = 0;
            }
            battleSuccess = myFleet.placeShip(xpos, ypos, generator.nextInt(2), BATTLESHIP);
            failCount++;
        }
        xpos = generator.nextInt(10);
        ypos = generator2.nextInt(10);
        while (!aircraftSuccess) {
            if (failCount >= 2) {
                xpos = generator2.nextInt(10);
                ypos = generator.nextInt(10);
                failCount = 0;
            }
            aircraftSuccess = myFleet.placeShip(xpos, ypos, generator.nextInt(2), AIRCRAFT_CARRIER);
            failCount++;
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
     *
     * @return A valid coordinate on the playing field.
     */
    @Override
    public Coordinate makeAttack() {
        int failCount = 0;
        boolean newAttack = false;
        while (patHit == true & newAttack == false) {
            if (phitX + 1 < 10) {
                if (attackPanel[phitX + 1][phitY] == -1 || attackPanel[phitX + 1][phitY] == -2) {
                    recX = phitX + 1;
                    recY = phitY;
                    newAttack = true;
                }
            }
            if (phitX - 1 >= 0 & newAttack == false) {
                if (attackPanel[phitX - 1][phitY] == -1 || attackPanel[phitX - 1][phitY] == -2) {
                    recX = phitX - 1;
                    recY = phitY;
                    newAttack = true;
                }
            }
            if (phitY + 1 < 10 & newAttack == false) {
                if (attackPanel[phitX][phitY + 1] == -1 || attackPanel[phitX][phitY + 1] == -2) {
                    recX = phitX;
                    recY = phitY + 1;
                    newAttack = true;
                }
            }
            if (phitY - 1 >= 0 & newAttack == false) {
                if (attackPanel[phitX][phitY - 1] == -1 || attackPanel[phitX][phitY - 1] == -2) {
                    recX = phitX;
                    recY = phitY - 1;
                    newAttack = true;
                }
            }
        }
        while (desHit == true & newAttack == false & desSunk == false) {
            if (dhitX + 1 < 10 & Xconstd == false) {
                if (attackPanel[dhitX + 1][dhitY] == -1 || attackPanel[dhitX + 1][dhitY] == -2) {
                    recX = dhitX + 1;
                    recY = dhitY;
                    newAttack = true;
                }
            }
            if (dhitX - 1 >= 0 & newAttack == false & Xconstd == false) {
                if (attackPanel[dhitX - 1][dhitY] == -1 || attackPanel[dhitX - 1][dhitY] == -2) {
                    recX = dhitX - 1;
                    recY = dhitY;
                    newAttack = true;
                }
            }
            if (dhitY + 1 < 10 & newAttack == false & Yconstd == false) {
                if (attackPanel[dhitX][dhitY + 1] == -1 || attackPanel[dhitX][dhitY + 1] == -2) {
                    recX = dhitX;
                    recY = dhitY + 1;
                    newAttack = true;
                }
            }
            if (dhitY - 1 >= 0 & newAttack == false & Yconstd == false) {
                if (attackPanel[dhitX][dhitY - 1] == -1 || attackPanel[dhitX][dhitY - 1] == -2) {
                    recX = dhitX;
                    recY = dhitY - 1;
                    newAttack = true;
                }
            }
            if (newAttack == false) {
                attackPanel[dhitX][dhitY] = -3;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (attackPanel[i][j] == 11) {
                            dhitX = i;
                            dhitY = j;
                        }
                    }
                }
            }
        }
        while (subHit == true & newAttack == false & subSunk == false) {
            if (shitX + 1 < 10 & Xconsts == false) {
                if (attackPanel[shitX + 1][shitY] == -1 || attackPanel[shitX + 1][shitY] == -2) {
                    recX = shitX + 1;
                    recY = shitY;
                    newAttack = true;
                }
            }
            if (shitX - 1 >= 0 & newAttack == false & Xconsts == false) {
                if (attackPanel[shitX - 1][shitY] == -1 || attackPanel[shitX - 1][shitY] == -2) {
                    recX = shitX - 1;
                    recY = shitY;
                    newAttack = true;
                }
            }
            if (shitY + 1 < 10 & newAttack == false & Yconsts == false) {
                if (attackPanel[shitX][shitY + 1] == -1 || attackPanel[shitX][shitY + 1] == -2) {
                    recX = shitX;
                    recY = shitY + 1;
                    newAttack = true;
                }
            }
            if (shitY - 1 >= 0 & newAttack == false & Yconsts == false) {
                if (attackPanel[shitX][shitY - 1] == -1 || attackPanel[shitX][shitY - 1] == -2) {
                    recX = shitX;
                    recY = shitY - 1;
                    newAttack = true;
                }
            }
            if (newAttack == false) {
                attackPanel[shitX][shitY] = -3;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (attackPanel[i][j] == 12) {
                            shitX = i;
                            shitY = j;
                        }
                    }
                }
            }
        }
        while (batHit == true & newAttack == false & batSunk == false) {
            if (bhitX + 1 < 10 & Xconstb == false) {
                if (attackPanel[bhitX + 1][bhitY] == -1 || attackPanel[bhitX + 1][bhitY] == -2) {
                    recX = bhitX + 1;
                    recY = bhitY;
                    newAttack = true;
                }
            }
            if (bhitX - 1 >= 0 & newAttack == false & Xconstb == false) {
                if (attackPanel[bhitX - 1][bhitY] == -1 || attackPanel[bhitX - 1][bhitY] == -2) {
                    recX = bhitX - 1;
                    recY = bhitY;
                    newAttack = true;
                }
            }
            if (bhitY + 1 < 10 & newAttack == false & Yconstb == false) {
                if (attackPanel[bhitX][bhitY + 1] == -1 || attackPanel[bhitX][bhitY + 1] == -2) {
                    recX = bhitX;
                    recY = bhitY + 1;
                    newAttack = true;
                }
            }
            if (bhitY - 1 >= 0 & newAttack == false & Yconstb == false) {
                if (attackPanel[bhitX][bhitY - 1] == -1 || attackPanel[bhitX][bhitY - 1] == -2) {
                    recX = bhitX;
                    recY = bhitY - 1;
                    newAttack = true;
                }
            }
            if (newAttack == false) {
                attackPanel[bhitX][bhitY] = -3;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (attackPanel[i][j] == 13) {
                            bhitX = i;
                            bhitY = j;
                        }
                    }
                }
            }
        }
        while (airHit == true & newAttack == false & airSunk == false) {
            if (ahitX + 1 < 10 & Xconsta == false) {
                if (attackPanel[ahitX + 1][ahitY] == -1 || attackPanel[ahitX + 1][ahitY] == -2) {
                    recX = ahitX + 1;
                    recY = ahitY;
                    newAttack = true;
                }
            }
            if (ahitX - 1 >= 0 & newAttack == false & Xconsta == false) {
                if (attackPanel[ahitX - 1][ahitY] == -1 || attackPanel[ahitX - 1][ahitY] == -2) {
                    recX = ahitX - 1;
                    recY = ahitY;
                    newAttack = true;
                }
            }
            if (ahitY + 1 < 10 & newAttack == false & Yconsta == false) {
                if (attackPanel[ahitX][ahitY + 1] == -1 || attackPanel[ahitX][ahitY + 1] == -2) {
                    recX = ahitX;
                    recY = ahitY + 1;
                    newAttack = true;
                }
            }
            if (ahitY - 1 >= 0 & newAttack == false & Yconsta == false) {
                if (attackPanel[ahitX][ahitY - 1] == -1 || attackPanel[ahitX][ahitY - 1] == -2) {
                    recX = ahitX;
                    recY = ahitY - 1;
                    newAttack = true;
                }
            }
            if (newAttack == false) {
                attackPanel[ahitX][ahitY] = -3;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (attackPanel[i][j] == 14) {
                            ahitX = i;
                            ahitY = j;
                        }
                    }
                }
            }
        }
        while (newAttack == false) {
            recX = generator2.nextInt(10);
            recY = generator.nextInt(10);
            if (attackPanel[recX][recY] == -1) {
                newAttack = true;
                failCount = 0;
            }
            failCount++;
            if (failCount == 15) {
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (attackPanel[i][j] == -1) {
                            recX = i;
                            recY = j;
                            newAttack = true;
                            failCount = 0;
                        }
                    }
                }
            }
        }
        newAttack = false;
        return new Coordinate(recX, recY);
    }

    /**
     *
     * @param result A code from Constants that tells me all about the results
     * of my last attack.
     */
    @Override
    public void resultOfAttack(int result) {
        attackPanel[recX][recY] = result;
        recResult = result;
        if (recResult == 20) {
            patHit = false;

        }
        if (recResult == 21) {
            desHit = false;
            desSunk = true;
        }
        if (recResult == 22) {
            subHit = false;
            subSunk = true;
        }
        if (recResult == 23) {
            batHit = false;
            batSunk = true;
        }
        if (recResult == 24) {
            airHit = false;
            airSunk = true;
        }
        if (recResult == 10) {
            patHit = true;
            phitX = recX;
            phitY = recY;
        }
        if (recResult == 11) {
            if (dhitX == recX) {
                Xconstd = true;
            }
            if (dhitY == recY) {
                Yconstd = true;
            }
            desHit = true;
            dhitX = recX;
            dhitY = recY;
        }
        if (recResult == 12) {
            if (shitX == recX) {
                Xconsts = true;
            }
            if (shitY == recY) {
                Yconsts = true;
            }
            subHit = true;
            shitX = recX;
            shitY = recY;
        }
        if (recResult == 13) {
            if (bhitX == recX) {
                Xconstb = true;
            }
            if (bhitY == recY) {
                Yconstb = true;
            }
            batHit = true;
            bhitX = recX;
            bhitY = recY;
        }
        if (recResult == 14) {
            if (ahitX == recX) {
                Xconsta = true;
            }
            if (ahitY == recY) {
                Yconsta = true;
            }
            airHit = true;
            ahitX = recX;
            ahitY = recY;
        }
        if (desSunk == true & subSunk == true & batSunk == true & airSunk == true) {
            if (schemeOne == true) {
                for (int i = 0; i < 9; i = i + 3) {
                    if (attackPanel[i][0] == -2) {
                        attackPanel[i][0] = -1;
                    }
                }
                if (attackPanel[2][1] == -2) {
                    attackPanel[2][1] = -1;
                }

                if (attackPanel[5][1] == -2) {
                    attackPanel[5][1] = -1;
                }

                if (attackPanel[9][1] == -2) {
                    attackPanel[9][1] = -1;
                }

                if (attackPanel[1][2] == -2) {
                    attackPanel[1][2] = -1;
                }

                if (attackPanel[4][2] == -2) {
                    attackPanel[4][2] = -1;
                }

                if (attackPanel[8][2] == -2) {
                    attackPanel[8][2] = -1;
                }

                if (attackPanel[0][3] == -2) {
                    attackPanel[0][3] = -1;
                }

                if (attackPanel[3][3] == -2) {
                    attackPanel[3][3] = -1;
                }

                if (attackPanel[7][3] == -2) {
                    attackPanel[7][3] = -1;
                }

                if (attackPanel[2][4] == -2) {
                    attackPanel[2][4] = -1;
                }

                if (attackPanel[6][4] == -2) {
                    attackPanel[6][4] = -1;
                }

                if (attackPanel[9][4] == -2) {
                    attackPanel[9][4] = -1;
                }

                if (attackPanel[1][5] == -2) {
                    attackPanel[1][5] = -1;
                }

                if (attackPanel[5][5] == -2) {
                    attackPanel[5][5] = -1;
                }

                if (attackPanel[8][5] == -2) {
                    attackPanel[8][5] = -1;
                }

                if (attackPanel[0][6] == -2) {
                    attackPanel[0][6] = -1;
                }

                if (attackPanel[4][6] == -2) {
                    attackPanel[4][6] = -1;
                }

                if (attackPanel[7][6] == -2) {
                    attackPanel[7][6] = -1;
                }

                if (attackPanel[3][7] == -2) {
                    attackPanel[3][7] = -1;
                }

                if (attackPanel[6][7] == -2) {
                    attackPanel[6][7] = -1;
                }

                if (attackPanel[9][7] == -2) {
                    attackPanel[9][7] = -1;
                }

                if (attackPanel[2][8] == -2) {
                    attackPanel[2][8] = -1;
                }

                if (attackPanel[5][8] == -2) {
                    attackPanel[5][8] = -1;
                }

                if (attackPanel[8][8] == -2) {
                    attackPanel[8][8] = -1;
                }

                if (attackPanel[1][9] == -2) {
                    attackPanel[1][9] = -1;
                }

                if (attackPanel[4][9] == -2) {
                    attackPanel[4][9] = -1;
                }

                if (attackPanel[7][9] == -2) {
                    attackPanel[7][9] = -1;
                }
            } else {
                for (int i = 0; i < 9; i = i + 3) {
                    if (attackPanel[i][2] == -2) {
                        attackPanel[i][2] = -1;
                    }
                }
                for (int i = 1; i < 10; i = i + 3) {
                    if (attackPanel[i][1] == -2) {
                        attackPanel[i][1] = -1;
                    }
                }
                for (int i = 2; i < 10; i = i + 3) {
                    if (attackPanel[i][0] == -2) {
                        attackPanel[i][0] = -1;
                    }
                }
                if (attackPanel[2][3] == -2) {
                    attackPanel[2][3] = -1;
                }

                if (attackPanel[5][3] == -2) {
                    attackPanel[5][3] = -1;
                }

                if (attackPanel[9][3] == -2) {
                    attackPanel[9][3] = -1;
                }

                if (attackPanel[1][4] == -2) {
                    attackPanel[1][4] = -1;
                }

                if (attackPanel[4][4] == -2) {
                    attackPanel[4][4] = -1;
                }

                if (attackPanel[8][4] == -2) {
                    attackPanel[8][4] = -1;
                }

                if (attackPanel[0][5] == -2) {
                    attackPanel[0][5] = -1;
                }

                if (attackPanel[3][5] == -2) {
                    attackPanel[3][5] = -1;
                }

                if (attackPanel[7][5] == -2) {
                    attackPanel[7][5] = -1;
                }

                if (attackPanel[2][6] == -2) {
                    attackPanel[2][6] = -1;
                }

                if (attackPanel[6][6] == -2) {
                    attackPanel[6][6] = -1;
                }

                if (attackPanel[9][6] == -2) {
                    attackPanel[9][6] = -1;
                }

                if (attackPanel[1][7] == -2) {
                    attackPanel[1][7] = -1;
                }

                if (attackPanel[5][7] == -2) {
                    attackPanel[5][7] = -1;
                }

                if (attackPanel[8][7] == -2) {
                    attackPanel[8][7] = -1;
                }

                if (attackPanel[0][8] == -2) {
                    attackPanel[0][8] = -1;
                }

                if (attackPanel[4][8] == -2) {
                    attackPanel[4][8] = -1;
                }

                if (attackPanel[7][8] == -2) {
                    attackPanel[7][8] = -1;
                }

                if (attackPanel[3][9] == -2) {
                    attackPanel[3][9] = -1;
                }

                if (attackPanel[6][9] == -2) {
                    attackPanel[6][9] = -1;
                }

                if (attackPanel[9][9] == -2) {
                    attackPanel[9][9] = -1;
                }
            }
            desSunk = false;
        }
    }

    /**
     *
     * @param coord The spot on the board where your opponent just attacked.
     */
    @Override
    public void opponentAttack(Coordinate coord) {
    }

    /**
     *
     * @param result A code from Constants that will equal WON or LOST.
     */
    @Override
    public void resultOfGame(int result) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                attackPanel[i][j] = -1;
            }
        }
        schemeOne = false;
        patHit = false;
        desHit = false;
        subHit = false;
        batHit = false;
        airHit = false;
        Yconstd = false;
        Xconstd = false;
        Yconsts = false;
        Xconsts = false;
        Yconstb = false;
        Xconstb = false;
        Yconsta = false;
        Xconsta = false;
        phitX = 35;
        phitY = 35;
        dhitX = 35;
        dhitY = 35;
        shitX = 35;
        shitY = 35;
        bhitX = 35;
        bhitY = 35;
        ahitX = 35;
        ahitY = 35;
        desSunk = false;
        subSunk = false;
        batSunk = false;
        airSunk = false;
    }
}
