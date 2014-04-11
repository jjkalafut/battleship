// Imports
import java.util.ArrayList;
import java.util.Random;

public class KapitanVogel implements Captain, Constants {

    protected Fleet myFleet;
    static final int HEIGHT = 10;
    static final int WIDTH = 10;
    static final int NUM_SHIPS = 5;
    static final boolean BASIC_SHOT = false;
    static final boolean DEBUG = false;
    Defence defence;
    Offence offence;

    public KapitanVogel() {
        defence = new Defence();
        offence = new Offence();
    }

    //NEW GAME
    public void initialize(int numMatches, int numCaptains, String opponet) {
        myFleet = new Fleet();
        placeFleet();
        offence.start();
    }

    public Fleet getFleet() {
        return myFleet;
    }

    public Coordinate makeAttack() {
        return offence.getShot(BASIC_SHOT);
    }

    public void resultOfAttack(int result) {
        if (result == MISS) {
            offence.miss();
            return;
        }

        if (result == DEFEATED) {
            return;
        }

        int model = result % HIT_MODIFIER;
        offence.hit(model);
        if (result / SUNK_MODIFIER >= 1) {
            offence.sunk(model);
        }
    }

    public void opponentAttack(Coordinate coord) {
        defence.attackedAt(coord);
    }

    public void resultOfGame(int result) {
        if (DEBUG) {
            if (result == WON) {
                System.out.printf("\nWON");
            } else {
                System.out.printf("\nLOSS");
            }
            offence.print();
        }
    }

    private void placeFleet() {
        ArrayList<Ship> placement = defence.start(getShipModels());
        for (Ship s : placement) {
            myFleet.placeShip(s.getLocation(), s.getDirection(), s.getModel());
        }
    }

    private int[] getShipModels() {
        int[] tmp = {AIRCRAFT_CARRIER,
            BATTLESHIP,
            SUBMARINE,
            DESTROYER,
            PATROL_BOAT};
        return tmp;
    }

    //Anything to do with placing the ships
    private class Defence {

        static final int NUMBER_OF_PLACEMENTS = 100;
        Random rand;
        int[][] attacks;

        public Defence() {
            rand = new Random();
            attacks = new int[WIDTH][HEIGHT];
        }

        public ArrayList<Ship> start(int[] shipModels) {
            ArrayList<Ship> placement = calculateBestPlacement(createPlacements(shipModels));
            return placement;
        }

        public void attackedAt(Coordinate coord) {
            attacks[coord.getX()][coord.getY()] += 1;
        }

        private ArrayList<ArrayList<Ship>> createPlacements(int[] shipModels) {
            ArrayList<ArrayList<Ship>> placements = new ArrayList<ArrayList<Ship>>();
            for (int i = 0; i < NUMBER_OF_PLACEMENTS; i++) {
                ArrayList<Ship> placement = new ArrayList<Ship>();
                for (int model : shipModels) {
                    int sCoord[] = new int[3];
                    while (true) {
                        sCoord[0] = rand.nextInt(WIDTH);
                        sCoord[1] = rand.nextInt(HEIGHT);
                        sCoord[2] = rand.nextInt(2);
                        Ship tmpShip = new Ship(new Coordinate(sCoord[0], sCoord[1]), sCoord[2], model);
                        if (!tmpShip.isValid()) {
                            continue;
                        }
                        boolean goodPlacement = true;
                        for (Ship t : placement) {
                            if (tmpShip.intersectsShip(t)) {
                                goodPlacement = false;
                                break;
                            }
                        }
                        if (goodPlacement) {
                            break;
                        }
                    }
                    Ship s = new Ship(new Coordinate(sCoord[0], sCoord[1]), sCoord[2], model);
                    placement.add(s);
                }
                placements.add(placement);
            }
            return placements;
        }

        private ArrayList<Ship> calculateBestPlacement(ArrayList<ArrayList<Ship>> placements) {
            int minPoints = 1000000000;
            ArrayList<Ship> bestPlacement = new ArrayList<Ship>();
            for (ArrayList<Ship> placement : placements) {
                int points = 0;
                for (Ship s : placement) {
                    Coordinate shipsPoints[] = new Coordinate[s.length];
                    for (int p = 0; p < shipsPoints.length; p++) {
                        int x, y;
                        if (HORIZONTAL == s.getDirection()) {
                            x = p + s.getLocation().getX();
                            y = s.getLocation().getY();
                        } else {
                            x = s.getLocation().getX();
                            y = p + s.getLocation().getY();
                        }
                        shipsPoints[p] = new Coordinate(x, y);
                    }
                    for (Coordinate p : shipsPoints) {
                        points += attacks[p.getX()][p.getY()];
                    }
                }
                if (points < minPoints) {
                    minPoints = points;
                    bestPlacement = placement;
                }
            }
            return bestPlacement;
        }
    }

    //Anything to do with attacking 
    private class Offence {

        static final int IS_SUNK = 1;
        Coordinate lastShot;
        int[][] shipStatus;
        int[][] shotsFired;
        int[][] gameBoard;
        Random rand;

        public Offence() {
            rand = new Random();
        }

