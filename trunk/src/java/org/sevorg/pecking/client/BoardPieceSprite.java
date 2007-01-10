package org.sevorg.pecking.client;

import org.sevorg.pecking.PeckingConstants;
import org.sevorg.pecking.data.PeckingPiece;

public class BoardPieceSprite extends PieceSprite implements PeckingConstants
{

    /**
     * Creates a piece sprite to display the supplied game piece.
     */
    public BoardPieceSprite(PeckingPiece piece)
    {
        update(piece);
    }

    public void update(PeckingPiece piece)
    {
        // set our location based on the location of the piece
        setLocation(piece.x * SIZE, piece.y * SIZE);
        super.update(piece);
    }
}
