import java.util.ArrayList;
import java.util.Random;


public class CaptainDo_Urden implements Captain {

	private 				Random rgen;
	protected int[] 		shipLength = {2,3,3,4,5};
	protected int[][] 		their_hits;
	protected int[][] 		their_misses;
	protected int			their_shots;
	@Override
	public void initialize(int numMatches, int numCaptains, String opponent) {
		// TODO Auto-generated method stub

	}

	@Override
	public Fleet getFleet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Coordinate makeAttack() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resultOfAttack(int result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void opponentAttack(Coordinate coord) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resultOfGame(int result) {
		// TODO Auto-generated method stub

	}

	// influid placements dont add more, shop botom, add more, and keep using all
	
	//ONLY WORRY ABOUT BINS
	private class SuperMegaShipPlacer_3000{
		
		//the weights to use for each metric
		private float[] 					metricWeights;
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
		
		
		public SuperMegaShipPlacer_3000(){
			this.metricWeights = new float[]{ .2f, .2f, .1f, .1f, .2f, .2f};
			this.bins = new ArrayList<PlacementBin>();
			this.scores = new ArrayList<Integer>();
			this.numberBins = 2;
			this.matchRedist = 10;
			this.numberMatch = 0;
			
			//create 2 new bins with 10 random maps each
			//this should create a two bins to determine a good metric (well start determining anyway)
			float[] rmeterics = new float[]{rgen.nextFloat(),rgen.nextFloat(),rgen.nextFloat(),rgen.nextFloat(),rgen.nextFloat(),rgen.nextFloat()};
			PlacementBin b1 = new PlacementBin(10,rmeterics);
			this.bins.add(b1);
			this.scores.add(45);
			
			for( int i = 0; i < rmeterics.length; i++){
				rmeterics[i] = 1-rmeterics[i];
			}
			PlacementBin b2 = new PlacementBin(10,rmeterics);
			this.bins.add(b2);
			this.scores.add(44);			
		}
		public Fleet getNextPlacement(boolean won, int turns_taken){
			this.numberMatch++;
			Fleet ret = new Fleet();		
			ShipMap place;
			
			this.total_turns += total_turns;
			
			
			if( won){
				//replay same ship pattern	
				place = this.last;
				
			}
			else{
				this.over_games++;		
				place = this.bins.get(0).getShipMap();
				this.last = place;
				
				if( this.numberMatch >= matchRedist ){
					reThink();
				}
			}
			
			for( int i = 0; i < 5; i++){
				ret.placeShip(place.ships[i].c, place.ships[i].direction, i);
			}
			return ret;					
		}

		private void reThink(){
			int new_score  = this.total_turns / this.over_games;
			//changing bins, see if another bin is better that the current
			int new_ind = getInd(new_score,1,this.scores.size());
			int old_score = this.scores.remove(0);
			this.scores.add(new_ind,new_score);
    		this.bins.add(new_ind, this.bins.remove(0));
			//evaluate how each bin is fairing compared with its meterics
			float[] importance = new float[6];
			
			
			//reweigh the meterics based on the success of bins emphasizing those meterics (weight based on further from .5 for SOME)
			
			//set match redist to a good value. if the enemy is switching fast, go faster. if switching slow, use less bins. Figues this out from the drop of the bin score over the last dist period.
			
			//maybe set bin# to 1000000 / match redist as to not waste resources?
			
			//maybe drop the worst bins. if creating more Use similarity and meteric weights. 
			
			this.over_games = 0;
			this.total_turns = 0;
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
    				return getInd(turns,test+1,q);
    			}
    		}
    	}
		
		
	}
	//ONLY WORRY ABOUT SHIP MAPS
	private class PlacementBin{
		public 	float[] 				target_meterics = new float[6];
		private ArrayList<ShipMap> 		ships;
		private int						cur_ind;
		
		public PlacementBin(int size, float[] meterics){
			this.ships = new ArrayList<ShipMap>();
			this.target_meterics = meterics;
			setSize(size);
			cur_ind = 0;
		}
		public ShipMap getShipMap(){
			this.cur_ind++;
			if(cur_ind == ships.size()){
				cur_ind = 0;
			}
			return ships.get(cur_ind);
		}
		public void setSize(int i){
			if( i > ships.size() ){
				generateNew();
				setSize(i);
			}
			else if( i < ships.size() ){
				this.cur_ind = 0;
				ships.remove(ships.size() - 1);
				setSize(i);
			}
		}
		private void generateNew(){ 
			ShipMap sm1 = randomMap();
			float[] difs = getDifferences(sm1.metrics,this.target_meterics);
			while( difs[6] > .15){
				sm1 = randomMap();
				difs = getDifferences(sm1.metrics,this.target_meterics);
			}
		}
		//usually m2 is traget meterics
		private float[] getDifferences(float[] m1, float[] m2){
			float[] ret = new float[7];
			float total = 0;
			for( int n = 0; n < 6; n++){
				ret[n] = m2[n] - m1[n];
				total += (ret[n] > 0) ? ret[n] : -ret[n];
			}
			ret[6] = total / 6f;
			return ret;
		}
		private ShipMap randomMap(){
			ShipMap ret = new ShipMap();
			boolean[][] ships = new boolean[10][10];
			for( int i = 0; i < 5; i++){
				int ship_len = shipLength[i];
				boolean hor_place = rgen.nextBoolean();
				boolean placed = true;
				while( !placed){
					placed = true;
					if(hor_place){
						int x = rgen.nextInt(11-ship_len);
						int y = rgen.nextInt(10);
						for( int j = 0; j < ship_len; j++){
							if(ships[x+j][y]){
								placed = false;
								break;
							}
						}
						if(placed){
							ret.ships[i] = new TinyShip(new Coordinate(x,y), 0);
							for( int j = 0; j < ship_len; j++){
								ships[x+j][y] = true;
							}
						}
					}
					else{
						int x = rgen.nextInt(10);
						int y = rgen.nextInt(11-ship_len);
						for( int j = 0; j < ship_len; j++){
							if(ships[x+j][y]){
								placed = false;
								break;
							}
						}
						if(placed){
							ret.ships[i] = new TinyShip(new Coordinate(x,y), 1);
							for( int j = 0; j < ship_len; j++){
								ships[x+j][y] = true;
							}
						}
					}
				}
			}
			ret.MetericMap(their_hits, their_misses, their_shots);
			return ret;
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
		public TinyShip[] ships = new TinyShip[5];		
		public float[] metrics = new float[6];		
		
		public ShipMap(){};
		
		public void MetericMap( int[][] enemy_hit, int[][] enemy_miss, int enemy_shots){

			boolean[][] map  = new boolean[10][10];
			boolean[][] nextTo  = new boolean[10][10];
			int total_x = 0;
			int total_y = 0;
			int hits = 0;
			int misses = 0;
			for( int i = 0; i < 5; i++){
				int ship_len = shipLength[i];
				int x = this.ships[i].c.getX();
				int y = this.ships[i].c.getX();
				
				if(this.ships[i].direction == 1){
					this.metrics[2] += .2;	//verticle = 1 so only add for verticel				
					
					if(y > 0){nextTo[x][y-1] = true;}
					if(y < 9 ){nextTo[x][y+1] = true;}
					
					for( int j = 0; j < ship_len; j++){
						total_x += x;
						total_y += y+j;
						hits += enemy_hit[x][y+j];
						misses += enemy_miss[x][y+j];
						if(nextTo[x][y+j]){ this.metrics[3] += .083f; }
						map[x][y+j] = true;						
						if( x > 0){nextTo[x-1][y+j] = true;}
						if( x < 9){nextTo[x+1][y+j] = true;}
					}
				}
				else{					
					if(x > 0){nextTo[x-1][y] = true;}
					if(x < 9 ){nextTo[x+1][y] = true;}
					
					for( int j = 0; j < ship_len; j++){
						total_x += x+j;
						total_y += y;
						hits += enemy_hit[x+j][y];
						misses += enemy_miss[x+j][y];
						if(nextTo[x+j][y]){ this.metrics[3] += .083f; }
						map[x+j][y] = true;						
						if( y > 0){nextTo[x+j][y-1] = true;}
						if( y < 9){nextTo[x+j][y+1] = true;}
					}
				}
			}
		this.metrics[4] = (float) total_x / 10f;
		this.metrics[5] = (float) total_y / 10f;
		this.metrics[0] = (float) hits / (float)enemy_shots;
		this.metrics[1] = (float) misses / (float)enemy_shots;
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
