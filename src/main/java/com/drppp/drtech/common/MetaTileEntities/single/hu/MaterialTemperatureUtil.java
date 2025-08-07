package com.drppp.drtech.common.MetaTileEntities.single.hu;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.List;


public class MaterialTemperatureUtil {
    public static HashMap<OrePrefix, Float> MaterialContent = new HashMap<OrePrefix, Float>(){
        {
            put(OrePrefix.ingot,1f);
            put(OrePrefix.gear,1f);
            put(OrePrefix.gearSmall,1f);
            put(OrePrefix.dust,1f);
        }
    };
    public static void init()
    {

    }
    public static void FindAllowRecipe(List<Material> materialList)
    {

    }
    public static int getMaterialTemperatureByMaterial(Material material)
    {
        if(material.hasFluid())
        {
           return  material.getFluid().getTemperature();
        }
        return -1;
    }
    public static int getMaterialTemperatureByItemStack(ItemStack item)
    {
        Material mat = getMaterialFromItemStack(item);
        if(mat==null)
            return -1;
        if(mat.hasFluid())
        {
            return  mat.getFluid().getTemperature();
        }
        return -1;
    }
    public static Material getMaterialFromItemStack(ItemStack item)
    {
        var stack =  OreDictUnifier.getMaterial(item);
        if(stack==null)
            return null;
        return stack.material;
    }
    public static float getContentByItemStack(ItemStack item)
    {
        var ore = OreDictUnifier.getPrefix(item);
        if(ore!=null)
        {
            if(MaterialContent.containsKey(ore))
            {
                return MaterialContent.get(ore);
            }
            return 0;
        }
        return 0;
    }
    public class MaterialStack{
        public Material mat;
        public Float num;

        public MaterialStack(Material mat, Float num) {
            this.mat = mat;
            this.num = num;
        }
    }
}
