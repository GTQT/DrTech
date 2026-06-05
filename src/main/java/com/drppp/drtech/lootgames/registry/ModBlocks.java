package com.drppp.drtech.lootgames.registry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.drppp.drtech.Tags;
import com.drppp.drtech.lootgames.api.block.BlockSmartSubordinate;
import com.drppp.drtech.lootgames.block.BlockDungeonBricks;
import com.drppp.drtech.lootgames.block.BlockDungeonLamp;
import com.drppp.drtech.lootgames.block.BlockPuzzleMaster;
import com.drppp.drtech.lootgames.minigame.gameoflight.BlockGOLMaster;
import com.drppp.drtech.lootgames.minigame.gameoflight.BlockGOLSubordinate;
import com.drppp.drtech.lootgames.minigame.gameoflight.TileEntityGOLMaster;
import com.drppp.drtech.lootgames.minigame.minesweeper.block.BlockMSActivator;
import com.drppp.drtech.lootgames.minigame.minesweeper.block.BlockMSMaster;
import com.drppp.drtech.lootgames.minigame.minesweeper.tileentity.TileEntityMSMaster;
import com.drppp.drtech.lootgames.tileentity.TileEntityPuzzleMaster;

public class ModBlocks {
    public static final BlockDungeonBricks DUNGEON_BRICKS = new BlockDungeonBricks();
    public static final BlockDungeonLamp DUNGEON_LAMP = new BlockDungeonLamp();
    public static final BlockPuzzleMaster PUZZLE_MASTER = new BlockPuzzleMaster();
    public static final BlockGOLSubordinate GOL_SUBORDINATE = new BlockGOLSubordinate();
    public static final BlockGOLMaster GOL_MASTER = new BlockGOLMaster();
    public static final BlockMSActivator MS_ACTIVATOR = new BlockMSActivator();
    public static final BlockMSMaster MS_MASTER = new BlockMSMaster();
    public static final BlockSmartSubordinate SMART_SUBORDINATE = new BlockSmartSubordinate();

    public static void registerBlocks(net.minecraftforge.event.RegistryEvent.Register<net.minecraft.block.Block> event) {
        event.getRegistry().registerAll(
                DUNGEON_BRICKS,
                DUNGEON_LAMP,
                PUZZLE_MASTER,
                GOL_SUBORDINATE,
                GOL_MASTER,
                MS_ACTIVATOR,
                MS_MASTER,
                SMART_SUBORDINATE
        );
    }

    public static void registerItemBlocks(net.minecraftforge.event.RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                new ItemMultiTexture(DUNGEON_BRICKS, DUNGEON_BRICKS,
                        stack -> BlockDungeonBricks.EnumType.byMetadata(stack.getMetadata()).getName())
                        .setRegistryName(DUNGEON_BRICKS.getRegistryName()),
                new ItemMultiTexture(DUNGEON_LAMP, DUNGEON_LAMP,
                        stack -> stack.getMetadata() == 0 ? "dungeon_lamp" : "dungeon_lamp_broken")
                        .setRegistryName(DUNGEON_LAMP.getRegistryName()),
                new ItemBlock(PUZZLE_MASTER).setRegistryName(PUZZLE_MASTER.getRegistryName()),
                new ItemBlock(GOL_MASTER).setRegistryName(GOL_MASTER.getRegistryName()),
                new ItemBlock(MS_ACTIVATOR).setRegistryName(MS_ACTIVATOR.getRegistryName())
        );
    }

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityPuzzleMaster.class, Tags.MODID + ":puzzle_master");
        GameRegistry.registerTileEntity(TileEntityGOLMaster.class, Tags.MODID + ":gol_master");
        GameRegistry.registerTileEntity(TileEntityMSMaster.class, Tags.MODID + ":ms_master");
    }

    @SideOnly(Side.CLIENT)
    public static void registerRenderers(net.minecraftforge.client.event.ModelRegistryEvent event) {
        com.drppp.drtech.DrTechModelRegister.onModelRegistration();
    }
}
