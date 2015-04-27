import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class CaptainDo_Urden implements Captain {

	private 									Random rgen;
	protected int[] 							shipLength = {2,3,3,4,5};
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
    private ArrayList<ArrayList<Placement>> 	theirPlacements;
    private int[]								accuracy; //0 is hits 1 is shots
    private double[][][]						lastTen;
    private double[][]							lastTenVal;
    private int[]								lastTenIdx;
    private boolean 							wasWin;
    private int 								turnNum;
    private ArrayList<AttackType>				attackMethods;
    private SuperMegaShipPlacer_3000 			sp;
    private Random								rGen;
    private int									match_num;
    private int[]								atk_used;
    private final int							atk_strats = 4;

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {

        this.theirPlacements = new ArrayList<ArrayList<Placement>>();
        this.rgen = new Random();
        this.myFleet = new Fleet();
        this.shipsAlive = new boolean[]{true, true, true, true, true};
        this.myMatchShots = new boolean[10][10];
        this.hitShips = new ArrayList<ArrayList<Coordinate>>();


        for (int i = 0; i < 10; i++) {
            Arrays.fill(this.myMatchShots[i], false);
        }

        if (!opponent.equals(this.lastOpponent)) {
        	       	
            this.lastTen = new double[10][atk_strats][5];
            this.lastTenVal = new double[atk_strats][5];
            this.atk_used = new int[atk_strats];
            this.lastTenIdx = new int[5];
        	this.accuracy = new int[atk_strats];
            this.wasWin = false;
            this.their_hits = new int[10][10];
            this.their_misses = new int [10][10];
            this.their_hit_shots = 100;
            this.their_miss_shots = 100;
            this.lastOpponent = opponent;
            for (int j = 0; j < 100; j++) {
            this.their_hits[j % 10][j / 10] = 1;
            this.their_misses[j % 10][j / 10] = 1;
            }
	        /*---------------------Attack Patterns-----------
	         * 
	         */
	        this.attackMethods = new ArrayList<AttackType>();
            this.attackMethods.add(new AttackType(){

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
            this.attackMethods.add(new AttackType(){

            	/* int 0 hits, int 1 shots, double 1 heat */
            	private Coordinate[] oldShots = new Coordinate[500];
            	private boolean[]oldHit = new boolean[500];
            	private int[] shipHitMod = new int[500];
            	private int shotIdx = 0;
            	
				@Override
				public void shotHere(boolean wasHit, int shipMod, Coordinate c) {
					
					if( !started ){
						Arrays.fill(oldHit, false);
						 for (int j = 0; j < 100; j++) {

							 shipIntArrays[j % 10][j / 10][0][1] = 6;				                
			                 for (int k = 0; k < 5; k++) {
			                	 oldShots[j + (k*100)] = new Coordinate(j % 10,j / 10);
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
            this.attackMethods.add(new AttackType(){

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
            this.attackMethods.add(new AttackType(){

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
	        /*
	         *Init attack patterns 
	         */            
            this.attackMethods.get(0).init(1, 2);
            this.attackMethods.get(1).init(1, 2);
            this.attackMethods.get(2).init(1, 2);
            this.attackMethods.get(3).init(1, 1);
            this.match_num = 0;
            sp = new SuperMegaShipPlacer_3000();
        }
        else{
        	this.match_num++;
        }
        /*
        if( (this.match_num % 1000) == 0){
        	for( int i = 0; i < this.attackMethods.size(); i++){
        		System.out.println("Accuracy "+i+" = " + this.lastTenVal[i]);
        	}
        }
		*/
        double best_val = 0;
        int idx = 0;
        double turn_heat[][][] = new double[10][10][5];
        for( int j = 0; j < 5; j++){
        	idx = 0;
        	best_val = 0;
        	for( int k=0; k < atk_strats; k++){
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

        rGen = new Random();
        this.myFleet = sp.getNextPlacement(this.wasWin, this.turnNum);
        //this.myFleet = getDistShipPlace();
        this.turnNum = 0;
        
        if( this.match_num == numMatches -1){
        	for( int i = 0; i < this.atk_used.length; i++){
        		System.out.println("Used Attack "+ i +" "+(float) this.atk_used[i]/(5*(float)this.match_num)+"% of the time.");
        	}
        }
    }

	private Fleet randomPlace(){
    	Fleet fl = new Fleet();
    	while (!fl.placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), AIRCRAFT_CARRIER)) {
        }
        while (!fl.placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), DESTROYER)) {
        }
        while (!fl.placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), SUBMARINE)) {
        }
        while (!fl.placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), BATTLESHIP)) {
        }
        while (!fl.placeShip(rGen.nextInt(10), rGen.nextInt(10), rGen.nextInt(2), PATROL_BOAT)) {
        }
        return fl;
    }
    
    private void createPlacements(double[][][] scorer, ArrayList<ArrayList<Placement>> list) {
        for (int s = 0; s < 5; s++) {
            int shipLen = this.shipLength[s];
            ArrayList<Placement> places = new ArrayList<Placement>();
            for (int i = 0; i < 11 - shipLen; i++) {
                for (int j = 0; j < 10; j++) {
                    Placement p = new Placement(0, s, new Coordinate(i, j));
                    double score = 0;

                    for (int k = 0; k < shipLen; k++) {
                        score += scorer[i + k][j][s];
                    }
                    p.score = score;
                    places.add(p);
                }
            }
            for (int j = 0; j < 11 - shipLen; j++) {
                for (int i = 0; i < 10; i++) {
                    Placement p = new Placement(1, s, new Coordinate(i, j));
                    double score = 0;

                    for (int k = 0; k < shipLen; k++) {
                        score += scorer[i][j + k][s];
                    }
                    p.score = score;
                    places.add(p);
                }
            }
            list.add(places);
        }
    }
    
    private void createPlacements(double[][][] scorer, double[][][] scorer_2, ArrayList<ArrayList<Placement>> list) {
        for (int s = 0; s < 5; s++) {
            int shipLen = this.shipLength[s];
            ArrayList<Placement> places = new ArrayList<Placement>();
            for (int i = 0; i < 11 - shipLen; i++) {
                for (int j = 0; j < 10; j++) {
                    Placement p = new Placement(0, s, new Coordinate(i, j));
                    double score = 0;

                    for (int k = 0; k < shipLen; k++) {
                        score += scorer[i + k][j][s];
                        score += scorer_2[i+k][j][s];
                    }
                    p.score = score;
                    places.add(p);
                }
            }
            for (int j = 0; j < 11 - shipLen; j++) {
                for (int i = 0; i < 10; i++) {
                    Placement p = new Placement(1, s, new Coordinate(i, j));
                    double score = 0;

                    for (int k = 0; k < shipLen; k++) {
                        score += scorer[i][j + k][s];
                        score += scorer_2[i][j + k][s];
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
        this.turnNum++;
        Coordinate shot = new Coordinate(0, 0);
        //ArrayList<Coordinate> possibles = new ArrayList<Coordinate>();
        /*
        double best = 0;
        int best_idx_mod =0;
        int best_idx_ship = 0;
        if (!this.hitShips.isEmpty()) {
            for (ArrayList<Coordinate> ship : this.hitShips) {
                int shipMod = ship.get(0).getX();
                ArrayList<Placement> places = this.theirPlacements.get(shipMod);
                for (Placement p : places) {
                    if(p.score > best){
                    	best = p.score;
                    	best_idx_mod = shipMod;
                    	best_idx_ship = places.indexOf(p);
                    }
                }
            }
        }
		*/
        double[][] turnHeat = new double[10][10];
        for (int s = 4; s >= 0; s--) {
            if (this.shipsAlive[s]) {
                ArrayList<Placement> places = this.theirPlacements.get(s);
                for (Placement p : places) {
                    for (Coordinate c : p.coords) {
                        turnHeat[c.getX()][c.getY()] += p.score;
                    }
                }
            }
        }
        int x = 0;
        int y = 0;
        double best = 0;
        //if (possibles.isEmpty()) {
            for (int i = 0; i < 100; i++) {
                if (!this.myMatchShots[i % 10][i / 10] && turnHeat[i % 10][i / 10] >= best) {
                    best = turnHeat[i % 10][i / 10];
                    x = i % 10;
                    y = i / 10;
                }
            }
            /*} else {
            for (Coordinate c : possibles) {
                if (!this.myMatchShots[c.getX()][c.getY()] && turnHeat[c.getX()][c.getY()] >= best) {
                    best = turnHeat[c.getX()][c.getY()];
                    x = c.getX();
                    y = c.getY();
                }
            }
        }
        */
        
        shot = new Coordinate(x,y);
        this.lastShot = shot;
        this.myMatchShots[shot.getX()][shot.getY()] = true;
        return shot;
    }

    @Override
    public void resultOfAttack(int result) {
        //this.myShots[this.lastShot.getX()][this.lastShot.getY()]++;
    	int shipMod = result % 10;
        if (result == MISS || result == DEFEATED) {
        	
        	for( AttackType pat : this.attackMethods){
            	pat.shotHere( false, 0, this.lastShot );
            }
            for (int s = 0; s < 5; s++) {
                if (this.shipsAlive[s]) {
                    ArrayList<Placement> places = this.theirPlacements.get(s);
                    ArrayList<Placement> bads = new ArrayList<Placement>();
                    for (Placement p : places) {
                        if (p.contains(this.lastShot)) {
                            bads.add(p);
                        }
                    }
                    places.removeAll(bads);
                }
            }
        } else {
        	this.accuracy[0]++;
        	
        	for( AttackType pat : this.attackMethods){
            	double val = pat.getHeat()[this.lastShot.getX()][this.lastShot.getY()][shipMod];
            	//System.out.println(this.lastTen[this.lastTenIdx][this.attackMethods.indexOf(pat)]);
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
                    ArrayList<Placement> places = this.theirPlacements.get(s);
                    ArrayList<Placement> bads = new ArrayList<Placement>();
                    for (Placement p : places) {
                        if (p.contains(this.lastShot)) {
                            bads.add(p);
                        }
                    }
                    places.removeAll(bads);
                } else if (this.shipsAlive[s] && s == shipMod) {
                    ArrayList<Placement> places = this.theirPlacements.get(s);
                    ArrayList<Placement> bads = new ArrayList<Placement>();
                    for (Placement p : places) {
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
        this.wasWin = result == WON;
        /* afterloss get data
        if (!this.wasWin) {
            for (ArrayList<Coordinate> ship : this.hitShips) {
                int ind = ship.get(0).getX();
                boolean[][][] stop = new boolean[10][10][5];
                for (Placement p : this.theirPlacements.get(ind)) {
                    for (Coordinate c : p.coords) {
                        if (!this.myMatchShots[c.getX()][c.getY()] && !stop[c.getX()][c.getY()][ind]) {
                            this.hitsHeat[c.getX()][c.getY()][ind]++;
                            stop[c.getX()][c.getY()][ind] = true;
                        }
                    }
                }
            }
        }
        */
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
		private int							numberToGo;
		private float						avg_mets;
		private boolean						init;
		
		
		public SuperMegaShipPlacer_3000(){
			this.metricWeights = new float[]{ .2f, .2f, .1f, .1f, .2f, .2f};
			this.avg_mets = .167f;
			this.bins = new ArrayList<PlacementBin>();
			this.scores = new ArrayList<Integer>();
			this.numberBins = 2;
			this.matchRedist = 1000;
			this.numberMatch = 0;
			this.numberToGo = 0;
			this.init = true;
			
			//create 2 new bins with 10 random maps each
			//this should create a two bins to determine a good metric (well start determining anyway)
			float[] rmeterics = new float[]{.5f,.5f,1f,.5f,.85f,.5f};
			PlacementBin b1 = new PlacementBin(500,rmeterics);
			this.bins.add(b1);
			this.scores.add(50);
			
			rmeterics = new float[]{.5f,.5f,0f,.5f,.5f,.85f};
			
			PlacementBin b2 = new PlacementBin(500,rmeterics);
			this.bins.add(b2);
			this.scores.add(49);			
			System.out.println("done with init");
		}
		public Fleet getNextPlacement(boolean won, int turns_taken){
			this.numberMatch++;
			Fleet ret = new Fleet();		
			ShipMap place;
			
			this.total_turns += turns_taken;
			
			
			if( won){
				//replay same ship pattern	
				this.consec_wins++;
				this.over_games++;
				place = this.last;
				
			}
			else{
				this.bins.get(0).rankPlace(turns_taken+this.consec_wins);
				this.over_games += 2;		
				place = this.bins.get(0).getShipMap();
				this.last = place;
				if( this.numberMatch >= matchRedist ){
					System.out.println("Step 2");
					reThink();
				}
				this.consec_wins = 0;
			}
			
			for( int i = 0; i < 5; i++){
				ret.placeShip(place.ships[i].c, place.ships[i].direction, i);
			}
			return ret;					
		}

		private void reThink(){

			System.out.println("Top Scores are: "+this.scores.get(0)+" and "+this.scores.get(1));
			if(this.scores.get(0) > 4 +this.scores.get(1)){
				this.scores.remove(1);
				this.scores.add(1, this.scores.get(0)-4);
			}
			//TODO
			if(this.scores.size() != this.bins.size()){
				System.out.println("______VERY VERY BAD_______");
			}
			int new_score  = this.total_turns / this.over_games;
			//changing bins, see if another bin is better that the current
			int new_ind = getInd(new_score,0,this.scores.size());
			int old_score = this.scores.remove(0);
			System.out.println("bin with meterics "+printArray(this.bins.get(0).target_meterics)+" went from "+ old_score+" to "+new_score);
			this.scores.add(new_ind,new_score);
    		this.bins.add(new_ind, this.bins.remove(0));
			//evaluate how each bin is fairing compared with its meterics
			float[] importance = new float[6];
			//make each importance between 0 (none) and 1 (most)
			importance[0] = Math.abs( .5f - this.bins.get(new_ind).target_meterics[0]) * 2;
			importance[1] = Math.abs( .5f - this.bins.get(new_ind).target_meterics[1]) * 2;
			importance[2] = Math.abs( .5f - this.bins.get(new_ind).target_meterics[2]) * 2;
			importance[3] = Math.abs( .5f - this.bins.get(new_ind).target_meterics[3]) * 2;
			//base 4 and 5 off of others due to lack of any other good metric
			float weight = (importance[0] + importance[1] + importance[2] + importance[3]) / 2f;
			importance[4] = Math.abs( .5f - this.bins.get(new_ind).target_meterics[4]) * weight;
			importance[5] = Math.abs( .5f - this.bins.get(new_ind).target_meterics[5]) * weight;
			
			float[] temp = Arrays.copyOf(importance, 6);
			Arrays.sort(temp);
			
			float total = 0f;
			for(int i = 0; i < 6; i++){
				if( new_score < old_score ){				
					if(importance[i] == temp[0] || importance[i] == temp[1] || importance[i] == temp[2]){
						if( this.metricWeights[i] < .995f){
							this.metricWeights[i] += .005f;
						}					
					}
					else{
						if( this.metricWeights[i] > .005f){
							this.metricWeights[i] -= .005f;
						}
					}
				
				}
				else{
					if(importance[i] == temp[0] || importance[i] == temp[1] || importance[i] == temp[2]){
						if( this.metricWeights[i] > .005f){
							this.metricWeights[i] -= .005f;
						}				
					}
					else{
						if( this.metricWeights[i] < .995f){
							this.metricWeights[i] += .005f;
						}
					}
				}
				System.out.println("New Meteric weight "+i+" is "+this.metricWeights[i]);
				total += this.metricWeights[i];
			}
			
			this.avg_mets = total/ 6f;
			//set match redist to a good value. if the enemy is switching fast, go faster. if switching slow, use less bins. Figues this out from the drop of the bin score over the last dist period.
			this.numberToGo = (this.total_turns / this.over_games) * 256;
			this.matchRedist = this.numberMatch + this.numberToGo; //max is appox 20000
			//maybe set bin# to 1000000 / match redist as to not waste resources?
			int amount = 100000 / this.numberToGo;
			System.out.println("Setting bin# to: "+amount);
			setBins( amount );

			this.init = false;
			//maybe drop the worst bins. if creating more Use similarity and meteric weights. 
			//number of placements per bin should be .5 * the number of matches its expected to go
			this.over_games = 0;
			this.total_turns = 0;
		}
		
		private void setBins( int i ){			
			while( i > this.numberBins ){
				int bin_size = this.numberToGo/2;
				this.bins.get(0).setSize(bin_size);
				//create new meterics different from others;
				float[] newMets  = new float[6];
				
				//TODO create these meterics based off of good ones
				newMets[0] = rgen.nextFloat();
				newMets[1] = rgen.nextFloat();
				newMets[2] = rgen.nextFloat();	
				newMets[3] = rgen.nextFloat();	
				newMets[4] = rgen.nextFloat();
				newMets[5] = rgen.nextFloat();			
				
				PlacementBin b = new PlacementBin(bin_size, newMets);
				int score;
				if(this.init){
					score = 40;
				}
				else{
					score = this.scores.get(1+rgen.nextInt(this.bins.size()-1));
				}
				int place_ind = getInd(score,0,this.bins.size());

				this.bins.add(place_ind, b);
				this.scores.add(place_ind, score);		
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
				generateNew();
			}
			while( i < ships.size() ){
				ships.remove(ships.size() - 1);
				pscores.remove(this.pscores.size()-1);
			}
		}
		private void generateNew(){ 
			//TODO generate the map not randomly
			ShipMap sm1 = randomMap();
			int difs = getDifferences(sm1.metrics,this.target_meterics, .1f);
			int breaknum = 0;
			while( difs == 0){
				breaknum++;
				if(breaknum == 1000){
					System.out.println("cant get more of this meteric "+ printArray(this.target_meterics));
					this.ships.clear();
					this.target_meterics[0] = rgen.nextFloat();
					this.target_meterics[1] = rgen.nextFloat();
					this.target_meterics[2] = rgen.nextFloat();	
					this.target_meterics[3] = rgen.nextFloat();	
					this.target_meterics[4] = rgen.nextFloat();
					this.target_meterics[5] = rgen.nextFloat();
					breaknum = 0;
				}
				sm1 = randomMap();
				difs = getDifferences(sm1.metrics,this.target_meterics, .1f);
			}
			int scr = 45 + rgen.nextInt(10);
			int place = getPlaceInd(scr,0,this.pscores.size());
			ships.add(place,sm1);		
			this.pscores.add(place,scr);
		}
		//usually m2 is traget meterics
		private int getDifferences(float[] m1, float[] m2, float comp){
			int ret = 0;
			//TODO
			//int ind = 0;
			for( int n = 0; n < 6; n++){
				float diff = ( m2[n] > m1[n]) ? ( m2[n] - m1[n]) : ( m1[n] - m2[n]);
				if(diff > comp ){
					ret++;
				}
			}
			//System.out.println("worst INdex: "+ind);
			return ret;
		}
		private ShipMap randomMap(){
			ShipMap ret = new ShipMap();
			boolean[][] ships = new boolean[10][10];
			for( int i = 0; i < 5; i++){
				int ship_len = shipLength[i];
				boolean hor_place = rgen.nextBoolean();
				boolean placed = false;
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
							if(ships[x][y+j]){
								placed = false;
								break;
							}
						}
						if(placed){
							ret.ships[i] = new TinyShip(new Coordinate(x,y), 1);
							for( int j = 0; j < ship_len; j++){
								ships[x][y+j] = true;
							}
						}
					}
				}
			}
			ret.MetericMap(their_hits, their_misses, their_hit_shots, their_miss_shots);
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
		public TinyShip[] ships;		
		public float[] metrics;		
		
		public ShipMap(){
			this.ships = new TinyShip[5];
			this.metrics = new float[6];
		};
		
		public void MetericMap( int[][] enemy_hit, int[][] enemy_miss, int enemy_hits, int enemy_misses){

			boolean[][] map  = new boolean[10][10];
			boolean[][] nextTo  = new boolean[10][10];
			int total_x = 0;
			int total_y = 0;
			int hits = 0;
			int misses = 0;
			for( int i = 0; i < 5; i++){
				int ship_len = shipLength[i];
				int x = this.ships[i].c.getX();
				int y = this.ships[i].c.getY();
				
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
		this.metrics[4] = (float) total_x / 100f;
		this.metrics[5] = (float) total_y / 100f;
		this.metrics[0] = (float) hits / (float)enemy_hits;
		this.metrics[1] = (float) misses / (float)enemy_misses;
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
	
	private abstract class AttackType{
		
		public boolean				started;
		public double 				accuracy;
		public double[][][][] 		shipDoubleArrays;
		public int[][][][] 			shipIntArrays;
		
		public AttackType(){
			
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
