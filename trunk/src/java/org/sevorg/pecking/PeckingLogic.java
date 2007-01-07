package org.sevorg.pecking;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.sevorg.pecking.PeckingObject.Piece;

public class PeckingLogic implements PeckingConstants
{

    public PeckingLogic(Piece[] pieces)
    {
        this._pieces = pieces;
    }

    public boolean addIfLegalAndCanContinue(Piece pie,
                                            int x,
                                            int y,
                                            List<Point> points)
    {
        Point poi = new Point(x, y);
        if(isLegal(pie, poi)) {
            points.add(poi);
        } else {
            return false;
        }
        return getPieceAt(poi) == null;
    }

    public List<Point> getLegalMoves(Piece p)
    {
        List<Point> points = new ArrayList<Point>();
        if(p.rank == CAGE || p.rank == WORM) {
            // Can't move at all
        } else if(p.rank == SCOUT) {
            int x = p.x, y = p.y;
            // Use this hideous looping construct since we don't have first
            // class functions
            while(addIfLegalAndCanContinue(p, --x, y, points)) {}
            x = p.x;
            while(addIfLegalAndCanContinue(p, ++x, y, points)) {}
            x = p.x;
            while(addIfLegalAndCanContinue(p, x, ++y, points)) {}
            y = p.y;
            while(addIfLegalAndCanContinue(p, x, --y, points)) {}
        } else {
            Point[] cardinalPoints = new Point[] {new Point(p.x + 1, p.y),
                                                  new Point(p.x - 1, p.y),
                                                  new Point(p.x, p.y + 1),
                                                  new Point(p.x, p.y - 1)};
            for(Point point : cardinalPoints) {
                if(isLegal(p, point)) {
                    points.add(point);
                }
            }
        }
        return points;
    }

    private boolean isLegal(Piece piece, Point to)
    {
        if(isOffBoard(to) || isInLake(to)) {
            return false;
        }
        Piece other = getPieceAt(to);
        return other == null || other.owner != piece.owner;
    }

    private boolean isInLake(Point to)
    {
        return (to.x == 2 || to.x == 3 || to.x == 6 || to.x == 7)
                && (to.y == 4 || to.y == 5);
    }

    private boolean isOffBoard(Point to)
    {
        return to.x >= 10 || to.x < 0 || to.y >= 10 || to.y < 0;
    }

    public Piece getPieceAt(Point poi)
    {
        int idx = getPieceIdxAt(poi);
        if(idx == -1) {
            return null;
        }
        return _pieces[idx];
    }

    public int getPieceIdxAt(Point poi)
    {
        for(int i = 0; i < _pieces.length; i++) {
            Piece p = _pieces[i];
            if(p.x == poi.x && p.y == poi.y) {
                return i;
            }
        }
        return -1;
    }

    private Piece[] _pieces;
}
