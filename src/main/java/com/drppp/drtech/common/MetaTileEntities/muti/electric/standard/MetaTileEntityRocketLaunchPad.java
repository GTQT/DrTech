package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import com.drppp.drtech.api.Muti.DrtMultiblockAbility;
import com.drppp.drtech.common.Entity.EntityAdvancedRocket;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityRocketLaunchPad extends MetaTileEntityBaseWithControl {
    public MetaTileEntityRocketLaunchPad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(FRONT, UP, RIGHT)
                .aisle("AAAAAAAAAAAAAAA", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("BBBBAAAAAAAAAAA", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BBBB           ")
                .aisle("BBBBAAAAAAAAAAA", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BBBB           ")
                .aisle("BBBBAAAAAAAAAAA", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC CCCCC       ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC CCCCC       ", "BC C           ", "BC C           ", "BBBB           ")
                .aisle("BBBBAAAAAAAAAAA", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BBBB           ")
                .aisle("SBBBAAAAAAAAAAA", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BBBB           ")
                .aisle("BBBBAAAAAAAAAAA", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BBBB           ")
                .aisle("BBBBAAAAAAAAAAA", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC CCCCC       ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC C           ", "BC CCCCC       ", "BC C           ", "BC C           ", "BBBB           ")
                .aisle("BBBBAAAAAAAAAAA", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BCDC           ", "BBBB           ")
                .aisle("BBBBAAAAAAAAAAA", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BCCC           ", "BBBB           ")
                .aisle("AAAAAAAAAAAAAAA", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .where('S', selfPredicate())
                .where(' ', any())
                .where('A', blocks(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH)))
                .where('B',
                        states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                                .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1))
                                .or(abilities(DrtMultiblockAbility.IMPORT_ITEM_FLUID).setMaxGlobalLimited(1)).or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                )
                .where('C', frames(Materials.Steel))
                .where('D', states(MetaBlocks.BOILER_CASING.getStateFromMeta(1)))
                .build();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityRocketLaunchPad(this.metaTileEntityId);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            // 获取玩家或实体的当前位置
            Entity entity = getWorld().getClosestPlayer(100, 64, 200, -1, false); // 替换为你的实体获取逻辑
            if (entity != null) {
                BlockPos centerPos = getPos();
                double leftRange = 5.0; // 左侧 5 格
                double rightRange = 5.0; // 右侧 5 格
                double backRange = 15.0; // 反方向 15 格
                double heightRange = 16.0; // 高度范围 8 格（上下各 8 格，总共 16 格）
                AxisAlignedBB aabb = new AxisAlignedBB(
                        centerPos.getX() - leftRange, centerPos.getY(), centerPos.getZ() + backRange,
                        centerPos.getX() + rightRange, centerPos.getY() + heightRange, centerPos.getZ()
                );
                for (Entity targetEntity : getWorld().getEntitiesWithinAABB(EntityAdvancedRocket.class, aabb)) {
                    if (targetEntity instanceof EntityAdvancedRocket rocket) { // 判断是否为 EntityAdvancedRocket
                        rocket.DimId = -1;
                        rocket.fuel_amount = 1000;
                        if (rocket.isBeingRidden()) {
                            List<Entity> passengers = rocket.getPassengers();
                            for (Entity passenger : passengers) {
                                if (passenger instanceof EntityPlayer player) {
                                    teleportPlayerToDimension(player, rocket.DimId);
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    // 维度切换方法
    private void teleportPlayerToDimension(EntityPlayer player, int targetDimId) {
        // 判断玩家是否已经在目标维度
        if (player.world.provider.getDimension() == targetDimId) {
            return;
        }

        // 切换维度
        WorldServer targetWorld = player.getServer().getWorld(targetDimId);
        if (targetWorld != null) {
            player.changeDimension(targetDimId, (world, entity, yaw) -> {
                // 在目标维度中执行必要的初始化逻辑（例如火箭落地）
                entity.setPosition(0, 120, 0); // 设置玩家在目标维度中的位置
            });
        } else {
            player.sendMessage(new TextComponentString("目标维度不存在！"));
        }
    }
}
