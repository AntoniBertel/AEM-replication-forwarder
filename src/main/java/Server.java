
import config.models.GlobalConfiguration;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.config.ConfigRetriever;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpClient;
import io.vertx.rxjava.core.http.HttpClientRequest;
import io.vertx.rxjava.core.http.HttpServer;
import org.apache.jackrabbit.util.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Server {


    public static void main(String[] args) {
        VertxOptions applicationOption = new VertxOptions();
        StartupUtil.readEventLoopPoolSize(applicationOption).ifPresent(applicationOption::setEventLoopPoolSize);
        StartupUtil.readBlockingPoolSize(applicationOption).ifPresent(applicationOption::setInternalBlockingPoolSize);

        Vertx v = Vertx.vertx(applicationOption);



        HttpServer replicator = v.createHttpServer(new HttpServerOptions().setPort(1234).setHost("localhost"));
        HttpClient hc = v.createHttpClient();


        replicator.requestStream().toObservable()
                .subscribe(request -> {
                    System.out.println(Long.parseLong(request.headers().get("Content-Length")));
                    final Long[] size = {0L};
                    HttpClientRequest client = hc.post(4503, "localhost", "/bin/receive?sling:authRequestLogin=1");
                    client.headers().setAll(request.headers());

                    String path = Text.unescape(client.headers().get("Path"));
                    if (path.equals("batch:")) {
//                        StringBuilder action = new StringBuilder();
//                        byte[] lengthBytes = new byte[8];
//                        request.customFrameHandler()
//                        long pathInfoSize = ByteBuffer.wrap(lengthBytes).order(ByteOrder.BIG_ENDIAN).getLong();
//                        byte[] pathInfoBytes = new byte[(int)pathInfoSize];
//                        is.read(pathInfoBytes);
//                        String[] paths = (new String(pathInfoBytes, "UTF-8")).split(":");
                    }

                    System.out.println();

                    client.handler(onResponse -> {
                        System.out.println("code:" + onResponse.statusCode());
                    });

                    request.handler(handler -> {
                        client.write(handler);
                        size[0] += handler.length();
                    }).endHandler(endHandler -> {
                        System.out.println("ENDED! " + size[0]);
                        request.response().end();
                        client.end();
                    });

                }, ex -> {

                });

        replicator.rxListen().subscribe(server -> {
                    // Server is listening
                },
                failure -> {
                    // Server could not start
                });

    }


    private String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException var3) {
            return value;
        }
    }

}