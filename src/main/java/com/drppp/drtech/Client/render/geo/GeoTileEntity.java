package com.drppp.drtech.Client.render.geo;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.file.AnimationFile;

public class GeoTileEntity extends TileEntity implements IAnimatable, ITickable {
    private final AnimationFactory manager = new AnimationFactory(this);

    @Override
    public void registerControllers(AnimationData data) {

    }

    @Override
    public AnimationFactory getFactory() {
        return this.manager;
    }


    @Override
    public void update() {

    }

}
