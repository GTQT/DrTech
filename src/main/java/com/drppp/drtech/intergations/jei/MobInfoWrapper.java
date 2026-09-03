package com.drppp.drtech.intergations.jei;

import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * mob_info 分类的配方包装器（布局/交互语义照 JustEnoughDrops-1.1.1 的 MobInfoWrapper 移植）：
 * 左侧裁剪区实时渲染实体模型，右侧文字行 + 掉落网格；
 * 悬停掉落物时在 tooltip 追加"数量 (概率%) + 条件说明"。
 */
public class MobInfoWrapper implements IRecipeWrapper, ITooltipCallback<ItemStack> {

    private static final Logger LOGGER = LogManager.getLogger("drtech.mob_info");

    private static final int SCISSOR_X = 1;
    private static final int SCISSOR_Y = 16;
    private static final int SCISSOR_W = 73;
    private static final int SCISSOR_H = 111;
    private static final int ENTITY_X = 37;
    private static final int ENTITY_Y = 100;
    private static final int TEXT_X = 98;
    private static final int TEXT_Y_XP = 3;
    private static final int NAME_Y = 3;

    private final MobInfoEntry entry;
    private final ItemStack spawnEgg;
    private final float scale;
    private final int offsetY;

    public MobInfoWrapper(MobInfoEntry entry, ItemStack spawnEgg) {
        this.entry = entry;
        this.spawnEgg = spawnEgg;
        EntityLiving entity = entry.getEntity();
        this.scale = getScale(entity);
        this.offsetY = getOffsetY(entity);
    }

    public MobInfoEntry getEntry() {
        return entry;
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, spawnEgg);
        List<ItemStack> outputs = new ArrayList<>();
        for (LootDropInfo drop : entry.getDrops()) {
            outputs.addAll(drop.getDrops());
        }
        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight,
                         int mouseX, int mouseY) {
        EntityLiving entity = entry.getEntity();

        // ---- 实体渲染区（scissor 裁剪） ----
        MobRenderUtil.scissor(SCISSOR_X, SCISSOR_Y, SCISSOR_W, SCISSOR_H);
        boolean wasHanging = false;
        try {
            if (entity instanceof EntityBat) {
                wasHanging = ((EntityBat) entity).getIsBatHanging();
                ((EntityBat) entity).setIsBatHanging(false);
            }
            MobRenderUtil.renderEntity(ENTITY_X, ENTITY_Y - offsetY, scale,
                    38 - mouseX, 70 - offsetY - mouseY, entity);
        } catch (Exception e) {
            LOGGER.debug("Failed to render entity {}: {}", entity.getClass().getName(), e.getMessage());
            String failed = I18n.format("drtech.mob_info.render_failed");
            MobRenderUtil.drawText(failed,
                    ENTITY_X - MobRenderUtil.getTextWidth(failed) / 2, 50);
        } finally {
            if (entity instanceof EntityBat) {
                ((EntityBat) entity).setIsBatHanging(wasHanging);
            }
            MobRenderUtil.stopScissor();
        }

        // ---- 名字（模型上方居中） ----
        String name = entry.getDisplayName();
        if (name == null || name.isEmpty() || "null".equalsIgnoreCase(name)) {
            name = I18n.format("drtech.mob_info.unknown");
        }
        MobRenderUtil.drawText(name,
                ENTITY_X - MobRenderUtil.getTextWidth(name) / 2, NAME_Y);

        // ---- 经验信息（右上） ----
        MobRenderUtil.drawText(I18n.format("drtech.mob_info.exp") + ": " + entry.getXp(), TEXT_X, TEXT_Y_XP);

        // ---- 无法识别横幅 ----
        if (entry.isUnrecognized() && entry.getDrops().isEmpty()) {
            String unrecognized = I18n.format("drtech.mob_info.unrecognized");
            int x = 136 - MobRenderUtil.getTextWidth(unrecognized) / 2;
            MobRenderUtil.drawText(unrecognized, x, 78);
        }
    }

    @Override
    public void onTooltip(int slotIndex, boolean input, @Nonnull ItemStack stack,
                          @Nonnull List<String> tooltip) {
        if (input) return;
        for (LootDropInfo drop : entry.getDrops()) {
            boolean hitSmelted = drop.canBeCooked() && drop.smeltedItem.isItemEqual(stack);
            if (drop.item.isItemEqual(stack) || hitSmelted) {
                tooltip.add(drop.toStringLine());
                List<String> lines = new ArrayList<>();
                for (DropCondition condition : drop.getConditions()) {
                    lines.add(condition.toLine());
                }
                if (hitSmelted) {
                    lines.add(DropCondition.BURNING.toLine());
                }
                tooltip.addAll(lines);
                return;
            }
        }
    }

    // ---------------- 缩放/偏移表（照 JED） ----------------

    private float getScale(EntityLiving entity) {
        if (entity instanceof EntityGiantZombie) return 18.0F;
        float width = entity.width;
        float height = entity.height;
        if (width > height) {
            // 宽型生物（蜘蛛、史莱姆类等）
            if (width < 1.0F) return 38.0F;
            if (width < 2.0F) return 27.0F;
            if (width < 3.0F) return 13.0F;
            return 9.0F;
        }
        // 高度主导
        if (height < 0.9F) return 50.0F;
        if (height < 1.0F) return 35.0F;
        if (height < 1.8F) return 33.0F;
        if (height < 2.0F) return 32.0F;
        if (height < 3.0F) return 24.0F;
        if (height < 4.0F) return 20.0F;
        return 10.0F;
    }

    private int getOffsetY(EntityLiving entity) {
        if (entity instanceof EntitySquid) return 20;
        if (entity instanceof EntityGiantZombie) return 35;
        if (entity instanceof EntityWitch) return -10;
        if (entity instanceof EntityGhast) return 15;
        if (entity instanceof EntityWither) return -15;
        if (entity instanceof EntityDragon) return 15;
        if (entity instanceof EntityEnderman) return -10;
        if (entity instanceof EntityGolem) return -10;
        if (entity instanceof EntityAnimal) return -20;
        if (entity instanceof EntityVillager) return -15;
        if (entity instanceof EntityVindicator) return -15;
        if (entity instanceof EntityEvoker) return -10;
        if (entity instanceof EntityBlaze) return -10;
        if (entity instanceof EntityCreeper) return -15;
        return 0;
    }
}
