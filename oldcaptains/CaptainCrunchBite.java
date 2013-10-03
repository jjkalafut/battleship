/*
 * AUTHOR:  Brandon Felch
 * DATE:    3/27/2013
 * PURPOSE: Battleship AI
 */

import java.util.LinkedList;
import java.util.Random;

public class CaptainCrunchBite implements Captain, Constants {

    Random generator;
    Fleet myFleet;
    int x = -2;
    int y = 0;
    boolean[][] attacked;
    boolean hit, up, down, right, left;
    Coordinate lastHit;
    Ship[][] nmeShips;
    LinkedList<Coordinate> hitStack;

    void placeShips() {
        //Randomly pick placement plan
        int defensePlan = generator.nextInt(4);

        if (defensePlan == 0) {
            SargeDefensePlan();
        } else if (defensePlan == 1) {
            GrifDefensePlan();
        } else if (defensePlan == 2) {
            DonutDefensePlan();
        } else {
            SimmonsDefensePlan();
        }
    }

    public void SargeDefensePlan() {
        //All set positions
        myFleet.placeShip(4, 2, VERTICAL, PATROL_BOAT);
        myFleet.placeShip(6, 1, HORIZONTAL, DESTROYER);
        myFleet.placeShip(2, 5, VERTICAL, SUBMARINE);
        myFleet.placeShip(4, 9, HORIZONTAL, BATTLESHIP);
        myFleet.placeShip(1, 0, VERTICAL, AIRCRAFT_CARRIER);
    }

    public void GrifDefensePlan() {
        //All set positions with some random orientations
        myFleet.placeShip(5, 0, HORIZONTAL, PATROL_BOAT);
        myFleet.placeShip(7, 2, VERTICAL, DESTROYER);
        myFleet.placeShip(4, 6, generator.nextInt(2), SUBMARINE);
        myFleet.placeShip(2, 3, generator.nextInt(2), BATTLESHIP);
        myFleet.placeShip(9, 5, VERTICAL, AIRCRAFT_CARRIER);
    }

    public void DonutDefensePlan() {
        //All set positions
        myFleet.placeShip(8, 9, HORIZONTAL, PATROL_BOAT);
        myFleet.placeShip(6, 5, VERTICAL, DESTROYER);
        myFleet.placeShip(2, 3, HORIZONTAL, SUBMARINE);
        myFleet.placeShip(9, 2, VERTICAL, BATTLESHIP);
        myFleet.placeShip(0, 9, HORIZONTAL, AIRCRAFT_CARRIER);
    }

