package com.drppp.drtech.lootgames.config;

import net.minecraftforge.common.config.Config;
import com.drppp.drtech.Tags;

@Config(modid = Tags.MODID, name = "DrTech_LootGames_Minesweeper")
@Config.LangKey("config.drtech.lootgames.minesweeper")
public class LGConfigMinesweeper {

    @Config.Comment("Base board size for minesweeper level 1")
    @Config.RangeInt(min = 3, max = 25)
    public static int boardSizeLevel1 = 7;

    @Config.Comment("Base board size for minesweeper level 2")
    @Config.RangeInt(min = 3, max = 25)
    public static int boardSizeLevel2 = 9;

    @Config.Comment("Base board size for minesweeper level 3")
    @Config.RangeInt(min = 3, max = 25)
    public static int boardSizeLevel3 = 11;

    @Config.Comment("Base board size for minesweeper level 4")
    @Config.RangeInt(min = 3, max = 25)
    public static int boardSizeLevel4 = 13;

    @Config.Comment("Bomb count for level 1")
    @Config.RangeInt(min = 1, max = 100)
    public static int bombCountLevel1 = 6;

    @Config.Comment("Bomb count for level 2")
    @Config.RangeInt(min = 1, max = 200)
    public static int bombCountLevel2 = 12;

    @Config.Comment("Bomb count for level 3")
    @Config.RangeInt(min = 1, max = 300)
    public static int bombCountLevel3 = 20;

    @Config.Comment("Bomb count for level 4")
    @Config.RangeInt(min = 1, max = 500)
    public static int bombCountLevel4 = 30;

    @Config.Comment("Max attempts before game over")
    @Config.RangeInt(min = 1, max = 10)
    public static int maxAttempts = 3;

    @Config.Comment("Detonation time in ticks before bombs explode")
    @Config.RangeInt(min = 10, max = 200)
    public static int detonationTime = 60;
}
