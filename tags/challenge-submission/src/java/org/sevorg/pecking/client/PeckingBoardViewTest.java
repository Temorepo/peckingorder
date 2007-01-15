package org.sevorg.pecking.client;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.sevorg.pecking.PeckingConstants;
import org.sevorg.pecking.PeckingLogic;
import org.sevorg.pecking.PeckingPlayLogic;
import org.sevorg.pecking.data.PeckingPiece;
import com.threerings.toybox.util.GameViewTest;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * A test harness for our board view.
 */
public class PeckingBoardViewTest extends GameViewTest implements
        PeckingConstants
{

    public static void main(String[] args)
    {
        PeckingBoardViewTest test = new PeckingBoardViewTest();
        test.display();
    }

    protected JComponent createInterface(ToyBoxContext ctx)
    {
        return _view = new PeckingBoardView(ctx, new PeckingController() {

            public PeckingLogic createLogic()
            {
                return new PeckingPlayLogic(pieces);
            }
        });
    }

    public static List<PeckingPiece> createPieces()
    {
        List<PeckingPiece>pieces = new ArrayList<PeckingPiece>(80);
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < COUNT_BY_RANK.length; j++) {
                for(int k = 0; k < COUNT_BY_RANK[j]; k++) {
                    pieces.add(new PeckingPiece(i,
                                                j + 1,
                                                OFF_BOARD,
                                                OFF_BOARD,
                                                pieces.size()));
                }
            }
        }
        return pieces;
    }

    protected void initInterface()
    {
        for(int i = 0; i < pieces.size(); i++) {
            PeckingPiece p = pieces.get(i);
            p.x = i % 10;
            p.y = i / 10;
            if(i % 2 == 0) {
                p.rank = UNKNOWN;
                if(i % 4 == 0) {
                    p.x = OFF_BOARD;
                    p.y = OFF_BOARD;
                } 
            }
            _view.updatePiece(p);
        }
        _view.updatePhase(PLAY);
        PeckingLogic logic = new PeckingPlayLogic(pieces);
        PeckingPiece simpleMovePiece = logic.getPieceAt(1, 0);
        simpleMovePiece.x = 0;
        _view.updatePiece(simpleMovePiece);
        PeckingPiece complexMoveSrc = logic.getPieceAt(2, 0);
        PeckingPiece complexMoveDest = logic.getPieceAt(3, 0);
        PeckingPiece complexMoveSrcOffBoard = complexMoveSrc.copyOffBoard();
        pieces.remove(complexMoveSrc);
        pieces.add(complexMoveSrcOffBoard);
        complexMoveSrcOffBoard.rank = 4;
        complexMoveSrcOffBoard.x = 3;
        complexMoveSrcOffBoard.y = 0;
        _view.updatePiece(complexMoveSrcOffBoard);
        complexMoveDest.x = OFF_BOARD;
        complexMoveDest.y = OFF_BOARD;
        _view.updatePiece(complexMoveDest);
    }

    private List<PeckingPiece> pieces = createPieces();

    protected PeckingBoardView _view;
}
