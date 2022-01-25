package cool.avocado.mc.forge.refill.compats.backpacks;

import cool.avocado.mc.forge.refill.compats.ICompat;
import cool.avocado.mc.forge.refill.utils.InventoryUtils;

public class SophisticatedBackpacksCompat implements ICompat {
    @Override
    public void setup() {
        InventoryUtils.backpacksInterop = new SophisticatedBackpacksInterop();
    }
}
