package org.sevorg.pecking.data;

import org.sevorg.pecking.PeckingConstants;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;

/**
 * Holds a client specific view of the state of a game
 * 
 * @author groves
 * 
 * Created on Jan 7, 2007
 */
public class PeckingPiecesObject extends DObject implements PeckingConstants
{

    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>pieces</code> field. */
    public static final String PIECES = "pieces";

    // AUTO-GENERATED: FIELDS END
    /**
     * Contains a view of the pieces in the game for a particular client. The
     * server, being omniscient, has a set with all fields filled in. Each of
     * the clients has PeckingPieces in this DSet with rank UNKNOWN if the piece
     * belongs to their opponent and it hasn't been revealed yet.
     * 
     */
    public DSet<PeckingPiece> pieces = new DSet<PeckingPiece>();

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the specified entry be added to the <code>pieces</code>
     * set. The set will not change until the event is actually propagated
     * through the system.
     */
    public void addToPieces(PeckingPiece elem)
    {
        requestEntryAdd(PIECES, pieces, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from the
     * <code>pieces</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void removeFromPieces(Comparable key)
    {
        requestEntryRemove(PIECES, pieces, key);
    }

    /**
     * Requests that the specified entry be updated in the <code>pieces</code>
     * set. The set will not change until the event is actually propagated
     * through the system.
     */
    public void updatePieces(PeckingPiece elem)
    {
        requestEntryUpdate(PIECES, pieces, elem);
    }

    /**
     * Requests that the <code>pieces</code> field be set to the specified
     * value. Generally one only adds, updates and removes entries of a
     * distributed set, but certain situations call for a complete replacement
     * of the set value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on clients) will
     * apply the value change when they received the attribute changed
     * notification.
     */
    public void setPieces(DSet<org.sevorg.pecking.data.PeckingPiece> value)
    {
        requestAttributeChange(PIECES, value, this.pieces);
        @SuppressWarnings("unchecked")
        DSet<org.sevorg.pecking.data.PeckingPiece> clone = (value == null) ? null
                : value.typedClone();
        this.pieces = clone;
    }
    // AUTO-GENERATED: METHODS END
}
