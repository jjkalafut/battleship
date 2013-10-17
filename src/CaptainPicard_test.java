
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * TODO Put here a description of what this class does.
 *
 * @author John. Created Mar 18, 2013.
 */
public class CaptainPicard_test implements Captain, Constants {

    private int[][] theirShots = new int[10][10];
    private boolean[][] theirGrid = new boolean[10][10];
    private int[][] hitsHeat = new int[10][10];
    private int[][] shotsHeat = new int[10][10];
    private double[][] avgHeat = new double[10][10];
    private double heatFactor = 0;
    private boolean[][] myShips = new boolean[10][10];
    private ArrayList<String[]> hitShips;
    private boolean[] enemyShips = new boolean[5];
    private int[] shipLength = {2, 3, 3, 4, 5};
    private ArrayList<Coordinate> availableShots = new ArrayList<Coordinate>();
    private Coordinate lastShot;
    private String opponent;
    private String lastOpp = "";
    private Random rGen;
    private Fleet myFleet;
    private int matchNumber = 0;
    private int matchTotal;
    private double cur_ver = 0;
    private double cur_hor = 0;
    private int turn_number;

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
        this.lastShot = null;
        this.turn_number = 0;
        this.matchTotal = numMatches;
        
        for (boolean[] b : this.theirGrid) {
            Arrays.fill(b, false);
        }
        //true mens enemy ship is still alive
        Arrays.fill(this.enemyShips, true);
        this.hitShips = new ArrayList<String[]>();
        for (int g = 0; g < 5; g++) {
            this.hitShips.add(null);
        }
        //see if this is the same opponent, if so keep track of matches
        this.opponent = opponent;
        if (opponent.equals(this.lastOpp)) {
            this.matchNumber++;
        } //else reset all the opponent data
        else {
            this.theirShots = new int[10][10];
            this.matchNumber = 0;
            this.shotsHeat = new int[10][10];
            this.hitsHeat = new int[10][10];
            this.avgHeat = new double[10][10];
            this.lastOpp = opponent;
        }

        //create a list of where my ships are
        for (boolean[] b : this.myShips) {
            Arrays.fill(b, false);
        }
        //Create a new fleet
        this.rGen = new Random();

        this.myFleet = new Fleet();
        for (int shipType = 0; shipType < 5; shipType++) {
            boolean placed = false;
            //if we don't have enough data on the opponent, randomly distribute the ships
            if (this.matchNumber <= (numMatches / 100)) {
                while (!placed) {
                    int baseCoord = this.rGen.nextInt(100);
                    placed = true;
                    if (!this.myFleet.placeShip(new Coordinate(baseCoord % 10, baseCoord / 10 - this.shipLength[shipType]), VERTICAL, shipType)) {
                        if (!this.myFleet.placeShip(new Coordinate(baseCoord % 10 - this.shipLength[shipType], baseCoord / 10), HORIZONTAL, shipType)) {
                            placed = false;
                        }
                    }
                }
            } //else pot the ships where they have shot least
            else {
                int[] placement = leastShotPlace(this.shipLength[shipType]);
                this.myFleet.placeShip(placement[0], placement[1], placement[2], shipType);
            }
        }
        
