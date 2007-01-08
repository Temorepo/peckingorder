package org.sevorg.pecking.client;

import com.threerings.presents.client.InvocationDecoder;
import org.sevorg.pecking.client.PeckingReceiver;

/**
 * Dispatches calls to a {@link PeckingReceiver} instance.
 */
public class PeckingDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static final String RECEIVER_CODE = "5c633e7f31a0a065788bd056375b612a";

    /** The method id used to dispatch {@link PeckingReceiver#setPeckingPiecesObjectOid}
     * notifications. */
    public static final int SET_PECKING_PIECES_OBJECT_OID = 1;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public PeckingDecoder (PeckingReceiver receiver)
    {
        this.receiver = receiver;
    }

    // documentation inherited
    public String getReceiverCode ()
    {
        return RECEIVER_CODE;
    }

    // documentation inherited
    public void dispatchNotification (int methodId, Object[] args)
    {
        switch (methodId) {
        case SET_PECKING_PIECES_OBJECT_OID:
            ((PeckingReceiver)receiver).setPeckingPiecesObjectOid(
                ((Integer)args[0]).intValue()
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
            return;
        }
    }
}
