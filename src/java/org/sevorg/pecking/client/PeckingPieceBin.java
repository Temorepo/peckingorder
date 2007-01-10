package org.sevorg.pecking.client;

import java.awt.Dimension;
import java.awt.Point;
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
        PeckingConstants, PlaceView
{

    public PeckingPieceBin(ToyBoxContext ctx,
                           PeckingController ctrl,
                           int binOwner)
    {
        super(ctx.getFrameManager());
        _ctx = ctx;
        _ctrl = ctrl;
        _ctrl.addPeckingPiecesListener(this);
        owner = binOwner;
    }

    public void didLeavePlace(PlaceObject plobj)
    {}

    public void willEnterPlace(PlaceObject plobj)
    {
        int color = ((PeckingObject)plobj).getPlayerIndex(((ToyBoxContext)_ctx).getUsername());
        if(color == owner) {
            layout = new RevealedPieceLayout(plobj);
        } else {
            layout = new HiddenPieceLayout();
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

    public void entryAdded(EntryAddedEvent event)
    {
        if(!event.getName().equals(PeckingPiecesObject.PIECES)) {
            return;
        }
        pieceUpdated((PeckingPiece)event.getEntry());
    }

    public void entryRemoved(EntryRemovedEvent event)
    {}

    public void entryUpdated(EntryUpdatedEvent event)
    {
        if(!event.getName().equals(PeckingPiecesObject.PIECES)) {
            return;
        }
        pieceUpdated((PeckingPiece)event.getEntry());
    }

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
                int xOffset = unrevealedSprites.size() % 8;
                int yOffset = unrevealedSprites.size() / 8;
                BinPieceSprite sprite = new BinPieceSprite(piece,
                                                           (int)(xOffset * X_SHIFT),
                                                           yOffset
                                                                   * PieceSprite.SIZE);
                sprite.setRenderOrder(xOffset);
                unrevealedSprites.add(sprite);
                addSprite(sprite);
                unrevealedPieces.add(piece);
            } else if(unrevealedPieces.contains(piece)) {
                unrevealedPieces.remove(piece);
                removeSprite(unrevealedSprites.remove(unrevealedSprites.size() - 1));
                if(unrevealedPieces.size() == 0) {
                    _ctrl.setReadyToPlay();
                }
            }
        }

        private List<BinPieceSprite> unrevealedSprites = new ArrayList<BinPieceSprite>();

        private Set<PeckingPiece> unrevealedPieces = new HashSet<PeckingPiece>();
    }

    class RevealedPieceLayout implements PieceLayout, PieceSelectedListener,
            AttributeChangeListener
    {

        private final class RevealedBinMouseAdapter extends MouseAdapter
        {

            public void mousePressed(MouseEvent e)
            {
                if(_ctrl.getSelectedPiece() != null
                        && !PeckingLogic.isOffBoard(_ctrl.getSelectedPiece())) {
                    _ctrl.move(_ctrl.getSelectedPiece(), OFF_BOARD, OFF_BOARD);
                }
                int clickRow = e.getY() / PieceSprite.SIZE;
                int lastFilled = findLastFilledSlot(clickRow);
                if(lastFilled != -1
                        && e.getX() <= pieces[clickRow][lastFilled].getX()
                                + PieceSprite.SIZE) {
                    _ctrl.setSelectedPiece(pieces[clickRow][lastFilled]._piece);
                } else if(clickRow == MARSHALL_ROW || clickRow == WORM_ROW) {
                    if(pieces[clickRow][LAST_COLUMN] != null
                            && e.getX() > pieces[clickRow][LAST_COLUMN].getX()) {
                        _ctrl.setSelectedPiece(pieces[clickRow][LAST_COLUMN]._piece);
                    }
                }
            }
        }

        public RevealedPieceLayout(PlaceObject plobj)
        {
            _ctrl.addPieceSelectedListener(this);
            plobj.addListener(this);
            addBinMouseListener();
        }

        public void attributeChanged(AttributeChangedEvent event)
        {
            if(event.getName().equals(PeckingObject.PHASE)) {
                if(event.getIntValue() == PeckingConstants.PLAY) {
                    removeMouseListener(listener);
                    listener = null;
                } else if(listener != null) {
                    addBinMouseListener();
                }
            }
        }

        private void addBinMouseListener()
        {
            listener = new RevealedBinMouseAdapter();
            addMouseListener(listener);
        }

        public void selectionChanged(PeckingPiece changedPiece, boolean newValue)
        {
            Point coords = findSprite(changedPiece);
            if(coords != null) {
                PieceSprite sprite = pieces[coords.x][coords.y];
                sprite.selected = newValue;
                sprite.invalidate();
            }
        }

        /**
         * @return - the row and column of a sprite as x and y in the point
         *         respectively
         */
        private Point findSprite(PeckingPiece piece)
        {
            for(int i = 0; i < pieces.length; i++) {
                for(int j = 0; j < pieces[i].length; j++) {
                    if(pieces[i][j] != null
                            && pieces[i][j]._piece.equals(piece)) {
                        return new Point(i, j);
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
                    // Piece moved out of bin, remove sprite from panel and
                    // shift everything left in the array
                    PieceSprite[] row = pieces[existingSpriteCoords.x];
                    removeSprite(row[existingSpriteCoords.y]);
                    row[existingSpriteCoords.y] = null;
                    for(int i = existingSpriteCoords.y + 1; i < row.length
                            && row[i] != null; i++) {
                        row[i - 1] = row[i];
                        row[i - 1].setLocation((int)(i * X_SHIFT),
                                               existingSpriteCoords.x
                                                       * PieceSprite.SIZE);
                    }
                }
                return;
            }
            if(!PeckingLogic.isOffBoard(piece)){
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
            BinPieceSprite sprite = new BinPieceSprite(piece,
                                                       (int)(column * X_SHIFT),
                                                       row * PieceSprite.SIZE);
            sprite.setRenderOrder(column);
            pieces[row][column] = sprite;
            addSprite(sprite);
        }

        int LAST_COLUMN = 7;

        private int MARSHALL_ROW = 0, WORM_ROW = 8;

        private BinPieceSprite[][] pieces = new BinPieceSprite[10][8];

        private MouseListener listener;
    }

    private void pieceUpdated(PeckingPiece piece)
    {
        if(piece.owner != owner) {
            return;
        }
        layout.update(piece);
    }

    private PieceLayout layout;

    private ToyBoxContext _ctx;

    private PeckingController _ctrl;

    private int owner;

    private static final double X_SHIFT = PieceSprite.SIZE * .2;
}
