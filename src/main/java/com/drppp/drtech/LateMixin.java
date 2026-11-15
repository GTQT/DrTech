package com.drppp.drtech;

import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LateMixin implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();
        configs.add("mixins.drtech_late.json");

        if (Loader.isModLoaded("biomesoplenty")) {
            configs.add("mixins.drtech_bop.json");
        }

        return configs;
    }
}
