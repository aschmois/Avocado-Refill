package cool.avocado.mc.forge.refill.logger;

import cool.avocado.mc.forge.refill.config.Handler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cool.avocado.mc.forge.refill.Main.MODID;

public class Log {
    public static final Logger LOGGER = LogManager.getLogger();

    public static void info(String info) {
        LOGGER.info("[" + MODID + "]" + info);
    }

    public static void debug(String debug) {
        if (Handler.GENERAL.enableDebug.get()) {
            LOGGER.info("[" + MODID + "]" + debug);
        }
    }

    public static void error(String error, Exception e) {
        LOGGER.error("[" + MODID + "]" + error, e);
    }
}
