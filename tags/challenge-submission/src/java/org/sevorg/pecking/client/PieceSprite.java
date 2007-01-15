package org.sevorg.pecking.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
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

    public PieceSprite(PeckingPiece piece, Point location)
    {
        this();
        update(piece, location);
    }

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
    public void update(PeckingPiece piece, Point location)
    {
        setLocation(location.x, location.y);
        update(piece);
    }

    public void update(PeckingPiece piece)
    {
        _piece = piece;
        if(_piece.rank != UNKNOWN && label == null) {
            label = createLabel(piece);
        }
        invalidate();
    }

    /**
     * @return - a Label that always draws centered over this piece
     */
    public Label createLabel(PeckingPiece piece)
    {
        String name;
        if(piece.rank == CAGE) {
            name = "B";
        } else if(piece.rank == WORM) {
            name = "F";
        } else if(piece.rank == ASSASSIN) {
            name = "A";
        } else {
            name = "" + piece.rank;
        }
        Label newLabel = new Label(name) {

            @Override
            public void layout(Graphics2D gfx)
            {
                super.layout(gfx);
                Dimension labelSize = super.getSize();
                labelX = _bounds.width / 2 - labelSize.width / 2;
                labelY = _bounds.height / 2 - labelSize.height / 2;
            }

            public Dimension getSize()
            {
                return new Dimension(SIZE, SIZE);
            }

            @Override
            public void render(Graphics2D gfx, float x, float y)
            {
                super.render(gfx, _bounds.x + labelX, _bounds.y + labelY);
            }

            private int labelX, labelY;
        };
        newLabel.setFont(new Font("Helvetica", Font.BOLD, 30));
        return newLabel;
    }

    @Override
    public void paint(Graphics2D gfx)
    {
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
            if(!label.isLaidOut()) {
                label.layout(gfx);
            }
            label.render(gfx, 0, 0);
        }
    }

    private Label label;

    private boolean selected = false;

    protected PeckingPiece _piece;

    /** The dimensions of our sprite in pixels. */
    public static final int SIZE = 40;
}