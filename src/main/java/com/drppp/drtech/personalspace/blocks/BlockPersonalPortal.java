package com.drppp.drtech.personalspace.blocks;

import com.drppp.drtech.Tags;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.drppp.drtech.personalspace.blocks.tile.TilePersonalPortal;
import com.drppp.drtech.personalspace.packet.Packets;

public class BlockPersonalPortal extends Block implements ITileEntityProvider {

    public BlockPersonalPortal() {
        super(Material.ROCK, MapColor.BLUE);
        this.setRegistryName(Tags.MODID, "personal_portal");
        this.setCreativeTab(CreativeTabs.MISC);
        this.setTranslationKey("personal_portal");
        this.setHarvestLevel("pickaxe", 2);
        this.setHardness(40.0f);
        this.setResistance(6000000.0f);
    }

    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625f, 0.0f, 0.0625f, 0.9375f, 1.0625f, 0.9375f);

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TilePersonalPortal();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote && !playerIn.isRiding()) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TilePersonalPortal tpp) {
                if (tpp.isActive() && !playerIn.isSneaking()) {
                    tpp.transport((EntityPlayerMP) playerIn, pos);
                    return true;
                } else {
                    OpenGUI(worldIn, playerIn, tpp);
                }
            }
        }
        return true;
    }

    public void OpenGUI(World world, EntityPlayer player, TilePersonalPortal portal) {
        if (portal.isActive()) {
            if (player.isSneaking()) {
                Packets.INSTANCE.sendOpenGui(portal).sendToPlayer(player);
            }
        } else {
            if (world.provider.getDimension() != 0) {
                player.sendMessage(new TextComponentTranslation("chat.overworldPersonalDimension"));
                return;
            }
            Packets.INSTANCE.sendOpenGui(portal).sendToPlayer(player);
        }
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
                             @org.jetbrains.annotations.Nullable TileEntity te, ItemStack stack) {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F);
        if (te != null) {
            if (te instanceof TilePersonalPortal tpp) {
                ItemStack stack1 = new ItemStack(state.getBlock());
                stack1.setTagCompound(tpp.writeToNBT(new NBTTagCompound()));
                worldIn.spawnEntity(new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack1));
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
                                ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TilePersonalPortal tpp)) {
            return;
        }

        if (world.isRemote)
            return;

        if (stack.hasTagCompound()) {
            NBTTagCompound nbtTagCompound = stack.getTagCompound();
            nbtTagCompound.setInteger("x", tpp.getPos().getX());
            nbtTagCompound.setInteger("y", tpp.getPos().getY());
            nbtTagCompound.setInteger("z", tpp.getPos().getZ());
            tpp.deserializeNBT(nbtTagCompound);
            EntityPlayerMP player = null;
            if (placer instanceof EntityPlayerMP) {
                player = (EntityPlayerMP) placer;
            }
            tpp.linkOtherPortal(false, player);
        }
        tpp.sendToClient();
        tpp.markDirty();
    }
}
