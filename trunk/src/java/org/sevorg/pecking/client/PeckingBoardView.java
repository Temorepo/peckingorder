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
import java.util.Map;
import org.sevorg.pecking.PeckingConstants;
import org.sevorg.pecking.PeckingLogic;
import org.sevorg.pecking.data.PeckingObject;
import org.sevorg.pecking.data.PeckingPiece;
import org.sevorg.pecking.data.PeckingPiecesObject;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.media.MediaPanel;
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
public class PeckingBoardView extends MediaPanel implements PlaceView,
        SetListener, AttributeChangeListener, PeckingConstants,
        PieceSelectedListener
{

    public PeckingBoardView(ToyBoxContext ctx, PeckingController ctrl)
    {
        super(ctx.getFrameManager());
        _ctx = ctx;
        _ctrl = ctrl;
        _ctrl.addPeckingPiecesListener(this);
        _ctrl.addPieceSelectedListener(this);
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
        System.out.println("PHASE CHANGE");
        phase = newPhase;
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

    private int phase;

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
            PeckingLogic logic = _ctrl.createLogic();
            if(_ctrl.getSelectedPiece() != null) {
                Point click = new Point(clickX, clickY);
                for(Point p : logic.getLegalMoves(_ctrl.getSelectedPiece())) {
                    if(click.equals(p)) {
                        _ctrl.moveSelected(p.x, p.y);
                        return;
                    }
                }
            }
            PeckingPiece p = logic.getPieceAt(clickX, clickY);
            if(p != null && p.owner == _ctrl.getColor()) {
                _ctrl.setSelectedPiece(p);
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
                    || PeckingLogic.isOffBoard(clickX, clickY)) {
                return;
            }
            PeckingLogic logic = _ctrl.createLogic();
            PeckingPiece current = logic.getPieceAt(clickX, clickY);
            if(_ctrl.getSelectedPiece() != null) {
                _ctrl.moveSelected(clickX, clickY);
            }
            if(current != null) {
                _ctrl.setSelectedPiece(current);
            }
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
        if(piece.x == OFF_BOARD) {
            if(sprites.containsKey(piece.id)) {
                removeSprite(sprites.get(piece.id));
                sprites.remove(piece.id);
            }
        } else {
            if(!sprites.containsKey(piece.id)) {
                PieceSprite sprite = new PieceSprite(piece, piece.x
                        * PieceSprite.SIZE, piece.y * PieceSprite.SIZE);
                sprites.put(piece.id, sprite);
                addSprite(sprite);
            } else {
                sprites.get(piece.id).update(piece);
            }
        }
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

    public void selectionChanged(PeckingPiece changedPiece, boolean newValue)
    {
        getRegionManager().invalidateRegion(getBounds());
    }

    protected void paintInFront(Graphics2D gfx, Rectangle dirtyRect)
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
        if(_ctrl.getSelectedPiece() != null) {
            gfx.setColor(Color.GREEN);
            Stroke current = gfx.getStroke();
            gfx.setStroke(FAT_STROKE);
            if(phase == SETUP) {
                int startRow, stopRow;
                if(_ctrl.getColor() == BLUE) {
                    startRow = 0;
                    stopRow = 4;
                } else {
                    startRow = 6;
                    stopRow = 10;
                }
                for(int i = 0; i < 10; i++) {
                    for(int j = startRow; j < stopRow; j++) {
                        gfx.drawRect(i * PieceSprite.SIZE,
                                     j * PieceSprite.SIZE,
                                     PieceSprite.SIZE,
                                     PieceSprite.SIZE);
                    }
                }
            } else {
                for(Point poi : _ctrl.createLogic()
                        .getLegalMoves(_ctrl.getSelectedPiece())) {
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

    private Map<Comparable, PieceSprite> sprites = new HashMap<Comparable, PieceSprite>();

    private Dimension boardSize = new Dimension(10, 10);

    private static final BasicStroke FAT_STROKE = new BasicStroke(5);

    private MouseListener phaseMouseListener;
}
