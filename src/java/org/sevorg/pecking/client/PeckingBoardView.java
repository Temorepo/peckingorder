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
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sevorg.pecking.PeckingConstants;
import org.sevorg.pecking.PeckingLogic;
import org.sevorg.pecking.data.PeckingObject;
import org.sevorg.pecking.data.PeckingPiece;
import org.sevorg.pecking.data.PeckingPiecesObject;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.media.VirtualMediaPanel;
import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Displays the main game interface (the board).
 */
public class PeckingBoardView extends VirtualMediaPanel implements PlaceView,
        SetListener, AttributeChangeListener, PeckingConstants
{

    public PeckingBoardView(ToyBoxContext ctx, PeckingController ctrl)
    {
        super(ctx.getFrameManager());
        _ctx = ctx;
        _ctrl = ctrl;
        _ctrl.addPeckingPiecesListener(this);
    }

    // from interface PlaceView
    public void willEnterPlace(PlaceObject plobj)
    {
        _gameobj = (PeckingObject)plobj;
        _gameobj.addListener(this);
        setPhase(_gameobj.phase);
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
        if(event.getName().equals(PeckingObject.PHASE)) {
            setPhase(event.getIntValue());
        }
    }

    private void setPhase(int newPhase)
    {
        if(phaseMouseListener != null) {
            removeMouseListener(phaseMouseListener);
        }
        if(newPhase == PLAY) {
            phaseMouseListener = new PlayMouseAdapter();
        } else {
            phaseMouseListener = new SetupMouseAdapter();
        }
        addMouseListener(phaseMouseListener);
    }

    private class PlayMouseAdapter extends MouseAdapter
    {

        public void mousePressed(MouseEvent e)
        {
            int clickX = e.getX() / PieceSprite.SIZE;
            int clickY = e.getY() / PieceSprite.SIZE;
            if(PeckingLogic.isInLake(clickX, clickY)
                    || PeckingLogic.isOffBoard(clickX, clickY)) {
                return;
            }
            if(possibleMoves != null) {
                Point click = new Point(clickX, clickY);
                for(Point p : possibleMoves) {
                    if(click.equals(p)) {
                        _ctrl.move(selectedPiece, p.x, p.y);
                        getRegionManager().addDirtyRegion(new Rectangle(getPreferredSize()));
                        return;
                    }
                }
            }
            clearSelectedPiece();
            PeckingLogic logic = _ctrl.createLogic();
            PeckingPiece p = logic.getPieceAt(clickX, clickY);
            if(p != null && p.owner == _ctrl.getColor()) {
                selectedPiece = p;
                possibleMoves = logic.getLegalMoves(p);
                getRegionManager().addDirtyRegion(new Rectangle(getPreferredSize()));
            }
        }
    }

    public class SetupMouseAdapter extends MouseAdapter implements
            MouseListener
    {

        public void mousePressed(MouseEvent e)
        {
            int clickX = e.getX() / PieceSprite.SIZE;
            int clickY = e.getY() / PieceSprite.SIZE;
            if(PeckingLogic.isInLake(clickX, clickY)
                    || PeckingLogic.isOffBoard(clickX, clickY)
                    || (_ctrl.getColor() == RED && clickY > 3)
                    || (_ctrl.getColor() == BLUE && clickY < 6)) {
                return;
            }
            PeckingLogic logic = _ctrl.createLogic();
            PeckingPiece current = logic.getPieceAt(clickX, clickY);
            if(_ctrl.getSelectedPiece() != null) {
                _ctrl.move(_ctrl.getSelectedPiece(), clickX, clickY);
            }
            if(current != null) {
                _ctrl.setSelectedPiece(current);
            }
            getRegionManager().addDirtyRegion(new Rectangle(getPreferredSize()));
        }
    }

    public void entryAdded(EntryAddedEvent event)
    {
        if(event.getName().equals(PeckingPiecesObject.PIECES)) {
            pieceUpdated((PeckingPiece)event.getEntry());
        }
    }

    public void entryRemoved(EntryRemovedEvent event)
    {
    // Pieces are never removed
    }

    public void entryUpdated(EntryUpdatedEvent event)
    {
        if(event.getName().equals(PeckingPiecesObject.PIECES)) {
            pieceUpdated((PeckingPiece)event.getEntry());
        }
    }

    protected void pieceUpdated(PeckingPiece piece)
    {
        if(selectedPiece != null && piece.id == selectedPiece.id) {
            clearSelectedPiece();
        }
        if(piece.x == OFF_BOARD) {
            if(sprites.containsKey(piece.id)) {
                removeSprite(sprites.get(piece.id));
                sprites.remove(piece.id);
            }
        } else {
            if(!sprites.containsKey(piece.id)) {
                BoardPieceSprite sprite = new BoardPieceSprite(piece);
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

    protected PeckingPiecesObject _pobj;

    private PeckingController _ctrl;

    private PeckingPiece selectedPiece;

    private List<Point> possibleMoves;

    private Map<Comparable, BoardPieceSprite> sprites = new HashMap<Comparable, BoardPieceSprite>();

    private Dimension boardSize = new Dimension(10, 10);

    private static final BasicStroke FAT_STROKE = new BasicStroke(5);

    private MouseListener phaseMouseListener;
}
