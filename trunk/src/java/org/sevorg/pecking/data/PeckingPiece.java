package org.sevorg.pecking.data;

import org.sevorg.pecking.PeckingConstants;
import com.threerings.presents.dobj.DSet;

public class PeckingPiece implements DSet.Entry, PeckingConstants
{

    /**
     * Creates an unowned, unranked piece positioned off the board
     */
    public PeckingPiece()
    {}

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
     * Creates a piece owned by owner of rank rank positioned at x y
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

    /*
     * The player that owns the piece. Either RED or BLUE.
     */
    public int owner;

    public int x = OFF_BOARD, y = OFF_BOARD;

    public int id;

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
        return new PeckingPiece(owner, UNKNOWN, x, y, id, revealed);
    }

    public PeckingPiece copyWithNewPosition(int newX, int newY)
    {
        return new PeckingPiece(owner, rank, newX, newY, id, revealed);
    }

    public PeckingPiece copyOffBoard()
    {
        return copyWithNewPosition(OFF_BOARD, OFF_BOARD);
    }
}