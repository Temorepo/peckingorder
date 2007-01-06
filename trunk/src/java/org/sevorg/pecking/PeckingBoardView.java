package org.sevorg.pecking;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import org.sevorg.pecking.PeckingObject.Piece;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.media.VirtualMediaPanel;
import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.ElementUpdateListener;
import com.threerings.presents.dobj.ElementUpdatedEvent;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Displays the main game interface (the board).
 */
public class PeckingBoardView extends VirtualMediaPanel implements PlaceView,
        ElementUpdateListener, AttributeChangeListener
{

    public PeckingBoardView(ToyBoxContext ctx, PeckingController ctrl)
    {
        super(ctx.getFrameManager());
        _ctx = ctx;
        _ctr = ctrl;
        addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e)
            {
                int col = e.getX() / PieceSprite.SIZE;
                int row = e.getY() / PieceSprite.SIZE;
                Point click = new Point(col, row);
                PeckingLogic logic = new PeckingLogic(_gameobj.pieces);
                if(possibleMoves != null) {
                    for(Point p : possibleMoves) {
                        if(click.equals(p)) {
                            _ctr.move(logic.getPieceAt(p), p);
                            return;
                        }
                    }
                }
                clearSelectedPiece();
                int pieceIdx = logic.getPieceIdxAt(click);
                if(sprites[pieceIdx] != null) {
                    selectedPiece = sprites[pieceIdx];
                    possibleMoves = logic.getLegalMoves(sprites[pieceIdx]._piece);
                }
                repaint();
            }
        });
    }

    // from interface PlaceView
    public void willEnterPlace(PlaceObject plobj)
    {
        _gameobj = (PeckingObject)plobj;
        _gameobj.addListener(this);
        updateAll();
    }

    private void updateAll()
    {
        for(int i = 0; i < _gameobj.pieces.length; i++) {
            if(_gameobj.pieces[i] != null) {
                pieceUpdated(i, _gameobj.pieces[i]);
            }
        }
    }

    // from interface PlaceView
    public void didLeavePlace(PlaceObject plobj)
    {
        _gameobj.removeListener(this);
        _gameobj = null;
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(boardSize.width * PieceSprite.SIZE + 1,
                             boardSize.height * PieceSprite.SIZE + 1);
    }

    public void attributeChanged(AttributeChangedEvent event)
    {
        if(event.getName().equals(PeckingObject.PIECES)) {
            updateAll();
        }
    }

    public void elementUpdated(ElementUpdatedEvent event)
    {
        if(event.getName().equals(PeckingObject.PIECES)) {
            pieceUpdated(event.getIndex(),
                         (PeckingObject.Piece)event.getValue());
        }
    }

    protected void pieceUpdated(int index, PeckingObject.Piece piece)
    {
        if(piece.x == PeckingConstants.OFF_BOARD) {
            if(sprites[index] != null) {
                if(sprites[index] == selectedPiece) {
                    clearSelectedPiece();
                }
                removeSprite(sprites[index]);
                sprites[index] = null;
            }
        } else {
            if(sprites[index] == null) {
                sprites[index] = new PieceSprite(piece);
                addSprite(sprites[index]);
            } else {
                sprites[index].update();
            }
        }
    }

    private void clearSelectedPiece()
    {
        selectedPiece = null;
        possibleMoves = null;
    }

    @Override
    protected void paintBehind(Graphics2D gfx, Rectangle dirtyRect)
    {
        super.paintBehind(gfx, dirtyRect);
        // fill in our background color
        gfx.setColor(Color.WHITE);
        gfx.fill(dirtyRect);
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
    }

    protected void paintBetween(Graphics2D gfx, Rectangle dirtyRect)
    {
        // Draw lakes in the middle of the board
        gfx.setColor(Color.DARK_GRAY);
        gfx.fillRect(2 * PieceSprite.SIZE,
                     4 * PieceSprite.SIZE,
                     2 * PieceSprite.SIZE,
                     2 * PieceSprite.SIZE);
        gfx.fillRect(6 * PieceSprite.SIZE,
                     4 * PieceSprite.SIZE,
                     2 * PieceSprite.SIZE,
                     2 * PieceSprite.SIZE);
        if(selectedPiece != null) {
            Piece p = selectedPiece._piece;
            gfx.setColor(Color.YELLOW);
            Stroke current = gfx.getStroke();
            gfx.setStroke(FAT_STROKE);
            gfx.drawRect(p.x * PieceSprite.SIZE,
                         p.y * PieceSprite.SIZE,
                         PieceSprite.SIZE,
                         PieceSprite.SIZE);
            gfx.setColor(Color.GREEN);
            if(possibleMoves != null) {
                for(Point poi : possibleMoves) {
                    gfx.drawRect(poi.x * PieceSprite.SIZE,
                                 poi.y * PieceSprite.SIZE,
                                 PieceSprite.SIZE,
                                 PieceSprite.SIZE);
                }
            }
            gfx.setStroke(current);
        }
    }

    /** Provides access to client services. */
    protected ToyBoxContext _ctx;

    /** A reference to our game object. */
    protected PeckingObject _gameobj;

    private PeckingController _ctr;

    private PieceSprite selectedPiece;

    private List<Point> possibleMoves;

    private PieceSprite[] sprites = new PieceSprite[PeckingConstants.NUM_PIECES];

    private Dimension boardSize = new Dimension(10, 10);

    private static final BasicStroke FAT_STROKE = new BasicStroke(5);
}
