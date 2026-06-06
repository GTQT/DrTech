package com.drppp.drtech.common.Items.MetaItems.behaviors;

import com.drppp.drtech.Client.Sound.SoundManager;
import com.drppp.drtech.api.Utils.RewardBoxManager;
import com.drppp.drtech.api.Utils.RewardEntry;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class LootTableBehavior implements IItemBehaviour {

    private final List<RewardEntry> table;
    private final Random random = new Random();

    public LootTableBehavior(@Nonnull String tableName) {
        List<RewardEntry> entries = RewardBoxManager.getRewardTable(tableName);
        this.table = entries != null ? entries : java.util.Collections.emptyList();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!world.isRemote && !table.isEmpty()) {
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }

            world.playSound(null, player.posX, player.posY, player.posZ,
                    SoundManager.lootBag, SoundCategory.PLAYERS, 1.0F, 1.0F);

            boolean dropped = false;
            for (RewardEntry entry : table) {
                if (random.nextInt(100) < entry.getProbability()) {
                    ItemStack prize = entry.getItemStack().copy();
                    if (!prize.isEmpty()) {
                        player.dropItem(prize, false);
                        dropped = true;
                    }
                }
            }

            if (!dropped) {
                RewardEntry fallback = table.get(random.nextInt(table.size()));
                ItemStack prize = fallback.getItemStack().copy();
                if (!prize.isEmpty()) {
                    player.dropItem(prize, false);
                }
            }
        }

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("behavior.loot_table.tooltip.1"));
        if (!table.isEmpty()) {
            lines.add(I18n.format("behavior.loot_table.tooltip.2", table.size()));
        }
    }
}
