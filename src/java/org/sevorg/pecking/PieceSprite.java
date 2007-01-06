package org.sevorg.pecking;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import com.threerings.media.sprite.Sprite;

public class PieceSprite extends Sprite
{

    /** The dimensions of our sprite in pixels. */
    public static final int SIZE = 64;

    /**
     * Creates a piece sprite to display the supplied game piece.
     */
    public PieceSprite(PeckingObject.Piece piece)
    {
        super(SIZE, SIZE);
        updatePiece(piece);
    }

    /**
     * Called when the piece we are displaying has been updated.
     */
    public void updatePiece(PeckingObject.Piece piece)
    {
        // keep track of our piece
        _piece = piece;
        // set our location based on the location of the piece
        setLocation(_piece.x * SIZE, _piece.y * SIZE);
        // force a redraw in case our color changed but not our location
        invalidate();
    }

    @Override
    // from Sprite
    public void paint(Graphics2D gfx)
    {
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        // set our color depending on the player that owns this piece
        gfx.setColor(_piece.owner == PeckingConstants.BLUE ? Color.BLUE
                : Color.RED);
        // draw a filled in circle in our piece color
        int px = _bounds.x + 3, py = _bounds.y + 3;
        int pwid = _bounds.width - 6, phei = _bounds.height - 6;
        gfx.fillRect(px, py, pwid, phei);
        // then outline that rectangle in black
        gfx.setColor(Color.black);
        gfx.drawRect(px, py, pwid, phei);
        gfx.setFont(new Font("Helvetica", Font.BOLD, 48));
        String name;
        if(_piece.type == PeckingConstants.CAGE){
            name = "B";
        }else if(_piece.type == PeckingConstants.WORM){
            name = "F";
        }else if(_piece.rank == 10){
            name = "S";
        }else{
            name = "" + _piece.rank;
        }
        gfx.drawString(name, _bounds.x + 20, _bounds.y + 48);
    }

    private PeckingObject.Piece _piece;
}
