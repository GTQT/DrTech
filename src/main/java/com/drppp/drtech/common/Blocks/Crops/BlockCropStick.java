package com.drppp.drtech.common.Blocks.Crops;

import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileCropStick;
import com.drppp.drtech.common.Items.ItemCropSeed;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 作物架方块
 *
 * 交互逻辑:
 * - 空手右键空架子: 放置第二层(进入杂交模式)
 * - 手持种子袋右键单层架子: 种植作物
 * - 空手右键成熟作物: 收获(保留作物架和根)
 * - 手持扳手/剪刀右键: 查看作物信息
 * - 左键/破坏: 完全移除
 */

public class BlockCropStick extends Block implements ITileEntityProvider {

    public static final PropertyBool DOUBLE = PropertyBool.create("double");
    public static final PropertyBool HAS_CROP = PropertyBool.create("has_crop");
    public static final PropertyInteger RENDER_STAGE = PropertyInteger.create("render_stage", 0, 7);

    private static final AxisAlignedBB CROP_AABB = new AxisAlignedBB(0.0625,0,0.0625,0.9375,0.9375,0.9375);
    private static final AxisAlignedBB STICK_AABB = new AxisAlignedBB(0.125,0,0.125,0.875,0.75,0.875);

    private static final Map<Item, String> VANILLA_SEED_MAP = new HashMap<>();

