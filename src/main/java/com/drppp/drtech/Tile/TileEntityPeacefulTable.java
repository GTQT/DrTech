package com.drppp.drtech.Tile;

import gregtech.api.util.GTTransferUtils;
import net.minecraft.init.Biomes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileEntityPeacefulTable extends TileEntity implements ITickable {
    private int tick=0;
    public TileEntityPeacefulTable(){

    }
    @Override
    public void update() {
        if(!this.getWorld().isRemote && this.getWorld().getDifficulty()== EnumDifficulty.PEACEFUL && tick++>200)
        {
            tick=0;
            if(this.getWorld().getTileEntity(this.getPos().offset(EnumFacing.UP,1))!=null && this.getWorld().getTileEntity(this.getPos().offset(EnumFacing.UP,1)).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,null))
            {
                var ca = this.getWorld().getTileEntity(this.getPos().offset(EnumFacing.UP,1)).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,null);

                for (int i = 0; i < ca.getSlots(); i++) {
                    if(ca.getStackInSlot(i).getItem() instanceof ItemSword)
                    {
                        ItemStack sword = ca.getStackInSlot(i);
                        //僵尸 末影人 骷髅 苦力怕 史莱姆
                        switch (new Random().nextInt(5))
                        {
                            case 1:
                                getDrops(LootTableList.ENTITIES_ZOMBIE,ca,sword,i);
                                break;
                            case 2:
                                getDrops(LootTableList.ENTITIES_SKELETON,ca,sword,i);
                                break;
                            case 3:
                                getDrops(LootTableList.ENTITIES_CREEPER,ca,sword,i);
                                break;
                            case 4:
                                getDrops(LootTableList.ENTITIES_ENDERMAN,ca,sword,i);
                                break;
                            case 0:
                                getDrops(LootTableList.ENTITIES_SLIME,ca,sword,i);
                                break;
                        }
                    }
                }
            }
        }
    }
    private void getDrops(ResourceLocation rl, IItemHandler handler, ItemStack sword,int loca)
    {
        List<ItemStack> allstacks = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            LootTable table = this.getWorld().getLootTableManager().getLootTableFromLocation(rl);
            LootContext ctx = new LootContext.Builder((WorldServer) this.getWorld()).build();
            List<ItemStack> stacks = table.generateLootForPools(this.getWorld().rand, ctx);

            allstacks.addAll(stacks);
            if(new Random().nextInt(2)==1)
            {
                sword.setItemDamage(sword.getItemDamage()+1);
                if(sword.getItemDamage()>=sword.getMaxDamage())
                    handler.extractItem(loca,1,false);
            }

        }
        GTTransferUtils.addItemsToItemHandler(handler, false, allstacks);
    }
}
