package org.sevorg.pecking.client;

import org.sevorg.pecking.data.PeckingObject;
import org.sevorg.pecking.data.PeckingPiece;
import org.sevorg.pecking.data.PeckingPiecesObject;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;
import com.threerings.parlor.card.Log;
import com.threerings.parlor.game.client.GameController;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Manages the client side mechanics of the game.
 */
public class PeckingController extends GameController implements
        PeckingReceiver, Subscriber<PeckingPiecesObject>
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
        if(_ctx.getClient().getClientObject().receivers.containsKey(PeckingDecoder.RECEIVER_CODE)) {
            Log.warning("Yuh oh, we already have a pecking  receiver registered and are trying for another...!");
            Thread.dumpStack();
        }
        _ctx.getClient()
                .getInvocationDirector()
                .registerReceiver(new PeckingDecoder(this));
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

    public void setPeckingPiecesObjectOid(int oid)
    {
        _ctx.getDObjectManager().subscribeToObject(oid, this);
    }

    public void requestFailed(int oid, ObjectAccessException cause)
    {
    // TODO - Figure out what causes this, what can be done....
    }

    public void objectAvailable(PeckingPiecesObject object)
    {
        _pieces = object;
        _panel._bview.setPieceObject(object);
    }

    public void move(PeckingPiece pie, int x, int y)
    {
        // tell the server we want to place our piece here
        _gameobj.manager.invoke("movePiece", pie.id, x, y);
    }

    /** Our game panel. */
    protected PeckingPanel _panel;

    /** Our game distributed object. */
    protected PeckingObject _gameobj;

    protected PeckingPiecesObject _pieces;

    protected int _color;
}
