package com.drppp.drtech.api.capability;

import com.drppp.drtech.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class DrtechCommonCapabilities {
    @CapabilityInject(IFuelRodData.class)
    public static Capability<IFuelRodData> CAPABILITY_FUEL_ROAD = null;
    @CapabilityInject(IHeatVent.class)
    public static Capability<IHeatVent> CAPABILITY_HEAT_VENT = null;
    @CapabilityInject(IHeatExchanger.class)
    public static Capability<IHeatExchanger> CAPABILITY_HEAT_EXCHANGER = null;
    @CapabilityInject(INeutronReflector.class)
    public static Capability<INeutronReflector> CAPABILITY_NEUTRON_REFLECTOR= null;
    @CapabilityInject(ICoolantCell.class)
    public static Capability<ICoolantCell> CAPABILITY_COOLANT_CELL= null;
    @CapabilityInject(ICoolantCell.class)
    public static Capability<IRotationEnergy> CAPABILITY_ROTATION_ENERGY= null;
}
