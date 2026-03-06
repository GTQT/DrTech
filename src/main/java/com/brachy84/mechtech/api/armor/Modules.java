package com.brachy84.mechtech.api.armor;

import com.brachy84.mechtech.api.armor.modules.*;
import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTLog;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.logging.log4j.Level;

import java.rmi.AlreadyBoundException;

public class Modules {

    private static final Int2ObjectOpenHashMap<IModule> REGISTRY = new Int2ObjectOpenHashMap<>();
    private static final Object2IntOpenHashMap<IModule> BACK_REGISTRY = new Object2IntOpenHashMap<>();
    private static final Int2ObjectOpenHashMap<MaterialArmorModuleBuilder> ARMOR_MODULES = new Int2ObjectOpenHashMap<>();

    public static Iterable<IModule> getRegisteredModules() {
        return REGISTRY.values();
    }

    public static Int2ObjectMap<MaterialArmorModuleBuilder> getArmorModules() {
        return Int2ObjectMaps.unmodifiable(ARMOR_MODULES);
    }

    public static final IModule NIGHT_VISION = new NightVision();
    public static final IModule SOLAR_GEN_I = new SolarGen(GTValues.V[1], 1);
    public static final IModule SOLAR_GEN_II = new SolarGen(GTValues.V[2], 2);
    public static final IModule SOLAR_GEN_III = new SolarGen(GTValues.V[3], 3);
    public static final IModule JETPACK = new JetpackModule();
    public static final IModule ADVANCED_JETPACK = new AdvancedJetpack();
    public static final IModule SHOCK_ABSORBER = new ShockAbsorber();
    public static final IModule INSULATOR = new Insulator();
    public static final IModule BINOCULARS = new Binoculars();
    public static final IModule AUTO_FEEDER = new AutoFeeder();
    public static final IModule TESLA_COIL = new TeslaCoil();
    public static final IModule OXYGEN_MASK = new OxygenMask();
    public static final IModule RECEIVER_MODULE = new ReceiverModule();

    static {
        registerModule(0, NIGHT_VISION);
        registerModule(1, SOLAR_GEN_I);
        registerModule(2, SOLAR_GEN_II);
        registerModule(3, SOLAR_GEN_III);
        registerModule(4, JETPACK);
        registerModule(5, ADVANCED_JETPACK);
        registerModule(6, SHOCK_ABSORBER);
        registerModule(7, INSULATOR);
        registerModule(8, BINOCULARS);
        registerModule(9, AUTO_FEEDER);
        registerModule(10, TESLA_COIL);
        registerModule(11, OXYGEN_MASK);
        registerModule(12, RECEIVER_MODULE);

        materialArmorBuilder(1000, Materials.Aluminium)
                .armor(3.4)
                .registerModule();
        materialArmorBuilder(1001, Materials.Copper)
                .armor(3.6)
                .registerModule();
        materialArmorBuilder(1002, Materials.Bronze)
                .armor(3.6)
                .registerModule();
        materialArmorBuilder(1003, Materials.Iron)
                .armor(3.75) // Full vanilla has 15 Armor -> 15 / 4 = 3.75
                .registerModule();
        materialArmorBuilder(1004, Materials.Cobalt)
                .armor(3.9, 0.1)
                .registerModule();
        materialArmorBuilder(1005, Materials.Steel)
                .armor(3.95)
                .registerModule();
        materialArmorBuilder(1006, Materials.StainlessSteel)
                .armor(4.2, 0.15)
                .registerModule();
        materialArmorBuilder(1007, Materials.Titanium)
                .armor(4.4, 0.5)
                .registerModule();
        materialArmorBuilder(1008, Materials.Diamond)
                .armor(5, 0.5)
                .registerModule();
        materialArmorBuilder(1009, Materials.Ruby)
                .armor(4.1, 0.2)
                .registerModule();
        materialArmorBuilder(1010, Materials.Sapphire)
                .armor(4.1, 0.2)
                .registerModule();
        materialArmorBuilder(1011, Materials.Osmium)
                .armor(5, 0)
                .registerModule();
        materialArmorBuilder(1012, Materials.Iridium)
                .armor(3.7, 0.7)
                .registerModule();
        materialArmorBuilder(1013, Materials.Osmiridium)
                .armor(4.7, 0.5)
                .registerModule();
        materialArmorBuilder(1014, Materials.HSSE)
                .armor(6, 0.5)
                .registerModule();
        materialArmorBuilder(1015, Materials.HSSG)
                .armor(6.6, 0.5)
                .registerModule();
        materialArmorBuilder(1016, Materials.HSSS)
                .armor(6, 0.8);
        materialArmorBuilder(2000, Materials.Neutronium)
                .armor(15, 10)
                .specialArmor(((entity, modularArmorPiece, moduleData, source, damage, slot) -> new AbsorbResult(0.5, 100)))
                .registerModule();
    }

    public static void registerModule(int id, IModule module) {
        if (REGISTRY.containsKey(id)) {
            GTLog.logger.throwing(Level.ERROR, new AlreadyBoundException("Can't register module with id " + id + " as it already exists"));
            return;
        }
        REGISTRY.put(id, module);
        BACK_REGISTRY.put(module, id);
    }

    public static MaterialArmorModuleBuilder materialArmorBuilder(int id, Material material) {
        MaterialArmorModuleBuilder builder = new MaterialArmorModuleBuilder(id, material);
        ARMOR_MODULES.put(id, builder);
        return builder;
    }

    public static IModule getModule(int id) {
        return REGISTRY.get(id);
    }

    public static int getModuleId(IModule module) {
        if (!BACK_REGISTRY.containsKey(module)) {
            throw new IllegalStateException("Module " + module.getModuleId() + " is not registered");
        }
        return BACK_REGISTRY.getInt(module);
    }

    public static void init() {
        for (IModule module : REGISTRY.values()) {
            module.getMetaValueItem().addComponents(module);
        }
    }
}
