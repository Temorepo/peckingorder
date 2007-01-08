package org.sevorg.pecking.data;

import org.sevorg.pecking.PeckingConstants;
import com.threerings.presents.dobj.DSet;

public class PeckingPiece implements DSet.Entry, PeckingConstants
{

    /**
     * Creates an unowned, unranked piece positioned off the board
     */
    public PeckingPiece()
    {
        this(UNKNOWN, UNKNOWN);
    }

    /**
     * Creates a piece owned by owner of rank rank positioned off the board
     */
    public PeckingPiece(int owner, int rank)
    {
        this(owner, rank, OFF_BOARD, OFF_BOARD);
    }

    /**
     * Creates a piece owned by owner of rank rank positioned at x y
     */
    public PeckingPiece(int owner, int rank, int x, int y)
    {
        this(owner, rank, x, y, 0);
    }

    /**
     * Creates a piece owned by owner of rank rank positioned at x y
     */
    public PeckingPiece(int owner, int rank, int x, int y, int id)
    {
        this(owner, rank, x, y, id, rank != UNKNOWN);
    }

    /**
     * Creates a piece with the given attributes
     */
    public PeckingPiece(int owner,
                        int rank,
                        int x,
                        int y,
                        int id,
                        boolean revealed)
    {
        this.owner = owner;
        this.rank = rank;
        this.x = x;
        this.y = y;
        this.id = id;
        this.revealed = revealed;
    }

    /**
     * The player that owns the piece. Either RED, BLUE or UNKNOWN.
     */
    public int owner;

    public int x = OFF_BOARD, y = OFF_BOARD;

    public int id;

    /**
     * Used by the server to keep track if this pieces' rank should be disclosed
     * to all clients
     */
    public transient boolean revealed = false;

    /*
     * The strength of a piece in a fight, starting with 1. The lower the value,
     * the stronger the piece. Some ranks have special meanings as indicated in
     * PeckingConstants.
     */
    public int rank = UNKNOWN;

    public Comparable getKey()
    {
        return id;
    }

    public String toString()
    {
        return "Piece" + id + "(x=" + x + ",y=" + y + ",rank=" + rank
                + ",owner=" + owner + ")";
    }

    /**
     * @return - a copy of this PeckingPiece with its rank set to UNKNOWN
     */
    public PeckingPiece copyWithoutRank()
    {
        return new PeckingPiece(owner, UNKNOWN, x, y, id);
    }

    /**
     * @return - a copy of this piece with its revealed field set to true
     */
    public PeckingPiece copyRevealed()
    {
        return copyWithNewPosition(x, y, true);
    }

    /**
     * @return - a copy of this piece with x set to newX and y set to newY
     */
    public PeckingPiece copyWithNewPosition(int newX, int newY)
    {
        return copyWithNewPosition(newX, newY, revealed);
    }

    /**
     * @return - a copy of this piece with x set to newX and y set to newY and
     *         revealed set to newRevealed
     */
    public PeckingPiece copyWithNewPosition(int newX,
                                            int newY,
                                            boolean newRevealed)
    {
        return new PeckingPiece(owner, rank, newX, newY, id, newRevealed);
    }

    /**
     * @return - a copy of this piece with x and y set to OFF_BOARD and revealed set to true
     */
    public PeckingPiece copyOffBoard()
    {
        return copyWithNewPosition(OFF_BOARD, OFF_BOARD, true);
    }
}