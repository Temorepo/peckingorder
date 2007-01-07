package org.sevorg.pecking.client;

import org.sevorg.pecking.data.PeckingObject;
import org.sevorg.pecking.data.PeckingPiece;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;
import com.threerings.parlor.game.client.GameController;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Manages the client side mechanics of the game.
 */
public class PeckingController extends GameController
{

    /**
     * Requests that we leave the game and return to the lobby.
     */
    public void backToLobby()
    {
        _ctx.getLocationDirector().moveBack();
    }

    @Override
    // from PlaceController
    public void willEnterPlace(PlaceObject plobj)
    {
        super.willEnterPlace(plobj);
        // get a casted reference to our game object
        _gameobj = (PeckingObject)plobj;
        // determine our piece color (-1 if we're not a player)
        _color = _gameobj.getPlayerIndex(((ToyBoxContext)_ctx).getUsername());
    }

    @Override
    // from PlaceController
    public void didLeavePlace(PlaceObject plobj)
    {
        super.didLeavePlace(plobj);
        // clear out our game object reference
        _gameobj = null;
    }

    @Override
    // from PlaceController
    protected PlaceView createPlaceView(CrowdContext ctx)
    {
        _panel = new PeckingPanel((ToyBoxContext)ctx, this);
        return _panel;
    }

    @Override
    // from GameController
    protected void gameDidStart()
    {
        super.gameDidStart();
        // here we can set up anything that should happen at the start of the
        // game
    }

    @Override
    // from GameController
    protected void gameDidEnd()
    {
        super.gameDidEnd();
        // here we can clear out anything that needs to be cleared out at the
        // end of a game
    }

    public void move(PeckingPiece pie, int x, int y)
    {
        // tell the server we want to place our piece here
        _gameobj.manager.invoke("movePiece", pie, x, y);
    }

    /** Our game panel. */
    protected PeckingPanel _panel;

    /** Our game distributed object. */
    protected PeckingObject _gameobj;
    
    protected int _color;
}
