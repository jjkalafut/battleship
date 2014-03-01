
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * This is the captain america battleship A.I. Be Afraid.
 *
 * @author John. Created Mar 18, 2013.
 */
public class CaptainMurica_Improvement implements Captain, Constants {

	//where the opponent has shot
    private int[][] theirShots = new int[10][10];
    //a map of where i have hit ships
    private int[][][] hitsHeat = new int[10][10][5];
    //a map of where i have shot
    private int[][] shotsHeat = new int[10][10];
    // a map of where I have put my ships
    private int[][] placeHeat = new int[10][10];
    //an array of battleship lengths
    private int[] shipLength = {2, 3, 3, 4, 5};
    //the current match number
    private int matchNumber = 0;
    //the total number of matches being played
    private int matchTotal;
    //a seed for ship placing
    private int seed;
    //the turn number it is 
    private int turn_num = 0;
    //my heatmap
    private double[][][] avgHeat = new double[10][10][5];
    //my heat factor
    private double heatFactor = 0;    
    //the best current horizontal enemy ship position
    private double cur_ver = 0;
    //the best verticle enemy ship position
    private double cur_hor = 0;
    //the grid that I am sooting on, to be sure i dont shoot the same spot twice
    private boolean[][] theirGrid = new boolean[10][10];
    //a map of where my ships are, used for placement
    private boolean[][] myShips = new boolean[10][10];
    //the status of the enemy ships 
    private boolean[] enemyShips = new boolean[5];
    //tell if i have detected reliable pattern
    private boolean pattern_detect;
    //more detailed stats of the enemy ship
    private ArrayList<ArrayList<Integer>> hitShips;
    //the shots that I can make (based on hitting ships)
    private ArrayList<Coordinate> availableShots = new ArrayList<Coordinate>();
    //the spot where i last fired
    private Coordinate lastShot;   
    //the name of my opponent
    private String lastOpp = "";
    // a random number generator
    private Random rGen;
    //my fleet object
    private Fleet myFleet;

    //test privates area
    private int ships_left;
    private int ship_hits_left;
    private int losses;
    private int last_ship_losses;
    private int placeMethods = 4;
    private int[][] placementRate;
    private int total_turns = 0;
    private int longest_game = 0;
    private int shortest_game = 100;
    private int games_over_69 = 0;

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
    	
        this.lastShot = null;
        this.turn_num = 0;
        //reset enemy grid
        for (boolean[] b : this.theirGrid) {
            Arrays.fill(b, false);
        }
        //true mens enemy ship is still alive
        Arrays.fill(this.enemyShips, true);
        //reset hittign shits
        this.hitShips = new ArrayList<ArrayList<Integer>>();
        
       
        //Create a new generator
        this.rGen = new Random();
        //match count increment
        if (opponent.equals(this.lastOpp)) {
            this.matchNumber++;            
        } //else reset all the opponent data
        else {
        	//
        	this.ship_hits_left = 0;
        	this.ships_left = 0;
        	this.losses = 0;
        	this.last_ship_losses = 0;
        	this.placementRate = new int[this.placeMethods][2];
        	//
        	this.pattern_detect = false;
            this.matchTotal = numMatches;
            this.theirShots = new int[10][10];
            this.matchNumber = 1;
            this.shotsHeat = new int[10][10];
            this.hitsHeat = new int[10][10][5];
            this.avgHeat = new double[10][10][6];
            this.lastOpp = opponent;
            this.seed = 2;
            //reset where i've been placing ships
            for (int[] b : this.placeHeat){
            	Arrays.fill(b, 0);
            }
            
        }
        //print out important stats at the end of each match
        if( this.matchNumber == this.matchTotal){
        	System.out.println("Average ships left for a loss: "+ ((double) this.ships_left / (double) (this.losses)));
        	System.out.println("% games lost on last ship: "+ ((double) this.last_ship_losses / (double) this.matchTotal));
        	System.out.println("Average hits left on last ship loss: "+ ((double) this.ship_hits_left / (double) (this.last_ship_losses)));
        }
        //reset a map of where my ships are
        for (boolean[] b : this.myShips) {
            Arrays.fill(b, false);
        }
        

        //make a new fleet
        this.myFleet = new Fleet();
        
        if( this.matchNumber < .1 * this.matchTotal ){
        	this.seed = (this.seed + 1) % this.placeMethods;
        }

    	placeShips();

