package com.drppp.drtech.Utils;

import com.drppp.drtech.Sync.SyncInit;
import com.drppp.drtech.Sync.UpdateTileEntityPacket;
import com.drppp.drtech.Tags;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.drppp.drtech.Load.DrtechReceipes.LOG_CREATE;

public class DrtechUtils {
    public static  List<Material> listMater = new ArrayList<>();
    public static  Integer baseTime = 700;
    @Nonnull
    public static ResourceLocation getRL(@Nonnull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

    public static void initList()
    {
        listMater.add(Materials.Hydrogen);
        listMater.add(Materials.Helium);
        listMater.add(Materials.Lithium);
        listMater.add(Materials.Beryllium);
        listMater.add(Materials.Boron);
        listMater.add(Materials.Carbon);
        listMater.add(Materials.Nitrogen);
        listMater.add(Materials.Oxygen);
        listMater.add(Materials.Fluorine);
        listMater.add(Materials.Neon);
        listMater.add(Materials.Sodium);
        listMater.add(Materials.Magnesium);
        listMater.add(Materials.Aluminium);
        listMater.add(Materials.Silicon);
        listMater.add(Materials.Phosphorus);
        listMater.add(Materials.Sulfur);
        listMater.add(Materials.Chlorine);
        listMater.add(Materials.Argon);
        listMater.add(Materials.Potassium);
        listMater.add(Materials.Calcium);
        listMater.add(Materials.Scandium);
        listMater.add(Materials.Titanium);
        listMater.add(Materials.Vanadium);
        listMater.add(Materials.Chrome);
        listMater.add(Materials.Manganese);
        listMater.add(Materials.Iron);
        listMater.add(Materials.Cobalt);
        listMater.add(Materials.Nickel);
        listMater.add(Materials.Copper);
        listMater.add(Materials.Zinc);
        listMater.add(Materials.Gallium);
        listMater.add(Materials.Germanium);
        listMater.add(Materials.Arsenic);
        listMater.add(Materials.Selenium);
        listMater.add(Materials.Bromine);
        listMater.add(Materials.Krypton);
        listMater.add(Materials.Rubidium);
        listMater.add(Materials.Strontium);
        listMater.add(Materials.Yttrium);
        listMater.add(Materials.Zirconium);
        listMater.add(Materials.Niobium);
        listMater.add(Materials.Molybdenum);
        listMater.add(Materials.Technetium);
        listMater.add(Materials.Ruthenium);
        listMater.add(Materials.Rhodium);
        listMater.add(Materials.Palladium);
        listMater.add(Materials.Silver);
        listMater.add(Materials.Cadmium);
        listMater.add(Materials.Indium);
        listMater.add(Materials.Tin);
        listMater.add(Materials.Antimony);
        listMater.add(Materials.Tellurium);
        listMater.add(Materials.Iodine);
        listMater.add(Materials.Xenon);
        listMater.add(Materials.Caesium);
        listMater.add(Materials.Barium);
        listMater.add(Materials.Lanthanum);
        listMater.add(Materials.Cerium);
        listMater.add(Materials.Praseodymium);
        listMater.add(Materials.Neodymium);
        listMater.add(Materials.Promethium);
        listMater.add(Materials.Samarium);
        listMater.add(Materials.Europium);
        listMater.add(Materials.Gadolinium);
        listMater.add(Materials.Terbium);
        listMater.add(Materials.Dysprosium);
        listMater.add(Materials.Holmium);
        listMater.add(Materials.Erbium);
        listMater.add(Materials.Thulium);
        listMater.add(Materials.Ytterbium);
        listMater.add(Materials.Lutetium);
        listMater.add(Materials.Hafnium);
        listMater.add(Materials.Tantalum);
        listMater.add(Materials.Tungsten);
        listMater.add(Materials.Rhenium);
        listMater.add(Materials.Osmium);
        listMater.add(Materials.Iridium);
        listMater.add(Materials.Platinum);
        listMater.add(Materials.Gold);
        listMater.add(Materials.Mercury);
        listMater.add(Materials.Thallium);
        listMater.add(Materials.Lead);
        listMater.add(Materials.Bismuth);
        listMater.add(Materials.Polonium);
        listMater.add(Materials.Astatine);
        listMater.add(Materials.Radon);
        listMater.add(Materials.Francium);
        listMater.add(Materials.Radium);
        listMater.add(Materials.Actinium);
        listMater.add(Materials.Thorium);
        listMater.add(Materials.Protactinium);
        listMater.add(Materials.Uranium235);
        listMater.add(Materials.Uranium238);
        listMater.add(Materials.Neptunium);
        listMater.add(Materials.Plutonium239);
        listMater.add(Materials.Plutonium241);
        listMater.add(Materials.Americium);
        listMater.add(Materials.Curium);
        listMater.add(Materials.Berkelium);
        listMater.add(Materials.Californium);
        listMater.add(Materials.Einsteinium);
        listMater.add(Materials.Fermium);
        listMater.add(Materials.Mendelevium);
        listMater.add(Materials.Nobelium);
        listMater.add(Materials.Lawrencium);
        listMater.add(Materials.Rutherfordium);
        listMater.add(Materials.Dubnium);
        listMater.add(Materials.Seaborgium);
        listMater.add(Materials.Bohrium);
        listMater.add(Materials.Hassium);
        listMater.add(Materials.Meitnerium);
        listMater.add(Materials.Darmstadtium);
        listMater.add(Materials.Roentgenium);
        listMater.add(Materials.Copernicium);
        listMater.add(Materials.Nihonium);
        listMater.add(Materials.Flerovium);
        listMater.add(Materials.Moscovium);
        listMater.add(Materials.Livermorium);
        listMater.add(Materials.Tennessine);
        listMater.add(Materials.Dubnium);
        listMater.add(Materials.Trinium);
        listMater.add(Materials.Neutronium);
        listMater.add(Materials.Naquadah);
        listMater.add(Materials.Tritanium);
        listMater.add(Materials.Duranium);
    }
    public static String getName(MetaItem.MetaValueItem is) {
        return is.getStackForm().getDisplayName();
    }
    public static String getName(Material mater)
    {
        return mater.getLocalizedName();
    }

    public static void addLogCreate(int EUt, int tick, int outNum, int meta)
    {
        LOG_CREATE.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.SAPLING,1,meta))
                .outputs(new ItemStack(Blocks.LOG,outNum,meta))
                .EUt(EUt)
                .duration(tick)
                .buildAndRegister();
    }
    public static void addLog2Create(int EUt, int tick, int outNum, int meta)
    {
        LOG_CREATE.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.SAPLING,1,meta+4))
                .outputs(new ItemStack(Blocks.LOG2,outNum,meta))
                .EUt(EUt)
                .duration(tick)
                .buildAndRegister();
    }
    public static BigInteger getBigIntegerMin(BigInteger a,BigInteger b)
    {
        int comparisonResult = a.compareTo(b);

        BigInteger minValue;
        if (comparisonResult < 0) {
            minValue = a;
        } else {
            minValue = b;
        }
        return  minValue;
    }

    public static void sendTileEntityUpdate(TileEntity tileEntity,NBTTagCompound nbt) {
        tileEntity.writeToNBT(nbt);
        UpdateTileEntityPacket packet = new UpdateTileEntityPacket(tileEntity.getPos(), nbt);
        SyncInit.NETWORK.sendToServer(packet);
    }
    public static void sendTileEntityClientUpdate(TileEntity tileEntity,NBTTagCompound nbt) {
        tileEntity.writeToNBT(nbt);
        UpdateTileEntityPacket packet = new UpdateTileEntityPacket(tileEntity.getPos(), nbt);
        SyncInit.NETWORK_CLIENT.sendToAll(packet);
    }
    public static int getPosDist(BlockPos a,BlockPos b)
    {
        int x = (int)Math.pow(a.getX()-b.getX(),2);
        int y = (int)Math.pow(a.getY()-b.getY(),2);
        int z = (int)Math.pow(a.getZ()-b.getZ(),2);

        return  (int)Math.sqrt(x+y+z);
    }

    public static EnumFacing getDirectionFromB1ToB2(BlockPos b1, BlockPos b2) {
        // 检查b1和b2是否为相同的位置
        if (b1.equals(b2)) {
            return EnumFacing.UP;
        }

        int diffX = b2.getX() - b1.getX();
        int diffY = b2.getY() - b1.getY();
        int diffZ = b2.getZ() - b1.getZ();

        // Minecraft中的方向是基于玩家视角的，南北东西对应负正的Z和X轴
        if (diffX > 0) {
            return EnumFacing.EAST;
        } else if (diffX < 0) {
            return EnumFacing.WEST;
        } else if (diffZ > 0) {
            return EnumFacing.SOUTH;
        } else if (diffZ < 0) {
            return EnumFacing.NORTH;
        } else if (diffY > 0) {
            return EnumFacing.UP;
        } else if (diffY < 0) {
            return EnumFacing.DOWN;
        } else {
            return EnumFacing.UP;
        }
    }
}
