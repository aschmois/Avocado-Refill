package cool.avocado.mc.forge.refill;

import cool.avocado.mc.forge.refill.compats.Compat;
import cool.avocado.mc.forge.refill.config.Handler;
import cool.avocado.mc.forge.refill.events.RefillEvent;
import cool.avocado.mc.forge.refill.events.RepairEvent;
import cool.avocado.mc.forge.refill.logger.Log;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "avocadorefill";

    public Main() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Handler.spec);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::loadComplete);
    }

    private void setup(final FMLCommonSetupEvent event) {
        Log.info("Hi from avocadorefill!");

        Compat.initCompats();
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        MinecraftForge.EVENT_BUS.register(new RefillEvent());
        MinecraftForge.EVENT_BUS.register(new RepairEvent());
    }
}
