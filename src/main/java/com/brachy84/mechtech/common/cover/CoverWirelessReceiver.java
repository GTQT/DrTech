package com.brachy84.mechtech.common.cover;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.brachy84.mechtech.client.ClientHandler;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;

public class CoverWirelessReceiver extends CoverBase {

    public CoverWirelessReceiver(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView, @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        IEnergyContainer container = coverable.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side);
        return container != null && container.inputsEnergy(getAttachedSide());
    }

    @Override
    public void renderCover(@NotNull CCRenderState ccRenderState, @NotNull Matrix4 matrix4, @NotNull IVertexOperation[] iVertexOperations, @NotNull Cuboid6 cuboid6, @NotNull BlockRenderLayer blockRenderLayer) {
        ClientHandler.COVER_WIRELESS_RECEIVER.renderSided(getAttachedSide(), cuboid6, ccRenderState, iVertexOperations, matrix4);
    }
}
