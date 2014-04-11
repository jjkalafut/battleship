
import java.util.ArrayList;

import java.util.Random;

//Created By Daniel Comstock,April 2014
public class CaptainRon implements Captain {
    // Called before each game to reset your ship locations.

    Random generator;
    Fleet myFleet;
    private int basicAttackPattern[][] = {
        {1, 3, 1, 2, 1, 3, 1, 2, 1, 3},
        {2, 1, 3, 1, 2, 1, 3, 1, 2, 1},
        {1, 2, 1, 3, 1, 2, 1, 3, 1, 2},
        {3, 1, 2, 1, 3, 1, 2, 1, 3, 1},
        {1, 3, 1, 2, 1, 3, 1, 2, 1, 3},
        {2, 1, 3, 1, 2, 1, 3, 1, 2, 1},
        {1, 2, 1, 3, 1, 2, 1, 3, 1, 2},
        {3, 1, 2, 1, 3, 1, 2, 1, 3, 1},
        {1, 3, 1, 2, 1, 3, 1, 2, 1, 3},
        {2, 1, 3, 1, 2, 1, 3, 1, 2, 1},};
    /*
     *    {1, 10,  1,  5,  1, 10,  1,  5,  1, 10},
     {5,  1, 10,  1,  5,  1, 10,  1,  5,  1},
     {1,  5,  1, 10,  1,  5,  1, 10,  1,  5},
     {10,  1,  5,  1, 10,  1,  5,  1, 10, 1},
     {1, 10,  1,  5,  1, 10,  1,  5,  1, 10},
     {5,  1, 10,  1,  5,  1, 10,  1,  5,  1},
     {1,  5,  1, 10,  1,  5,  1, 10,  1,  5},
     {10,  1,  5,  1, 10,  1,  5,  1, 10, 1},
     {1, 10,  1,  5,  1, 10,  1,  5,  1, 10},
     {5,  1, 10,  1,  5,  1, 10,  1,  5,  1} };
     */
    private int patternAdapter[][];
    private int currentBoard[][];
    private int calculatedAttackPattern[][];
    private int oppenentAttacks[][];
    private ArrayList<Coordinate> shotsFiredOnShip = new ArrayList<Coordinate>();
    private ArrayList<Coordinate> foundPatrolBoat = new ArrayList<Coordinate>();
    private ArrayList<Coordinate> foundDestroyer = new ArrayList<Coordinate>();
    private ArrayList<Coordinate> foundSubmarine = new ArrayList<Coordinate>();
    private ArrayList<Coordinate> foundBattleship = new ArrayList<Coordinate>();
    private ArrayList<Coordinate> foundAircraftCarrier = new ArrayList<Coordinate>();
    private int attackX, attackY, attackWeight, lastShotX, lastShotY, gameCount, shotsOnShip, shipAttacking, wins;
    private boolean completeKill, horz, vert, tryLeft, tryRight, tryUp, tryDown, oops;

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
        generator = new Random();
        myFleet = new Fleet();
        shipAttacking = 9;
        gameCount = 0;
        lastShotX = 0;
        lastShotY = 0;
        wins = 0;
        completeKill = false;
        horz = false;
        vert = false;
        shotsOnShip = 1;
        tryLeft = false;
        tryRight = false;
        tryUp = false;
        tryDown = false;
        oops = false;

