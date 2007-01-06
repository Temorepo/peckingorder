//
// $Id$
package org.sevorg.pecking;

import java.util.ArrayList;
import java.util.List;
import com.threerings.parlor.game.data.GameObject;
import com.threerings.presents.dobj.DSet;

/**
 * Maintains the shared state of the game.
 */
public class PeckingObject extends GameObject implements PeckingConstants
{

    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>pieces</code> field. */
    public static final String PIECES = "pieces";

    // AUTO-GENERATED: FIELDS END
    public static class Piece implements DSet.Entry
    {
        
        public Piece(){}
        
        public Piece(int owner, int rank, int type){
            this.owner = owner;
            this.rank = rank;
            this.type = type;
        }

        public int pieceId;

        public int owner;

        public int x, y;

        public int rank;

        public int type;

        public Comparable getKey()
        {
            return pieceId;
        }
    }

    private static final int[] countByRank = new int[] {1,
                                                        1,
                                                        2,
                                                        3,
                                                        4,
                                                        4,
                                                        4,
                                                        5,
                                                        8,
                                                        1};

    public static List<Piece> createPieces()
    {
        List<Piece> pieces = new ArrayList<Piece>(40);
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < countByRank.length; j++) {
                for(int k = 0; k < countByRank[j]; k++) {
                    pieces.add(new Piece(i, j + 1, BIRD));
                }
            }
            pieces.add(new Piece(i, -1, WORM));
            for(int j = 0; j < 6; j++) {
                pieces.add(new Piece(i, -1, CAGE));
            }
        }
        return pieces;
    }

    /** Contains the pieces in the game. */
    public DSet<Piece> pieces = new DSet<Piece>();

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the specified entry be added to the <code>pieces</code>
     * set. The set will not change until the event is actually propagated
     * through the system.
     */
    public void addToPieces(PeckingObject.Piece elem)
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
    public void updatePieces(PeckingObject.Piece elem)
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
    public void setPieces(DSet<org.sevorg.pecking.PeckingObject.Piece> value)
    {
        requestAttributeChange(PIECES, value, this.pieces);
        @SuppressWarnings("unchecked")
        DSet<org.sevorg.pecking.PeckingObject.Piece> clone = (value == null) ? null
                : value.typedClone();
        this.pieces = clone;
    }
    // AUTO-GENERATED: METHODS END
}
