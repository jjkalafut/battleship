
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class CaptainCopyKat implements Captain, Constants {

    
    protected Random generator;

    protected Fleet myFleet;
    private int[] shipLength = {2,3,3,4,5};
    private boolean[] shipsAlive;
    private boolean[][] myMatchShots;
    private ArrayList<Coordinate> followShots;
    private int matchNumber;
    private int matchTotal;
    private String lastOpponent = "";
    private double[][][] hitsHeat;
    private Coordinate lastShot;
    private float heatFactor;
    private ArrayList<ArrayList<Coordinate>> hitShips;

    private ArrayList<ArrayList<Coordinate>> sunkShips;
    private int[] consecMisses;
    private int shootingInd;
    private Coordinate theirLastShot;
    private ArrayList<int[]> theirShips = new ArrayList<int[]>();
    private boolean[] followed_theirShips;

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
    	followed_theirShips = new boolean[]{false,false,false,false,false};
    	//new random
        this.generator = new Random();
        //new fleet
        this.myFleet = new Fleet();
        //true if ship alive
        this.shipsAlive = new boolean[]{true, true, true, true, true};
        //true if i have shot there
        this.myMatchShots = new boolean[10][10];
        //following shots array
        this.followShots = new ArrayList<Coordinate>();
        // ltr
        this.matchTotal = numMatches;
        //xcoord of first coord is ship modifier
        this.hitShips = new ArrayList<ArrayList<Coordinate>>();
        //
        this.sunkShips = new ArrayList<ArrayList<Coordinate>>();
        //
        this.consecMisses = new int[5];
        //
        this.shootingInd = 4;
        //
        this.theirLastShot = null;
        
        for( int i = 0; i < 10; i++){
        	Arrays.fill(this.myMatchShots[i], false);
        }
        
        if( opponent.equals(this.lastOpponent) ){
        	this.matchNumber++;
        }else{
        	this.lastOpponent = opponent;
        	this.matchNumber=0;
        	this.hitsHeat = new double[10][10][5];
        	int[][] random_place = new int[][]{
        			{ 8, 10, 12, 14, 15, 15, 14, 12, 10, 8},
        			{ 10, 12, 14, 15, 17, 17, 15, 14, 12, 10},
        			{ 12, 14, 15, 17, 18, 18, 17, 15, 14, 12},
        			{ 14, 15, 17, 18, 2, 2, 18, 17, 15, 14},
        			{ 15, 17, 18, 2, 21, 21, 2, 18, 17, 15},
        			{ 15, 17, 18, 2, 21, 21, 2, 18, 17, 15},
        			{ 14, 15, 17, 18, 2, 2, 18, 17, 15, 14},
        			{ 12, 14, 15, 17, 18, 18, 17, 15, 14, 12},
        			{ 10, 12, 14, 15, 17, 17, 15, 14, 12, 10},
        			{ 8, 10, 12, 14, 15, 15, 14, 12, 10, 8} 
	        	};
        	for(int j = 0; j < 5; j++){
        		for( int k = 0; k < 100; k++){
        			this.hitsHeat[k%10][k/10][j] = random_place[k%10][k/10];
        		}
        	}
     
        }
        
        this.heatFactor = (float) this.matchNumber / (float) this.matchTotal;
        // Each type of ship must be placed on the board.  Note that the .place method return whether it was
        // possible to put a ship at the indicated position.  If the coordinates were not on the board or if
        // it overlapped with a ship you already tried to place it will return false.
        //randomPlace(myFleet);
        copyPlace();
        this.theirShips = new ArrayList<int[]>();
       
    }

    private void copyPlace() {
		for( int[] ship : this.theirShips){
			if( this.myFleet.placeShip(new Coordinate(ship[1],ship[2]), ship[3], ship[0])){
				this.followed_theirShips[ship[0]] = true;
			}
			else{
				System.out.println(""+ship[0]+", "+ship[1]+", "+ship[2]+", "+ship[3]);
			}
		}
		
		if( !this.followed_theirShips[0] ){
			while (!this.myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), PATROL_BOAT)) {
	        }
		}
		if( !this.followed_theirShips[1] ){			
			while (!this.myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), DESTROYER)) {
			}
			
        }
		if( !this.followed_theirShips[2] ){
				
			while (!this.myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), SUBMARINE)) {
			}
		}
		if( !this.followed_theirShips[3] ){
				
	        while (!this.myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), BATTLESHIP)) {
	        }
		}
		if( !this.followed_theirShips[4] ){
			
	        while (!this.myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), AIRCRAFT_CARRIER)) {
	        }
		}
		
	}

	private void randomPlace(Fleet flt){
    	 while (!flt.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), PATROL_BOAT)) {
         }
         while (!flt.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), DESTROYER)) {
         }
         while (!flt.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), SUBMARINE)) {
         }
         while (!flt.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), BATTLESHIP)) {
         }
         while (!flt.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), AIRCRAFT_CARRIER)) {
         }
    }
    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    @Override
    public Coordinate makeAttack() {
    	
    	//printArray(this.myMatchShots);
    	Coordinate shot = new Coordinate(0,0);
    	if( this.followShots.size() > 0 ){
    		shot = this.followShots.get( 0 );
    		this.followShots.clear();
    	}
    	else{
    		
    		
	    	if(this.theirLastShot == null || this.myMatchShots[this.theirLastShot.getX()][this.theirLastShot.getY()]){
	    		int x = this.generator.nextInt(10);
	    		int y = this.generator.nextInt(10);
	    		
	    		while( this.myMatchShots[x][y] ){
	    			x = this.generator.nextInt(10);
		    		y = this.generator.nextInt(10);
	    		}
	    		
	    		shot = new Coordinate(x,y);
	    	}
	    	else{
	    		shot = this.theirLastShot ;
	    	}

    	}    	
    	
    	
    	this.lastShot = shot;
    	mark(this.myMatchShots, shot);
        return shot;
    }

	private void mark(boolean[][] array, Coordinate shot) {
		if( array[shot.getX()][shot.getY()] ){
			array[shot.getX()][shot.getY()] = false;
		}
		else{
			array[shot.getX()][shot.getY()] = true;
		}		
	}

	private void probibilityShips(double[][] shipsHere) {
		
		
		if( this.shipsAlive[4] ){
			for( int i = 0; i < 6; i++){
				for( int j = 0; j < 10; j++){
					if( !this.myMatchShots[i][j] && !this.myMatchShots[i + 1][j] && !this.myMatchShots[i + 2][j] && !this.myMatchShots[i + 3][j] && !this.myMatchShots[i + 4][j] ){
						shipsHere[i][j]++;
						shipsHere[i + 1][j]++;
						shipsHere[i + 2][j]++;
						shipsHere[i + 3][j]++;
						shipsHere[i + 4][j]++;
					}
				}
			}
			for( int j = 0; j < 6; j++){
				for( int i = 0; i < 10; i++){
					if( !this.myMatchShots[i][j] && !this.myMatchShots[i][j + 1] && !this.myMatchShots[i][j + 2] && !this.myMatchShots[i][j + 3] && !this.myMatchShots[i][j + 4] ){
						shipsHere[i][j]++;
						shipsHere[i][j + 1]++;
						shipsHere[i][j + 2]++;
						shipsHere[i][j + 3]++;
						shipsHere[i][j + 4]++;
					}
				}
			}
		}
		else if( this.shipsAlive[3] ){
			for( int i = 0; i < 7; i++){
				for( int j = 0; j < 10; j++){
					if( !this.myMatchShots[i][j] && !this.myMatchShots[i + 1][j] && !this.myMatchShots[i + 2][j] && !this.myMatchShots[i + 3][j] ){
						shipsHere[i][j]++;
						shipsHere[i + 1][j]++;
						shipsHere[i + 2][j]++;
						shipsHere[i + 3][j]++;
					}
				}
			}
			for( int j = 0; j < 7; j++){
				for( int i = 0; i < 10; i++){
					if( !this.myMatchShots[i][j] && !this.myMatchShots[i][j + 1] && !this.myMatchShots[i][j + 2] && !this.myMatchShots[i][j + 3] ){
						shipsHere[i][j]++;
						shipsHere[i][j + 1]++;
						shipsHere[i][j + 2]++;
						shipsHere[i][j + 3]++;
					}
				}
			}
			
		}

		else{
			//System.out.println("last ship");
			for( int i = 0; i < 9; i++){
				for( int j = 0; j < 10; j++){
					if( !this.myMatchShots[i][j] && !this.myMatchShots[i+1][j] ){
						shipsHere[i][j]++;
						shipsHere[i+1][j]++;
					}
				}
			}
			for( int j = 0; j < 9; j++){
				for( int i = 0; i < 10; i++){
					if( !this.myMatchShots[i][j] && !this.myMatchShots[i][j + 1] ){
						shipsHere[i][j]++;
						shipsHere[i][j + 1]++;
					}
				}
			}
		}			
	}

	@Override
    public void resultOfAttack(int result) {
        if( result == MISS || result == DEFEATED ){
        	
        	
    		for( ArrayList<Coordinate> ship : this.hitShips ){
        		if( ship.size() == 2 ){        			
        			makeFire( ship );
        		}
        		else{
        			continueFire( ship );
        		}
        	}
        }
        else{
        	
        	int shipMod = result % 10;
        	
        	if( result >= 20){
	        	for( int h = 0; h < this.hitShips.size(); h++){ //ArrayList<Coordinate> ship : this.hitShips ){
	        		if( hitShips.get(h).get(0).getX() == shipMod ){
	        			hitShips.get(h).add(this.lastShot);
	        			this.sunkShips.add(this.hitShips.remove(h));
	        			this.shipsAlive[shipMod] = false;
	        		}
	        	}
	        	for( ArrayList<Coordinate> ship : this.hitShips ){
	        		if( ship.size() == 2 ){        			
	        			makeFire( ship );
	        		}
	        		else{
	        			continueFire( ship );
	        		}
	        	}
        	}
        	else{
        		boolean hitBefore = false;
        		for( ArrayList<Coordinate> ship : this.hitShips ){
	        		if( ship.get(0).getX() == shipMod ){
	        			hitBefore = true;
	        			ship.add(this.lastShot);
	        			continueFire( ship );
	        			break;
	        		}
	        	}
        		if( !hitBefore ){
        			ArrayList<Coordinate> newShip = new ArrayList<Coordinate>();
        			newShip.add( new Coordinate( shipMod, 0 ) );
        			newShip.add( this.lastShot );
        			this.hitShips.add(newShip);

        			makeFire( newShip );
        		}
        	}
        }
    }

    private void makeFire(ArrayList<Coordinate> newShip) {
		//System.out.println("adding to the list");
    	int x = newShip.get(1).getX();
    	int y = newShip.get(1).getY();
    	int spacesNeeded = this.shipLength[newShip.get(0).getX()];
    	int spacesH = 1;
    	int spacesV = 1;
    	
    	for( int i = x + 1; i < 10; i++){
    		if(this.myMatchShots[i][y] || spacesH >= spacesNeeded){
    			//System.out.println("here no x :"+this.myMatchShots[i][y]+", "+  (spacesH >= spacesNeeded));
    			break;
    		}
    		spacesH++;
    	}
    	for( int i = x - 1; i >= 0; i--){
    		if(this.myMatchShots[i][y] || spacesH >= spacesNeeded){
    			break;
    		}
    		spacesH++;
    	}
    	if( spacesH >= spacesNeeded ){
    		if( x != 9 && !this.myMatchShots[x + 1][y] ){
    			this.followShots.add( new Coordinate(x+1, y) );
    			//System.out.println(" Added1: "+this.followShots.get(this.followShots.size()-1));
    		}
    		if( x != 0 && !this.myMatchShots[x - 1][y] ){
    			this.followShots.add( new Coordinate(x-1, y) );
    			//System.out.println(" Added2: "+this.followShots.get(this.followShots.size()-1));
    		}
    	}
    	for( int j = y + 1; j < 10; j++){
    		if(this.myMatchShots[x][j] || spacesV >= spacesNeeded){
    			break;
    		}
    		spacesV++;
    	}
    	for( int j = y - 1; j >= 0; j--){
    		if(this.myMatchShots[x][j] || spacesV >= spacesNeeded){
    			break;
    		}
    		spacesV++;
    	}
    	if( spacesV >= spacesNeeded ){
    		if( y != 9 && !this.myMatchShots[x][y + 1] ){
    			this.followShots.add( new Coordinate(x, y + 1) );
    			//System.out.println(" Added3: "+this.followShots.get(this.followShots.size()-1));
    		}
    		if( y != 0 && !this.myMatchShots[x][y - 1] ){
    			this.followShots.add( new Coordinate(x, y - 1) );
    			//System.out.println(" Added4: "+this.followShots.get(this.followShots.size()-1));
    		}
    	}
		
	}

	private void continueFire(ArrayList<Coordinate> ship) {
		Coordinate[] hits = new Coordinate[ ship.size() - 1];
		for( int i = 1; i < hits.length + 1; i++){
			hits[i-1] = ship.get(i);
		}		
		if( hits[0].getX() == hits[1].getX() ){
			//order by y coordinates
			int[] ys  = new int[hits.length];
			for( int g = 0; g < ys.length; g++ ){
				ys[g] = hits[g].getY();
			}
			Arrays.sort(ys);
			Coordinate temp = new Coordinate(0 , 0);
			int moveInd = 0;
			for( int sort = 0; sort < hits.length-1; sort++){
				if( ys[sort] != hits[sort].getY() ){
					for( int sort2 = sort; sort2 < hits.length; sort2++){
						if( hits[sort2].getY() == ys[sort] ){
							temp = hits[sort2];
							moveInd = sort2;
							break;
						}
					}
					hits[moveInd] = hits[sort];
					hits[sort] = temp;
				}
			}
			for( int j = 1; j < hits.length; j++){
				if( hits[j].getY() != hits[j-1].getY() + 1){
					this.followShots.add(0, new Coordinate( hits[0].getX(), hits[j-1].getY() + 1  ) );
					return;
				}
			}
			if( hits[0].getY() > 0 && !this.myMatchShots[hits[0].getX()][hits[0].getY() - 1]){
				this.followShots.add( 0, new Coordinate( hits[0].getX(), hits[0].getY() - 1 ) );
			}
			if( hits[hits.length-1].getY() < 9 && !this.myMatchShots[hits[hits.length-1].getX()][hits[hits.length-1].getY() + 1]){
				this.followShots.add( 0, new Coordinate( hits[hits.length-1].getX(), hits[hits.length-1].getY() + 1 ) );
			}
		}
		else{
			//order by x coordinates
			int[] xs  = new int[hits.length];
			for( int g = 0; g < xs.length; g++ ){
				xs[g] = hits[g].getX();
			}
			Arrays.sort(xs);
			Coordinate temp = new Coordinate(0 , 0);
			int moveInd = 0;
			for( int sort = 0; sort < hits.length-1; sort++){
				if( xs[sort] != hits[sort].getX() ){
					for( int sort2 = sort; sort2 < hits.length; sort2++){
						if( hits[sort2].getX() == xs[sort] ){
							temp = hits[sort2];
							moveInd = sort2;
							break;
						}
					}
					hits[moveInd] = hits[sort];
					hits[sort] = temp;
				}
			}
			for( int j = 1; j < hits.length; j++){
				if( hits[j].getX() != hits[j-1].getX() + 1){
					this.followShots.add(0, new Coordinate(  hits[j-1].getX() + 1, hits[0].getY()  ) );
					return;
				}
			}
			if( hits[0].getX() > 0 && !this.myMatchShots[hits[0].getX() - 1][hits[0].getY()]){
				this.followShots.add(0,  new Coordinate( hits[0].getX() - 1, hits[0].getY() ) );
			}
			if( hits[hits.length-1].getX() < 9 && !this.myMatchShots[hits[hits.length-1].getX() + 1][hits[hits.length-1].getY() ]){
				this.followShots.add( 0, new Coordinate( hits[hits.length-1].getX() + 1, hits[hits.length-1].getY() ) );
			}
		}
		
	}

	@Override
    public void opponentAttack(Coordinate coord) {
       this.theirLastShot = coord;
    }

    @Override
    public void resultOfGame(int result) {
    	this.theirShips = new ArrayList<int[]>();
    	for( ArrayList<Coordinate> ship : this.sunkShips){
    		int[] giveShip = new int[4];
    		giveShip[0] = ship.get(0).getX();
    		if(ship.get(1).getX() == ship.get(2).getX()){
    			giveShip[3] = 1;
    			int least = 9;
    			for( int i = 1; i < ship.size(); i++){
    				if( ship.get(i).getY() < least){
    					least = ship.get(i).getY();
    				}
    			}
    			giveShip[1] = ship.get(1).getX();
    			giveShip[2] = least;
    		}
    		else{
    			giveShip[3] = 0;
    			int least = 9;
    			for( int i = 1; i < ship.size(); i++){
    				if( ship.get(i).getX() < least){
    					least = ship.get(i).getX();
    				}
    			}
    			giveShip[2] = ship.get(1).getY();
    			giveShip[1] = least;
    		}
    		
    		//System.out.println(giveShip);
    		this.theirShips.add(giveShip);
    	}
    }
    
    private void printArray(boolean[][] ray) {
    	System.out.println("--------------------------------------------------------------------------------------");
    	for( int i = 0; i < 10; i++){
    		System.out.println();
    		for( int j = 0; j < 10; j++ ){
    			System.out.print(ray[j][i]+", ");
    		}
    	}
    	System.out.println();
    	System.out.println("--------------------------------------------------------------------------------------");
    }

    private void printArray(double[][] ray) {
    	System.out.println("--------------------------------------------------------------------------------------");
    	for( int i = 0; i < 10; i++){
    		System.out.println();
    		for( int j = 0; j < 10; j++ ){
    			System.out.print(ray[j][i]+", ");
    		}
    	}
    	System.out.println();
    	System.out.println("--------------------------------------------------------------------------------------");
		
	}
}
