package com.drppp.drtech.MetaTileEntities.muti.ecectric.standard;

import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.Blocks.MetaBlocks.MetaGlasses;
import com.drppp.drtech.Blocks.MetaBlocks.MetaGlasses1;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.Items.MetaItems.MyMetaItems;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.drppp.drtech.Load.DrtechReceipes.MOB_KILLER;


public class MetaTileEntityMobsKiller extends RecipeMapMultiblockController {

    public MetaTileEntityMobsKiller(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, MOB_KILLER);
        this.recipeMapWorkable = new MobKillerLogic(this, true);
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityMobsKiller(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "FGGGF", "FGGGF", "FGGGF", "FGGGF","FGGGF","XXXXX")
                .aisle("XXXXX", "G###G", "GAAAG", "GAAAG", "GAAAG","GAAAG","XXXXX")
                .aisle("XXXXX", "G###G", "GAAAG", "GAAAG", "GAAAG","GAAAG","XXXXX")
                .aisle("XXXXX", "G###G", "GAAAG", "GAAAG", "GAAAG","GAAAG","XXXXX")
                .aisle("XXSXX", "FGGGF", "FGGGF", "FGGGF", "FGGGF","FGGGF","XXXXX")
                .where('S', selfPredicate())
                .where('X',
                        states(getCasingState()).setMinGlobalLimited(10)
                                .or(autoAbilities(false, true, true, true, true, true, false))
                                .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                )
                .where('G', states(getCasingState3()))
                .where('F', frames(Materials.Steel))
                .where('A', air())
                .where('#', blocks(Blocks.END_ROD))
                .build();
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }


    private static IBlockState getCasingState3() {
        return BlocksInit.TRANSPARENT_CASING.getState(MetaGlasses.CasingType.TI_BORON_SILICATE_GLASS_BLOCK);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return gregtech.client.renderer.texture.Textures.SOLID_STEEL_CASING;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return false;
    }
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.mobskiller.tip.1"));
        tooltip.add(I18n.format("drtech.machine.mobskiller.tip.2"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentTranslation("",""));
    }

    protected class MobKillerLogic extends MultiblockRecipeLogic {

        public MobKillerLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
            super(tileEntity, hasPerfectOC);
        }
        @Override
        protected void outputRecipeOutputs() {
            getOutputList();
            GTTransferUtils.addItemsToItemHandler(this.getOutputInventory(), false, this.itemOutputs);
            GTTransferUtils.addFluidsToFluidHandler(this.getOutputTank(), false, this.fluidOutputs);
        }
        @Override
        public int getParallelLimit() {
            int tire = 1;
            for (int i = 0; i < GTValues.V.length; i++) {
                if(GTValues.V[i]==this.getMaxVoltage())
                    tire = i;
            }
            return tire*2;
        }
        private void getOutputList()
        {
            int level = 0,circuit = 0,flag=0;
            ItemStack sword = null;
            float attack=0;
            for (int i = 0; i < this.getInputInventory().getSlots(); i++) {
                ItemStack is = this.getInputInventory().getStackInSlot(i);
                if(is.getItem() instanceof ItemSword)
                {
                    level = getLevel(is);
                    flag = 1;
                    sword = is;
                }
                if(is.getItem() == MetaItems.INTEGRATED_CIRCUIT.getMetaItem() && is.getMetadata()== MetaItems.INTEGRATED_CIRCUIT.getMetaValue())
                {
                    NBTTagCompound compound = is.getTagCompound();
                    if(compound.hasKey("Configuration"))
                        circuit = compound.getInteger("Configuration");
                    else
                        circuit=0;
                }
            }
            if(flag==1)
            {
                switch (circuit)
                {
                    case 1:
                        getDrops(LootTableList.ENTITIES_ZOMBIE,level,sword);
                        break;
                    case 2:
                        getDrops(LootTableList.ENTITIES_SKELETON,level,sword);
                        break;
                    case 3:
                        getDrops(LootTableList.ENTITIES_CREEPER,level,sword);
                        break;
                    case 4:
                        getDrops(LootTableList.ENTITIES_WITCH,level,sword);
                        break;
                    case 5:
                        if(this.metaTileEntity.getWorld().getBiome(this.metaTileEntity.getPos())== Biomes.HELL)
                        getDrops(LootTableList.ENTITIES_BLAZE,level,sword);
                        break;
                    case 6:
                        if(this.metaTileEntity.getWorld().getBiome(this.metaTileEntity.getPos())== Biomes.HELL)
                        getDrops(LootTableList.ENTITIES_ZOMBIE_PIGMAN,level,sword);
                        break;
                    case 7:
                        if(this.metaTileEntity.getWorld().getBiome(this.metaTileEntity.getPos())== Biomes.HELL)
                        getDrops(LootTableList.ENTITIES_WITHER_SKELETON,level,sword,7);
                        break;
                    case 8:
                        if(this.metaTileEntity.getWorld().getBiome(this.metaTileEntity.getPos())== Biomes.HELL)
                        getDrops(LootTableList.ENTITIES_GHAST,level,sword);
                        break;
                    case 9:
                        if(this.metaTileEntity.getWorld().getBiome(this.metaTileEntity.getPos())== Biomes.SKY)
                        getDrops(LootTableList.ENTITIES_ENDERMAN,level,sword);
                        break;
                    case 10:
                        if(this.metaTileEntity.getWorld().getBiome(this.metaTileEntity.getPos())== Biomes.SWAMPLAND)
                            getDrops(LootTableList.ENTITIES_SLIME,level,sword);
                        break;
                    case 11:
                        getDrops(LootTableList.ENTITIES_COW,level,sword);
                        break;
                    case 12:
                        getDrops(LootTableList.ENTITIES_CHICKEN,level,sword);
                        break;
                    case 13:
                        getDrops(LootTableList.ENTITIES_SHEEP,level,sword);
                        break;
                    case 14:
                        getDrops(LootTableList.ENTITIES_PIG,level,sword);
                        break;

                }
            }
        }
        private void getDrops(ResourceLocation rl,int level,ItemStack sword)
        {
            List<ItemStack> allstacks = new ArrayList<>();
            for (int i = 0; i < this.getParallelLimit(); i++) {
                LootTable table = this.metaTileEntity.getWorld().getLootTableManager().getLootTableFromLocation(rl);
                LootContext ctx = new LootContext.Builder((WorldServer) this.metaTileEntity.getWorld()).build();
                List<ItemStack> stacks = table.generateLootForPools(this.metaTileEntity.getWorld().rand, ctx);

                allstacks.addAll(stacks);
                if(new Random().nextInt(21)==8)
                {
                    sword.setItemDamage(sword.getItemDamage()+1);
                }
            }

            GTTransferUtils.addItemsToItemHandler(this.getOutputInventory(), false, allstacks);
        }
        private void getDrops(ResourceLocation rl,int level,ItemStack sword,int flag)
        {
            List<ItemStack> allstacks = new ArrayList<>();
            for (int i = 0; i < this.getParallelLimit(); i++) {
                LootTable table = this.metaTileEntity.getWorld().getLootTableManager().getLootTableFromLocation(rl);
                LootContext ctx = new LootContext.Builder((WorldServer) this.metaTileEntity.getWorld()).build();
                List<ItemStack> stacks = table.generateLootForPools(this.metaTileEntity.getWorld().rand, ctx);

                allstacks.addAll(stacks);
                if(new Random().nextInt(21)==8)
                {
                    sword.setItemDamage(sword.getItemDamage()+1);
                }

            }
            if(flag==7 && new Random().nextInt(26-level)==1)
                for (int i = 0; i < level; i++) {
                    allstacks.add(new ItemStack(Items.SKULL,1*level,1));
                }
            GTTransferUtils.addItemsToItemHandler(this.getOutputInventory(), false, allstacks);
        }
        private int getLevel(ItemStack itemStack)
        {
            if (itemStack.getItem() instanceof ItemSword) {
                NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
                if (nbtTagCompound != null && nbtTagCompound.hasKey("ench", 9))
                {
                    NBTTagList enchantments = nbtTagCompound.getTagList("ench", 10);
                    for (int i = 0; i < enchantments.tagCount(); i++)
                    {
                        NBTTagCompound enchantmentTag = enchantments.getCompoundTagAt(i);
                        short id = enchantmentTag.getShort("id");
                        if (id == 21)
                        {
                            short level = enchantmentTag.getShort("lvl");
                            return level;
                        }
                    }
                }
            }
            return 1;
        }
    }

}