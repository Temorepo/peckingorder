//
// $Id$
package org.sevorg.pecking;

import com.threerings.io.Streamable;
import com.threerings.parlor.game.data.GameObject;

/**
 * Maintains the shared state of the game.
 */
public class PeckingObject extends GameObject
{

    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>pieces</code> field. */
    public static final String PIECES = "pieces";
    // AUTO-GENERATED: FIELDS END

    public static class Piece implements Streamable
    {

        public Piece()
        {}

        public Piece(int owner, int rank)
        {
            this.owner = owner;
            this.rank = rank;
        }

        public int owner;

        public int x = PeckingConstants.OFF_BOARD,
                y = PeckingConstants.OFF_BOARD;

        public int rank = PeckingConstants.UNKNOWN;

    }

    /** Contains the pieces in the game. */
    public Piece[] pieces = new Piece[PeckingConstants.NUM_PIECES];

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>pieces</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setPieces (PeckingObject.Piece[] value)
    {
        PeckingObject.Piece[] ovalue = this.pieces;
        requestAttributeChange(
            PIECES, value, ovalue);
        this.pieces = (value == null) ? null : (PeckingObject.Piece[])value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>pieces</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    public void setPiecesAt (PeckingObject.Piece value, int index)
    {
        PeckingObject.Piece ovalue = this.pieces[index];
        requestElementUpdate(
            PIECES, index, value, ovalue);
        this.pieces[index] = value;
    }
    // AUTO-GENERATED: METHODS END
}
