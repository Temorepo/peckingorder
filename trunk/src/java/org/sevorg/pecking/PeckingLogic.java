package org.sevorg.pecking;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.sevorg.pecking.data.PeckingPiece;
import com.threerings.presents.dobj.DSet;

public class PeckingLogic implements PeckingConstants
{

    public PeckingLogic(DSet<PeckingPiece> pieces)
    {
        this._pieces = pieces;
    }

    public boolean isWinner(int owner)
    {
        int other = owner == RED ? BLUE : RED;
        // A player is a winner if his opponents worm is off the board or
        // either he can move, or both he and his opponent can't move which is a
        // draw
        return !isWormOffBoard(owner)
                && (isWormOffBoard(other) || hasLegalMoves(owner) || !hasLegalMoves(other));
    }

    public boolean shouldEndGame()
    {
        return isWormOffBoard(RED) || isWormOffBoard(BLUE)
                || !hasLegalMoves(RED) || !hasLegalMoves(BLUE);
    }

    public boolean isWormOffBoard(int owner)
    {
        for(PeckingPiece p : _pieces) {
            if(p.rank == WORM && p.owner == owner && isOffBoard(p)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasLegalMoves(int owner)
    {
        for(PeckingPiece p : _pieces) {
            if(p.owner == owner && getLegalMoves(p).size() > 0) {
                return true;
            }
        }
        return false;
    }

    public List<Point> getLegalMoves(PeckingPiece p)
    {
        List<Point> points = new ArrayList<Point>();
        if(isImmobile(p)) {
            // That makes calculating moves easy
        } else if(p.rank == SCOUT) {
            int x = p.x, y = p.y;
            while(addIfLegalAndCanContinue(p, --x, y, points)) {}
            x = p.x;
            while(addIfLegalAndCanContinue(p, ++x, y, points)) {}
            x = p.x;
            while(addIfLegalAndCanContinue(p, x, ++y, points)) {}
            y = p.y;
            while(addIfLegalAndCanContinue(p, x, --y, points)) {}
        } else {
            int[][] cardinalPoints = new int[][] { {p.x + 1, p.y},
                                                  {p.x - 1, p.y},
                                                  {p.x, p.y + 1},
                                                  {p.x, p.y - 1}};
            for(int[] point : cardinalPoints) {
                if(isLegal(p, point[0], point[1])) {
                    points.add(new Point(point[0], point[1]));
                }
            }
        }
        return points;
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

    private boolean addIfLegalAndCanContinue(PeckingPiece pie,
                                             int x,
                                             int y,
                                             List<Point> points)
    {
        if(isLegal(pie, x, y)) {
            points.add(new Point(x, y));
        } else {
            return false;
        }
        return getPieceAt(x, y) == null;
    }

    public boolean isLegal(PeckingPiece piece, int x, int y)
    {
        if(isOffBoard(x, y) || isInLake(x, y) || isImmobile(piece)) {
            return false;
        }
        PeckingPiece other = getPieceAt(x, y);
        return other == null || other.owner != piece.owner;
    }

    private boolean isImmobile(PeckingPiece piece)
    {
        return piece.rank == WORM || piece.rank == CAGE;
    }

    /**
     * @return - an array containing the pieces that would change if src moved
     *         to x, y It can be a single piece if x, y is unoccupied or if the
     *         piece at x, y defeats src. If the piece at x, y is defeated by
     *         src, two pieces are returned. If this move is illegal, an array
     *         of length 0 is returned
     */
    public PeckingPiece[] move(PeckingPiece src, int x, int y)
    {
        if(!isLegal(src, x, y)) {
            return new PeckingPiece[] {};
        }
        PeckingPiece dest = getPieceAt(x, y);
        if(dest == null) {
            // Simplest case, we're moving to an empty dest
            return new PeckingPiece[] {src.copyWithNewPosition(x, y)};
        }
        if(dest.rank == CAGE) {
            if(src.rank == CAGE_OPENER) {
                return replace(dest, src);
            } else {
                if(!dest.revealed) {
                    return new PeckingPiece[] {src.copyOffBoard(),
                                               dest.copyRevealed()};
                }
                return new PeckingPiece[] {src.copyOffBoard()};
            }
        } else if((src.rank == ASSASSIN && dest.rank == MARSHALL)
                || (src.rank < dest.rank)) {
            return replace(dest, src);
        } else if(src.rank == dest.rank) {
            return new PeckingPiece[] {src.copyOffBoard(), dest.copyOffBoard()};
        } else {
            if(!dest.revealed) {
                return new PeckingPiece[] {src.copyOffBoard(),
                                           dest.copyRevealed()};
            }
            return new PeckingPiece[] {src.copyOffBoard()};
        }
    }

    /**
     * return - an array of length 2 with the piece src at the position of the
     * piece at dest and dest off the board
     */
    private PeckingPiece[] replace(PeckingPiece dest, PeckingPiece src)
    {
        return new PeckingPiece[] {dest.copyOffBoard(),
                                   src.copyWithNewPosition(dest.x, dest.y, true)};
    }

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

    private DSet<PeckingPiece> _pieces;
}
