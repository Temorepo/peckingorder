package org.sevorg.pecking;

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
        return _view = new PeckingBoardView(ctx);
    }

    protected void initInterface()
    {
        List<Piece> pieces = PeckingObject.createPieces();
        for(int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            p.x = i%10;
            p.y = i/10;
            _view.addSprite(new PieceSprite(p));
        }
    }

    protected PeckingBoardView _view;
}
