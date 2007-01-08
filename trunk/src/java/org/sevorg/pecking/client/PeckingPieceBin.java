package org.sevorg.pecking.client;

import com.threerings.media.MediaPanel;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;
import com.threerings.toybox.util.ToyBoxContext;

public class PeckingPieceBin extends MediaPanel implements SetListener
{

    public PeckingPieceBin(ToyBoxContext ctx, PeckingController ctrl)
    {
        super(ctx.getFrameManager());
        _ctrl = ctrl;
        _ctrl.addPeckingPiecesListener(this);
    }

    public void entryAdded(EntryAddedEvent event)
    {
    }

    public void entryRemoved(EntryRemovedEvent event)
    {
    }

    public void entryUpdated(EntryUpdatedEvent event)
    {
    }

    private PeckingController _ctrl;
}
