package org.sevorg.pecking;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.sevorg.pecking.data.PeckingPiece;

public class PeckingSetupLogic extends PeckingLogic
{

    public PeckingSetupLogic(Iterable<PeckingPiece> pieces)
    {
        super(pieces);
    }

    public List<Point> getLegalMoves(PeckingPiece p)
    {
        List<Point> points = new ArrayList<Point>();
        if(p.owner == RED) {
            for(int i = 0; i < 10; i++) {
                for(int j = RED_MIN; j <= RED_MAX; j++) {
                    points.add(new Point(i, j));
                }
            }
        } else {
            for(int i = 0; i < 10; i++) {
                for(int j = BLUE_MIN; j <= BLUE_MAX; j++) {
                    points.add(new Point(i, j));
                }
            }
        }
        return points;
    }

    /**
     * @return - an array containing the pieces that would change if src moved
     *         to x, y It can be a single piece if x, y is unoccupied. If
     *         there's a piece at x, y, two pieces are returned. If this move is
     *         illegal, an array of length 0 is returned
     */
    public PeckingPiece[] move(PeckingPiece src, int x, int y)
    {
        if(isOffBoard(x, y)) {
            return new PeckingPiece[] {src.copyOffBoard(false)};
        }
        if((src.owner == RED && y < RED_MIN) || (src.owner == BLUE && y > BLUE_MAX)) {
            return new PeckingPiece[] {};
        }
        PeckingPiece dest = getPieceAt(x, y);
        if(dest == null) {
            // Simplest case, we're moving to an empty dest, so just move src there
            return new PeckingPiece[] {src.copyWithNewPosition(x, y)};
        }
        // Otherwise, swap src and dest
        return new PeckingPiece[] {src.copyWithNewPosition(x, y),
                                   dest.copyWithNewPosition(src.x, src.y)};
    }
}
