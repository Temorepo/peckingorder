package org.sevorg.pecking.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import org.sevorg.pecking.PeckingConstants;
import org.sevorg.pecking.data.PeckingObject;
import org.sevorg.pecking.data.PeckingPiecesObject;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.presents.dobj.ElementUpdateListener;
import com.threerings.presents.dobj.ElementUpdatedEvent;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;
import com.threerings.toybox.util.ToyBoxContext;

public class ReadyCheckBox extends JCheckBox implements PlaceView,
        ElementUpdateListener, ActionListener, PeckingConstants, SetListener
{

    public ReadyCheckBox(String text,
                         ToyBoxContext ctx,
                         PeckingController ctrl,
                         boolean forLocalPlayer)
    {
        super(text);
        _ctx = ctx;
        _ctrl = ctrl;
        _forLocalPlayer = forLocalPlayer;
        _ctrl.addPeckingPiecesListener(this);
        addActionListener(this);
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent e)
    {
        _ctrl.toggleReadyToPlay();
    }

    public void willEnterPlace(PlaceObject plobj)
    {
        _gameobj = (PeckingObject)plobj;
        _gameobj.addListener(this);
        // determine our piece color (-1 if we're not a player)
        int playerColor = _gameobj.getPlayerIndex(((ToyBoxContext)_ctx).getUsername());
        if(_forLocalPlayer) {
            _color = playerColor;
        } else if(playerColor == RED) {
            _color = BLUE;
        } else {
            _color = RED;
        }
    }

    public void didLeavePlace(PlaceObject plobj)
    {}

    public void elementUpdated(ElementUpdatedEvent event)
    {
        if(event.getName().equals(PeckingObject.READY_TO_PLAY)) {
            if(event.getIndex() == _color) {
                setSelected((Boolean)event.getValue());
            }
            // When both are ready to play, disable checkbox
            for(int i = 0; i < _gameobj.readyToPlay.length; i++) {
                if(!_gameobj.readyToPlay[i]) {
                    return;
                }
            }
            setEnabled(false);
        }
    }

    public void entryAdded(EntryAddedEvent event)
    {
        entryChanged(event.getName());
    }

    public void entryRemoved(EntryRemovedEvent event)
    {}

    public void entryUpdated(EntryUpdatedEvent event)
    {
        entryChanged(event.getName());
    }

    private void entryChanged(String eventName)
    {
        if(_gameobj.phase == SETUP && _forLocalPlayer
                && eventName.equals(PeckingPiecesObject.PIECES)) {
            setEnabled(_ctrl.createLogic().allOnBoard(_color));
        }
    }

    private boolean _forLocalPlayer;

    private int _color;

    private ToyBoxContext _ctx;

    private PeckingObject _gameobj;

    private PeckingController _ctrl;
}
