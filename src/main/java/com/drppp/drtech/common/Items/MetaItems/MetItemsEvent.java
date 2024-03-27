package com.drppp.drtech.common.Items.MetaItems;

import com.drppp.drtech.Client.Sound.SoundManager;
import com.drppp.drtech.common.Entity.EntityHyperGunBullet;
import com.drppp.drtech.common.Entity.EntityPlasmaBullet;
import com.drppp.drtech.common.Entity.EntityTachyonBullet;
import com.drppp.drtech.Tile.TileEntityConnector;
import com.drppp.drtech.api.Utils.DrtechUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class MetItemsEvent {
    public static void  onItemRightClick( World world, EntityPlayer player, EnumHand hand) {
        ItemStack item = player.getHeldItem(hand);
        if(player.isSneaking() && item.getItem()== MyMetaItems.GRAVITY_SHIELD.getMetaItem() && item.getMetadata()==MyMetaItems.GRAVITY_SHIELD.getMetaValue())
        {
            if (!world.isRemote) {
                if (player instanceof EntityPlayerMP) {
                    EntityPlayerMP playerMP = (EntityPlayerMP)player;
                    int currentDimension = playerMP.dimension;
                    int targetDimension;

                    // 根据当前维度设置目标维度，这里是如果玩家在主世界，则传送到Nether，否则传送回主世界
                    if (currentDimension != 300) {
                        targetDimension = 300;
                    } else {
                        targetDimension = DimensionType.OVERWORLD.getId();
                    }

                    // 传送玩家到目标维度
                    playerMP.getServer().getPlayerList().transferPlayerToDimension(playerMP, targetDimension, new Teleporter(playerMP.getServer().getWorld(targetDimension)));
                    playerMP.timeUntilPortal = 10; // 冷却时间，防止连续传送
                }
            }
        }else if(item.getItem()== MyMetaItems.TACTICAL_LASER_SUBMACHINE_GUN.getMetaItem() && item.getMetadata()==MyMetaItems.TACTICAL_LASER_SUBMACHINE_GUN.getMetaValue())
        {
            ItemStack currentGun = player.getHeldItem(hand);
            if(!hasEnergy(currentGun))
                return;
            long lastRightClick = getLastRightClick(currentGun);
            long currentTime = System.currentTimeMillis();
            if(currentTime - lastRightClick > 150 && hasEnergy(currentGun))
            {
                if(drainenergy(currentGun,1000,true))
                {
                    drainenergy(currentGun,1000,false);
                    EntityHyperGunBullet entity = new EntityHyperGunBullet(world, player, 50f, 360);
                    entity.shoot(player.rotationYaw, player.rotationPitch, 3.0f);
                    world.spawnEntity(entity);

                    world.playSound((EntityPlayer)null, player.posX , player.posY, player.posZ,
                            SoundManager.laser_bullet_shoot, player.getSoundCategory(), 0.2f, 1.0F);
                    setLastRightClick(currentGun, currentTime);
                }
            }
        }else if(item.getItem()== MyMetaItems.ELECTRIC_PLASMA_GUN.getMetaItem() && item.getMetadata()==MyMetaItems.ELECTRIC_PLASMA_GUN.getMetaValue())
        {
            ItemStack currentGun = player.getHeldItem(hand);
            if(!hasEnergy(currentGun))
                return;
            long lastRightClick = getLastRightClick(currentGun);
            long currentTime = System.currentTimeMillis();
            if(currentTime - lastRightClick > 500 && hasEnergy(currentGun))
            {
                lastRightClick = currentTime;
                if(drainenergy(currentGun,5000,true))
                {
                    EntityPlasmaBullet entity = new EntityPlasmaBullet(world, player, 20f);
                    entity.shoot(player.rotationYaw, player.rotationPitch, 1.5f);
                    world.spawnEntity(entity);

                    world.playSound((EntityPlayer)null, player.posX , player.posY, player.posZ,
                            SoundManager.plasma_launch, player.getSoundCategory(), 0.5f, 0.8F);

                    setLastRightClick(currentGun, lastRightClick);
                }

            }
        }else if(item.getItem()== MyMetaItems.ADVANCED_TACHINO_DISRUPTOR.getMetaItem() && item.getMetadata()==MyMetaItems.ADVANCED_TACHINO_DISRUPTOR.getMetaValue())
        {
            ItemStack currentGun = player.getHeldItem(hand);
            if(!hasEnergy(currentGun))
                return;
            long lastRightClick = getLastRightClick(currentGun);
            long currentTime = System.currentTimeMillis();
            if(currentTime - lastRightClick > 250)
            {
                if(drainenergy(currentGun,50000,true))
                {
                    drainenergy(currentGun,50000,false);
                    EntityTachyonBullet entity = new EntityTachyonBullet(world, player, 120f, 600);
                    entity.shoot(player.rotationYaw, player.rotationPitch, 4.0f);
                    world.spawnEntity(entity);

                    world.playSound((EntityPlayer)null, player.posX , player.posY, player.posZ,
                            SoundManager.laser_bullet_shoot, player.getSoundCategory(), 0.2f, 0.4F);
                    setLastRightClick(currentGun, currentTime);
                }
            }
        }

    }
    public static void onItemUse(EntityPlayer player, @NotNull World world, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if(player.isSneaking() && stack.getItem()== MyMetaItems.POS_CARD.getMetaItem() && stack.getMetadata()==MyMetaItems.POS_CARD.getMetaValue())
        {
            if( world.getTileEntity(pos)!=null &&  world.getTileEntity(pos)instanceof TileEntityConnector)
            {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setInteger("x",pos.getX());
                nbt.setInteger("y",pos.getY());
                nbt.setInteger("z",pos.getZ());
                stack.setTagCompound(nbt);
                player.sendMessage(new TextComponentString("已保存坐标: x:"+pos.getX() +"y:"+pos.getY()+"z:"+pos.getZ()));
            }


        }
        else  if(stack.getItem()== MyMetaItems.POS_CARD.getMetaItem() && stack.getMetadata()==MyMetaItems.POS_CARD.getMetaValue())
        {
            if( world.getTileEntity(pos)!=null &&  world.getTileEntity(pos)instanceof TileEntityConnector && stack.hasTagCompound())
            {
                NBTTagCompound nbt = stack.getTagCompound();
                BlockPos bpos = new BlockPos(nbt.getInteger("x"),nbt.getInteger("y"),nbt.getInteger("z"));

                if(DrtechUtils.getPosDist(pos,bpos)<=100)
                {
                    TileEntityConnector con = ((TileEntityConnector)world.getTileEntity(pos));
                    con.beforePos = bpos;
                    NBTTagCompound newnbt = new NBTTagCompound();
                    newnbt.setTag("locahost",nbt);
                    DrtechUtils.sendTileEntityUpdate(world.getTileEntity(pos),newnbt);
                    player.sendMessage(new TextComponentString("已写入坐标: x:"+bpos.getX() +"y:"+bpos.getY()+"z:"+bpos.getZ()));
                }
                else
                    player.sendMessage(new TextComponentString("距离超过100格!"));
            }
        }
    }

    public static void hitEntity(ItemStack stack, EntityLivingBase targetEntity, EntityLivingBase attacker) {
        if (stack.getItem()== MyMetaItems.TACTICAL_LASER_SUBMACHINE_GUN.getMetaItem() && stack.getMetadata()==MyMetaItems.TACTICAL_LASER_SUBMACHINE_GUN.getMetaValue() && targetEntity instanceof EntityLivingBase && attacker instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) attacker;
            EntityLivingBase enemyEntity =  targetEntity;
            if (hasEnergy(stack)) {
                if(drainenergy(stack,100,true))
                {
                    drainenergy(stack,100,false);
                    enemyEntity.knockBack(attacker, 1.0f, MathHelper.sin(player.rotationYaw * 0.017453292F),
                            (-MathHelper.cos(player.rotationYaw * 0.017453292F)));
                    enemyEntity.attackEntityFrom(DamageSource.causePlayerDamage(player), 25.0f);
                }
            }
        }
        else if(stack.getItem()== MyMetaItems.ADVANCED_TACHINO_DISRUPTOR.getMetaItem() && stack.getMetadata()==MyMetaItems.ADVANCED_TACHINO_DISRUPTOR.getMetaValue() && targetEntity instanceof EntityLivingBase && attacker instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) attacker;
            EntityLivingBase enemyEntity = (EntityLivingBase) targetEntity;
            if(!hasEnergy(stack))
                return;
            if (drainenergy(stack,300,true)) {
                drainenergy(stack,300,false);
                enemyEntity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 30, 3));
                enemyEntity.knockBack(attacker, 1.0f, (double) MathHelper.sin(player.rotationYaw * 0.017453292F),
                        (double) (-MathHelper.cos(player.rotationYaw * 0.017453292F)));
                enemyEntity.attackEntityFrom(DamageSource.causePlayerDamage(player), 35);
            }
        }
    }
    private static void enableFlyingAbility(EntityPlayer player)
    {
        player.capabilities.allowFlying = true;
        player.capabilities.isFlying = false;
        player.sendPlayerAbilities();
        player.sendStatusMessage(new TextComponentString(I18n.format("metaitem.gravity_shield.info.1", new Object[0])), true);
    }

    private static void disableFlyingAbility(EntityPlayer player)
    {
        player.capabilities.allowFlying = false;
        player.capabilities.isFlying = false;
        player.sendPlayerAbilities();
        player.sendStatusMessage(new TextComponentString(I18n.format("metaitem.gravity_shield.info.2", new Object[0])), true);
    }

    private static boolean hasEnergy(ItemStack item)
    {
        NBTTagCompound tag = item.getTagCompound();
        if(tag!=null && !tag.hasKey("Charge"))
            return false;
        if(tag.getLong("Charge")<=0)
            return false;
        return true;
    }
    //返回是否能消耗能量
    private static boolean drainenergy(ItemStack item,long amount,boolean simulate)
    {
        NBTTagCompound tag = item.getTagCompound();
        long leftEnergy = tag.getLong("Charge");
        if(leftEnergy<amount)
            return false;
        if(!simulate)
        {
            leftEnergy -=amount;
            tag.setLong("Charge",leftEnergy);
            item.setTagCompound(tag);
        }
        return true;
    }
    private static void setLastRightClick(ItemStack stack, long value)
    {
        stack.getItem().getNBTShareTag(stack).setLong("LastRightClick", value);
    }
    private static long getLastRightClick(ItemStack stack)
    {
        long value = 0;
        try {
            value = stack.getItem().getNBTShareTag(stack).getLong("LastRightClick");
        } catch (Exception expt) {}
        return value;
    }
}
