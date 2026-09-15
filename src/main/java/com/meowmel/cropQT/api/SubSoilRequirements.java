package com.meowmel.cropQT.api;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * 预定义的底土要求。
 *
 * <p>每个成员都同时登记了 GT 材料与矿物词典，所以 GT 的压缩块、别的 mod 的矿石、
 * 原版方块都能被同一份要求认出来。例如 {@link #copper} 会命中：
 * <ul>
 *     <li>GT 的铜压缩块、铜矿石（走材料匹配）</li>
 *     <li>任何注册了 {@code oreCopper} / {@code blockCopper} 的方块（走矿物词典）</li>
 * </ul>
 *
 * <p>需求没覆盖到的材料照 CropsNH 的做法<b>留空</b>——没有对应要求的作物就是不挑底土，
 * 不需要为它们造占位实例。
 *
 * @see SubSoilRequirement
 */
public final class SubSoilRequirements {

    // ==================== 金属 ====================

    public static final SubSoilRequirement copper = metal("copper", "Copper", Materials.Copper);
    public static final SubSoilRequirement tin = metal("tin", "Tin", Materials.Tin);
    public static final SubSoilRequirement iron = metal("iron", "Iron", Materials.Iron);
    public static final SubSoilRequirement lead = metal("lead", "Lead", Materials.Lead);
    public static final SubSoilRequirement silver = metal("silver", "Silver", Materials.Silver);
    public static final SubSoilRequirement gold = metal("gold", "Gold", Materials.Gold);
    public static final SubSoilRequirement aluminium = metal("aluminium", "Aluminium", Materials.Aluminium);
    public static final SubSoilRequirement nickel = metal("nickel", "Nickel", Materials.Nickel);
    public static final SubSoilRequirement platinum = metal("platinum", "Platinum", Materials.Platinum);
    public static final SubSoilRequirement tungsten = metal("tungsten", "Tungsten", Materials.Tungsten);
    public static final SubSoilRequirement titanium = metal("titanium", "Titanium", Materials.Titanium);
    public static final SubSoilRequirement manganese = metal("manganese", "Manganese", Materials.Manganese);
    public static final SubSoilRequirement uranium = metal("uranium", "Uranium238", Materials.Uranium238);
    public static final SubSoilRequirement zinc = metal("zinc", "Zinc", Materials.Zinc);

    // ==================== 非金属 ====================

    public static final SubSoilRequirement coal = both("coal", "Coal", Blocks.COAL_ORE, Blocks.COAL_BLOCK);
    public static final SubSoilRequirement diamond = both("diamond", "Diamond", Blocks.DIAMOND_ORE, Blocks.DIAMOND_BLOCK);
    public static final SubSoilRequirement emerald = both("emerald", "Emerald", Blocks.EMERALD_ORE, Blocks.EMERALD_BLOCK);
    public static final SubSoilRequirement lapis = both("lapis", "Lapis", Blocks.LAPIS_ORE, Blocks.LAPIS_BLOCK);
    public static final SubSoilRequirement redstone = SubSoilRequirement.of("redstone")
            .addBlock(Blocks.REDSTONE_ORE)
            .addBlock(Blocks.LIT_REDSTONE_ORE)
            .addBlock(Blocks.REDSTONE_BLOCK)
            .addOreDict("oreRedstone")
            .addOreDict("blockRedstone");
    public static final SubSoilRequirement quartz = SubSoilRequirement.of("quartz")
            .addBlock(Blocks.QUARTZ_ORE)
            .addBlock(Blocks.QUARTZ_BLOCK)
            .addOreDict("oreQuartz")
            .addOreDict("blockQuartz");
    public static final SubSoilRequirement thulium = metal("thulium", "Thulium", Materials.Thulium);
    public static final SubSoilRequirement salt = SubSoilRequirement.of("salt")
            .addMaterial(Materials.Salt)
            .addMaterial(Materials.RockSalt)
            .addOreDict("oreSalt")
            .addOreDict("blockSalt");
    public static final SubSoilRequirement mica = SubSoilRequirement.of("mica")
            .addMaterial(Materials.Mica)
            .addOreDict("oreMica")
            .addOreDict("blockMica");
    public static final SubSoilRequirement certusQuartz = SubSoilRequirement.of("certus_quartz")
            .addMaterial(Materials.CertusQuartz)
            .addOreDict("oreCertusQuartz")
            .addOreDict("blockCertusQuartz");

    // ==================== 石头变种（石中百合用） ====================

    public static final SubSoilRequirement stone = stoneVariant("stone", Materials.Stone);
    public static final SubSoilRequirement granite = stoneVariant("granite", Materials.Granite);
    public static final SubSoilRequirement blackGranite = stoneVariant("black_granite", Materials.GraniteBlack);
    public static final SubSoilRequirement redGranite = stoneVariant("red_granite", Materials.GraniteRed);
    public static final SubSoilRequirement marble = stoneVariant("marble", Materials.Marble);
    public static final SubSoilRequirement basalt = stoneVariant("basalt", Materials.Basalt);
    public static final SubSoilRequirement andesite = stoneVariant("andesite", Materials.Andesite);
    public static final SubSoilRequirement diorite = stoneVariant("diorite", Materials.Diorite);

    // ==================== 其他 ====================

    public static final SubSoilRequirement obsidian = SubSoilRequirement.of("obsidian")
            .addBlock(Blocks.OBSIDIAN);
    public static final SubSoilRequirement netherrack = SubSoilRequirement.of("netherrack")
            .addBlock(Blocks.NETHERRACK)
            .addMaterial(Materials.Netherrack);
    public static final SubSoilRequirement endStone = SubSoilRequirement.of("end_stone")
            .addBlock(Blocks.END_STONE)
            .addMaterial(Materials.Endstone);
    public static final SubSoilRequirement soulSand = SubSoilRequirement.of("soul_sand")
            .addBlock(Blocks.SOUL_SAND)
            .addOreDict("soulSand");
    public static final SubSoilRequirement clay = SubSoilRequirement.of("clay")
            .addBlock(Blocks.CLAY)
            .addMaterial(Materials.Clay);
    public static final SubSoilRequirement sand = SubSoilRequirement.of("sand")
            .addBlock(Blocks.SAND)
            .addOreDict("sand");
    public static final SubSoilRequirement glowstone = SubSoilRequirement.of("glowstone")
            .addBlock(Blocks.GLOWSTONE)
            .addMaterial(Materials.Glowstone);
    public static final SubSoilRequirement bedrock = SubSoilRequirement.of("bedrock")
            .addBlock(Blocks.BEDROCK);
    public static final SubSoilRequirement magma = SubSoilRequirement.of("magma")
            .addBlock(Blocks.MAGMA);
    public static final SubSoilRequirement water = SubSoilRequirement.of("water", new ItemStack(Items.WATER_BUCKET))
            .addBlock(Blocks.WATER)
            .addBlock(Blocks.FLOWING_WATER);

    /**
     * 机械脑：硅 / 磷 / 钠夸德 / 中子素，四种压缩块任意一种都算满足。
     *
     * <p>招牌取硅块——它只是四个之一，提示文本会说「缺少 Silicon Block」，
     * 但实际放另外三种也能长。这是多选要求的固有取舍。
     */
    public static final SubSoilRequirement mechBrain = SubSoilRequirement.of("mech_brain", blockFormOf(Materials.Silicon))
            .addMaterial(Materials.Silicon)
            .addMaterial(Materials.Phosphorus)
            .addMaterial(Materials.Naquadah)
            .addMaterial(Materials.Neutronium);

    // ==================== 按注册名引用 ====================
    //
    // 这些方块编译期拿不到类：要么属于可能没装的 mod，要么是 GT 按材料生成的。
    // 用注册名匹配——方块不存在时永不命中，不会崩。

    public static final SubSoilRequirement fleshBlock = SubSoilRequirement.of("flesh_block")
            .addBlockId("thaumcraft:flesh_block");
    public static final SubSoilRequirement livingwood = SubSoilRequirement.of("livingwood")
            .addBlockId("botania:livingwood");
    public static final SubSoilRequirement livingrock = SubSoilRequirement.of("livingrock")
            .addBlockId("botania:livingrock");
    public static final SubSoilRequirement oilSands = SubSoilRequirement.of("oil_sands")
            .addBlockId("gregtech:ore_oilsands_0");
    /** 末影花：GT 的某个压缩块。注册名里的编号对不上材料，只能照原名引用。 */
    public static final SubSoilRequirement compressed26 = SubSoilRequirement.of("compressed_26")
            .addBlockId("gregtech:meta_block_compressed_26");
    /** 凋零花 / 星之疣：GT 的另一个压缩块（带元数据）。 */
    public static final SubSoilRequirement compressed100Meta2 = SubSoilRequirement.of("compressed_100_2")
            .addBlockId("gregtech:meta_block_compressed_100:2");

    private SubSoilRequirements() {
    }

    // ==================== 构造辅助 ====================
    //
    // 每个辅助都显式挑一个「代表物品」当招牌。挑哪个形态由这里决定，不去猜。

    /** 金属：招牌用压缩块，匹配走 材料 + {@code oreX} + {@code blockX} 三路。 */
    private static SubSoilRequirement metal(String name, String oreDictName, Material material) {
        return SubSoilRequirement.of(name, blockFormOf(material))
                .addMaterial(material)
                .addOreDict("ore" + oreDictName)
                .addOreDict("block" + oreDictName);
    }

    /** 宝石类：招牌用矿石，匹配走两个原版方块 + {@code oreX} + {@code blockX}。 */
    private static SubSoilRequirement both(String name, String oreDictName, Block ore, Block block) {
        return SubSoilRequirement.of(name, new ItemStack(ore))
                .addBlock(ore)
                .addBlock(block)
                .addOreDict("ore" + oreDictName)
                .addOreDict("block" + oreDictName);
    }

    /**
     * 石头变种：招牌用 GT 的 {@code OrePrefix.stone} 方块（原版石头带 meta 的那种）。
     *
     * <p>石头类材料没有 INGOT/GEM，所以没有压缩块——只有 {@code OrePrefix.stone} 这一条路。
     */
    private static SubSoilRequirement stoneVariant(String name, Material material) {
        return SubSoilRequirement.of(name, OreDictUnifier.get(OrePrefix.stone, material))
                .addMaterial(material);
    }

    /** 取材料的压缩块形态；拿不到就返回空栈，让显示名回退到内部名。 */
    private static ItemStack blockFormOf(Material material) {
        return OreDictUnifier.get(OrePrefix.block, material);
    }
}
