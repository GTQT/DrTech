package com.drppp.drtech.common.Items.Behavior;



import appeng.api.AEApi;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseTileBlock;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;

public class BluePrintBehavior implements IItemBehaviour, ItemUIFactory {

    public static final BluePrintBehavior INSTANCE = new BluePrintBehavior();

    protected BluePrintBehavior() {/**/}

    @Override
    public ModularUI createUI(PlayerInventoryHolder playerInventoryHolder, EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 120)
                .image(10, 8, 156, 50, GuiTextures.DISPLAY)
                .dynamicLabel(15, 13, () -> {
                    int x = 0;
                    int y = 0;
                    int z = 0;
                    if (getPos(playerInventoryHolder.getCurrentItem()) != null) {
                        BlockPos[] blockPos = getPos(playerInventoryHolder.getCurrentItem());
                        x = 1 + blockPos[1].getX() - blockPos[0].getX();
                        y = 1 + blockPos[1].getY() - blockPos[0].getY();
                        z = 1 + blockPos[1].getZ() - blockPos[0].getZ();
                    }
                    return I18n.format("metaitem.debug.structure_writer.structural_scale", x, y, z);
                }, 0xFAF9F6)
                .widget(new ClickButtonWidget(10, 68, 77, 20, "保存到NBT", clickData -> exportLog(playerInventoryHolder)))
                .widget(new ClickButtonWidget(90, 68, 77, 20, "蓝图绘制", clickData -> blueprintdraw(playerInventoryHolder)))
                .build(playerInventoryHolder, entityPlayer);
    }

    private void exportLog(PlayerInventoryHolder playerInventoryHolder) {
        if (getPos(playerInventoryHolder.getCurrentItem()) != null && !playerInventoryHolder.player.world.isRemote ) {
            var list = getPos(playerInventoryHolder.getCurrentItem());
            if(list.length>1)
            {
                NBTTagCompound tag = saveStructure(playerInventoryHolder.player.world,list[0],list[1]);
                playerInventoryHolder.getCurrentItem().setTagInfo("SaveBlocks",tag);
            }
        }
    }
    private void blueprintdraw(PlayerInventoryHolder playerInventoryHolder)
    {
        EntityPlayer player = playerInventoryHolder.player;
        var s = playerInventoryHolder.getCurrentItem();
        if(!player.world.isRemote && s.getTagCompound().hasKey("SaveBlocks"))
        {
            loadStructure(player,player.world,player.getPosition(),s.getTagCompound().getCompoundTag("SaveBlocks"));
        }
    }
    public NBTTagCompound saveStructure(World world, BlockPos pos1, BlockPos pos2) {
        NBTTagCompound nbt = new NBTTagCompound();
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        NBTTagList blockList = new NBTTagList();
        NBTTagList tileEntityList = new NBTTagList();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    TileEntity tileEntity = world.getTileEntity(currentPos);
                    IBlockState state = world.getBlockState(currentPos);

                    if (tileEntity != null && tileEntity instanceof IGregTechTileEntity)
                    {
                        MetaTileEntity sampleMetaTileEntity = GTUtility.getMetaTileEntity(world,currentPos);
                        if (sampleMetaTileEntity != null)
                        {
                            NBTTagCompound tileNBT = new NBTTagCompound();
                            tileNBT.setInteger("x", x - minX);
                            tileNBT.setInteger("y", y - minY);
                            tileNBT.setInteger("z", z - minZ);
                            NBTTagCompound tag = new NBTTagCompound();
                            sampleMetaTileEntity.getStackForm().writeToNBT(tag);
                            tileNBT.setTag("MachineItem",tag);
                            tileNBT.setInteger("MachineFacing",sampleMetaTileEntity.getFrontFacing().getIndex());
                            tag = new NBTTagCompound();
                            sampleMetaTileEntity.writeToNBT(tag);
                            tileNBT.setTag("MetaTile",tag);
                            tileEntityList.appendTag(tileNBT);
                        }
                    }
                    else
                    {
                        NBTTagCompound blockNBT = new NBTTagCompound();
                        blockNBT.setInteger("x", x - minX);
                        blockNBT.setInteger("y", y - minY);
                        blockNBT.setInteger("z", z - minZ);
                        blockNBT.setString("block", state.getBlock().getRegistryName().toString());
                        blockNBT.setInteger("meta", state.getBlock().getMetaFromState(state));
                        ItemStack iss = new ItemStack(ItemBlock.getItemFromBlock(state.getBlock()),1, state.getBlock().getMetaFromState(state));

                        NBTTagCompound tagg=  new NBTTagCompound();
                        iss.writeToNBT(tagg);
                        blockNBT.setTag("BlockItem",tagg);
                        if(tileEntity instanceof TileEntityMaterialPipeBase)
                        {
                            final var pipe = (TileEntityMaterialPipeBase)tileEntity;
                            BlockMaterialPipe pipeblock = (BlockMaterialPipe)state.getBlock();
                            Material  ma = pipe.getPipeMaterial();
                            ItemStack is = pipeblock.getItem(ma);
                            NBTTagCompound tag = new NBTTagCompound();
                            is.writeToNBT(tag);
                            blockNBT.setTag("PipeItem",tag);
                        }
                        blockList.appendTag(blockNBT);
                    }
                }
            }
        }

        nbt.setTag("blocks", blockList);
        nbt.setTag("GtEntities", tileEntityList);
        return nbt;
    }

    public void loadStructure(EntityPlayer player,World world, BlockPos targetPos, NBTTagCompound nbt) {
        NBTTagList blockList = nbt.getTagList("blocks", 10);
        NBTTagList tileEntityList = nbt.getTagList("GtEntities", 10);

        for (int i = 0; i < blockList.tagCount(); i++) {
            NBTTagCompound blockNBT = blockList.getCompoundTagAt(i);
            int x = blockNBT.getInteger("x") + targetPos.getX();
            int y = blockNBT.getInteger("y") + targetPos.getY();
            int z = blockNBT.getInteger("z") + targetPos.getZ();
            BlockPos pos = new BlockPos(x, y, z);
            if(blockNBT.hasKey("PipeItem"))
            {
                ItemStack itemStack = new ItemStack(blockNBT.getCompoundTag("PipeItem"));
                if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlock) {
                    ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
                    Block block = itemBlock.getBlock();
                    IBlockState state = block.getStateFromMeta(itemBlock.getMetadata(itemStack.getMetadata()));
                    itemBlock.placeBlockAt(itemStack, player, world, pos, EnumFacing.NORTH, x, y, z, state);
                }
            }
           else
            {
//                Block block = Block.getBlockFromName(blockNBT.getString("block"));
//                IBlockState state = block.getStateFromMeta(blockNBT.getInteger("meta"));
//                world.setBlockState(pos, state, 2);
                ItemStack itemStack = new ItemStack(blockNBT.getCompoundTag("BlockItem"));
                if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlock) {
                    ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
                    Block block = itemBlock.getBlock();
                    IBlockState state = block.getStateFromMeta(itemBlock.getMetadata(itemStack.getMetadata()));
                    itemBlock.placeBlockAt(itemStack, player, world, pos, EnumFacing.NORTH, x, y, z, state);
                }
            }
        }

        for (int i = 0; i < tileEntityList.tagCount(); i++) {
            NBTTagCompound tileNBT = tileEntityList.getCompoundTagAt(i);
            int x = tileNBT.getInteger("x") + targetPos.getX();
            int y = tileNBT.getInteger("y") + targetPos.getY();
            int z = tileNBT.getInteger("z") + targetPos.getZ();
            BlockPos pos = new BlockPos(x, y, z);
            ItemStack machine = new ItemStack(tileNBT.getCompoundTag("MachineItem"));
            if(GTUtility.getMetaTileEntity(machine) !=null)
            {
                ItemBlock itemBlock = (ItemBlock) machine.getItem();
                IBlockState state = itemBlock.getBlock().getStateFromMeta(itemBlock.getMetadata(machine.getMetadata()));
                world.setBlockState(pos, state);
                EnumFacing facing = EnumFacing.byIndex(tileNBT.getInteger("MachineFacing"));
                TileEntity holder = world.getTileEntity(pos);
                if (holder instanceof IGregTechTileEntity) {
                    MetaTileEntity sampleMetaTileEntity = GregTechAPI.MTE_REGISTRY
                            .getObjectById(machine.getItemDamage());
                    if (sampleMetaTileEntity != null) {
                        MetaTileEntity metaTileEntity = ((IGregTechTileEntity) holder)
                                .setMetaTileEntity(sampleMetaTileEntity);
                        metaTileEntity.onPlacement();
                        if (machine.getTagCompound() != null) {
                            metaTileEntity.initFromItemStackData(machine.getTagCompound());
                        }
                        metaTileEntity.setFrontFacing(facing);
                        metaTileEntity.readFromNBT(tileNBT.getCompoundTag("MetaTile"));
                    }


                }
            }
        }
    }

    public static boolean isItemStructureWriter(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (stack.getItem() instanceof MetaItem<?> metaItem) {
            MetaItem<?>.MetaValueItem valueItem = metaItem.getItem(stack);
            return valueItem != null && valueItem.getBehaviours().contains(INSTANCE);
        }
        return false;
    }

    public static BlockPos[] getPos(ItemStack stack) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("blueprint");
        if (!tag.hasKey("minX")) return null;
        return new BlockPos[]{
                new BlockPos(tag.getInteger("minX"), tag.getInteger("minY"), tag.getInteger("minZ")),
                new BlockPos(tag.getInteger("maxX"), tag.getInteger("maxY"), tag.getInteger("maxZ"))
        };
    }

    public static void addPos(ItemStack stack, BlockPos pos) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("blueprint");
        if (!tag.hasKey("minX") || tag.getInteger("minX") > pos.getX()) {
            tag.setInteger("minX", pos.getX());
        }
        if (!tag.hasKey("maxX") || tag.getInteger("maxX") < pos.getX()) {
            tag.setInteger("maxX", pos.getX());
        }

        if (!tag.hasKey("minY") || tag.getInteger("minY") > pos.getY()) {
            tag.setInteger("minY", pos.getY());
        }
        if (!tag.hasKey("maxY") || tag.getInteger("maxY") < pos.getY()) {
            tag.setInteger("maxY", pos.getY());
        }

        if (!tag.hasKey("minZ") || tag.getInteger("minZ") > pos.getZ()) {
            tag.setInteger("minZ", pos.getZ());
        }
        if (!tag.hasKey("maxZ") || tag.getInteger("maxZ") < pos.getZ()) {
            tag.setInteger("maxZ", pos.getZ());
        }
    }

    public static void removePos(ItemStack stack) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("blueprint");
        tag.removeTag("minX");
        tag.removeTag("maxX");
        tag.removeTag("minY");
        tag.removeTag("maxY");
        tag.removeTag("minZ");
        tag.removeTag("maxZ");
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.isSneaking()) {
            addPos(stack, pos);
        } else {
            removePos(stack);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            removePos(stack);
        } else {
            if (!worldIn.isRemote) {
                PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
                holder.openUI();
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
