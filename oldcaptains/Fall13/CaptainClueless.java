
import java.util.Random;

public class CaptainClueless implements Captain {

    /**
     * A random number generated which I use for everything (because I'm LOCO!!)
     */
    protected Random generator;
    /**
     * My fleet (you probably want to keep this).
     */
    protected Fleet myFleet;
    protected int x, y, roulette, tempX, tempY, counter, twoX, twoY;
    protected boolean[][] canAttack;
    protected int[][] hit;
    protected boolean hitOnce, kill, two, three, threeAgain, four, five, inTwo, inThree, inThree2, inFour, inFive;		//hit, and destroyed
    protected boolean[] quadDone;
    protected int[] xOff, yOff, xLeft, yLeft, hitX, hitY;
    private int offIndex, curQud, size, numHits;

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
        generator = new Random();
        myFleet = new Fleet();
        quadDone = new boolean[4];
        xOff = new int[10];
        yOff = new int[10];
        xLeft = new int[100];
        yLeft = new int[100];
        size = -1;
        for (int i = 0; i < 4; i++) {
            quadDone[i] = false;
        }
        canAttack = new boolean[10][10];		//What I'm attacking
        hit = new int[10][10];	//What I have hit

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                canAttack[i][j] = true;
                hit[i][j] = -1;
            }
        }
        xOff[0] = 0;
        xOff[1] = 1;
        xOff[2] = 2;
        xOff[3] = 3;
        xOff[4] = 0;
        xOff[5] = 1;
        xOff[6] = 2;
        xOff[7] = 3;
        xOff[8] = 4;
        xOff[9] = 4;

        yOff[0] = 1;
        yOff[1] = 2;
        yOff[2] = 3;
        yOff[3] = 4;
        yOff[4] = 3;
        yOff[5] = 4;
        yOff[6] = 0;
        yOff[7] = 1;
        yOff[8] = 2;
        yOff[9] = 0;

        offIndex = -1;
        curQud = 0;




        y = -1;
        x = -1;
        roulette = -1;
        hitOnce = false;					//Hit but not killed
        kill = false;					//Death to all who oppose me
        two = true;
        inTwo = false;
        three = true;
        threeAgain = true;
        inThree = false;
        inThree2 = false;
        four = true;
        inFour = false;
        five = true;
        inFive = false;
        counter = 0;

        // Each type of ship must be placed on the board.  Note that the .place method return whether it was
        // possible to put a ship at the indicated position.  If the coordinates were not on the board or if
        // it overlapped with a ship you already tried to place it will return false.
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

    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    @Override
    public Coordinate makeAttack() {
        if (offIndex == -1) {
            curQud = generator.nextInt(4);
            offIndex = 0;
        } else if (offIndex > 9) {
            quadDone[curQud] = true;
            offIndex = 0;
            curQud++;
            if (curQud > 3) {
                curQud = 0;
            }
        }
        if (quadDone[curQud] == true) {
            Coordinate local = new Coordinate(-1, -1);
            local = beSmart();

            //pick random for now
//	    		int rx = generator.nextInt(10);
//	    		int ry = generator.nextInt(10);
//	    		
//	    		while (canAttack[rx][ry] == false)
//	    		{
//	    			rx = generator.nextInt(10);
//		    		ry = generator.nextInt(10);
//	    		}
            canAttack[local.getX()][local.getY()] = false;

            return local;

        }

        int startX = 0;
        int startY = 0;

        if (curQud == 0) {
            startX = 5;
            startY = 5;
        } else if (curQud == 1) {
            startX = 0;
            startY = 5;
        } else if (curQud == 2) {
            startX = 0;
            startY = 0;
        } else if (curQud == 3) {
            startX = 5;
            startY = 0;
        }


        int hereX;
        int hereY;

        hereX = startX + yOff[offIndex];
        hereY = startY + xOff[offIndex];

        ++offIndex;
        canAttack[hereX][hereY] = false;
        return new Coordinate(hereX, hereY);
//	    	int i = 0;
//	    	boolean done = false;
//	    	
//	    	
//	    	while ((false == done) && (i < 10))
//	    	{
//	    		int j = 0;
//	    		while ((false == done) && (j < 10))
//	    		{
//	    			if (canAttack[i][j] == true)
//	    			{
//	    				hereX = i;
//	    				hereY = j;
//	    				canAttack[i][j] = false;
//	    				done = true;
//	    			}
//	    			++j;
//	    		}
//	    		++i;
//	    	}
//
//	    	
//			return new Coordinate(hereX, hereY);

    }

    public Coordinate beSmart() {
//			int hereX =-1;
//			int hereY =-1;
        Coordinate retVal = new Coordinate(-1, -1);

        if (five == true) {
            retVal = huntFive();
        } else if (four == true) {
            retVal = huntFour();
        } else if (three == true) {
            retVal = huntThree();
        } else if (threeAgain == three) {
            retVal = huntThreeAgain();
        } else if (two == true) {
            retVal = huntTwo();
        } else {
            retVal = pickRandom();
        }

        return retVal;
    }

    // -------------------------------------------------------------
    // pickRandom
    // picks a Random not previously selected cell
    // -------------------------------------------------------------
    public Coordinate pickRandom() {
        // make a list of all possible points NOT selected
        // 0 to 100 of them

        int pointsLeft = 0;    // this will be how many really are left

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (canAttack[i][j] == true) {
                    xLeft[pointsLeft] = i;
                    yLeft[pointsLeft] = j;
                    ++pointsLeft;
                }
            }
        }

        int selectOne = generator.nextInt(pointsLeft); // range from 0 to pointsLeft -1

        canAttack[xLeft[selectOne]][yLeft[selectOne]] = false;
        // but this should get returned to makeAttack

        return new Coordinate(xLeft[selectOne], yLeft[selectOne]);

    }

    public Coordinate huntFive() {
        size = 5;
        numHits = 0;
        hitX = new int[size - 1];
        hitY = new int[size - 1];    // max hits is 4 else its already sunk

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (hit[i][j] == HIT_AIRCRAFT_CARRIER) {
                    hitX[numHits] = i;
                    hitY[numHits] = j;
                    ++numHits;
                }
            }
        }

        // if numHits >=size we have a problem may want to print message
        // for now just go random
        if ((numHits <= 0) || (numHits >= size)) {
            return pickRandom();
        }

        if (numHits > 1) {
            if (hitX[0] == hitX[1]) {
                int i = hitX[0];
                int j = hitY[0] + 1;
                int incrementBy = 1;
                boolean done = false;
                while (false == done) {
                    if (canAttack[i][j] == true) {
                        done = true;
                    }
                    if (j > 9) {
                        j = hitY[0];
                        incrementBy = -1;
                    }
                    //if (j < 0) { done = true; }  /// <--- this should never happen
                    j = j + incrementBy;
                }
                return new Coordinate(i, j);
            } // end if X matches
            else if (hitY[0] == hitY[1]) {
                int i = hitX[0] + 1;
                int j = hitY[0];
                int incrementBy = 1;
                boolean done = false;
                while (false == done) {
                    if (canAttack[i][j] == true) {
                        done = true;
                    }
                    if (i > 9) {
                        i = hitX[0];
                        incrementBy = -1;
                    }
                    //if (i < 0) { done = true; }  /// <--- this should never happen
                    i = i + incrementBy;
                }
                return new Coordinate(i, j);
            } // end if Y matches
            else // this should never happen
            {
                return pickRandom();
            }
        } // end if numHits > 1


        Coordinate retVal;
        retVal = huntFour();
        return retVal;
    }

    // -------------------------------------------------------------
    // killFour
    // Hunt the FourGuy
    // -------------------------------------------------------------
    public Coordinate huntFour() {
        size = 4;
        numHits = 0;
        hitX = new int[size - 1];
        hitY = new int[size - 1];    // max hits is 3 else its already sunk

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (hit[i][j] == HIT_BATTLESHIP) {
                    hitX[numHits] = i;
                    hitY[numHits] = j;
                    ++numHits;
                }
            }
        }

        if ((numHits <= 0) || (numHits >= size)) {
            return pickRandom();
        }

        if (numHits > 1) {
            if (hitX[0] == hitX[1]) {
                int i = hitX[0];
                int j = hitY[0] + 1;
                int incrementBy = 1;
                boolean done = false;
                while (false == done) {
                    if (canAttack[i][j] == true) {
                        done = true;
                    }
                    if (j > 9) // start looking the other direction of first hit
                    {
                        j = hitY[0];
                        incrementBy = -1;
                    }
                    //if (j < 0) { done = true; }  /// <--- this should never happen
                    j = j + incrementBy;
                }
                return new Coordinate(i, j);
            } // end if X matches
            else if (hitY[0] == hitY[1]) {
                int i = hitX[0] + 1;
                int j = hitY[0];
                int incrementBy = 1;
                boolean done = false;
                while (false == done) {
                    if (canAttack[i][j] == true) {
                        done = true;
                    }
                    if (i > 9) // start looking the other direction of first hit
                    {
                        i = hitX[0];
                        incrementBy = -1;
                    }
                    //if (i < 0) { done = true; }  /// <--- this should never happen
                    i = i + incrementBy;
                }
                return new Coordinate(i, j);
            } // end if Y matches
            else // this should never happen
            {
                return pickRandom();
            }
        }
        Coordinate retVal;
        retVal = huntThree();
        return retVal;

    }

    // -------------------------------------------------------------
    // killThree
    // Hunt the FirstThreeGuy
    // -------------------------------------------------------------
    public Coordinate huntThree() {
        size = 3;
        numHits = 0;
        hitX = new int[size - 1];
        hitY = new int[size - 1];    // max hits is 2 else its already sunk

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (hit[i][j] == HIT_SUBMARINE) {
                    hitX[numHits] = i;
                    hitY[numHits] = j;
                    ++numHits;
                }
            }
        }


        if ((numHits <= 0) || (numHits >= size)) {
            return pickRandom();
        }

        if (numHits > 1) // we have a row or column
        {
            if (hitX[0] == hitX[1]) // the x's same so have row (an i coord match)
            {
                int i = hitX[0];
                int j = hitY[0] + 1;
                int incrementBy = 1;
                boolean done = false;
                while (false == done) {
                    if (canAttack[i][j] == true) {
                        done = true;
                    }
                    if (j > 9) // start looking the other direction of first hit
                    {
                        j = hitY[0];
                        incrementBy = -1;
                    }
                    //if (j < 0) { done = true; }  /// <--- this should never happen
                    j = j + incrementBy;
                }
                return new Coordinate(i, j);
            }
        }
        Coordinate retVal;
        retVal = huntThreeAgain();
        return retVal;
    }

    // -------------------------------------------------------------
    // killSecondThree
    // Hunt the SecondThreeGuy
    // -------------------------------------------------------------
    public Coordinate huntThreeAgain() {
        size = 3;
        int numHits = 0;
        hitX = new int[size - 1];
        hitY = new int[size - 1];    // max hits is 2 else its already sunk

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (hit[i][j] == HIT_DESTROYER) {
                    hitX[numHits] = i;
                    hitY[numHits] = j;
                    ++numHits;
                }
            }
        }

        if ((numHits <= 0) || (numHits >= size)) {
            return pickRandom();
        }

        if (numHits > 1) // we have a row or column
        {
            if (hitX[0] == hitX[1]) // the x's same so have row (an i coord match)
            {
                int i = hitX[0];
                int j = hitY[0] + 1;
                int incrementBy = 1;
                boolean done = false;
                while (false == done) {
                    if (canAttack[i][j] == true) {
                        done = true;
                    }
                    if (j > 9) // start looking the other direction of first hit
                    {
                        j = hitY[0];
                        incrementBy = -1;
                    }
                    //if (j < 0) { done = true; }  /// <--- this should never happen
                    j = j + incrementBy;
                }
                return new Coordinate(i, j);
            }
        }

        return huntTwo();
    }

    // -------------------------------------------------------------
    // killFour
    // Hunt the FourGuy
    // -------------------------------------------------------------
    public Coordinate huntTwo() {
        size = 2;
        int numHits = 0;
        hitX = new int[size - 1];
        hitY = new int[size - 1];    // max hits is 1 else its already sunk

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (hit[i][j] == HIT_PATROL_BOAT) {
                    hitX[numHits] = i;
                    hitY[numHits] = j;
                    ++numHits;
                }
            }
        }

        if ((numHits <= 0) || (numHits >= size)) {
            return pickRandom();
        }

        int i = hitX[0];
        int j = hitY[0];
        if ((i < 9) && (canAttack[i + 1][j] == true)) // right
        {
            return new Coordinate(i + 1, j);
        } else if ((j < 9) && (canAttack[i][j + 1] == true)) // down
        {
            return new Coordinate(i, j + 1);
        } else if ((i > 0) && (canAttack[i - 1][j] == true)) // left
        {
            return new Coordinate(i - 1, j);
        } else if ((j > 0) && (canAttack[i][j - 1] == true)) // up
        {
            return new Coordinate(i, j - 1);
        }

        return pickRandom();
    }

    @Override
    public void resultOfAttack(int result) {
        if (result == HIT_PATROL_BOAT) {
            hit[tempX][tempY] = HIT_PATROL_BOAT;
        } else if (result == HIT_DESTROYER) {
            hit[tempX][tempY] = HIT_DESTROYER;
        } else if (result == HIT_SUBMARINE) {
            hit[tempX][tempY] = HIT_SUBMARINE;
        } else if (result == HIT_BATTLESHIP) {
            hit[tempX][tempY] = HIT_BATTLESHIP;
        } else if (result == HIT_AIRCRAFT_CARRIER) {
            hit[tempX][tempY] = HIT_AIRCRAFT_CARRIER;
        } else if (result == SUNK_PATROL_BOAT) {
            two = false;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (hit[i][j] == HIT_PATROL_BOAT) {
                        hit[i][j] = SUNK_PATROL_BOAT;
                    }
                }
            }
        } else if (result == SUNK_DESTROYER) {
            three = false;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (hit[i][j] == HIT_DESTROYER) {
                        hit[i][j] = SUNK_DESTROYER;
                    }
                }
            }
        } else if (result == SUNK_SUBMARINE) {
            threeAgain = false;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (hit[i][j] == HIT_SUBMARINE) {
                        hit[i][j] = SUNK_SUBMARINE;
                    }
                }
            }
        } else if (result == SUNK_BATTLESHIP) {
            four = false;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (hit[i][j] == HIT_BATTLESHIP) {
                        hit[i][j] = SUNK_BATTLESHIP;
                    }
                }
            }
        } else if (result == SUNK_AIRCRAFT_CARRIER) {
            five = false;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (hit[i][j] == HIT_AIRCRAFT_CARRIER) {
                        hit[i][j] = SUNK_AIRCRAFT_CARRIER;
                    }
                }
            }
        }
    }

    @Override
    public void opponentAttack(Coordinate coord) {
        // Add code here to process or record opponent attacks
    }

    @Override
    public void resultOfGame(int result) {
        // Add code here to process the result of a game
    }
}