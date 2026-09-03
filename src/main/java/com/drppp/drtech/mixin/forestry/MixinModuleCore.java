package com.drppp.drtech.mixin.forestry;

import binnie.core.modules.BlankModule;
import binnie.genetics.modules.ModuleCore;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModuleCore.class)
public class MixinModuleCore extends BlankModule {
    public MixinModuleCore() {
        super("forestry", "core");
    }

    @Override
    public void doInit() {

    }
}
