package com.drppp.drtech.common.MetaTileEntities.muti.appeng;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.MetaTileEntityBaseWithControl;
import gregtech.common.ConfigHolder;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MetaTileentityLargeMolecularAssembler extends MetaTileEntityBaseWithControl implements ICraftingProvider, IActionHost, IGridProxyable {
    private boolean allowExtraConnections;
    private AENetworkProxy gridProxy;
    public MetaTileentityLargeMolecularAssembler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Nullable
    @Override
    public IGridNode getGridNode(@NotNull AEPartLocation aePartLocation) {
        return null;
    }

    @Override
    public void securityBreak() {

    }

    @Override
    public void provideCrafting(ICraftingProviderHelper iCraftingProviderHelper) {

    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails iCraftingPatternDetails, InventoryCrafting inventoryCrafting) {
        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @NotNull
    @Override
    public IGridNode getActionableNode() {
        return getProxy().getNode();
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(
                this.getWorld(),
                this.getPos());
    }

    @Override
    public @NotNull AECableType getCableConnectionType(@NotNull AEPartLocation part) {
        if (part.getFacing() != this.frontFacing && !this.allowExtraConnections) {
            return AECableType.NONE;
        }
        return AECableType.SMART;
    }

    @Override
    public @Nullable AENetworkProxy getProxy() {
        if (this.gridProxy == null) {
            return this.gridProxy = this.createProxy();
        }
        if (!this.gridProxy.isReady() && this.getWorld() != null) {
            this.gridProxy.onReady();
        }
        return this.gridProxy;
    }

    @Override
    public void gridChanged() {
    }
    @Nullable
    private AENetworkProxy createProxy() {
        if (this.getHolder() instanceof IGridProxyable holder) {
            AENetworkProxy proxy = new AENetworkProxy(holder, "mte_proxy", this.getStackForm(), true);
            proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            proxy.setIdlePowerUsage(ConfigHolder.compat.ae2.meHatchEnergyUsage);
            proxy.setValidSides(getConnectableSides());
            return proxy;
        }
        return null;
    }
    public EnumSet<EnumFacing> getConnectableSides() {
        return this.allowExtraConnections ? EnumSet.allOf(EnumFacing.class) : EnumSet.of(getFrontFacing());
    }
}
