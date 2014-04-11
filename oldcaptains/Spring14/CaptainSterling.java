
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class CaptainSterling implements Captain, Constants{
	
	//where the opponent has shot
    private int[][] theirShots = new int[10][10];
    //a map of where the opponents have hit my ships
    private int[][] theirHits = new int[10][10];
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
    //a seed for ship atacking
    private int attackSeed;
    //the turn number it is 
    private int turn_num = 0;
    //a value for weighting shots
    private int shotValue;
    //value for area around my shot
    private int shotOffValue;
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
    //more detailed stats of the enemy ship
    private ArrayList<ArrayList<Integer>> hitShips;
    //the shots that I can make (based on hitting ships)
    private ArrayList<Coordinate> availableShots = new ArrayList<Coordinate>();
    //the spot where i last fired
    private Coordinate lastShot;   
    //the name of my opponent
    private String lastOpp = "";
    // a random number generator
    private Random rGen = new Random();
    //my fleet object
    private Fleet myFleet;
    //the number of placement methods I have
    private int placeMethods = 1;
    //the number of attack patterns
    private int attackMethods = 4;
    //the tracker for my place methods
    private int[][] placementRate;
    //the tracker for my attack methods
    private int[][] attackRate;
    
	@Override
	public void initialize(int numMatches, int numCaptains, String opponent) {

		this.lastShot = null;
        this.turn_num = 0;
        this.shotValue = 2;
        this.shotOffValue = 1;
        
        //reset enemy grid
        for (boolean[] b : this.theirGrid) {
            Arrays.fill(b, false);
        }
        
        //true mens enemy ship is still alive
        Arrays.fill(this.enemyShips, true);
        
        //reset hitting ships
        this.hitShips = new ArrayList<ArrayList<Integer>>();

        //match count increment
        if (opponent.equals(this.lastOpp)) {
            this.matchNumber++;            
        } //else reset all the opponent data
        else {
        	this.placementRate = new int[this.placeMethods][2];
        	this.attackRate = new int[this.attackMethods][2];
            this.matchTotal = numMatches;
            this.theirShots = new int[10][10];
            this.matchNumber = 1;
            this.shotsHeat = new int[10][10];
            this.hitsHeat = new int[10][10][5];
            this.avgHeat = new double[10][10][6];
            this.lastOpp = opponent;
            this.seed = 2;
            this.attackSeed = 0;
            //reset where i've been placing ships
            for (int[] b : this.placeHeat){
            	Arrays.fill(b, 0);
            }
            
        }

        //reset a map of where my ships are
        for (boolean[] b : this.myShips) {
            Arrays.fill(b, false);
        }
        
        //make a new fleet
        this.myFleet = new Fleet();
        
        if( this.matchNumber < .1 * this.matchTotal ){
        	this.seed = (this.seed + 1) % this.placeMethods;
        	//only switch attack pattern on a ful cycle of placements 
        	if( this.seed == 2){
        		this.attackSeed = (this.attackSeed + 1) % this.attackMethods;
        	}
        }

    	placeShips();
		
	}

    /**
     * this method determines which strategys to use.
     */
    private void determineSeeds() {

    	double bestPlace = 0;
    	//place where shooting + missing
    	//place where not shooting
    	//place in average shot rate
    	
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
    	
    	double bestAttack = 0;
    	//if random weigh middle 5-10 spots
    	//if hiding shoot where not shooting
    	//or shoot where not hitting
    	for ( int j = 0; j < this.attackMethods; j++ ){
    		double attackRate = 0;
    		if ( this.attackRate[j][1] == 0 ){
    			attackRate = this.attackRate[j][0];
    		}
    		else{
    			attackRate = (double) this.attackRate[j][0] / (double) this.attackRate[j][1];
    		}
    		if( attackRate > bestAttack ){
    			bestAttack = attackRate;
    			this.attackSeed = j;
    		}
    	}		
    	
    	this.heatFactor = 100.0 * (double) (this.matchNumber) / (double) (this.matchTotal);
	}
    
	private void placeShips() {
    	
    	switch( this.seed ){
    	//case 0: evenDistributeTouchingPlace(); break;
    	case 1: evenDistributePlace(); break;
    	case 2: learningPlace(); break;
    	case 3: randomPlace(); break;
    	case 0: randomPlace(); break;
    	//case 4: zigzagoonPlace(); break;      	
    	}	

	}
	
	private void learningPlace() {
		for (int shipType = 0; shipType < 5; shipType++) {
			int[] placement = leastShotPlace(this.shipLength[shipType]);
            placeShip(placement[0], placement[1], placement[2], shipType);
		}		
	}
	
	private void zigzagoonPlace() {
		
		ArrayList<int[]> starts = new ArrayList<int[]>();
		
		for( int shipType = 4; shipType>=0; shipType--){
			
			int shipLen = this.shipLength[shipType];
			int x = 0;
        	int y = 0;
        	int z = 0;
	        
	        
	        //randomly place the large ship
	        if( shipType == 4){
	        	
	        	x = this.rGen.nextInt(10);
	        	y = this.rGen.nextInt(10);
	        	z = this.rGen.nextInt(2);
	        	
	        	while(!placeShip(x,y,z,shipType)){
	        		x = this.rGen.nextInt(10);
		        	y = this.rGen.nextInt(10);
		        	z = this.rGen.nextInt(2);
	        	}
	        	/*
	        	 * 0 = right
	        	 * 1 = up
	        	 * 2 = left
	        	 * 3 = down
	        	 */
	        	
	        	
	        }
	     //not the 5-length ship
	        else{
	        	boolean placed = false;
	        	
	        	if(starts.isEmpty()){
	        		x = this.rGen.nextInt(10);
		        	y = this.rGen.nextInt(10);
		        	z = this.rGen.nextInt(2);
		        	
		        	while(!placeShip(x,y,z,shipType)){
		        		x = this.rGen.nextInt(10);
			        	y = this.rGen.nextInt(10);
			        	z = this.rGen.nextInt(2);
		        	}
	        	}
	        	else{
	        		for( int[] args : starts){
	        			switch(args[2]){
	        			
		        			case 0:	x=args[0]; 			y=args[1]; 			z = 0; placed = placeShip(args[0],args[1],0,shipType); break;
		        			
		        			case 1: x=args[0]; 			y=args[1]-shipLen+1; 	z = 1; placed = placeShip(args[0],args[1]-shipLen+1,1,shipType); break;
		        			
		        			case 2: x=args[0]-shipLen+1; 	y=args[1]; 			z = 0; placed = placeShip(args[0]-shipLen+1,args[1],0,shipType); break;
		        			
		        			case 3: x=args[0]; 			y=args[1]; 			z = 1; placed = placeShip(args[0], args[1], 1, shipType); break;
		        			
	        			}
	        			if(placed){
	        				starts.remove(args);
	        				break;
	        			}
	        		}
	        		//no available placements
	        		if(!placed){
	        			x = this.rGen.nextInt(10);
			        	y = this.rGen.nextInt(10);
			        	z = this.rGen.nextInt(2);
			        	
			        	while(!placeShip(x,y,z,shipType)){
			        		x = this.rGen.nextInt(10);
				        	y = this.rGen.nextInt(10);
				        	z = this.rGen.nextInt(2);
			        	}
	        		}
	        	}	        	
	        }
	        
	      //placed a ship, now add points to arraylist  
	      //horizontal
        	if( z==0 ){
        		if( y > 0 ){
        			if( (x+shipLen) < 8 ){
        				starts.add(new int[]{x+shipLen,y-1,0});
        			}
        			if( x > 1 ){
        				starts.add(new int[]{x-1,y-1,2});
        			}
	        		if( y > 1 ){
	        			if( x > 0 ){
	        				starts.add(new int[]{x-1,y-1,1});
	        			}
	        			if( (x+shipLen) < 9 ){
	        				starts.add(new int[]{x+shipLen,y-1,1});
	        			}
	        		}
	        		
        		}
	        	if( y < 9){
	        		if( (x+shipLen) < 8 ){
        				starts.add(new int[]{x+shipLen,y+1,0});
        			}
        			if( x > 1 ){
        				starts.add(new int[]{x-1,y+1,2});
        			}
	        		if( y < 8 ){
	        			if( x > 0 ){
	        				starts.add(new int[]{x-1,y+1,3});
	        			}
	        			if( (x+shipLen) < 9 ){
	        				starts.add(new int[]{x+shipLen,y+1,3});
	        			}
	        		}
	        	}
        	}
        	//verticle
        	else{
        		if( x > 0 ){
        			if( (y+shipLen) < 8 ){
        				starts.add(new int[]{x-1,(y+shipLen),3});
        			}
        			if( y > 1 ){
        				starts.add(new int[]{x-1,y-1,1});
        			}
	        		if( x > 1 ){
	        			if( y > 0 ){
	        				starts.add(new int[]{x-1,y-1,2});
	        			}
	        			if( (y+shipLen) < 9 ){
	        				starts.add(new int[]{x-1,(y+shipLen),2});
	        			}
	        		}
	        		
        		}
	        	if( x < 9){
	        		if( (y+shipLen) < 8 ){
        				starts.add(new int[]{x+1,y+shipLen,3});
        			}
        			if( y > 1 ){
        				starts.add(new int[]{x+1,y+1,1});
        			}
	        		if( x > 1 ){
	        			if( y > 0 ){
	        				starts.add(new int[]{x+1,y+1,0});
	        			}
	        			if( (y+shipLen) < 9 ){
	        				starts.add(new int[]{x+1,(y+shipLen),0});
	        			}
	        		}
	        	}
        	}
        	//mix up the array to avoid horizontal tendency
        	starts = shuffle(starts);
		}	
	}

	private ArrayList<int[]> shuffle(ArrayList<int[]> list){
		int shuf_seed = this.rGen.nextInt(list.size());
		for (int i = 0; i < (list.size()/2); i++){
			list.add(list.remove(shuf_seed));
		}
		return list;
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
			randomPlace();
		}
		
	}

    private void evenDistributePlace() {
		
		if( this.matchNumber > 100){
			//a little random to avoid repeating same pattern
			boolean r = this.rGen.nextBoolean();
			
			for( int shipType = 0; shipType<5; shipType++){
				
				int shipLen = this.shipLength[shipType];
				int bestRect = -1;
		        Coordinate rectCoord = null;
		        
				if( r ){					
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
				placeShip(rectCoord.getX(), rectCoord.getY(), r ? 1 : 0, shipType);				
			}
		}else{
			randomPlace();
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
        return ret;
    }
    
    private void shotHere(Coordinate c) {
    	if( !checkCoord(c.getX(), c.getY()) ){
        	System.out.println("bad shot!");
        }
    	this.turn_num++;
        this.shotsHeat[c.getX()][c.getY()]++;
        this.theirGrid[c.getX()][c.getY()] = true;
    }
    
	@Override
	public Fleet getFleet() {
		return this.myFleet;
	}

	@Override
	//TODO fix this
	public Coordinate makeAttack() {
		
		Coordinate shot;
		switch(this.attackSeed){
		case 0:		this.shotValue = 2; 	this.shotOffValue = 1;		break;
		case 1: 	this.shotValue = 0;		this.shotOffValue = 2;		break;
		case 2:		this.shotValue = 1;		this.shotOffValue = 2;		break;
		case 3:		this.shotValue = 2;		this.shotOffValue = 0;		break;
        default: 	this.shotValue = 2; 	this.shotOffValue = 1; 		break;
		}
		shot = defaultAttack(5);
		
		this.lastShot = shot;
        shotHere(shot);
        return shot;
        
    }

	private Coordinate defaultAttack( int scouts) {
		if ( this.turn_num < scouts ) {
            Coordinate shot = makeScoutShot();            
            return shot;
        } 
        else if( this.availableShots.size() > 0 ) {    	 	
            return this.availableShots.remove(0);            
        }
        else {
        	if( this.matchNumber > 500){
        		 return makeEducatedShot();
        	}
        	else{
        		return makeAvailableShot();
        	}
        }
		
	}
	
	private Coordinate makeScoutShot(){
    	if(this.matchNumber < 500){
    		return makeMinimizerShot();
    	}
    	else{
    		return makeProbabilityShot();
    	}
    }
	
	private Coordinate makeProbabilityShot() {

		//TODO apply 50 filter
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
	
	private Coordinate makeMinimizerShot() {   
		//TODO apply a 50 grid to this
		int r = this.rGen.nextInt(100);
		int x = r/10;
		int y = r%10;
		
		boolean rerun = true;
		
		while( rerun ){
			rerun = false;
			for (int i = 0; i < 10; i++){				
				if( this.theirGrid[x][i] ){
					rerun = true;
					break;
				}
				if( this.theirGrid[i][y] ){
					rerun = true;
					break;
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

	private Coordinate makeEducatedShot() {
       
    	double[][] appHeat = new double[10][10];
        double best = 0;
        int bestX = 0;
        int bestY = 0;
        
        

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
                            	appHeat[i][j + k] += this.avgHeat[i ][j+k][s];
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
                            	appHeat[i+k][j] += this.avgHeat[i + k][j][s];
                            }
                        }
                    }
                }

            }
        }

        //TODO apply 50 filter
        for (int q = 0; q < 100; q++) {
        	if (appHeat[q % 10][q / 10] > best) {
                best = appHeat[q % 10][q / 10];
                bestX = q % 10;
                bestY = q / 10;
            }
        }
        
        return new Coordinate(bestX, bestY);

    }

	private Coordinate makeAvailableShot() {
	       
    	int[][] appHeat = new int[10][10];
        double best = 0;
        int bestX = 0;
        int bestY = 0;
        
        

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
                            	appHeat[i][j + k]++;
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
                            	appHeat[i+k][j]++;
                            }
                        }
                    }
                }

            }
        }

        for (int q = 0; q < 100; q++) {
        	if (appHeat[q % 10][q / 10] > best) {
                best = appHeat[q % 10][q / 10];
                bestX = q % 10;
                bestY = q / 10;
            }
        }
        
        return new Coordinate(bestX, bestY);

    }
	
	public void resultOfAttack(int result) {
        if( result != MISS ){
            this.hitsHeat[this.lastShot.getX()][this.lastShot.getY()][result % 10] += this.shotValue;
            if( this.lastShot.getX() > 0 ){
            	this.hitsHeat[this.lastShot.getX() -1 ][this.lastShot.getY()][result % 10] += this.shotOffValue;
            }
            if( this.lastShot.getX() < 9){
            	this.hitsHeat[this.lastShot.getX() +1 ][this.lastShot.getY()][result % 10] += this.shotOffValue;
            }
            if( this.lastShot.getY() > 0 ){
            	this.hitsHeat[this.lastShot.getX()  ][this.lastShot.getY()-1][result % 10] += this.shotOffValue;
            }
            if( this.lastShot.getY() < 9){
            	this.hitsHeat[this.lastShot.getX()  ][this.lastShot.getY()+1][result % 10] += this.shotOffValue;
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
    				rect_value += ( this.avgHeat[k+j][y][ship_type] * this.heatFactor );
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
    				rect_value += ( this.avgHeat[x][k+j][ship_type] * this.heatFactor );
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
    

    public void resultOfGame(int result) {
    	if( result == WON ){
    		this.placementRate[this.seed][0]++;
    		this.attackRate[this.attackSeed][0]++;
    	}
    	else{
    		this.placementRate[this.seed][1]++;
    		this.attackRate[this.attackSeed][1]++;
    		//figureShips();
    		if( this.matchNumber >= .1 * this.matchTotal){
    			determineSeeds();
    		}
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
        if( this.matchNumber == this.matchTotal - 1){
	        //If match over print stats
	        for ( int i = 0; i < this.attackMethods; i++ ){
	        	double attackRate;
	    		if ( this.attackRate[i][1] == 0 ){
	    			attackRate = this.attackRate[i][0];
	    		}
	    		else{
	    			attackRate = (double) this.attackRate[i][0] / (double) this.attackRate[i][1];
	    		}
	    		 System.out.printf("Case %d rate: %f \n", i, attackRate );	
	    	}
	        System.out.println("ended on seed: " + this.attackSeed );
        }
    }

    @Override
    public void opponentAttack(Coordinate coord) {
        this.theirShots[coord.getX()][coord.getY()]++;
        if( this.myFleet.isShipAt(coord)){
        	this.theirHits[coord.getX()][coord.getY()]++;
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
								this.hitsHeat[ship.get(2)][ y_coords[y_coords.length - 1] + j][ship.get(0)] += this.shotOffValue;
							}
							else{
								break;
							}
						}
						for( int j = 1; j < (this.shipLength[ship.get(0)] - (ship.size() - 3) ); j++){
							if(checkCoord( ship.get(2), y_coords[0] + j)){
								this.hitsHeat[ship.get(2)][ y_coords[0] + j][ship.get(0)] += this.shotOffValue;
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
								this.hitsHeat[x_coords[x_coords.length - 1] + j][ ship.get(3)][ship.get(0)] += this.shotOffValue;
							}
							else{
								break;
							}
						}
						for( int j = 1; j < (this.shipLength[ship.get(0)] - (ship.size() - 3) ); j++){
							if(checkCoord( x_coords[0] - j, ship.get(3))){
								this.hitsHeat[x_coords[0] - j][ ship.get(3)][ship.get(0)] += this.shotOffValue;
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
								this.hitsHeat[x_coords[x_coords.length - 1] + j][ ship.get(3)][ship.get(0)] += this.shotOffValue;
							}
							else{
								break;
							}
						}
						for( int j = 1; j < (this.shipLength[ship.get(0)] - (ship.size() - 3) ); j++){
							if(checkCoord( x_coords[0] - j, ship.get(3))){
								this.hitsHeat[x_coords[0] - j][ ship.get(3)][ship.get(0)] += this.shotOffValue;
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
								this.hitsHeat[ship.get(2)][ y_coords[y_coords.length - 1] + j][ship.get(0)] += this.shotOffValue;
							}
							else{
								break;
							}
						}
						for( int j = 1; j < (this.shipLength[ship.get(0)] - (ship.size() - 3) ); j++){
							if(checkCoord( ship.get(2), y_coords[0] + j)){
								this.hitsHeat[ship.get(2)][ y_coords[0] + j][ship.get(0)] += this.shotOffValue;
							}
							else{
								break;
							}
						}
					}
				}
			}
		}
	}
}
