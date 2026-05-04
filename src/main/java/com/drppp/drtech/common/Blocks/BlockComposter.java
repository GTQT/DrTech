package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityComposter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BlockComposter extends Block {
    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 8);
    private static final AxisAlignedBB OUTLINE = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB FLOOR_BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 2.0D / 16.0D, 1.0D);
    private static final AxisAlignedBB WEST_WALL = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 2.0D / 16.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB EAST_WALL = new AxisAlignedBB(14.0D / 16.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB NORTH_WALL = new AxisAlignedBB(2.0D / 16.0D, 0.0D, 0.0D, 14.0D / 16.0D, 1.0D, 2.0D / 16.0D);
    private static final AxisAlignedBB SOUTH_WALL = new AxisAlignedBB(2.0D / 16.0D, 0.0D, 14.0D / 16.0D, 14.0D / 16.0D, 1.0D, 1.0D);
    private static final Map<Item, Float> COMPOSTABLES = new HashMap<>();
    private static final IBehaviorDispenseItem COMPOSTER_DISPENSE_BEHAVIOR = new BehaviorDefaultDispenseItem() {
        @Override
        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            World world = source.getWorld();
            EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
            BlockPos targetPos = source.getBlockPos().offset(facing);
            IBlockState targetState = world.getBlockState(targetPos);
            if (facing == EnumFacing.DOWN && targetState.getBlock() instanceof BlockComposter) {
                return ((BlockComposter) targetState.getBlock()).insertForAutomation(world, targetPos, targetState, stack, false);
            }
            return super.dispenseStack(source, stack);
        }
    };

    static {
        register(0.30F, Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
        register(0.30F, Blocks.SAPLING, Blocks.LEAVES, Blocks.LEAVES2, Blocks.TALLGRASS);

        register(0.50F, Items.REEDS, Items.MELON);
        register(0.50F, Blocks.CACTUS, Blocks.VINE);

        register(0.65F, Items.APPLE, Items.BEETROOT, Items.CARROT, Items.NETHER_WART, Items.POTATO, Items.WHEAT);
        register(0.65F, Blocks.YELLOW_FLOWER, Blocks.RED_FLOWER, Blocks.DOUBLE_PLANT, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.WATERLILY, Blocks.MELON_BLOCK, Blocks.PUMPKIN);

        register(0.85F, Items.BAKED_POTATO, Items.BREAD, Items.COOKIE);
        register(0.85F, Blocks.HAY_BLOCK, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.RED_MUSHROOM_BLOCK, Blocks.NETHER_WART_BLOCK);

        register(1.00F, Items.CAKE, Items.PUMPKIN_PIE);
    }

    public BlockComposter() {
        super(Material.WOOD);
        setRegistryName(Tags.MODID, "composter");
        setTranslationKey(Tags.MODID + ".composter");
        setCreativeTab(DrTechMain.DrTechTab);
        setHardness(0.6F);
        setResistance(0.6F);
        setSoundType(SoundType.WOOD);
        setHarvestLevel("axe", 0);
        setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, 0));
    }

    private static void register(float chance, Item... items) {
        for (Item item : items) {
            COMPOSTABLES.put(item, chance);
        }
    }

    private static void register(float chance, Block... blocks) {
        for (Block block : blocks) {
            Item item = Item.getItemFromBlock(block);
            if (item != Items.AIR) {
                COMPOSTABLES.put(item, chance);
            }
        }
    }

    public static void registerDispenseBehaviors() {
        for (Item item : COMPOSTABLES.keySet()) {
            BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(item, COMPOSTER_DISPENSE_BEHAVIOR);
        }
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.DYE, COMPOSTER_DISPENSE_BEHAVIOR);
    }

    public static Collection<Item> getCompostableItems() {
        return Collections.unmodifiableSet(COMPOSTABLES.keySet());
    }

    @Nullable
    public static Float getCompostChance(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (stack.getItem() == Items.DYE && stack.getMetadata() == 3) {
            return 0.65F;
        }
        return COMPOSTABLES.get(stack.getItem());
    }

    public boolean canAcceptForCompost(IBlockState state, ItemStack stack) {
        return state.getValue(LEVEL) < 7 && getCompostChance(stack) != null;
    }

    public ItemStack insertForAutomation(World worldIn, BlockPos pos, IBlockState state, ItemStack stack, boolean simulate) {
        if (!canAcceptForCompost(state, stack)) {
            return stack;
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(1);
        if (!simulate) {
            Float chance = getCompostChance(stack);
            if (chance != null) {
                attemptCompost(worldIn, pos, state, chance);
            }
        }
        return remainder;
    }

    public ItemStack extractProduce(World worldIn, BlockPos pos, IBlockState state, boolean simulate) {
        if (state.getValue(LEVEL) != 8) {
            return ItemStack.EMPTY;
        }
        if (!simulate) {
            setComposterLevel(worldIn, pos, state.withProperty(LEVEL, 0));
        }
        return new ItemStack(Items.DYE, 1, 15);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        int level = state.getValue(LEVEL);
        ItemStack heldItem = playerIn.getHeldItem(hand);

        if (level == 8) {
            if (!worldIn.isRemote) {
                ItemStack boneMeal = new ItemStack(Items.DYE, 1, 15);
                if (!playerIn.inventory.addItemStackToInventory(boneMeal)) {
                    spawnAsEntity(worldIn, pos.up(), boneMeal);
                }
                setComposterLevel(worldIn, pos, state.withProperty(LEVEL, 0));
            }
            return true;
        }

        if (level == 7) {
            return true;
        }

        Float chance = getCompostChance(heldItem);
        if (chance == null) {
            return false;
        }

        if (!worldIn.isRemote) {
            if (!playerIn.capabilities.isCreativeMode) {
                heldItem.shrink(1);
            }
            attemptCompost(worldIn, pos, state, chance);
        }
        return true;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (state.getValue(LEVEL) == 7) {
            setComposterLevel(worldIn, pos, state.withProperty(LEVEL, 8));
        }
    }

    @Override
    public int tickRate(World worldIn) {
        return 20;
    }

    private void attemptCompost(World worldIn, BlockPos pos, IBlockState state, float chance) {
        if (worldIn.rand.nextFloat() >= chance) {
            return;
        }
        int nextLevel = state.getValue(LEVEL) + 1;
        IBlockState nextState = state.withProperty(LEVEL, nextLevel);
        setComposterLevel(worldIn, pos, nextState);
        if (nextLevel == 7) {
            worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
        }
    }

    private void setComposterLevel(World world, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state, 3);
        world.updateComparatorOutputLevel(pos, this);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityComposter();
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable net.minecraft.entity.Entity entityIn, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, FLOOR_BOX);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_WALL);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_WALL);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_WALL);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_WALL);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return OUTLINE;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        return blockState.getValue(LEVEL);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LEVEL);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(LEVEL, Math.max(0, Math.min(8, meta)));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LEVEL);
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 5;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 5;
    }
}
