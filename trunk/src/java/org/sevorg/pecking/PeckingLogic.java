package org.sevorg.pecking;

import java.awt.Point;
import java.util.Set;
import org.sevorg.pecking.data.PeckingPiece;

/**
 * Base class for determining the rules in Pecking Order.
 * {@link PeckingPlayLogic} handles the rules during a game while
 * {@link PeckingSetupLogic} handles the possibilites during setup
 */
public abstract class PeckingLogic implements PeckingConstants
{

    public PeckingLogic(Iterable<PeckingPiece> pieces)
    {
        this._pieces = pieces;
    }

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
     * @return - true if all pieces of the given color are on the board
     */
    public boolean allOnBoard(int color)
    {
        for(PeckingPiece p : _pieces) {
            if(p.owner == color && isOffBoard(p)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return - All possible x, y combinations p can move to
     */
    public abstract Set<Point> getLegalMoves(PeckingPiece p);

    /**
     * @return - all pieces involved if src moved to x, y
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

    protected Iterable<PeckingPiece> _pieces;
}
