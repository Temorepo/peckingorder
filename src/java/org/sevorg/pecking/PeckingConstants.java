package org.sevorg.pecking;

public interface PeckingConstants
{

    /**
     * A position on piece indicating it's off the board
     */
    public static final int OFF_BOARD = -1;

    /**
     * Piece layout game phase
     */
    public static final int SETUP = 0;

    /**
     * Phase after piece layout, when players are going back and forth moving
     * piecess
     */
    public static final int PLAYING = 1;

    public static final int RED = 0;

    public static final int BLUE = 1;

    /**
     * Number of pieces of each rank in a standard game. Since pieces start at
     * 1, add 1 to the array index for each rank
     */
    public static final int[] COUNT_BY_RANK = new int[] {1,
                                                         1,
                                                         2,
                                                         3,
                                                         4,
                                                         4,
                                                         4,
                                                         5,
                                                         8,
                                                         1,
                                                         6,
                                                         1};

    /**
     * Unknown piece rank
     */
    public static final int UNKNOWN = -1;

    /**
     * Highest rank. Can only be defated by assassin
     */
    public static final int MARSHALL = 1;

    /**
     * Rank that can open cages.
     */
    public static final int CAGE_OPENER = 8;

    /**
     * Rank that can move any open distance in a straight line
     */
    public static final int SCOUT = 9;

    /**
     * Weakest bird, however if it attacks MARSHALL it will win
     */
    public static final int ASSASSIN = 10;

    /**
     * An immobile item that defeats all other ranks except CAGE_OPENER
     */
    public static final int CAGE = 11;

    /**
     * An immobile item that any moving piece can capture. If captured, the game
     * ends.
     */
    public static final int WORM = 12;
}
