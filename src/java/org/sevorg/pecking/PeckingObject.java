//
// $Id$
package org.sevorg.pecking;

import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.turn.data.TurnGameObject;
import com.threerings.presents.dobj.DSet;
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

    /** The field name of the <code>pieces</code> field. */
    public static final String PIECES = "pieces";
    // AUTO-GENERATED: FIELDS END

    /**
     * Puts the piece of index newOccupentIdx at the position of the piece of
     * currentOccupantIdx and moves currentOccupantIdx off the board
     */
    public void replace(PeckingPiece currentOccupant, PeckingPiece newOccupant)
    {
        move(newOccupant, currentOccupant.x, currentOccupant.y);
        removeFromBoard(currentOccupant);
    }

    /**
     * Moves the piece p off the board
     */
    public void removeFromBoard(PeckingPiece p)
    {
       move(p, OFF_BOARD, OFF_BOARD);
    }

    /**
     * Moves the piece p to position x, y which should be free
     */
    public void move(PeckingPiece p, int x, int y)
    {
        System.err.println("Moving " + p + " to " + x + "," + y);
        PeckingPiece newPiece = new PeckingPiece(p.owner, p.rank, x, y);
        newPiece.id = p.id;
        updatePieces(newPiece);
    }

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

    /** Contains the pieces in the game. */
    public DSet<PeckingPiece> pieces = new DSet<PeckingPiece>();

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
     * Requests that the specified entry be added to the
     * <code>pieces</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToPieces (PeckingPiece elem)
    {
        requestEntryAdd(PIECES, pieces, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>pieces</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromPieces (Comparable key)
    {
        requestEntryRemove(PIECES, pieces, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>pieces</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updatePieces (PeckingPiece elem)
    {
        requestEntryUpdate(PIECES, pieces, elem);
    }

    /**
     * Requests that the <code>pieces</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setPieces (DSet<org.sevorg.pecking.PeckingPiece> value)
    {
        requestAttributeChange(PIECES, value, this.pieces);
        @SuppressWarnings("unchecked") DSet<org.sevorg.pecking.PeckingPiece> clone =
            (value == null) ? null : value.typedClone();
        this.pieces = clone;
    }
    // AUTO-GENERATED: METHODS END
}