    public BlockCropStick() {
        super(Material.PLANTS);
        setTranslationKey(Tags.MODID + ".crop_stick");
        setRegistryName(Tags.MODID, "crop_stick");
        setHardness(0.2f);
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState()
                .withProperty(DOUBLE, false).withProperty(HAS_CROP, false).withProperty(RENDER_STAGE, 0));
    }

    public static void initVanillaSeedMap() {
        VANILLA_SEED_MAP.clear();
        VANILLA_SEED_MAP.put(Items.WHEAT_SEEDS, "wheat");
        VANILLA_SEED_MAP.put(Items.POTATO, "potato");
        VANILLA_SEED_MAP.put(Items.CARROT, "carrot");
        VANILLA_SEED_MAP.put(Items.MELON_SEEDS, "melon");
        VANILLA_SEED_MAP.put(Items.PUMPKIN_SEEDS, "pumpkin");
        VANILLA_SEED_MAP.put(Items.BEETROOT_SEEDS, "beetroot");
        VANILLA_SEED_MAP.put(Items.NETHER_WART, "nether_wart_crop");
        VANILLA_SEED_MAP.put(Items.REEDS, "reed");
        // 花卉种子: 通过花本身种植
        VANILLA_SEED_MAP.put(Item.getItemFromBlock(Blocks.YELLOW_FLOWER), "dandelion");
        VANILLA_SEED_MAP.put(Item.getItemFromBlock(Blocks.RED_FLOWER), "rose");
    }

    public static void registerVanillaSeed(Item item, String cropId) {
        VANILLA_SEED_MAP.put(item, cropId);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileCropStick)) return false;
        TileCropStick tile = (TileCropStick) te;
        ItemStack held = player.getHeldItem(hand);

        if (!held.isEmpty() && held.getItem() instanceof ItemCropSeed) {
            String id = ItemCropSeed.getCropId(held);
            return tryPlant(tile, player, held, id, ItemCropSeed.getCropStats(held));
        }
        if (!held.isEmpty()) {
            String id = VANILLA_SEED_MAP.get(held.getItem());
            if (id != null) return tryPlant(tile, player, held, id, new CropStats(1, 1, 1));
        }
        if (!held.isEmpty() && held.getItem() == Item.getItemFromBlock(this)) {
            if (!tile.hasCrop() && !tile.isDoubleCropStick()) {
                tile.setDoubleCropStick(true);
                if (!player.isCreative()) held.shrink(1);
                player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "已进入杂交模式"));
                return true;
            }
            return false;
        }
        if (held.isEmpty()) {
            if (tile.hasCrop() && tile.isMature()) return doHarvest(world, pos, tile, player);
            if (player.isSneaking()) { showInfo(tile, player); return true; }
        }
        return false;
    }

    private boolean tryPlant(TileCropStick tile, EntityPlayer player, ItemStack item, String cropId, CropStats stats) {
        if (tile.isDoubleCropStick()) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "双层作物架用于杂交，不能直接种植!"));
            return false;
        }
        if (tile.hasCrop()) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "已经有作物了!"));
            return false;
        }
        if (cropId == null || cropId.isEmpty() || !CropRegistry.exists(cropId)) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "未知的作物类型!"));
            return false;
        }
        tile.plantCrop(cropId, stats);
        if (!player.isCreative()) item.shrink(1);
        CropType type = CropRegistry.get(cropId);
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "种植了 " + (type != null ? type.getDisplayName() : cropId)));
        return true;
    }

    private boolean doHarvest(World world, BlockPos pos, TileCropStick tile, EntityPlayer player) {
        CropType type = tile.getCropType();
        if (type == null) return false;
        for (ItemStack drop : type.getDrops()) {
            ItemStack copy = drop.copy();
            copy.setCount(copy.getCount() + tile.getStats().getYieldBonus(world.rand));
            spawnAsEntity(world, pos.up(), copy);
        }
        tile.harvest();
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "收获了 " + type.getDisplayName() + "!"));
        return true;
    }

    private void showInfo(TileCropStick tile, EntityPlayer player) {
        if (!tile.hasCrop()) {
            player.sendMessage(new TextComponentString(TextFormatting.GRAY + (tile.isDoubleCropStick() ? "杂交模式 - 等待成熟作物..." : "空的作物架")));
            return;
        }
        CropType type = tile.getCropType();
        CropStats s = tile.getStats();
        String name = type != null ? type.getDisplayName() : tile.getCropId();
        int tier = type != null ? type.getTier() : 0;
        int max = type != null ? type.getMaxGrowthStage() : 0;
        player.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== 作物信息 ==="));
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "类型: " + TextFormatting.WHITE + name + TextFormatting.GRAY + " (Tier " + tier + ")"));
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "阶段: " + TextFormatting.WHITE + tile.getGrowthStage() + "/" + max + TextFormatting.GRAY + " (进度:" + tile.getGrowthProgress() + ")"));
        player.sendMessage(new TextComponentString(TextFormatting.RED + "Gr:" + s.getGrowth() + " " + TextFormatting.YELLOW + "Ga:" + s.getGain() + " " + TextFormatting.BLUE + "Re:" + s.getResistance()));
        player.sendMessage(new TextComponentString(TextFormatting.AQUA + "杂交率: " + s.getCrossBreedChance() + "%"));
        if (tile.isWeedPlant()) player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "⚠ 杂草!"));
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileCropStick) {
            TileCropStick tile = (TileCropStick) te;
            if (tile.hasCrop() && !tile.isWeedPlant()) {
                ItemStack bag = ItemCropSeed.createSeedBag(tile.getCropId(), tile.getStats());
                if (!bag.isEmpty()) spawnAsEntity(world, pos, bag);
            }
            if (tile.isDoubleCropStick() && !tile.hasCrop()) spawnAsEntity(world, pos, new ItemStack(this));
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess w, BlockPos p, IBlockState s, int f) {
        List<ItemStack> d = new ArrayList<>(); d.add(new ItemStack(this)); return d;
    }

    @Override public boolean canPlaceBlockAt(World w, BlockPos p) {
        Block b = w.getBlockState(p.down()).getBlock();
        return b == Blocks.FARMLAND || b == Blocks.DIRT || b == Blocks.GRASS ;
    }

    @Override public void neighborChanged(IBlockState s, World w, BlockPos p, Block b, BlockPos f) {
        if (!canPlaceBlockAt(w, p)) { dropBlockAsItem(w, p, s, 0); w.setBlockToAir(p); }
    }

    @Override public void randomTick(World w, BlockPos p, IBlockState s, Random r) {}

    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, DOUBLE, HAS_CROP, RENDER_STAGE); }

    @Override public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileCropStick) {
            TileCropStick t = (TileCropStick) te;
            return state.withProperty(DOUBLE, t.isDoubleCropStick()).withProperty(HAS_CROP, t.hasCrop()).withProperty(RENDER_STAGE, Math.min(t.getGrowthStage(), 7));
        }
        return state;
    }

    @Override public int getMetaFromState(IBlockState s) { return 0; }
    @Override public IBlockState getStateFromMeta(int m) { return getDefaultState(); }
    @Nullable @Override public TileEntity createNewTileEntity(World w, int m) { return new TileCropStick(); }
    @Override public boolean isOpaqueCube(IBlockState s) { return false; }
    @Override public boolean isFullCube(IBlockState s) { return false; }
    @Override public EnumBlockRenderType getRenderType(IBlockState s) { return EnumBlockRenderType.MODEL; }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override public AxisAlignedBB getBoundingBox(IBlockState s, IBlockAccess w, BlockPos p) {
        TileEntity te = w.getTileEntity(p);
        return (te instanceof TileCropStick && ((TileCropStick) te).hasCrop()) ? CROP_AABB : STICK_AABB;
    }

    @Nullable @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState s, IBlockAccess w, BlockPos p) { return NULL_AABB; }
}

