

import java.util.Random;

public class CaptainAwesome implements Captain {
    // Called before each game to reset your ship locations.

    @Override
    public void initialize(int numMatches, int numCaptains, String opponent) {
        generator = new Random();
        myFleet = new Fleet();
        x = -1;
        y = 0;

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
        if (x >= 9) {
            x = 0;
            y++;
        } else {
            x++;
        }
        return new Coordinate(x, y);
    }

    // Informs you of the result of your most recent attack
    @Override
    public void resultOfAttack(int result) {
        // Add code here to process the success/failure of attacks
    }

    // Informs you of the position of an attack against you.
    @Override
    public void opponentAttack(Coordinate coord) {
        // Add code here to process or record opponent attacks
    }

    // Informs you of the result of the game.
    @Override
    public void resultOfGame(int result) {
        // Add code here to process the result of a game
    }
    int x, y;
    Random generator;
    Fleet myFleet;
}
