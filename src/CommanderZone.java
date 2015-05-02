import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class CommanderZone implements Captain {

	protected final int[] 						shipLength = {2,3,3,4,5};
    protected Fleet 							myFleet;
    private boolean[] 							shipsAlive;
    private boolean[][] 						myMatchShots;
    private boolean[]							shipWasHit;
    private String 								lastOpponent = "";
    private Coordinate 							lastShot;
    private ArrayList<ArrayList<ZonePlacement>> theirPlacements;
    private double[][][]						lastTen;
    private double[][]							lastTenVal;
    private int[]								lastTenIdx;
    private int[][]								hitTheirShips;
    private double[][][][]						atkHeats;
    private boolean 							wasWin;
    private int 								turnNum;
    private ArrayList<Coordinate>				firstShots;
    private ArrayList<ZoneAttackType>			attackMethods;
    private Random								rGen;
    private int									match_num;
    private int[]								atk_used;
    private ZoneDist							redPlace;
    private int									randShots;

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {

    	this.rGen = new Random();
        this.theirPlacements = new ArrayList<ArrayList<ZonePlacement>>();
        this.myFleet = new Fleet();
        this.shipsAlive = new boolean[]{true, true, true, true, true};
        this.myMatchShots = new boolean[10][10];
        this.randShots = rGen.nextInt(5) + 2;
        this.firstShots = new ArrayList<Coordinate>();
        checkShots();

        for (int i = 0; i < 10; i++) {
            Arrays.fill(this.myMatchShots[i], false);
        }

        if (!opponent.equals(this.lastOpponent)) {
        	newEnemy(opponent);
        }
        else{
        	this.match_num++;
        	if( match_num > 13000){
        		redPlace.numFleets = 3;
        	}
        }
        
        double total_avg[][] = new double[this.attackMethods.size()][5];
        double total_cnt[] = new double[5];
        for( int b = 0; b < this.attackMethods.size(); b++ ){
	        double total_tmp[] = new double[5];
			for( int k = 0; k < 100; k++  ){
				total_tmp[0] += this.atkHeats[b][k%10][k/10][0];
				total_tmp[1] += this.atkHeats[b][k%10][k/10][1];
				total_tmp[2] += this.atkHeats[b][k%10][k/10][2];
				total_tmp[3] += this.atkHeats[b][k%10][k/10][3];
				total_tmp[4] += this.atkHeats[b][k%10][k/10][4];
			}
			total_cnt[0] += total_tmp[0];
			total_avg[b][0] = total_tmp[0];
			total_cnt[1] += total_tmp[1];
			total_avg[b][1] = total_tmp[1];
			total_cnt[2] += total_tmp[2];
			total_avg[b][2] = total_tmp[2];
			total_cnt[3] += total_tmp[3];
			total_avg[b][3] = total_tmp[3];
			total_cnt[4] += total_tmp[4];
			total_avg[b][4] = total_tmp[4];
        }

        for( int i = 0; i < 5; i++){
        	total_cnt[i] = total_cnt[i] / 5.0;
        }
        for( int c = 0; c < this.attackMethods.size(); c++){
        	for( int d = 0; d < 5; d++){
        		double factor = total_avg[c][d] / total_cnt[d];
        		for( int e = 0; e < 100; e++){
        			this.atkHeats[c][e%10][e/10][d] = factor * attackMethods.get(c).getHeat()[e%10][e/10][d];
        		}
        	}
        }
        
        double best_val = 0;
        double sec_best_val = 0;
        int idx = 0;
        int sec_idx = 0;
        double turn_heat[][][] = new double[10][10][5];
        for( int j = 0; j < 5; j++){
        	best_val = 0;
        	sec_best_val = 0;
        	for( int k = 0; k < attackMethods.size(); k++){
	        	if( this.lastTenVal[k][j] > best_val){
	        		best_val = this.lastTenVal[k][j];
	        		idx = k;
	        	}
        	}
        	for( int g = 0; g < attackMethods.size(); g++){
        		if( g!= idx ){
		        	if( this.lastTenVal[g][j] > sec_best_val){
		        		sec_best_val = this.lastTenVal[g][j];
		        		sec_idx = g;
		        	}
        		}
        	}
        	for( int l = 0; l < 100; l++){
        		turn_heat[l%10][l/10][j] = this.atkHeats[idx][l%10][l/10][j] + ( .5 * this.atkHeats[sec_idx][l%10][l/10][j] );
        	}
        	this.atk_used[idx]++;
        }
        
        createPlacements(turn_heat, this.theirPlacements);
        //this.myFleet = randomFleet();
        //placement
        this.turnNum = 0;
        
        //TODO FIX AVERAGES 
        if( this.match_num == (numMatches-1)){
        	int total = 1;
        	for( int num: this.atk_used){
        		total += num;
        	}
        	for( int i =0; i < this.atk_used.length; i++){
        		System.out.println( "Attack "+i+" used "+( atk_used[i]* 100 / total )+"% of the time.");
        	}
        	for( int j = 0; j < this.attackMethods.size(); j++){
        		double total_tmp = 0;
        		for( int k = 0; k < 100; k++  ){
        			total_tmp += this.atkHeats[j][k%10][k/10][0];
        			total_tmp += this.atkHeats[j][k%10][k/10][1];
        			total_tmp += this.atkHeats[j][k%10][k/10][2];
        			total_tmp += this.atkHeats[j][k%10][k/10][3];
        			total_tmp += this.atkHeats[j][k%10][k/10][4];
        		}
        		System.out.println("total avg for method "+j+" is "+(total_tmp / 500.0));
        	}
        	
        	double max = 5 * this.match_num;
        	for( int k = 0; k < redPlace.numFleets; k++ ){
        		System.out.println("Placement " + k + " used "+ ( ( (double)redPlace.used[k] / max) * 100) +"% of the time.");
        	}
        	
        	for( int g = 0; g < 5; g++){
        		System.out.println("Ship "+g+" is "+redPlace.shipNumbers[g][5][5]+" and "+redPlace.shipNumbers[g][5][6]);
        	}
        }
        redPlace.refresh();
        if( this.wasWin){
        	this.redPlace.getFleet(this.shipWasHit);
        }
        else{
        	redPlace.getFleet();
        }
        redPlace.remakeFleets();
        shipWasHit = new boolean[]{ false, false, false, false, false};
    }
    private void checkShots() {
    	
    	if( this.match_num % 2 == 0){
    		this.firstShots.add(new Coordinate(0,0));
    		this.firstShots.add(new Coordinate(0,2));
    		this.firstShots.add(new Coordinate(0,4));
    		this.firstShots.add(new Coordinate(0,6));
    		this.firstShots.add(new Coordinate(0,8));
    		this.firstShots.add(new Coordinate(2,0));
    		this.firstShots.add(new Coordinate(4,0));
    		this.firstShots.add(new Coordinate(6,0));
    		this.firstShots.add(new Coordinate(8,0));
    		this.firstShots.add(new Coordinate(9,1));
    		this.firstShots.add(new Coordinate(9,3));
    		this.firstShots.add(new Coordinate(9,5));
    		this.firstShots.add(new Coordinate(9,7));
    		this.firstShots.add(new Coordinate(9,9));
    		this.firstShots.add(new Coordinate(1,9));
    		this.firstShots.add(new Coordinate(3,9));
    		this.firstShots.add(new Coordinate(5,9));
    		this.firstShots.add(new Coordinate(7,9));
    	}else{
    		this.firstShots.add(new Coordinate(0,9));
    		this.firstShots.add(new Coordinate(0,7));
    		this.firstShots.add(new Coordinate(0,5));
    		this.firstShots.add(new Coordinate(0,3));
    		this.firstShots.add(new Coordinate(0,1));
    		this.firstShots.add(new Coordinate(9,0));
    		this.firstShots.add(new Coordinate(7,0));
    		this.firstShots.add(new Coordinate(5,0));
    		this.firstShots.add(new Coordinate(3,0));
    		this.firstShots.add(new Coordinate(1,0));
    		this.firstShots.add(new Coordinate(9,2));
    		this.firstShots.add(new Coordinate(9,4));
    		this.firstShots.add(new Coordinate(9,6));
    		this.firstShots.add(new Coordinate(9,8));
    		this.firstShots.add(new Coordinate(2,9));
    		this.firstShots.add(new Coordinate(4,9));
    		this.firstShots.add(new Coordinate(6,9));
    		this.firstShots.add(new Coordinate(8,9));    		
    	}
		
	}
    
	private void printArray(int[][] array) {
		for( int i = 0; i < 10; i++){
			for( int j = 0; j < 10; j++){
				System.out.print(array[j][i]+", ");
			}
			System.out.println();		
		}		
	}

    private void createPlacements(double[][][] scorer, ArrayList<ArrayList<ZonePlacement>> list) {
        for (int s = 0; s < 5; s++) {
            int shipLen = this.shipLength[s];
            ArrayList<ZonePlacement> places = new ArrayList<ZonePlacement>();
            for (int i = 0; i < 11 - shipLen; i++) {
                for (int j = 0; j < 10; j++) {
                	ZonePlacement p = new ZonePlacement(0, s, new Coordinate(i, j));
                	ZonePlacement p2 = new ZonePlacement(1, s, new Coordinate(j, i));
                	
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
        this.redPlace = new ZoneDist();
        this.wasWin = false;
        this.lastOpponent = opponent;
        this.hitTheirShips = new int[10][10];
        /*---------------------Attack Patterns-----------
         * 
         */
        this.attackMethods = new ArrayList<ZoneAttackType>();
        /*---------------------Attack Pattern 1-----------
         * standard shots/misses
         */
        this.attackMethods.add(new ZoneAttackType(){

        	/* int 0 hits, int 1 shots, double 1 heat */
			@Override
			public void shotHere(boolean wasHit, int shipMod, Coordinate c) {
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
				
				/*
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
				*/
				double [][][] ret = new double[10][10][5];
				for (int i = 0; i < 100; i++) {
					for (int j = 0; j < 5; j++){
						ret[i % 10][i / 10][j] = shipDoubleArrays[i % 10][i / 10][j][0];
					}						
				}
				return ret;
			}
			
        });
        
        /*---------------------Attack Pattern 2-----------
         * last 100 hits
         */
        this.attackMethods.add(new ZoneAttackType(){

        	// int 0 hits, int 1 shots, double 1 heat 
        	private Coordinate[] oldShots = new Coordinate[100];
        	private boolean[]oldHit = new boolean[100];
        	private int[] shipHitMod = new int[100];
        	private int shotIdx = 0;
        	
			@Override
			public void shotHere(boolean wasHit, int shipMod, Coordinate c) {

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
				/*
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
				*/
				double [][][] ret = new double[10][10][5];
				for (int i = 0; i < 100; i++) {
					for (int j = 0; j < 5; j++){
						ret[i % 10][i / 10][j] = shipDoubleArrays[i % 10][i / 10][j][0];
					}
				}
				return ret;
			}
			
        	
        });
        
        /*---------------------Attack Pattern 3-----------
         * 
         */
        this.attackMethods.add(new ZoneAttackType(){

        	/* int 0 hits, int 1 shots, double 1 heat */
			@Override
			public void shotHere(boolean wasHit, int shipMod, Coordinate c) {				
				shipIntArrays[c.getX()][c.getY()][0][0]++;
				
				for( int m = 0; m < 5; m++){
					shipDoubleArrays[c.getX()][c.getY()][m][0] = 800000.0 / (double) shipIntArrays[c.getX()][c.getY()][0][0];
				}
			}

			@Override
			public double[][][] getHeat() {
				if( !started ){
					 for (int j = 0; j < 100; j++) {						 
			                shipIntArrays[j % 10][j / 10][0][0] = 2;				                
			          }
					 started = true;
				}
				/*
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
				*/
				double [][][] ret = new double[10][10][5];
				for (int i = 0; i < 100; i++) {
					for (int j = 0; j < 5; j++){
						ret[i % 10][i / 10][j] = shipDoubleArrays[i % 10][i / 10][j][0];
					}
				}
				return ret;
			}
			
        });
        /*---------------------Attack Pattern 4-----------
         * what do...
         */
        this.attackMethods.add(new ZoneAttackType(){

        	// int 0 washits, int 1 nowhit, int 2 heading 
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
        
        /*---------------------Attack Pattern 4-----------
         * what do...
         */
        this.attackMethods.add(new ZoneAttackType(){

        	// int 0 washits, int 1 nowhit, int 2 heading 
			@Override
			public void shotHere(boolean wasHit, int shipMod, Coordinate c) {
				
			}

			@Override
			public double[][][] getHeat() {
				double [][][] ret = new double[10][10][5];
				double[][] basic = new double[][]{
						{.9,	.9,		.5,		.5, .8, .7, .6, .5, .9, .9},
						{.9, 	.5, 	.3, 	.2, .3, .3, .2, .3, .5, .9},
						{.5, 	.3, 	.3, 	.1, .2, .1, .2, .3, .3, .5},
						{.5, 	.2, 	.1, 	.3, .1, .2, .1, .2, .2, .6},
						{.8, 	.3, 	.2, 	.1, .3, .1, .2, .2, .3, .7},
						{.7, 	.3, 	.1, 	.2, .1, .3, .1, .2, .3, .8},
						{.6, 	.2, 	.2, 	.1, .2, .1, .2, .2, .2, .5},
						{.5, 	.3, 	.1, 	.2, .1, .2, .1, .3, .3, .5},
						{.9, 	.5, 	.3, 	.2, .3, .3, .2, .3, .5, .9},
						{.9,	.9,		.5,		.6, .7, .8, .5, .5, .9, .9}
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
        
        /*---------------------Attack Pattern 4-----------
         * what do...
         */
        this.attackMethods.add(new ZoneAttackType(){

        	// int 0 washits, int 1 nowhit, int 2 heading 
			@Override
			public void shotHere(boolean wasHit, int shipMod, Coordinate c) {
				
			}

			@Override
			public double[][][] getHeat() {
				double [][][] ret = new double[10][10][5];
				double[][] basic = new double[][]{
						{.9,	.9,		.5,		.5, .8, .7, .6, .5, .9, .9},
						{.9, 	.5, 	.3, 	.1, .3, .3, .1, .3, .5, .9},
						{.5, 	.3, 	.3, 	.2, .1, .2, .1, .3, .3, .5},
						{.5, 	.1, 	.2, 	.1, .2, .1, .3, .1, .1, .8},
						{.6, 	.3, 	.1, 	.2, .1, .3, .1, .1, .3, .7},
						{.7, 	.3, 	.2, 	.1, .3, .1, .2, .1, .3, .6},
						{.8, 	.1, 	.1, 	.3, .1, .2, .1, .1, .1, .5},
						{.5, 	.3, 	.2, 	.1, .2, .1, .2, .3, .3, .5},
						{.9, 	.5, 	.3, 	.1, .3, .3, .1, .3, .5, .9},
						{.9,	.9,		.5,		.8, .7, .6, .5, .5, .9, .9}
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
        /*---------------------Attack Pattern 5-----------
         * where I've shot the least
         * 
         */
        this.attackMethods.add(new ZoneAttackType(){

			@Override
			public void shotHere(boolean wasHit, int shipMod, Coordinate c) {
				shipIntArrays[c.getX()][c.getY()][0][0]++;
				
				if( shipIntArrays[c.getX()][c.getY()][0][0] > accuracy){
					accuracy = shipIntArrays[c.getX()][c.getY()][0][0];
				}
			}

			@Override
			public double[][][] getHeat() {
				double [][][] ret = new double[10][10][5];
				
				for( int i = 0; i < 100; i++){
					int x = i % 10;
					int y = i / 10;
					double val = 1 - ((double)shipIntArrays[x][y][0][0] / accuracy);
					ret[x][y][0] = val;
					ret[x][y][1] = val;
					ret[x][y][2] = val;
					ret[x][y][3] = val;
					ret[x][y][4] = val;
				}
				
				return ret;
				
			}
			
        });
        
        this.lastTen = new double[10][attackMethods.size()][5];
        this.lastTenVal = new double[attackMethods.size()][5];
        this.atk_used = new int[attackMethods.size()];
        this.lastTenIdx = new int[5];
        /*
         *Init attack patterns 
         */            
        this.attackMethods.get(0).init(1, 2);
        this.attackMethods.get(1).init(1, 2);
        this.attackMethods.get(2).init(1, 1);
        this.attackMethods.get(3).init(0, 0);
        this.attackMethods.get(4).init(0, 0);
        this.attackMethods.get(5).init(0, 0);
        this.attackMethods.get(6).init(0, 1);
        this.match_num = 0;
        
        this.atkHeats = new double[this.attackMethods.size()][10][10][5];
    }

    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    @Override
    public Coordinate makeAttack() {
        this.turnNum++;
        Coordinate shot = new Coordinate(0, 0);
        
        if( this.randShots > 0 ){
        	this.randShots--;
        	int ind = rGen.nextInt(this.firstShots.size());
        	shot = this.firstShots.remove(ind);
        	this.lastShot = shot;
        	this.myMatchShots[shot.getX()][shot.getY()] = true;
        	return shot;
        }
        
        double[][] turnHeat = new double[10][10];
        for (int s = 4; s >= 0; s--) {
            if (this.shipsAlive[s]) {
                ArrayList<ZonePlacement> places = this.theirPlacements.get(s);
                for (ZonePlacement p : places) {
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
        	
        	for( ZoneAttackType pat : this.attackMethods){
            	pat.shotHere( false, 0, this.lastShot );
            }
            for (int s = 0; s < 5; s++) {
                if (this.shipsAlive[s]) {
                    ArrayList<ZonePlacement> places = this.theirPlacements.get(s);
                    ArrayList<ZonePlacement> bads = new ArrayList<ZonePlacement>();
                    for (ZonePlacement p : places) {
                        if (p.contains(this.lastShot)) {
                            bads.add(p);
                        }
                    }
                    places.removeAll(bads);
                }
            }
        } else {
        	
        	for( int q = 0; q < this.attackMethods.size(); q++){
        		double val = this.atkHeats[q][this.lastShot.getX()][this.lastShot.getY()][shipMod];
        		this.lastTenVal[q][shipMod] -= this.lastTen[this.lastTenIdx[shipMod]][q][shipMod];
            	this.lastTenVal[q][shipMod] += val;
            	this.lastTen[this.lastTenIdx[shipMod]][q][shipMod] = val;
            	this.attackMethods.get(q).shotHere(true, shipMod, this.lastShot);
        	}
        	this.lastTenIdx[shipMod] = (this.lastTenIdx[shipMod] + 1) % 10;
        	this.hitTheirShips[lastShot.getX()][lastShot.getY()] ++;
            for (int s = 0; s < 5; s++) {
                if (this.shipsAlive[s] && s != shipMod) {
                    ArrayList<ZonePlacement> places = this.theirPlacements.get(s);
                    ArrayList<ZonePlacement> bads = new ArrayList<ZonePlacement>();
                    for (ZonePlacement p : places) {
                        if (p.contains(this.lastShot)) {
                            bads.add(p);
                        }
                    }
                    places.removeAll(bads);
                } else if (this.shipsAlive[s] && s == shipMod) {
                    ArrayList<ZonePlacement> places = this.theirPlacements.get(s);
                    ArrayList<ZonePlacement> bads = new ArrayList<ZonePlacement>();
                    for (ZonePlacement p : places) {
                        if (!p.contains(this.lastShot)) {
                            bads.add(p);
                        }
                    }
                    places.removeAll(bads);
                }
            }
            if( result / 10 == 2 ){
            	this.shipsAlive[shipMod] = false;
            }
        }
    }

    @Override
    public void opponentAttack(Coordinate coord) {
    	if( myFleet.getLastAttackValue() != MISS ){
    		shipWasHit[ myFleet.getLastAttackValue() % 10 ] = true;
    	}
    	this.redPlace.shotAt(coord);
    }

    @Override
    public void resultOfGame(int result) {
        this.wasWin = ( result == WON );
        if( !wasWin ){
        	for( int i = 0; i < 5; i++){
        		if(shipsAlive[i] && (theirPlacements.get(i).size() < 3) ){
        			for( ZonePlacement p : theirPlacements.get(i)){
        				for( Coordinate c: p.coords){
        					int x = c.getX();
        					int y = c.getY();
        					if( !this.myMatchShots[x][y] ){
        						for( int q = 0; q < this.attackMethods.size(); q++){
        			        		double val = this.atkHeats[q][x][y][i];
        			        		this.lastTenVal[q][i] -= this.lastTen[this.lastTenIdx[i]][q][i];
        			            	this.lastTenVal[q][i] += val;
        			            	this.lastTen[this.lastTenIdx[i]][q][i] = val;
        			            	this.attackMethods.get(q).shotHere(true, i, c);
        			        	}
        						this.myMatchShots[x][y] = true;
        					}
        				}
        			}
        		}
        	}
        }
    }
	
	private class ZonePlacement {

        public Coordinate[] coords;
        public double score = 0;
        public double scoreTwo = 0;
        public int[] shipLength = {2, 3, 3, 4, 5};
        public int direc;

        ZonePlacement(int direc, int type, Coordinate loc) {
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
	
	/***********************************************************************
	 * 
	 * A tree object that keeps track of sequences of indexes stored as shorts.
	 * The tree will grow and link nodes together as they are used in a placement.
	 * It also keep track of the last number of turns that link ended in.
	 * 
	 * @author John
	 *
	 **********************************************************************/
	private class ZoneDist{
		
		public ArrayList<ZonePlacement[]> placements;
		public int[][][] shipNumbers;
		public int[] lastInd;
		private int[][] shipInd;
		private int[][] turnsToHit;
		private int[][] theirShots;
		private Fleet[] fleets;
		private Fleet gameFleet;
		public int numFleets = 4;
		private int turnNum;
		public int[] used;
		
		public ZoneDist(){
			//create levels to store ship config ind's
			used = new int[numFleets];
			shipNumbers = new int[5][10][10];
			lastInd = new int[5];
			shipInd = new int[numFleets][5];
			turnsToHit = new int[numFleets][5];
			fleets = new Fleet[numFleets];
			theirShots = new int[10][10];
			refresh();
			remakeFleets();
			turnNum = 0;
		}
		
		public void shotAt(Coordinate c){
			turnNum++;
			this.theirShots[c.getX()][c.getY()]++;
			for(int i = 0; i < numFleets; i++ ){
				int atk = fleets[i].attacked(c);
				if( atk != MISS && atk != DEFEATED){
					int ship = atk % 10;
					if( turnsToHit[i][ship] == 100){
						turnsToHit[i][ship] = turnNum;
					}
				}
			}
		}
		//gets a fleet with the given ship and index
		public void getFleet(boolean[] wasHit){
			
			turnNum = 0;
			gameFleet = new Fleet();
			for( int k = 0; k < 5; k++){
				if( !wasHit[k] ){
					ZonePlacement sp = placements.get(k)[lastInd[k]];
					gameFleet.placeShip(sp.coords[0], sp.direc, k);
				}
			}
						
			
			for( int i = 0; i < 5; i++){
				if( wasHit[i] ){				
					int best = 0;
					int ind = 0;
					for( int j = 0; j < numFleets; j++){
						if( turnsToHit[j][i] > best){
							best = turnsToHit[j][i];
							ind = j;
						}
					}
					if(gameFleet.placeShip(placements.get(i)[shipInd[ind][i]].coords[0], placements.get(i)[shipInd[ind][i]].direc, i)){
						used[ind]++;
						lastInd[i] = shipInd[ind][i];
					}else{
						used[ind]++;
						lastInd[i] = getShip(i, ind, gameFleet);
					}
				}
			}
			
			for( int k = 0; k < 5; k++){
				for( Coordinate c: placements.get(k)[lastInd[k]].coords ){
					shipNumbers[k][c.getX()][c.getY()]++;
				}
			}
			
			if(!gameFleet.isFleetReady()){
				System.out.println("mod not ready");
			}
			myFleet = gameFleet;
		}
		//gets a random non-used fleet; 
		public void getFleet(){
			turnNum = 0;
			gameFleet = new Fleet();
			
			for( int i = 0; i < 5; i++){
				int best = 0;
				int ind = 0;
				for( int j = 0; j < numFleets; j++){
					if( turnsToHit[j][i] > best){
						best = turnsToHit[j][i];
						ind = j;
					}
				}
				if(gameFleet.placeShip(placements.get(i)[shipInd[ind][i]].coords[0], placements.get(i)[shipInd[ind][i]].direc, i)){
					used[ind]++;
					lastInd[i] = shipInd[ind][i];
				}else{
					used[ind]++;
					lastInd[i] = getShip(i, ind, gameFleet);
				}
			}	
			
			for( int k = 0; k < 5; k++){
				for( Coordinate c: placements.get(k)[lastInd[k]].coords ){
					shipNumbers[k][c.getX()][c.getY()]++;
				}
			}
			
			if(!gameFleet.isFleetReady()){
				System.out.println("reg not ready");
			}
			myFleet = gameFleet;
		}
		
		public int getShip(int shipMod, int place, Fleet fl){
			int ret = 0;
			switch (place){
			case 0: ret = randomPlace(shipMod, fl); break;
			case 1: ret = disPlace(shipMod, fl); break;
			case 2: ret = secondPlace(shipMod, fl); break;
			case 3: ret = ratePlace(shipMod, fl); break;
			default: ret = disPlace(shipMod, fl); break;
			}
			return ret;
		}
		
		public int randomPlace( int shipMod, Fleet fl ){
			int max = ( 20 * ( 11 - shipLength[shipMod] ) );
			int ind = rGen.nextInt(max);
			ZonePlacement sp = placements.get(shipMod)[ind];
			
			while(!fl.placeShip( sp.coords[0], sp.direc, shipMod) ){
				ind = rGen.nextInt(max);
				sp = placements.get(shipMod)[ind];
			}
			
			return ind;
		}
		
		public int secondPlace( int shipMod, Fleet fl ){
			int smallest = Integer.MAX_VALUE;
			int ind = 0;
			int secondPlaceInd = 0;
			int placeInd = 0;
			ZonePlacement best = placements.get(shipMod)[0];
			ZonePlacement secondBest = placements.get(shipMod)[0];
			for( ZonePlacement sp : placements.get(shipMod)){				
				if( sp.score <= smallest ){
					boolean ok = true;
					for(Coordinate c: sp.coords){
						for( Ship p: fl.fleet){
							if( p != null && p.isOnShip(c)){
								ok = false;
								break;
							}
						}
					}
					if( ok ){
						if( smallest == Integer.MAX_VALUE){
							secondBest = sp;
							smallest = (int) sp.score;
							secondPlaceInd = ind;
							placeInd = ind;
							best = sp;
						}
						else{
							secondBest = best;
							smallest = (int) sp.score;
							secondPlaceInd = placeInd;
							placeInd = ind;
							best = sp;
						}
					}
				}
				ind++;
			}
			fl.placeShip( secondBest.coords[0], secondBest.direc, shipMod);
			
			return secondPlaceInd;
		}

		public int disPlace( int shipMod, Fleet fl ){
			int ind = 0;
			int placeInd = 0;
			int smallest = Integer.MAX_VALUE;
			ZonePlacement best = new ZonePlacement(0, 0, new Coordinate(0, 0) );
			for( ZonePlacement sp : placements.get(shipMod)){
				if( sp.score < smallest ){
					boolean ok = true;
					for(Coordinate c: sp.coords){
						for( Ship p: fl.fleet){
							if( p != null && p.isOnShip(c)){
								ok = false;
								break;
							}
						}
					}
					if( ok ){
						smallest = (int) sp.score;
						best = sp;
						placeInd = ind;
					}
				}
				ind++;
			}
			fl.placeShip( best.coords[0], best.direc, shipMod);
			
			return placeInd;
		}	
		
		public int ratePlace( int shipMod, Fleet fl ){
			int ind = 0;
			int placeInd = 0;
			int smallest = Integer.MAX_VALUE;
			ZonePlacement best = new ZonePlacement(0, 0, new Coordinate(0, 0) );
			for( ZonePlacement sp : placements.get(shipMod)){
				if( sp.scoreTwo < smallest ){
					boolean ok = true;
					for(Coordinate c: sp.coords){
						for( Ship p: fl.fleet){
							if( p != null && p.isOnShip(c)){
								ok = false;
								break;
							}
						}
					}
					if( ok ){
						smallest = (int) sp.scoreTwo;
						best = sp;
						placeInd = ind;
					}
				}
				ind++;
			}
			fl.placeShip( best.coords[0], best.direc, shipMod);			
			return placeInd;			
		}
		public void remakeFleets(){
			//remake all fleets
			for( int p = 0; p < numFleets; p++){
				fleets[p] = new Fleet();
				for(int q = 0; q < 5; q++){
					shipInd[p][q] = getShip(q, p, fleets[p]);
					turnsToHit[p][q] = 100;
				}
			}
		}
		public void refresh(){
			placements = new ArrayList<ZonePlacement[]>();
			for (int s = 0; s < 5; s++) {
	            int shipLen = shipLength[s];
	            ZonePlacement[] places = new ZonePlacement[( 20 * ( 11 - shipLen ) )];
	            int ind = 0;
	            for (int i = 0; i < 11 - shipLen; i++) {
	                for (int j = 0; j < 10; j++) {
	                	ZonePlacement p = new ZonePlacement(0, s, new Coordinate(i, j));
	                	ZonePlacement p2 = new ZonePlacement(1, s, new Coordinate(j, i));
	                	
	                    double score = 0;
	                    double score2 = 0;
	                    
	                    double scoreA = 0;
	                    double scoreA2 = 0;

	                    for (int k = 0; k < shipLen; k++) {
	                    	scoreA = theirShots[i + k][j];
	                    	scoreA2 = theirShots[j][i + k];
	                        score += shipNumbers[s][i + k][j];
	                        score2 += shipNumbers[s][j][i + k];
	                    }
	                    p.score = score;
	                    p.scoreTwo = scoreA;
	                    p2.score = score2;
	                    p2.scoreTwo = scoreA2;
	                    
	                    places[ind] = p;
	                    ind++;
	                    places[ind] = p2;
	                    ind++;
	                }
	            }
	            //System.out.println("Mod "+s+" ships: "+places.size());
	            placements.add(places);
	        }
		}
	}
	
	private abstract class ZoneAttackType{
		
		public boolean				started;
		public double 				accuracy;
		public double[][][][] 		shipDoubleArrays;
		public int[][][][] 			shipIntArrays;

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
