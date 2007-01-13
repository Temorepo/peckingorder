//
// $Id$
package org.sevorg.pecking.data;

import org.sevorg.pecking.PeckingConstants;
import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.turn.data.TurnGameObject;
import com.threerings.util.Name;

/**
 * Maintains the shared state of the game.
 */
public class PeckingObject extends GameObject implements TurnGameObject,
        PeckingConstants
{

    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>turnHolder</code> field. */
    public static final String TURN_HOLDER = "turnHolder";

    /** The field name of the <code>phase</code> field. */
    public static final String PHASE = "phase";

    /** The field name of the <code>readyToPlay</code> field. */
    public static final String READY_TO_PLAY = "readyToPlay";
    // AUTO-GENERATED: FIELDS END

    // from interface TurnGameObject
    public String getTurnHolderFieldName()
    {
        return TURN_HOLDER;
    }

    // from interface TurnGameObject
    public Name getTurnHolder()
    {
        return turnHolder;
    }

    // from interface TurnGameObject
    public Name[] getPlayers()
    {
        return players;
    }

    /** The username of the current turn holder or null. */
    public Name turnHolder;
    
    /** The current phase of the game.  Can be PLAY or SETUP. */
    public int phase;
    
    /** 
     * If a player is ready to play, ie move from SETUP to PLAY
     */
    public boolean[] readyToPlay = new boolean[]{false, false};

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>turnHolder</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setTurnHolder (Name value)
    {
        Name ovalue = this.turnHolder;
        requestAttributeChange(
            TURN_HOLDER, value, ovalue);
        this.turnHolder = value;
    }

    /**
     * Requests that the <code>phase</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setPhase (int value)
    {
        int ovalue = this.phase;
        requestAttributeChange(
            PHASE, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.phase = value;
    }

    /**
     * Requests that the <code>readyToPlay</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setReadyToPlay (boolean[] value)
    {
        boolean[] ovalue = this.readyToPlay;
        requestAttributeChange(
            READY_TO_PLAY, value, ovalue);
        this.readyToPlay = (value == null) ? null : (boolean[])value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>readyToPlay</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    public void setReadyToPlayAt (boolean value, int index)
    {
        boolean ovalue = this.readyToPlay[index];
        requestElementUpdate(
            READY_TO_PLAY, index, Boolean.valueOf(value), Boolean.valueOf(ovalue));
        this.readyToPlay[index] = value;
    }
    // AUTO-GENERATED: METHODS END
}
