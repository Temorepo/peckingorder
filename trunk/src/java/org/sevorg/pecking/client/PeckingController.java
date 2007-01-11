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
import com.threerings.parlor.card.Log;
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

    public int getColor()
    {
        return _color;
    }

    @Override
    // from PlaceController
    public void didLeavePlace(PlaceObject plobj)
    {
        super.didLeavePlace(plobj);
        // clear out our game object reference
        _gameobj = null;
        clearPieces();
    }

    @Override
    // from PlaceController
    protected PlaceView createPlaceView(CrowdContext ctx)
    {
        return new PeckingPanel((ToyBoxContext)ctx, this);
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
        // TODO - I was clearing out pieces at game end, but this is called when
        // the game starts from what I can tell
        // clearPieces();
    }

    private void clearPieces()
    {
        if(_pieces != null) {
            for(SetListener listener : peckingPiecesListeners) {
                _pieces.removeListener(listener);
            }
            _pieces.removeSubscriber(this);
            _pieces = null;
        }
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
        for(SetListener listener : peckingPiecesListeners) {
            object.addListener(listener);
            addAllPiecesToListener(listener);
        }
        for(PeckingPiece piece : _pieces.pieces) {
            if(piece.rank == MARSHALL && piece.owner == _color) {
                setSelectedPiece(piece);
                break;
            }
        }
    }

    public PeckingLogic createLogic()
    {
        if(_gameobj.phase == SETUP) {
            return new PeckingSetupLogic(_pieces.pieces);
        } else {
            return new PeckingPlayLogic(_pieces.pieces);
        }
    }

    public void addPeckingPiecesListener(SetListener listener)
    {
        peckingPiecesListeners.add(listener);
        if(_pieces != null) {
            addAllPiecesToListener(listener);
        }
    }

    public void removePeckingPiecesListener(SetListener listener)
    {
        if(_pieces != null && peckingPiecesListeners.remove(listener)) {
            _pieces.removeListener(listener);
        }
    }

    private void addAllPiecesToListener(SetListener listener)
    {
        if(_pieces == null) {
            return;
        }
        for(PeckingPiece piece : _pieces.pieces) {
            listener.entryAdded(new EntryAddedEvent<PeckingPiece>(_pieces.getOid(),
                                                                  PeckingPiecesObject.PIECES,
                                                                  piece));
        }
    }

    public void setReadyToPlay()
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

    private List<PieceSelectedListener> listeners = new ArrayList<PieceSelectedListener>();

    private Set<SetListener> peckingPiecesListeners = new HashSet<SetListener>();

    /** Our game distributed object. */
    private PeckingObject _gameobj;

    private PeckingPiecesObject _pieces;

    private int _color;

    private PeckingPiece selectedPiece;

    public void entryAdded(EntryAddedEvent event)
    {}

    public void entryRemoved(EntryRemovedEvent event)
    {}

    public void entryUpdated(EntryUpdatedEvent event)
    {
        if(event.getName().equals(PeckingPiecesObject.PIECES)) {
            if(event.getEntry().equals(getSelectedPiece())) {
                setSelectedPiece((PeckingPiece)event.getEntry());
            }
        }
    }
}