        this.heatFactor = 100.0 * (double) (this.matchNumber) / (double) (this.matchTotal);

    }

    private int[] leastShotPlace(int shipLen) {
        int bestRect = -1;
        Coordinate rectCoord = null;
        int orientation = 1;
        //try best verticle rectangle
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
        //try best horizontal rectangle
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
        //System.out.println(rectCoord.getX()+" "+rectCoord.getY()+" - "+orientation);
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
        return this.myFleet;
    }

    private void shotHere(Coordinate c) {
    	//System.out.println("the shot here: "+c);
        this.shotsHeat[c.getX()][c.getY()]++;
        this.theirGrid[c.getX()][c.getY()] = true;
    }

    @Override
    public Coordinate makeAttack() {
    	//System.out.println(this.hitShips);
        if (this.availableShots.size() > 0) {
            //System.out.println("there were available shots!");
            //System.out.println("the shot: "+this.availableShots.get(0));
            this.lastShot = this.availableShots.get(0);
            this.availableShots.remove(0);
            shotHere(this.lastShot);
            return this.lastShot;
        } 
       
        else {
            if (this.matchNumber > (50)) {
                Coordinate shot = makeEducatedShot();
                this.lastShot = shot;
                shotHere(shot);
                return shot;
            } else {
                Coordinate shot = makeGuessShot();
                this.lastShot = shot;
                shotHere(shot);
                return shot;
            }
        }

    }

    /**
     * TODO Put here a description of what this method does.
     *
     * @return
     */
    private Coordinate makeGuessShot() {
        // TODO Auto-generated method stub.
        if (this.lastShot == null) {
        	
        	int best = 7;
        	int best_x = 0;
        	int best_y = 0;
        	int x = 0;
        	int y = 0;
        	for( int i=0; i<5; i++){
	            int guess = rGen.nextInt(100);
	            x = guess / 10;
	            y = guess % 10;
	            
	            if( this.matchNumber % 2 == 0){
	            	if( ( (x % 2) == 0  && (y % 2) == 0) || ( (x % 2) == 1  && (y % 2) == 1) ){
	            		x = (x+1) % 10;            		
	            	}	  
	            	
	            	while(!checkCoord(x,y)){
	            		guess = rGen.nextInt(100);
	    	            x = guess / 10;
	    	            y = guess % 10;	            		
	            		if( ( (x % 2) == 0  && (y % 2) == 0) || ( (x % 2) == 1  && (y % 2) == 1) ){
		            		x = (x+1) % 10;            		
		            	}
	            	}
	            	
	            	if( surroundCoord(new Coordinate(x,y)) < best){
	            		best = surroundCoord(new Coordinate(x,y));
	            		best_x = x;
	            		best_y = y;
	            	}
	            }
	            else{
	            	if( ( (x % 2) == 1  && (y % 2) == 0) || ( (x % 2) == 0  && (y % 2) == 1) ){
	            		y = (y+1) % 10;            		
	            	}	  
	            	
	            	while(!checkCoord(x,y)){
	            		guess = rGen.nextInt(100);
	    	            x = guess / 10;
	    	            y = guess % 10;	            		
	            		if( ( (x % 2) == 1  && (y % 2) == 0) || ( (x % 2) == 0  && (y % 2) == 1) ){
		            		y = (y+1) % 10;            		
		            	}
	            	}
	            	
	            	if( surroundCoord(new Coordinate(x,y)) < best){
	            		best = surroundCoord(new Coordinate(x,y));
	            		best_x = x;
	            		best_y = y;
	            	}
	            }            
        	}
            return new Coordinate(best_x, best_y);
            
        } else {
        	int best = 7;
        	int best_x = -1;
        	int best_y = 0;
        	int x = lastShot.getX();
        	int y = lastShot.getY();
        	
        	if( (this.matchNumber % 2 == 0) && ((x % 2) != (y % 2) ) ){
        		
        		for( int i = 0; i<10; i++){
        			if( checkCoord(  ( x+i ) % 10 , (y+i) % 10 )  ){
    	            	if( surroundCoord(new Coordinate(( x+i) % 10,(y+i) % 10)) < best){
    	            		best = surroundCoord(new Coordinate(( x+i) % 10,(y+i) % 10));
    	            		best_x = ( x+i ) % 10;
    	            		best_y = ( y+i ) % 10;
    	            	}
        			}
        		}
        		
        	}
        	else if((this.matchNumber % 2 == 0)){
        		x = (x - 1) % 10;
        		
        		for( int i = 0; i<10; i++){
        			if( checkCoord(  ( x+i) % 10 , (y+i) % 10 )  ){
    	            	if( surroundCoord(new Coordinate(( x+i) % 10,(y+i) % 10)) < best){
    	            		best = surroundCoord(new Coordinate(( x+i) % 10,(y+i) % 10));
    	            		best_x = ( x+i ) % 10;
    	            		best_y = ( y+i ) % 10;
    	            	}
        			}
        		}
        		
        	}
        	else if( (this.lastShot.getX() % 2) == (this.lastShot.getY() % 2) ){
        		for( int i = 0; i<10; i++){
        			if( checkCoord(  ( x+i) % 10 , (y+i) % 10 )  ){
    	            	if( surroundCoord(new Coordinate(( x+i) % 10,(y+i) % 10)) < best){
    	            		best = surroundCoord(new Coordinate(( x+i) % 10,(y+i) % 10));
    	            		best_x = ( x+i ) % 10;
    	            		best_y = ( y+i ) % 10;
    	            	}
        			}
        		}
        	}
        	else{
        		x = (x + 1) % 10;
        		
        		for( int i = 0; i<10; i++){
        			if( checkCoord(  ( x+i) % 10 , (y+i) % 10 )  ){
    	            	if( surroundCoord(new Coordinate(( x+i) % 10,(y+i) % 10)) < best){
    	            		best = surroundCoord(new Coordinate(( x+i) % 10,(y+i) % 10));
    	            		best_x = ( x+i ) % 10;
    	            		best_y = ( y+i ) % 10;
    	            	}
        			}
        		}
        		
        	}
        	
        	if( best_x == -1){
        		this.lastShot = null;
        		return makeGuessShot();
        	}
        	return new Coordinate(best_x, best_y);            
        }
    }

    /**
     * TODO Put here a description of what this method does.
     *
     * @param lastShot2
     * @return
     */
    private int surroundCoord(Coordinate last) {
        int retVal = 0;
        if (!checkCoord(last.getX() + 1, last.getY() + 1)) {
            retVal++;
        }
        if (!checkCoord(last.getX(), last.getY() + 1)) {
            retVal++;
        }
        if (!checkCoord(last.getX() - 1, last.getY() + 1)) {
            retVal++;
        }
        if (!checkCoord(last.getX() + 1, last.getY() - 1)) {
            retVal++;
        }
        if (!checkCoord(last.getX(), last.getY() - 1)) {
            retVal++;
        }
        if (!checkCoord(last.getX() - 1, last.getY() - 1)) {
            retVal++;
        }
        if (!checkCoord(last.getX() + 1, last.getY())) {
            retVal++;
        }
        if (!checkCoord(last.getX() - 1, last.getY())) {
            retVal++;
        }
        return retVal;
    }
    //make a shot based on enemy ship placements (covert intel)

    private Coordinate makeEducatedShot() {
        

        double[][] heat = new double[10][10];

        for (int s = 0; s < 5; s++) {
            if (this.enemyShips[s]) {
                int shipLen = this.shipLength[s];

                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j <= (10 - shipLen); j++) {
                        boolean testOk = true;
                        for (int k = shipLen - 1; k >= 0; k--) {
                            if (this.theirGrid[i][j + k]) {
                                testOk = false;
                                continue;
                            }
                        }
                        if (testOk) {
                            for (int k = shipLen - 1; k >= 0; k--) {
                                heat[i][j + k]++;
                            }
                        }
                    }
                }
                //try best horizontal rectangle
                for (int i = 0; i <= (10 - shipLen); i++) {
                    for (int j = 0; j < 10; j++) {
                        boolean testOk = true;
                        for (int k = shipLen - 1; k >= 0; k--) {
                            if (this.theirGrid[i + k][j]) {
                                testOk = false;
                                continue;
                            }
                        }
                        if (testOk) {
                            for (int k = shipLen - 1; k >= 0; k--) {
                                heat[i + k][j]++;
                            }
                        }
                    }
                }

            }
        }
        
        //now shift to a diagonal aligned with guess shot.
        if( this.matchNumber % 2 == 1 ){
        	for (int q = 0; q < 100; q++) {
        		int x_temp = q % 10;
        		int y_temp = q / 10;
        		if ((x_temp % 2) != (y_temp %2)){ 
        			heat[q % 10][q / 10] = 0;
        		}
            }
        } else {
        	for (int q = 0; q < 100; q++) {
        		int x_temp = q % 10;
        		int y_temp = q / 10;
        		if ((x_temp % 2) == (y_temp %2)){ 
        			heat[q % 10][q / 10] = 0;
        		}
            }
        	
        }

        //apply factors with surrounding coordinates
        
        for (int q = 0; q < 100; q++) {
        	int sur = surroundCoord(new Coordinate(q % 10, q / 10));
        	if( sur == 0 ){
        		heat[q % 10][q / 10] *= this.avgHeat[q % 10][q / 10] * this.heatFactor;
        		
        	}
        	else if( sur < 3 ){
        		heat[q % 10][q / 10] *= this.avgHeat[q % 10][q / 10] * this.heatFactor * .95;
        	}
        	else if( sur < 5 ){
        		heat[q % 10][q / 10] *= this.avgHeat[q % 10][q / 10] * this.heatFactor * .9;
        	}
        	else{
        		heat[q % 10][q / 10] *= this.avgHeat[q % 10][q / 10] * this.heatFactor * .85;
        	}
        		
        }
             
        
        double best = 0;
        int bestX = 0;
        int bestY = 0;
        for (int q = 0; q < 100; q++) {
            if (heat[q % 10][q / 10] > best) {
                best = heat[q % 10][q / 10];
                bestX = q % 10;
                bestY = q / 10;
            }
        }
        
        //for method error possibility
        if (!checkCoord(bestX, bestY)) {
            return makeGuessShot();
        }
        return new Coordinate(bestX, bestY);

    }

    /**
     * TODO Put here a description of what this method does.
     *
     * @param biggestSquare
     * @return
     */
    @Override
    public void resultOfAttack(int result) {
    	this.turn_number++;
        if( result != MISS ){
            this.hitsHeat[this.lastShot.getX()][this.lastShot.getY()]++;
            if (result >= 20) {
                this.hitShips.set(result % 20, null);
                this.availableShots.clear();

                //System.out.println("I sunk a ship!");
            } 
            else {
            	//ship hit before
                if (this.hitShips.get(result % 10) != null) {
                	//ship is verticle
                	this.hitShips.get(result % 10)[4] = String.valueOf( Integer.parseInt(this.hitShips.get(result % 10)[4]) + 1);
                	this.hitShips.get(result % 10)[5] = "" + lastShot.getX();
                	this.hitShips.get(result % 10)[6] = "" + lastShot.getY();
                    if (this.hitShips.get(result % 10)[0].equals("1")) {
                    	if( this.lastShot.getX() != Integer.parseInt(this.hitShips.get(result % 10)[1])){
                    		//if ship hit horizontal, but was supposed to be verticle, set to horizontal.
                    		this.hitShips.get(result % 10)[0] = "0";            		
                            
                    	}
                    	
                    } 
                    //horizontal
                    else {
                    	if( this.lastShot.getY() != Integer.parseInt(this.hitShips.get(result % 10)[2])){
                    		this.hitShips.get(result % 10)[0] = "1";
                    	}                    	
                    }
                }
                //ship never hit before
                else {
                    int shipMod = result % 10;
                    if (shipMod == 0) {
                        this.hitShips.set(shipMod, new String[]{"0", "" + lastShot.getX(), "" + lastShot.getY(), "2", "1", "" + lastShot.getX(), "" + lastShot.getY()});
                    } else if (shipMod == 1 || shipMod == 2) {
                        this.hitShips.set(shipMod, new String[]{"0", "" + lastShot.getX(), "" + lastShot.getY(), "3", "1", "" + lastShot.getX(), "" + lastShot.getY()});
                    } else if (shipMod == 3) {
                        this.hitShips.set(shipMod, new String[]{"0", "" + lastShot.getX(), "" + lastShot.getY(), "4", "1",  "" + lastShot.getX(), "" + lastShot.getY()});
                    } else {
                        this.hitShips.set(shipMod, new String[]{"0", "" + lastShot.getX(), "" + lastShot.getY(), "5", "1",  "" + lastShot.getX(), "" + lastShot.getY()});
                    }

                }
            }
            
            
        }

       buildShots();
        
    }

    /**
     * TODO Put here a description of what this method does.
     *
     */
    /**
     * TODO Put here a description of what this method does.
     *
     * @return
     *
     */
    private void buildShots() {
        this.availableShots.clear();
        //System.out.println("building some shots!");
        //indicies of available shots that are 1 away form a hit
        for (int i = 0; i < 5; i++) {
            if (this.hitShips.get(i) != null) {
            	//System.out.println("hitting a ship");
                String[] curShip = this.hitShips.get(i);
                //System.out.println("ship left: "+i);
                //check to make sure ship can fit in that direction if there is only one hit
                int ship_left = Integer.parseInt(curShip[3]) - Integer.parseInt(curShip[4]);
                //System.out.println("Ship Left: "+ship_left);

                //if the ship has only been hit once, no additional numbers.
                if (curShip[4].equals("1")) {
                    //System.out.println("ship was only hit once");
                    //right-left
                    
                    int y = Integer.parseInt(curShip[2]);
                    int x = Integer.parseInt(curShip[1]);
                    int rspaces = 0;
                    int lspaces = 0;
                    int tspaces = 0;
                    int bspaces = 0;
                    //System.out.println(curShip[0]+" "+curShip[1]+" "+curShip[2]+" "+curShip[3]+" "+curShip[4]+" "+curShip[5]+" "+curShip[6]);
                    
                    for( int j = -1; j<2; j += 2){
	                    if( checkCoord(x+(1*j),y)){

	                    	//System.out.println("x-1 was ok");
	                    	if( j == -1)
	                    		lspaces++;
	                    	else
	                    		rspaces++;
	                    	if( (ship_left > 1) && checkCoord(x+(2*j),y)){
		                    	if( j == -1)
		                    		lspaces++;
		                    	else
		                    		rspaces++;
	                    		if( (ship_left > 2) && checkCoord(x+(3*j),y)){
	    	                    	if( j == -1)
	    	                    		lspaces++;
	    	                    	else
	    	                    		rspaces++;
	                    			if( (ship_left > 3) && checkCoord(x+(4*j),y)){
	        	                    	if( j == -1)
	        	                    		lspaces++;
	        	                    	else
	        	                    		rspaces++;	                    				
	                    			}
	                    		}
	                    	}
	                    }
                    }
                    for( int j = -1; j<2; j += 2){
	                    if( checkCoord(x,y+(1*j))){
	                    	if( j == -1)
	                    		tspaces++;
	                    	else
	                    		bspaces++;
	                    	if( (ship_left > 1) && checkCoord(x,y+(2*j))){
		                    	if( j == -1)
		                    		tspaces++;
		                    	else
		                    		bspaces++;
	                    		if( (ship_left > 2) && checkCoord(x,y+(3*j))){
	    	                    	if( j == -1)
	    	                    		tspaces++;
	    	                    	else
	    	                    		bspaces++;
	                    			if( (ship_left > 3) && checkCoord(x,y+(4*j))){
	        	                    	if( j == -1)
	        	                    		tspaces++;
	        	                    	else
	        	                    		bspaces++;	                    				
	                    			}
	                    		}
	                    	}
	                    }
                    }
                    
                    Coordinate hor = null;
                    Coordinate vert = null;
                    this.cur_hor = 0;
                    this.cur_ver = 0;
                    if( (rspaces + lspaces) >= ship_left ){
                    	
                    	hor = find_best_fit(x,y,lspaces, rspaces, ship_left, 0);
                    	//System.out.println(hor);
                    }

                    if( (tspaces + bspaces) >= ship_left){
                    	
                    	vert = find_best_fit(x,y,tspaces, bspaces, ship_left, 1);
                    }
    
                    //System.out.println(rspaces + " "+lspaces + " horu");
                    //System.out.println(tspaces + " " + bspaces + " vert");
                    if( this.cur_ver > this.cur_hor){
                    	this.availableShots.add(0, new Coordinate(vert.getX(), vert.getY()));
                    }
                    else if (this.cur_hor > this.cur_ver ){
                    	this.availableShots.add(0, new Coordinate(hor.getX(), hor.getY()));
                    }
                    else{
                    	if (vert != null){
                    		this.availableShots.add(0, new Coordinate(vert.getX(), vert.getY()));
                    	}
                    	else{
                    		this.availableShots.add(0, new Coordinate(hor.getX(), hor.getY()));
                    	}
                    }

                }
                

                 //already hit more than once, add which shots you can still make
                else {

            		int ship_length = Integer.parseInt(curShip[3]);
                    //System.out.println("ship's been hit more than once");
                	if( curShip[0] == "0"){
                		//shot was in +x direc
                		int last_x = Integer.parseInt(curShip[5]);
                		int last_y = Integer.parseInt(curShip[6]);
                		int first_x = Integer.parseInt(curShip[1]);
                		if( last_x > first_x ){
                			if( (checkCoord(last_x+1, last_y)) && (last_x < (first_x + ship_length)) ){
                				this.availableShots.add(0, new Coordinate(last_x+1, last_y));
                			}
                			//ship hit at both ends 
                			else {
                				for( int k = 0; k < Integer.parseInt(curShip[3]); k++ ){
                					if(checkCoord( last_x-k,last_y )){
                						this.availableShots.add(0, new Coordinate(last_x-k, last_y));
                						break;
                					}
                				}
                			}
                		}
                		//ship was hit in -x direction
                		else{
                			if( (checkCoord(last_x-1, last_y)) && (last_x > (first_x - ship_length)) ){
                				this.availableShots.add(0, new Coordinate(last_x-1, last_y));
                			}
                			//ship hit at both ends 
                			else {
                				for( int k = 0; k < Integer.parseInt(curShip[3]); k++ ){
                					if(checkCoord( last_x+k,last_y )){
                						this.availableShots.add(0, new Coordinate(last_x+k, last_y));
                						break;
                					}
                				}
                			}
                		}
                	}
                	else{
                		//shot was in +y direc
                		int last_x = Integer.parseInt(curShip[5]);
                		int last_y = Integer.parseInt(curShip[6]);
                		int first_y = Integer.parseInt(curShip[2]);
                		if( last_y > first_y ){
                			if( (checkCoord(last_x, last_y+1)) && (last_y < (first_y + ship_length)) ){
                				this.availableShots.add(0, new Coordinate(last_x, last_y+1));
                			}
                			//ship hit at both ends 
                			else {
                				for( int k = 0; k < Integer.parseInt(curShip[3]); k++ ){
                					if(checkCoord( last_x,last_y-k)){
                						this.availableShots.add(0, new Coordinate(last_x, last_y-k));
                						break;
                					}
                				}
                			}
                		}
                		//ship was hit in -y direction
                		else{
                			if( (checkCoord(last_x, last_y-1)) && (last_y > (first_y - ship_length)) ){
                				this.availableShots.add(0, new Coordinate(last_x, last_y-1));
                			}
                			//ship hit at both ends 
                			else {
                				for( int k = 0; k < Integer.parseInt(curShip[3]); k++ ){
                					if(checkCoord( last_x,last_y+k )){
                						this.availableShots.add(0, new Coordinate(last_x, last_y+k));
                						break;
                					}
                				}
                			}
                		}
                	}
                }
            }            
        }
    }

    private Coordinate find_best_fit(int x, int y, int neg_spaces, int pos_spaces, int needed_spaces, int i) {

    	//horizontal
    	if( i == 0){
    		double best_rect_val = 0;
    		Coordinate best_rect_start = null;
    		if( neg_spaces >0 ){
    			best_rect_start = new Coordinate(x-1,y);
    		}
    		else{
    			best_rect_start = new Coordinate(x+1,y);
    		}
    		
    		for( int j = (x - neg_spaces); j< ( x + pos_spaces - needed_spaces); j++){
    			double rect_value = 0;
    			for( int k = 0; k<needed_spaces; k++){
    				rect_value += ( this.avgHeat[k+j][y] * this.heatFactor);
    			}
    			
    			if( rect_value > best_rect_val ){
    				best_rect_val = rect_value;
    				best_rect_start = new Coordinate(j, y); //rect contnues in +x direction
    			}
    		}
    		this.cur_hor = best_rect_val;
    		if( best_rect_start.getX() < (x-1)){
    			return new Coordinate(x-1, y);
    		}
    		else if(pos_spaces >0){
    			return new Coordinate( x+1, y);
    		}
    		else{
    			return new Coordinate(x-1, y);
    		}    		
    	}
    	//verticle
    	else{
    		double best_rect_val = 0;
    		Coordinate best_rect_start = null;
    		if( neg_spaces > 0 ){
    			best_rect_start = new Coordinate(x,y-1);
    		}
    		else{
    			best_rect_start = new Coordinate(x,y+1);
    		}
    		for( int j = (y - neg_spaces); j< ( y + pos_spaces - needed_spaces); j++){
    			double rect_value = 0;
    			for( int k = 0; k<needed_spaces; k++){
    				rect_value += ( this.avgHeat[x][k+j] * this.heatFactor);
    			}
    			
    			if( rect_value > best_rect_val ){
    				best_rect_val = rect_value;
    				best_rect_start = new Coordinate(x, j); //rect contnues in +x direction
    			}
    		}
    		this.cur_ver = best_rect_val;
    		
    		if( best_rect_start.getY() < (y-1)){
    			return new Coordinate(x, y-1);
    		}
    		else if(pos_spaces >0){
    			return new Coordinate( x, y+1);
    		}
    		else{
    			return new Coordinate(x, y-1);
    		}  
    	}
	}

	/**
     * TODO Put here a description of what this method does.
     *
     * @param i
     * @param y
     * @return
     */
    private boolean checkCoord(int x, int y) {
        if (x < 10 && y < 10 && x >= 0 && y >= 0) {
            if (!this.theirGrid[x][y]) {
                return true;
            }
        }

        return false;
    }
    private boolean checkCoord_test(int x, int y) {
    	System.out.println("Checking Coordinate: "+x+","+y);
        if (x < 10 && y < 10 && x >= 0 && y >= 0) {
        	System.out.println("passed param1");
            if (!this.theirGrid[x][y]) {
            	System.out.println("passed param2 ret true");
                return true;
            }
        }

        return false;
    }
    @Override
    public void opponentAttack(Coordinate coord) {
        // TODO Auto-generated method stub.
        this.theirShots[coord.getX()][coord.getY()]++;

    }

    @Override
    //recalculates the heat of each cell
    public void resultOfGame(int result) {
        for (int i = 0; i < 100; i++) {
            if (this.shotsHeat[i / 10][i % 10] != 0) {
                this.avgHeat[i / 10][i % 10] = (double) (this.hitsHeat[i / 10][i % 10]) / (double) (this.shotsHeat[i / 10][i % 10]);
            } else {
                this.avgHeat[i / 10][i % 10] = 0;
            }
        }

    }
}
