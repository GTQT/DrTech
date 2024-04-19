package com.drppp.drtech.Farm;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.Crops.CropsInit;
import net.minecraft.block.BlockFarmland;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemFungus extends Item {
    public ItemFungus() {
        this.setRegistryName(Tags.MODID, "fungus");
        this.setTranslationKey(Tags.MODID + "." + "Fungus");
        this.setCreativeTab(DrTechMain.Mytab);
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);

        if (worldIn.isAirBlock(pos.up()) && !worldIn.isRemote)
        {
            worldIn.setBlockState(pos.up(), BlocksInit.BLOCK_FUNGUS.getDefaultState());
            stack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
        else
            return EnumActionResult.FAIL;
    }
}
