import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CaptainNolan implements Captain {

	protected final int[] 						shipLength = {2,3,3,4,5};
    protected Fleet 							myFleet;
    private boolean[] 							shipsAlive;
    private boolean[][] 						myMatchShots;
    private boolean[]							shipWasHit;
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
    private NolanTree							treeOfLife;

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
        
        boolean placed = false;
        if( this.wasWin){
        	for(int q = 0; q < 5; q++){
        		if(!shipWasHit[q]){
        			this.myFleet = treeOfLife.getFleet(q, treeOfLife.lastSeq[q]);
        			placed = true;
        			break;
        		}
        	}
        }
        if(!placed){
        	this.myFleet = treeOfLife.getFleet();
        }
        
        shipWasHit = new boolean[]{ false, false, false, false, false};
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
        this.treeOfLife = new NolanTree();
        this.wasWin = false;
        this.lastOpponent = opponent;
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
    	if( myFleet.getLastAttackValue() != MISS ){
    		shipWasHit[ myFleet.getLastAttackValue() % 10 ] = true;
    	}
    }

    @Override
    public void resultOfGame(int result) {
        this.wasWin = ( result == WON );
        this.treeOfLife.place( this.turnNum );
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
	
	/***********************************************************************
	 * 
	 * A tree object that keeps track of sequences of indexes stored as shorts.
	 * The tree will grow and link nodes together as they are used in a placement.
	 * It also keep track of the last number of turns that link ended in.
	 * 
	 * @author John
	 *
	 **********************************************************************/
	private class NolanTree{
		
		private ArrayList<List<NolanNode<Short>>> levels;
		short[] lastSeq;
		
		public NolanTree(){
			//create levels to store ship config ind's
			levels = new ArrayList<List<NolanNode<Short>>>();
			for(int i = 0; i < 5; i++){
				levels.add(new ArrayList<NolanNode<Short>>());
			}
			//create all ship configurations
			
			
		}
		
		public void place( int numTurns){
			
			int ind = Arrays.binarySearch(levels.get(0).toArray(), lastSeq[0]);
			
			if( ind < 0){
				NolanNode<Short> temp = new NolanNode<Short>();
				temp.data = lastSeq[0];
				ind = (-1 * ind) - 1;
				levels.get(0).add( ind, temp);
			}
			
			int lastInd = ind;
			
			for( int i = 0; i < 4; i++){
				
				//add nodes to levels and then to parent nodes.
							
				NolanNode<Short> currentNode = levels.get(i).get(lastInd);
				
				int childInd = Arrays.binarySearch(indToNode(currentNode.children, levels.get(i+1) ).toArray(), lastSeq[i+1]);
				if( childInd >= 0){
					currentNode.turns.set(childInd, (byte) numTurns);
				}
				else{
					childInd = (-1*childInd) - 1;
					int childLevelInd = Arrays.binarySearch(levels.get(i+1).toArray(), lastSeq[i+1]);
					if( childLevelInd >= 0 ){
						currentNode.children.add(childInd, (short) childLevelInd);
						levels.get(i+1).get(childLevelInd).parents.add( (short) 0, (short) lastInd);
						currentNode.turns.add( childInd, (byte) numTurns );
					}else{
						childLevelInd = (-1*childLevelInd) - 1;
						NolanNode<Short> tempChild = new NolanNode<Short>();
						tempChild.data = lastSeq[i+1];
						tempChild.parents.add((short) lastInd);
						levels.get(i+1).add( childLevelInd, tempChild);
						currentNode.turns.add( childInd, (byte) numTurns );
						currentNode.children.add( childInd, (short) childLevelInd );
					}
				}
					
				lastInd = childInd;			
			}
		}
		//creates a placement from a given integer which has a max of the max number if uniq placements pre that shiptype
		//start at 0,0 verticle going by x, then y
		//proceed from 0,0 horizonal going y then x
		public int[] toPlacement(int shipMod, int ind){
			int[] ret = {theirPlacements.get(shipMod).get(ind).coords[0].getX(), theirPlacements.get(shipMod).get(ind).coords[0].getY(), theirPlacements.get(shipMod).get(ind).direc};
			return ret;
		}
		//creates index from placement
		//need to fix this TODO
		public short toIndex(int shipMod, int x, int y, int direc){
			
			
			return 0;
		}
		//gets a fleet with the given ship and index
		public Fleet getFleet(int shipMod, int ind){
			int less = shipMod;
			int more = shipMod;
			short[] seq = new short[5];
			Fleet ret = new Fleet();
			boolean ready = false;
			
			while(!ready){
				ret = new Fleet();
				seq[shipMod] = (short) ind;
				int[] given = toPlacement(shipMod, ind);
				ret.placeShip(given[0], given[1], given[2], shipMod);
				
				for( int i = 0; i < less; i++ ){
					int maxVal = ( 20 * ( 11 - shipLength[i] ) );
					int seed = rGen.nextInt( maxVal );
					int[] ship = toPlacement( i, seed );
					while(!ret.placeShip( ship[0], ship[1], ship[2], i )){
						//System.out.println(ship[0]+":"+ ship[1]+":"+ ship[2]+":"+ i);
						seed = rGen.nextInt( maxVal );
						ship = toPlacement( i, seed );
					}
					seq[i] = (short) seed;
					
				}
				for( int j = 4; j >  more; j--){
					int maxVal = ( 20 * ( 11 - shipLength[j] ) );
					int seed = rGen.nextInt( maxVal );
					int[] ship = toPlacement( j, seed );
					while(!ret.placeShip( ship[0], ship[1], ship[2], j )){
						//System.out.println(ship[0]+":"+ ship[1]+":"+ ship[2]+":"+ i);
						seed = rGen.nextInt( maxVal );
						ship = toPlacement( j, seed );
					}
					seq[j] = (short) seed;
				}
				int det = wasUsed(seq);
				if( det >= 0 && det < 135 ){
					ready = true;
				}
			}
			this.lastSeq = seq;
			return ret;
		}
		//gets a random non-used fleet; 
		public Fleet getFleet(){
			Fleet ret = new Fleet();
			short[] seq = new short[5];
			boolean ready = false;
			
			while( !ready ){
				ret = new Fleet();
				for( int i = 0; i < 5; i++){
					int maxVal = ( 20 * ( 11 - shipLength[i] ) );
					int seed = rGen.nextInt( maxVal );
					int[] ship = toPlacement( i, seed );
					while(!ret.placeShip( ship[0], ship[1], ship[2], i )){
						//System.out.println(ship[0]+":"+ ship[1]+":"+ ship[2]+":"+ i);
						seed = rGen.nextInt( maxVal );
						ship = toPlacement( i, seed );
					}
					seq[i] = (short) seed;					
				}
				int det = wasUsed(seq);
				if( det >= 0 && det < 135 ){
					ready = true;
				}
			}
			
			this.lastSeq = seq;
			return ret;
			
		}
		public List<NolanNode> indToNode(List<Short> inds, List<NolanNode<Short>> items){
			List<NolanNode> ret = new ArrayList<NolanNode>();
			for( Short ind: inds){
				ret.add(items.get(ind));
			}
			return ret;					
		}
		//return -1 if it was used, else the average of associated turn numbers
		public int wasUsed(short[] seq){
			
			int total = 0;
			int links = 0;
			int lastInd = -1;			
			
			for( int i = 0; i < 4; i++){
				int ind;
				if( lastInd == -1){
					 ind = Arrays.binarySearch(levels.get(i).toArray(), seq[i]);
				}else{
					ind = lastInd;
				}
				if( ind < 0){
					lastInd = -1;
					continue;
				}
				int childInd = Arrays.binarySearch(indToNode(levels.get(i).get(ind).children, levels.get(i+1) ).toArray(), seq[i]);
				if( childInd < 0){
					lastInd = -1;
					continue;
				}
				else{
					lastInd = childInd;
					links++;
					total += levels.get(i).get(ind).turns.get(childInd);
				}
			}
			
			if(links == 4 ){
				return -1;
			}
			
			if(links > 0){
				return ( total / links );
			}
			return 0;
			
		}
		
	}
	private static class NolanNode<Short> implements Comparable<Short>{
		private Short data;
        private List<Byte> turns = new ArrayList<Byte>();
        private List<Short> children = new ArrayList<Short>();;
        private List<Short> parents = new ArrayList<Short>();;
		@Override
		public int compareTo(Short arg) {
			if ( (short) data < (short)arg ){
				return -1;
			}else if( (short) data < (short)arg ){
				return 1;
			}
			return 0;
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
