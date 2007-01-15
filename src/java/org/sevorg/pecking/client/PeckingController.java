package org.sevorg.pecking.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sevorg.pecking.PeckingConstants;
import org.sevorg.pecking.PeckingLogic;
import org.sevorg.pecking.PeckingPlayLogic;
import org.sevorg.pecking.PeckingSetupLogic;
import org.sevorg.pecking.data.PeckingObject;
import org.sevorg.pecking.data.PeckingPiece;
import org.sevorg.pecking.data.PeckingPiecesObject;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;
import com.threerings.parlor.game.client.GameController;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.SetListener;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Manages the client side mechanics of the game.
 */
public class PeckingController extends GameController implements
        PeckingReceiver, Subscriber<PeckingPiecesObject>, PeckingConstants,
        SetListener
{

    public PeckingController()
    {
        addPeckingPiecesListener(this);
    }

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
            System.err.println("Yuh oh, we already have a pecking receiver registered and are trying for another...!");
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

    /**
     * @return - the color of the player handled by this controller, RED or BLUE
     */
    public int getColor()
    {
        if(_color == COLOR_UNKNOWN) {
            throw new IllegalStateException("The color isn't available until willEnterPlace has been called");
        }
        return _color;
    }

    @Override
    // from PlaceController
    public void didLeavePlace(PlaceObject plobj)
    {
        super.didLeavePlace(plobj);
        // clear out our game object reference
        _gameobj = null;
        selectedPiece = null;
        if(_pieces != null) {
            for(SetListener listener : peckingPiecesListeners) {
                _pieces.removeListener(listener);
            }
            _pieces.removeSubscriber(this);
            _pieces = null;
        }
        _ctx.getClient()
                .getInvocationDirector()
                .unregisterReceiver(PeckingDecoder.RECEIVER_CODE);
    }

    @Override
    // from PlaceController
    protected PlaceView createPlaceView(CrowdContext ctx)
    {
        return new PeckingPanel((ToyBoxContext)ctx, this);
    }

    /**
     * Called by the PeckingManager to indicate what oid the PeckingPiecesObject
     * can be found under since we set ourselves as the receiver in
     * willEnterPlace
     */
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
        for(SetListener listener : peckingPiecesListeners) {
            hookListenerIntoPieces(listener);
        }
        for(PeckingPiece piece : _pieces.pieces) {
            if(piece.rank == MARSHALL && piece.owner == _color) {
                setSelectedPiece(piece);
                break;
            }
        }
    }

    /**
     * @return - an instance of PeckingLogic appropriate for the current phase
     *         of the game, either {@link PeckingPlayLogic} or
     *         {@link PeckingSetupLogic}
     */
    public PeckingLogic createLogic()
    {
        if(_gameobj.phase == SETUP) {
            return new PeckingSetupLogic(_pieces.pieces);
        } else {
            return new PeckingPlayLogic(_pieces.pieces);
        }
    }

    /**
     * Adds listener on the pieces set from this player's PeckingPiecesObject
     * and fires EntryAddedEvents for each piece in it when it arrives. If
     * pieces has already arrived from the server, this just happens
     * immediately.
     */
    public void addPeckingPiecesListener(SetListener listener)
    {
        peckingPiecesListeners.add(listener);
        if(_pieces != null) {
            hookListenerIntoPieces(listener);
        }
    }

    public void removePeckingPiecesListener(SetListener listener)
    {
        if(peckingPiecesListeners.remove(listener) && _pieces != null) {
            _pieces.removeListener(listener);
        }
    }

    /**
     * Adds listener to _pieces and fires EntryAddedEvents for each piece in
     * _pieces
     */
    private void hookListenerIntoPieces(SetListener listener)
    {
        _pieces.addListener(listener);
        for(PeckingPiece piece : _pieces.pieces) {
            listener.entryAdded(new EntryAddedEvent<PeckingPiece>(_pieces.getOid(),
                                                                  PeckingPiecesObject.PIECES,
                                                                  piece));
        }
    }

    /**
     * Switch the state of the boolean in ready to play for this player in the
     * server
     */
    public void toggleReadyToPlay()
    {
        _gameobj.manager.invoke("toggleReadyToPlay");
    }

    /**
     * Move the selected piece to x, y
     */
    public void moveSelected(int x, int y)
    {
        // tell the server we want to move our selected piece to x y
        _gameobj.manager.invoke("movePiece", getSelectedPiece().id, x, y);
    }

    /**
     * Set the piece selected by the player to move next to <code>p</code>
     */
    public void setSelectedPiece(PeckingPiece p)
    {
        for(PieceSelectedListener listener : listeners) {
            if(selectedPiece != null) {
                listener.selectionChanged(selectedPiece, false);
            }
            listener.selectionChanged(p, true);
        }
        selectedPiece = p;
    }

    /**
     * @return - the currently selected piece
     */
    public PeckingPiece getSelectedPiece()
    {
        return selectedPiece;
    }

    public void addPieceSelectedListener(PieceSelectedListener listener)
    {
        listeners.add(listener);
    }

    public void removePieceSelectedListener(PieceSelectedListener listener)
    {
        listeners.remove(listener);
    }

    public void entryAdded(EntryAddedEvent event)
    {}

    public void entryRemoved(EntryRemovedEvent event)
    {}

    public void entryUpdated(EntryUpdatedEvent event)
    {
        //In case the selected piece has its data modified
        if(event.getName().equals(PeckingPiecesObject.PIECES)) {
            if(event.getEntry().equals(getSelectedPiece())) {
                setSelectedPiece((PeckingPiece)event.getEntry());
            }
        }
    }

    private List<PieceSelectedListener> listeners = new ArrayList<PieceSelectedListener>();

    private Set<SetListener> peckingPiecesListeners = new HashSet<SetListener>();

    /** Our game distributed object. */
    private PeckingObject _gameobj;

    private PeckingPiecesObject _pieces;

    private static final int COLOR_UNKNOWN = -3;

    private int _color = COLOR_UNKNOWN;

    private PeckingPiece selectedPiece;
}