    public void SimmonsDefensePlan() {
        //Completely random positions
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), generator.nextInt(5))) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), generator.nextInt(5))) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), generator.nextInt(5))) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), generator.nextInt(5))) {
        }
        while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), generator.nextInt(5))) {
        }
    }

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
        generator = new Random();
        myFleet = new Fleet();
        attacked = new boolean[10][10];
        hit = false;
        up = false;
        down = false;
        right = false;
        left = false;

        nmeShips = new Ship[10][10];
        hitStack = new LinkedList<Coordinate>();

        //Creates custom ship that symbolizes non-attacked space
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                nmeShips[i][j] = new Ship(new Coordinate(-1, -1), -1, -1);
            }
        }

        placeShips();
    }

    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    @Override
    public Coordinate makeAttack() {
        //Attacks if move is on board and hasn't been attacked before
        do {
            //Attack above ship if possible and one was hit
            if (up) {
                if (x + 1 < 10 && nmeShips[x + 1][y].sunk == false) {
                    x++;
                    y = lastHit.getY();
                } else {
                    up = false;
                }
            } //Attack below ship if possible and one was hit
            else if (down) {
                if (x - 1 >= 0 && nmeShips[x - 1][y].sunk == false) {
                    x--;
                    y = lastHit.getY();
                } else {
                    down = false;
                }
            } //Attack to right of ship if possible and one was hit
            else if (right) {
                if (y + 1 < 10 && nmeShips[x][y + 1].sunk == false) {
                    x = lastHit.getX();
                    y++;
                } else {
                    right = false;
                }
            } //Attack to left of ship if possible and one was hit
            else if (left) {
                if (y - 1 >= 0 && nmeShips[x][y - 1].sunk == false) {
                    x = lastHit.getX();
                    y--;
                } else {
                    left = false;
                }
            } //Random attack if no ship in focus
            else {
                x = generator.nextInt(10);
                y = generator.nextInt(10);
            }
        } while (!(x < 10 && y < 10 && x >= 0 && y >= 0) || nmeShips[x][y].model != -1);

        //Marks attacked position to prevent duplicate shots
        attacked[x][y] = true;

        //Makes attack
        return new Coordinate(x, y);
    }

    @Override
    public void resultOfAttack(int result) {
        int sunkShip = -1;

        //Marks ship type on hit or sink
        if (result == HIT_PATROL_BOAT) {
            nmeShips[x][y].model = PATROL_BOAT;
        } else if (result == HIT_DESTROYER) {
            nmeShips[x][y].model = DESTROYER;
        } else if (result == HIT_SUBMARINE) {
            nmeShips[x][y].model = SUBMARINE;
        } else if (result == HIT_BATTLESHIP) {
            nmeShips[x][y].model = BATTLESHIP;
        } else if (result == HIT_AIRCRAFT_CARRIER) {
            nmeShips[x][y].model = AIRCRAFT_CARRIER;
        } else if (result == SUNK_PATROL_BOAT) {
            nmeShips[x][y].model = PATROL_BOAT;
            sunkShip = PATROL_BOAT;
        } else if (result == SUNK_DESTROYER) {
            nmeShips[x][y].model = DESTROYER;
            sunkShip = DESTROYER;
        } else if (result == SUNK_SUBMARINE) {
            nmeShips[x][y].model = SUBMARINE;
            sunkShip = SUBMARINE;
        } else if (result == SUNK_BATTLESHIP) {
            nmeShips[x][y].model = BATTLESHIP;
            sunkShip = BATTLESHIP;
        } else if (result == SUNK_AIRCRAFT_CARRIER) {
            nmeShips[x][y].model = AIRCRAFT_CARRIER;
            sunkShip = AIRCRAFT_CARRIER;
        }

        //Checks for hit
        if (result == HIT_PATROL_BOAT
                || result == HIT_DESTROYER
                || result == HIT_SUBMARINE
                || result == HIT_BATTLESHIP
                || result == HIT_AIRCRAFT_CARRIER) {
            //If second hit, determines if it was on same ship
            if (hit) {
                //If different ship, stores first ship coordinates, focus on second ship
                if (nmeShips[x][y].model != nmeShips[lastHit.getX()][lastHit.getY()].model) {
                    hitStack.push(lastHit);
                    up = true;
                    down = true;
                    right = true;
                    left = true;
                }
            } //If first hit, sets all directions to true (possible)
            else {
                up = true;
                down = true;
                right = true;
                left = true;
            }

            //Keeps track of previous hits and tracks possible directions
            hit = true;
            lastHit = new Coordinate(x, y);
            if (x + 1 >= 10) {
                up = false;
            } else if (x - 1 < 0) {
                down = false;
            }
            if (y + 1 >= 10) {
                right = false;
            } else if (y - 1 < 0) {
                left = false;
            }
        } //Checks for sink and resets
        else if (result == SUNK_PATROL_BOAT
                || result == SUNK_DESTROYER
                || result == SUNK_SUBMARINE
                || result == SUNK_BATTLESHIP
                || result == SUNK_AIRCRAFT_CARRIER) {
            hit = false;
            up = false;
            down = false;
            right = false;
            left = false;

            //Marks sunk ships on 'field'
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (nmeShips[i][j].model == sunkShip) {
                        nmeShips[i][j].sunk = true;
                    }
                }
            }

            //If ship location was stored, focus on that ship
            if (!hitStack.isEmpty()) {
                lastHit = hitStack.pop();
                hit = true;
                up = true;
                down = true;
                right = true;
                left = true;
            }
        } //model of -2 symbolizes miss
        else {
            nmeShips[x][y].model = -2;

            //If missed after a hit, remove that direction as a possibility
            if (hit) {
                if (up) {
                    up = false;
                } else if (down) {
                    down = false;
                } else if (right) {
                    right = false;
                } else if (left) {
                    left = false;
                }
            }
        }
    }

    @Override
    public void opponentAttack(Coordinate coord) {
    }

    @Override
    public void resultOfGame(int result) {
    }
}