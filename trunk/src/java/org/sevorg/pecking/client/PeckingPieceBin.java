package org.sevorg.pecking.client;

import java.awt.Dimension;
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
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;
import com.threerings.toybox.util.ToyBoxContext;

public class PeckingPieceBin extends MediaPanel implements SetListener,
        PeckingConstants, PlaceView
{

    private static final double X_OFFSET = PieceSprite.SIZE * .2;

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

    @Override
    public Dimension getPreferredSize()
    {
        // Make this wide enough to show a full card, plus 20% of 7 more cards
        // so we can fit the largest number of one type(Cages) in a row
        return new Dimension(PieceSprite.SIZE + (int)(X_OFFSET * 7),
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
                                                           (int)(xOffset * X_OFFSET),
                                                           yOffset
                                                                   * PieceSprite.SIZE);
                unrevealedSprites.add(sprite);
                addSprite(sprite);
                unrevealedPieces.add(piece);
            } else if(unrevealedPieces.contains(piece)) {
                unrevealedPieces.remove(piece);
                if(unrevealedSprites.size() > 0) {
                    removeSprite(unrevealedSprites.remove(unrevealedSprites.size() - 1));
                }
                if(unrevealedPieces.size() == 0) {
                    _ctrl.setReadyToPlay();
                }
            }
        }

        private List<BinPieceSprite> unrevealedSprites = new ArrayList<BinPieceSprite>();

        private Set<PeckingPiece> unrevealedPieces = new HashSet<PeckingPiece>();
    }

    class RevealedPieceLayout implements PieceLayout
    {

        public void update(PeckingPiece piece)
        {
            if(!PeckingLogic.isOffBoard(piece)) {
                return;
            }
            int row = piece.rank - 2;
            if(piece.rank == MARSHALL) {
                row = 0;
            } else if(piece.rank == WORM) {
                row = 8;
            }
            int column = numOnRow[row]++;
            if(piece.rank == MARSHALL || piece.rank == ASSASSIN) {
                column = 0;
            } else if(piece.rank == GENERAL || piece.rank == WORM) {
                column = 5;// Use half column space
            }
            BinPieceSprite sprite = new BinPieceSprite(piece,
                                                       (int)(column * X_OFFSET),
                                                       row * PieceSprite.SIZE);
            sprite.setRenderOrder(numOnRow[row]);
            addSprite(sprite);
        }

        private int[] numOnRow = new int[10];
    }

    void pieceUpdated(PeckingPiece piece)
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

    public void didLeavePlace(PlaceObject plobj)
    {}

    public void willEnterPlace(PlaceObject plobj)
    {
        int color = ((PeckingObject)plobj).getPlayerIndex(((ToyBoxContext)_ctx).getUsername());
        if(color == owner) {
            layout = new RevealedPieceLayout();
        } else {
            layout = new HiddenPieceLayout();
        }
    }
}
