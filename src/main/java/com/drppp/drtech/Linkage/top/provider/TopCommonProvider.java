package com.drppp.drtech.Linkage.top.provider;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.drppp.drtech.Items.MetaItems.MyMetaItems;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.store.MetaTileEntityYotTank;
import com.drppp.drtech.Tags;
import gregtech.api.util.GTUtility;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public class TopCommonProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return Tags.MODID+":top_common_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world, IBlockState iBlockState, IProbeHitData iProbeHitData) {
        boolean flag = false;
        if (Loader.isModLoaded("baubles"))
        {

            IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(entityPlayer);
            for (int i = 0; i < baubles.getSlots(); ++i) {
                ItemStack stack = baubles.getStackInSlot(i);
                if (stack.getItem()== MyMetaItems.TELEPATHIC_NECKLACE.getMetaItem() && stack.getMetadata()==MyMetaItems.TELEPATHIC_NECKLACE.getMetaValue()) {
                    flag=true;
                }
            }
        }
        if(entityPlayer.isSneaking() || flag)
        {
            addProbeInfoWithGlassOrSneak(probeMode,iProbeInfo,entityPlayer,world,iBlockState,iProbeHitData);
        }
    }
    public void addProbeInfoWithGlassOrSneak(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world, IBlockState iBlockState, IProbeHitData iProbeHitData) {

    }
}
