import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class CaptainNoTime implements Captain {

	private 									Random rgen;
	protected final int[] 						shipLength = {2,3,3,4,5};
	protected int[][] 							their_hits;
	protected int[][] 							their_misses;
	protected int								their_hit_shots;
	protected int								their_miss_shots;
	private boolean[]							shipWasHit;
    protected Fleet 							myFleet;
    private boolean[][] 						myMatchShots;
    private String 								lastOpponent = "";
    private boolean 							wasWin;
    private int 								turnNum;
    private SuperMegaShipPlacer_3000 			sp;
    private Random								rGen;
    private int									match_num;
    private final int							atk_strats = 3;

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {

    	this.rGen = new Random();
        this.rgen = new Random();
        this.myFleet = new Fleet();
        this.myMatchShots = new boolean[10][10];
        this.shipWasHit = new boolean[]{ false, false, false, false, false };


        for (int i = 0; i < 10; i++) {
            Arrays.fill(this.myMatchShots[i], false);
        }

        if (!opponent.equals(this.lastOpponent)) {
        	       	
        	this.turnNum = 50;
            this.wasWin = false;
            this.their_hits = new int[10][10];
            this.their_misses = new int [10][10];
            this.their_hit_shots = 100;
            this.their_miss_shots = 100;
            this.lastOpponent = opponent;
            for (int j = 0; j < 100; j++) {
            this.their_hits[j % 10][j / 10] = rGen.nextInt(50);
            this.their_misses[j % 10][j / 10] = rGen.nextInt(50);
            }
	       
            sp = new SuperMegaShipPlacer_3000();
        }
        else{
        	this.match_num++;
        }
        
        this.myFleet = sp.getNextPlacement(this.wasWin, this.turnNum, this.shipWasHit);
        //this.myFleet = getDistShipPlace();
        this.turnNum = 0;
        
    }

	   
    public int getIndex(ArrayList<Placement> ray, int score, int p, int q){
		if((q-p) < 2){
			return p;
		}
		else{
			int test = ( p + q ) / 2;
			if( ray.get(test).score < score){
				return getIndex( ray, score,p,test);
			}
			else{
				return getIndex( ray, score,test,q);
			} 
		}
	}
    
    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    @Override
    public Coordinate makeAttack() {
        this.turnNum++;
        Coordinate shot = new Coordinate(0, 0);
        return shot;
        
    }

    @Override
    public void resultOfAttack(int result) {
    }

    @Override
    public void opponentAttack(Coordinate coord) {
    	if( myFleet.getLastAttackValue() != MISS ){
    		shipWasHit[ myFleet.getLastAttackValue() % 10 ] = true;
    	}
    	if( this.myFleet.isShipAt(coord)){
    		this.their_hits[coord.getX()][coord.getY()]++;
    		this.their_hit_shots++;
    	}
    	else{
    		this.their_misses[coord.getX()][coord.getY()]++;
    		this.their_miss_shots++;
    	}
    }

    @Override
    public void resultOfGame(int result) {
        this.wasWin = result == WON;
    }

    private class Placement {

        public Coordinate[] coords;
        public double score = 0;
        public int[] shipLength = {2, 3, 3, 4, 5};
        public int direc;

        Placement(int direc, int type, Coordinate loc) {
            this.direc = direc;
            coords = new Coordinate[shipLength[type]];
            coords[0] = loc;
            if (direc == 0) {
                for (int i = 1; i < this.shipLength[type]; i++) {
                    coords[i] = new Coordinate(i + loc.getX(), loc.getY());
                }
            } else {
                for (int i = 1; i < this.shipLength[type]; i++) {
                    coords[i] = new Coordinate(loc.getX(), i + loc.getY());
                }
            }
        }

        public boolean contains(Coordinate c) {
            int x = c.getX();
            int y = c.getY();
            if (direc == 0) {
                if (y != coords[0].getY()) {
                    return false;
                }
                if (x >= coords[0].getX() && x <= coords[coords.length - 1].getX()) {
                    return true;
                }
                return false;
            } else {
                if (x != coords[0].getX()) {
                    return false;
                }
                if (y >= coords[0].getY() && y <= coords[coords.length - 1].getY()) {
                    return true;
                }
                return false;
            }
        }

        public String toString() {
            return "" + coords[0] + ", " + coords[1];
        }
    }


	public String printArray(float[] r){
		String ret = "";
		for( float f : r){
			ret = ret.concat(f+", ");
		}
		ret.substring(0, ret.length() -2);
		return ret;
	}
	public String printArray(boolean[] b){
		String ret = "";
		for( boolean f : b){
			ret = ret.concat(f+", ");
		}
		ret.substring(0, ret.length() -2);
		return ret;
	}
	public String printArray(boolean[][] b, int id_1){
		String ret = "";
		boolean[][] b2 = new boolean[10][10];
		for( int l = 0; l < 100; l++){
			b2[l/10][l%10] = b[l%10][l/10];
		}
		for( int i = 0; i < id_1; i++){
			ret = ret.concat(printArray(b2[i])).concat("\n");
		}
		return ret;
	}
	private class SuperMegaShipPlacer_3000{
		
		//the number of placement "bins"-Seth Dutter I have
		private int 						numberBins;
		//the number of turns before I reselect which bin to use
		private int							matchRedist;
		//matches won in a row
		private int							consec_wins;
		//match number
		private int							numberMatch;
		//placement bins
		private ArrayList<PlacementBin>		bins;
		//their scores
		private ArrayList<Integer> 			scores;
		//last fleet			
		private ShipMap						last;
		//scoring per consec wins/turns
		private int							total_turns;
		private int							over_games;
		private int							numberToGo;
		private boolean						init;
		
		
		public SuperMegaShipPlacer_3000(){
			this.bins = new ArrayList<PlacementBin>();
			this.scores = new ArrayList<Integer>();
			this.numberBins = 2;
			this.matchRedist = 1000;
			this.numberMatch = 0;
			this.numberToGo = 0;
			this.init = true;
			
			//create 2 new bins with 10 random maps each
			//this should create a two bins to determine a good metric (well start determining anyway)
			float[] rmeterics = new float[]{.3f,.7f,1f,.5f,.3f,.2f};
			PlacementBin b1 = new PlacementBin(500,rmeterics);
			this.bins.add(b1);
			this.scores.add(50);
			
			rmeterics = new float[]{.3f,.3f,.2f,.5f,.8f,.7f};
			
			PlacementBin b2 = new PlacementBin(500,rmeterics);
			this.bins.add(b2);
			this.scores.add(49);
		}
		public Fleet getNextPlacement(boolean won, int turns_taken, boolean[] wasHit){
			this.numberMatch++;
			Fleet ret = new Fleet();		
			ShipMap place;
			
			this.total_turns += turns_taken;
			if( won){
				//replay same ship pattern	
				this.consec_wins++;
				this.over_games++;
				int ind = -1;
				for( int i = 0; i < 5; i++){
					if(!wasHit[i]){
						ret.placeShip(this.last.ships[i].c, this.last.ships[i].direction, i);
						ind = i;
						break;
					}
				}
				for( int j = 0; j < 5; j++){
					if(j != ind){
						while(!ret.placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), j)){
						}
					}
				}				
			}
			else{
				this.bins.get(0).rankPlace(turns_taken - this.consec_wins);
				this.over_games += 2;		
				place = this.bins.get(0).getShipMap();
				this.last = place;
				if( this.numberMatch >= matchRedist ){
					//System.out.println("Step 2");
					reThink();
				}
				else if( turns_taken < 35 ){
					int new_score  = this.total_turns / this.over_games;
					int new_ind = getInd(new_score,0,this.scores.size());
					int old_score = this.scores.remove(0);
					this.scores.add(new_ind,new_score);
		    		this.bins.add(new_ind, this.bins.remove(0));
				}
				this.consec_wins = 0;				

				for( int i = 0; i < 5; i++){
					ret.placeShip(place.ships[i].c, place.ships[i].direction, i);
				}
			}
			return ret;					
		}

		private void reThink(){

			//System.out.println("Top Scores are: "+this.scores.get(0)+" and "+this.scores.get(1));
			int new_score  = this.total_turns / this.over_games;
			//changing bins, see if another bin is better that the current
			int new_ind = getInd(new_score,0,this.scores.size());
			int old_score = this.scores.remove(0);
			this.scores.add(new_ind,new_score);
    		this.bins.add(new_ind, this.bins.remove(0));
			this.numberToGo = this.total_turns / 20;
			int amount = 25 - rGen.nextInt( 10 );
			
			setBins( amount );
			setBins( 25 );
			this.matchRedist = this.numberMatch + 20 * numberToGo;
			
			this.init = false;
			
			this.over_games = 0;
			this.total_turns = 0;
		}
		
		private void setBins( int i ){			
			while( i > this.numberBins ){
				int bin_size = this.numberToGo;
				this.bins.get(0).setSize(bin_size);
				//create new meterics different from others;
				float[] newMets  = new float[6];
				
				//TODO create these meterics based off of good ones
				newMets[0] = rgen.nextFloat();
				newMets[1] = rgen.nextFloat();
				newMets[2] = rgen.nextFloat();	
				newMets[3] = rgen.nextFloat();	
				newMets[4] = (this.bins.get(0).target_meterics[4] + (rGen.nextBoolean() ? .2f : 0f) ) % 1f;
				newMets[5] = (this.bins.get(0).target_meterics[5] + .2f) % 1f;			
				
				PlacementBin b = new PlacementBin(bin_size, newMets );
				int score;
				if(this.init){
					score = 40;
				}
				else{
					score = 1+ this.scores.get(0);
				}

				this.bins.add(0, b);
				this.scores.add(0, score);		
				this.numberBins++;
			}
			while( i < this.numberBins){
				int bin_size = this.numberToGo/2;
				this.bins.get(0).setSize(bin_size);
				this.bins.remove(this.bins.size() - 1);
				this.scores.remove(this.scores.size() - 1);
				this.numberBins--;
			}
		}
		public int getInd(int turns, int p, int q){
    		if((q-p) < 2){
    			return p;
    		}
    		else{
    			int test = ( p + q ) / 2;
    			if(this.scores.get(test) < turns){
    				return getInd(turns,p,test);
    			}
    			else{
    				return getInd(turns,test,q);
    			}
    		}
    	}
		
		
	}
	//ONLY WORRY ABOUT SHIP MAPS
	private class PlacementBin{
		//TODO rank placements in bins????? 
		public 	float[] 				target_meterics = new float[6];
		private ArrayList<ShipMap> 		ships;
		private ArrayList<Integer>		pscores;
		
		public PlacementBin(int size, float[] meterics){
			this.ships = new ArrayList<ShipMap>();
			this.pscores = new ArrayList<Integer>();
			this.target_meterics = meterics;
			setSize(size);
		}
		public void rankPlace(int turns){
			int ind = getPlaceInd(turns,0,this.pscores.size());
			this.pscores.add(ind,this.pscores.remove(0));
			this.ships.add(ind,this.ships.remove(0));
		}
		public int getPlaceInd(int turns, int p, int q){
    		if((q-p) < 2){
    			return p;
    		}
    		else{
    			int test = ( p + q ) / 2;
    			if(this.pscores.get(test) < turns){
    				return getPlaceInd(turns,p,test);
    			}
    			else{
    				return getPlaceInd(turns,test,q);
    			}
    		}
    	}
		public ShipMap getShipMap(){
			return ships.get(0);
		}
		
		public void setSize(int i){
			while( ships.size() < i){
				generateNewXY();
			}
			while( i < ships.size() ){
				ships.remove(ships.size() - 1);
				pscores.remove(this.pscores.size()-1);
			}
		}
		private void generateNewXY(){ 
			
			ShipMap sm1 = new ShipMap();
			// go through ships and try to place them as best as possible. start small?
			boolean[][] valid = new boolean[10][10];
			for( int g = 0; g < 10; g++){
				Arrays.fill(valid[g], true);
			}
			
			for( int i = 0; i < 5; i++){
				int len = shipLength[i];
				if( rGen.nextInt(25) < 9 ){
					// ~32% chance of random place
					sm1.place( i, valid );
					if( sm1.ships[i].direction == 1){
						for( int k = 0 ; k < len; k++){
							valid[sm1.ships[i].c.getX()][sm1.ships[i].c.getY()+k] = false;
						}
					}
					else{
						for( int k = 0 ; k < len; k++){
							valid[sm1.ships[i].c.getX()+k][sm1.ships[i].c.getY()] = false;
						}
					}
					continue;
				}				
				float half = (5*this.target_meterics[2]);
				float horizon =  (half + rgen.nextInt((int)half+1));// get a random number based off ship hor/verticle 
				if( horizon < 3.75 ){
					//do horizontal since verticle = 10
					int cnt = 0;
					boolean placed = false;
					while(cnt < 20){
						//find a ship x place
						int posx = 1 - rGen.nextInt(3); // [-1,1]
						int posy = 2 - rGen.nextInt(5); // [-1,1]
						int x  = (int) ( posx + ( ((10 * target_meterics[4]) - len/2) ) );
						//check if over edge
						if( x < 0 ){
							x = 0;
						}
						else if( x + len - 1 > 9 ){
							x = 10 - len;
						}
						
						int y = (int) ( posy + 10 * target_meterics[5] );
						if( y < 0 ){
							y = 0;
						}
						else if ( y > 9){
							y = 9;
						}
						
						boolean val = true;
						for( int j = 0; j < len; j++){
							if( !valid[x+j][y] ){
								val = false;
								break;
							}
						}
						if(val){
							for( int k = 0 ; k < len; k++){
								valid[x+k][y] = false;
							}
							sm1.ships[i] = new TinyShip(new Coordinate(x,y), 0);
							placed = true;
							break;
						}
						cnt++;
					}
					if( !placed ){
						sm1.place(i, valid);
						if( sm1.ships[i].direction == 1){
							for( int k = 0 ; k < len; k++){
								valid[sm1.ships[i].c.getX()][sm1.ships[i].c.getY()+k] = false;
							}
						}
						else{
							for( int k = 0 ; k < len; k++){
								valid[sm1.ships[i].c.getX()+k][sm1.ships[i].c.getY()] = false;
							}
						}
					}
					
				}
				else{
					int cnt = 0;
					boolean placed = false;
					while(cnt < 20){
						//find a ship x place
						int posx = 2 - rGen.nextInt(5); // [-2,2]
						int posy = 1 - rGen.nextInt(3); // [-1,1]
						int y  = (int) ( posy + ( ((10 * target_meterics[5]) - len/2) ) );
						//check if over edge
						if( y < 0 ){
							y = 0;
						}
						else if( y + len - 1 > 9 ){
							y = 10 - len;
						}
						
						int x = (int) ( posx + 10 * target_meterics[4] );
						if( x < 0 ){
							x = 0;
						}
						else if( x > 9){
							x = 9;
						}
						
						boolean val = true;
						for( int j = 0; j < len; j++){
							if( !valid[x][y+j] ){
								val = false;
								break;
							}
						}
						if(val){
							for( int k = 0 ; k < len; k++){
								valid[x][y+k] = false;
							}
							sm1.ships[i] = new TinyShip(new Coordinate(x,y), 1);
							placed = true;
							break;
						}
						cnt++;
					}
					if( !placed ){
						sm1.place(i, valid);
						if( sm1.ships[i].direction == 1){
							for( int k = 0 ; k < len; k++){
								valid[sm1.ships[i].c.getX()][sm1.ships[i].c.getY()+k] = false;
							}
						}
						else{
							for( int k = 0 ; k < len; k++){
								valid[sm1.ships[i].c.getX()+k][sm1.ships[i].c.getY()] = false;
							}
						}
					}
				}
			}
			int spots = 0;
			for( int h = 0; h < 100; h++){
				if( !valid[h/10][h%10]){
					spots++;
				}
			}
			if( spots != 17 ){
				//System.out.println(printArray(valid,10));
			}
			int scr = 45;
			ships.add(0,sm1);		
			this.pscores.add(0,scr);
			
		}
		
	}
	
	private class ShipMap{
		/**
		 * metrics are:
		 * 0 = enemy hits
		 * 1 = enemy misses
		 * 2 = vertical/horizontal ships
		 * 3 = ships together/apart
		 * 4 = avg ship location x
		 * 5 = ||                y
		 */
		public TinyShip[] ships;	
		
		public ShipMap(){
			this.ships = new TinyShip[5];
		};
		
		public void place( int shipMod, boolean[][] valid ){
			// check sonly indicies less than the given. i.e. don't place 5 then try to place 4;
			//System.out.println(" random placed "+ shipMod);
			boolean placed = false;
			int x = 0;
			int y = 0;
			boolean vert = true;
			while( !placed ){
				vert = rGen.nextBoolean();
				boolean val = true;
				if( vert ){
					x = rGen.nextInt( 10 );
					y = rGen.nextInt( 11 - shipLength[shipMod] );
					for( int k = 0; k < shipLength[shipMod]; k++){
						if( !valid[x][k+y] ){
							val = false;
							break;
						}
					}					
				}
				else{
					x = rGen.nextInt( 11 - shipLength[shipMod] );
					y = rGen.nextInt( 10 );
					for( int k = 0; k < shipLength[shipMod]; k++){
						if( !valid[x+k][y] ){
							val = false; 
							break;
						}
					}					
				}
				placed = val;					
			}
			this.ships[shipMod] = new TinyShip(new Coordinate(x,y), vert ? 1 : 0 );
		}
	}
	
	
	private class TinyShip{
		public Coordinate c;
		public int direction;
		public TinyShip(Coordinate cord, int d){
			c = cord;
			direction = d;
		}
	}
}
