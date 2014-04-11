
import java.util.ArrayList;
import java.util.Random;

public class CaptainObvious implements Captain {

    protected Random generator;
    protected Fleet myFleet;
    protected int[][] boardAttacks, crossValues, theirFirstShots, theirShots, myShots, myHits;//board, misses are 1's and 0's if spot hasn't been fired at and 2's for hits
    protected int[][] totalHits2Boat, totalHits3BoatVert, totalHits3BoatHor;//the total shots fired on the patrol boat's,submarine's and destroyer's possible placement.
    protected int attackX, attackY, counter, mod, modnum, lastX, lastY, numAttacks, randomAttacks, assumingVertical, gamesWon;
    public int highCrossX, highCrossY, targetX, targetY, numMatches/*,secondLeastX,secondLeastY*/;
    public int patBPX, patBPY, destBPX, destBPY, subBPX, subBPY;//the boats placement x and y
    protected int[] theirShipVerticalities;//indexs:0=patrol boat 1=destroyer 2=sub 3=battleship 4=aircraft carrier
    //array of 4 integers, target's X, target's Y and what ship it is. This only stores the first hit of each ship
    private ArrayList<int[]> targets;//4th digit is horizontality, 0 for no, 1 for yes, -1 for unknown.
    //5th digit is length
    private String opponent = "";
    private boolean sameOpponent = false;
    private int gamesAgainst = 0;
    private int shipPlacement1Wins, shipPlacement2Wins, shipPlacement3Wins, leastHitHits, mostHitHits;

    /**
     * This is captain Obvious. It's attack method when searching for a ship is
     * to calculate which shot will hit the spot that has the most open tiles on
     * all 4 sides. It's ship placement strategy is it tests 3 different
     * strategies in the first half of the match then chooses the one that works
     * the best for the rest of the match. The 3 strategies are all vertical,
     * all horizontal and placing on the spots that haven't been shot at.
     *
     * @author Josh Loschen, Alec Matschiner 10-24-13
     */
    @Override
    public Coordinate makeAttack() {
        numAttacks++;
        if (numAttacks % 400 == 0) {
            resetMostCommonHits();
        }
        if (targets.isEmpty()) {//no current targets
            randomAttacks++;
            if (randomAttacks == 1) {
                findLeastHitSpot(5, 0, 10, 5);
            } else if (randomAttacks == 2) {
                findLeastHitSpot(5, 5, 10, 10);
            } else if (randomAttacks == 3) {
                findLeastHitSpot(0, 5, 5, 10);
            } else if (randomAttacks == 4) {
                findLeastHitSpot(0, 0, 5, 5);
            } else if (randomAttacks == 5) {
                findMostCommonSpot(5, 0, 10, 5);
            } else if (randomAttacks == 6) {
                findMostCommonSpot(0, 5, 5, 10);
            } else if (randomAttacks == 7) {
                findMostCommonSpot(0, 0, 5, 5);
            } else if (randomAttacks == 8) {
                findMostCommonSpot(5, 5, 10, 10);
            } else {
                computeCrossValues();
                setHighestCrossValue();
                attackX = highCrossX;
                attackY = highCrossY;
            }//!adjacentShot() returns true if coodinates are next to another shot
        } else {//atleast 1 target. Work on first target in list.
            targetX = targets.get(0)[0];//int 
            targetY = targets.get(0)[1];// int taken off
            if (targets.get(0)[3] == 0) {//ship is vertical
                setVerticalAttack();
            } else if (targets.get(0)[3] == 1) {//ship is horizontal
                setHorizontalAttack();
            } else {
                if (gamesAgainst % 2 == 0) {
                    setHorizontalAttack();
                    setVerticalAttack();
                } else {
                    setVerticalAttack();
                    setHorizontalAttack();
                }
            }
        }
        while (boardAttacks[attackX][attackY] != 0) {//now just looking for a shot that hasn't been done yet
            attackX = generator.nextInt(10);
            attackY = generator.nextInt(10);
        }
        myShots[attackX][attackY]++;
        boardAttacks[attackX][attackY] = 1;//update board, this assumes it is a miss, will get updated later
        return new Coordinate(attackX, attackY);
    }

