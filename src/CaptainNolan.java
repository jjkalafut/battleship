import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class CaptainNolan implements Captain {

	protected final int[] 						shipLength = {2,3,3,4,5};
	protected int[][] 							their_hits;
	protected int[][] 							their_misses;
	protected int								their_hit_shots;
	protected int								their_miss_shots;
    protected Fleet 							myFleet;
    private boolean[] 							shipsAlive;
    private boolean[][] 						myMatchShots;
    private String 								lastOpponent = "";
    private Coordinate 							lastShot;
    private ArrayList<ArrayList<Coordinate>> 	hitShips;
    private ArrayList<ArrayList<NolanPlacement>> 	theirPlacements;
    private int[]								accuracy; //0 is hits 1 is shots
    private double[][][]						lastTen;
    private double[][]							lastTenVal;
    private int[]								lastTenIdx;
    private boolean 							wasWin;
    private int 								turnNum;
    private ArrayList<NolanAttackType>			attackMethods;
    private Random								rGen;
    private int									match_num;
    private int[]								atk_used;

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {

    	this.rGen = new Random();
        this.theirPlacements = new ArrayList<ArrayList<NolanPlacement>>();
        this.myFleet = new Fleet();
        this.shipsAlive = new boolean[]{true, true, true, true, true};
        this.myMatchShots = new boolean[10][10];
        this.hitShips = new ArrayList<ArrayList<Coordinate>>();


        for (int i = 0; i < 10; i++) {
            Arrays.fill(this.myMatchShots[i], false);
        }

        if (!opponent.equals(this.lastOpponent)) {
        	newEnemy(opponent);
        }
        else{
        	this.match_num++;
        }
        
        double best_val = 0;
        int idx = 0;
        double turn_heat[][][] = new double[10][10][5];
        for( int j = 0; j < 5; j++){
        	idx = 0;
        	best_val = 0;
        	for( int k=0; k < attackMethods.size(); k++){
	        	if( this.lastTenVal[k][j] > best_val){
	        		best_val = this.lastTenVal[k][j];
	        		idx = k;
	        	}
        	}
        	double temp[][][] = this.attackMethods.get(idx).getHeat();
        	for( int l = 0; l < 100; l++){
        		turn_heat[l%10][l/10][j] = temp[l%10][l/10][idx];
        	}
        	this.atk_used[idx]++;
        }
        
        createPlacements(turn_heat, this.theirPlacements);
        this.myFleet = randomFleet();
        //placement
        this.turnNum = 0;
        
        if( this.match_num == (numMatches-1)){
        	int total = 0;
        	for( int num: this.atk_used){
        		total += num;
        	}
        	for( int i =0; i < this.atk_used.length; i++){
        		System.out.println( "Attack "+i+" used "+( atk_used[i]* 100 / total )+"% of the time.");
        	}
        }
        
    }
    
    private Fleet randomFleet(){
    	Fleet ret = new Fleet();
    	for( int i = 0; i < 5; i++){
    		while(!ret.placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), i)){}
    	}
    	return ret;
    }
    
    private void createPlacements(double[][][] scorer, ArrayList<ArrayList<NolanPlacement>> list) {
        for (int s = 0; s < 5; s++) {
            int shipLen = this.shipLength[s];
            ArrayList<NolanPlacement> places = new ArrayList<NolanPlacement>();
            for (int i = 0; i < 11 - shipLen; i++) {
                for (int j = 0; j < 10; j++) {
                	NolanPlacement p = new NolanPlacement(0, s, new Coordinate(i, j));
                	NolanPlacement p2 = new NolanPlacement(1, s, new Coordinate(j, i));
                	
                    double score = 0;
                    double score2 = 0;

                    for (int k = 0; k < shipLen; k++) {
                        score += scorer[i + k][j][s];
                        score2 += scorer[j][i + k][s];
                    }
                    
                    p.score = score;
                    p2.score = score2;
                    
                    places.add(p);
                    places.add(p2);
                }
            }
            list.add(places);
        }
    }
    
    private void newEnemy(String opponent){

       	/*------------------------------------------------------------------
       	 * 
       	 * ReInitialize enemy dependant variables
       	 * 
       	 -----------------------------------------------------------------*/
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
        /*---------------------Attack Patterns-----------
         * 
         */
        this.attackMethods = new ArrayList<NolanAttackType>();
        /*---------------------Attack Pattern 1-----------
         * standard shots/misses
         */
        this.attackMethods.add(new NolanAttackType(){

        	/* int 0 hits, int 1 shots, double 1 heat */
			@Override
			public void shotHere(boolean wasHit, int shipMod, Coordinate c) {
				
				if( !started ){
					 for (int j = 0; j < 100; j++) {
						 
			                shipIntArrays[j % 10][j / 10][0][1] = 2;				                
			                for (int k = 0; k < 5; k++) {
			                	shipIntArrays[j % 10][j / 10][k][0] = 1;
			                	shipDoubleArrays[j % 10][j / 10][k][0] = (double) shipIntArrays[j % 10][j / 10][k][0]/ (double) shipIntArrays[j % 10][j / 10][0][1];
			                }
			          }
					 started = true;
				}
				
				shipIntArrays[c.getX()][c.getY()][0][1]++;
				
				if( wasHit ){
					shipIntArrays[c.getX()][c.getY()][shipMod][0]++;
				}
				
				for( int m = 0; m < 5; m++){
					shipDoubleArrays[c.getX()][c.getY()][m][0] = (double) shipIntArrays[c.getX()][c.getY()][m][0]/ (double) shipIntArrays[c.getX()][c.getY()][0][1];
				}
			}

			@Override
			public double[][][] getHeat() {
				double [][][] ret = new double[10][10][5];
				double biggest = 0;
				for (int i = 0; i < 100; i++) {
					for (int j = 0; j < 5; j++){
						ret[i % 10][i / 10][j] = shipDoubleArrays[i % 10][i / 10][j][0];
						if( shipDoubleArrays[i % 10][i / 10][j][0] > biggest ){
							biggest = shipDoubleArrays[i % 10][i / 10][j][0];
						}
					}						
				}
				if( biggest == 0 ){
					biggest = 1;
				}
				for (int i = 0; i < 100; i++) {
					for (int j = 0; j < 5; j++){
						ret[i % 10][i / 10][j] /= biggest;
					}
				}
				return ret;
			}
			
        });
        /*---------------------Attack Pattern 2-----------
         * last 100 hits
         */
        this.attackMethods.add(new NolanAttackType(){

        	/* int 0 hits, int 1 shots, double 1 heat */
        	private Coordinate[] oldShots = new Coordinate[100];
        	private boolean[]oldHit = new boolean[100];
        	private int[] shipHitMod = new int[100];
        	private int shotIdx = 0;
        	
			@Override
			public void shotHere(boolean wasHit, int shipMod, Coordinate c) {
				
				if( !started ){
					Arrays.fill(oldHit, false);
					 for (int j = 0; j < 100; j++) {

						 oldShots[j] = new Coordinate(j % 10,j / 10);
						 shipIntArrays[j % 10][j / 10][0][1] = 2;				                
		                 for (int k = 0; k < 5; k++) {			                	 
		                	 shipIntArrays[j % 10][j / 10][k][0] = 1;
		                	 shipDoubleArrays[j % 10][j / 10][k][0] = (double) shipIntArrays[j % 10][j / 10][k][0]/ (double) shipIntArrays[j % 10][j / 10][0][1];
		                 }
			          }

					 started = true;
				}

				if( oldHit[shotIdx] ){
					shipIntArrays[oldShots[shotIdx].getX()][oldShots[shotIdx].getY()][shipHitMod[shotIdx]][0]--;
				}
				shipIntArrays[oldShots[shotIdx].getX()][oldShots[shotIdx].getY()][0][1]--;
				
				shipIntArrays[c.getX()][c.getY()][0][1]++;
				oldShots[shotIdx] = c;
				oldHit[shotIdx] = false;
				
				if( wasHit ){
					shipIntArrays[c.getX()][c.getY()][shipMod][0]++;
					oldHit[shotIdx] = true;
					shipHitMod[shotIdx] = shipMod;
					
				}
				
				shotIdx = (shotIdx + 1) % oldShots.length;
				for( int m = 0; m < 5; m++){
					shipDoubleArrays[c.getX()][c.getY()][m][0] = (double) shipIntArrays[c.getX()][c.getY()][m][0]/ (double) shipIntArrays[c.getX()][c.getY()][0][1];
				}
			}

			@Override
			public double[][][] getHeat() {
				double [][][] ret = new double[10][10][5];
				double biggest = 0;
				for (int i = 0; i < 100; i++) {
					for (int j = 0; j < 5; j++){
						ret[i % 10][i / 10][j] = shipDoubleArrays[i % 10][i / 10][j][0];
						if( shipDoubleArrays[i % 10][i / 10][j][0] > biggest ){
							biggest = shipDoubleArrays[i % 10][i / 10][j][0];
						}
					}						
				}
				if( biggest == 0 ){
					biggest = 1;
				}
				for (int i = 0; i < 100; i++) {
					for (int j = 0; j < 5; j++){
						ret[i % 10][i / 10][j] /= biggest;
					}
				}
				return ret;
			}
			
        	
        });
        /*---------------------Attack Pattern 3-----------
         * least shot i think
         */
        this.attackMethods.add(new NolanAttackType(){

        	/* int 0 hits, int 1 shots, double 1 heat */
			@Override
			public void shotHere(boolean wasHit, int shipMod, Coordinate c) {
				
				if( !started ){
					 for (int j = 0; j < 100; j++) {						 
			                shipIntArrays[j % 10][j / 10][0][0] = 2;				                
			          }
					 started = true;
				}
				
				shipIntArrays[c.getX()][c.getY()][0][0]++;
				
				for( int m = 0; m < 5; m++){
					shipDoubleArrays[c.getX()][c.getY()][m][0] = 800000.0 / (double) shipIntArrays[c.getX()][c.getY()][0][0];
				}
			}

			@Override
			public double[][][] getHeat() {
				double [][][] ret = new double[10][10][5];
				double biggest = 0;
				double smallest = Double.MAX_VALUE;
				for (int i = 0; i < 100; i++) {
					for (int j = 0; j < 5; j++){
						ret[i % 10][i / 10][j] = shipDoubleArrays[i % 10][i / 10][j][0];
						if( shipDoubleArrays[i % 10][i / 10][j][0] > biggest ){
							biggest = shipDoubleArrays[i % 10][i / 10][j][0];
						}
						if( shipDoubleArrays[i % 10][i / 10][j][0] < smallest ){
							smallest = shipDoubleArrays[i % 10][i / 10][j][0];
						}
					}						
				}
				if( biggest == 0 ){
					biggest = 1;
				}
				if(smallest == biggest){
					smallest = .5*biggest;
				}
				biggest -= smallest;
				for (int i = 0; i < 100; i++) {
					for (int j = 0; j < 5; j++){
						ret[i % 10][i / 10][j] -= smallest;
						ret[i % 10][i / 10][j] /= biggest;
					}
				}
				return ret;
			}
			
        });
        /*---------------------Attack Pattern 4-----------
         * what do...
         */
        this.attackMethods.add(new NolanAttackType(){

        	/* int 0 washits, int 1 nowhit, int 2 heading */
			@Override
			public void shotHere(boolean wasHit, int shipMod, Coordinate c) {
				
			}

			@Override
			public double[][][] getHeat() {
				double [][][] ret = new double[10][10][5];
				double[][] basic = new double[][]{
						{0,	.1,	.2,	.3, .4, .4, .3, .2, .1, 0},
						{.1, .2, .3, .4, .5, .5, .4, .3, .2, .1},
						{.2, .3, .4, .5, .6, .6, .5, .4, .3, .2},
						{.3, .4, .5, .6, .7, .7, .6, .5, .4, .3},
						{.4, .5, .6, .7, .8, .8, .7, .6, .5, .4},
						{.4, .5, .6, .7, .8, .8, .7, .6, .5, .4},
						{.3, .4, .5, .6, .7, .7, .6, .5, .4, .3},
						{.2, .3, .4, .5, .6, .6, .5, .4, .3, .2},
						{.1, .2, .3, .4, .5, .5, .4, .3, .2, .1},
						{0,	.1,	.2,	.3, .4, .4, .3, .2, .1, 0}
				};
				for( int i = 0; i < 100; i++){
					ret[i/10][i%10][0] = basic[i/10][i%10];
					ret[i/10][i%10][1] = basic[i/10][i%10];
					ret[i/10][i%10][2] = basic[i/10][i%10];
					ret[i/10][i%10][3] = basic[i/10][i%10];
					ret[i/10][i%10][4] = basic[i/10][i%10];
				}
				
				return ret;
				
			}
			
        });
        
        this.lastTen = new double[10][attackMethods.size()][5];
        this.lastTenVal = new double[attackMethods.size()][5];
        this.atk_used = new int[attackMethods.size()];
        this.lastTenIdx = new int[5];
    	this.accuracy = new int[attackMethods.size()];
        /*
         *Init attack patterns 
         */            
        this.attackMethods.get(0).init(1, 2);
        this.attackMethods.get(1).init(1, 2);
        this.attackMethods.get(2).init(1, 1);
        this.attackMethods.get(3).init(0, 0);
        this.match_num = 0;
    }

    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    @Override
    public Coordinate makeAttack() {
        this.turnNum++;
        Coordinate shot = new Coordinate(0, 0);
        
        double[][] turnHeat = new double[10][10];
        for (int s = 4; s >= 0; s--) {
            if (this.shipsAlive[s]) {
                ArrayList<NolanPlacement> places = this.theirPlacements.get(s);
                for (NolanPlacement p : places) {
                    for (Coordinate c : p.coords) {
                        turnHeat[c.getX()][c.getY()] += p.score;
                    }
                }
            }
        }
        int x = 0;
        int y = 0;
        double best = 0;
        for (int i = 0; i < 100; i++) {
            if (!this.myMatchShots[i % 10][i / 10] && turnHeat[i % 10][i / 10] >= best) {
                best = turnHeat[i % 10][i / 10];
                x = i % 10;
                y = i / 10;
            }
        }
        
        shot = new Coordinate(x,y);
        this.lastShot = shot;
        this.myMatchShots[shot.getX()][shot.getY()] = true;
        return shot;
    }

    @Override
    public void resultOfAttack(int result) {
    	int shipMod = result % 10;
        if (result == MISS || result == DEFEATED) {
        	
        	for( NolanAttackType pat : this.attackMethods){
            	pat.shotHere( false, 0, this.lastShot );
            }
            for (int s = 0; s < 5; s++) {
                if (this.shipsAlive[s]) {
                    ArrayList<NolanPlacement> places = this.theirPlacements.get(s);
                    ArrayList<NolanPlacement> bads = new ArrayList<NolanPlacement>();
                    for (NolanPlacement p : places) {
                        if (p.contains(this.lastShot)) {
                            bads.add(p);
                        }
                    }
                    places.removeAll(bads);
                }
            }
        } else {
        	this.accuracy[0]++;
        	
        	for( NolanAttackType pat : this.attackMethods){
            	double val = pat.getHeat()[this.lastShot.getX()][this.lastShot.getY()][shipMod];
            	this.lastTenVal[this.attackMethods.indexOf(pat)][shipMod] -= this.lastTen[this.lastTenIdx[shipMod]][this.attackMethods.indexOf(pat)][shipMod];
            	this.lastTenVal[this.attackMethods.indexOf(pat)][shipMod] += val;
            	this.lastTen[this.lastTenIdx[shipMod]][this.attackMethods.indexOf(pat)][shipMod] = val;
            	pat.shotHere( true, shipMod, this.lastShot );
            }
        	this.lastTenIdx[shipMod] = (this.lastTenIdx[shipMod] + 1) % 10;

            if (result >= 20) {

                this.shipsAlive[shipMod] = false;
                ArrayList<Coordinate> bad_ship = new ArrayList<Coordinate>();
                for (ArrayList<Coordinate> ship : this.hitShips) {
                    if (ship.get(0).getX() == shipMod) {
                        bad_ship = ship;
                        break;
                    }
                }
                this.hitShips.remove(bad_ship);
            } else {
                boolean hitBefore = false;
                for (ArrayList<Coordinate> ship : this.hitShips) {
                    if (ship.get(0).getX() == shipMod) {
                        hitBefore = true;
                        //ship.add(this.lastShot);
                        break;
                    }
                }
                if (!hitBefore) {
                    ArrayList<Coordinate> newShip = new ArrayList<Coordinate>();
                    newShip.add(new Coordinate(shipMod, 0));
                    newShip.add(this.lastShot);
                    this.hitShips.add(newShip);
                }
            }
            for (int s = 0; s < 5; s++) {
                if (this.shipsAlive[s] && s != shipMod) {
                    ArrayList<NolanPlacement> places = this.theirPlacements.get(s);
                    ArrayList<NolanPlacement> bads = new ArrayList<NolanPlacement>();
                    for (NolanPlacement p : places) {
                        if (p.contains(this.lastShot)) {
                            bads.add(p);
                        }
                    }
                    places.removeAll(bads);
                } else if (this.shipsAlive[s] && s == shipMod) {
                    ArrayList<NolanPlacement> places = this.theirPlacements.get(s);
                    ArrayList<NolanPlacement> bads = new ArrayList<NolanPlacement>();
                    for (NolanPlacement p : places) {
                        if (!p.contains(this.lastShot)) {
                            bads.add(p);
                        }
                    }
                    places.removeAll(bads);
                }
            }

        }
    }

    @Override
    public void opponentAttack(Coordinate coord) {
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
        this.wasWin = ( result == WON );
    }
	
	private class NolanPlacement {

        public Coordinate[] coords;
        public double score = 0;
        public int[] shipLength = {2, 3, 3, 4, 5};
        public int direc;

        NolanPlacement(int direc, int type, Coordinate loc) {
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
	
	private abstract class NolanAttackType{
		
		public boolean				started;
		public double 				accuracy;
		public double[][][][] 		shipDoubleArrays;
		public int[][][][] 			shipIntArrays;
		
		public NolanAttackType(){
			
		}

		public void init( int num_dbl_arrays, int num_int_arrays ){
			shipDoubleArrays = new double[10][10][5][num_dbl_arrays];
			shipIntArrays = new int[10][10][5][num_int_arrays];
			accuracy = 1.0;
			started = false;
		}
		public abstract void shotHere( boolean wasHit, int shipMod, Coordinate c);
		public abstract double[][][] getHeat();
		
	}
}
