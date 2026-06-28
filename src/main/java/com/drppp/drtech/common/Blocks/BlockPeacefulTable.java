package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityPeacefulTable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockPeacefulTable extends Block {
    public BlockPeacefulTable() {
        super(Material.WOOD);
        this.setResistance(5F);
        this.setRegistryName(Tags.MODID,"peaceful_table");
        this.setCreativeTab(DrTechMain.DrTechTab);
        this.setTranslationKey(Tags.MODID+".peaceful_table");
    }
    @Nonnull
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityPeacefulTable();
    }
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(I18n.format("tile.drtech.peaceful_table.tooltip.1"));
        tooltip.add(I18n.format("tile.drtech.peaceful_table.tooltip.2"));
        tooltip.add(I18n.format("tile.drtech.peaceful_table.tooltip.3"));
    }
}
