package org.nustaq.kontraktor.apputil;

import io.undertow.server.HttpServerExchange;
import org.nustaq.kontraktor.Actor;
import org.nustaq.kontraktor.Actors;
import org.nustaq.kontraktor.IPromise;
import org.nustaq.kontraktor.annotations.CallerSideMethod;
import org.nustaq.kontraktor.annotations.Local;
import org.nustaq.kontraktor.remoting.http.undertow.builder.BldFourK;
import org.nustaq.kontraktor.services.rlclient.DataClient;
import org.nustaq.reallive.api.Record;

import java.util.UUID;

public interface LinkMapperMixin<SELF extends Actor<SELF>> {

    public static String TableName = "links";

    static void auto(BldFourK bld, Object linkMapper) {
        bld.httpHandler("link", httpServerExchange ->  {
            httpServerExchange.dispatch();
            ((LinkMapperMixin)linkMapper).handleRawHttp(httpServerExchange);
        });
    }

    @CallerSideMethod @Local DataClient getDClient();

    /**
     * @param linkId
     * @param linkRecord
     * @return htmlpage to render
     */
    @CallerSideMethod @Local String handleLinkSuccess(String linkId, RegistrationRecordWrapper linkRecord );

    /**
     * @param linkId
     * @return htmlpage to render
     */
    @CallerSideMethod @Local String handleLinkFailure(String linkId);
    SELF getActor();

    /**
     * return uuid to use as link
     * @param rec
     * @return
     */
    default IPromise<String> putRegistrationRecord(RegistrationRecordWrapper rec /*e.g. maprecord*/ ) {
        String key  = UUID.randomUUID().toString();
        rec.key(key);
        getDClient().tbl("links" ).setRecord(rec.key(key));
        return Actors.resolve(key);
    }

    /**
     * assume registration on builder with e.g.
     *
     * .httpHandler("link", httpServerExchange ->  {
     *    httpServerExchange.dispatch();
     *    app.handleRawHttp(httpServerExchange);
     * })
     *
     * @param httpServerExchange
     */
    default void handleRawHttp(HttpServerExchange httpServerExchange) {
        String path = httpServerExchange.getRelativePath();
        if ( path.startsWith("/") )
            path = path.substring(1);
        String finalPath = path;
        getDClient().tbl(TableName).get(path).then( (rec, err) -> {
           if ( rec != null ) {
               httpServerExchange.setResponseCode(200).getResponseSender().send(((LinkMapperMixin)getActor()).handleLinkSuccess(finalPath,new RegistrationRecordWrapper(rec)));
           } else {
               httpServerExchange.setResponseCode(200).getResponseSender().send(((LinkMapperMixin)getActor()).handleLinkFailure(finalPath));
           }
        });
    }

}