        this.heatFactor = 100.0 * (double) (this.matchNumber) / (double) (this.matchTotal);
    }

    /**
     * this method determines which placement strategy to use.
     */
    private void determineSeed() {

    	double bestPlace = 0;
    	for ( int i = 0; i < this.placeMethods; i++ ){
    		double placeRate = 0;
    		if ( this.placementRate[i][1] == 0 ){
    			placeRate = this.placementRate[i][0];
    		}
    		else{
    			placeRate = (double) this.placementRate[i][0] / (double) this.placementRate[i][1];
    		}
    		if( placeRate > bestPlace ){
    			bestPlace = placeRate;
    			this.seed = i;
    		}    		
    	}
		
	}
    
    /**
     * this method uses the seed to call the current ship placement strat
     */
    
	private void placeShips() {
    	
    	switch( this.seed ){
    	case 0: evenDistributeTouchingPlace(); break;
    	case 1: evenDistributePlace(); break;
    	case 2: learningPlace(); break;
    	case 3: randomPlace(); break;
    	//default: evenDistributeTouchingPlace(); break;      	
    	}	

	}

	/**
	 * This method places each ship at a random location.
	 */
    private void randomPlace() {
		while (!placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), PATROL_BOAT)) {
        }
        while (!placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), DESTROYER)) {
        }
        while (!placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), SUBMARINE)) {
        }
        while (!placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), BATTLESHIP)) {
        }
        while (!placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), AIRCRAFT_CARRIER)) {
        }
		
	}

    /**
     * this method places a ship and keeps track of where it is put.
     * @param x the x coordinate to place the left of the ship
     * @param y the y coordinate to place the top of the ship
     * @param direc the direction to place the ship, 0 = horizontal, 1 = verticle
     * @param shipType the type of ship (to determine the length)
     * @return a boolean, true if ship can be/has been placed
     */
	private boolean placeShip(int x, int y, int direc, int shipType){
    	
    	if( this.myFleet.placeShip(x, y, direc, shipType)){
    		int shipLen = this.shipLength[shipType];
            if (direc == 0) {
                for (int k = 0; k < shipLen; k++) {
                    this.myShips[x + k][y] = true;
                    this.placeHeat[x + k][y]++;
                }
            } else {
                for (int k = 0; k < shipLen; k++) {
                    this.myShips[x][y + k] = true;
                    this.placeHeat[x][y+k]++;
                }

            }
    		return true;
    	}
    	return false;
    	
    	
    	
    }

	/**
	 * This method returns whether or not i can place a hip somewhere.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return true if i can place a ship here
	 */
    private boolean isShip(int x, int y){
    	if( x < 10 && x>= 0 && y<10 && y >= 0){
    		return this.myShips[x][y];
    	}
    	return false;
    	
    }

	private void learningPlace() {
		for (int shipType = 0; shipType < 5; shipType++) {
			int[] placement = leastShotPlace(this.shipLength[shipType]);
            placeShip(placement[0], placement[1], placement[2], shipType);
		}		
	}

	private void evenDistributePlace() {
		
		if( this.matchNumber > 100){
			//a little random to avoid repeating same pattern
			int r = this.rGen.nextInt(2);
			
			for( int shipType = 0; shipType<5; shipType++){
				
				int shipLen = this.shipLength[shipType];
				int bestRect = -1;
		        Coordinate rectCoord = null;
		        
				if( r == 1 ){					
			        //try best verticle rectangle
			        for (int i = 0; i < 10; i++) {
			            for (int j = 0; j <= (10 - shipLen); j++) {
			                int testRect = 0;
			                boolean testOk = true;
			                for (int k = 0; k < shipLen; k++) {
	
			                    if (this.myShips[i][j + k]) {
			                        testOk = false;
			                        break;
			                    }
			                    testRect = +this.placeHeat[i][j + k];
			                }
			                if (testOk && (bestRect == -1 || testRect < bestRect) ) {
			                    bestRect = testRect;
			                    rectCoord = new Coordinate(i, j);
			                }
			            }
			        }
			        
				}
		        else{
					//try best horizontal rectangle
			        for (int i = 0; i <= (10 - shipLen); i++) {
			            for (int j = 0; j < (10); j++) {
			                int testRect = 0;
			                boolean testOk = true;
			                for (int k = 0; k <shipLen; k++) {
			                    if (this.myShips[i + k][j]) {
			                        testOk = false;
			                        break;
			                    }
			                    testRect = +this.placeHeat[i + k][j];
			                }
			                if ( testOk && (bestRect == -1 || testRect < bestRect) ) {
			                    bestRect = testRect;
			                    rectCoord = new Coordinate(i, j);
			                }
			            }
			        }
					
				}
				
				//boolean return ignored. Should always be ok, since methods above check
				placeShip(rectCoord.getX(), rectCoord.getY(), r, shipType);
				
			}
		}else{
			notTouchingPlace();
		}
		
	}
	private void evenDistributeTouchingPlace() {
		
		if( this.matchNumber > 100){
			//a little random to avoid repeating same pattern
			int r = this.rGen.nextInt(2);
			
			for( int shipType = 0; shipType<5; shipType++){
				
				int shipLen = this.shipLength[shipType];
				int bestRect = -1;
		        Coordinate rectCoord = null;
		        
				if( r == 1 ){					
			        //try best verticle rectangle
			        for (int i = 0; i < 10; i++) {
			            for (int j = 0; j <= (10 - shipLen); j++) {
			                int testRect = 0;
			                boolean testOk = true;
			                for (int k = 0; k < shipLen; k++) {
	
			                    if (this.myShips[i][j + k]) {
			                        testOk = false;
			                        break;
			                    }
			                    if( i == 0){
			                    	if( this.myShips[i+1][j + k] ){
			                    		testOk = false;
				                        break;
			                    	}
			                    }
			                    else if( i == 9){
			                    	if( this.myShips[i-1][j + k]){
			                    		testOk = false;
				                        break;
			                    	}
			                    }
			                    else{
			                    	if( this.myShips[i+1][j + k] || this.myShips[i-1][j + k]){
			                    		testOk = false;
				                        break;
			                    	}
			                    }
			                    testRect = +this.placeHeat[i][j + k];
			                }
			                if (testOk && ( bestRect == -1 || testRect < bestRect) ) {
			                    bestRect = testRect;
			                    rectCoord = new Coordinate(i, j);
			                }
			            }
			        }
			        
				}
				if(bestRect == -1){
					r = 0;
				}
		        if( r == 0 ){
					//try best horizontal rectangle
			        for (int i = 0; i <= (10 - shipLen); i++) {
			            for (int j = 0; j < (10); j++) {
			                int testRect = 0;
			                boolean testOk = true;
			                for (int k = 0; k <shipLen; k++) {
			                    if (this.myShips[i + k][j]) {
			                        testOk = false;
			                        break;
			                    }
			                    if( j == 0 ){
			                    	if( this.myShips[i + k][j+1] ){
			                    		testOk = false;
				                        break;
			                    	}
			                    }
			                    else if(j == 9){
			                    	if(this.myShips[i + k][j-1] ){
			                    		testOk = false;
				                        break;
			                    	}
			                    }
			                    else{
			                    	if( this.myShips[i + k][j+1] || this.myShips[i + k][j-1] ){
			                    		testOk = false;
				                        break;
			                    	}
			                    }
			                    testRect = +this.placeHeat[i + k][j];
			                }
			                if ( testOk && (bestRect == -1 || testRect < bestRect) ) {
			                    bestRect = testRect;
			                    rectCoord = new Coordinate(i, j);
			                }
			            }
			        }
					
				}
				if(bestRect == -1){
					r = 1;
					shipType--;
				}
				else{
					//boolean return ignored. Should always be ok, since methods above check
					placeShip(rectCoord.getX(), rectCoord.getY(), r, shipType);
				}
				
			}
		}else{
			notTouchingPlace();
		}
		
	}
	private void notTouchingPlace() {
		for (int shipType = 0; shipType < 5; shipType++) {
			boolean placed = false;
			int t_counter = 0;
			while(!placed){
				
				if( t_counter > 300 ){
					int x = this.rGen.nextInt(10);
					int y = this.rGen.nextInt(10);
					int z = this.rGen.nextInt(2);
					while( !this.myFleet.placeShip(x,  y,  z, shipType)){
						x = this.rGen.nextInt(10);
						y = this.rGen.nextInt(10);
						z = this.rGen.nextInt(2);
					}
					placed = true;
				}
				else{
					//true means that there are no touching ships
					boolean surround = true;					
					int x = this.rGen.nextInt(10);
					int y = this.rGen.nextInt(10);
					
					if( x == 0 || x == 9 || y ==0 || y== 9){
						if( x == 0 ){
							//try y coords (up/down)
							for( int i = 0; i < this.shipLength[shipType]; i++){
								
								if( isShip( x, y+i) || isShip( x+1, y+i)){
									surround = false;
									break;
								}
							}
							if( isShip( x, y-1) || isShip(x , y + this.shipLength[shipType]) ){
								surround = false;
							}
							if( surround && checkCoord(x, y) && checkCoord( x , y + this.shipLength[shipType] - 1 ) ){
								if(placeShip(x, y, VERTICAL, shipType)){
									placed = true;
								}
							}
						}
						else if(x==9){
							//try y coords (up/down)
							for( int i = 0; i < this.shipLength[shipType]; i++){
								
								if( isShip( x, y+i) || isShip(  x-1, y+i) ){
									surround = false;
									break;
								}
							}
							if( isShip( x, y-1) || isShip(x , y + this.shipLength[shipType]) ){
								surround = false;
							}
							if( surround && checkCoord(x, y) && checkCoord( x , y + this.shipLength[shipType] + 1 ) ){
								if(placeShip(x, y, VERTICAL, shipType)){
									placed = true;
								}
							}
						}
						else if(y == 0){
							for( int i = 0; i < this.shipLength[shipType]; i++){
								
								if( isShip( x+i, y) || isShip(  x+i, y+1)){
									surround = false;
									break;
								}
							}
							if( isShip( x-1, y) || isShip(x + this.shipLength[shipType], y) ){
								surround = false;
							}
							if( surround && checkCoord(x, y) && checkCoord( x + this.shipLength[shipType] - 1 , y) ){
								if(placeShip(x, y, HORIZONTAL, shipType)){
									placed = true;
								}
							}
						}
						else{
							for( int i = 0; i < this.shipLength[shipType]; i++){
								
								if( isShip( x + i, y) || isShip( x+i, y-1) ){
									surround = false;
									break;
								}
							}
							if( isShip( x-1, y) || isShip(x + this.shipLength[shipType], y) ){
								surround = false;
							}
							if( surround && checkCoord(x, y) && checkCoord( x + this.shipLength[shipType] - 1, y) ){
								if(placeShip(x, y, HORIZONTAL, shipType)){
									placed = true;
								}
							}
						}
					}	
					else{
						
						//try x coords (right/left)
						for( int i = 0; i < this.shipLength[shipType]; i++){
							
							if( isShip( x+i, y) || isShip(x+i, y-1) || isShip(x+i, y+1)){
								surround = false;
								break;
							}
						}
						if( isShip(x-1, y) || isShip(x + this.shipLength[shipType], y) ){
							surround = false;
						}
						if( surround && checkCoord(x, y) && checkCoord( x + this.shipLength[shipType] - 1, y) ){
							if(placeShip(x, y, HORIZONTAL, shipType)){
								placed = true;
							}
						}
						//try y coords (up/down)
						for( int i = 0; i < this.shipLength[shipType]; i++){
							
							if( isShip(x, y+i) || isShip(  x-1, y+i) || isShip( x+1, y+i)){
								surround = false;
								break;
							}
						}
						if( isShip( x, y - 1) || isShip(x , y + this.shipLength[shipType]) ){
							surround = false;
						}
						if( surround && checkCoord(x, y) && checkCoord( x , y + this.shipLength[shipType]-1) ){
							if(placeShip(x, y, VERTICAL, shipType)){
								placed = true;
							}
						}
					}
				}
				
				t_counter++;
			}			
		}		
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
                for (int k = 0; k < shipLen; k++) {

                    if (this.myShips[i][j + k]) {
                        testOk = false;
                        break;
                    }
                    testRect = +this.theirShots[i][j + k];
                }
                if (testOk && (bestRect == -1 || testRect < bestRect) ) {
                    bestRect = testRect;
                    rectCoord = new Coordinate(i, j);
                }
            }
        }
        //try best horizontal rectangle
        for (int i = 0; i <= (10 - shipLen); i++) {
            for (int j = 0; j < (10); j++) {
                int testRect = 0;
                boolean testOk = true;
                for (int k = 0; k <shipLen; k++) {
                    if (this.myShips[i + k][j]) {
                        testOk = false;
                        break;
                    }
                    testRect = +this.theirShots[i + k][j];
                }
                if (testOk && testRect < bestRect ) {
                    bestRect = testRect;
                    rectCoord = new Coordinate(i, j);
                    orientation = 0;
                }
            }
        }


        int[] ret = {rectCoord.getX(), rectCoord.getY(), orientation};
        //System.out.println(rectCoord.getX()+" "+rectCoord.getY()+" - "+orientation);
        return ret;
    }

    @Override
    public Fleet getFleet() {
        return this.myFleet;
    }

    private void shotHere(Coordinate c) {
        if( !checkCoord(c.getX(), c.getY()) ){
        	//ystem.out.println("bad shot!");
        }
    	this.turn_num++;
        this.shotsHeat[c.getX()][c.getY()]++;
        this.theirGrid[c.getX()][c.getY()] = true;
    }

    @Override
    public Coordinate makeAttack() {

        if ( this.turn_num < 5 ) {
            Coordinate shot = makeScoutShot();
            this.lastShot = shot;
            shotHere(shot);
            return shot;
        } 
        else if( this.availableShots.size() > 0 ) {
    	 	
            this.lastShot = this.availableShots.get(0);
            this.availableShots.remove(0);
            shotHere(this.lastShot);
            return this.lastShot;
            
        }
        else {
        	Coordinate shot = makeEducatedShot();
            this.lastShot = shot;
            shotHere(shot);
            return shot;
        }

    }

    private Coordinate makeScoutShot(){
    	if( this.pattern_detect ){
    		return makePatternShot();
    	}
    	else if(this.matchNumber < 500){
    		return makeMinimizerShot();
    	}
    	else{
    		return makeProbabilityShot();
    	}
    }
    private Coordinate makeProbabilityShot() {

    	ArrayList<Integer> bad_x = new ArrayList<Integer>();
    	ArrayList<Integer> bad_y = new ArrayList<Integer>();
    	
    	for( int i = 0; i < 100; i++ ){
    		if( this.theirGrid[i/10][i%10] ){
    			bad_x.add(i/10);
    			bad_y.add(i%10);
    		}
    	}
    	
    	double best = 0;
    	int best_x = 0;
    	int best_y = 0;
    	
    	for (int w = 0; w < 10; w++){
    		if( !bad_x.contains(w) ){
	        	for (int z = 0; z < 10; z++ ){
	        		if( !bad_y.contains(z)){
		        		double total = 0;
		        		for (int v = 0; v < 5; v++ ){
		        			if( this.enemyShips[v] ){
		        				total += this.avgHeat[w][z][v];
		        			}
		        		}
		        		if( total > best){
		        			best = total;
		        			best_x = w;
		        			best_y = z;
		        		}
	        		}
	        	}
    		}
        }
    	//should almost never happen
    	if( best == 0 ){
    		return makeMinimizerShot();
    	}
    	else{
    		return new Coordinate( best_x, best_y );
    	}  	
    	
	}

	private Coordinate makePatternShot() {
		// TODO Auto-generated method stub
		return null;
	}

	// shoot in 5 different rows and cols
	private Coordinate makeMinimizerShot() {   
		
		int r = this.rGen.nextInt(100);
		int x = r/10;
		int y = r%10;
		
		boolean rerun = true;
		
		while( rerun ){
			rerun = false;
			for (int i = 0; i < 10; i++){				
				if( this.theirGrid[x][i] ){
					rerun = true;
				}
				if( this.theirGrid[i][y] ){
					rerun = true;
				}			
			}

			if( !rerun ){
				return new Coordinate(x, y);
			}
			
			x = this.rGen.nextInt(10);
			y = this.rGen.nextInt(10);			
		}
		
		System.out.println("Should never be here");
		return new Coordinate(0,0);
    }
    
    /*
    //make a shot based on enemy ship placements (covert intel)
    private Coordinate makeEducatedShot() {
       
        int[][] intersections = new int[10][10];

        //get the longest gap in each row and column
        // make sure its big enough        
        int smallest_ship = 0;
        for ( int i = 0; i < 5; i++ ){
        	if( this.enemyShips[i] ){
        		smallest_ship = this.shipLength[i];
        		break;
        	}
        }
        
        //x will be the row start, y will be length
        Coordinate[] sect_coords_row = new Coordinate[5];
        int[] sect_row_num = new int[5];
        Coordinate[] sect_coords_col = new Coordinate[5];
        int[] sect_col_num = new int[5];
        //keep track of fake marks
        ArrayList<Coordinate> fakes = new ArrayList<Coordinate>();
        // get 5 best intersections
        for ( int sect = 0; sect < 5; sect ++){
	        // row/col number
	        int longest_row = 0;
	        int row_num = 0;
	    	int row_start = -1;
	    	int longest_col = 0;
	    	int col_num = 0;
	    	int col_start = -1;
	    	
	        for( int j = 0; j < 10; j++){        	
	        	
	        	int tracker = 0;
	        	int col_tracker = 0;
	        	for( int k = 0; k < 10; k++){
	        	//rows
		        	if( this.theirGrid[k][j] ){
		        		if ( (k - tracker) >= smallest_ship && (k - tracker) > longest_row ){
		        			longest_row = (k - tracker);
		        			row_start = tracker;
		        			row_num = j;
		        		}
		        		tracker = k + 1;
		        	}
		        	else if( k == 9 ){
		        		if ( (k - tracker + 1) >= smallest_ship && (k - tracker + 1) > longest_row ){
		        			longest_row = (k - tracker + 1);
		        			row_start = tracker;
		        			row_num = j;
		        		}
		        	}      	        	
	        	//columns
		        	if( this.theirGrid[j][k] ){
		        		if ( (k - col_tracker) >= smallest_ship && (k - col_tracker) > longest_col ){
		        			longest_col = (k - col_tracker);
		        			col_start = col_tracker;
		        			col_num = j;
		        		}
		        		col_tracker = k + 1;
		        	}
		        	else if( k == 9 ){
		        		if ( (k - col_tracker + 1) >= smallest_ship && (k - col_tracker + 1) > longest_col ){
		        			longest_col = (k - col_tracker + 1);
		        			col_start = col_tracker;
		        			col_num = j;
		        		}
		        	}	        		
	        	}
	        }
	        
	        //very rare but necessary if statements. 
	        if( row_start != -1){
	        	sect_coords_row[sect] = new Coordinate(row_start, longest_row);
	        	sect_row_num[sect] = row_num;
	        	fakes.add(new Coordinate( (row_start + (longest_row / 2) ), row_num ));
	        	this.theirGrid[ row_start + (longest_row / 2) ][row_num] = true;
	        }
	        if( col_start != -1){
	        	sect_coords_col[sect] = new Coordinate(col_start, longest_col);
	        	sect_col_num[sect] = col_num;
	        	fakes.add(new Coordinate( col_num, (col_start + (longest_col / 2) ) ));
	        	this.theirGrid[ col_num ] [ col_start + (longest_col / 2) ] = true;
	        }
	        
        }
        //rest all the fake placements
        for ( Coordinate c : fakes){
        	this.theirGrid[c.getX()][c.getY()] = false;
        }
        // compare all coordinate intersections
        for( int l = 0; l < 5; l++ ){
        	if( sect_coords_row[l] != null){
        		Coordinate c = sect_coords_row[l];
        		for( int m = 0; m < c.getY(); m++){
        			intersections[c.getX() + m][sect_row_num[l]] += c.getY();
        		}
        	}
        	else{
        		break;
        	}
        }
        //again for columns
        for( int l = 0; l < 5; l++ ){
        	if( sect_coords_col[l] != null){
        		Coordinate c = sect_coords_col[l];
        		for( int m = 0; m < c.getY(); m++){
        			intersections[sect_col_num[l]][c.getX() + m] += c.getY();
        		}
        	}
        	else{
        		break;
        	}
        }
        //overlay all heatmaps
        boolean goodShot = (this.matchNumber < 1000) ? false : true ;
        
        double best = 0;
        int bestX = 0;
        int bestY = 0;
        
        if ( goodShot) {
	        for (int w = 0; w < 10; w++){
	        	for (int z = 0; z < 10; z++ ){
	        		double total = 0;
	        		for (int v = 0; v < 5; v++ ){
	        			if( this.enemyShips[v] ){
	        				total += this.avgHeat[w][z][v];
	        			}
	        		}
	        		total *= this.heatFactor ;
	        		if( total * (double) intersections[w][z] > best){
	        			best = total * (double) intersections[w][z];
	        			bestX = w;
	        			bestY = z;
	        		}
	        	}
	        }
	        //extra saftey catch, in case a ship can only be where i've never hit it before
	        if( best == 0){
		        goodShot = false;
	        }

        }
        if( !goodShot ){
        	for (int w = 0; w < 10; w++){
	        	for (int z = 0; z < 10; z++ ){	        		
	        		if( intersections[w][z] > best){
	        			best = intersections[w][z] ;
	        			bestX = w;
	        			bestY = z;
	        		}
	        	}
	        }
        	//this should never happen
        	if( best == 0){
        		for (Coordinate c : fakes){
        			System.out.println(c);
        		}
        	}
        } 
       if( !checkCoord(bestX, bestY)){
    	   for ( int q = 0; q < 100; q++){
    		   if( q%10 == 0){
    			   System.out.println();
    		   }
    		   System.out.print(intersections[q/10][q%10]+ ", ");
    	   }
    	   System.out.println();
    	   for ( int q = 0; q < 100; q++){
    		   if( q%10 == 0){
    			   System.out.println();
    		   }
    		   System.out.print(this.theirGrid[q/10][q%10]+ ", ");
    	   }
    	   System.out.println();
    	   System.out.println("col_num: " + sect_col_num[0]);
    	   System.out.println("row_num: " + sect_row_num[0]);
    	   System.out.println("sect_row: "+sect_coords_row[0]);
       }
       return new Coordinate(bestX, bestY);

    }
*/
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
                                break;
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
                                break;
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

        //apply factors
        double best = 0;
        int bestX = 0;
        int bestY = 0;
        
        double[][] appHeat = new double[10][10];
        
        for (int q = 0; q < 100; q++) {
        	double total = 0;
        	for( int s = 0; s < 5; s++){
        		if ( !this.enemyShips[s] ){
        			continue;
        		} 
        		total += this.avgHeat[q % 10][q / 10][s];
        	}
        	appHeat[q % 10][q / 10] = heat[q % 10][q / 10] * total; 
        	if (appHeat[q % 10][q / 10] > best) {
                best = appHeat[q % 10][q / 10];
                bestX = q % 10;
                bestY = q / 10;
            }
        }
        
       
        //for method error possibility (early game)
        if (!checkCoord(bestX, bestY)) {
            for (int q = 0; q < 100; q++) {            	 
            	if (heat[q % 10][q / 10] > best) {
                    best = heat[q % 10][q / 10];
                    bestX = q % 10;
                    bestY = q / 10;
                }
            }
        }
        return new Coordinate(bestX, bestY);

    }

    //mskr this.hitShips an ArrayList<String>[]
    @Override
    public void resultOfAttack(int result) {
    	if( this.turn_num>69){
	    	for (int rep = 0; rep < 100; rep++){
				if(rep%10==0){
					System.out.println();
				}
				if( this.theirGrid[rep%10][rep/10] ){
					System.out.print(" "+this.theirGrid[rep%10][rep/10]+" ");
				}
				else{
					System.out.print(""+this.theirGrid[rep%10][rep/10]+" ");
				}
			}
	    	System.out.println("Last Shot: "+this.lastShot);
    	}
		
        if( result != MISS ){
            this.hitsHeat[this.lastShot.getX()][this.lastShot.getY()][result % 10] += 2;
            if( this.lastShot.getX() > 0 ){
            	this.hitsHeat[this.lastShot.getX() -1 ][this.lastShot.getY()][result % 10]++;
            }
            if( this.lastShot.getX() < 9){
            	this.hitsHeat[this.lastShot.getX() +1 ][this.lastShot.getY()][result % 10]++;
            }
            if( this.lastShot.getY() > 0 ){
            	this.hitsHeat[this.lastShot.getX()  ][this.lastShot.getY()-1][result % 10]++;
            }
            if( this.lastShot.getY() < 9){
            	this.hitsHeat[this.lastShot.getX()  ][this.lastShot.getY()+1][result % 10]++;
            }
            
            if (result >= 20) {
            	//System.out.println( "ship id:" + (result % 20));
            	int index = -2;
            	for( ArrayList<Integer> s : this.hitShips ){
            		//System.out.println( "suggested id: " + s.get(0));
            		if( s.get(0) == result % 20 ){
            			index = this.hitShips.indexOf(s);
                	} 
            	} 
            	this.enemyShips[this.hitShips.get(index).get(0)] = false;
            	this.hitShips.remove(index);
                this.availableShots.clear();
            }
           
            else {
            	ArrayList<Integer> hit_ship = null;
            	for( ArrayList<Integer> s : this.hitShips ){
            		if( s.get(0) == result % 10 ){
            			hit_ship = s;
            		}
            	}
            	//ship hit before
                if ( hit_ship != null) {
                	//ship is verticle
                	if (hit_ship.get(2) == lastShot.getX()){
                		hit_ship.add(lastShot.getY() );
                		hit_ship.set( 1, 1);
                	}
                	else {
                		hit_ship.add( lastShot.getX() );
                		hit_ship.set( 1, 0 );
                	}
                }
                //ship never hit before
                else {
                    int shipMod = result % 10;
                	ArrayList<Integer> ship = new ArrayList<Integer>();
                	ship.add(shipMod);
                	ship.add(0);
                	ship.add(lastShot.getX());
                	ship.add(lastShot.getY());
                    this.hitShips.add(ship);
                }
            }
            
            
        }
        
        buildShots();
        
    }

    
    private void buildShots() {
        this.availableShots.clear();
        //System.out.println("building some shots!");
        //indicies of available shots that are 1 away form a hit
        for( ArrayList<Integer> hit_ship : this.hitShips ){
            //check to make sure ship can fit in that direction if there is only one hit
            int ship_left = this.shipLength[hit_ship.get(0)] - (hit_ship.size() - 3);
            //if the ship has only been hit once, no additional numbers.
            if ( hit_ship.size() == 4 ) {
                //right-left
                
                int y = hit_ship.get(3);
                int x = hit_ship.get(2);
                int rspaces = 0;
                int lspaces = 0;
                int tspaces = 0;
                int bspaces = 0;
                
                for( int j = -1; j<2; j += 2){
                    if( checkCoord(x+(1*j),y)){

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
                	
                	hor = find_best_fit(x,y,lspaces, rspaces, ship_left, 0, hit_ship.get(0));
                }

                if( (tspaces + bspaces) >= ship_left){
                	
                	vert = find_best_fit(x,y,tspaces, bspaces, ship_left, 1, hit_ship.get(0));
                }

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
            	if( hit_ship.get( 1 ) == 0 ){
	            	//check if shots are consecutive
	            	int[] shots = new int[ hit_ship.size() - 3 ];
	            	shots[0] = hit_ship.get( 2 ) ;
	            	for( int i = 1; i < shots.length; i++ ){
	            		shots[i] = hit_ship.get( 3 + i );
	            	}
	            	Arrays.sort(shots);
	            	//are the hits continous?
	            	boolean contin = true;
	            	int missing = 0;
	            	for( int j = 1; j < shots.length; j++){
	            		if( shots[j] - shots[j-1] != 1 ){
	            			contin = false;
	            			missing = shots[j-1] + 1;
	            			break;
	            		}
	            	}
	            	//if not shoot in between shots
	            	int y = hit_ship.get( 3 );
	            	if( !contin ){
	            		this.availableShots.add(0, new Coordinate( missing, y ) );
	            		
	            	}
	            	else{
	            		int ship_mod = hit_ship.get(0);
	            		boolean both_pos = (checkCoord( shots[0] - 1, y) && checkCoord( shots[ shots.length - 1 ] + 1, y ) ) ? true : false ;
	            		if(  both_pos && this.avgHeat[ shots[ 0 ] - 1  ][ y ][ ship_mod ] >  this.avgHeat[ shots[ shots.length - 1 ] + 1 ][ y ][ ship_mod ]){
	            			availableShots.add(new Coordinate( shots[0] - 1, y ) );
	            		} 
	            		else if( both_pos ){
	            			availableShots.add(new Coordinate( shots[ shots.length - 1 ] + 1, y ) );
	            		}
	            		else if( checkCoord( shots[0] - 1, y) ){
	            			availableShots.add(new Coordinate( shots[0] - 1, y ) );
	            		}
	            		else{
	            			availableShots.add(new Coordinate( shots[ shots.length - 1 ] + 1, y ) );
	            		}
	            	}
            	}
            	else{
            		//check if shots are consecutive
	            	int[] shots = new int[ hit_ship.size() - 3 ];
	            	shots[0] = hit_ship.get( 3 ) ;
	            	for( int i = 1; i < shots.length; i++ ){
	            		shots[i] = hit_ship.get( 3 + i );
	            	}
	            	Arrays.sort(shots);
	            	//are the hits continous?
	            	boolean contin = true;
	            	int missing = 0;
	            	for( int j = 1; j < shots.length; j++){
	            		if( shots[j] - shots[j-1] != 1 ){
	            			contin = false;
	            			missing = shots[j-1] + 1;
	            			break;
	            		}
	            	}
	            	//if not shoot in between shots
	            	int x = hit_ship.get( 2 );
	            	if( !contin ){
	            		this.availableShots.add(0, new Coordinate( x, missing ) );
	            	}
	            	else{
	            		int ship_mod = hit_ship.get(0);
	            		boolean both_pos = (checkCoord( x, shots[0] - 1 ) && checkCoord( x, shots[ shots.length - 1 ] + 1 ) ) ? true : false ;
	            		if(  both_pos && this.avgHeat[ x ][ shots[ 0 ] - 1  ][ ship_mod ] >  this.avgHeat[ x ][ shots[ shots.length - 1 ] + 1 ][ ship_mod ]){
	            			availableShots.add(new Coordinate( x, shots[0] - 1 ) );
	            		} 
	            		else if( both_pos ){
	            			availableShots.add(new Coordinate( x, shots[ shots.length - 1 ] + 1 ) );
	            		}
	            		else if( checkCoord( x, shots[0] - 1 ) ){
	            			availableShots.add(new Coordinate( x, shots[0] - 1 ) );
	            		}
	            		else{
	            			availableShots.add(new Coordinate( x, shots[ shots.length - 1 ] + 1 ) );
	            		}
	            	}
            	}
            	
            }
        }
        
    }
    
    //only get called for a ship hit 1 time
    private Coordinate find_best_fit(int x, int y, int neg_spaces, int pos_spaces, int needed_spaces, int i, int ship_type) {

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
    				rect_value += ( this.avgHeat[k+j][y][ship_type] * this.heatFactor);
    			}
    			
    			if( rect_value > best_rect_val ){
    				best_rect_val = rect_value;
    				best_rect_start = new Coordinate(j, y); //rect contnues in +x direction
    			}
    		}
    		this.cur_hor = best_rect_val;
    		//ship pinning
    		if( needed_spaces > neg_spaces){
    			return new Coordinate( x+1, y);
    		}
    		else if( needed_spaces > pos_spaces ){
    			return new Coordinate(x-1, y);
    		}
    		/////
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
    				rect_value += ( this.avgHeat[x][k+j][ship_type] * this.heatFactor);
    			}
    			
    			if( rect_value > best_rect_val ){
    				best_rect_val = rect_value;
    				best_rect_start = new Coordinate(x, j); //rect contnues in +x direction
    			}
    		}
    		this.cur_ver = best_rect_val;
    		
    		//ship pinning
    		if( needed_spaces > neg_spaces){
    			return new Coordinate( x, y+1);
    		}
    		else if( needed_spaces > pos_spaces){
    			return new Coordinate(x, y-1);
    		}
    		//
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

    private boolean checkCoord(int x, int y) {
        if (x < 10 && y < 10 && x >= 0 && y >= 0) {
            if (!this.theirGrid[x][y]) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void opponentAttack(Coordinate coord) {
        this.theirShots[coord.getX()][coord.getY()]++;
    }

    @Override
    //recalculates the heat of each cell
    public void resultOfGame(int result) {
    	if( result == WON ){
    		this.placementRate[this.seed][0]++;
    		
    	}
    	else{
    		this.placementRate[this.seed][1]++;
    		figureShips();
    		if( this.matchNumber > .1 * this.matchTotal){
    			determineSeed();
    		}
    		int ships_hit = 0;
        	for( boolean b : this.enemyShips){
        		if(!b){
        			ships_hit++;
        		}
        	}
        	ships_hit += this.hitShips.size();
        	this.ships_left += 5 - ships_hit;
        	if( ships_hit == 5 ){
        		int qq = 0;
        		for( ArrayList<Integer> i : this.hitShips){
        			qq += this.shipLength[i.get(0)] - (i.size() - 3);
        		}
        		this.ship_hits_left += qq;
        		this.last_ship_losses++;
        	}
        	this.losses++;
    	}
        for (int i = 0; i < 100; i++) {
        	for (int j = 0; j < 5; j++){
	            if (this.shotsHeat[i / 10][i % 10] != 0) {
	                this.avgHeat[i / 10][i % 10][j] = (double) (this.hitsHeat[i / 10][i % 10][j]) / (double) (this.shotsHeat[i / 10][i % 10]);
	            } else {
	                this.avgHeat[i / 10][i % 10][j] = 0;
	            }
        	}
        }
        if (this.turn_num < this.shortest_game){
			this.shortest_game = this.turn_num;
		}
		else if(this.turn_num > this.longest_game){
			this.longest_game = this.turn_num;
		}
		if (this.turn_num > 69 ){
			this.games_over_69++;
			
			
			
			
		}
		this.total_turns = this.total_turns + this.turn_num;
        if( this.matchNumber == this.matchTotal - 1){
	        /*If match over print stats
	        for ( int i = 0; i < this.placeMethods; i++ ){
	        	double placeRate;
	    		if ( this.placementRate[i][1] == 0 ){
	    			placeRate = this.placementRate[i][0];
	    		}
	    		else{
	    			placeRate = (double) this.placementRate[i][0] / (double) this.placementRate[i][1];
	    		}
	    		 System.out.printf("Case %d rate: %f \n", i, placeRate );	
	    	}
	        System.out.println("ended on seed: " + this.seed );
	        */
        	
        	System.out.println("Shortest Game: "+this.shortest_game);
        	System.out.println("Longest Game: "+this.longest_game);
        	System.out.println("Games over 65: "+this.games_over_69);
        	System.out.println("Average Turns: "+((double)this.total_turns/(double)this.matchNumber));
        }
       

    }

	private void figureShips() {

		if( !this.hitShips.isEmpty() ){
			for( ArrayList<Integer> ship : this.hitShips ){
				this.enemyShips[ship.get(0)] = false;
				//was only hit once
				if ( ship.size() < 5){
					int y = ship.get(3);
	                int x = ship.get(2);
	                int rspaces = 0;
	                int lspaces = 0;
	                int tspaces = 0;
	                int bspaces = 0;
	                int ship_left = this.shipLength[ship.get(0)] - 1;
	                
	                for( int j = -1; j<2; j += 2){
	                    if( checkCoord(x+(1*j),y)){

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
					
	                
	                if( tspaces + bspaces > ship_left){
	                	int[] y_coords = new int[ship.size() - 3];
						y_coords[0] = ship.get(3);
						for(int k = 0; k < (ship.size() - 4); k++){
							y_coords[1+k] = ship.get(4+k);
						}
						Arrays.sort(y_coords);
						
						for( int j = 1; j < (this.shipLength[ship.get(0)] - (ship.size() - 3) ); j++){
							if(checkCoord( ship.get(2), y_coords[y_coords.length - 1] + j)){
								this.hitsHeat[ship.get(2)][ y_coords[y_coords.length - 1] + j][ship.get(0)]++;
							}
							else{
								break;
							}
						}
						for( int j = 1; j < (this.shipLength[ship.get(0)] - (ship.size() - 3) ); j++){
							if(checkCoord( ship.get(2), y_coords[0] + j)){
								this.hitsHeat[ship.get(2)][ y_coords[0] + j][ship.get(0)]++;
							}
							else{
								break;
							}
						}
	                }
	                if( rspaces + lspaces > ship_left ){
	                	int[] x_coords = new int[ship.size() - 3];
						x_coords[0] = ship.get(2);
						for(int k = 0; k < (ship.size() - 4); k++){
							x_coords[1+k] = ship.get(4+k);
						}
						Arrays.sort(x_coords);
						
						for( int j = 1; j < (this.shipLength[ship.get(0)] - (ship.size() - 3) ); j++){
							if(checkCoord( x_coords[x_coords.length - 1] + j, ship.get(3))){
								this.hitsHeat[x_coords[x_coords.length - 1] + j][ ship.get(3)][ship.get(0)]++;
							}
							else{
								break;
							}
						}
						for( int j = 1; j < (this.shipLength[ship.get(0)] - (ship.size() - 3) ); j++){
							if(checkCoord( x_coords[0] - j, ship.get(3))){
								this.hitsHeat[x_coords[0] - j][ ship.get(3)][ship.get(0)]++;
							}
							else{
								break;
							}
						}
	                }
				}
				else{
					if( ship.get(1) == 0){
						int[] x_coords = new int[ship.size() - 3];
						x_coords[0] = ship.get(2);
						for(int k = 0; k < (ship.size() - 4); k++){
							x_coords[1+k] = ship.get(4+k);
						}
						Arrays.sort(x_coords);
						
						for( int j = 1; j < (this.shipLength[ship.get(0)] - (ship.size() - 3) ); j++){
							if(checkCoord( x_coords[x_coords.length - 1] + j, ship.get(3))){
								this.hitsHeat[x_coords[x_coords.length - 1] + j][ ship.get(3)][ship.get(0)]++;
							}
							else{
								break;
							}
						}
						for( int j = 1; j < (this.shipLength[ship.get(0)] - (ship.size() - 3) ); j++){
							if(checkCoord( x_coords[0] - j, ship.get(3))){
								this.hitsHeat[x_coords[0] - j][ ship.get(3)][ship.get(0)]++;
							}
							else{
								break;
							}
						}
						
					}
					else{
						int[] y_coords = new int[ship.size() - 3];
						y_coords[0] = ship.get(3);
						for(int k = 0; k < (ship.size() - 4); k++){
							y_coords[1+k] = ship.get(4+k);
						}
						Arrays.sort(y_coords);
						
						for( int j = 1; j < (this.shipLength[ship.get(0)] - (ship.size() - 3) ); j++){
							if(checkCoord( ship.get(2), y_coords[y_coords.length - 1] + j)){
								this.hitsHeat[ship.get(2)][ y_coords[y_coords.length - 1] + j][ship.get(0)]++;
							}
							else{
								break;
							}
						}
						for( int j = 1; j < (this.shipLength[ship.get(0)] - (ship.size() - 3) ); j++){
							if(checkCoord( ship.get(2), y_coords[0] + j)){
								this.hitsHeat[ship.get(2)][ y_coords[0] + j][ship.get(0)]++;
							}
							else{
								break;
							}
						}
					}
				}
			}
		}
		for( int i = 0; i < 5; i++ ){
			
		}
	}
}
