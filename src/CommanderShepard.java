import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CommanderShepard implements Captain {

	protected final int[] 						shipLength = {2,3,3,4,5};
    protected Fleet 							myFleet;
    private boolean[] 							shipsAlive;
    private boolean[][] 						myMatchShots;
    private boolean[]							shipWasHit;
    private String 								lastOpponent = "";
    private Coordinate 							lastShot;
    private ArrayList<ArrayList<Coordinate>> 	hitShips;
    private ArrayList<ArrayList<ShepardPlacement>> 	theirPlacements;
    private int[]								accuracy; //0 is hits 1 is shots
    private double[][][]						lastTen;
    private double[][]							lastTenVal;
    private int[]								lastTenIdx;
    private boolean 							wasWin;
    private int 								turnNum;
    private ArrayList<ShepardAttackType>		attackMethods;
    private Random								rGen;
    private int									match_num;
    private int[]								atk_used;
    private ShepardDist							redPlace;

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {

    	this.rGen = new Random();
        this.theirPlacements = new ArrayList<ArrayList<ShepardPlacement>>();
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
        //this.myFleet = randomFleet();
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
        redPlace.refresh();
        boolean placed = false;
        if( this.wasWin){
        	for(int q = 0; q < 5; q++){
        		if(!shipWasHit[q]){
        			redPlace.getFleet(q, redPlace.lastInd[q]);
        			placed = true;
        			break;
        		}
        	}
        }
        if(!placed){
        	redPlace.getFleet();
        }
        
        shipWasHit = new boolean[]{ false, false, false, false, false};
    }
    
    private void createPlacements(double[][][] scorer, ArrayList<ArrayList<ShepardPlacement>> list) {
        for (int s = 0; s < 5; s++) {
            int shipLen = this.shipLength[s];
            ArrayList<ShepardPlacement> places = new ArrayList<ShepardPlacement>();
            for (int i = 0; i < 11 - shipLen; i++) {
                for (int j = 0; j < 10; j++) {
                	ShepardPlacement p = new ShepardPlacement(0, s, new Coordinate(i, j));
                	ShepardPlacement p2 = new ShepardPlacement(1, s, new Coordinate(j, i));
                	
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
            //System.out.println("Mod "+s+" ships: "+places.size());
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
        this.redPlace = new ShepardDist();
        this.wasWin = false;
        this.lastOpponent = opponent;
        /*---------------------Attack Patterns-----------
         * 
         */
        this.attackMethods = new ArrayList<ShepardAttackType>();
        /*---------------------Attack Pattern 1-----------
         * standard shots/misses
         */
        this.attackMethods.add(new ShepardAttackType(){

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
         
        this.attackMethods.add(new ShepardAttackType(){

        	// int 0 hits, int 1 shots, double 1 heat 
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
        */
        /*---------------------Attack Pattern 3-----------
         * least shot i think
         */
        this.attackMethods.add(new ShepardAttackType(){

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
         
        this.attackMethods.add(new ShepardAttackType(){

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
        */
        this.lastTen = new double[10][attackMethods.size()][5];
        this.lastTenVal = new double[attackMethods.size()][5];
        this.atk_used = new int[attackMethods.size()];
        this.lastTenIdx = new int[5];
    	this.accuracy = new int[attackMethods.size()];
        /*
         *Init attack patterns 
         */            
        this.attackMethods.get(0).init(1, 2);
        //this.attackMethods.get(1).init(1, 2);
        this.attackMethods.get(1).init(1, 1);
        //this.attackMethods.get(3).init(0, 0);
        this.match_num = 0;
    }

    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    @Override
    public Coordinate makeAttack() {
        this.turnNum++;
        /*
        Coordinate shot = new Coordinate(0, 0);
        
        double[][] turnHeat = new double[10][10];
        for (int s = 4; s >= 0; s--) {
            if (this.shipsAlive[s]) {
                ArrayList<ShepardPlacement> places = this.theirPlacements.get(s);
                for (ShepardPlacement p : places) {
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
        */
        return new Coordinate(1,1);
    }

    @Override
    public void resultOfAttack(int result) {
    	/*
    	int shipMod = result % 10;
        if (result == MISS || result == DEFEATED) {
        	
        	for( ShepardAttackType pat : this.attackMethods){
            	pat.shotHere( false, 0, this.lastShot );
            }
            for (int s = 0; s < 5; s++) {
                if (this.shipsAlive[s]) {
                    ArrayList<ShepardPlacement> places = this.theirPlacements.get(s);
                    ArrayList<ShepardPlacement> bads = new ArrayList<ShepardPlacement>();
                    for (ShepardPlacement p : places) {
                        if (p.contains(this.lastShot)) {
                            bads.add(p);
                        }
                    }
                    places.removeAll(bads);
                }
            }
        } else {
        	this.accuracy[0]++;
        	
        	for( ShepardAttackType pat : this.attackMethods){
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
                    ArrayList<ShepardPlacement> places = this.theirPlacements.get(s);
                    ArrayList<ShepardPlacement> bads = new ArrayList<ShepardPlacement>();
                    for (ShepardPlacement p : places) {
                        if (p.contains(this.lastShot)) {
                            bads.add(p);
                        }
                    }
                    places.removeAll(bads);
                } else if (this.shipsAlive[s] && s == shipMod) {
                    ArrayList<ShepardPlacement> places = this.theirPlacements.get(s);
                    ArrayList<ShepardPlacement> bads = new ArrayList<ShepardPlacement>();
                    for (ShepardPlacement p : places) {
                        if (!p.contains(this.lastShot)) {
                            bads.add(p);
                        }
                    }
                    places.removeAll(bads);
                }
            }

        }
        */
    }

    @Override
    public void opponentAttack(Coordinate coord) {
    	if( myFleet.getLastAttackValue() != MISS ){
    		shipWasHit[ myFleet.getLastAttackValue() % 10 ] = true;
    	}
    }

    @Override
    public void resultOfGame(int result) {
        this.wasWin = ( result == WON );
    }
	
	private class ShepardPlacement {

        public Coordinate[] coords;
        public double score = 0;
        public int[] shipLength = {2, 3, 3, 4, 5};
        public int direc;

        ShepardPlacement(int direc, int type, Coordinate loc) {
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
	private class ShepardDist{
		
		private ArrayList<ShepardPlacement[]> placements;
		private int[][][] shipNumbers;
		int[] lastInd;
		
		public ShepardDist(){
			//create levels to store ship config ind's
			shipNumbers = new int[5][10][10];
			lastInd = new int[5];
		}
		
		//gets a fleet with the given ship and index
		public void getFleet(int shipMod, int ind){
			
			myFleet = new Fleet();
			ShepardPlacement sp = placements.get(shipMod)[ind];
			myFleet.placeShip(sp.coords[0], sp.direc, shipMod);
			
			for( Coordinate c : sp.coords ){
				shipNumbers[shipMod][c.getX()][c.getY()]++;
			}
			
			for( int i = 0; i < 5; i++){
				if( i == shipMod ){
					continue;
				}
				int seed = rGen.nextInt(10);
				switch (seed){
				case 1: randomPlace(i); break;
				case 2: secondPlace(i); break;
				default: disPlace(i); break;
				}
			}
			
			if(!myFleet.isFleetReady()){
				System.out.println("mod not ready");
			}
		}
		//gets a random non-used fleet; 
		public void getFleet(){
			myFleet = new Fleet();
			
			for( int i = 0; i < 5; i++){
				int seed = rGen.nextInt(10);
				switch (seed){
				//case 1: randomPlace(i); break;
				//case 2: secondPlace(i); break;
				case 1:
				case 2:
				case 3:
				case 4: randomPlace(i); break;
				default: disPlace(i); break;
				}
			}	
			
			if(!myFleet.isFleetReady()){
				System.out.println("reg not ready");
			}
		}
		
		public void randomPlace( int shipMod ){
			int max = ( 20 * ( 11 - shipLength[shipMod] ) );
			int ind = rGen.nextInt(max);
			ShepardPlacement sp = placements.get(shipMod)[ind];
			
			while(!myFleet.placeShip( sp.coords[0], sp.direc, shipMod) ){
				ind = rGen.nextInt(max);
				sp = placements.get(shipMod)[ind];
			}
			for( Coordinate c : placements.get(shipMod)[ind].coords ){
				shipNumbers[shipMod][c.getX()][c.getY()]++;
			}
			lastInd[shipMod] = ind;
		}
		
		public void secondPlace( int shipMod ){
			int smallest = Integer.MAX_VALUE;
			int ind = 0;
			int secondPlaceInd = 0;
			int placeInd = 0;
			ShepardPlacement best = placements.get(shipMod)[0];
			ShepardPlacement secondBest = placements.get(shipMod)[0];
			for( ShepardPlacement sp : placements.get(shipMod)){				
				if( sp.score <= smallest ){
					boolean ok = true;
					for(Coordinate c: sp.coords){
						for( Ship p: myFleet.fleet){
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
			myFleet.placeShip( secondBest.coords[0], secondBest.direc, shipMod);
			for( Coordinate c : secondBest.coords ){
				shipNumbers[shipMod][c.getX()][c.getY()]++;
			}
			lastInd[shipMod] = secondPlaceInd;
		}

		public void disPlace( int shipMod ){
			int ind = 0;
			int placeInd = 0;
			int smallest = Integer.MAX_VALUE;
			ShepardPlacement best = new ShepardPlacement(0, 0, new Coordinate(0, 0) );
			for( ShepardPlacement sp : placements.get(shipMod)){
				if( sp.score < smallest ){
					boolean ok = true;
					for(Coordinate c: sp.coords){
						for( Ship p: myFleet.fleet){
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
			myFleet.placeShip( best.coords[0], best.direc, shipMod);
			for( Coordinate c : best.coords ){
				shipNumbers[shipMod][c.getX()][c.getY()]++;
			}
			lastInd[shipMod] = placeInd;
		}	
		
		public void refresh(){
			placements = new ArrayList<ShepardPlacement[]>();
			for (int s = 0; s < 5; s++) {
	            int shipLen = shipLength[s];
	            ShepardPlacement[] places = new ShepardPlacement[( 20 * ( 11 - shipLen ) )];
	            int ind = 0;
	            for (int i = 0; i < 11 - shipLen; i++) {
	                for (int j = 0; j < 10; j++) {
	                	ShepardPlacement p = new ShepardPlacement(0, s, new Coordinate(i, j));
	                	ShepardPlacement p2 = new ShepardPlacement(1, s, new Coordinate(j, i));
	                	
	                    double score = 0;
	                    double score2 = 0;

	                    for (int k = 0; k < shipLen; k++) {
	                        score += shipNumbers[s][i + k][j];
	                        score2 += shipNumbers[s][j][i + k];
	                    }
	                    p.score = score;
	                    p2.score = score2;
	                    
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
	
	private abstract class ShepardAttackType{
		
		public boolean				started;
		public double 				accuracy;
		public double[][][][] 		shipDoubleArrays;
		public int[][][][] 			shipIntArrays;
		
		public ShepardAttackType(){
			
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
