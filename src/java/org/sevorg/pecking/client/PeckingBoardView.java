package org.sevorg.pecking.client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sevorg.pecking.PeckingConstants;
import org.sevorg.pecking.PeckingLogic;
import org.sevorg.pecking.data.PeckingObject;
import org.sevorg.pecking.data.PeckingPiece;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.media.VirtualMediaPanel;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Displays the main game interface (the board).
 */
public class PeckingBoardView extends VirtualMediaPanel implements PlaceView,
        SetListener, PeckingConstants
{

    public PeckingBoardView(ToyBoxContext ctx, PeckingController ctrl)
    {
        super(ctx.getFrameManager());
        _ctx = ctx;
        _ctr = ctrl;
        addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e)
            {
                int clickX = e.getX() / PieceSprite.SIZE;
                int clickY = e.getY() / PieceSprite.SIZE;
                if(PeckingLogic.isInLake(clickX, clickY)
                        || PeckingLogic.isOffBoard(clickX, clickY)) {
                    return;
                }
                PeckingLogic logic = new PeckingLogic(_gameobj.pieces);
                if(possibleMoves != null) {
                    Point click = new Point(clickX, clickY);
                    for(Point p : possibleMoves) {
                        if(click.equals(p)) {
                            _ctr.move(selectedPiece, p.x, p.y);
                            getRegionManager().addDirtyRegion(new Rectangle(getPreferredSize()));
                            return;
                        }
                    }
                }
                clearSelectedPiece();
                PeckingPiece p = logic.getPieceAt(clickX, clickY);
                if(p.owner == _ctr._color) {
                    selectedPiece = p;
                    possibleMoves = logic.getLegalMoves(p);
                    getRegionManager().addDirtyRegion(new Rectangle(getPreferredSize()));
                }
            }
        });
    }

    // from interface PlaceView
    public void willEnterPlace(PlaceObject plobj)
    {
        _gameobj = (PeckingObject)plobj;
        _gameobj.addListener(this);
        for(PeckingPiece p: _gameobj.pieces) {
            pieceUpdated(p);
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

    public void entryAdded(EntryAddedEvent event)
    {
        if(event.getName().equals(PeckingObject.PIECES)) {
            pieceUpdated((PeckingPiece)event.getEntry());
        }
    }

    public void entryRemoved(EntryRemovedEvent event)
    {
    // Pieces are never removed
    }

    public void entryUpdated(EntryUpdatedEvent event)
    {
        if(event.getName().equals(PeckingObject.PIECES)) {
            pieceUpdated((PeckingPiece)event.getEntry());
        }
    }

    protected void pieceUpdated(PeckingPiece piece)
    {
        if(selectedPiece != null && piece.id == selectedPiece.id) {
            clearSelectedPiece();
        }
        if(piece.x == OFF_BOARD) {
            if(sprites.containsKey(piece.id)){
                removeSprite(sprites.get(piece.id));
                sprites.remove(piece.id);
            }
        }else{
            if(!sprites.containsKey(piece.id)) {
                PieceSprite sprite = new PieceSprite(piece);
                sprites.put(piece.id, sprite);
                addSprite(sprite);
            } else {
                sprites.get(piece.id).update(piece);
            }
        }
    }

    private void clearSelectedPiece()
    {
        selectedPiece = null;
        possibleMoves = null;
        getRegionManager().addDirtyRegion(new Rectangle(getPreferredSize()));
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
            gfx.setColor(Color.YELLOW);
            Stroke current = gfx.getStroke();
            gfx.setStroke(FAT_STROKE);
            gfx.drawRect(selectedPiece.x * PieceSprite.SIZE, selectedPiece.y
                    * PieceSprite.SIZE, PieceSprite.SIZE, PieceSprite.SIZE);
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

    private PeckingPiece selectedPiece;

    private List<Point> possibleMoves;

    private Map<Comparable, PieceSprite> sprites = new HashMap<Comparable, PieceSprite>();

    private Dimension boardSize = new Dimension(10, 10);

    private static final BasicStroke FAT_STROKE = new BasicStroke(5);
}