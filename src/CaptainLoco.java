
import java.util.Random;

/**
 * <p>I'm captain LOCO and I have much to learn about Battleship but you can
 * copy me to get started. I am just about the simplest working captain one
 * could provide and will help you understand how to create your own captain.
 * All I do is place my ships randomly around the board and then attack
 * randomly.</p>
 *
 * <p>I do lots of silly things. <ul> <li>I don't bother to keep track of where
 * I have already attacked so I may attack the same square twice.</li> <li>I
 * don't pay attention when I get a successful hit and try to find the rest of
 * the ship that was hit.</li> <li>I don't keep track of where my opponent has
 * attacked so I can try and learn what they are doing and re-think my ship
 * placement next time I do battle with them.</li> <li>I don't keep track of
 * where my opponent put their ships so I can guide my attacks next time I face
 * them.</li> </ul></p>
 *
 * <p>This class is a good starting point for you own Captain. You may want to
 * copy it as you get started but you won't win if you don't change its
 * strategy!</p>
 *
 * @author Seth Dutter - dutters@uwstout.edu
 * @author Seth Berrier - berriers@uwstout.edu
 *
 * @version SPRING.2013
 */
public class CaptainLoco implements Captain {

    /**
     * A random number generated which I use for everything (because I'm LOCO!!)
     */
    protected Random generator;
    /**
     * My fleet (you probably want to keep this).
     */
    protected Fleet myFleet;

    /**
     * <p>We are starting a new battle so I need to setup my fleet. I'm crazy so
     * I will just randomly place my pieces around the board until I have a
     * valid board layout.<p>
     *
     * <p>I must do two things as I setup my board. <ol> <li>Place a piece at a
     * particular location in a particular orientation (vertical or
     * horizontal)</li> <li>Ensure that the piece is on the board and doesn't
     * overlap with any other pieces</li> </ol></p>
     *
     * <p>I'm not trying too hard so I will just place a piece at a random
     * location and a random orientation every time. Luckily, 'placeShip' will
     * tell me if it was valid. It returns false if something went wrong (i.e.
     * if my ship was outside the board or overlapped another piece I had
     * already placed). I'll just keep picking random locations and orientations
     * until it succeeds.</p>
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
     * Return my fleet that was constructed in initialize. This is a method you
     * probably don't need to change.
     *
     * @return A valid fleet representing my ship placements for this round.
     */
    @Override
    public Fleet getFleet() {
        return myFleet;
    }

    /**
     * Roll the dice and pick a square to attack. This is not very smart. At the
     * very lest you want to keep track of where you have already attacked so
     * you don't attack a square twice. You probably also want to remember what
     * the most recent attack was so you can use it in resultOfAttack().
     *
     * @return A valid coordinate on the playing field.
     */
    @Override
    public Coordinate makeAttack() {
        return new Coordinate(generator.nextInt(10), generator.nextInt(10));
    }

    /**
     * I just ignore the results of every attack. It's hard to even remember
     * where I last attacked but I'm LOCO!
     *
     * @param result A code from Constants that tells me all about the results
     * of my last attack.
     */
    @Override
    public void resultOfAttack(int result) {
        // Add code here to process the success/failure of attacks
    }

    /**
     * I don't care what my opponents do. I just ignore their attacks and don't
     * learn from them.
     *
     * @param coord The spot on the board where your opponent just attacked.
     */
    @Override
    public void opponentAttack(Coordinate coord) {
        // Add code here to process or record opponent attacks
    }

    /**
     * For me, result is usually LOST so why bother to keep track of it. You may
     * find it useful but I'm LOCO so I don't.
     *
     * @param result A code from Constants that will equal WON or LOST.
     */
    @Override
    public void resultOfGame(int result) {
        // Add code here to process the result of a game
    }
}
