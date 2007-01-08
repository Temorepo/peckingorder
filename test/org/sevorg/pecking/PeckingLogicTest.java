package org.sevorg.pecking;

import org.sevorg.pecking.data.PeckingPiece;
import junit.framework.TestCase;
import com.threerings.presents.dobj.DSet;

public class PeckingLogicTest extends TestCase implements PeckingConstants
{

    public void setUp()
    {
        pieces = new TestDSet();
        logic = new PeckingLogic(pieces);
        // A SCOUT in the corner
        originScout = pieces.addPiece(RED, SCOUT, 0, 0);
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

    public void testMovingPieceOntoEquallyRankedPiece()
    {
        PeckingPiece p = pieces.addPiece(BLUE, SCOUT, 0, 9);
        p.revealed = false;// To test that attacking with a pieces reveals it
        checkResults(logic.move(p, 0, 0), 2, new int[][] { {SCOUT, -1, -1},
                                                          {SCOUT, -1, -1}});
    }

    public void testAssassinAttackingMarshall()
    {
        pieces.addPiece(BLUE, MARSHALL, 0, 9);
        PeckingPiece[] results = logic.move(pieces.addPiece(RED, ASSASSIN, 1, 9),
                                            0,
                                            9);
        checkResults(results, 2, new int[][] { {MARSHALL, -1, -1},
                                              {ASSASSIN, 0, 9}});
    }

    public void testMarshallAttackingAssassin()
    {
        pieces.addPiece(BLUE, ASSASSIN, 1, 9);
        PeckingPiece[] results = logic.move(pieces.addPiece(RED, MARSHALL, 0, 9),
                                            1,
                                            9);
        checkResults(results, 2, new int[][] { {MARSHALL, 1, 9},
                                              {ASSASSIN, -1, -1}});
    }

    public void testGreaterAttackingLesser()
    {
        pieces.addPiece(BLUE, 3, 1, 9);
        PeckingPiece[] results = logic.move(pieces.addPiece(RED, 2, 0, 9), 1, 9);
        checkResults(results, 2, new int[][] { {2, 1, 9}, {3, -1, -1}});
    }

    public void testLesserAttackingGreater()
    {
        pieces.addPiece(BLUE, 2, 1, 9);
        checkResults(logic.move(pieces.addPiece(RED, 3, 0, 9), 1, 9),
                     1,
                     new int[][] {{3, -1, -1}});
    }

    public void testMarshallAttackingCage()
    {
        pieces.addPiece(BLUE, CAGE, 1, 9);
        checkResults(logic.move(pieces.addPiece(RED, 1, 0, 9), 1, 9),
                     1,
                     new int[][] {{MARSHALL, -1, -1}});
    }

    public void testCageOpenerAttackingCage()
    {
        pieces.addPiece(BLUE, CAGE, 1, 9);
        checkResults(logic.move(pieces.addPiece(RED, CAGE_OPENER, 0, 9), 1, 9),
                     2,
                     new int[][] { {CAGE, -1, -1}, {CAGE_OPENER, 1, 9}});
    }

    public void testIllegalMoveReturnsZeroLength()
    {
        assertEquals(0,
                     logic.move(originScout, originScout.x, originScout.y).length);
    }

    private void checkResults(PeckingPiece[] results,
                              int expectedLength,
                              int[][] expectedValues)
    {
        assertEquals(expectedLength, results.length);
        outer : for(int i = 0; i < expectedValues.length; i++) {
            for(int j = 0; j < results.length; j++) {
                if(expectedValues[i][0] == results[j].rank) {
                    assertEquals(expectedValues[i][1], results[j].x);
                    assertEquals(expectedValues[i][2], results[j].y);
                    assertTrue(results[i].revealed);
                    continue outer;
                }
            }
            fail("We got back a piece with an unexpected rank!");
        }
    }

    protected static class TestDSet extends DSet<PeckingPiece>
    {

        public PeckingPiece addPiece(int owner, int rank, int x, int y)
        {
            // Set the pieces id to the size of the set. Should be unique as
            // long as things aren't removed
            PeckingPiece p = new PeckingPiece(owner, rank, x, y, size());
            add(p);
            return p;
        }
    }

    private PeckingPiece originScout;

    private PeckingLogic logic;

    private TestDSet pieces;
}
