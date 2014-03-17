/*
 * @Author Michael Fleming
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class DaddysHome implements Captain {

    private Fleet myFleet;
    private Random rGen;
    //  private boolean horizontalShipPos = false;
    //  private boolean horizontalShipNeg = false;
    //  private boolean verticalShipPos = false;
    //  private boolean verticalShipNeg = false;
    private ArrayList<Coordinate> shotGrid = new ArrayList<Coordinate>();
    int x, y;
    private boolean[][] board = new boolean[10][10];
    private boolean lastShotHit;
    private Coordinate hitPoint;
    private boolean foundHim = false;
    private int udlr = 0;
//    private int distanceCoutner = 2;
    private int firstShotVariable = 0;
    private int v;
    private int iCounter = 0;
    private boolean safetynet;
    private int randomShotCoutner;
    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;
    private int upCount;
    private int downCount;
    private int leftCount;
    private int rightCount;
    private boolean lastOneIHope;
    private Coordinate[] logicShots = {
        new Coordinate(0, 0), new Coordinate(0, 2), new Coordinate(0, 4), new Coordinate(0, 6),
        new Coordinate(0, 8), new Coordinate(1, 1), new Coordinate(1, 3), new Coordinate(1, 5),
        new Coordinate(1, 7), new Coordinate(1, 9), new Coordinate(2, 0), new Coordinate(2, 2),
        new Coordinate(2, 4), new Coordinate(2, 6), new Coordinate(2, 8), new Coordinate(3, 1),
        new Coordinate(3, 3), new Coordinate(3, 5), new Coordinate(3, 7), new Coordinate(3, 9),
        new Coordinate(4, 0), new Coordinate(4, 2), new Coordinate(4, 4), new Coordinate(4, 6),
        new Coordinate(4, 8), new Coordinate(5, 1), new Coordinate(5, 3), new Coordinate(5, 5),
        new Coordinate(5, 7), new Coordinate(5, 9), new Coordinate(6, 0), new Coordinate(6, 2),
        new Coordinate(6, 4), new Coordinate(6, 6), new Coordinate(6, 8), new Coordinate(7, 1),
        new Coordinate(7, 3), new Coordinate(7, 5), new Coordinate(7, 7), new Coordinate(7, 9),
        new Coordinate(8, 0), new Coordinate(8, 2), new Coordinate(8, 4), new Coordinate(8, 6),
        new Coordinate(8, 8), new Coordinate(9, 1), new Coordinate(9, 3), new Coordinate(9, 5),
        new Coordinate(9, 7), new Coordinate(9, 9)};

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
        //Arrays.fill(board, false);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                board[i][j] = false;
            }
        }
        up = false;
        down = false;
        left = false;
        right = false;
        upCount = 2;
        downCount = 2;
        rightCount = 2;
        leftCount = 2;

        randomShotCoutner = 0;
        safetynet = false;
        iCounter += 1;
        //	System.out.println("initalize  " + iCounter);
        //	shotGrid.clear();
        //	firstShot=false;
        udlr = 0;
        //	distanceCoutner=2;
        //	horizontalShipPos = false;
        //	horizontalShipNeg = false;
        //	verticalShipPos = false;
        //	verticalShipNeg = false;
        firstShotVariable = 0;
        foundHim = false;
        lastOneIHope = false;
        //	pinPoint=false;
        this.myFleet = new Fleet();
        this.rGen = new Random();
        int upDown = this.rGen.nextInt(2);
        //   x = 0;
        //   y = 0;
        v = -1;
        Collections.shuffle(Arrays.asList(logicShots));




        if (upDown == 0) {
            while (!this.myFleet.placeShip(new Coordinate(1, 7), HORIZONTAL, PATROL_BOAT)) {
                //	System.out.println("placing patrol");
            }
            while (!this.myFleet.placeShip(new Coordinate(this.rGen.nextInt(3), this.rGen.nextInt(10)), VERTICAL, SUBMARINE)) {
                //	System.out.println("placing sub");
            }
            while (!this.myFleet.placeShip(new Coordinate(this.rGen.nextInt(3) + 7, this.rGen.nextInt(10)), VERTICAL, AIRCRAFT_CARRIER)) {
                //		System.out.println("placing carrier");
            }
            while (!this.myFleet.placeShip(new Coordinate(this.rGen.nextInt(10), this.rGen.nextInt(3) + 7), HORIZONTAL, BATTLESHIP)) {
                //		System.out.println("placing battleship");
            }
            while (!this.myFleet.placeShip(new Coordinate(this.rGen.nextInt(4), this.rGen.nextInt(10)), VERTICAL, DESTROYER)) {
                //		System.out.println("placing destroyer");
            }


        } else {

            while (!this.myFleet.placeShip(new Coordinate(3, 1), VERTICAL, PATROL_BOAT)) {
                //	System.out.println("placing patrol");
            }
            while (!this.myFleet.placeShip(new Coordinate(this.rGen.nextInt(10), this.rGen.nextInt(3) + 7), HORIZONTAL, AIRCRAFT_CARRIER)) {
                //	System.out.println("placing carrier");
            }
            while (!this.myFleet.placeShip(new Coordinate(this.rGen.nextInt(3) + 7, this.rGen.nextInt(10)), VERTICAL, BATTLESHIP)) {
                //	System.out.println("placing battleship");
            }
            while (!this.myFleet.placeShip(new Coordinate(this.rGen.nextInt(10), this.rGen.nextInt(4)), HORIZONTAL, DESTROYER)) {
                //	System.out.println("placing destroyer");
            }
            while (!this.myFleet.placeShip(new Coordinate(this.rGen.nextInt(10), this.rGen.nextInt(3)), HORIZONTAL, SUBMARINE)) {
                //	System.out.println("placing sub");
            }
        }


    }

    @Override
    public Fleet getFleet() {
        firstShotVariable = 0;
        return myFleet;
    }

    public Coordinate shotRandomizer() {
        x = this.rGen.nextInt(10);
        y = this.rGen.nextInt(10);

        return new Coordinate(x, y);
    }

    public void addToArray(Coordinate shotMade) {
        shotGrid.add(shotMade);
    }

    public Coordinate followOut() {
        int xCord = hitPoint.getX();
        int yCord = hitPoint.getY();
        if (up == true) {
            if (lastShotHit == true || lastOneIHope == true) {
                lastOneIHope = false;
                xCord += upCount;
                upCount += 1;
                if (xCord < 10 && board[xCord][yCord] == false) {
                    //System.out.println("upWorks  " + xCord + yCord);
                    return new Coordinate(xCord, yCord);
                }
                udlr = 0;
                upCount = 2;
                up = false;
                lastOneIHope = true;
                foundHim = false;
            }
            udlr = 0;
            upCount = 2;
            up = false;
            lastOneIHope = true;
            foundHim = false;


        }
        xCord = hitPoint.getX();
        yCord = hitPoint.getY();
        if (down == true) {
            if (lastShotHit == true || lastOneIHope == true) {
                lastOneIHope = false;
                xCord -= downCount;
                downCount += 1;
                if (xCord >= 0 && board[xCord][yCord] == false) {
                    //				System.out.println("downWorks  " +xCord+yCord);
                    return new Coordinate(xCord, yCord);
                }
                udlr = 0;
                downCount = 2;
                down = false;
                lastOneIHope = true;
                foundHim = false;
            }
            udlr = 0;
            downCount = 2;
            down = false;
            lastOneIHope = true;
            foundHim = false;


        }
        xCord = hitPoint.getX();
        yCord = hitPoint.getY();
        if (right == true) {
            if (lastShotHit == true || lastOneIHope == true) {
                lastOneIHope = false;
                yCord += rightCount;
                rightCount += 1;
                if (yCord < 10 && board[xCord][yCord] == false) {
                    //				System.out.println("rightWorks   "+xCord+yCord);
                    return new Coordinate(xCord, yCord);
                }
                udlr = 0;
                foundHim = false;
                rightCount = 2;
                right = false;
                lastOneIHope = true;
            }
            udlr = 0;
            foundHim = false;
            rightCount = 2;
            right = false;
            lastOneIHope = true;


        }
        udlr = 0;
        xCord = hitPoint.getX();
        yCord = hitPoint.getY();
        if (left == true) {
            if (lastShotHit == true || lastOneIHope == true) {
                lastOneIHope = false;
                yCord -= leftCount;
                leftCount += 1;
                if (yCord >= 0 && board[xCord][yCord] == false) {
                    //			System.out.println("leftWorks   "+xCord+yCord);
                    return new Coordinate(xCord, yCord);
                }
                leftCount = 2;
                left = false;
                lastOneIHope = true;
                foundHim = false;
            }
            leftCount = 2;
            left = false;
            lastOneIHope = true;
            foundHim = false;


        }
        /*	if(down==true){
         down=false;
         System.out.println("down Works");
         }
         if(right==true){
         right=false;
         System.out.println("right Works");
         }
         if(left==true){
         left=false;
         System.out.println("left Works");
         }*/
        return null;
    }

    public boolean shotChecker(int xPos, int yPos) {
        if (board[xPos][yPos] == true) {
            return false;
        }


        return true;
    }

    public Coordinate doAttack() {
        if (udlr > 3) {
            if (lastShotHit == true) {
                //		System.out.println("left is true");
                left = true;
                udlr = 0;
            } else {
                udlr = 0;
            }
        }
        //	System.out.println("V-Card:" + v);
        //System.out.println(firstShotVariable);

        if (firstShotVariable == 1 && safetynet == false) {
            foundHim = true;
            lastOneIHope = true;
        }

        if (foundHim == true) {
            safetynet = true;
            if (udlr == 0) {
                x = hitPoint.getX();
                x += 1;
                y = hitPoint.getY();
                if (x < 10 && board[x][y] == false) {
                    //		System.out.println("shot0 + cord " +x+y);
                    udlr += 1;
                    board[x][y] = true;
                    return new Coordinate(x, y);
                } else {
                    udlr += 1;
                }/*System.out.println("skip0 + cord " +x+y);*/
                lastShotHit = false;
            }
            if (udlr == 1) {
                if (lastShotHit == true) {
                    up = true;
                    //	System.out.println("up is true");

                }
                x = hitPoint.getX();
                x -= 1;
                y = hitPoint.getY();
                if (x >= 0 && board[x][y] == false) {
                    //	System.out.println("shot1 + cord " +x+y);
                    udlr += 1;
                    board[x][y] = true;
                    return new Coordinate(x, y);
                } else {
                    udlr += 1;
                }/*System.out.println("skip1 + cord " +x+y);*/
                lastShotHit = false;
            }
            if (udlr == 2) {
                if (lastShotHit == true) {
                    //	System.out.println("down is true");
                    down = true;
                }
                y = hitPoint.getY();
                y += 1;
                x = hitPoint.getX();
                if (y < 10 && board[x][y] == false) {
                    //		System.out.println("shot2 + cord " +x+y);
                    udlr += 1;
                    board[x][y] = true;
                    return new Coordinate(x, y);
                } else {
                    udlr += 1;
                }/*System.out.println("skip2 + cord " +x+y);*/
                lastShotHit = false;
            }
            if (udlr == 3) {
                if (lastShotHit == true) {
                    right = true;
                    //	System.out.println("right is true");

                }
                y = hitPoint.getY();
                y -= 1;
                x = hitPoint.getX();
                if (y >= 0 && board[x][y] == false) {
                    //	System.out.println("shot3 + cord " +x+y);
                    udlr++;
                    firstShotVariable = 0;
                    foundHim = false;
                    board[x][y] = true;
                    return new Coordinate(x, y);
                } else {
                    lastShotHit = false;
                    udlr++;
                    firstShotVariable = 0;
                    foundHim = false;/*System.out.println("skip3 + cord " +x+y );*/
                }
            }
        }

        if ((up == true || down == true || left == true || right == true) && foundHim == false) {
            Coordinate follower = followOut();
            if (follower != null) {
                //System.out.println("logic repeat " + follower.getX() + follower.getY());
                board[follower.getX()][follower.getY()] = true;
                return follower;
            } else {
                foundHim = false;
            }

        }

        if ((v < 49 && foundHim == false) || (safetynet == true && udlr == 0)) {
            firstShotVariable = 0;
            randomShotCoutner++;
            safetynet = false;
            udlr = 0;
            //	System.out.println("randomShot " + randomShotCoutner);
            v++;
            //	System.out.print(logicShots[v].getX());
            //	System.out.print(logicShots[v].getY());
            //	System.out.println();
            if (board[logicShots[v].getX()][logicShots[v].getY()] == true) {
                //		System.out.println("no Shot repeat point");
                hitPoint = logicShots[v];
            }
            //	System.out.println("logic shot point  " + logicShots[v].getX()+logicShots[v].getY());
            hitPoint = logicShots[v];
            board[logicShots[v].getX()][logicShots[v].getY()] = true;
            return logicShots[v];
        }

        if (v == 49) {
            //v=0;
            //		 System.out.println("safetyShot");
            return new Coordinate(0, 9);
        }

        //	System.out.println("lastResort");
        firstShotVariable = 0;
        foundHim = false;
        return null;//logicShots[v];

    }

    @Override
    public Coordinate makeAttack() {

        return doAttack();


    }

    @Override
    public void resultOfAttack(int result) {
        if (result != MISS) {
            lastShotHit = true;
            firstShotVariable += 1;
        }
        if (result == MISS) {
            lastShotHit = false;
        }

    }

    @Override
    public void opponentAttack(Coordinate coord) {
        // TODO Auto-generated method stub
    }

    @Override
    public void resultOfGame(int result) {
        for (int i = 9; i > -1; i--) {
            for (int j = 9; j > -1; j--) {
                //		System.out.print(board[i][j] + "   ");
            }
            //	System.out.println();
        }

    }
}
