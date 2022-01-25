package cool.avocado.mc.forge.refill.compats;

import cool.avocado.mc.forge.refill.compats.backpacks.SophisticatedBackpacksCompat;
import cool.avocado.mc.forge.refill.compats.curios.CuriosCompat;
import cool.avocado.mc.forge.refill.logger.Log;
import net.minecraftforge.fml.ModList;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class Compat {
    private static final Map<String, Supplier<Callable<ICompat>>> compatFactories = new HashMap<>();

    static {
        compatFactories.put("sophisticatedbackpacks", () -> SophisticatedBackpacksCompat::new);
        compatFactories.put("curios", () -> CuriosCompat::new);
    }

    public static void initCompats() {
        for (Map.Entry<String, Supplier<Callable<ICompat>>> entry : compatFactories.entrySet()) {
            if (ModList.get().isLoaded(entry.getKey())) {
                try {
                    entry.getValue().get().call().setup();
                    Log.debug("Loaded mod compat: " + entry.getKey());
                } catch (Exception e) {
                    Log.error("Error instantiating compatibility ", e);
                }
            }
        }
    }
}
