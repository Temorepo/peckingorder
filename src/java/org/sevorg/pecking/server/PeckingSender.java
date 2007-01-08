package org.sevorg.pecking.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;
import org.sevorg.pecking.client.PeckingDecoder;
import org.sevorg.pecking.client.PeckingReceiver;

/**
 * Used to issue notifications to a {@link PeckingReceiver} instance on a
 * client.
 */
public class PeckingSender extends InvocationSender
{
    /**
     * Issues a notification that will result in a call to {@link
     * PeckingReceiver#setPeckingPiecesObjectOid} on a client.
     */
    public static void setPeckingPiecesObjectOid (
        ClientObject target, int arg1)
    {
        sendNotification(
            target, PeckingDecoder.RECEIVER_CODE, PeckingDecoder.SET_PECKING_PIECES_OBJECT_OID,
            new Object[] { Integer.valueOf(arg1) });
    }

}
