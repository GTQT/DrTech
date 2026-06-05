package com.drppp.drtech.lootgames.block;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import com.drppp.drtech.lootgames.api.block.BlockGame;

import javax.annotation.Nonnull;

public class BlockDungeonBricks extends BlockGame {
    public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class);

    public BlockDungeonBricks() {
        setRegistryName("lootgames_dungeon_bricks");
        setTranslationKey("lootgames.dungeon_bricks");
        setLightLevel(0.0F);
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMeta();
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for (EnumType type : EnumType.values()) {
            list.add(new ItemStack(this, 1, type.getMeta()));
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMeta();
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT);
    }

    public enum EnumType implements IStringSerializable {
        DUNGEON_FLOOR(0, "dungeon_floor"),
        DUNGEON_FLOOR_CRACKED(1, "dungeon_floor_cracked"),
        DUNGEON_FLOOR_SHIELDED(2, "dungeon_floor_shielded"),
        DUNGEON_WALL(3, "dungeon_wall"),
        DUNGEON_WALL_CRACKED(4, "dungeon_wall_cracked"),
        DUNGEON_CEILING(5, "dungeon_ceiling"),
        DUNGEON_CEILING_CRACKED(6, "dungeon_ceiling_cracked");

        private static final EnumType[] META_LOOKUP = new EnumType[values().length];
        private final int meta;
        private final String name;

        EnumType(int meta, String name) {
            this.meta = meta;
            this.name = name;
        }

        public static EnumType byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
            return META_LOOKUP[meta];
        }

        static {
            for (EnumType type : values()) {
                META_LOOKUP[type.meta] = type;
            }
        }

        public int getMeta() {
            return meta;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
