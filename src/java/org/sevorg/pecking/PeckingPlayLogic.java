package org.sevorg.pecking;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.sevorg.pecking.data.PeckingPiece;

public class PeckingPlayLogic extends PeckingLogic
{

    public PeckingPlayLogic(Iterable<PeckingPiece> pieces)
    {
        super(pieces);
    }

    public boolean isWinner(int owner)
    {
        // A player is a winner if his opponents worm is off the board,
        // he can move while his opponent can't, or if his worm is unassailable
        // due to cages and an opponents lack of cage openers. If both he and
        // his opponent can't move, it's a draw so true is returned.
        int other = owner == RED ? BLUE : RED;
        return !isWormOffBoard(owner)
                && (isWormOffBoard(other) || !hasLegalMoves(other) || isWormCageProtected(owner));
    }

    public boolean shouldEndGame()
    {
        return isWinner(RED) || isWinner(BLUE);
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

    /**
     * @return - true if owner's worm is surrounded by cages or the board edge,
     *         and the other player has no CAGE_OPENERS
     */
    public boolean isWormCageProtected(int owner)
    {
        PeckingPiece worm = null;
        for(PeckingPiece p : _pieces) {
            if(p.rank == WORM && p.owner == owner) {
                worm = p;
            } else if(p.rank == CAGE_OPENER && p.owner != owner) {
                return false;
            }
        }
        if(isOffBoard(worm)) {
            return false;
        }
        int[][] surroundingPoints = getSurroundingPoints(worm);
        for(int[] point : surroundingPoints) {
            if(isOffBoard(point[0], point[1])) {
                // If a surrounding point is off the board, then the worm is
                // protected by the board edge
                continue;
            }
            PeckingPiece pointPiece = getPieceAt(point[0], point[1]);
            if(pointPiece == null || pointPiece.rank != CAGE) {
                return false;
            }
        }
        return true;
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
            int[][] cardinalPoints = getSurroundingPoints(p);
            for(int[] point : cardinalPoints) {
                if(isLegal(p, point[0], point[1])) {
                    points.add(new Point(point[0], point[1]));
                }
            }
        }
        return points;
    }

    private int[][] getSurroundingPoints(PeckingPiece p)
    {
        return new int[][] { {p.x + 1, p.y},
                            {p.x - 1, p.y},
                            {p.x, p.y + 1},
                            {p.x, p.y - 1}};
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

    private boolean isLegal(PeckingPiece piece, int x, int y)
    {
        if(isOffBoard(x, y) || isInLake(x, y) || isImmobile(piece)) {
            return false;
        }
        PeckingPiece other = getPieceAt(x, y);
        return other == null || other.owner != piece.owner;
    }

    public static boolean isImmobile(PeckingPiece piece)
    {
        return piece.rank == WORM || piece.rank == CAGE;
    }

    /**
     * @return - an array containing the pieces that would change if src moved
     *         to x, y It can be a single piece if x, y is unoccupied. If the
     *         piece at x, y defeats src or the piece at x, y is defeated by
     *         src, two pieces are returned. The piece that's moving off the
     *         board is always returned first.  If this move is illegal, an array
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
            }
            return new PeckingPiece[] {src.copyOffBoard(), dest.copyRevealed()};
        } else if((src.rank == ASSASSIN && dest.rank == MARSHALL)
                || src.rank < dest.rank) {
            return replace(dest, src);
        } else if(src.rank == dest.rank) {
            return new PeckingPiece[] {src.copyOffBoard(), dest.copyOffBoard()};
        } else {
            return new PeckingPiece[] {src.copyOffBoard(), dest.copyRevealed()};
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
}
