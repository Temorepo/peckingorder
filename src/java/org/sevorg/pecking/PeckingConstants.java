package org.sevorg.pecking;

public interface PeckingConstants
{
    
    public static final int NUM_PIECES = 80;

    public static final int OFF_BOARD = -1;

    //Game phases
    public static final int SETUP = 0;

    public static final int PLAYING = 1;

    public static final int RED = 0;

    public static final int BLUE = 1;

    // Rank on Piece uses this for unknown
    public static final int UNKNOWN = -1;

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

    // Ranks with special abilities. The ranks 1-7 just indicate their strength
    public static final int CAGE_OPENER = 8;

    public static final int SCOUT = 9;

    public static final int ASSASSIN = 10;
    
    //Ranks that can't move
    public static final int CAGE = 11;

    public static final int WORM = 12;
}
