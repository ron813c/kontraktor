package org.nustaq.kontraktor.routers;

import org.nustaq.kontraktor.Actor;
import org.nustaq.kontraktor.Callback;
import org.nustaq.kontraktor.IPromise;
import org.nustaq.kontraktor.annotations.CallerSideMethod;
import org.nustaq.kontraktor.annotations.Local;
import org.nustaq.kontraktor.remoting.base.RemoteRegistry;
import org.nustaq.kontraktor.remoting.encoding.RemoteCallEntry;
import org.nustaq.kontraktor.remoting.encoding.SerializerType;
import org.nustaq.kontraktor.remoting.tcp.TCPNIOPublisher;
import org.nustaq.kontraktor.util.Log;
import java.util.ArrayList;

/**
 * HotHot failover Router
 *
 * forwards incoming messages to all registered service actors. The one replying first
 * is picked and results are routed back to client. For callback's/streaming (multiple results), the one sending first
 * response is picked.
 *
 * @param <T>
 */
public class HotHotFailoverKrouter<T extends HotHotFailoverKrouter> extends AbstractKrouter<T> {

    ArrayList<Actor> remoteServices;

    public void init() {
        remoteServices = new ArrayList<>();
    }

    public IPromise router$Register(Actor remoteRef) {
        ArrayList services = new ArrayList();
        services.addAll(remoteServices);
        services.add(remoteRef);
        remoteServices = services;
        return resolve();
    }

    @Local
    public void router$handleDisconnect(Actor x) {
        //FIXME: reply pending callbacks / promises with error
        boolean remove = remoteServices.remove(x.getActor());
        if ( ! remove )
            remove = remoteServices.remove(x.getActorRef());
        if ( ! remove ) {
//            Log.Warn(this,"failed to remove disconnected service "+x);
            // can happen multiple times
        } else {
            Log.Info(this, "removed service "+x);
        }
    }

    @Override @CallerSideMethod
    protected boolean dispatchRemoteCall(RemoteCallEntry rce, RemoteRegistry clientRemoteRegistry) {
        if ( getActor().remoteServices.size() == 0 )
            return false;
        // attention: breaking threading contract here ! (see immutable add in register)
        boolean[] done = {false};
        Callback[] selected = {null};
        dispatchImpl(rce, clientRemoteRegistry, done, selected);
        return true;
    }

    @CallerSideMethod
    protected void dispatchImpl(RemoteCallEntry rce, RemoteRegistry clientRemoteRegistry, boolean[] done, Callback[] selected) {
        getActor().remoteServices.forEach( service -> {
            forwardMultiCall(rce, (Actor) service,clientRemoteRegistry, done, selected);
        });
    }

}
