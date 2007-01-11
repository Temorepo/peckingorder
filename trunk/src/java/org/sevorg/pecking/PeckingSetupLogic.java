package org.sevorg.pecking;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.sevorg.pecking.data.PeckingPiece;
import com.threerings.presents.dobj.DSet;

public class PeckingSetupLogic extends PeckingLogic
{

    public PeckingSetupLogic(DSet<PeckingPiece> pieces)
    {
        super(pieces);
    }

    public List<Point> getLegalMoves(PeckingPiece p)
    {
        List<Point> points = new ArrayList<Point>();
        if(p.owner == RED) {
            for(int i = 0; i < 10; i++) {
                for(int j = 6; j < 10; j++) {
                    points.add(new Point(i, j));
                }
            }
        } else {
            for(int i = 0; i < 10; i++) {
                for(int j = 0; j < 4; j++) {
                    points.add(new Point(i, j));
                }
            }
        }
        return points;
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
        if(isOffBoard(x, y)) {
            return new PeckingPiece[] {src.copyOffBoard(false)};
        }
        if((src.owner == RED && y < 6) || (src.owner == BLUE && y > 3)) {
            return new PeckingPiece[] {};
        }
        PeckingPiece dest = getPieceAt(x, y);
        if(dest == null) {
            // Simplest case, we're moving to an empty dest
            return new PeckingPiece[] {src.copyWithNewPosition(x, y)};
        }
        return new PeckingPiece[] {src.copyWithNewPosition(x, y),
                                   dest.copyOffBoard(false)};
    }
}
