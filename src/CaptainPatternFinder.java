
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;


public class CaptainPatternFinder implements Captain {

    
    protected Random generator;
   
    protected Fleet myFleet;
    
    private boolean[][][] shotPattern = new boolean[10][10][50000];
    private int index;
    private int matchnum = 0; 

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
    	
        generator = new Random();
        myFleet = new Fleet();
        matchnum++;
        // Each type of ship must be placed on the board.  Note that the .place method return whether it was
        // possible to put a ship at the indicated position.  If the coordinates were not on the board or if
        // it overlapped with a ship you already tried to place it will return false.
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), PATROL_BOAT)) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), DESTROYER)) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), SUBMARINE)) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), BATTLESHIP)) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), AIRCRAFT_CARRIER)) {
        }
        index = 0;
        int ships_hit = 0;
        boolean all_hit = false;
        boolean[][] shots = new boolean[10][10];
        
        while(!all_hit){
        	for (int i = 0; i<30; i++){
        		int x = generator.nextInt(10);
        		int y = generator.nextInt(10);
        		if(!shots[x][y]){
        			shots[x][y] = true;
        		}
        		else{
        			i--;
        			continue;
        		}
        		if(myFleet.isShipAt(new Coordinate(x,y))){
        			ships_hit++;
        		}
        	}
        	if(ships_hit>=8){
        		for( int j = 0; j<10; j++){
        			for( int k = 0; k<10; k++ ){
        				shotPattern[k][j][index]=shots[k][j];
        			}
        		}
        		all_hit = true;
        	}
        }
        if(matchnum == numMatches){
        	compileBest();
        }
    }


    private void compileBest() {
		int[][] bestOpts = new int[10][10];
		for( int i = 0; i<50000; i++){
			for( int j = 0; j<10; j++){
				for( int k = 0; k<10; k++){
					if(shotPattern[k][j][i]){
						bestOpts[k][j]++;
					}
				}
			}
		}
		boolean[][] shots = new boolean[10][10];
		for( int l = 0; l < 40; l++){
			int biggest = 0;
			int x = 0;
			int y = 0;
			for( int j =0; j<100; j++){
				if(bestOpts[j%10][j/10]>biggest){
					biggest = bestOpts[j%10][j/10];
					x = j%10;
					y = j/10;
				}
			}
			bestOpts[x][y]=0;
			shots[x][y] = true;
		}
		PrintWriter writer;
		try {
			writer = new PrintWriter("map.txt", "UTF-8");
			for(int k = 0; k < 10; k++){
				
				for( int p = 0; p < 10; p++){
					writer.print(""+shots[p][k]+",");
				}
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		
	}


	@Override
    public Fleet getFleet() {
        return myFleet;
    }


    @Override
    public Coordinate makeAttack() {
        return new Coordinate(generator.nextInt(10), generator.nextInt(10));
    }


    @Override
    public void resultOfAttack(int result) {
        // Add code here to process the success/failure of attacks
    }


    @Override
    public void opponentAttack(Coordinate coord) {
        // Add code here to process or record opponent attacks
    }


    @Override
    public void resultOfGame(int result) {
        // Add code here to process the result of a game
    }
}
