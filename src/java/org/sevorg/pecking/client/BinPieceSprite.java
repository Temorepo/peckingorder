package org.sevorg.pecking.client;

import org.sevorg.pecking.data.PeckingPiece;

public class BinPieceSprite extends PieceSprite
{
    public BinPieceSprite(PeckingPiece piece, int x, int y){
        setLocation(x, y);
        update(piece);
    }
    
    @Override
    public void update(PeckingPiece piece)
    {
        _piece = piece;
        invalidate();
    }
}
