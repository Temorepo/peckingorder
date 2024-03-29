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
import com.threerings.media.animation.FadeLabelAnimation;
import com.threerings.media.animation.FloatingTextAnimation;
import com.threerings.media.sprite.PathAdapter;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.ArcPath;
import com.threerings.media.util.DelayPath;
import com.threerings.media.util.LinePath;
import com.threerings.media.util.Path;
import com.threerings.media.util.PathSequence;
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
        removeMouseListener(_strat);
        _strat = strat;
        addMouseListener(_strat);
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
        _strat.update(piece);
    }

    /**
     * If this board contains a sprite for piece, remove it from both the board
     * and our piece to sprite map
     */
    private boolean removeSprite(PeckingPiece piece)
    {
        if(sprites.containsKey(piece)) {
            removeSprite(sprites.get(piece));
            sprites.remove(piece);
            return true;
        }
        return false;
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
        if(_gameobj != null && _gameobj.phase == SETUP
                && !displayedSetupMessage) {
            displayFloatingText("b.place_pieces");
            displayedSetupMessage = true;
        }
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
            for(Point poi : _ctrl.createLogic()
                    .getLegalMoves(_ctrl.getSelectedPiece())) {
                gfx.drawRect(poi.x * PieceSprite.SIZE,
                             poi.y * PieceSprite.SIZE,
                             PieceSprite.SIZE,
                             PieceSprite.SIZE);
            }
            gfx.setStroke(current);
        }
    }

    /**
     * Floats the supplied text over the board for 1500 millis.
     */
    public void displayFloatingText(String message)
    {
        displayFloatingText(message, 1500);
    }

    /**
     * Floats the supplied text over the board for duration millis.
     */
    public void displayFloatingText(String message, long duration)
    {
        Label label = ScoreAnimation.createLabel(_ctx.xlate("pecking", message),
                                                 Color.white,
                                                 new Font("Helvetica",
                                                          Font.BOLD,
                                                          48),
                                                 this);
        int lx = (getWidth() - label.getSize().width) / 2;
        int ly = (getHeight() - label.getSize().height) / 2;
        addAnimation(new FloatingTextAnimation(label, lx, ly, duration));
    }

    private ToyBoxContext _ctx;

    private PeckingObject _gameobj;

    private PeckingController _ctrl;

    private PhaseStrategy _strat = new SetupPhaseStrategy();

    private Map<PeckingPiece, PieceSprite> sprites = new HashMap<PeckingPiece, PieceSprite>();

    private Dimension boardSize = new Dimension(10, 10);

    private static final BasicStroke FAT_STROKE = new BasicStroke(5);

    private boolean displayedSetupMessage = false;

    // Length of animations for sprite moves
    private static final long MOVE_DELAY = 500;

    /**
     * Combination of all of the times of the animations that go into a piece
     * move
     */
    public static final long MAX_MOVE_DELAY = MOVE_DELAY + 1000;// 1000 is the

    // label fade
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

        /**
         * Fade in the text on this PieceSprite if the given new PeckingPiece
         * for it causes a reveal.
         * 
         * @return - true if newPiece caused a reveal and a fade is happening
         */
        protected boolean fadeInLabel(final PieceSprite sprite,
                                      final PeckingPiece newPiece)
        {
            if(sprite._piece.rank != UNKNOWN) {
                return false;
            }
            // If the step changes, change MAX_MOVE_DELAY
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
            return true;
        }

        public Point getLocation(PeckingPiece p)
        {
            return new Point(p.x * PieceSprite.SIZE, p.y * PieceSprite.SIZE);
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
            Point click = new Point(clickX, clickY);
            if(logic.getLegalMoves(_ctrl.getSelectedPiece()).contains(click)) {
                _ctrl.moveSelected(clickX, clickY);
                return;
            }
            if(clicked != null && clicked.owner == _ctrl.getColor()
                    && !PeckingPlayLogic.isImmobile(clicked)) {
                _ctrl.setSelectedPiece(clicked);
            }
        }

        private Path createPath(PieceSprite sprite,
                                final PeckingPiece piece,
                                long delay)
        {
            Path move;
            if(PeckingLogic.isOffBoard(piece)) {
                sprite.addSpriteObserver(new PathAdapter() {

                    public void pathCompleted(Sprite s, Path path, long when)
                    {
                        removeSprite(piece);
                    }
                });
                // CHUNK IT
                move = new ArcPath(new Point(sprite.getX(), sprite.getY()),
                                   500,
                                   500,
                                   50,
                                   1,
                                   MOVE_DELAY,
                                   ArcPath.NONE);
            } else {
                move = createSimpleMove(piece);
            }
            if(delay > 0) {
                return new PathSequence(new DelayPath(delay), move);
            }
            return move;
        }

        @Override
        public void update(PeckingPiece piece)
        {
            if(!sprites.containsKey(piece)) {
                PieceSprite sprite = new PieceSprite(piece, getLocation(piece));
                sprites.put(piece, sprite);
                addSprite(sprite);
                return;
            }
            PieceSprite pieceSprite = sprites.get(piece);
            if(defeatedPiece != null) {
                // Since we've got a defeated piece, we know the piece coming in
                // now is the victor
                PieceSprite defeatedSprite = sprites.get(defeatedPiece);
                long delay = 0;
                // Fade in any pieces revealed by the move
                if(fadeInLabel(pieceSprite, piece)
                        || fadeInLabel(defeatedSprite, defeatedPiece)) {
                    delay = 1000;
                }
                defeatedSprite.move(createPath(defeatedSprite,
                                               defeatedPiece,
                                               delay));
                pieceSprite.move(createPath(pieceSprite, piece, delay));
                defeatedPiece = null;
            } else if(PeckingLogic.isOffBoard(piece)) {
                // If a piece is defated, it's always returned first
                defeatedPiece = piece;
            } else {
                pieceSprite.move(createSimpleMove(piece));
            }
        }

        private Path createSimpleMove(PeckingPiece piece)
        {
            return new LinePath(getLocation(piece), MOVE_DELAY);
        }

        private PeckingPiece defeatedPiece;
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
                if(removeSprite(piece) && piece.rank != UNKNOWN){
                    knownPieces--;
                }
            } else {
                if(!sprites.containsKey(piece)) {
                    PieceSprite sprite = new PieceSprite(piece,
                                                         getLocation(piece));
                    sprites.put(piece, sprite);
                    addSprite(sprite);
                    if(piece.rank != UNKNOWN){
                        knownPieces++;
                    }
                } else {
                    sprites.get(piece).update(piece, getLocation(piece));
                }
                if(piece.equals(_ctrl.getSelectedPiece())) {
                    sprites.get(piece).setSelected(true);
                }
            }
            if(knownPieces == 40 && !shownClickReady) {
                displayFloatingText("b.click_ready", 2500);
                //Only tell player to click ready once
                shownClickReady = true;
            }
        }
        
        private int knownPieces;
        
        private boolean shownClickReady = false;
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
