package org.sevorg.pecking.server;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.sevorg.pecking.PeckingConstants;
import org.sevorg.pecking.PeckingLogic;
import org.sevorg.pecking.client.PeckingBoardViewTest;
import org.sevorg.pecking.data.PeckingObject;
import org.sevorg.pecking.data.PeckingPiece;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.parlor.game.server.GameManager;
import com.threerings.parlor.turn.server.TurnGameManager;
import com.threerings.parlor.turn.server.TurnGameManagerDelegate;
import com.threerings.toybox.data.ToyBoxGameConfig;

/**
 * Handles the server side of the game.
 */
public class PeckingManager extends GameManager implements PeckingConstants,
        TurnGameManager
{

    public PeckingManager()
    {
        // we're a turn based game, so we use a turn game manager delegate
        addDelegate(_turndel = new TurnGameManagerDelegate(this));
    }

    @Override
    // from PlaceManager
    public void didInit()
    {
        super.didInit();
        // get a casted reference to our game configuration
        _gameconf = (ToyBoxGameConfig)_config;
        // this is called when our manager is created but before any
        // game-specific actions take place; we don't yet have our game object
        // at this point but we do have our game configuration
    }

    @Override
    // from PlaceManager
    public void didStartup()
    {
        super.didStartup();
        // grab our own casted game object reference
        _gameobj = (PeckingObject)super._gameobj;
        // this method is called after we have created our game object but
        // before we do any game related things
    }

    @Override
    // from PlaceManager
    public void didShutdown()
    {
        super.didShutdown();
        // this is called right before we finally disappear for good
    }

    @Override
    // from PlaceManager
    protected PlaceObject createPlaceObject()
    {
        return new PeckingObject();
    }

    @Override
    // from GameManager
    protected void gameWillStart()
    {
        super.gameWillStart();
        // when all the players have entered the game room, the game is
        // automatically started and this method is called just before the
        // event is delivered to the clients that will start the game
        // this is the place to do any pre-game setup that needs to be done
        // each time a game is started rather than just once at the very
        // beginning (those sorts of things should be done in didStartup())
        PeckingPiece[] pieces = PeckingBoardViewTest.createPieces()
                .toArray(new PeckingPiece[0]);
        Set used = new HashSet();
        Random r = new Random();
        int redCount = 0, blueCount = 0;
        for(int i = 0; i < pieces.length; i++) {
            if(pieces[i].owner == RED) {
                pieces[i].x = redCount % 10;
                pieces[i].y = redCount / 10;
                redCount++;
            } else {
                pieces[i].x = blueCount % 10;
                pieces[i].y = 9 - blueCount / 10;
                blueCount++;
            }
            // Give each piece a distinct, random id
            // The id can't just be the array index(or any other constant
            // assignment) as that would allow a cheating client to figure out
            // the rank of a piece given its id
            int perhapsId;
            do {
                perhapsId = r.nextInt();
            } while(used.contains(perhapsId));
            used.add(perhapsId);
            pieces[i].id = perhapsId;
            _gameobj.addToPieces(pieces[i]);
        }
    }

    @Override
    // from GameManager
    protected void gameDidEnd()
    {
        super.gameDidEnd();
        // this is called after the game has ended. somewhere in the game
        // manager a call to endGame() should be made when the manager knows
        // the game to be over and that will trigger the end-of-game processing
        // including calling this method
    }

    @Override
    // from GameManager
    protected void assignWinners(boolean[] winners)
    {
        super.assignWinners(winners);
        PeckingLogic logic = new PeckingLogic(_gameobj.pieces);
        // A player is a winner if his opponents worm is off the board and
        // either he can move, or both he and his opponent can't move which is a
        // draw
        winners[RED] = logic.isWinner(RED);
        winners[BLUE] = logic.isWinner(BLUE);
    }

    public void turnDidEnd()
    {
        if(new PeckingLogic(_gameobj.pieces).shouldEndGame()) {
            endGame();
        }
    }

    public void turnDidStart()
    {}

    public void turnWillStart()
    {}

    public void movePiece(BodyObject player, PeckingPiece p, int x, int y)
    {
        // make sure it's this player's turn
        int pidx = _turndel.getTurnHolderIndex();
        if(_playerOids[pidx] != player.getOid()) {
            System.err.println("Requested to place piece by non-turn holder "
                    + "[who=" + player.who() + ", turnHolder="
                    + _gameobj.turnHolder + "].");
            return;
        }
        PeckingLogic logic = new PeckingLogic(_gameobj.pieces);
        System.err.println("We were told to move " + p + " to " + x + " " + y);
        if(!logic.isLegal(p, x, y)) {
            System.err.println("Received illegal move request " + "[who="
                    + player.who() + ", piece=" + p + "].");
            return;
        }
        logic.move(p, x, y, _gameobj);
        _turndel.endTurn();
    }

    /** Our game object. */
    protected PeckingObject _gameobj;

    /** Our game configuration. */
    protected ToyBoxGameConfig _gameconf;

    /** Handles our turn based game flow. */
    protected TurnGameManagerDelegate _turndel;
}
