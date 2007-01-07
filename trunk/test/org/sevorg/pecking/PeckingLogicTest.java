package org.sevorg.pecking;

import junit.framework.TestCase;
import com.threerings.presents.dobj.DSet;

public class PeckingLogicTest extends TestCase implements PeckingConstants
{

    public void setUp()
    {
        pieces = new TestDSet();
        logic = new PeckingLogic(pieces);
        // A SCOUT in the corner
        pieces.addPiece(RED, SCOUT, 0, 0);
        pieces.addPiece(RED, CAGE, 1, 0);
        // Only the WORM on the BLUE team
        pieces.addPiece(BLUE, WORM, 9, 9);
    }

    public void testImmobileDraw()
    {
        pieces.addPiece(RED, WORM, 0, 1);
        assertTrue(logic.shouldEndGame());
        assertTrue(logic.isWinner(BLUE));
        assertTrue(logic.isWinner(RED));
    }

    public void testImmobileWithWinner()
    {
        // Box a SCOUT in the corner
        pieces.addPiece(RED, CAGE, 1, 0);
        // Add an offboard worm
        pieces.addPiece(RED, WORM, OFF_BOARD, OFF_BOARD);
        assertTrue(logic.shouldEndGame());
        assertTrue(logic.isWinner(BLUE));
        assertFalse(logic.isWinner(RED));
    }

    public void testWormCapturedWinner()
    {
        // Add an offboard worm
        pieces.addPiece(RED, WORM, OFF_BOARD, OFF_BOARD);
        assertTrue(logic.shouldEndGame());
        assertTrue(logic.isWinner(BLUE));
        assertFalse(logic.isWinner(RED));
    }

    protected static class TestDSet extends DSet<PeckingPiece>
    {

        public void addPiece(int owner, int rank, int x, int y)
        {
            add(new PeckingPiece(owner, rank, x, y, size()));
        }
    }

    private PeckingLogic logic;

    private TestDSet pieces;
}