        public void start() {
            shipStatus = new int[NUM_SHIPS][4];     //[Ship Model] [isAttacking, isSunk, numHits, direction]
            shotsFired = new int[WIDTH][HEIGHT];
            gameBoard = new int[WIDTH][HEIGHT];
        }

        public Coordinate getShot(boolean basic) {
            if (basic) {
                return getBasicShot();
            } else {
                return getAdvancedShot();
            }
        }

        private Coordinate getBasicShot() {
            int x, y;
            while (true) {
                x = rand.nextInt(WIDTH);
                y = rand.nextInt(HEIGHT);
                if (shotsFired[x][y] != 1) {
                    shotsFired[x][y] = 1;
                    break;
                }
            }
            lastShot = new Coordinate(x, y);
            return new Coordinate(x, y);
        }

        private Coordinate getAdvancedShot() {
            calculateGameBoard();

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    if (shotsFired[x][y] != 1) {
                        if (gameBoard[x][y] == 7) {
                            shotsFired[x][y] = 1;
                            if (DEBUG) {
                                System.out.printf("\nADVANCED SHOT: X:%d  Y:%d", x, y);
                                print();
                            }
                            lastShot = new Coordinate(x, y);
                            return new Coordinate(x, y);
                        }
                    }
                }
            }
            return getBasicShot();
        }

        private void calculateGameBoard() {
            //Reset all Possible positions to 0
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    if (gameBoard[x][y] == 7) {
                        gameBoard[x][y] = 0;
                    }
                }
            }
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    if (gameBoard[x][y] > 0 && gameBoard[x][y] < 6 && shipStatus[(gameBoard[x][y]) - 1][1] != IS_SUNK) {
                        if (shipStatus[(gameBoard[x][y]) - 1][2] <= 1) {
                            if (x - 1 >= 0) {
                                if (gameBoard[x - 1][y] == 0) {
                                    gameBoard[x - 1][y] = 7;
                                }
                            }
                            if (x + 1 < WIDTH) {
                                if (gameBoard[x + 1][y] == 0) {
                                    gameBoard[x + 1][y] = 7;
                                }
                            }
                            if (y - 1 >= 0) {
                                if (gameBoard[x][y - 1] == 0) {
                                    gameBoard[x][y - 1] = 7;
                                }
                            }
                            if (y + 1 < HEIGHT) {
                                if (gameBoard[x][y + 1] == 0) {
                                    gameBoard[x][y + 1] = 7;
                                }
                            }
                        } else {
                            //Vertical
                            if (shipStatus[(gameBoard[x][y]) - 1][3] == 2) {
                                if (y - 1 >= 0) {
                                    if (gameBoard[x][y - 1] == 0) {
                                        gameBoard[x][y - 1] = 7;
                                    }
                                }
                                if (y + shipStatus[(gameBoard[x][y]) - 1][2] < HEIGHT) {
                                    if (gameBoard[x][y + shipStatus[(gameBoard[x][y]) - 1][2]] == 0) {
                                        gameBoard[x][y + shipStatus[(gameBoard[x][y]) - 1][2]] = 7;
                                    }
                                }
                            } //Horizontal
                            else if (shipStatus[(gameBoard[x][y]) - 1][3] == 1) {
                                if (x - 1 >= 0) {
                                    if (gameBoard[x - 1][y] == 0) {
                                        gameBoard[x - 1][y] = 7;
                                    }
                                }
                                if (x + shipStatus[(gameBoard[x][y]) - 1][2] < WIDTH) {
                                    if (gameBoard[x + shipStatus[(gameBoard[x][y]) - 1][2]][y] == 0) {
                                        gameBoard[x + shipStatus[(gameBoard[x][y]) - 1][2]][y] = 7;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        public void hit(int model) {
            shipStatus[model][2]++;
            gameBoard[lastShot.getX()][lastShot.getY()] = model + 1;				// used for basic shooting
            calculateDirection(model, lastShot.getX(), lastShot.getY());
        }

        public void sunk(int model) {
            shipStatus[model][0] = 0;
            shipStatus[model][1] = IS_SUNK;
        }

        public void miss() {
            gameBoard[lastShot.getX()][lastShot.getY()] = 8;
        }

        private void calculateDirection(int model, int x, int y) {
            if (shipStatus[model][3] == 0) {
                if (shipStatus[model][2] > 1) {
                    if (x - 1 >= 0) {
                        if (gameBoard[x - 1][y] == model + 1) {
                            shipStatus[model][3] = 1;
                        }
                    }
                    if (x + 1 < WIDTH) {
                        if (gameBoard[x + 1][y] == model + 1) {
                            shipStatus[model][3] = 1;
                        }
                    }
                    if (y - 1 >= 0) {
                        if (gameBoard[x][y - 1] == model + 1) {
                            shipStatus[model][3] = 2;
                        }
                    }
                    if (y + 1 < HEIGHT) {
                        if (gameBoard[x][y + 1] == model + 1) {
                            shipStatus[model][3] = 2;
                        }
                    }
                } else {
                    shipStatus[model][3] = 0;
                }
            }
        }

        public void print() {
            System.out.printf("\nGameBoard\n");
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    System.out.printf("%d	", gameBoard[x][y]);
                }
                System.out.printf("\n");
            }
        }
    }
}
