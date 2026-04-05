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

    private static final AxisAlignedBB CROP_AABB = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 0.9375, 0.9375);
    private static final AxisAlignedBB STICK_AABB = new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.75, 0.875);

    // 原版物品 -> 作物ID 映射表
    private static final Map<Item, String> VANILLA_SEED_MAP = new HashMap<>();

    public BlockCropStick() {
        super(Material.PLANTS);
        setTranslationKey(Tags.MODID + ".crop_stick");
        setRegistryName(Tags.MODID, "crop_stick");
        setHardness(0.2f);
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState()
                .withProperty(DOUBLE, false)
                .withProperty(HAS_CROP, false)
                .withProperty(RENDER_STAGE, 0));
    }

    /**
     * 初始化原版种子映射 - 必须在CropRegistry.registerDefaults()之后调用
     */
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
    }

    public static void registerVanillaSeed(Item item, String cropId) {
        VANILLA_SEED_MAP.put(item, cropId);
    }

    // ==================== 核心交互 ====================

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        // 关键修复: 客户端只返回是否应该swing手臂
        // 所有实际逻辑在服务端执行
        if (world.isRemote) {
            return true; // 客户端总是返回true让手臂swing
        }

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileCropStick)) {
            return false;
        }

        TileCropStick tile = (TileCropStick) te;
        ItemStack heldItem = player.getHeldItem(hand);

        // 1. 手持种子袋
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof ItemCropSeed) {
            String cropId = ItemCropSeed.getCropId(heldItem);
            CropStats stats = ItemCropSeed.getCropStats(heldItem);
            return tryPlant(tile, player, heldItem, cropId, stats);
        }

        // 2. 手持原版种子
        if (!heldItem.isEmpty()) {
            String cropId = VANILLA_SEED_MAP.get(heldItem.getItem());
            if (cropId != null) {
                return tryPlant(tile, player, heldItem, cropId, new CropStats(1, 1, 1));
            }
        }

        // 3. 手持作物架方块 -> 加第二层
        if (!heldItem.isEmpty() && heldItem.getItem() == Item.getItemFromBlock(this)) {
            if (!tile.hasCrop() && !tile.isDoubleCropStick()) {
                tile.setDoubleCropStick(true);
                if (!player.isCreative()) heldItem.shrink(1);
                player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "已进入杂交模式"));
                return true;
            }
            return false;
        }

        // 4. 空手
        if (heldItem.isEmpty()) {
            // 收获成熟作物
            if (tile.hasCrop() && tile.isMature()) {
                return doHarvest(world, pos, tile, player);
            }
            // 蹲下查看信息
            if (player.isSneaking()) {
                showInfo(tile, player);
                return true;
            }
        }

        return false;
    }

    /**
     * 统一种植逻辑 - 种子袋和原版种子共用
     */
    private boolean tryPlant(TileCropStick tile, EntityPlayer player, ItemStack seedItem,
                             String cropId, CropStats stats) {
        if (tile.isDoubleCropStick()) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "双层作物架用于杂交，不能直接种植!"));
            return false;
        }
        if (tile.hasCrop()) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "已经有作物了!"));
            return false;
        }
        if (cropId == null || cropId.isEmpty()) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "无效的种子!"));
            return false;
        }
        if (!CropRegistry.exists(cropId)) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "未知的作物类型: " + cropId));
            return false;
        }

        // 执行种植
        tile.plantCrop(cropId, stats);

        if (!player.isCreative()) {
            seedItem.shrink(1);
        }

        CropType type = CropRegistry.get(cropId);
        String name = type != null ? type.getDisplayName() : cropId;
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "种植了 " + name));
        return true;
    }

    private boolean doHarvest(World world, BlockPos pos, TileCropStick tile, EntityPlayer player) {
        CropType type = tile.getCropType();
        if (type == null) return false;

        for (ItemStack drop : type.getDrops()) {
            ItemStack copy = drop.copy();
            int bonus = tile.getStats().getYieldBonus(world.rand);
            copy.setCount(copy.getCount() + bonus);
            spawnAsEntity(world, pos.up(), copy);
        }

        tile.harvest();
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "收获了 " + type.getDisplayName() + "!"));
        return true;
    }

    private void showInfo(TileCropStick tile, EntityPlayer player) {
        if (!tile.hasCrop()) {
            String msg = tile.isDoubleCropStick() ? "杂交模式 - 等待相邻成熟作物..." : "空的作物架";
            player.sendMessage(new TextComponentString(TextFormatting.GRAY + msg));
            return;
        }

        CropType type = tile.getCropType();
        CropStats stats = tile.getStats();
        String name = type != null ? type.getDisplayName() : tile.getCropId();
        int tier = type != null ? type.getTier() : 0;
        int maxStage = type != null ? type.getMaxGrowthStage() : 0;

        player.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== 作物信息 ==="));
        player.sendMessage(new TextComponentString(
                TextFormatting.GREEN + "类型: " + TextFormatting.WHITE + name +
                        TextFormatting.GRAY + " (Tier " + tier + ")"));
        player.sendMessage(new TextComponentString(
                TextFormatting.GREEN + "阶段: " + TextFormatting.WHITE + tile.getGrowthStage() + "/" + maxStage));
        player.sendMessage(new TextComponentString(
                TextFormatting.GREEN + "属性: " +
                        TextFormatting.RED + "Gr:" + stats.getGrowth() + " " +
                        TextFormatting.YELLOW + "Ga:" + stats.getGain() + " " +
                        TextFormatting.BLUE + "Re:" + stats.getResistance()));
        if (tile.isWeedPlant()) {
            player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "⚠ 杂草!"));
        }
    }

    // ==================== 破坏/掉落 ====================

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileCropStick) {
            TileCropStick tile = (TileCropStick) te;
            if (tile.hasCrop() && !tile.isWeedPlant()) {
                ItemStack seedBag = ItemCropSeed.createSeedBag(tile.getCropId(), tile.getStats());
                if (!seedBag.isEmpty()) {
                    spawnAsEntity(world, pos, seedBag);
                }
            }
            if (tile.isDoubleCropStick() && !tile.hasCrop()) {
                spawnAsEntity(world, pos, new ItemStack(this));
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(this));
        return drops;
    }

    // ==================== 放置条件 ====================

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        Block blockBelow = world.getBlockState(pos.down()).getBlock();
        return blockBelow == Blocks.FARMLAND
                || blockBelow == Blocks.DIRT
                || blockBelow == Blocks.GRASS
                || blockBelow == Blocks.SOUL_SAND;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!canPlaceBlockAt(world, pos)) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        if(world.isRemote)
            return;
        var te = world.getTileEntity(pos);
        if(te!=null && te instanceof TileCropStick)
            ((TileCropStick)te).onBlockRandomTick();
    }

    // ==================== 方块状态 ====================

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DOUBLE, HAS_CROP, RENDER_STAGE);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileCropStick) {
            TileCropStick tile = (TileCropStick) te;
            return state
                    .withProperty(DOUBLE, tile.isDoubleCropStick())
                    .withProperty(HAS_CROP, tile.hasCrop())
                    .withProperty(RENDER_STAGE, Math.min(tile.getGrowthStage(), 7));
        }
        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) { return 0; }

    @Override
    public IBlockState getStateFromMeta(int meta) { return getDefaultState(); }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileCropStick();
    }

    // ==================== 渲染 ====================

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }



    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        TileEntity te = source.getTileEntity(pos);
        if (te instanceof TileCropStick && ((TileCropStick) te).hasCrop()) {
            return CROP_AABB;
        }
        return STICK_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
    }
}

