package com.drppp.drtech.Mixin.forestry;

import binnie.core.Constants;
import binnie.core.modules.BlankModule;
import binnie.genetics.modules.GeneticsModuleUIDs;
import binnie.genetics.modules.ModuleMachine;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModuleMachine.class)
public class MixinModuleMachine extends BlankModule {
    public MixinModuleMachine() {
        super(Constants.GENETICS_MOD_ID, GeneticsModuleUIDs.CORE);
    }
    @Override
    public void doInit() {
    }

}
