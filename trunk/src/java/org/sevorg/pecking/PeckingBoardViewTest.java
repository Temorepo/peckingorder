package org.sevorg.pecking;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.sevorg.pecking.PeckingObject.Piece;
import com.threerings.toybox.util.GameViewTest;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * A test harness for our board view.
 */
public class PeckingBoardViewTest extends GameViewTest
{

    public static void main(String[] args)
    {
        PeckingBoardViewTest test = new PeckingBoardViewTest();
        test.display();
    }

    protected JComponent createInterface(ToyBoxContext ctx)
    {
        return _view = new PeckingBoardView(ctx, logic);
    }

    public static List<Piece> createPieces()
    {
        List<Piece> pieces = new ArrayList<Piece>(40);
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < PeckingConstants.COUNT_BY_RANK.length; j++) {
                for(int k = 0; k < PeckingConstants.COUNT_BY_RANK[j]; k++) {
                    pieces.add(new Piece(i, j + 1, PeckingConstants.BIRD));
                }
            }
            pieces.add(new Piece(i, -1, PeckingConstants.WORM));
            for(int j = 0; j < 6; j++) {
                pieces.add(new Piece(i, -1, PeckingConstants.CAGE));
            }
        }
        return pieces;
    }


    protected void initInterface()
    {
        List<Piece> pieces = createPieces();
        for(int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            p.x = i%10;
            p.y = i/10;
            if(i%2 == 0){
                p.type = PeckingConstants.UNKNOWN;
                p.rank = PeckingConstants.UNKNOWN;
                if(i%4 == 0){
                    p.x = PeckingConstants.OFF_BOARD;
                    p.y = PeckingConstants.OFF_BOARD;
                }
            }
            logic.setState(pieces.toArray(new Piece[0])); 
            _view.pieceUpdated(i, p);
        }
    }

    private PeckingLogic logic = new PeckingLogic();
    
    protected PeckingBoardView _view;
}
