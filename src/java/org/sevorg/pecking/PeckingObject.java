//
// $Id$
package org.sevorg.pecking;

import com.threerings.io.Streamable;
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

    /** The field name of the <code>pieces</code> field. */
    public static final String PIECES = "pieces";
    // AUTO-GENERATED: FIELDS END

    public static class Piece implements Streamable
    {

        /**
         * Creates an unowned, unranked piece positioned off the board
         */
        public Piece()
        {}

        /**
         * Creates a piece owned by owner of rank rank positioned off the board
         */
        public Piece(int owner, int rank)
        {
            this(owner, rank, OFF_BOARD, OFF_BOARD);
        }

        /**
         * Creates a piece owned by owner of rank rank positioned at x y
         */
        public Piece(int owner, int rank, int x, int y)
        {
            this.owner = owner;
            this.rank = rank;
            this.x = x;
            this.y = y;
        }

        /*
         * The player that owns the piece. Either RED or BLUE.
         */
        public int owner;

        public int x = OFF_BOARD, y = OFF_BOARD;

        /*
         * The strength of a piece in a fight, starting with 1. The lower the
         * value, the stronger the piece. Some ranks have special meanings as
         * indicated in PeckingConstants.
         */
        public int rank = UNKNOWN;
    }

    /**
     * Puts the piece of index newOccupentIdx at the position of the piece of
     * currentOccupantIdx and moves currentOccupantIdx off the board
     */
    public void replace(int currentOccupantIdx, int newOccupantIdx)
    {
        setPiecesAt(new Piece(pieces[newOccupantIdx].owner,
                              pieces[newOccupantIdx].rank,
                              pieces[currentOccupantIdx].x,
                              pieces[currentOccupantIdx].y), newOccupantIdx);
        setPiecesAt(new Piece(pieces[currentOccupantIdx].owner,
                              pieces[currentOccupantIdx].rank),
                    currentOccupantIdx);
    }

    /**
     * Moves the piece at idx off the board
     */
    public void removeFromBoard(int idx)
    {
        setPiecesAt(new Piece(pieces[idx].owner, pieces[idx].rank), idx);
    }

    /**
     * Moves the piece at idx to position x, y which should be free
     */
    public void move(int idx, int x, int y)
    {
        setPiecesAt(new Piece(pieces[idx].owner, pieces[idx].rank, x, y), idx);
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
    public Piece[] pieces = new Piece[PeckingConstants.NUM_PIECES];

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