        calculatedAttackPattern = new int[10][10];
        patternAdapter = new int[10][10];
        oppenentAttacks = new int[10][10];
        currentBoard = new int[10][10];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                patternAdapter[i][j] = 1;
                currentBoard[i][j] = 999;
            }
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                calculatedAttackPattern[i][j] = patternAdapter[i][j] * basicAttackPattern[i][j];
            }
        }

        // Each type of ship must be placed on the board.
        // the .place methods return whether it was possible to put a ship at the indicated position.
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
    }

    // Passes your ship locations to the main program.
    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    // Makes an attack on the opponent
    @Override
    public Coordinate makeAttack() {
        //If a ship is currently being attacked, engages finishthem() to finish it off.
        //Otherwise finds best coordinate to attack based on calculatedAttackPattern, and
        //keeps track of where attacked by setting the attackvalue of the coordinates attacked
        // to 0 so it won't be called again.
        attackX = 0;
        attackY = 0;
        attackWeight = 0;
        //if completekill == false and foundwhile attacking has coords, call method to attack based on those coords to find that ship
        //keep track of number of hits on ship, if greater than one, pass number of shots on ship, if hits greater than one and not adjacent,
        //find open coords between hits and shoot there, then try to sink from there.

        if (completeKill == true) {
            finishThem();
            if (attackX == 10) {
                attackX = 9;
            } else if (attackX == -1) {
                attackX = 0;
            }
            if (attackY == 10) {
                attackY = 9;
            } else if (attackY == -1) {
                attackY = 0;
            }

            lastShotX = attackX;
            lastShotY = attackY;
            shotsOnShip++;
            calculatedAttackPattern[attackY][attackX] = 0;
            shotsFiredOnShip.add(new Coordinate(attackX, attackY));
            if (oops == true) {
                resetFinishThem();
                oops = false;
            }
            return new Coordinate(attackX, attackY);
        } /* else if(foundAircraftCarrier.isEmpty()==false){
         if(foundAircraftCarrier.size()==1){
         completeKill = true;
         lastShotX = foundAircraftCarrier.get(0).getX();
         lastShotY = foundAircraftCarrier.get(0).getY();
         shotsFiredOnShip.add(new Coordinate(foundAircraftCarrier.get(0).getX(),foundAircraftCarrier.get(0).getY()));
         finishThem();
         lastShotX = attackX;
         lastShotY = attackY;
         shotsOnShip++;
         calculatedAttackPattern[attackY][attackX] = 0;
         shotsFiredOnShip.add(new Coordinate(attackX,attackY));
         foundAircraftCarrier.clear();
         return new Coordinate(attackX, attackY);
         }
         }
         else if(foundBattleship.isEmpty()==false){
         if(foundBattleship.size()==1){
         completeKill = true;
         lastShotX = foundBattleship.get(0).getX();
         lastShotY = foundBattleship.get(0).getY();
         shotsFiredOnShip.add(new Coordinate(foundBattleship.get(0).getX(),foundBattleship.get(0).getY()));
         finishThem();
         lastShotX = attackX;
         lastShotY = attackY;
         shotsOnShip++;
         calculatedAttackPattern[attackY][attackX] = 0;
         shotsFiredOnShip.add(new Coordinate(attackX,attackY));
         foundBattleship.clear();
         return new Coordinate(attackX, attackY);
         }
         }
         else if(foundSubmarine.isEmpty()==false){
         if(foundSubmarine.size()==1){
         completeKill = true;
         lastShotX = foundSubmarine.get(0).getX();
         lastShotY = foundSubmarine.get(0).getY();
         shotsFiredOnShip.add(new Coordinate(foundSubmarine.get(0).getX(),foundSubmarine.get(0).getY()));
         finishThem();
         lastShotX = attackX;
         lastShotY = attackY;
         shotsOnShip++;
         calculatedAttackPattern[attackY][attackX] = 0;
         shotsFiredOnShip.add(new Coordinate(attackX,attackY));
         foundSubmarine.clear();
         return new Coordinate(attackX, attackY);
         }
         }
         else if(foundDestroyer.isEmpty()==false){
         if(foundDestroyer.size()==1){
         completeKill = true;
         lastShotX = foundDestroyer.get(0).getX();
         lastShotY = foundDestroyer.get(0).getY();
         shotsFiredOnShip.add(new Coordinate(foundDestroyer.get(0).getX(),foundDestroyer.get(0).getY()));
         finishThem();
         lastShotX = attackX;
         lastShotY = attackY;
         shotsOnShip++;
         calculatedAttackPattern[attackY][attackX] = 0;
         shotsFiredOnShip.add(new Coordinate(attackX,attackY));
         foundDestroyer.clear();
         return new Coordinate(attackX, attackY);
         }
         }
         else if(foundPatrolBoat.isEmpty()==false){
         if(foundPatrolBoat.size()==1){
         completeKill = true;
         lastShotX = foundPatrolBoat.get(0).getX();
         lastShotY = foundPatrolBoat.get(0).getY();
         shotsFiredOnShip.add(new Coordinate(foundPatrolBoat.get(0).getX(),foundPatrolBoat.get(0).getY()));
         finishThem();
         lastShotX = attackX;
         lastShotY = attackY;
         shotsOnShip++;
         calculatedAttackPattern[attackY][attackX] = 0;
         shotsFiredOnShip.add(new Coordinate(attackX,attackY));
         foundPatrolBoat.clear();
         return new Coordinate(attackX, attackY);
         }
         }
         */ else {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (calculatedAttackPattern[i][j] >= attackWeight) {
                        attackWeight = calculatedAttackPattern[i][j];
                        attackX = j;
                        attackY = i;
                    }
                }
            }
        }
        calculatedAttackPattern[attackY][attackX] = 0;
        lastShotX = attackX;
        lastShotY = attackY;
        return new Coordinate(attackX, attackY);
    }

    // Informs you of the result of your most recent attack
    @Override
    public void resultOfAttack(int result) {
        // Add code here to process the success/failure of attacks


        if (result == HIT_PATROL_BOAT) {
            patternAdapter[attackY][attackX]++;
            currentBoard[attackY][attackX] = PATROL_BOAT;
            completeKill = true;
            if (shipAttacking == 9) {
                shipAttacking = PATROL_BOAT;
            }
            if (shipAttacking != PATROL_BOAT && shipAttacking != 9) {
                foundPatrolBoat.add(new Coordinate(lastShotX, lastShotY));
            }

        } else if (result == HIT_DESTROYER) {
            patternAdapter[attackY][attackX]++;
            currentBoard[attackY][attackX] = DESTROYER;
            completeKill = true;
            if (shipAttacking == 9) {
                shipAttacking = DESTROYER;
            }
            if (shipAttacking != DESTROYER && shipAttacking != 9) {
                foundDestroyer.add(new Coordinate(lastShotX, lastShotY));
            }
        } else if (result == HIT_SUBMARINE) {
            patternAdapter[attackY][attackX]++;
            currentBoard[attackY][attackX] = SUBMARINE;
            completeKill = true;
            if (shipAttacking == 9) {
                shipAttacking = SUBMARINE;
            }
            if (shipAttacking != SUBMARINE && shipAttacking != 9) {
                foundSubmarine.add(new Coordinate(lastShotX, lastShotY));
            }
        } else if (result == HIT_BATTLESHIP) {
            patternAdapter[attackY][attackX]++;
            currentBoard[attackY][attackX] = BATTLESHIP;
            completeKill = true;
            if (shipAttacking == 9) {
                shipAttacking = BATTLESHIP;
            }
            if (shipAttacking != BATTLESHIP && shipAttacking != 9) {
                foundBattleship.add(new Coordinate(lastShotX, lastShotY));
            }
        } else if (result == HIT_AIRCRAFT_CARRIER) {
            patternAdapter[attackY][attackX]++;
            currentBoard[attackY][attackX] = AIRCRAFT_CARRIER;
            completeKill = true;
            if (shipAttacking == 9) {
                shipAttacking = AIRCRAFT_CARRIER;
            }
            if (shipAttacking != AIRCRAFT_CARRIER && shipAttacking != 9) {
                foundAircraftCarrier.add(new Coordinate(lastShotX, lastShotY));
            }
        } else if (result == SUNK_PATROL_BOAT) {
            patternAdapter[attackY][attackX]++;
            currentBoard[attackY][attackX] = PATROL_BOAT;
            resetFinishThem();
            foundPatrolBoat.clear();
            if (shipAttacking == PATROL_BOAT) {
                shipAttacking = 9;
            }
        } else if (result == SUNK_DESTROYER) {
            patternAdapter[attackY][attackX]++;
            currentBoard[attackY][attackX] = DESTROYER;
            resetFinishThem();
            foundDestroyer.clear();
            if (shipAttacking == DESTROYER) {
                shipAttacking = 9;
            }
        } else if (result == SUNK_SUBMARINE) {
            patternAdapter[attackY][attackX]++;
            currentBoard[attackY][attackX] = SUBMARINE;
            resetFinishThem();
            foundSubmarine.clear();
            if (shipAttacking == SUBMARINE) {
                shipAttacking = 9;
            }
        } else if (result == SUNK_BATTLESHIP) {
            patternAdapter[attackY][attackX]++;
            currentBoard[attackY][attackX] = BATTLESHIP;
            resetFinishThem();
            foundBattleship.clear();
            if (shipAttacking == BATTLESHIP) {
                shipAttacking = 9;
            }
        } else if (result == SUNK_AIRCRAFT_CARRIER) {
            patternAdapter[attackY][attackX]++;
            currentBoard[attackY][attackX] = AIRCRAFT_CARRIER;
            resetFinishThem();
            foundAircraftCarrier.clear();
            if (shipAttacking == AIRCRAFT_CARRIER) {
                shipAttacking = 9;
            }
        } else if (result == MISS) {
            currentBoard[attackY][attackX] = MISS;
        }
    }

    // Informs you of the position of an attack against you.
    @Override
    public void opponentAttack(Coordinate coord) {
        oppenentAttacks[coord.getY()][coord.getX()]++;
    }

    // Informs you of the result of the game.
    @Override
    public void resultOfGame(int result) {
        gameCount++;
        wins = wins + result;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                currentBoard[i][j] = 999;
            }
        }

        if (gameCount >= 100) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    calculatedAttackPattern[i][j] = patternAdapter[i][j] * basicAttackPattern[i][j];
                }
            }
            gameCount = 0;
            wins = 0;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    patternAdapter[i][j] = 1;
                }
            }
        }
        resetFinishThem();
        lastShotX = 0;
        lastShotY = 0;
        shipAttacking = 9;

    }

    private void finishThem() {
        if (vert == false && horz == false && shotsOnShip > 1) {
            findOrientation();
        }

        if (shotsOnShip == 1) {
            shotsFiredOnShip.add(new Coordinate(lastShotX, lastShotY));
            make2ndHit();
        } else {
            if (horz == true) {
                attackHorizontally();
            } else if (vert == true) {
                attackVertically();
            } else {
                make2ndHit();
            }

        }
    }

    private void findOrientation() {
        for (int i = 1; i < shotsOnShip; i++) {
            if (shotsFiredOnShip.get(0).getX() == shotsFiredOnShip.get(i).getX() && currentBoard[shotsFiredOnShip.get(i).getY()][shotsFiredOnShip.get(i).getX()] == shipAttacking) {
                vert = true;
                if (shotsFiredOnShip.get(0).getY() > shotsFiredOnShip.get(i).getY()) {
                    tryDown = true;
                } else if (shotsFiredOnShip.get(0).getY() < shotsFiredOnShip.get(i).getY()) {
                    tryUp = true;
                }
                break;
            } else if (shotsFiredOnShip.get(0).getY() == shotsFiredOnShip.get(i).getY() && currentBoard[shotsFiredOnShip.get(i).getY()][shotsFiredOnShip.get(i).getX()] == shipAttacking) {
                horz = true;
                if (shotsFiredOnShip.get(0).getX() > shotsFiredOnShip.get(i).getX()) {
                    tryLeft = true;
                } else if (shotsFiredOnShip.get(0).getX() < shotsFiredOnShip.get(i).getX()) {
                    tryRight = true;
                }
                break;
            }
        }
    }

    private void make2ndHit() {
        if (shotsFiredOnShip.get(0).getY() != 0 && calculatedAttackPattern[shotsFiredOnShip.get(0).getY() - 1][shotsFiredOnShip.get(0).getX()] != 0) {
            attackX = shotsFiredOnShip.get(0).getX();
            attackY = shotsFiredOnShip.get(0).getY() - 1;
        } else if (shotsFiredOnShip.get(0).getX() != 0 && calculatedAttackPattern[shotsFiredOnShip.get(0).getY()][shotsFiredOnShip.get(0).getX() - 1] != 0) {
            attackX = shotsFiredOnShip.get(0).getX() - 1;
            attackY = shotsFiredOnShip.get(0).getY();
        } else if (shotsFiredOnShip.get(0).getX() != 9 && calculatedAttackPattern[shotsFiredOnShip.get(0).getY()][shotsFiredOnShip.get(0).getX() + 1] != 0) {
            attackX = shotsFiredOnShip.get(0).getX() + 1;
            attackY = shotsFiredOnShip.get(0).getY();
        } else if (shotsFiredOnShip.get(0).getY() != 9 && calculatedAttackPattern[shotsFiredOnShip.get(0).getY() + 1][shotsFiredOnShip.get(0).getX()] != 0) {
            attackX = shotsFiredOnShip.get(0).getX();
            attackY = shotsFiredOnShip.get(0).getY() + 1;
        } else {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (calculatedAttackPattern[i][j] >= attackWeight) {
                        attackWeight = calculatedAttackPattern[i][j];
                        attackX = j;
                        attackY = i;
                    }
                }
            }
            oops = true;
        }
    }

    private void attackVertically() {
        if (tryDown == true) {
            if (currentBoard[lastShotY][lastShotX] != shipAttacking || lastShotY == 0) { // ==MISS should be not currentattack code
                attackX = shotsFiredOnShip.get(0).getX();
                attackY = shotsFiredOnShip.get(0).getY() + 1;
                tryDown = false;
                tryUp = true;
            } else {
                attackX = shotsFiredOnShip.get(0).getX();
                attackY = shotsFiredOnShip.get(shotsOnShip - 1).getY() - 1;
            }
        } else if (tryUp == true) {
            if (currentBoard[lastShotY][lastShotX] != shipAttacking || lastShotY == 9) {
                attackX = shotsFiredOnShip.get(0).getX();
                attackY = shotsFiredOnShip.get(0).getY() - 1;
                tryUp = false;
                tryDown = true;
            } else {
                attackX = shotsFiredOnShip.get(0).getX();
                attackY = shotsFiredOnShip.get(shotsOnShip - 1).getY() + 1;
            }
        }
    }

    private void attackHorizontally() {
        if (tryLeft == true) {
            if (currentBoard[lastShotY][lastShotX] != shipAttacking || lastShotX == 0) {
                attackX = shotsFiredOnShip.get(0).getX() + 1;
                attackY = shotsFiredOnShip.get(0).getY();
                tryLeft = false;
                tryRight = true;
            } else {
                attackX = shotsFiredOnShip.get(shotsOnShip - 1).getX() - 1;
                attackY = shotsFiredOnShip.get(0).getY();
            }
        } else if (tryRight == true) {
            if (currentBoard[lastShotY][lastShotX] != shipAttacking || lastShotX == 9) {
                attackX = shotsFiredOnShip.get(0).getX() - 1;
                attackY = shotsFiredOnShip.get(0).getY();
                tryRight = false;
                tryLeft = true;
            } else {
                attackX = shotsFiredOnShip.get(shotsOnShip - 1).getX() + 1;
                attackY = shotsFiredOnShip.get(0).getY();
            }
        }
    }

    private void resetFinishThem() {
        completeKill = false;
        shotsOnShip = 1;
        vert = false;
        horz = false;
        shotsFiredOnShip.clear();
        tryLeft = false;
        tryRight = false;
        tryUp = false;
        tryDown = false;

    }
}
