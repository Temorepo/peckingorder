package org.sevorg.pecking;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
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
        // Find owner's worm and simultaneously check if the other player has
        // any cage openers
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
        // Now that we know that there aren't any CAGE_OPENER pieces for the
        // other player, check that all of the points around the worm are either
        // cages or off the board
        int[][] surroundingPoints = new int[][] { {worm.x + 1, worm.y},
                                                 {worm.x - 1, worm.y},
                                                 {worm.x, worm.y + 1},
                                                 {worm.x, worm.y - 1}};
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

    public Set<Point> getLegalMoves(PeckingPiece p)
    {
        Set<Point> points = new HashSet<Point>();
        if(p == null || isImmobile(p)) {
            return points;
        }
        for(int x = p.x - 1; isValid(p, x, p.y); x--) {
            points.add(new Point(x, p.y));
            if(getPieceAt(x, p.y) != null || p.rank != SCOUT) {
                break;
            }
        }
        for(int x = p.x + 1; isValid(p, x, p.y); x++) {
            points.add(new Point(x, p.y));
            if(getPieceAt(x, p.y) != null || p.rank != SCOUT) {
                break;
            }
        }
        for(int y = p.y - 1; isValid(p, p.x, y); y--) {
            points.add(new Point(p.x, y));
            if(getPieceAt(p.x, y) != null || p.rank != SCOUT) {
                break;
            }
        }
        for(int y = p.y + 1; isValid(p, p.x, y); y++) {
            points.add(new Point(p.x, y));
            if(getPieceAt(p.x, y) != null || p.rank != SCOUT) {
                break;
            }
        }
        return points;
    }

    /**
     * @return - true if piece can move into x, y ie that it's on the board, not
     *         in the lakes and either unoccupied or occupied by the other
     *         player's pieces. This doesn't check if piece can physically move
     *         to x, y, just that it could conceivably be there
     */
    private boolean isValid(PeckingPiece piece, int x, int y)
    {
        if(isOffBoard(x, y) || isInLake(x, y)) {
            return false;
        }
        PeckingPiece other = getPieceAt(x, y);
        return other == null || other.owner != piece.owner;
    }

    /**
     * Checks if x, y is a valid place for piece to move to on the board.
     */
    private boolean isLegal(PeckingPiece piece, int x, int y)
    {
        return getLegalMoves(piece).contains(new Point(x, y));
    }

    /**
     * Checks if piece can't move, ie if it's a worm or cage
     */
    public static boolean isImmobile(PeckingPiece piece)
    {
        return piece.rank == WORM || piece.rank == CAGE;
    }

    /**
     * @return - an array containing the pieces that would change if src moved
     *         to x, y. It can be a single piece if x, y is unoccupied. If the
     *         piece at x, y defeats src or the piece at x, y is defeated by
     *         src, two pieces are returned. The piece that's moving off the
     *         board is always returned first. If this move is illegal, an array
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
     * @return - an array of length 2 with the piece src at the position of the
     * piece at dest and dest off the board
     */
    private PeckingPiece[] replace(PeckingPiece dest, PeckingPiece src)
    {
        return new PeckingPiece[] {dest.copyOffBoard(),
                                   src.copyWithNewPosition(dest.x, dest.y, true)};
    }
}
