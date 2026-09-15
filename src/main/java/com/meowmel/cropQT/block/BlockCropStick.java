package com.meowmel.cropQT.block;

import com.drppp.drtech.Tags;
import com.meowmel.cropQT.tile.TileCropStick;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.SoilRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.item.ItemCropSeed;
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
import net.minecraftforge.oredict.OreDictionary;

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

    public static final Map<Item, String> VANILLA_SEED_MAP = new HashMap<>();

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
        VANILLA_SEED_MAP.put(Items.CHORUS_FRUIT, "chorus_crop");
        VANILLA_SEED_MAP.put(Item.getItemFromBlock(Blocks.YELLOW_FLOWER), "dandelion");
        VANILLA_SEED_MAP.put(Item.getItemFromBlock(Blocks.RED_FLOWER), "rose");
        VANILLA_SEED_MAP.put(Item.getItemFromBlock(Blocks.SAPLING), "bonsai");
        VANILLA_SEED_MAP.put(Item.getItemFromBlock(Blocks.CACTUS), "cactus");
        VANILLA_SEED_MAP.put(Item.getItemFromBlock(Blocks.BROWN_MUSHROOM), "brown_mushroom");
        VANILLA_SEED_MAP.put(Item.getItemFromBlock(Blocks.RED_MUSHROOM), "red_mushroom");
        VANILLA_SEED_MAP.put(Item.getItemFromBlock(Blocks.WATERLILY), "lotus_leaf");
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

        // 除草：只认矿辞，不认具体物品——GT 的电动剪刀、各种 mod 的剪刀都在 toolShears 里
        if (isWeedingTool(held) && tile.isWeedPlant()) {
            tile.destroyCrop();
            if (!player.isCreative()) {
                held.damageItem(1, player);
            }
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "杂草已清除!"));
            return true;
        }
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
                return true;
            }
            return false;
        }
        if (held.isEmpty()) {
            if (tile.hasCrop() && tile.isMature()) return doHarvest(world, pos, tile, player);
        }
        return false;
    }

    /**
     * 手上的东西能不能用来除草。
     *
     * <p>只看矿辞 {@code toolShears}，不看具体物品 —— 这样 GT 的电动剪刀、
     * 各种 mod 的剪刀、以及别的模组往这个矿辞里登记的东西都能用，不用为每种工具写一遍。
     */
    public static boolean isWeedingTool(@Nullable ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        int shearId = OreDictionary.getOreID("toolShears");
        if (shearId < 0) {
            return false;
        }
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (id == shearId) {
                return true;
            }
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
        if (!tile.plantCrop(cropId, stats)) {
            player.sendMessage(new TextComponentString(
                    TextFormatting.RED + "土壤不合作物要求，种不下去!（需要 "
                            + describeSoil(tile, cropId) + "）"));
            return false;
        }
        if (!player.isCreative()) item.shrink(1);
        CropType type = CropRegistry.get(cropId);
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "种植了 " + (type != null ? type.getDisplayName() : cropId)));
        return true;
    }

    /** 给玩家看的「这株作物要什么土壤」。取不到需求时退回一句笼统提示。 */
    private String describeSoil(TileCropStick tile, String cropId) {
        CropType type = CropRegistry.get(cropId);
        if (type == null || type.getSoilTypes() == null) {
            return "不挑土壤";
        }
        String name = type.getSoilTypes().getName();
        net.minecraft.util.text.ITextComponent localized =
                new net.minecraft.util.text.TextComponentTranslation("cropqt.soil." + name);
        String text = localized.getUnformattedText();
        // 没配 lang key 时 getUnformattedText 会把 key 原样返回，退到内部名
        return text.startsWith("cropqt.soil.") ? name : text;
    }

    /**
     * 收获 - 使用TileCropStick的新getHarvestDrops()方法
     */
    private boolean doHarvest(World world, BlockPos pos, TileCropStick tile, EntityPlayer player) {
        CropType type = tile.getCropType();
        if (type == null) return false;

        List<ItemStack> drops = tile.getHarvestDrops();
        for (ItemStack drop : drops) {
            spawnAsEntity(world, pos.up(), drop);
        }

        tile.harvest();
        return true;
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

    @Override public List<ItemStack> getDrops(IBlockAccess w, BlockPos p, IBlockState s, int f) {
        List<ItemStack> d = new ArrayList<>(); d.add(new ItemStack(this)); return d;
    }

    /**
     * 能不能架在这里：只要脚下那块是<b>已登记的土壤</b>就行。
     *
     * <p>规则照 CropsNH——不写死方块列表。这样 {@link com.meowmel.cropQT.api.SoilTypes}
     * 里加了新土壤，作物架就自动能架上去，不需要回来改这里。
     */
    @Override public boolean canPlaceBlockAt(World w, BlockPos p) {
        return SoilRegistry.getSoilFor(w.getBlockState(p.down())) != null;
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
