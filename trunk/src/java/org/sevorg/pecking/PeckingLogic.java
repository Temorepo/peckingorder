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

    public int getPieceIdx(Piece p)
    {
        for(int i = 0; i < _pieces.length; i++) {
            if(p == _pieces[i]){
                return i;
            }
        }
        return -1;
    }

    private boolean addIfLegalAndCanContinue(Piece pie,
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

    public boolean isLegal(Piece piece, Point to)
    {
        if(isOffBoard(to) || isInLake(to) || isImmobile(piece)) {
            return false;
        }
        Piece other = getPieceAt(to);
        return other == null || other.owner != piece.owner;
    }

    private boolean isImmobile(Piece piece)
    {
        return piece.rank == WORM || piece.rank == CAGE;
    }

    public void move(int idx, int x, int y, PeckingObject _gameobj)
    {
        Piece src = _gameobj.pieces[idx];
        int destIdx = getPieceIdxAt(new Point(x, y));
        if(destIdx == -1){
            // Simplest case, we're moving to an empty dest
            _gameobj.move(idx, x, y);
            return;
        }
        Piece dest = _gameobj.pieces[idx];
        if(dest.rank == CAGE) {
            if(src.rank == CAGE_OPENER) {
                _gameobj.replace(destIdx, idx);
            } else {
                _gameobj.removeFromBoard(idx);
            }
        } else if((src.rank == ASSASSIN && dest.rank == MARSHALL) ||
                (src.rank > dest.rank)) {
            _gameobj.replace(destIdx, idx);
        }  else if(src.rank == dest.rank) {
            _gameobj.removeFromBoard(idx);
            _gameobj.removeFromBoard(destIdx);
        } else {
            _gameobj.removeFromBoard(idx);
        }
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

    private Piece[] _pieces;
}
