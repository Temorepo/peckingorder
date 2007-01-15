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
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;
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
 * A display for pieces that are off the board. It uses HiddenPieceLayout to
 * show the other player's pieces during the setup phase. Otherwise it uses
 * RevealedPieceLayout to show the known pieces for a particular owner off the
 * board
 * 
 */
public class PeckingPieceBin extends MediaPanel implements SetListener,
        PeckingConstants, PlaceView, AttributeChangeListener
{

    /**
     * Create a bin for the pieces of binOwner, either RED or BLUE
     */
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

    /**
     * @return - the pixel x, y of a piece on row, col
     */
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

    /**
     * A piece layout for when the rank of the pieces are known. It stuffs the
     * pieces with only one instance of their rank two ranks to a row, otherwise
     * it gives each rank a row. This gives a total of 10 rows.
     * 
     * If added as a mouse listener to the bin, it handles moving pieces on and
     * off the board for setup.
     */
    class RevealedPieceLayout extends MouseAdapter implements PieceLayout,
            PieceSelectedListener
    {

        public RevealedPieceLayout(PlaceObject plobj)
        {
            _ctrl.addPieceSelectedListener(this);
        }

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

        /**
         * Finds the index of the last column with a piece in it starting at 0
         */
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
                    // Select the next piece in the bin for rapid fire setup
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
            final PieceSprite sprite = new PieceSprite(piece,
                                                       getLocation(row, column));
            sprite.setRenderOrder(column);
            pieces[row][column] = sprite;
            if(_gameobj.phase == PLAY) {
                // Add the piece to the bin after the animation on the board has
                // finished if we're in the PLAY phase so we don't ruin the
                // surprise
                if(timer == null) {
                    timer = new Timer("BinPieceAdder", true);
                }
                // I like the java.util.Timer's mechanism for scheduling one
                // execution tasks better than Swing's timer, but we have to
                // execute on the EDT. Thus you have this ridiculous anonymous
                // inner class nesting.
                timer.schedule(new TimerTask() {

                    @Override
                    public void run()
                    {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run()
                            {
                                addSprite(sprite);
                            }
                        });
                    }
                }, PeckingBoardView.MAX_MOVE_DELAY);
            } else {
                addSprite(sprite);
                _ctrl.setSelectedPiece(piece);
            }
        }

        private Timer timer;

        int LAST_COLUMN = 7;

        private int MARSHALL_ROW = 0, WORM_ROW = 8;

        private PieceSprite[][] pieces = new PieceSprite[10][8];
    }
}
