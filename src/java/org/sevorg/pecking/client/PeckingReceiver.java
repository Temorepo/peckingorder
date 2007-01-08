package org.sevorg.pecking.client;

import com.threerings.presents.client.InvocationReceiver;

public interface PeckingReceiver extends InvocationReceiver
{

    /**
     * This gives the client its particular PeckingPiecesObject oid so that it
     * can immediately turn around and subscribe to it
     * 
     * TODO - Find a way to send a presubscribed DObject to a client to avoid
     * this rigamarole
     */
    public void setPeckingPiecesObjectOid(int peckingPiecesObjOid);
}
