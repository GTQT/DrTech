package com.drppp.drtech.intergations.top.provider;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntitySapBag;
import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.MetaTileEntityBaseWithControl;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.MetaTileEntityMatrixSolver;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.MetaTileEntutyLargeBeeHive;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityIndustrialApiary;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

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
                if (stack.getItem()== DrMetaItems.TELEPATHIC_NECKLACE.getMetaItem() && stack.getMetadata()==DrMetaItems.TELEPATHIC_NECKLACE.getMetaValue()) {
                    flag=true;
                }
            }
        }
        if(GTUtility.getMetaTileEntity(world,iProbeHitData.getPos()) instanceof MetaTileEntityBaseWithControl)
        {
            MetaTileEntityBaseWithControl s = (MetaTileEntityBaseWithControl) GTUtility.getMetaTileEntity(world,iProbeHitData.getPos());
            iProbeInfo.progress(s.getProgress(), s.getMaxProgress(), iProbeInfo.defaultProgressStyle()
                    .suffix(" / " + TextFormattingUtil.formatNumbers(s.getMaxProgress()) + " t")
                    .filledColor(0xFFEEE600)
                    .alternateFilledColor(0xFFEEE600)
                    .borderColor(0xFF555555).numberFormat(mcjty.theoneprobe.api.NumberFormat.COMMAS));
            if(GTUtility.getMetaTileEntity(world,iProbeHitData.getPos()) instanceof MetaTileEntityMatrixSolver)
            {
                MetaTileEntityMatrixSolver ss= (MetaTileEntityMatrixSolver) GTUtility.getMetaTileEntity(world,iProbeHitData.getPos());
                iProbeInfo.text("耗电:"+ss.EUT+"Eu/t");
                iProbeInfo.text("产出信息:");
                if(ss.isWorkingEnabled() && ss.run_recipe!=null)
                {
                    for (var item:ss.run_recipe.outputItems) {
                        iProbeInfo.text(item.getDisplayName() +"*"+item.getCount());
                    }
                    for (var fluid:ss.run_recipe.outputFluids) {
                        iProbeInfo.text(fluid.getLocalizedName() +"*"+fluid.amount);
                    }
                }
            }
        }
        if(entityPlayer.isSneaking() || flag)
        {
            addProbeInfoWithGlassOrSneak(probeMode,iProbeInfo,entityPlayer,world,iBlockState,iProbeHitData);
        }
    }//MetaTileEntutyLargeBeeHive
    public void addProbeInfoWithGlassOrSneak(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world, IBlockState iBlockState, IProbeHitData iProbeHitData) {
        if (GTUtility.getMetaTileEntity(world,iProbeHitData.getPos()) instanceof MetaTileEntutyLargeBeeHive) {
            var s = (MetaTileEntutyLargeBeeHive)GTUtility.getMetaTileEntity(world,iProbeHitData.getPos());
            if(s.isActive() && s.isWorkingEnabled())
            {

                List<String> list = new ArrayList<>();
                List<Integer> listNum = new ArrayList<>();
                for (var is:s.listdrops)
                {
                    if(!list.contains(is.getDisplayName()))
                    {
                        list.add(is.getDisplayName());
                        listNum.add(is.getCount());
                    }
                    else
                    {
                        int loca = list.indexOf(is.getDisplayName());
                        int num = listNum.remove(loca);
                        listNum.add(loca,num+is.getCount());
                    }
                }
                int maxprocess = s.maxProcess;
               if( s.workType==1 && s.productType==1)
                   maxprocess*=4;
                iProbeInfo.progress(s.getProgress(), maxprocess, iProbeInfo.defaultProgressStyle()
                        .suffix(" / " + TextFormattingUtil.formatNumbers(maxprocess) + " %")
                        .filledColor(0xFFEEE600)
                        .alternateFilledColor(0xFFEEE600)
                        .borderColor(0xFF555555).numberFormat(mcjty.theoneprobe.api.NumberFormat.COMMAS));
                for (int i = 0; i < listNum.size(); i++) {
                    String item = list.get(i);
                    int num = listNum.get(i);
                    iProbeInfo.text("产出物品:"+item +"*"+num);
                }
            }
        }
        else if(world.getTileEntity(iProbeHitData.getPos()) instanceof TileEntitySapBag)
        {
            ItemStackHandler inventory = ((TileEntitySapBag)(world.getTileEntity(iProbeHitData.getPos()))).inventory;
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack s  =inventory.getStackInSlot(i).copy();
                iProbeInfo.text("贮存物品:"+ s.getDisplayName() +"*"+s.getCount());
            }

        }else if(GTUtility.getMetaTileEntity(world,iProbeHitData.getPos()) instanceof MetaTileEntityIndustrialApiary)
        {
            var machine = (MetaTileEntityIndustrialApiary)GTUtility.getMetaTileEntity(world,iProbeHitData.getPos());
            iProbeInfo.text("耗电:"+machine.mEUt+"Eu/t");
            iProbeInfo.text("温度:"+machine.getTemperature());
            iProbeInfo.text("湿度:"+machine.getHumidity());
            var list = machine.mOutputItems;
            if(machine.isWorking())
                for (int i = 0; i < list.length; i++) {
                    if(!list[i].isEmpty())
                    {
                        String item = list[i].getDisplayName();
                        int num = list[i].getCount();
                        iProbeInfo.text("产出物品:"+item +"*"+num);
                    }
                }
            if(!machine.hasErrors())
                iProbeInfo.text("没有任何错误,老铁!");
            else
            {
                iProbeInfo.text("错误列表:");
                machine.mErrorStates.forEach(info->{
                    iProbeInfo.text("{*"+info.getUnlocalizedDescription()+"*}");
                });
            }
        }
    }
}
