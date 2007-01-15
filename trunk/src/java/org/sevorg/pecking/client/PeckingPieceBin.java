package org.sevorg.pecking.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public class PeckingPieceBin extends MediaPanel implements SetListener,
        PeckingConstants, PlaceView, AttributeChangeListener
{

    public PeckingPieceBin(ToyBoxContext ctx,
                           PeckingController ctrl,
                           int binOwner)
    {
        super(ctx.getFrameManager());
        _ctx = ctx;
        _ctrl = ctrl;
        _ctrl.addPeckingPiecesListener(this);
        binColor = binOwner;
    }

    public void didLeavePlace(PlaceObject plobj)
    {}

    public void willEnterPlace(PlaceObject plobj)
    {
        _gameobj = (PeckingObject)plobj;
        _gameobj.addListener(this);
        playerColor = ((PeckingObject)_gameobj).getPlayerIndex(((ToyBoxContext)_ctx).getUsername());
        setLayout();
    }

    private void setLayout()
    {
        if(layout != null && layout instanceof MouseListener) {
            removeMouseListener((MouseListener)layout);
        }
        if(_gameobj.phase == SETUP) {
            if(playerColor != binColor) {
                layout = new HiddenPieceLayout();
            } else {
                layout = new RevealedPieceLayout(_gameobj);
                addMouseListener((MouseListener)layout);
            }
        } else {
            layout = new RevealedPieceLayout(_gameobj);
        }
    }

    @Override
    public Dimension getPreferredSize()
    {
        // Make this wide enough to show a full card, plus 20% of 7 more cards
        // so we can fit the largest number of one type(Cages) in a row
        return new Dimension(PieceSprite.SIZE + (int)(X_SHIFT * 7),
                             10 * PieceSprite.SIZE + 1);
    }

    @Override
    protected void paintBehind(Graphics2D gfx, Rectangle dirtyRect)
    {
        super.paintBehind(gfx, dirtyRect);
        // fill in our background color
        gfx.setColor(Color.WHITE);
        gfx.fill(dirtyRect);
    }

    public void attributeChanged(AttributeChangedEvent event)
    {
        if(event.getName().equals(PeckingObject.PHASE)) {
            setLayout();
        }
    }

    public void entryAdded(EntryAddedEvent event)
    {
        if(!event.getName().equals(PeckingPiecesObject.PIECES)) {
            return;
        }
        updatePiece((PeckingPiece)event.getEntry());
    }

    public void entryRemoved(EntryRemovedEvent event)
    {}

    public void entryUpdated(EntryUpdatedEvent event)
    {
        if(!event.getName().equals(PeckingPiecesObject.PIECES)) {
            return;
        }
        updatePiece((PeckingPiece)event.getEntry());
    }

    private Point getLocation(int row, int col)
    {
        return new Point((int)(col * X_SHIFT), row * PieceSprite.SIZE);
    }

    private void updatePiece(PeckingPiece piece)
    {
        if(piece.owner != binColor) {
            return;
        }
        layout.update(piece);
    }

    private PeckingObject _gameobj;

    private PieceLayout layout;

    private ToyBoxContext _ctx;

    private PeckingController _ctrl;

    private int binColor, playerColor;

    /**
     * The amount to shift each piece in a row by in pixels
     */
    private static final double X_SHIFT = PieceSprite.SIZE * .2;

    interface PieceLayout
    {

        public void update(PeckingPiece piece);
    }

    class HiddenPieceLayout implements PieceLayout
    {

        public void update(PeckingPiece piece)
        {
            if(PeckingLogic.isOffBoard(piece)
                    && !unrevealedPieces.contains(piece)) {
                int col = unrevealedSprites.size() % 8;
                int row = unrevealedSprites.size() / 8;
                PieceSprite sprite = new PieceSprite(piece, getLocation(row,
                                                                        col));
                sprite.setRenderOrder(col);
                unrevealedSprites.add(sprite);
                addSprite(sprite);
                unrevealedPieces.add(piece);
            } else if(unrevealedPieces.contains(piece)) {
                unrevealedPieces.remove(piece);
                removeSprite(unrevealedSprites.remove(unrevealedSprites.size() - 1));
            }
        }

        private List<PieceSprite> unrevealedSprites = new ArrayList<PieceSprite>();

        private Set<PeckingPiece> unrevealedPieces = new HashSet<PeckingPiece>();
    }

    class RevealedPieceLayout extends MouseAdapter implements PieceLayout,
            PieceSelectedListener
    {

        public void mousePressed(MouseEvent e)
        {
            if(_ctrl.getSelectedPiece() != null
                    && !PeckingLogic.isOffBoard(_ctrl.getSelectedPiece())) {
                _ctrl.moveSelected(OFF_BOARD, OFF_BOARD);
            }
            int clickRow = e.getY() / PieceSprite.SIZE;
            int lastFilled = findLastFilledSlot(clickRow);
            if(lastFilled != -1
                    && e.getX() <= pieces[clickRow][lastFilled].getX()
                            + PieceSprite.SIZE) {
                _ctrl.setSelectedPiece(pieces[clickRow][lastFilled]._piece);
            } else if(clickRow == MARSHALL_ROW || clickRow == WORM_ROW) {
                if(pieces[clickRow][LAST_COLUMN] != null
                        && e.getX() >= pieces[clickRow][LAST_COLUMN].getX()) {
                    _ctrl.setSelectedPiece(pieces[clickRow][LAST_COLUMN]._piece);
                }
            }
        }

        public RevealedPieceLayout(PlaceObject plobj)
        {
            _ctrl.addPieceSelectedListener(this);
        }

        public void selectionChanged(PeckingPiece changedPiece, boolean newValue)
        {
            Point coords = findSprite(changedPiece);
            if(coords != null) {
                PieceSprite sprite = pieces[coords.y][coords.x];
                sprite.setSelected(newValue);
            }
        }

        /**
         * @return - the column and row of a sprite as x and y in the point
         *         respectively
         */
        private Point findSprite(PeckingPiece piece)
        {
            for(int i = 0; i < pieces.length; i++) {
                for(int j = 0; j < pieces[i].length; j++) {
                    if(pieces[i][j] != null
                            && pieces[i][j]._piece.equals(piece)) {
                        return new Point(j, i);
                    }
                }
            }
            return null;
        }

        private int findLastFilledSlot(int row)
        {
            int i;
            for(i = 0; i < pieces[row].length; i++) {
                if(pieces[row][i] == null) {
                    return i - 1;
                }
            }
            return i - 1;
        }

        public void update(PeckingPiece piece)
        {
            Point existingSpriteCoords = findSprite(piece);
            if(existingSpriteCoords != null) {
                if(PeckingLogic.isOffBoard(piece)) {
                    // A piece already in the bin was modified in a way that
                    // didn't move it
                } else {
                    // Piece moved out of bin, remove sprite from panel
                    int row = existingSpriteCoords.y, col = existingSpriteCoords.x;
                    removeSprite(pieces[row][col]);
                    pieces[row][col] = null;
                    for(int i = row; i < pieces.length; i++) {
                        for(int j = pieces[i].length - 1; j >= 0; j--) {
                            if(pieces[i][j] != null) {
                                _ctrl.setSelectedPiece(pieces[i][j]._piece);
                                return;
                            }
                        }
                    }
                    for(int i = 0; i < row; i++) {
                        for(int j = pieces[i].length - 1; j >= 0; j--) {
                            if(pieces[i][j] != null) {
                                _ctrl.setSelectedPiece(pieces[i][j]._piece);
                                return;
                            }
                        }
                    }
                }
                return;
            }
            if(!PeckingLogic.isOffBoard(piece)) {
                return;
            }
            int row = piece.rank - 2;
            if(piece.rank == MARSHALL) {
                row = MARSHALL_ROW;
            } else if(piece.rank == WORM) {
                row = WORM_ROW;
            }
            int column = findLastFilledSlot(row) + 1;
            if(piece.rank == MARSHALL || piece.rank == ASSASSIN) {
                column = 0;
            } else if(piece.rank == GENERAL || piece.rank == WORM) {
                column = LAST_COLUMN;// Move it all the way to the right
            }
            PieceSprite sprite = new PieceSprite(piece,
                                                 getLocation(row, column));
            sprite.setRenderOrder(column);
            pieces[row][column] = sprite;
            addSprite(sprite);
            if(_gameobj.phase == SETUP) {
                _ctrl.setSelectedPiece(piece);
            }
        }

        int LAST_COLUMN = 7;

        private int MARSHALL_ROW = 0, WORM_ROW = 8;

        private PieceSprite[][] pieces = new PieceSprite[10][8];
    }
}
