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

    public static class Piece implements DSet.Entry
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

        public int id;

        /*
         * The strength of a piece in a fight, starting with 1. The lower the
         * value, the stronger the piece. Some ranks have special meanings as
         * indicated in PeckingConstants.
         */
        public int rank = UNKNOWN;

        public Comparable getKey()
        {
            return id;
        }
        
        public String toString(){
            return "Piece " + id + " " + x + ", " + y;
        }
    }

    /**
     * Puts the piece of index newOccupentIdx at the position of the piece of
     * currentOccupantIdx and moves currentOccupantIdx off the board
     */
    public void replace(Piece currentOccupant, Piece newOccupant)
    {
        move(newOccupant, currentOccupant.x, currentOccupant.y);
        removeFromBoard(currentOccupant);
    }

    /**
     * Moves the piece p off the board
     */
    public void removeFromBoard(Piece p)
    {
       move(p, OFF_BOARD, OFF_BOARD);
    }

    /**
     * Moves the piece p to position x, y which should be free
     */
    public void move(Piece p, int x, int y)
    {
        System.err.println("Moving " + p + " to " + x + "," + y);
        Piece newPiece = new Piece(p.owner, p.rank, x, y);
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
    public DSet<Piece> pieces = new DSet<Piece>();

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
    public void addToPieces (PeckingObject.Piece elem)
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
    public void updatePieces (PeckingObject.Piece elem)
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
    public void setPieces (DSet<org.sevorg.pecking.PeckingObject.Piece> value)
    {
        requestAttributeChange(PIECES, value, this.pieces);
        @SuppressWarnings("unchecked") DSet<org.sevorg.pecking.PeckingObject.Piece> clone =
            (value == null) ? null : value.typedClone();
        this.pieces = clone;
    }
    // AUTO-GENERATED: METHODS END
}
