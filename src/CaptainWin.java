/*
 * Created by Marc Velasquez
 * Date April 2014
 */

import java.util.LinkedList;
import java.util.Random;

public class CaptainWin implements Captain {

    private Random generator;
    private Fleet myFleet;
    int[][] attackedGrid;
    boolean up, down, left, right, hit;
    Coordinate last;
    int x, y;
    LinkedList<Coordinate> lastHitStack;
    Ship[][] ships;
    String oppLast = "";
    int win;
    int lastWin;
    int plan;
    int lastPlan;

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
        generator = new Random();
        myFleet = new Fleet();
        attackedGrid = new int[10][10];
        lastHitStack = new LinkedList<Coordinate>();
        up = false;
        down = false;
        left = false;
        right = false;
        hit = false;
        ships = new Ship[10][10];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                attackedGrid[i][j] = 0;
            }
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                ships[i][j] = new Ship(new Coordinate(-1, -1), -1, -1);
            }
        }

        if (opponent.equals(this.oppLast) && win != lastWin) {
            plan = lastPlan;
        } else {
            while (plan == lastPlan) {
                plan = generator.nextInt(6);
            }
        }

        lastPlan = plan;
        lastWin = win;
        oppLast = opponent;
        /*
         * Different orientations at a semi set location plan
         */
        if (plan == 0) {
            while (!myFleet.placeShip(1, 8, generator.nextInt(2), PATROL_BOAT));
            while (!myFleet.placeShip(5, 7, generator.nextInt(2), DESTROYER));
            while (!myFleet.placeShip(3, 2, generator.nextInt(2), SUBMARINE));
            while (!myFleet.placeShip(generator.nextInt(10), 1, generator.nextInt(2), BATTLESHIP));
            while (!myFleet.placeShip(generator.nextInt(10), 5, generator.nextInt(2), AIRCRAFT_CARRIER));

        }
        /*
         * The outer edge plan
         */
        if (plan == 1) {
            myFleet.placeShip(0, 2, VERTICAL, PATROL_BOAT);
            myFleet.placeShip(0, 5, VERTICAL, DESTROYER);
            myFleet.placeShip(4, 9, HORIZONTAL, SUBMARINE);
            myFleet.placeShip(3, 0, HORIZONTAL, BATTLESHIP);
            myFleet.placeShip(9, 3, VERTICAL, AIRCRAFT_CARRIER);
        }
        /*
         * Different orientations at a set X random Y location plan
         */
        if (plan == 2) {
            while (!myFleet.placeShip(8, generator.nextInt(10), generator.nextInt(2), PATROL_BOAT));
            while (!myFleet.placeShip(1, generator.nextInt(10), generator.nextInt(2), DESTROYER));
            while (!myFleet.placeShip(5, generator.nextInt(10), generator.nextInt(2), SUBMARINE));
            while (!myFleet.placeShip(6, generator.nextInt(10), generator.nextInt(2), BATTLESHIP));
            while (!myFleet.placeShip(0, generator.nextInt(10), generator.nextInt(2), AIRCRAFT_CARRIER));
        }
        /*
         * All Vertical plan
         */
        if (plan == 3) {
            myFleet.placeShip(1, 7, VERTICAL, PATROL_BOAT);
            myFleet.placeShip(1, 3, VERTICAL, DESTROYER);
            myFleet.placeShip(3, 1, VERTICAL, SUBMARINE);
            myFleet.placeShip(8, 5, VERTICAL, BATTLESHIP);
            myFleet.placeShip(5, 4, VERTICAL, AIRCRAFT_CARRIER);
        }
        /*
         * The Arrow plan
         */
        if (plan == 4) {
            myFleet.placeShip(8, 8, generator.nextInt(2), PATROL_BOAT);
            myFleet.placeShip(7, 7, generator.nextInt(2), DESTROYER);
            myFleet.placeShip(5, 5, generator.nextInt(2), SUBMARINE);
            myFleet.placeShip(3, 3, generator.nextInt(2), BATTLESHIP);
            myFleet.placeShip(1, 1, generator.nextInt(2), AIRCRAFT_CARRIER);
        }
        /*
         * Stacked Arrow/picture of kite plan
         */
        if (plan == 5) {
            myFleet.placeShip(4, 6, generator.nextInt(2), PATROL_BOAT);
            myFleet.placeShip(5, 7, generator.nextInt(2), DESTROYER);
            myFleet.placeShip(3, 5, generator.nextInt(2), SUBMARINE);
            myFleet.placeShip(2, 4, generator.nextInt(2), BATTLESHIP);
            myFleet.placeShip(1, 3, generator.nextInt(2), AIRCRAFT_CARRIER);
        }
        /*
         * Full random plan
         */
        if (plan == 6) {
            while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), generator.nextInt(5)));
            while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), generator.nextInt(5)));
            while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), generator.nextInt(5)));
            while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), generator.nextInt(5)));
            while (!myFleet.placeShip(generator.nextInt(10), generator.nextInt(10), generator.nextInt(2), generator.nextInt(5)));
        }
    }

    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    @Override
    public Coordinate makeAttack() {
        do {

            if (right) {
                if (x + 1 < 10 && ships[x + 1][y].sunk == false) {
                    x++;
                    y = last.getY();
                } else {
                    right = false;
                }
            } else if (left) {
                if (x - 1 > 0 && ships[x - 1][y].sunk == false) {
                    x--;
                    y = last.getY();
                } else {
                    left = false;
                }
            } else if (up) {
                if (y + 1 < 10 && ships[x][y + 1].sunk == false) {
                    x = last.getX();
                    y++;
                } else {
                    up = false;
                }
            } else if (down) {
                if (y - 1 > 0 && ships[x][y - 1].sunk == false) {
                    x = last.getX();
                    y--;
                } else {
                    down = false;
                }
            } else {
                while (attackedGrid[x][y] == 1) {
                    x = generator.nextInt(10);
                    y = generator.nextInt(10);
                }
            }

        } while (!(x < 10 && y < 10 && x >= 0 && y >= 0) || ships[x][y].model != -1);

        attackedGrid[x][y] = 1;

        return new Coordinate(x, y);
    }

    @Override
    public void resultOfAttack(int result) {
        int sunk = -2;

        if (result == HIT_PATROL_BOAT) {
            ships[x][y].model = PATROL_BOAT;
        } else if (result == HIT_DESTROYER) {
            ships[x][y].model = DESTROYER;
        } else if (result == HIT_SUBMARINE) {
            ships[x][y].model = SUBMARINE;
        } else if (result == HIT_BATTLESHIP) {
            ships[x][y].model = BATTLESHIP;
        } else if (result == HIT_AIRCRAFT_CARRIER) {
            ships[x][y].model = AIRCRAFT_CARRIER;
        } else if (result == SUNK_PATROL_BOAT) {
            ships[x][y].model = PATROL_BOAT;
            sunk = PATROL_BOAT;
        } else if (result == SUNK_DESTROYER) {
            ships[x][y].model = DESTROYER;
            sunk = DESTROYER;
        } else if (result == SUNK_SUBMARINE) {
            ships[x][y].model = SUBMARINE;
            sunk = SUBMARINE;
        } else if (result == SUNK_BATTLESHIP) {
            ships[x][y].model = BATTLESHIP;
            sunk = BATTLESHIP;
        } else if (result == SUNK_AIRCRAFT_CARRIER) {
            ships[x][y].model = AIRCRAFT_CARRIER;
            sunk = AIRCRAFT_CARRIER;
        }

        if (result == HIT_PATROL_BOAT
                || result == HIT_DESTROYER
                || result == HIT_SUBMARINE
                || result == HIT_BATTLESHIP
                || result == HIT_AIRCRAFT_CARRIER) {

            if (hit) {
                if (ships[x][y].model != ships[last.getX()][last.getY()].model) {
                    lastHitStack.push(last);
                    up = true;
                    down = true;
                    left = true;
                    right = true;
                }
            } else {
                up = true;
                down = true;
                left = true;
                right = true;
            }
            /*
             * will check which places are possible to attack and then try those and not the others
             */
            hit = true;
            last = new Coordinate(x, y);
            if (x + 1 >= 10) {
                right = false;
            } else if (x - 1 < 0) {
                left = false;
            }
            if (y + 1 >= 10) {
                up = false;
            } else if (y - 1 < 0) {
                down = false;
            }

        } else if (result == SUNK_PATROL_BOAT
                || result == SUNK_DESTROYER
                || result == SUNK_SUBMARINE
                || result == SUNK_BATTLESHIP
                || result == SUNK_AIRCRAFT_CARRIER) {
            hit = false;
            up = false;
            down = false;
            left = false;
            right = false;
            /*
             * will mark the ship on the board as sunk
             */
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (ships[i][j].model == sunk) {
                        ships[i][j].sunk = true;
                    }
                }
            }
            /*
             * If something is in the stack it will not focus on this ship
             */
            if (!lastHitStack.isEmpty()) {
                last = lastHitStack.pop();
                hit = true;
                up = true;
                down = true;
                right = true;
                left = true;
            }
        } else {
            ships[x][y].model = 106;

            if (hit) {
                if (right) {
                    right = false;
                } else if (left) {
                    left = false;
                } else if (up) {
                    up = false;
                } else if (down) {
                    down = false;
                }
            }
        }
    }

    @Override
    public void opponentAttack(Coordinate coord) {
    }

    @Override
    public void resultOfGame(int result) {
        if (result == WON) {
            win++;
        }

    }
}
