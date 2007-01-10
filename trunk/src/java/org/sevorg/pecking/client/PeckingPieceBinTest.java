package org.sevorg.pecking.client;

import javax.swing.JComponent;
import org.sevorg.pecking.PeckingConstants;
import org.sevorg.pecking.data.PeckingPiece;
import org.sevorg.pecking.data.PeckingPiecesObject;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.toybox.util.GameViewTest;
import com.threerings.toybox.util.ToyBoxContext;

public class PeckingPieceBinTest extends GameViewTest implements
        PeckingConstants
{

    @Override
    protected JComponent createInterface(ToyBoxContext arg0)
    {
        return _view = new PeckingPieceBin(arg0, new PeckingController(), BLUE);
    }

    protected void initInterface()
    {
        for(PeckingPiece p : PeckingBoardViewTest.createPieces()) {
            p.rank = UNKNOWN;
            _view.entryAdded(new EntryAddedEvent<PeckingPiece>(-1, PeckingPiecesObject.PIECES, p));
        }
    }

    public static void main(String[] args)
    {
        PeckingPieceBinTest test = new PeckingPieceBinTest();
        test.display();
    }

    private PeckingPieceBin _view;
}
