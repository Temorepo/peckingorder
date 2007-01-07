package org.sevorg.pecking;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.sevorg.pecking.PeckingObject.Piece;
import com.threerings.presents.dobj.DSet;

public class PeckingLogic implements PeckingConstants
{

    public PeckingLogic(DSet<Piece> pieces)
    {
        this._pieces = pieces;
    }

    public List<Point> getLegalMoves(Piece p)
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

    public Piece getPieceAt(int x, int y)
    {
        for(Piece p : _pieces) {
            if(p.x == x && p.y == y) {
                return p;
            }
        }
        return null;
    }

    private boolean addIfLegalAndCanContinue(Piece pie,
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

    public boolean isLegal(Piece piece, int x, int y)
    {
        if(isOffBoard(x, y) || isInLake(x, y) || isImmobile(piece)) {
            return false;
        }
        Piece other = getPieceAt(x, y);
        return other == null || other.owner != piece.owner;
    }

    private boolean isImmobile(Piece piece)
    {
        return piece.rank == WORM || piece.rank == CAGE;
    }

    public void move(Piece src, int x, int y, PeckingObject _gameobj)
    {
        Piece dest = getPieceAt(x, y);
        if(dest == null) {
            // Simplest case, we're moving to an empty dest
            _gameobj.move(src, x, y);
            return;
        }
        if(dest.rank == CAGE) {
            if(src.rank == CAGE_OPENER) {
                _gameobj.replace(dest, src);
            } else {
                _gameobj.removeFromBoard(src);
            }
        } else if((src.rank == ASSASSIN && dest.rank == MARSHALL)
                || (src.rank < dest.rank)) {
            _gameobj.replace(dest, src);
        } else if(src.rank == dest.rank) {
            _gameobj.removeFromBoard(src);
            _gameobj.removeFromBoard(dest);
        } else {
            _gameobj.removeFromBoard(src);
        }
    }

    public static boolean isInLake(int x, int y)
    {
        return (x == 2 || x == 3 || x == 6 || x == 7) && (y == 4 || y == 5);
    }

    public static boolean isOffBoard(int x, int y)
    {
        return x >= 10 || x < 0 || y >= 10 || y < 0;
    }

    private DSet<Piece> _pieces;
}
