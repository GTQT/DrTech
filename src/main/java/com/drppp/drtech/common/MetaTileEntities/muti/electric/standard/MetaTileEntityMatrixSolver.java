package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.api.Muti.DrtMultiblockAbility;
import com.drppp.drtech.api.Utils.CustomeRecipe;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Items.MetaItems.MyMetaItems;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiMapMultiblockController;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import keqing.gtqtcore.client.textures.GTQTTextures;
import keqing.gtqtcore.common.block.GTQTMetaBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityMatrixSolver extends MetaTileEntityBaseWithControl{
    public int mode = 0;
    public List<RecipeMap> recipemaps = new ArrayList<>();
    public CustomeRecipe scan = new CustomeRecipe();
    public Recipe scan_recipe;
    public String NBT_TAG_NAME = "StoreRecipe";
    //执行配方用变量
    public List<CustomeRecipe> run_cres = new ArrayList<>();
    public long EUT =0;
    public CustomeRecipe run_recipe=null;
    public int parra=1;
    public MetaTileEntityMatrixSolver(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("Mode", mode);
        //nbt.setBoolean("IsRunning", is_running);
        nbt.setInteger("MaxProcess", maxProcess);
        nbt.setInteger("Process", process);
        nbt.setInteger("Parra", parra);
        nbt.setLong("EUT", EUT);
        if (scan != null) {
            nbt.setTag("ScanRecipe", scan.writeToNBT());
        }
        if (run_recipe != null) {
            nbt.setTag("RunRecipe", run_recipe.writeToNBT());
        }
        NBTTagList recipeListNBT = new NBTTagList();
        for (CustomeRecipe recipe : run_cres) {
            recipeListNBT.appendTag(recipe.writeToNBT());
        }
        nbt.setTag("RunCres", recipeListNBT);
        return nbt;
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        mode = nbt.getInteger("Mode");
        //is_running = nbt.getBoolean("IsRunning");
        maxProcess = nbt.getInteger("MaxProcess");
        process = nbt.getInteger("Process");
        parra = nbt.getInteger("Parra");
        EUT = nbt.getLong("EUT");
        if (nbt.hasKey("ScanRecipe")) {
            scan = new CustomeRecipe(nbt.getCompoundTag("ScanRecipe"));
        }
        if (nbt.hasKey("RunRecipe")) {
            run_recipe = new CustomeRecipe(nbt.getCompoundTag("RunRecipe"));
        }
        run_cres.clear();
        if (nbt.hasKey("RunCres")) {
            NBTTagList recipeListNBT = nbt.getTagList("RunCres", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < recipeListNBT.tagCount(); i++) {
                NBTTagCompound recipeNBT = recipeListNBT.getCompoundTagAt(i);
                run_cres.add(new CustomeRecipe(recipeNBT));
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add("该机器分成三种运行模式:");
        tooltip.add("1-配方解析====================");
        tooltip.add("在该模式下，机器用于解析配方，并把配方写入==矩阵水晶==");
        tooltip.add("输入总线需要放入该机器支持的大机器主方块，机器会读取该机器的配方");
        tooltip.add("受支持的机器可以在jei进行查看/可通过大化反按U进行查询");
        tooltip.add("输入总线/仓放入需要执行的配方的原料，然后放入矩阵水晶，机器会把配方写入矩阵水晶，并在输出总线输出");
        tooltip.add("2-配方联和====================");
        tooltip.add("输入总线放入多个已经写入的单配方水晶，最后一个是最终产出水晶");
        tooltip.add("然后放入一个空的矩阵水晶，运算器会自动解析并尝试组合配方");
        tooltip.add("最后一个带有配方的水晶不参与运算，即产出水晶");
        tooltip.add("运算完毕后，结果可能成功可能会失败，如果失败可以检查前置步骤是否存在无法配平的循环配方");
        tooltip.add("提示：可以尝试分步骤组装。");
        tooltip.add("运算结束后会输出一个联合后的矩阵水晶，可用于生产");
        tooltip.add("运算后的配方会联和所有的单配方，消除中间步骤，通过原料直接产出最终产品");
        tooltip.add("3-配方生产====================");
        tooltip.add("需要用配方联合后的矩阵水晶进行生产，过程会校验配方用的机器，机器需放入输入总线");
        tooltip.add("矩阵水晶也需要放入输入总线");
        tooltip.add("原材料也正常放入总线、仓室");
        tooltip.add("不支持单步配方生产，即未联和运算过的配方(深度为0的配方)");
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("AAAAAAAAAAAAAAAAA", "B       B       B", "B       B       B", "B       B       B", "B       B       B", "B       B       B", "AAAAAAAAAAAAAAAAA", "B       B       B", "B       B       B", "B       B       B", "B       B       B", "B       B       B", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", "                 ", "                 ", "                 ", "                 ", "                 ", "AAAAAAAAAAAAAAAAA", "                 ", "                 ", "                 ", "                 ", "                 ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", "B       B       B", "B       B       B", "B       B       B", "B       B       B", "B       B       B", "AAAAAAAAAAAAAAAAA", "B       B       B", "B       B       B", "B       B       B", "B       B       B", "B       B       B", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", "                 ", "                 ", "                 ", "                 ", "                 ", "AAAAAAAAAAAAAAAAA", "                 ", "                 ", "                 ", "                 ", "                 ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "                 ", " CCC CCC CCC CCC ", " CCC CCC CCC CCC ", "AAAAAAAAAAAAAAAAA" )
                .aisle("AAAAAAAAAAAAAAAAA", "B       B       B", "B       B       B", "B       B       B", "B       B       B", "B       B       B", "AAAAAAAASAAAAAAAA", "B       B       B", "B       B       B", "B       B       B", "B       B       B", "B       B       B", "AAAAAAAAAAAAAAAAA" )
                .where('S', selfPredicate())
                .where(' ',any() )
                .where('A',states(BlocksInit.COMMON_CASING1.getStateFromMeta(7)).setMinGlobalLimited(578)
                        .or(autoAbilities(true,true))
                        .or(abilities(DrtMultiblockAbility.EXPORT_ITEM_FLUID).setMaxGlobalLimited(16))
                        .or(abilities(DrtMultiblockAbility.IMPORT_ITEM_FLUID).setMaxGlobalLimited(16))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                        .or(abilities(MultiblockAbility.INPUT_LASER).setMaxGlobalLimited(1))
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMaxGlobalLimited(16))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMaxGlobalLimited(16))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(16))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMaxGlobalLimited(16))
                )
                .where('B', states(GTQTMetaBlocks.KQCC.getStateFromMeta(10)))
                .where('D', states(BlocksInit.COMMON_CASING.getStateFromMeta(3)))
                .where('C', states(BlocksInit.TRANSPARENT_CASING1.getStateFromMeta(2))
                )
                .build();
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.MATRIX_CASING;
    }
    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.INDUSTRIAL_MACHINE;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), this.isWorkingEnabled()&&isStructureFormed(), this.isWorkingEnabled()&&isStructureFormed());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMatrixSolver(this.metaTileEntityId);
    }
    @Override
    @Nonnull
    protected Widget getFlexButton(int x, int y, int width, int height) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);
        group.addWidget(new ClickButtonWidget(0, 0, 18, 18, "", this::changeProductType)
                .setButtonTexture(GuiTextures.BUTTON_CLEAR_GRID)
                .setTooltipText("drtech.multiblock.lbh.changep"));
        return group;
    }
    private void changeProductType(Widget.ClickData clickData)
    {
        this.mode = (++this.mode)%4;
    }
    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if(this.mode==0)
        {
            textList.add(new TextComponentString("工作方式:"+"扫描输出单配方"));
            textList.add(new TextComponentString("配方仓库:"+ (this.recipemaps.size()==0?"空":this.recipemaps.size()+"个")));
            if(this.scan_recipe!=null)
            {
                textList.add(new TextComponentString("匹配到配方"));
            }
        }
        else if(this.mode==1)
            textList.add(new TextComponentString("工作方式:"+"待定"));
        else if(this.mode==2)
            textList.add(new TextComponentString("工作方式:"+"解析联和配方"));
        else if(this.mode==3)
        {
            textList.add(new TextComponentString("工作方式:"+"执行配方产出"));
            textList.add(new TextComponentString("运行状态:"+isWorkingEnabled()));
            if(run_recipe!=null)
            {
                textList.add(new TextComponentString("并行:"+parra +"/16"));
                textList.add(new TextComponentString("耗电:"+EUT));
                textList.add(new TextComponentString("进度:"+process+"/"+maxProcess+"Tick"));
            }
        }

    }

    @Override
    protected void updateFormedValid() {
        if(!this.getWorld().isRemote && this.isWorkingEnabled() && this.isStructureFormed())
        {

           if(mode==0)
           {
               recipemaps = new ArrayList<>();
               if(this.inputInventory!=null && this.inputInventory.getSlots()>0)
               {
                   for (int i = 0; i < this.inputInventory.getSlots(); i++)
                   {
                       ItemStack item = this.inputInventory.getStackInSlot(i).copy();
                       if(GTUtility.getMetaTileEntity(item) != null && CustomeRecipe.ListContainsItem(CustomeRecipe.CAN_DO_WORK_MACHINES,item))
                       {
                           var machine = GTUtility.getMetaTileEntity(item);
                           if(!CustomeRecipe.ListContainsItem(scan.machineItems,item) )
                           {
                               scan.machineItems.add(item);
                               scan.machines.add(machine);
                           }
                           if(machine instanceof MultiMapMultiblockController)
                           {
                                var maps = ((MultiMapMultiblockController)machine).getAvailableRecipeMaps();
                               for (int j = 0; j < maps.length; j++) {
                                   var rec = maps[j];
                                   if (recipemaps.stream().noneMatch(x -> x.equals(rec))) {
                                       recipemaps.add(rec);
                                   }
                               }
                           }
                          else if(machine.getRecipeMap() !=null)
                           {
                               if (recipemaps.stream().noneMatch(x -> x.equals(machine.getRecipeMap()))) {
                                   recipemaps.add(machine.getRecipeMap());
                               }
                           }
                       }
                   }
               }
               if(!recipemaps.isEmpty())
               {
                    for(var formap: recipemaps)
                    {
                        scan_recipe = formap.findRecipe(Integer.MAX_VALUE,this.inputInventory,this.inputFluidInventory);
                        if(scan_recipe!=null)
                        {
                            var list = scan.machineItems;
                            scan = new CustomeRecipe();
                            scan.GetDataFromRecipe(scan_recipe);
                            scan.machineItems = list;

                            if(this.inputInventory!=null && this.inputInventory.getSlots()>0)
                            {
                                for (int i = 0; i < this.inputInventory.getSlots(); i++)
                                {
                                    if(IsMatrixGem(this.inputInventory.getStackInSlot(i)) && this.scan!=null)
                                    {
                                        var item = this.inputInventory.getStackInSlot(i).copy();
                                        item.setCount(1);
                                        NBTTagCompound tag = scan.writeToNBT();
                                        item.setTagInfo(NBT_TAG_NAME,tag);

                                        if(this.outputInventory!=null && this.outputInventory.getSlots()>0 && !this.inputInventory.extractItem(i,1,false).isEmpty())
                                        {
                                            GTTransferUtils.insertItem(this.outputInventory,item,false);
                                        }
                                    }
                                }
                            }
                        }
                    }
               }
           }else if(mode==1)
           {

           }
           else if(mode==2)
           {
               List<CustomeRecipe> cres = new ArrayList<>();
               if(this.inputInventory!=null && this.inputInventory.getSlots()>0)
               {
                   for (int i = 0; i < this.inputInventory.getSlots(); i++)
                   {
                       if(IsMatrixGem(this.inputInventory.getStackInSlot(i)))
                       {
                           var item = this.inputInventory.getStackInSlot(i).copy();
                           if(item.hasTagCompound() && item.getTagCompound().hasKey(NBT_TAG_NAME))
                           {
                                cres.add(new CustomeRecipe(item.getTagCompound().getCompoundTag(NBT_TAG_NAME)));
                           }
                       }
                   }
               }
               if(!cres.isEmpty() && cres.size()>1)
               {
                   DrtechUtils.yunSuan(cres);
                   CustomeRecipe Merged = new CustomeRecipe();
                   for (int i = 0; i < cres.size(); i++) {
                       Merged = CustomeRecipe.mergeRecipes(Merged,cres.get(i));
                   }
                   Merged.reduceToSmallest();
                   NBTTagCompound tag = Merged.writeToNBT();
                   for (int i = 0; i < this.inputInventory.getSlots(); i++)
                   {
                       if(IsMatrixGem(this.inputInventory.getStackInSlot(i)) && !this.inputInventory.getStackInSlot(i).hasTagCompound())
                       {
                           var item = this.inputInventory.getStackInSlot(i).copy();
                           item.setCount(1);
                           item.setTagInfo(NBT_TAG_NAME,tag);
                           if(this.outputInventory!=null && this.outputInventory.getSlots()>0 && !this.inputInventory.extractItem(i,1,false).isEmpty())
                           {
                               GTTransferUtils.insertItem(this.outputInventory,item,false);
                           }
                       }
                   }
               }
           }
           else if(mode==3){
               if(!isWorkingEnabled() || run_cres.isEmpty())
               {
                   run_cres = new ArrayList<>();
                   if(this.inputInventory!=null && this.inputInventory.getSlots()>0)
                   {
                       for (int i = 0; i < this.inputInventory.getSlots(); i++)
                       {
                           if(IsMatrixGem(this.inputInventory.getStackInSlot(i)) && run_cres.isEmpty())
                           {
                               var item = this.inputInventory.getStackInSlot(i).copy();
                               if(item.hasTagCompound() && item.getTagCompound().hasKey(NBT_TAG_NAME))
                               {
                                   run_cres.add(new CustomeRecipe(item.getTagCompound().getCompoundTag(NBT_TAG_NAME)));
                               }
                           }
                       }
                   }
               }
               if(!run_cres.isEmpty() && !isWorkingEnabled())
               {
                   setWorkingEnabled(true);
               }
               if(isWorkingEnabled() && !run_cres.isEmpty())
               {
                    if(run_recipe==null)
                    {
                        for (var rec: run_cres)
                        {
                            if(inputInventory.getSlots()>0 && this.inputFluidInventory.getTanks()>0)
                            {
                                if( rec.CheckCustomerRecipes(this.inputInventory,this.inputFluidInventory))
                                {
                                    int parrac = rec.CalculationParra(this.inputInventory,this.inputFluidInventory);
                                    this.parra = parrac;
                                    run_recipe = rec;
                                    process=0;
                                    maxProcess = run_recipe.during;
                                    EUT = rec.eut;
                                    if(this.getEnergyContainer()!=null)
                                    {
                                        int inputv= GTUtility.getTierByVoltage(this.getEnergyContainer().getInputVoltage());
                                        int recpev =  GTUtility.getTierByVoltage(run_recipe.eut);
                                        if (inputv > recpev) {
                                            int level = inputv - recpev;
                                            int count = 0;
                                            while (maxProcess > 1 && count < level) {
                                                maxProcess /= 4;
                                                count++;
                                            }
                                            int rate = (int) Math.pow(4, count);
                                            maxProcess = Math.max(maxProcess, 1);
                                            EUT *= rate;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(run_recipe!=null && this.getEnergyContainer()!=null  && this.getEnergyContainer().getEnergyStored()>=run_recipe.eut && GTUtility.getTierByVoltage(this.getEnergyContainer().getInputVoltage())>= GTUtility.getTierByVoltage(run_recipe.eut))
                    {
                        this.getEnergyContainer().changeEnergy(-run_recipe.eut);
                        if(++process>=maxProcess)
                        {
                            for (int i = 0; i < parra; i++) {
                                run_recipe.RunRecipe(this.inputInventory,this.inputFluidInventory,this.outputInventory,this.outputFluidInventory);
                            }
                            process=0;
                            maxProcess=0;
                            run_recipe=null;
                        }
                    }
               }
               else
               {
                   setWorkingEnabled(false);

               }
           }

        }
    }
    public boolean IsMatrixGem(ItemStack item)
    {
        if(item.getItem()== MyMetaItems.MATRIX_GEMS.getMetaItem() && item.getMetadata()==MyMetaItems.MATRIX_GEMS.getMetaValue())
            return true;
        return false;
    }

}
