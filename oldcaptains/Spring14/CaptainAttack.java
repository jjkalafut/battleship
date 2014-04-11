
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * TODO Put here a description of what this class does.
 *
 * @author John. Created Mar 18, 2013.
 */
public class CaptainAttack implements Captain, Constants {

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
    //current win rate
    private int wins = 0;
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
            if( this.matchNumber % 10000 == 0){
            	double win_rate = (double) this.wins / (double) 10000;
            	if( win_rate < .51){
            		this.seed++;
            		this.seed = this.seed % 3;
            	}
            	this.wins = 0;
            }
        } //else reset all the opponent data
        else {

        	//
            this.matchTotal = numMatches;
            this.theirShots = new int[10][10];
            this.matchNumber = 1;
            this.shotsHeat = new int[10][10];
            this.hitsHeat = new int[10][10][5];
            this.avgHeat = new double[10][10][6];
            this.lastOpp = opponent;
            //reset where i've been placing ships
            for (int[] b : this.placeHeat){
            	Arrays.fill(b, 0);
            }
            
        }
        /*
        if( this.matchNumber == this.matchTotal){
        	System.out.println("Average ships left for a loss: "+ ((double) this.ships_left / (double) (this.losses)));
        	System.out.println("% games lost on last ship: "+ ((double) this.last_ship_losses / (double) this.matchTotal));
        	System.out.println("Average hits left on last ship loss: "+ ((double) this.ship_hits_left / (double) (this.last_ship_losses)));
        }\
        */
        //reset a map of where my ships are
        for (boolean[] b : this.myShips) {
            Arrays.fill(b, false);
        }
        

        //make a new fleet
        this.myFleet = new Fleet();
        
        placeShips();

        this.heatFactor = 100.0 * (double) (this.matchNumber) / (double) (this.matchTotal);
    }

    private void placeShips() {
    	
    	while (!myFleet.placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), PATROL_BOAT)) {
        }
        while (!myFleet.placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), DESTROYER)) {
        }
        while (!myFleet.placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), SUBMARINE)) {
        }
        while (!myFleet.placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), BATTLESHIP)) {
        }
        while (!myFleet.placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), AIRCRAFT_CARRIER)) {
        }

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
    	if(this.matchNumber < 500){
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
    		this.wins++;
    	}
    	for (int i = 0; i < 100; i++) {
        	for (int j = 0; j < 5; j++){
	            if (this.theirGrid[i / 10][i % 10]) {
	                this.avgHeat[i / 10][i % 10][j] = ((double) (this.hitsHeat[i / 10][i % 10][j]) / (double) (this.shotsHeat[i / 10][i % 10]));
	            }
        	}
        }

    }

}
