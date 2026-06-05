package com.drppp.drtech.lootgames.block;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import com.drppp.drtech.lootgames.api.block.BlockGame;

import javax.annotation.Nonnull;

public class BlockDungeonLamp extends BlockGame {
    public static final PropertyBool BROKEN = PropertyBool.create("broken");

    public BlockDungeonLamp() {
        setRegistryName("lootgames_dungeon_lamp");
        setTranslationKey("lootgames.dungeon_lamp");
        setDefaultState(getDefaultState().withProperty(BROKEN, false));
        setLightLevel(1.0F);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(BROKEN) ? 1 : 0;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        list.add(new ItemStack(this, 1, 0));
        list.add(new ItemStack(this, 1, 1));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BROKEN, meta == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BROKEN) ? 1 : 0;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BROKEN);
    }
}
