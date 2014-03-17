import java.util.ArrayList;
import java.util.Arrays;


public class CaptainHolder {

	

	private void randomPlace(Fleet flt){
		
		if(this.wasWin){
			for( int i = 0; i < 5; i++){
				flt.placeShip(this.lastFleet[i], this.lastDirecs[i], i);
			}		
		}		
		else{		
			for( int j = 0; j < 5; j++){
				Coordinate c = new Coordinate(generator.nextInt(10), generator.nextInt(10));
				int direc = generator.nextInt(2);
				
				while(!flt.placeShip(c, direc, j)) {
					c = new Coordinate(generator.nextInt(10), generator.nextInt(10));
					direc = generator.nextInt(2);
				}			
				this.lastDirecs[j] = direc;
				this.lastFleet[j] = c;
			}
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
					this.followShots.add(new Coordinate( hits[0].getX(), hits[j-1].getY() + 1  ) );
					return;
				}
			}
			if( hits[0].getY() > 0 && !this.myMatchShots[hits[0].getX()][hits[0].getY() - 1]){
				this.followShots.add( new Coordinate( hits[0].getX(), hits[0].getY() - 1 ) );
			}
			if( hits[hits.length-1].getY() < 9 && !this.myMatchShots[hits[hits.length-1].getX()][hits[hits.length-1].getY() + 1]){
				this.followShots.add( new Coordinate( hits[hits.length-1].getX(), hits[hits.length-1].getY() + 1 ) );
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
					this.followShots.add(new Coordinate(  hits[j-1].getX() + 1, hits[0].getY()  ) );
					return;
				}
			}
			if( hits[0].getX() > 0 && !this.myMatchShots[hits[0].getX() - 1][hits[0].getY()]){
				this.followShots.add( new Coordinate( hits[0].getX() - 1, hits[0].getY() ) );
			}
			if( hits[hits.length-1].getX() < 9 && !this.myMatchShots[hits[hits.length-1].getX() + 1][hits[hits.length-1].getY() ]){
				this.followShots.add( new Coordinate( hits[hits.length-1].getX() + 1, hits[hits.length-1].getY() ) );
			}
		}
		
	}
}
