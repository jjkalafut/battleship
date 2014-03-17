
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class CaptainLana implements Captain, Constants {

    //splash pattern
	//figureships()
	
    protected Random generator;

    protected Fleet myFleet;
    private int[] shipLength = {2,3,3,4,5};
    private boolean[] shipsAlive;
    private boolean[][] myMatchShots;
    private String lastOpponent = "";
    private double[][][] hitsHeat;
    private int[][][] myHits;
    private int[][]	 myShots;
    private int[][]  myShipPlaces;
    private Coordinate lastShot;
    private ArrayList<ArrayList<Coordinate>> hitShips;
    private ArrayList<ArrayList<Placement>> theirPlacements;
    private ArrayList<ArrayList<Placement>> myPlacements;
    private Coordinate[] lastFleet;
    private int[] lastDirecs;
    private boolean wasWin;

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
    	
    	this.theirPlacements = new ArrayList<ArrayList<Placement>>();
    	this.myPlacements = new ArrayList<ArrayList<Placement>>();
    	//new random
        this.generator = new Random();
        //new fleet
        this.myFleet = new Fleet();
        //true if ship alive
        this.shipsAlive = new boolean[]{true, true, true, true, true};
        //true if i have shot there
        this.myMatchShots = new boolean[10][10];
        //xcoord of first coord is ship modifier
        this.hitShips = new ArrayList<ArrayList<Coordinate>>();
        
        for( int i = 0; i < 10; i++){
        	Arrays.fill(this.myMatchShots[i], false);
        }
        
        if( !opponent.equals(this.lastOpponent) ){
        	this.wasWin = false;
        	this.lastFleet = new Coordinate[5];
        	this.lastDirecs = new int[5];
        	this.myHits = new int[10][10][5];
            this.myShots = new int[10][10];
            this.myShipPlaces = new int[10][10];
        	this.lastOpponent = opponent;
        	this.hitsHeat = new double[10][10][5];     
        	for( int j = 0; j < 100; j++){
        		this.myShots[j%10][j/10] = 2;
        		for( int k = 0; k < 5; k++){
        			this.myHits[j%10][j/10][k] = 1;        			
        		}
        	}
        }
        for( int s = 0; s < 5; s++){
	        for( int i = 0; i < 100; i++){
	        	this.hitsHeat[i%10][i/10][s] = (double)this.myHits[i%10][i/10][s]/(double)this.myShots[i%10][i/10];
	        }
        }
        createPlacements( this.myShipPlaces, this.myPlacements);
        createPlacements( this.hitsHeat, this.theirPlacements);
        distPlace(myFleet);       
    }

	private void distPlace(Fleet flt) {
		if(this.wasWin){
			for( int i = 0; i < 5; i++){
				flt.placeShip(this.lastFleet[i], this.lastDirecs[i], i);
			}		
		}		
		else{		
			for( int j = 0; j < 5; j++){
				ArrayList<Placement> places = this.myPlacements.get(j);
				int score = (int) places.get(0).score;
				Coordinate c = places.get(0).coords[0];
				int direc = places.get(0).direc;
				Coordinate[] badCoords = places.get(0).coords;
				
				for( Placement p : places){
					if( p.score < score ){
						score = (int) p.score;
						c = p.coords[0];
						direc = p.direc;
						badCoords = p.coords;
					}
				}
				
				flt.placeShip(c, direc, j);
				this.lastFleet[j] = c;
				this.lastDirecs[j] = direc;				
				for( int k = j + 1; k < 5; k++){
					ArrayList<Placement> bads = new ArrayList<Placement>();
					for( Coordinate bad_c : badCoords){
						for( Placement p : this.myPlacements.get(k)){						
							if( p.contains(bad_c)){
								bads.add(p);
							}
						}
					}
					this.myPlacements.get(k).removeAll(bads);					
				}				
			}
		}
		for( int i = 0; i < 5; i++){
			int shipLen = this.shipLength[i];
			if(this.lastDirecs[i] == 0){
				for( int j = this.lastFleet[i].getX(); j < shipLen + this.lastFleet[i].getX(); j++ ){
					this.myShipPlaces[j][this.lastFleet[i].getY()]++;
				}
			}
			else{
				for( int j = this.lastFleet[i].getY(); j < shipLen + this.lastFleet[i].getY(); j++ ){
					this.myShipPlaces[this.lastFleet[i].getX()][j]++;
				}
			}
			
		}
				
	}

	private void createPlacements(int[][] scorer, ArrayList<ArrayList<Placement>> list) {
		for( int s = 0; s < 5; s++){
			int shipLen = this.shipLength[s];
			ArrayList<Placement> places = new ArrayList<Placement>();
			for(int i = 0; i < 11-shipLen; i++){
				for( int j = 0; j < 10; j++){
					Placement p = new Placement(0, s, new Coordinate(i,j));
					double score = 0;
					for( int k = 0; k < shipLen; k++){
						score += scorer[i + k][j];
					}
					p.score = score;
					places.add(p);
				}
			}
			for( int j = 0; j < 11 - shipLen; j++){
				for( int i = 0; i < 10; i++){
					Placement p = new Placement(1, s, new Coordinate(i,j));
					double score = 0;
					for( int k = 0; k < shipLen; k++){
						score += scorer[i][j + k];
					}
					p.score = score;
					places.add(p);
				}
			}
			list.add(places);
		}		
	}
    private void createPlacements(double[][][] scorer, ArrayList<ArrayList<Placement>> list) {
		for( int s = 0; s < 5; s++){
			int shipLen = this.shipLength[s];
			ArrayList<Placement> places = new ArrayList<Placement>();
			for(int i = 0; i < 11-shipLen; i++){
				for( int j = 0; j < 10; j++){
					Placement p = new Placement(0, s, new Coordinate(i,j));					
					double score = 0;
					
					for( int k = 0; k < shipLen; k++){
						score += scorer[i + k][j][s];
					}
					p.score = score;					 
					places.add(p);
				}
			}
			for( int j = 0; j < 11 - shipLen; j++){
				for( int i = 0; i < 10; i++){
					Placement p = new Placement(1, s, new Coordinate(i,j));					
					double score = 0;
					
					for( int k = 0; k < shipLen; k++){
						score += scorer[i][j + k][s];
					}
					p.score = score;					 
					places.add(p);
				}
			}
			list.add(places);
		}		
	}

    @Override
    public Fleet getFleet() {
        return myFleet;
    }
    private boolean isAdjacent(Coordinate c1, Coordinate c2){
    	if(c1.getX()==c2.getX()){
    		if(c1.getY() == 1+c2.getY()){
    			return true;
    		}
    		if(c1.getY()+1 == c2.getY()){
    			return true;
    		}
    	}
    	if(c1.getY()==c2.getY()){
    		if(c1.getX() == 1+c2.getX()){
    			return true;
    		}
    		if(c1.getX()+1 == c2.getX()){
    			return true;
    		}
    	}
    	return false;
    }
    @Override
    public Coordinate makeAttack() {
    	
    	Coordinate shot = new Coordinate(0,0); 
    	if( !this.hitShips.isEmpty() ){
    		int shipMod = this.hitShips.get(0).get(0).getX();
    		ArrayList<Placement> places = this.theirPlacements.get(shipMod);
			double best = -1;
			int ind = 0;
			int best_ind = 0;
	    	for( Placement p : places ){
	    		if( p.score > best ){
	    			best = p.score;
	    			best_ind = ind;
	    		}
	    		ind++;
	    	}
	    	ArrayList<Coordinate> hits = new ArrayList<Coordinate>();	    	
	    	hits.addAll( this.hitShips.get(0));
	    	hits.remove(0);
	    	
	    	for( Coordinate c : places.get(best_ind).coords ){
	    		if(!this.myMatchShots[c.getX()][c.getY()]){
	    			for( Coordinate c2 : hits){
	    				if(isAdjacent(c,c2)){
		    				shot = c;				    			
			    			break;
	    				}	
		    		}	
	    		}
	    	}			    	
    	}
    	else{    		
			double[][] turnHeat = new double[10][10];
	    	for( int s = 4; s >= 0; s--){
	    		if( this.shipsAlive[s]){
	    			ArrayList<Placement> places = this.theirPlacements.get(s);
			    	for( Placement p : places ){
			    		for(Coordinate c : p.coords ){
			    			turnHeat[c.getX()][c.getY()] += this.hitsHeat[c.getX()][c.getY()][s];
			    		}
			    	}				    	
	    		}	    		
	    	}
	    	int x = 0;
	    	int y = 0;
	    	double best = 0;
	    	for( int i = 0; i < 100; i++){	    		
	    		if( !this.myMatchShots[i%10][i/10] && turnHeat[i%10][i/10] >= best){
	    			best = turnHeat[i%10][i/10];
	    			x = i%10;
	    			y = i/10;
	    		}
	    	}	    	
	    	shot = new Coordinate(x,y);
    	}    	  	
    	
    	if( this.myMatchShots[shot.getX()][shot.getY()]){
    		System.out.println(shot);
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

	@Override
    public void resultOfAttack(int result) {
		this.myShots[this.lastShot.getX()][this.lastShot.getY()]++;
		
        if( result == MISS || result == DEFEATED ){        	
        	for( int s = 0; s < 5; s++){
        		if( this.shipsAlive[s]){
        			ArrayList<Placement> places = this.theirPlacements.get(s);
        			ArrayList<Placement> bads = new ArrayList<Placement>();
        			for(Placement p : places ){
        				if( p.contains(this.lastShot) ){
        					bads.add(p);
        				}
        			}
        			places.removeAll(bads);
        		}
        	}
    		
        }
        else{
        	int shipMod = result % 10;
        	this.myHits[this.lastShot.getX()][this.lastShot.getY()][shipMod] += 2;
        	if( this.lastShot.getX() > 0 ){
            	this.myHits[this.lastShot.getX() -1 ][this.lastShot.getY()][result % 10]++;
            }
            if( this.lastShot.getX() < 9){
            	this.myHits[this.lastShot.getX() +1 ][this.lastShot.getY()][result % 10]++;
            }
            if( this.lastShot.getY() > 0 ){
            	this.myHits[this.lastShot.getX()  ][this.lastShot.getY()-1][result % 10]++;
            }
            if( this.lastShot.getY() < 9){
            	this.myHits[this.lastShot.getX()  ][this.lastShot.getY()+1][result % 10]++;
            }
        	
        	if( result >= 20){
	     
	        	this.shipsAlive[shipMod] = false;
	        	ArrayList<Coordinate> bad_ship = new ArrayList<Coordinate>();
	        	for( ArrayList<Coordinate> ship : this.hitShips ){
	        		if(ship.get(0).getX() == shipMod ){
	        			bad_ship = ship;
	        			break;
	        		}
	        	}
	        	this.hitShips.remove(bad_ship);	        	
        	}
        	else{
        		boolean hitBefore = false;
        		for( ArrayList<Coordinate> ship : this.hitShips ){
	        		if( ship.get(0).getX() == shipMod ){
	        			hitBefore = true;
	        			ship.add(this.lastShot);
	        			break;
	        		}
	        	}
        		if( !hitBefore ){
        			ArrayList<Coordinate> newShip = new ArrayList<Coordinate>();
        			newShip.add( new Coordinate( shipMod, 0 ) );
        			newShip.add( this.lastShot );
        			this.hitShips.add(newShip);
        		}
        	}
        	getPossibles();
        	for( int s = 0; s < 5; s++){
        		if( this.shipsAlive[s] && s != shipMod){
        			ArrayList<Placement> places = this.theirPlacements.get(s);
        			ArrayList<Placement> bads = new ArrayList<Placement>();
        			for(Placement p : places ){
        				if( p.contains(this.lastShot) ){
        					bads.add(p);
        				}
        			}
        			places.removeAll(bads);
        		}
        	}
        }
    }

	private void getPossibles(){
		for( int s = 0; s < 5; s++){
			if( this.shipsAlive[s] ){
				ArrayList<Placement> places = this.theirPlacements.get(s);
				ArrayList<Placement> bads = new ArrayList<Placement>();
				for( ArrayList<Coordinate> list : this.hitShips ){
					if( list.get(0).getX() == s){
						for( int i = 1; i < list.size(); i++){
							for( Placement p : places){
								if( !p.contains(list.get(i))){
									bads.add(p);
								}
							}
						}
					}
				}
				places.removeAll(bads);
			}
		}
		
	}

	@Override
    public void opponentAttack(Coordinate coord) {

    }

    @Override
    public void resultOfGame(int result) {
    	this.wasWin = result == WON;
    }
    
}

class Placement{
	
	public Coordinate[] coords;
	public double score = 0;
    public int[] shipLength = {2,3,3,4,5};
    public int direc;
    public int type;
	
	Placement(int direc, int type, Coordinate loc){
		this.direc = direc;
		this.type = type;
		coords = new Coordinate[shipLength[type]];
		coords[0] = loc;
		if( direc == 0 ){
			for( int i = 1; i < this.shipLength[type]; i++){
				coords[i] = new Coordinate(i+loc.getX(),loc.getY());
			}
		}
		else{
			for( int i = 1; i < this.shipLength[type]; i++){
				coords[i] = new Coordinate(loc.getX(), i+loc.getY());
			}
		}
	}
	
	public boolean contains(int x, int y){
		if(direc == 0){
			if( y != coords[0].getY()){
				return false;
			}
			if( x >= coords[0].getX() && x <= coords[coords.length-1].getX()){
				return true;
			}
			return false;
		}
		else{
			if( x != coords[0].getX()){
				return false;
			}
			if( y >= coords[0].getY() && y <= coords[coords.length-1].getY()){
				return true;
			}
			return false;
		}
	}
	public boolean contains(Coordinate c){
		int x= c.getX();
		int y= c.getY();
		if(direc == 0){
			if( y != coords[0].getY()){
				return false;
			}
			if( x >= coords[0].getX() && x <= coords[coords.length-1].getX()){
				return true;
			}
			return false;
		}
		else{
			if( x != coords[0].getX()){
				return false;
			}
			if( y >= coords[0].getY() && y <= coords[coords.length-1].getY()){
				return true;
			}
			return false;
		}
	}
	public String toString(){
		return ""+coords[0]+", "+coords[1];
	}	
	
}