    private void setHorizontalAttack() {
        for (int i = 0; i < targets.get(0)[4]; i++) {
            if (targetX + i < 10) {//check for index out of bounds
                if (boardAttacks[targetX + i][targetY] == 0) {//looking in positive x direction
                    if (noMissesBetween(i, 1, targetX, targetY)) {
                        attackX = targetX + i;
                        attackY = targetY;
                        break;//get out of for loop, stop looking once find closest spot.
                    }
                }
            }
            if (targetX - i > -1) {//check for index out of bounds
                if (boardAttacks[targetX - i][targetY] == 0) {
                    if (noMissesBetween(i, 3, targetX, targetY)) {
                        attackX = targetX - i;
                        attackY = targetY;
                        break;//get out of for loop, stop looking once find closest spot.
                    }
                }
            }
        }
    }

    private void setVerticalAttack() {
        for (int i = 0; i < targets.get(0)[4]; i++) {
            if (targetY + i < 10) {//check for index out of bounds
                if (boardAttacks[targetX][targetY + i] == 0) {//looking in positive y direction
                    if (noMissesBetween(i, 0, targetX, targetY)) {
                        attackX = targetX;
                        attackY = targetY + i;
                        break;//get out of for loop, stop looking once find closest spot.
                    }
                }
            }
            if (targetY - i > -1) {//check for index out of bounds
                if (boardAttacks[targetX][targetY - i] == 0) {
                    if (noMissesBetween(i, 2, targetX, targetY)) {//second param is direction 0=up,1=right,2=down,3=left
                        attackX = targetX;
                        attackY = targetY - i;
                        break;//get out of for loop, stop looking once find closest spot.
                    }
                }
            }
        }
    }

