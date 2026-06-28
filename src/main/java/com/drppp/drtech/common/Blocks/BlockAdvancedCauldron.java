package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityAdvancedCauldron;
import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class BlockAdvancedCauldron extends BlockCauldron {

    public BlockAdvancedCauldron() {
        super();
        this.setHardness(2.0F);
        this.setTranslationKey("advanced_cauldron");
        this.setRegistryName(Tags.MODID,"advanced_cauldron");
        this.setCreativeTab(DrTechMain.DrTechTab);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityAdvancedCauldron();
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return ItemsInit.ITEM_BLOCK_ADVANCED_CAULDRON;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(I18n.format("tile.advanced_cauldron.tooltip.1"));
        tooltip.add(I18n.format("tile.advanced_cauldron.tooltip.2"));
    }
}
