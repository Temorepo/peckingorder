package org.sevorg.pecking.client;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.sevorg.pecking.PeckingConstants;
import org.sevorg.pecking.data.PeckingPiece;
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
        return _view = new PeckingBoardView(ctx, new PeckingController());
    }

    public static List<PeckingPiece> createPieces()
    {
        List<PeckingPiece> pieces = new ArrayList<PeckingPiece>(80);
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < PeckingConstants.COUNT_BY_RANK.length; j++) {
                for(int k = 0; k < PeckingConstants.COUNT_BY_RANK[j]; k++) {
                    pieces.add(new PeckingPiece(i, j + 1));
                }
            }
        }
        return pieces;
    }


    protected void initInterface()
    {
        List<PeckingPiece> pieces = createPieces();
        for(int i = 0; i < pieces.size(); i++) {
            PeckingPiece p = pieces.get(i);
            p.x = i%10;
            p.y = i/10;
            if(i%2 == 0){
                p.rank = PeckingConstants.UNKNOWN;
                if(i%4 == 0){
                    p.x = PeckingConstants.OFF_BOARD;
                    p.y = PeckingConstants.OFF_BOARD;
                }
            }
            _view.pieceUpdated(p);
        }
    }

    protected PeckingBoardView _view;
}
