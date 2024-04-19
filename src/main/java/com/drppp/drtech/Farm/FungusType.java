package com.drppp.drtech.Farm;

import com.drppp.drtech.Tags;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class FungusType {
    public String TypeName;
    public int GrowSpeed;//生长速度
    public int Yield;//产量
    public int Level;//等级 对生长速度和产量有倍率计算
    public int Tire;//成熟阶段
    public int Light;//亮度
    public Block BottomBlock;
    public List<FungusDrop> DropList;
    public List<FungusDrop> SpecialDropList;
    public List<ResourceLocation> RL=new ArrayList<>();

    public FungusType(String typeName, int growSpeed, int yield, int level, int tire, int light, Block bottomBlock, List<FungusDrop> dropList, List<FungusDrop> specialDropList) {
        TypeName = typeName;
        GrowSpeed = growSpeed;
        Yield = yield;
        Level = level;
        Tire = tire;
        Light = light;
        BottomBlock = bottomBlock;
        DropList = dropList;
        SpecialDropList = specialDropList;
        for (int i = 1; i <= tire; i++) {
            this.RL.add(new ResourceLocation(Tags.MODID,"textures/crops/"+this.TypeName+"/"+this.TypeName+i+".png"));
        }
        FungusTypes.allType.add(this);
    }

    public FungusType() {
    }
}
