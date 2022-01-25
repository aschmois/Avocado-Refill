package cool.avocado.mc.forge.refill.compats.curios;

import cool.avocado.mc.forge.refill.compats.ICompat;
import cool.avocado.mc.forge.refill.utils.InventoryUtils;

public class CuriosCompat implements ICompat {
    @Override
    public void setup() {
        InventoryUtils.curiosInterop = new RealCuriosInterop();
    }
}
