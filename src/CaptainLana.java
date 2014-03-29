
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
    private int[][][]  myShipPlaces;
    private Coordinate lastShot;
    private ArrayList<ArrayList<Coordinate>> hitShips;
    private ArrayList<ArrayList<Placement>> theirPlacements;
    private ArrayList<ArrayList<Placement>> myPlacements;
    private Coordinate[] lastFleet;
    private int[] lastDirecs;
    private boolean wasWin;
    private int turnNumber;
    private int[] moving;

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
    	
    	this.theirPlacements = new ArrayList<ArrayList<Placement>>();
    	this.myPlacements = new ArrayList<ArrayList<Placement>>();
    	this.turnNumber = 0;
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
        	this.myHits = new int[10][10][5];
            this.myShots = new int[10][10];
        	this.lastOpponent = opponent;
        	this.hitsHeat = new double[10][10][5]; 
        	this.myShipPlaces = new int[10][10][5];
        	for( int j = 0; j < 100; j++){
        		this.myShots[j%10][j/10] = 2;
        		for( int k = 0; k < 5; k++){
        			this.myHits[j%10][j/10][k] = 1;            			
        		}
        	}
        	this.moving = new int[]{0,0,0,1,1};
    		this.lastDirecs= new int[]{ 1, 1, 1, 0, 0};
    		this.lastFleet= new Coordinate[]{ new Coordinate(9,8), new Coordinate(4,7), new Coordinate(0,7), new Coordinate(5,2), new Coordinate(0,4)};
        }
        for( int s = 0; s < 5; s++){
	        for( int i = 0; i < 100; i++){
	        	this.hitsHeat[i%10][i/10][s] = (double)this.myHits[i%10][i/10][s]/(double)this.myShots[i%10][i/10];
	        }
        }
        
        createPlacements( this.hitsHeat, this.theirPlacements);
        createPlacements( this.myShipPlaces, this.myPlacements);
        disPlace(myFleet);
        //interPlace(myFleet);       
    }

    private void interPlace(Fleet flt) {
		if( !this.wasWin){
			for(int i = 0; i < 5; i++){
				int shipLen = this.shipLength[i];
				if(this.moving[i] == 1){
					if(this.lastFleet[i].getY() == 9){
						if( this.lastFleet[i].getX() == 10-shipLen ){
							this.lastDirecs[i] = 1;
							this.moving[i] = 0; 
							this.lastFleet[i] = new Coordinate(0,0);
							i--;
							continue;
						}
						else{
							this.lastFleet[i] = new Coordinate(this.lastFleet[i].getX() + 1,0);
						}						
					}
					else{
						this.lastFleet[i] = new Coordinate(this.lastFleet[i].getX(),this.lastFleet[i].getY()+1);
					}
				}
				else{
					if(this.lastFleet[i].getX() == 9){
						if( this.lastFleet[i].getY() == 10-shipLen ){
							this.lastDirecs[i] = 0;
							this.moving[i] = 1; 
							this.lastFleet[i] = new Coordinate(0,0);
							i--;
							continue;
						}
						else{
							this.lastFleet[i] = new Coordinate(0,this.lastFleet[i].getY() + 1);
						}
					}
					else{
						this.lastFleet[i] = new Coordinate(this.lastFleet[i].getX()+1,this.lastFleet[i].getY());
					}
				}
				if(!flt.placeShip(this.lastFleet[i], this.lastDirecs[i], i)){
					i--;
				}
			}
		}
		else{
			for( int i = 0; i < 5; i++){
				flt.placeShip(this.lastFleet[i], this.lastDirecs[i], i);
			}
		}		
	}

    private void disPlace(Fleet flt){
    	if( !this.wasWin){
			for(int i = 0; i < 5; i++){
				int direc = this.myPlacements.get(i).get(0).direc;
				Coordinate pos = this.myPlacements.get(i).get(0).coords[0];
				double best = this.myPlacements.get(i).get(0).score;
				Coordinate[] coords = this.myPlacements.get(i).get(0).coords;
				for( Placement p : this.myPlacements.get(i)){
					if( p.score < best ){
						best = p.score;
						pos = p.coords[0];
						direc = p.direc;
						coords = p.coords;
					}
				}
				this.lastDirecs[i] = direc;
				this.lastFleet[i] = pos;
				flt.placeShip(pos, direc, i);
				for( Coordinate mark : coords){
					this.myShipPlaces[mark.getX()][mark.getY()][i]++;
				}
				for( int j = i; j < 5; j++){
					ArrayList<Placement> bad = new ArrayList<Placement>();
					for(Placement other : this.myPlacements.get(j)){
						for(Coordinate no_good : coords){
							if( other.contains(no_good)){
								bad.add(other);
							}
						}
					}
					this.myPlacements.get(j).removeAll(bad);
				}
			}
		}
		else{
			for( int i = 0; i < 5; i++){
				flt.placeShip(this.lastFleet[i], this.lastDirecs[i], i);
			}
		}
    }
	private void createPlacements(double[][] scorer, ArrayList<ArrayList<Placement>> list) {
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
    private void createPlacements(int[][][] scorer, ArrayList<ArrayList<Placement>> list) {
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
    @Override
    public Coordinate makeAttack() {
    	this.turnNumber++;
    	if( this.turnNumber < 0){
    		this.lastShot = makeMinimizerShot();
    		mark(this.myMatchShots, this.lastShot);
    	    return this.lastShot;
    	}    	
    	Coordinate shot = new Coordinate(0,0); 
    	ArrayList<Coordinate> possibles = new ArrayList<Coordinate>();
    	if( !this.hitShips.isEmpty() ){
    		for ( ArrayList<Coordinate> ship : this.hitShips ){
	    		int shipMod = ship.get(0).getX();
	    		ArrayList<Placement> places = this.theirPlacements.get(shipMod);
		    	for( Placement p : places ){
		    		for(Coordinate rd : p.coords){
		    			if(!this.myMatchShots[rd.getX()][rd.getY()]){
		    				possibles.add(rd);
		    			}
		    		}
		    	}	
    		}
    	}    	
    	
    	
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
    	if( possibles.isEmpty() ){
			for( int i = 0; i < 100; i++){	    		
				if( !this.myMatchShots[i%10][i/10] && turnHeat[i%10][i/10] >= best){
					best = turnHeat[i%10][i/10];
					x = i%10;
					y = i/10;
				}
			}	  
    	}
    	else{
    		for( Coordinate c: possibles){
    			if( !this.myMatchShots[c.getX()][c.getY()] && turnHeat[c.getX()][c.getY()] >= best){
					best = turnHeat[c.getX()][c.getY()];
					x = c.getX();
					y = c.getY();
				}
    		}
    	}
    	shot = new Coordinate(x,y);
	  	  	
	
	if( this.myMatchShots[shot.getX()][shot.getY()]){
		System.out.println(shot);
	}
	this.lastShot = shot;
	mark(this.myMatchShots, shot);
    return shot;
    
	}

	private Coordinate makeMinimizerShot() {
		switch(this.turnNumber){
			case 1:  
				int least = this.myHits[0][0][0];
				Coordinate c = new Coordinate(0,0);
				for( int i = 0; i < 100; i++){
					if( !this.myMatchShots[i%10][i/10] ){
						for( int j = 0; j < 5; j++){
							if( this.myHits[i%10][i/10][j] < least){
								c = new Coordinate( i%10, i/10);
								least = this.myHits[i%10][i/10][j];
							}
						}
					}
				}
				return c;
			
			case 2:
				int best = this.myHits[0][0][0];
				Coordinate g = new Coordinate(0,0);
				for( int i = 0; i < 100; i++){
					if( !this.myMatchShots[i%10][i/10] ){
						for( int j = 0; j < 5; j++){
							if( this.myHits[i%10][i/10][j] > best){
								g = new Coordinate( i%10, i/10);
								best = this.myHits[i%10][i/10][j];
							}
						}
					}
				}
				return g;
			
			case 3:
				ArrayList<Placement> places = this.theirPlacements.get(0);
				double score = places.get(0).score;
				Coordinate b = places.get(0).coords[0];
				for( Placement p : places){
					if( p.score > score){
						score = p.score;
						b = p.coords[0];
					}
				}
				return b;
			
			case 4:
				int least_shot = this.myShots[0][0];
				Coordinate s = new Coordinate(0,0);
				for( int i = 0; i < 100; i++){
					if( !this.myMatchShots[i%10][i/10] && this.myShots[i%10][i/10] < least_shot){
						s = new Coordinate( i%10, i/10);
						least_shot = this.myShots[i%10][i/10];
					}				
				}
				return s;
		}
		System.out.println("badness occured");
		return null;
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
	        		        	
        	}
        	else{
        		boolean hit = false;
        		for( ArrayList<Coordinate> c : this.hitShips){
        			if( c.get(0).getX() == shipMod ){
        				hit = true;
        			}
        			if(!hit){
        				ArrayList<Coordinate> temp = new ArrayList<Coordinate>();
        				temp.add(new Coordinate(shipMod,0));
        				this.hitShips.add(temp);
        			}
        		}
        	}
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
        		else if( this.shipsAlive[s] && s == shipMod ){
        			ArrayList<Placement> places = this.theirPlacements.get(s);
        			ArrayList<Placement> bads = new ArrayList<Placement>();
        			for(Placement p : places ){
        				if( !p.contains(this.lastShot) ){
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

    private class Placement{
	
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

}
