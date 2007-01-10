package org.sevorg.pecking.client;

import org.sevorg.pecking.data.PeckingPiece;

public interface PieceSelectedListener
{

    public void selectionChanged(PeckingPiece changedPiece, boolean newValue);
}
