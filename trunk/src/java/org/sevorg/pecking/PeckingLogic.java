package org.sevorg.pecking;

import java.awt.Point;
import java.util.List;
import org.sevorg.pecking.data.PeckingPiece;
import com.threerings.presents.dobj.DSet;

public abstract class PeckingLogic implements PeckingConstants
{

    public PeckingLogic(DSet<PeckingPiece> pieces)
    {
        this._pieces = pieces;
    }
    

    public abstract List<Point> getLegalMoves(PeckingPiece p);

    public PeckingPiece getPieceAt(int x, int y)
    {
        for(PeckingPiece p : _pieces) {
            if(p.x == x && p.y == y) {
                return p;
            }
        }
        return null;
    }

    /**
     * @return - an array containing the pieces that would change if src moved
     *         to x, y It can be a single piece if x, y is unoccupied or if the
     *         piece at x, y defeats src. If the piece at x, y is defeated by
     *         src, two pieces are returned. If this move is illegal, an array
     *         of length 0 is returned
     */
    public abstract PeckingPiece[] move(PeckingPiece src, int x, int y);


    public static boolean isInLake(int x, int y)
    {
        return (x == 2 || x == 3 || x == 6 || x == 7) && (y == 4 || y == 5);
    }

    public static boolean isOffBoard(PeckingPiece p)
    {
        return isOffBoard(p.x, p.y);
    }

    public static boolean isOffBoard(int x, int y)
    {
        return x >= 10 || x < 0 || y >= 10 || y < 0;
    }

    protected DSet<PeckingPiece> _pieces;
}
