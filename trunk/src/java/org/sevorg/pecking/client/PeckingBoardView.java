package org.sevorg.pecking.client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import org.sevorg.pecking.PeckingPlayLogic;
import org.sevorg.pecking.data.PeckingObject;
import org.sevorg.pecking.data.PeckingPiece;
import org.sevorg.pecking.data.PeckingPiecesObject;
import com.samskivert.swing.Label;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.media.MediaPanel;
import com.threerings.media.animation.Animation;
import com.threerings.media.animation.AnimationAdapter;
import com.threerings.media.animation.AnimationWaiter;
import com.threerings.media.animation.FadeLabelAnimation;
import com.threerings.media.animation.FloatingTextAnimation;
import com.threerings.media.sprite.PathAdapter;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.ArcPath;
import com.threerings.media.util.LinePath;
import com.threerings.media.util.Path;
import com.threerings.parlor.media.ScoreAnimation;
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
        updatePhase(_gameobj.phase);
    }

    // from interface PlaceView
    public void didLeavePlace(PlaceObject plobj)
    {
        _gameobj.removeListener(this);
        _gameobj = null;
    }

    public void attributeChanged(AttributeChangedEvent event)
    {
        if(event.getName().equals(PeckingObject.PHASE)) {
            updatePhase(event.getIntValue());
        } else if(event.getName().equals(PeckingObject.WINNERS)) {
            setPhaseStrat(new PostGameStrategy());
            if(_gameobj.winners[BLUE] == _gameobj.winners[RED]) {
                displayFloatingText("b.draw");
            } else if(_gameobj.winners[_ctrl.getColor()]) {
                displayFloatingText("b.win");
            } else {
                displayFloatingText("b.lose");
            }
        }
    }

    protected void updatePhase(int newPhase)
    {
        if(newPhase == PLAY) {
            setPhaseStrat(new PlayPhaseStrategy());
            displayFloatingText("b.begin_play");
        } else {
            setPhaseStrat(new SetupPhaseStrategy());
        }
        // Repaint entire board to clean off old selection bounds
        setWholeBoardDirty();
    }

    private void setPhaseStrat(PhaseStrategy strat)
    {
        removeMouseListener(phaseStrat);
        phaseStrat = strat;
        addMouseListener(phaseStrat);
    }

    private void setWholeBoardDirty()
    {
        getRegionManager().addDirtyRegion(new Rectangle(getPreferredSize()));
    }

    public void entryAdded(EntryAddedEvent event)
    {
        if(event.getName().equals(PeckingPiecesObject.PIECES)) {
            updatePiece((PeckingPiece)event.getEntry());
        }
    }

    public void entryRemoved(EntryRemovedEvent event)
    {
        if(event.getName().equals(PeckingPiecesObject.PIECES)) {
            removeSprite((PeckingPiece)event.getOldEntry());
        }
    }

    public void entryUpdated(EntryUpdatedEvent event)
    {
        if(event.getName().equals(PeckingPiecesObject.PIECES)) {
            updatePiece((PeckingPiece)event.getEntry());
        }
    }

    protected void updatePiece(PeckingPiece piece)
    {
        phaseStrat.update(piece);
    }

    /**
     * If this board contains a sprite for piece, remove it
     */
    private void removeSprite(PeckingPiece piece)
    {
        if(sprites.containsKey(piece)) {
            removeSprite(sprites.get(piece));
            sprites.remove(piece);
        }
    }

    public void selectionChanged(PeckingPiece changedPiece, boolean newValue)
    {
        setWholeBoardDirty();
        if(sprites.containsKey(changedPiece)) {
            sprites.get(changedPiece).setSelected(newValue);
        }
    }

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

    @Override
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
        if(_ctrl.getSelectedPiece() != null) {
            gfx.setColor(Color.GREEN);
            Stroke current = gfx.getStroke();
            gfx.setStroke(FAT_STROKE);
            if(_gameobj.phase == SETUP) {
                int startRow, stopRow;
                if(_ctrl.getColor() == BLUE) {
                    startRow = BLUE_MIN;
                    stopRow = BLUE_MAX;
                } else {
                    startRow = RED_MIN;
                    stopRow = RED_MAX;
                }
                for(int i = 0; i < 10; i++) {
                    for(int j = startRow; j <= stopRow; j++) {
                        gfx.drawRect(i * PieceSprite.SIZE,
                                     j * PieceSprite.SIZE,
                                     PieceSprite.SIZE,
                                     PieceSprite.SIZE);
                    }
                }
            } else {
                List<Point> legalMoves = _ctrl.createLogic()
                        .getLegalMoves(_ctrl.getSelectedPiece());
                for(Point poi : legalMoves) {
                    gfx.drawRect(poi.x * PieceSprite.SIZE,
                                 poi.y * PieceSprite.SIZE,
                                 PieceSprite.SIZE,
                                 PieceSprite.SIZE);
                }
            }
            gfx.setStroke(current);
        }
    }

    /**
     * Floats the supplied text over the board.
     */
    public void displayFloatingText(String message)
    {
        Label label = ScoreAnimation.createLabel(_ctx.xlate("pecking", message),
                                                 Color.white,
                                                 new Font("Helvetica",
                                                          Font.BOLD,
                                                          48),
                                                 this);
        int lx = (getWidth() - label.getSize().width) / 2;
        int ly = (getHeight() - label.getSize().height) / 2;
        addAnimation(new FloatingTextAnimation(label, lx, ly));
    }

    private ToyBoxContext _ctx;

    private PeckingObject _gameobj;

    private PeckingController _ctrl;

    private Map<PeckingPiece, PieceSprite> sprites = new HashMap<PeckingPiece, PieceSprite>();

    private Dimension boardSize = new Dimension(10, 10);

    private static final BasicStroke FAT_STROKE = new BasicStroke(5);

    private PhaseStrategy phaseStrat = new SetupPhaseStrategy();

    private abstract class PhaseStrategy extends MouseAdapter
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
            handle(logic, logic.getPieceAt(clickX, clickY), clickX, clickY);
        }

        protected Animation fadeInLabel(final PieceSprite sprite,
                                        final PeckingPiece newPiece)
        {
            Animation fader = new FadeLabelAnimation(sprite.createLabel(newPiece),
                                                     sprite.getX(),
                                                     sprite.getY(),
                                                     0,
                                                     .001f,
                                                     1);
            fader.addAnimationObserver(new AnimationAdapter() {

                @Override
                public void animationCompleted(Animation anim, long when)
                {
                    sprite.update(newPiece);
                }
            });
            addAnimation(fader);
            return fader;
        }

        public abstract void update(PeckingPiece piece);

        public abstract void handle(PeckingLogic logic,
                                    PeckingPiece clicked,
                                    int clickRow,
                                    int clickY);
    }

    public class PlayPhaseStrategy extends PhaseStrategy
    {

        @Override
        public void handle(PeckingLogic logic,
                           PeckingPiece clicked,
                           int clickX,
                           int clickY)
        {
            if(_ctrl.getSelectedPiece() != null) {
                Point click = new Point(clickX, clickY);
                for(Point p : logic.getLegalMoves(_ctrl.getSelectedPiece())) {
                    if(click.equals(p)) {
                        _ctrl.moveSelected(p.x, p.y);
                        return;
                    }
                }
            }
            if(clicked != null && clicked.owner == _ctrl.getColor()
                    && !PeckingPlayLogic.isImmobile(clicked)) {
                _ctrl.setSelectedPiece(clicked);
            }
        }

        private Path createSpriteMove(PieceSprite pieceSprite,
                                      final PeckingPiece piece)
        {
            if(PeckingLogic.isOffBoard(piece)) {
                pieceSprite.addSpriteObserver(new PathAdapter() {

                    public void pathCompleted(Sprite sprite,
                                              Path path,
                                              long when)
                    {
                        removeSprite(piece);
                    }
                });
                // CHUNK IT
                return new ArcPath(new Point(pieceSprite.getX(),
                                             pieceSprite.getY()),
                                   500,
                                   500,
                                   50,
                                   1,
                                   500,
                                   ArcPath.NONE);
            }
            return createSimpleMove(piece);
        }

        @Override
        public void update(final PeckingPiece piece)
        {
            if(!sprites.containsKey(piece)) {
                int x = piece.x * PieceSprite.SIZE, y = piece.y
                        * PieceSprite.SIZE;
                PieceSprite sprite = new PieceSprite(piece, x, y);
                sprites.put(piece, sprite);
                addSprite(sprite);
                return;
            }
            final PieceSprite pieceSprite = sprites.get(piece);
            if(halfMove != null) {
                final PieceSprite halfMoveSprite = sprites.get(halfMove);
                final Path halfMovePath = createSpriteMove(halfMoveSprite,
                                                           halfMove);
                final Path piecePath = createSpriteMove(pieceSprite, piece);
                AnimationWaiter waiter = new AnimationWaiter() {

                    @Override
                    public void allAnimationsFinished()
                    {
                        halfMoveSprite.move(halfMovePath);
                        pieceSprite.move(piecePath);
                    }
                };
                if(pieceSprite._piece.rank == UNKNOWN) {
                    waiter.addAnimation(fadeInLabel(pieceSprite, piece));
                } else if(halfMoveSprite._piece.rank == UNKNOWN) {
                    waiter.addAnimation(fadeInLabel(halfMoveSprite, halfMove));
                } else {
                    halfMoveSprite.move(halfMovePath);
                    pieceSprite.move(piecePath);
                }
                halfMove = null;
            } else {
                // Since the DSet has the updated piece position in it, we need
                // to check if there's more than one piece at this position to
                // see if it was previously unoccupied
                if(PeckingLogic.isOffBoard(piece)) {
                    halfMove = piece;
                } else {
                    pieceSprite.move(createSimpleMove(piece));
                }
            }
        }

        private Path createSimpleMove(PeckingPiece piece)
        {
            int x = piece.x * PieceSprite.SIZE, y = piece.y * PieceSprite.SIZE;
            return new LinePath(new Point(x, y), 500);
        }

        private PeckingPiece halfMove;
    }

    public class SetupPhaseStrategy extends PhaseStrategy
    {

        @Override
        public void handle(PeckingLogic logic,
                           PeckingPiece clicked,
                           int clickX,
                           int clickY)
        {
            if(_ctrl.getSelectedPiece() != null) {
                _ctrl.moveSelected(clickX, clickY);
            }
            if(clicked != null) {
                _ctrl.setSelectedPiece(clicked);
            }
        }

        @Override
        public void update(PeckingPiece piece)
        {
            if(piece.x == OFF_BOARD) {
                removeSprite(piece);
            } else {
                int x = piece.x * PieceSprite.SIZE, y = piece.y
                        * PieceSprite.SIZE;
                if(!sprites.containsKey(piece)) {
                    PieceSprite sprite = new PieceSprite(piece, x, y);
                    sprites.put(piece, sprite);
                    addSprite(sprite);
                } else {
                    sprites.get(piece).update(piece, x, y);
                }
                if(piece.equals(_ctrl.getSelectedPiece())) {
                    sprites.get(piece).setSelected(true);
                }
            }
        }
    }

    private class PostGameStrategy extends PhaseStrategy
    {

        @Override
        public void handle(PeckingLogic logic,
                           PeckingPiece clicked,
                           int clickRow,
                           int clickY)
        {
        // Ignore clicks after the game
        }

        @Override
        public void update(PeckingPiece piece)
        {
            if(sprites.containsKey(piece)) {
                fadeInLabel(sprites.get(piece), piece);
            }
        }
    }
}
