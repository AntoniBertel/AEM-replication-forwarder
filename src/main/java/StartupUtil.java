import config.models.GlobalConfiguration;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.config.ConfigRetriever;
import io.vertx.rxjava.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Created by antoni.bertel on 27.03.2017.
 */
public class StartupUtil {
    private static final Logger LOGGER = LogManager.getLogger("main");
    private static final String VERTX_OPTIONS_EVENT_LOOP_POOL_SIZE = "vertx.options.eventLoopPoolSize";
    private static final String VERTX_OPTIONS_BLOCKING_LOOP_POOL_SIZE = "vertx.options.blockingLoopPoolSize";
    public static final String FILE = "file";
    public static final String CONFIG_PATH_KEY = "path";
    public static final String CONFIG_PATH_VALUE = "replication-forwarder.json";
    public static final int CONFIG_SCAN_PERIOD = 5000;

    public static Optional<Integer> readBlockingPoolSize(VertxOptions applicationOption) {
        try {
            Integer blockingLoopPoolSize = Integer.parseInt(System.getProperty(VERTX_OPTIONS_BLOCKING_LOOP_POOL_SIZE));
            if (blockingLoopPoolSize > 0) {
                return Optional.of(blockingLoopPoolSize);
            }
        } catch (Exception ignored) {

        }
        LOGGER.info("Can't read configured blocking pool size, default will be used, value should be > 0, property name: {}", VERTX_OPTIONS_BLOCKING_LOOP_POOL_SIZE);
        return Optional.empty();
    }

    public static Optional<Integer> readEventLoopPoolSize(VertxOptions applicationOption) {
        try {
            Integer eventLoopPoolSize = Integer.parseInt(System.getProperty(VERTX_OPTIONS_EVENT_LOOP_POOL_SIZE));
            if (eventLoopPoolSize > 0) {
                return Optional.of(eventLoopPoolSize);
            }
        } catch (Exception ignored) {
        }
        LOGGER.info("Can't read configured loop pool size, default will be used, value should be > 0, property name: {}", VERTX_OPTIONS_EVENT_LOOP_POOL_SIZE);
        return Optional.empty();
    }

    public static Optional<GlobalConfiguration> readApplicationConfig(Vertx v) {
        ConfigStoreOptions fileStore = new ConfigStoreOptions().setType(FILE).setConfig(new JsonObject().put(CONFIG_PATH_KEY, CONFIG_PATH_VALUE));
        ConfigRetrieverOptions options = new ConfigRetrieverOptions().setScanPeriod(CONFIG_SCAN_PERIOD).addStore(fileStore);

        ConfigRetriever retriever = ConfigRetriever.create(v, options);
        retriever.getConfig(json -> {
            // Initial retrieval of the configuration
        });
        retriever.listen(change -> {
            Json.mapper.convertValue(change.getNewConfiguration().getMap(), GlobalConfiguration.class);
        });

        return null;

    }

}
