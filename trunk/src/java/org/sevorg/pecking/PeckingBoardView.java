package org.sevorg.pecking;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.media.VirtualMediaPanel;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Displays the main game interface (the board).
 */
public class PeckingBoardView extends VirtualMediaPanel implements PlaceView
{

    /**
     * Constructs a view which will initialize itself and prepare to display the
     * game board.
     */
    public PeckingBoardView(ToyBoxContext ctx)
    {
        super(ctx.getFrameManager());
        _ctx = ctx;
    }

    // from interface PlaceView
    public void willEnterPlace(PlaceObject plobj)
    {
        _gameobj = (PeckingObject)plobj;
    }

    // from interface PlaceView
    public void didLeavePlace(PlaceObject plobj)
    {}

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(boardSize.width * PieceSprite.SIZE + 1,
                             boardSize.height * PieceSprite.SIZE + 1);
    }

    @Override
    protected void paintBehind(Graphics2D gfx, Rectangle dirtyRect)
    {
        super.paintBehind(gfx, dirtyRect);
        // fill in our background color
        gfx.setColor(Color.WHITE);
        gfx.fillRect(0, 0, getWidth(), getHeight());
        // draw our grid
        gfx.setColor(Color.BLACK);
        for(int yy = 0; yy <= boardSize.height; yy++) {
            int ypos = yy * PieceSprite.SIZE;
            gfx.drawLine(0, ypos, PieceSprite.SIZE * boardSize.width, ypos);
        }
        for(int xx = 0; xx <= boardSize.width; xx++) {
            int xpos = xx * PieceSprite.SIZE;
            gfx.drawLine(xpos, 0, xpos, PieceSprite.SIZE * boardSize.height);
        }
        //Draw lakes in the middle of the board
        gfx.setColor(Color.BLUE);
        gfx.fillRect(2 * PieceSprite.SIZE,
                     4 * PieceSprite.SIZE,
                     2 * PieceSprite.SIZE,
                     2 * PieceSprite.SIZE);
        gfx.fillRect(6 * PieceSprite.SIZE,
                     4 * PieceSprite.SIZE,
                     2 * PieceSprite.SIZE,
                     2 * PieceSprite.SIZE);
    }

    /** Provides access to client services. */
    protected ToyBoxContext _ctx;

    /** A reference to our game object. */
    protected PeckingObject _gameobj;

    private Dimension boardSize = new Dimension(10, 10);
}
