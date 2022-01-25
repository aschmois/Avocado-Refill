package cool.avocado.mc.forge.refill.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Handler {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();

    public static class General {
        public final ForgeConfigSpec.ConfigValue<Boolean> protectArmor;
        public final ForgeConfigSpec.ConfigValue<Boolean> protectTools;
        public final ForgeConfigSpec.ConfigValue<Boolean> lookInBackpacks;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableDebug;

        public General(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            protectArmor = builder
                    .comment("Protect armor items from breaking")
                    .define("protectArmor", true);
            protectTools = builder
                    .comment("Protect tools & weapons from breaking")
                    .define("protectTools", true);
            lookInBackpacks = builder
                    .comment("Look for items in backpacks to refill. Currently supports sophisticated backpacks")
                    .define("lookInBackpacks", true);
            enableDebug = builder
                    .comment("Enable debug logs")
                    .define("enableDebug", false);

            builder.pop();
        }
    }
}