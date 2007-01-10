package org.sevorg.pecking.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import org.sevorg.pecking.PeckingConstants;
import org.sevorg.pecking.data.PeckingPiece;
import com.threerings.media.sprite.Sprite;

public abstract class PieceSprite extends Sprite implements PeckingConstants
{

    public PieceSprite()
    {
        super(SIZE, SIZE);
    }

    /** The dimensions of our sprite in pixels. */
    public static final int SIZE = 64;


    /**
     * Called when the piece we are displaying has changed.
     * @param piece 
     */
    public void update(PeckingPiece piece)
    {
        _piece = piece;
        invalidate();
    }
    
    @Override
    public void paint(Graphics2D gfx)
    {
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        // set our color depending on the player that owns this piece
        gfx.setColor(_piece.owner == PeckingConstants.BLUE ? Color.BLUE
                : Color.RED);
        // draw a filled in rectangle in our piece color
        int px = _bounds.x + 3, py = _bounds.y + 3;
        int pwid = _bounds.width - 6, phei = _bounds.height - 6;
        gfx.fillRect(px, py, pwid, phei);
        // then outline that rectangle in black
        if(selected){
            gfx.setColor(Color.yellow);
        }else{
            gfx.setColor(Color.black);
        }
        gfx.drawRect(px, py, pwid, phei);
        gfx.setFont(new Font("Helvetica", Font.BOLD, 48));
        String name;
        if(_piece.rank == UNKNOWN) {
            name = "";
        } else if(_piece.rank == CAGE) {
            name = "C";
        } else if(_piece.rank == WORM) {
            name = "W";
        } else if(_piece.rank == ASSASSIN) {
            name = "A";
        } else {
            name = "" + _piece.rank;
        }
        gfx.drawString(name, _bounds.x + 20, _bounds.y + 48);
    }
    
    public boolean selected = false;

    protected PeckingPiece _piece;
}