    private void findLeastHitSpot(int startX, int startY, int endX, int endY) {
        int leastHitX = endX - 3;
        int leastHitY = endY - 3;
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                if (myShots[x][y] < myShots[leastHitX][leastHitY]) {
                    leastHitX = x;
                    leastHitY = y;
                }
            }
        }
        attackX = leastHitX;
        attackY = leastHitY;
    }

    private void findMostCommonSpot(int startX, int startY, int endX, int endY) {
        int mostHitX = endX - 2;
        int mostHitY = endY - 2;
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                if (myHits[x][y] > myHits[mostHitX][mostHitY]) {
                    mostHitX = x;
                    mostHitY = y;
                }
            }
        }
        attackX = mostHitX;
        attackY = mostHitY;
        if (gamesAgainst % 4 == 0) {
            if (randomAttacks == 5) {
                attackX--;
                attackY++;
            } else if (randomAttacks == 6) {
                attackX++;
                attackY--;
            } else if (randomAttacks == 7) {
                attackX++;
                attackY++;
            } else if (randomAttacks == 8) {
                attackX--;
                attackY--;
            }
        }
    }

    private void resetMostCommonHits() {
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                myHits[x][y] = 0;
            }
        }
    }

    /**
     * computes the crossValues for each spot using the method below to do the
     * calculation
     */
    private void computeCrossValues() {
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (boardAttacks[x][y] == 0) {
                    crossValues[x][y] = compute(x, y);
                } else {
                    crossValues[x][y] = 0;
                }
            }
        }
    }

    /**
     * Computes how many spaces on either side of a given tile haven't been shot
     * at yet Called in computeCrossValues()
     */
    private int compute(int x, int y) {
        int crossValue = 0;
        int up, down, left, right;
        up = down = left = right = 1;
        if (y < 9) {
            while (y + up < 10) {
                if (boardAttacks[x][y + up] == 0) {
                    crossValue++;
                } else {
                    break;//stop counting once hit something other than 1
                }
                up++;
            }
        }
        if (y > 0) {
            while (y - down > -1) {
                if (boardAttacks[x][y - down] == 0) {
                    crossValue++;
                } else {
                    break;
                }
                down++;
            }
        }
        if (x < 9) {
            while (x + right < 10) {
                if (boardAttacks[x + right][y] == 0) {
                    crossValue++;
                } else {
                    break;//stop counting once hit something other than 1
                }
                right++;
            }
        }
        if (x > 0) {
            while (x - left > -1) {
                if (boardAttacks[x - left][y] == 0) {
                    crossValue++;
                } else {
                    break;//stop counting once hit something other than 1
                }
                left++;
            }
        }
        crossValue = crossValue - adjacentShot(x, y);
        return crossValue;
    }

    private void setHighestCrossValue() {
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (boardAttacks[x][y] == 0) {//haven't already attacked at these coordinates
                    if (crossValues[x][y] >= crossValues[highCrossX][highCrossY]) {//if this spot is better or equal to
                        if (crossValues[x][y] > crossValues[highCrossX][highCrossY]) {//if strictly better then update highest spot
                            highCrossX = x;
                            highCrossY = y;
                        } else {
                            if (myShots[x][y] < myShots[highCrossX][highCrossY]) {//if they are tied then tie breaker is how many hits in previous game at the coordinates
                                highCrossX = x;
                                highCrossY = y;
                            }
                        }
                    }
                }
            }
        }
    }

    private int adjacentShot(int x, int y) {
        int sum = 0;
        if (x < 9) {
            if (boardAttacks[x + 1][y] == 1 || boardAttacks[x + 1][y] == 2)//if spot above is a miss or a hit, similar if test for other 3 adjacent spots below 
            {
                sum++;
            }
        }
        if (x > 0) {
            if (boardAttacks[x - 1][y] == 1 || boardAttacks[x - 1][y] == 2) {
                sum++;
            }
        }
        if (y < 9) {
            if (boardAttacks[x][y + 1] == 1 || boardAttacks[x][y + 1] == 2) {
                sum++;
            }
        }
        if (y > 0) {
            if (boardAttacks[x][y - 1] == 1 || boardAttacks[x][y - 1] == 2) {
                sum++;
            }
        }
        return sum;
    }

    /**
     * this method is used in makeAttack() inside for loops to make sure not
     * firing on opposite side of miss from known hit direction: 0=up 1=right
     * 2=down 3=left
     */
    private boolean noMissesBetween(int i, int direction, int targetX, int targetY) {
        switch (direction) {
            case 0:
                for (int j = 1; j < i; j++) {//i is same i from for loop in makeAttack()
                    if (boardAttacks[targetX][targetY + j] == 1) {
                        return false;
                    }
                }
                break;
            case 1:
                for (int j = 1; j < i; j++) {//i is same i from for loop in makeAttack()
                    if (boardAttacks[targetX + j][targetY] == 1) {
                        return false;
                    }
                }
                break;
            case 2:
                for (int j = 1; j < i; j++) {//i is same i from for loop in makeAttack()
                    if (boardAttacks[targetX][targetY - j] == 1) {
                        return false;
                    }
                }
                break;
            case 3:
                for (int j = 1; j < i; j++) {//i is same i from for loop in makeAttack()
                    if (boardAttacks[targetX - j][targetY] == 1) {
                        return false;
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public void resultOfAttack(int result) {
        int ship = result % 10;
        if (result > 9 && result < 20) {//attack was a hit
            boardAttacks[attackX][attackY] = 2;
            myHits[attackX][attackY]++;
            if (targets.isEmpty()) {
                int length = getLength(ship);
                int[] newTarget = {attackX, attackY, ship, -1, length};
                targets.add(newTarget);
            } else if (!shipInTargets(ship)) {//if ship has been hit but not yet targeted
                int length = getLength(ship);
                int[] newTarget = {attackX, attackY, ship, -1, length};
                targets.add(newTarget);
            } else {//ship has been hit and is already in targets
                int index = findShipIndex(ship);
                if (targets.get(index)[3] == -1) {//if horizontality is unknown set it
                    if (attackX == targets.get(index)[0]) {//attack and target have same x value
                        targets.get(index)[3] = 0;
                        theirShipVerticalities[ship]--;
                    } else if (attackY == targets.get(index)[1]) {//attack and target have same y value
                        targets.get(index)[3] = 1;
                        theirShipVerticalities[ship]++;
                    }
                }
            }
        } else if (result > 19 && result < 29) {//attack sunk a ship
            if (shipInTargets(ship)) {//shouldn't be needed since if ship is sunk it was already hit...but just in case
                targets.remove(findShipIndex(ship));
            }
        } else {//attack was a miss
            //if have hit spots on either side of target spot and still haven't hit the ship then we know the direction
            //of the ship.		
            if (!targets.isEmpty()) {//if have a target check to get horizontality
                if (missesOnBothSides()) {
                    if (targets.get(0)[3] == -1) {
                        targets.get(0)[3] = 0;
                    }
                } else if (missesAboveAndBelow()) {
                    if (targets.get(0)[3] == -1) {
                        targets.get(0)[3] = 1;
                    }
                }
            }
        }
    }

    private boolean missesAboveAndBelow() {//used to compute crossValues in compute method
        int x = targets.get(0)[0];
        int y = targets.get(0)[1];
        if (y == 0) {
            if (boardAttacks[x][y + 1] == 1) {
                return true;
            }
        } else if (y > 0 && y < 9) {
            if (boardAttacks[x][y + 1] == 1 && boardAttacks[x][y - 1] == 1) {
                return true;
            }
        } else if (y == 9) {
            if (boardAttacks[x][y - 1] == 1) {
                return true;
            }
        }
        return false;
    }

    private boolean missesOnBothSides() {//used to compute crossValues in compute method
        int x = targets.get(0)[0];
        int y = targets.get(0)[1];
        if (x == 0) {
            if (boardAttacks[x + 1][y] == 1) {
                return true;
            }
        } else if (x > 0 && x < 9) {
            if (boardAttacks[x + 1][y] == 1 && boardAttacks[x - 1][y] == 1) {
                return true;
            }
        } else if (x == 9) {
            if (boardAttacks[x - 1][y] == 1) {
                return true;
            }
        }
        return false;
    }

    private int getLength(int ship) {
        switch (ship) {
            case 0:
                return 2;
            case 1:
                return 3;
            case 2:
                return 3;
            case 3:
                return 4;
            case 4:
                return 5;
        }
        return 5;
    }

    private int findShipIndex(int ship) {//linear search to find index of a ship in target list
        for (int i = 0; i < targets.size(); i++) {
            if (targets.get(i)[2] == ship) {
                return i;
            }
        }
        return -1;
    }

    private boolean shipInTargets(int ship) {//checks to see if the ship that was hit has already been targeted
        boolean isIn = false;
        for (int i = 0; i < targets.size(); i++) {
            if (targets.get(i)[2] == ship) {
                isIn = true;
            }
        }
        return isIn;
    }

    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    @Override
    public void opponentAttack(Coordinate coord) {
        this.theirShots[coord.getX()][coord.getY()]++;
        if (numAttacks < 10 && sameOpponent) {
            theirFirstShots[coord.getX()][coord.getY()]++;
        }
    }

    @Override
    public void resultOfGame(int result) {
        //////////////////////////Ship placement 1 == Verical
        if (gamesAgainst <= (double) numMatches * (1 / 6.0)) {
            if (result == 1) {
                shipPlacement1Wins++;
            }
            //////////////////////// Ship PLacement 2 == Hoz
        } else if (gamesAgainst > (double) numMatches * (1 / 6.0) && gamesAgainst <= (double) numMatches * (2 / 6.0)) {
            if (result == 1) {
                shipPlacement2Wins++;
            }
            ////////////////////////////////// Ship placement 3 == HEAT
        } else if (gamesAgainst > (double) numMatches * (2 / 6.0) && gamesAgainst <= (double) numMatches * (3 / 6.0)) {
            if (result == 1) {
                shipPlacement3Wins++;
            }
        }
    }

    public void calculate2BoatSpot() {//placement method
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (y < 9) {
                    totalHits2Boat[x][y] = theirShots[x][y] + theirShots[x][y + 1];
                } else {
                    totalHits2Boat[x][y] = 9999999;
                }
            }
        }
    }

    public void calculate2BoatSpotHor() {//placement method
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (x < 9) {
                    totalHits2Boat[x][y] = theirShots[x][y] + theirShots[x + 1][y];
                } else {
                    totalHits2Boat[x][y] = 9999999;
                }
            }
        }
    }
    //this one checks vertically

    public void calculateDestroyerSpotVert() {//placement method 
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (y < 8) {
                    totalHits3BoatVert[x][y] = theirShots[x][y] + theirShots[x][y + 1] + theirShots[x][y + 2];
                } else {
                    totalHits3BoatVert[x][y] = 9999999;
                }
                if (x == patBPX && (y == patBPY || y == patBPY - 1 || y == patBPY - 2 || y == patBPY + 1)) {
                    totalHits3BoatVert[x][y] = 9999999;//number so big that it definately won't be chosen.
                }
            }
        }
    }

    public void calculateDestroyerSpotHor() {//placement method 
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (x < 8) {
                    totalHits3BoatVert[x][y] = theirShots[x][y] + theirShots[x + 1][y] + theirShots[x + 2][y];
                } else {
                    totalHits3BoatVert[x][y] = 9999999;
                }
                if ((x == patBPX - 1 || x == patBPX || x == patBPX + 1 || x == patBPX - 2) && (y == patBPY)) {//if(x==patBPX && (y==patBPY ||y==patBPY-1||y==patBPY-2||y==patBPY+1)){
                    totalHits3BoatVert[x][y] = 9999999;//number so big that it definately won't be chosen.
                }
            }
        }
    }

    public void calculateSubSpotHor() {//placement method 
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (x < 8) {
                    totalHits3BoatHor[x][y] = theirShots[x][y] + theirShots[x + 1][y] + theirShots[x + 2][y];
                } else {
                    totalHits3BoatHor[x][y] = 9999999;
                }
                if (((x == patBPX) || (x == patBPX - 1) || (x == patBPX - 2)) && ((y == patBPY) || (y == patBPY + 1))) {//make sure won't run into patrol boat
                    totalHits3BoatHor[x][y] = 999999;//num so big this spot will never get chosen
                }
                if (((x == destBPX) || x == (destBPX - 1) || (x == destBPX - 2)) && ((y == destBPY) || (y == destBPY + 1) || (y == destBPY + 2))) {//won't run into destroyer
                    totalHits3BoatHor[x][y] = 999999;
                }
            }
        }
    }

    public void findBest2BoatSpot() {//placement method
        int bestX = 0;
        int bestY = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (totalHits2Boat[x][y] < totalHits2Boat[bestX][bestY]) {
                    bestX = x;
                    bestY = y;
                }
            }
        }
        patBPX = bestX;
        patBPY = bestY;
    }

    public void findBestDestroyerSpot() {//placement method, destroyer is placed vertical
        int bestX = 7;
        int bestY = 7;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (totalHits3BoatVert[x][y] < totalHits3BoatVert[bestX][bestY]) {
                    bestX = x;
                    bestY = y;
                }
            }
        }
        destBPX = bestX;
        destBPY = bestY;
    }

    public void findBestSubSpot() {//placement method, sub is placed vertical
        int bestX = 5;
        int bestY = 5;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (totalHits3BoatHor[x][y] < totalHits3BoatHor[bestX][bestY]) {
                    bestX = x;
                    bestY = y;
                }
            }
        }
        subBPX = bestX;
        subBPY = bestY;
    }

    public void HEATMF() {
        calculate2BoatSpot();
        findBest2BoatSpot();
        calculateDestroyerSpotVert();
        findBestDestroyerSpot();
        calculateSubSpotHor();
        findBestSubSpot();
        while (!myFleet.placeShip(patBPX, patBPY, VERTICAL, PATROL_BOAT)) {
        }

        if (patBPX == destBPX && patBPY == destBPY) {
            destBPX = (destBPX + 5) % 10;
            destBPY = (destBPY + 5) % 10;
        }
        while (!myFleet.placeShip(destBPX, destBPY, VERTICAL, DESTROYER)) {
        }
        if (gamesAgainst > 2) {
            while (!myFleet.placeShip(subBPX, subBPY, HORIZONTAL, SUBMARINE)) {
            }
        } else {
            while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), SUBMARINE)) {
            }
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), BATTLESHIP)) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), AIRCRAFT_CARRIER)) {
        }
    }
    /////////////////////////////////////////////////////////////////////
    // SET VERTICAL AND HORIZONTAL METHODS
    /////////////////////////////////////////////////////////////////////

    public void setShipsVertical() {
        calculate2BoatSpot();
        findBest2BoatSpot();
        calculateDestroyerSpotVert();
        findBestDestroyerSpot();
        while (!myFleet.placeShip(patBPX, patBPY, VERTICAL, PATROL_BOAT)) {
        }
        while (!myFleet.placeShip(destBPX, destBPY, VERTICAL, DESTROYER)) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), VERTICAL, SUBMARINE)) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), VERTICAL, BATTLESHIP)) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), VERTICAL, AIRCRAFT_CARRIER)) {
        }
    }

    public void setShipsHorizontal() {
        calculate2BoatSpotHor();
        findBest2BoatSpot();
        calculateDestroyerSpotHor();
        findBestDestroyerSpot();
        while (!myFleet.placeShip(patBPX, patBPY, HORIZONTAL, PATROL_BOAT)) {
        }
        while (!myFleet.placeShip(destBPX, destBPY, HORIZONTAL, DESTROYER)) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), HORIZONTAL, SUBMARINE)) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), HORIZONTAL, BATTLESHIP)) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), HORIZONTAL, AIRCRAFT_CARRIER)) {
        }
    }

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
        if (this.opponent == opponent) {//facing the same opponent
            sameOpponent = true;
            gamesAgainst++;
        } else {//                        facing a new opponent
            leastHitHits = mostHitHits = 0;
            this.numMatches = numMatches;
            theirShipVerticalities = new int[5];
            theirShots = new int[10][10];
            theirFirstShots = new int[10][10];
            myShots = new int[10][10];
            myHits = new int[10][10];
            totalHits2Boat = new int[10][10];
            totalHits3BoatVert = new int[10][10];
            totalHits3BoatHor = new int[10][10];
            shipPlacement1Wins = shipPlacement2Wins = shipPlacement3Wins = 0;
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    theirShots[x][y] = myHits[x][y] = myShots[x][y] = 0;
                }
            }
            this.opponent = opponent;
            for (int i = 0; i < 5; i++) {
                theirShipVerticalities[i] = 0;
            }
            gamesAgainst = 0;
            sameOpponent = false;
        }
        generator = new Random();
        myFleet = new Fleet();
        boardAttacks = new int[10][10];
        crossValues = new int[10][10];//crossValues is the number of open tiles on each of the 4 sides added up
        numAttacks = randomAttacks = 0;
        highCrossX = highCrossY = generator.nextInt(10);
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                boardAttacks[x][y] = 0;
                crossValues[x][y] = 0;
            }
        }
        targets = new ArrayList<int[]>();
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////START 1st PLACEMENT STRATEGY HERE -------> Vertical
////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (gamesAgainst <= (double) numMatches * (1 / 6.0)) {//1rst ship placement method
            setShipsVertical();
        } //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////START 2nd PLACEMENT STRATEGY HERE ------> Hoz
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        else if (gamesAgainst > (double) numMatches * (1 / 6.0) && gamesAgainst <= (double) numMatches * (2 / 6.0)) {//2nd ship placement method*/
            setShipsHorizontal();
        } ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Start 3rd -----> HEATMF
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        else if (gamesAgainst > (double) numMatches * (2 / 6.0) && gamesAgainst < (double) numMatches * (3 / 6.0)) {
            HEATMF();
        }
        if (gamesAgainst >= (double) numMatches * (1 / 2.0)) {
            if (shipPlacement1Wins > shipPlacement2Wins && shipPlacement1Wins > shipPlacement3Wins) {
                setShipsVertical();
            } else if (shipPlacement2Wins > shipPlacement1Wins && shipPlacement2Wins > shipPlacement3Wins) {
                setShipsHorizontal();
            } else if (shipPlacement3Wins > shipPlacement1Wins && shipPlacement3Wins > shipPlacement2Wins) {
                HEATMF();
            }
        }
    }
}
