
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 *
 *
 * @author Cody Machak
 * @author John Crane
 * @author Will Brereton
 *
 * @version SPRING.2014
 *
 * GIVE ME LIBERTY, OR A BRAN MUFFIN!!!!!
 */
public class CaptainNotGonnaWin implements Captain {

    /**
     * A random number generator
     */
    protected Random generator;
    /**
     * My fleet
     */
    protected Fleet myFleet;
    public ArrayList<Coordinate> possibleAttack;

    /**
     * We are starting a new battle so we need to setup our fleet. We randomly
     * place our pieces around the board until we have a valid board layout.
     *
     * @param numMatches	The number matches you will be engaging in with this
     * opponent.
     * @param numCaptains	The number of opponents you will be facing during the
     * current set of battles.
     * @param opponent	The name of your opponent for this match
     */
    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
        generator = new Random();
        myFleet = new Fleet();
        possibleAttack = new ArrayList<Coordinate>(100);

        for (int k = 0; k < 10; k++) {
            for (int l = 0; l < 10; l++) {
                Coordinate toAdd = new Coordinate(k, l);
                possibleAttack.add(toAdd);
            }
        }


        //PLACE SHIPS
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
    }

    /**
     * Return my fleet that was constructed in initialize.
     *
     * @return A valid fleet representing my ship placements for this round.
     */
    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    //ATTACK AI
    /**
     * Attack pattern starts in the lower Right corner and moves up and over
     * until all spaces are attacked
     *
     * @return A valid coordinate on the playing field.
     */
    @Override
    public Coordinate makeAttack() {
        Coordinate attackLocation = new Coordinate(-1, -1);

        attackLocation = possibleAttack.remove(possibleAttack.size() - 1);

        return attackLocation;
    }

    /**
     * Unused by this captain
     *
     * @param result A code from Constants that tells me all about the results
     * of my last attack.
     */
    @Override
    public void resultOfAttack(int result) {
        // Add code here to process the success/failure of attacks
    }

    /**
     * Unused by this captain
     *
     * @param coord The spot on the board where your opponent just attacked.
     */
    @Override
    public void opponentAttack(Coordinate coord) {
        // Add code here to process or record opponent attacks
    }

    /**
     * Unused by this captain
     *
     * @param result A code from Constants that will equal WON or LOST.
     */
    @Override
    public void resultOfGame(int result) {
        // Add code here to process the result of a game
    }
}
