
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;


public class CaptainPatternFinder implements Captain {

    
    protected Random generator;
   
    protected Fleet myFleet;
    
    private boolean[][][] shotPattern = new boolean[10][10][1000000];
    private int index;
    private int matchnum = 0; 
    private float avg_hits = 0;

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

        
	        float ships_hit = 0;
	        boolean all_hit = false;
	        float[] hit_rates = {(float) 0.1852,(float) 0.1830,(float) 0.1738,(float) 0.1834,(float) 0.1859,(float) .1866, (float) .1819,(float) .1935,(float) .1898,(float) .1896}; 
	        //18.51-18.52% accurate
	        boolean[][] map1={
	        		{false,false,false,false,false,true,false,false,false,false},
	        		{false,false,true,false,true,false,true,true,false,false},
	        		{false,false,false,false,false,true,false,false,true,false},
	        		{false,true,true,true,true,false,true,true,false,false},
	        		{false,false,true,false,false,false,true,true,true,false},
	        		{false,true,false,false,true,false,false,false,false,true},
	        		{false,true,false,false,false,true,true,true,false,false},
	        		{false,true,false,false,false,false,false,false,false,true},
	        		{false,false,false,false,true,false,false,true,false,false},
	        		{false,false,true,false,false,false,true,false,false,false}
	        };
	        //18.30% accurate
	        boolean[][] map2={
	        		{false,true,true,false,false,true,false,false,false,false},
	        		{false,false,true,false,true,false,false,false,true,false},
	        		{true,false,true,true,false,false,true,false,false,false},
	        		{true,false,false,true,true,false,false,false,true,false},
	        		{true,false,false,true,false,true,false,true,true,false},
	        		{true,false,false,true,false,true,false,false,true,false},
	        		{false,false,false,true,false,false,true,true,false,false},
	        		{false,true,false,true,false,false,true,false,false,false},
	        		{false,false,false,false,false,false,false,true,false,false},
	        		{false,false,false,false,false,false,false,false,false,false}
	        		
	        };
	        //17.38%
	        boolean[][] map3={
	        		{false,false,true,true,false,true,true,false,false,false},
	        		{false,false,false,false,true,false,false,false,false,false},
	        		{false,true,false,false,false,true,true,true,false,true},
	        		{true,false,false,false,true,false,false,false,false,false},
	        		{true,true,false,false,false,true,false,false,false,false},
	        		{false,false,false,false,true,true,true,false,true,false},
	        		{false,false,false,true,false,true,false,false,true,true},
	        		{false,false,false,false,true,false,false,false,false,false},
	        		{false,false,true,false,false,true,false,false,false,true},
	        		{false,false,true,false,false,false,false,false,true,true}
	        };
	        //18.34-18.35%
	        boolean[][] map4={
	        		{false,false,false,true,false,false,false,false,false,false},
	        		{false,false,true,true,false,false,false,false,false,false},
	        		{false,false,false,false,false,false,false,false,true,false},
	        		{true,true,true,true,false,false,true,true,false,false},
	        		{false,false,true,false,true,true,true,false,true,false},
	        		{false,false,false,false,false,false,false,false,true,true},
	        		{false,false,false,false,true,false,false,false,false,true},
	        		{false,false,true,false,true,true,false,true,true,false},
	        		{false,false,false,true,true,false,false,false,true,false},
	        		{false,false,false,true,true,true,false,false,false,false}
	        };
	        //18.59%
	        boolean[][] map5={
	        		{false,true,true,false,false,false,false,false,false,false},
	        		{false,true,false,true,true,false,false,true,false,false},
	        		{false,false,false,false,false,true,false,false,true,false},
	        		{false,false,true,false,true,true,false,true,true,false},
	        		{false,false,true,false,true,false,false,true,false,false},
	        		{false,false,true,true,true,false,false,false,false,true},
	        		{false,false,false,true,false,true,false,false,true,false},
	        		{false,false,true,true,true,false,false,false,false,true},
	        		{false,false,true,false,false,true,true,false,false,false},
	        		{false,false,false,false,false,false,false,false,false,false}
	        };
	        //18.66%
	        boolean[][] map6={
	        		{false,false,false,false,false,false,false,false,false,false},
	        		{false,false,true,true,false,true,false,false,true,false},
	        		{false,false,true,false,true,false,false,false,true,false},
	        		{false,false,true,true,true,true,false,false,true,true},
	        		{false,false,false,true,false,false,true,false,false,false},
	        		{true,false,true,false,false,true,true,false,false,false},
	        		{false,false,true,true,true,false,false,false,false,false},
	        		{false,true,false,false,true,true,false,false,false,true},
	        		{false,false,false,false,false,false,true,false,false,false},
	        		{false,false,true,false,false,true,true,false,false,false}
	        };
	        //18.19%
	        boolean[][] map7={
	        		{false,false,false,true,false,true,false,true,false,false},
	        		{false,false,true,false,false,false,false,false,false,false},
	        		{false,false,true,false,false,false,false,true,false,false},
	        		{false,false,false,true,true,true,false,false,false,true},
	        		{false,true,false,true,false,false,false,false,false,true},
	        		{true,false,true,false,true,true,true,true,false,false},
	        		{false,false,false,false,false,true,false,false,true,true},
	        		{false,false,false,true,false,true,false,false,true,false},
	        		{true,false,true,true,false,false,false,false,false,false},
	        		{false,false,false,true,false,false,true,false,false,false}
	        };
	        //19.35%
	        boolean[][] map8={
	        		{false,true,true,false,false,false,false,false,false,false},
	        		{false,false,false,false,false,false,true,false,false,false},
	        		{false,false,false,false,true,true,true,false,true,false},
	        		{true,false,false,true,true,true,true,true,false,false},
	        		{false,false,false,true,false,true,true,true,false,false},
	        		{false,false,true,true,false,true,true,false,true,false},
	        		{true,false,false,false,false,true,true,false,false,false},
	        		{false,false,true,true,false,false,true,false,true,false},
	        		{false,false,false,false,false,false,true,false,false,false},
	        		{false,false,false,false,false,false,false,false,false,false}
	        };
	        //18.98%
	        boolean[][] map9={
	        		{false,false,false,true,false,false,false,false,false,false},
	        		{false,false,false,true,true,false,false,true,false,false},
	        		{false,false,true,false,true,false,false,false,false,false},
	        		{false,true,false,true,true,false,false,true,true,true},
	        		{false,false,false,true,false,true,true,false,false,false},
	        		{false,true,false,false,true,true,false,false,true,false},
	        		{false,false,false,true,false,false,true,true,false,true},
	        		{false,false,true,true,false,true,false,true,false,false},
	        		{false,false,false,false,false,true,true,false,false,false},
	        		{false,false,false,false,false,false,false,false,true,false}	
	        };
	        //18.96%
	        boolean[][] map10={
	        		{false,false,false,false,false,false,false,false,false,false},
	        		{false,true,true,false,false,false,true,false,false,false},
	        		{false,false,false,false,true,false,false,true,false,false},
	        		{false,false,false,true,false,true,false,false,false,false},
	        		{true,true,false,true,true,true,false,false,true,true},
	        		{false,false,false,false,true,true,false,false,true,false},
	        		{true,false,false,true,true,true,true,false,true,false},
	        		{false,true,true,false,false,true,false,true,false,false},
	        		{false,false,false,false,true,false,false,false,false,false},
	        		{false,false,true,true,false,false,false,false,false,false}
	        };
	        //
	        boolean[][] mapfinal={
	        		{false,false,false,false,false,false,false,false,false,false},
	        		{false,false,false,false,false,false,false,true,false,false},
	        		{false,true,true,true,false,true,false,true,false,false},
	        		{false,true,false,true,true,false,true,true,false,false},
	        		{false,true,true,true,false,true,false,false,false,false},
	        		{false,false,true,true,true,true,true,true,false,false},
	        		{false,false,false,false,true,false,true,false,true,false},
	        		{false,false,false,true,true,false,false,false,false,false},
	        		{false,false,true,true,true,true,false,false,false,false},
	        		{false,false,false,false,false,false,false,false,false,false}
	        };
	        boolean[][][] maps = { map1, map2, map3, map4, map5, map6, map7, map8, map9, map10};
	        /*
	        float[][] final_map = new float[10][10];
	        for(int i = 0; i<100; i++){
	        	for( int j = 0; j <10; j++){
		        	if( maps[j][i%10][i/10] ){
		        		final_map[i%10][i/10] += hit_rates[j]/30;
		        	}
		        	else
		        	{
		        		final_map[i%10][i/10] += ( ( ( (float) 1 )-hit_rates[j] ) / (float) 70 )*((float)9/(float)70);
		        	}
	        	}
	        }
	        
	        
	        */
	        for(int i = 0; i<100; i++){
		        	if( mapfinal[i%10][i/10] && this.myFleet.isShipAt(new Coordinate(i%10,i/10))){
		        		ships_hit++;
		        	}
	        }
	        avg_hits += ships_hit/(float)(numMatches);
	        if(matchnum == numMatches){
	        	System.out.println("Average Hits for map 1: "+avg_hits);
	        	System.out.println(100*avg_hits/30+"% accuracy");
	        }
	        /*
	        boolean[][] shots = new boolean[10][10];
	        
	        while(!all_hit){
	        	for (int i = 0; i<30; i++){
	        		int x = generator.nextInt(10);
	        		int y = generator.nextInt(10);
	        		if(!shots[x][y]){
	        			shots[x][y] = true;
	        			if(myFleet.isShipAt(new Coordinate(x,y))){
	            			ships_hit++;
	            		}
	        		}
	        		else{
	        			i--;
	        			continue;
	        		}
	        		
	        	}
	        	if(ships_hit>=8){
	        		for( int j = 0; j<10; j++){
	        			for( int k = 0; k<10; k++ ){
	        				shotPattern[k][j][matchnum-1]=shots[k][j];
	        			}
	        		}
	        		all_hit = true;
	        	}
	        }
	        if(matchnum == numMatches){
	        	compileBest();
	        }
	        */
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
		for( int l = 0; l < 30; l++){
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
