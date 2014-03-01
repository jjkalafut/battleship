
/**
 * <p>A fleet of Battleship pieces placed on a standard 10 by 10 board. There
 * are five ships in a standard Battleship fleet: the patrol boat, the
 * destroyer, the submarine, the battleship and the aircraft carrier. The class
 * helps you to place the pieces on the board in a valid configuration where all
 * of the ships are within the bounds and not overlapping. It also helps you to
 * check if a particular attack has hit a ship and if so, how that attack
 * affects the fleet (was a ship sunk, which ship was it, etc).</p>
 *
 * <p>You should NOT need to do anything to this class in order to participate
 * in the competition and modifying it is discouraged and may make your entry
 * malfunction.</p>
 *
 * @author Seth Dutter - dutters@uwstout.edu
 * @author Seth Berrier - berriers@uwstout.edu
 *
 * @version SPRING.2013
 */
public class Fleet implements Constants {

    /**
     * A fleet is ready once all five ships have been validly placed on the
     * board.
     */
    protected boolean isReady;
    /**
     * The five standard ships of a single Battleship game
     */
    protected Ship[] fleet;
    
    protected int attackValue;

    /**
     * Construct a new empty fleet with room for five ships.
     */
    public Fleet() {
        isReady = false;
        attackValue = MISS;
        fleet = new Ship[5];
    }

    /**
     * Get the ships in this fleet.
     *
     * @return The five ships in the fleet.
     */
    public Ship[] getFleet() {
        return fleet;
    }

    /**
     * Ensure the most recently placed ship is valid (does not overlap with any
     * other ships). As a side effect, isReady is set to true when the last ship
     * is placed and found to be valid.
     *
     * @param index The ship to check (should be the most recently placed ship
     * and cannot be null).
     * @return True if it does not invalidate the fleet, false otherwise.
     */
    protected boolean isValid(int index) {
        // Make sure this is a valid ship
        if (!fleet[index].isValid()) {
            return false;
        }

        // Compare it with every other ship in the fleet
        for (Ship s : fleet) {
            if (s != null && !s.equals(fleet[index]) && fleet[index].intersectsShip(s)) {
                return false;
            }
        }

        // If every other ship in the fleet is not null then the fleet is ready!
        isReady = true;
        for (Ship s : fleet) {
            if (s == null) {
                isReady = false;
            }
        }

        // The placement of this ship is valid
        return true;
    }

    /**
     * Check to see whether there is a ship at the given coordinate
     *
     * @param coord The position on the board to check.
     * @return True if there is a ship at those coordinates, false otherwise.
     */
    public boolean isShipAt(Coordinate coord) {
        for (Ship s : fleet) {
            if (s.isOnShip(coord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the fleet is ready (all ships validly placed on board)
     *
     * @return True if the fleet is ready, false otherwise.
     */
    public boolean isFleetReady() {
        return isReady;
    }

    /**
     * Return the appropriate attack code for an attack at the given coordinate
     *
     * @param coord The coordinates for which to generate an attack code.
     * @return A combination of the values in Constants to make an attack code.
     */
    public int attacked(Coordinate coord) {
        if (!isFleetReady()) {
            return DEFEATED;    // Forfeit if your fleet isn't complete
        }
        for (Ship s : fleet) {
            attackValue = s.attacked(coord);
            if (attackValue - (attackValue % 10) == HIT_MODIFIER) {
                return attackValue;
            } else if (attackValue - (attackValue % 10) == SUNK_MODIFIER) {
                for (Ship t : fleet) {
                    if (!t.isSunk()) {
                        return attackValue;
                    }
                }
                return DEFEATED;
            }
        }
        return MISS;
    }
    
    public int getLastAttackValue() {
        return attackValue;
    }

    /**
     * A convenience method that for calling attack without a Coordinate object
     *
     * @param x The column of the attack on this board.
     * @param y The row of the attack on this board.
     * @return The attack code generated by attacked(Coordinate(x, y))
     */
    public int attacked(int x, int y) {
        return attacked(new Coordinate(x, y));
    }

    /**
     * Place a ship on the board. Note that once a ship is placed it cannot be
     * moved.
     *
     * @param location The location of the topmost or leftmost peg hold for this
     * ship.
     * @param direction The orientation of the ship (VERTICAL or HORIZONTAL)
     * @param model The model of the ship (PATROL_BOAT, DESTROYER, SUBMARINE,
     * BATTLESHIP or AIRCRAFT_CARRIER)
     * @return True if the piece was successfully placed on the board, false
     * otherwise.
     */
    // Once a ship is placed it cannot be moved
    public boolean placeShip(Coordinate location, int direction, int model) {
        if (model < 6 && model > -1) {
            if (fleet[model] == null) {
                fleet[model] = new Ship(location, direction, model);
                if (isValid(model)) {
                    CaptainDebugger.addShip(location, direction, model, true);
                    return true;
                } else {
                    fleet[model] = null;
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * A convenience method to call placeShip(Coordinate(x, y), direction,
     * model).
     *
     * @param x The column of the location of the ship on the board.
     * @param y The row of the location of the ship on the board.
     * @param direction The orientation of the ship (VERTICAL or HORIZONTAL)
     * @param model The model of the ship (PATROL_BOAT, DESTROYER, SUBMARINE,
     * BATTLESHIP or AIRCRAFT_CARRIER)
     * @return The result of calling placeShip(Coordinate(x, y), direction,
     * model)
     */
    public boolean placeShip(int x, int y, int direction, int model) {
        return placeShip(new Coordinate(x, y), direction, model);
    }
}
