package org.sevorg.pecking.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import org.sevorg.pecking.PeckingConstants;
import org.sevorg.pecking.data.PeckingPiece;
import com.samskivert.swing.Label;
import com.threerings.media.sprite.Sprite;

public class PieceSprite extends Sprite implements PeckingConstants
{

    public PieceSprite()
    {
        super(SIZE, SIZE);
    }

    public PieceSprite(PeckingPiece piece, int x, int y)
    {
        this();
        update(piece, x, y);
    }

    /** The dimensions of our sprite in pixels. */
    public static final int SIZE = 48;

    public void setSelected(boolean newValue)
    {
        selected = newValue;
        invalidate();
    }

    /**
     * Called when the piece we are displaying has changed.
     * 
     * @param piece
     */
    public void update(PeckingPiece piece, int x, int y)
    {
        setLocation(x, y);
        update(piece);
    }

    public void update(PeckingPiece piece)
    {
        _piece = piece;
        String name;
        if(_piece.rank == CAGE) {
            name = "B";
        } else if(_piece.rank == WORM) {
            name = "F";
        } else if(_piece.rank == ASSASSIN) {
            name = "A";
        } else {
            name = "" + _piece.rank;
        }
        if(_piece.rank != UNKNOWN
                && (label == null || !label.getText().equals(name))) {
            labelSize = null;
            label = new Label(name);
            label.setFont(new Font("Helvetica", Font.BOLD, 36));
        }
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
        if(selected) {
            gfx.setColor(Color.YELLOW);
        } else {
            gfx.setColor(Color.BLACK);
        }
        gfx.drawRect(px, py, pwid, phei);
        if(label != null) {
            if(labelSize == null) {
                label.layout(gfx);
                labelSize = label.getSize();
                labelX = _bounds.width / 2 - labelSize.width / 2;
                labelY = _bounds.height / 2 - labelSize.height / 2;
            }
            label.render(gfx, _bounds.x + labelX, _bounds.y + labelY);
        }
    }

    private Dimension labelSize;

    private int labelX, labelY;

    private Label label;

    private boolean selected = false;

    protected PeckingPiece _piece;
}