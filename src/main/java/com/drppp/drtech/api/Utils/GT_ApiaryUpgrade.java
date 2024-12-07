package com.drppp.drtech.api.Utils;

import com.drppp.drtech.common.Items.MetaItems.MyMetaItems;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityIndustrialApiary;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public enum  GT_ApiaryUpgrade {
    speed1(UNIQUE_INDEX.SPEED_UPGRADE, 29, 1, (mods, n) -> mods.maxSpeed = 1),
    speed2(UNIQUE_INDEX.SPEED_UPGRADE, 30, 1, (mods, n) -> mods.maxSpeed = 2),
    speed3(UNIQUE_INDEX.SPEED_UPGRADE, 31, 1, (mods, n) -> mods.maxSpeed = 3),
    speed4(UNIQUE_INDEX.SPEED_UPGRADE, 32, 1, (mods, n) -> mods.maxSpeed = 4),
    speed5(UNIQUE_INDEX.SPEED_UPGRADE, 33, 1, (mods, n) -> mods.maxSpeed = 5),
    speed6(UNIQUE_INDEX.SPEED_UPGRADE, 34, 1, (mods, n) -> mods.maxSpeed = 6),
    speed7(UNIQUE_INDEX.SPEED_UPGRADE, 35, 1, (mods, n) -> mods.maxSpeed = 7),
    speed8(UNIQUE_INDEX.SPEED_UPGRADE, 36, 1, (mods, n) -> mods.maxSpeed = 8),
    speed8upgraded(UNIQUE_INDEX.SPEED_UPGRADE, 37, 1, (mods, n) -> {
        mods.maxSpeed = 8;
        mods.production = 17.19926784f;
        mods.energy *= 14.75;
    }),
    production(UNIQUE_INDEX.PRODUCTION_UPGRADE, 38, 8, (mods, n) -> {
        mods.production = 4.f * (float) Math.pow(1.2d, n);
        mods.energy *= Math.pow(1.4f, n);
    }),
    plains(UNIQUE_INDEX.PLAINS_UPGRADE, 39, 1, (mods, n) -> {
        mods.biomeOverride = Biomes.PLAINS;
        mods.energy *= 1.2f;
    }),
    light(UNIQUE_INDEX.LIGHT_UPGRADE, 40, 1, (mods, n) -> {
        mods.isSelfLighted = true;
        mods.energy *= 1.05f;
    }),
    flowering(UNIQUE_INDEX.FLOWERING_UPGRADE, 41, 8, (mods, n) -> {
        mods.flowering *= Math.pow(1.2f, n);
        mods.energy *= Math.pow(1.1f, n);
    }),
    winter(UNIQUE_INDEX.WINTER_UPGRADE, 42, 1, (mods, n) -> {
        mods.biomeOverride = Biomes.TAIGA;
        mods.energy *= 1.5f;
    }),
    dryer(UNIQUE_INDEX.DRYER_UPGRADE, 43, 16, (mods, n) -> {
        mods.humidity -= 0.125f * n;
        mods.energy *= Math.pow(1.025f, n);
    }),
    automation(UNIQUE_INDEX.AUTOMATION_UPGRADE, 44, 1, (mods, n) -> {
        mods.isAutomated = true;
        mods.energy *= 1.1f;
    }),
    humidifier(UNIQUE_INDEX.HUMIDIFIER_UPGRADE, 45, 16, (mods, n) -> {
        mods.humidity += 0.125f * n;
        mods.energy *= Math.pow(1.05f, n);
    }),
    hell(UNIQUE_INDEX.HELL_UPGRADE, 46, 1, (mods, n) -> {
        mods.biomeOverride = Biomes.HELL;
        mods.energy *= 1.5f;
    }),
    pollen(UNIQUE_INDEX.POLLEN_UPGRADE, 47, 1, (mods, n) -> {
        mods.flowering = 0f;
        mods.energy *= 1.3f;
    }),
    desert(UNIQUE_INDEX.DESERT_UPGRADE, 48, 1, (mods, n) -> {
        mods.biomeOverride = Biomes.DESERT;
        mods.energy *= 1.2f;
    }),
    cooler(UNIQUE_INDEX.COOLER_UPGRADE, 49, 16, (mods, n) -> {
        mods.temperature -= 0.125f * n;
        mods.energy *= Math.pow(1.025f, n);
    }),
    lifespan(UNIQUE_INDEX.LIFESPAN_UPGRADE, 50, 4, (mods, n) -> {
        mods.lifespan /= Math.pow(1.5f, n);
        mods.energy *= Math.pow(1.05f, n);
    }),
    seal(UNIQUE_INDEX.SEAL_UPGRADE, 51, 1, (mods, n) -> {
        mods.isSealed = true;
        mods.energy *= 1.05f;
    }),
    stabilizer(UNIQUE_INDEX.STABILIZER_UPGRADE, 52, 1, (mods, n) -> {
        mods.geneticDecay = 0f;
        mods.energy *= 2.50f;
    }),
    jungle(UNIQUE_INDEX.JUNGLE_UPGRADE, 53, 1, (mods, n) -> {
        mods.biomeOverride = Biomes.JUNGLE;
        mods.energy *= 1.20f;
    }),
    territory(UNIQUE_INDEX.TERRITORY_UPGRADE, 54, 4, (mods, n) -> {
        mods.territory *= Math.pow(1.5f, n);
        mods.energy *= Math.pow(1.05f, n);
    }),
    ocean(UNIQUE_INDEX.OCEAN_UPGRADE, 55, 1, (mods, n) -> {
        mods.biomeOverride = Biomes.OCEAN;
        mods.energy *= 1.20f;
    }),
    sky(UNIQUE_INDEX.SKY_UPGRADE, 56, 1, (mods, n) -> {
        mods.isSunlightSimulated = true;
        mods.energy *= 1.05f;
    }),
    heater(UNIQUE_INDEX.HEATER_UPGRADE, 57, 16, (mods, n) -> {
        mods.temperature += 0.125f * n;
        mods.energy *= Math.pow(1.025f, n);
    }),
    sieve(UNIQUE_INDEX.SIEVE_UPGRADE, 58, 1, (mods, n) -> {
        mods.isCollectingPollen = true;
        mods.energy *= 1.05f;
    }),
    unlight(UNIQUE_INDEX.LIGHT_UPGRADE, 59, 1, (mods, n) -> {
        mods.isSelfUnlighted = false;
        mods.energy *= 1.05f;
    });

    private enum UNIQUE_INDEX {

        SPEED_UPGRADE,
        PRODUCTION_UPGRADE,
        PLAINS_UPGRADE,
        LIGHT_UPGRADE, // also unlight
        FLOWERING_UPGRADE,
        WINTER_UPGRADE,
        DRYER_UPGRADE,
        AUTOMATION_UPGRADE,
        HUMIDIFIER_UPGRADE,
        HELL_UPGRADE,
        POLLEN_UPGRADE,
        DESERT_UPGRADE,
        COOLER_UPGRADE,
        LIFESPAN_UPGRADE,
        SEAL_UPGRADE,
        STABILIZER_UPGRADE,
        JUNGLE_UPGRADE,
        TERRITORY_UPGRADE,
        OCEAN_UPGRADE,
        SKY_UPGRADE,
        HEATER_UPGRADE,
        SIEVE_UPGRADE,;

        void apply(Consumer<GT_ApiaryUpgrade> fn) {
            UNIQUE_UPGRADE_LIST.get(this)
                    .forEach(fn);
        }
    }

    private static final EnumMap<UNIQUE_INDEX, ArrayList<GT_ApiaryUpgrade>> UNIQUE_UPGRADE_LIST = new EnumMap<>(
            UNIQUE_INDEX.class);

    private  int meta = 0;
    private  int maxnumber = 1;
    private  final UNIQUE_INDEX unique_index ;
    private  final BiConsumer<MetaTileEntityIndustrialApiary.GT_ApiaryModifier, Integer> applier;

    GT_ApiaryUpgrade(UNIQUE_INDEX unique_index, int meta, int maxnumber,
                     BiConsumer<MetaTileEntityIndustrialApiary.GT_ApiaryModifier, Integer> applier) {
        this.unique_index = unique_index;
        this.meta = meta;
        this.maxnumber = maxnumber;
        this.applier = applier;
    }

    private void setup_static_variables() {
        quickLookup.put(this.meta, this);
        ArrayList<GT_ApiaryUpgrade> un = UNIQUE_UPGRADE_LIST.get(this.unique_index);
            un = new ArrayList<>(1);
            UNIQUE_UPGRADE_LIST.put(this.unique_index, un);
        un.add(this);
    }

    public static GT_ApiaryUpgrade getUpgrade(ItemStack s) {
        if (s == null) return null;
        if (!isUpgrade(s)) return null;
        return quickLookup.get(s.getItemDamage());
    }

    public int getMaxNumber() {
        return maxnumber;
    }

    public void applyModifiers(MetaTileEntityIndustrialApiary.GT_ApiaryModifier mods, ItemStack stack) {
        if (applier != null) applier.accept(mods, stack.getCount());
    }

    public ItemStack get(int count) {
        return new ItemStack(MyMetaItems.UPGRADE_PLAIN.getMetaItem(), count, meta);
    }

    public static boolean isUpgrade(ItemStack s) {
        return s.getItem()==MyMetaItems.UPGRADE_SPEED1.getMetaItem() && (s.getMetadata()>=29 && s.getMetadata()<=59);
    }

    private static final HashMap<Integer, GT_ApiaryUpgrade> quickLookup = new HashMap<>();

    static {
        EnumSet.allOf(GT_ApiaryUpgrade.class)
                .forEach(GT_ApiaryUpgrade::setup_static_variables);
    }
}
