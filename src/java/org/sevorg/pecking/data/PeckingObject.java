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
    // AUTO-GENERATED: METHODS END
}
