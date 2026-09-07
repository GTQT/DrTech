package com.drppp.drtech.common.drone.program.library;

import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Server-side allow-list of built-in, known-good example programs. */
public final class DroneOfficialProgramTemplates {
    public static final ResourceLocation BASIC_WAIT = id("basic_wait");
    public static final ResourceLocation RETURN_AND_CHARGE = id("return_and_charge");
    public static final ResourceLocation AREA_TRAVERSAL = id("area_traversal");
    public static final ResourceLocation HYBRID_FARM = id("hybrid_farm");
    public static final ResourceLocation REGION_MINING = id("region_mining");
    public static final ResourceLocation ITEM_LOGISTICS = id("item_logistics");
    public static final ResourceLocation FLUID_LOGISTICS = id("fluid_logistics");
    public static final ResourceLocation EU_LOGISTICS = id("eu_logistics");
    public static final ResourceLocation GT_MAINTENANCE = id("gt_maintenance");
    public static final ResourceLocation FLEET_HAUL = id("fleet_haul");

    private static final Map<ResourceLocation, DroneOfficialProgramTemplate> TEMPLATES = createTemplates();

    private DroneOfficialProgramTemplates() {}

    public static List<DroneOfficialProgramTemplate> all() {
        return Collections.unmodifiableList(new ArrayList<>(TEMPLATES.values()));
    }

    public static DroneOfficialProgramTemplate get(ResourceLocation id) { return TEMPLATES.get(id); }

    private static Map<ResourceLocation, DroneOfficialProgramTemplate> createTemplates() {
        Map<ResourceLocation, DroneOfficialProgramTemplate> result = new LinkedHashMap<>();
        result.put(BASIC_WAIT, new DroneOfficialProgramTemplate(BASIC_WAIT,
                "drtech.drone.template.basic_wait.name", "drtech.drone.template.basic_wait.description",
                "drtech.drone.template.hardware.basic", DroneOfficialProgramTemplates::basicWait));
        result.put(RETURN_AND_CHARGE, new DroneOfficialProgramTemplate(RETURN_AND_CHARGE,
                "drtech.drone.template.return_and_charge.name", "drtech.drone.template.return_and_charge.description",
                "drtech.drone.template.hardware.dock", DroneOfficialProgramTemplates::returnAndCharge));
        result.put(AREA_TRAVERSAL, new DroneOfficialProgramTemplate(AREA_TRAVERSAL,
                "drtech.drone.template.area_traversal.name", "drtech.drone.template.area_traversal.description",
                "drtech.drone.template.hardware.navigation", DroneOfficialProgramTemplates::areaTraversal));
        result.put(HYBRID_FARM, template(HYBRID_FARM, "tool_arm", DroneOfficialProgramTemplates::hybridFarm));
        result.put(REGION_MINING, template(REGION_MINING, "tool_arm", DroneOfficialProgramTemplates::regionMining));
        result.put(ITEM_LOGISTICS, template(ITEM_LOGISTICS, "cargo", DroneOfficialProgramTemplates::itemLogistics));
        result.put(FLUID_LOGISTICS, template(FLUID_LOGISTICS, "fluid", DroneOfficialProgramTemplates::fluidLogistics));
        result.put(EU_LOGISTICS, template(EU_LOGISTICS, "eu", DroneOfficialProgramTemplates::euLogistics));
        result.put(GT_MAINTENANCE, template(GT_MAINTENANCE, "tool_arm",
                DroneOfficialProgramTemplates::gtMaintenance));
        result.put(FLEET_HAUL, template(FLEET_HAUL, "fleet", DroneOfficialProgramTemplates::fleetHaul));
        return result;
    }

    private static DroneOfficialProgramTemplate template(ResourceLocation id, String hardware,
            java.util.function.Supplier<DroneProgramGraph> factory) {
        String base = "drtech.drone.template." + id.getPath();
        return new DroneOfficialProgramTemplate(id, base + ".name", base + ".description",
                "drtech.drone.template.hardware." + hardware, factory);
    }

    private static DroneProgramGraph basicWait() {
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 20, 40);
        DroneProgramNode wait = node(DrTechDroneNodes.WAIT, 180, 40, configInt("Ticks", 20));
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 340, 40);
        return graph("Basic Wait", new Object[] {start, wait, end,
                edge(start, "next", wait, "in"), edge(wait, "next", end, "in")});
    }

    private static DroneProgramGraph returnAndCharge() {
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 20, 40);
        DroneProgramNode dock = DroneProgramNode.create(DrTechDroneNodes.FIND_NEAREST_DOCK, 170, 120);
        DroneProgramNode ret = DroneProgramNode.create(DrTechDroneNodes.RETURN_TO_DOCK, 330, 40);
        DroneProgramNode charge = node(DrTechDroneNodes.CHARGE_UNTIL, 500, 40, configDouble("Percent", 100.0D));
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 680, 40);
        return graph("Return and Charge", new Object[] {start, dock, ret, charge, end,
                edge(start, "next", ret, "in"), edge(dock, "value", ret, "target"),
                edge(ret, "next", charge, "in"), edge(charge, "next", end, "in")});
    }

    private static DroneProgramGraph areaTraversal() {
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 20, 40);
        DroneProgramNode area = node(DrTechDroneNodes.AREA, 170, 120,
                configInt("X1", 0, "Y1", 0, "Z1", 0, "X2", 8, "Y2", 0, "Z2", 8));
        DroneProgramNode loop = node(DrTechDroneNodes.FOR_EACH_COORDINATE, 350, 40,
                configString("Order", "SERPENTINE"));
        DroneProgramNode wait = node(DrTechDroneNodes.WAIT, 350, 160, configInt("Ticks", 1));
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 560, 40);
        return graph("Area Traversal", new Object[] {start, area, loop, wait, end,
                edge(start, "next", loop, "in"), edge(area, "value", loop, "area"),
                edge(loop, "body", wait, "in"), edge(wait, "next", loop, "in"),
                edge(loop, "done", end, "in")});
    }

    private static DroneProgramGraph hybridFarm() {
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 20, 40);
        DroneProgramNode area = area(20, 150, 0, 0, 0, 8, 0, 8);
        DroneProgramNode loop = node(DrTechDroneNodes.FOR_EACH_COORDINATE, 210, 40,
                configString("Order", "SERPENTINE"));
        DroneProgramNode harvest = DroneProgramNode.create(DrTechDroneNodes.HARVEST_CROP, 390, 40);
        DroneProgramNode replant = DroneProgramNode.create(DrTechDroneNodes.REPLANT_AREA, 570, 40);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 750, 40);
        return graph("Hybrid Farm", new Object[] {start, area, loop, harvest, replant, end,
                edge(start, "next", loop, "in"), edge(area, "value", loop, "area"),
                edge(loop, "body", harvest, "in"), edge(loop, "coordinate", harvest, "target"),
                edge(harvest, "next", loop, "in"), edge(loop, "done", replant, "in"),
                edge(area, "value", replant, "area"), edge(replant, "next", end, "in")});
    }

    private static DroneProgramGraph regionMining() {
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 20, 40);
        DroneProgramNode area = area(20, 140, 0, 0, 0, 8, 4, 8);
        DroneProgramNode mine = DroneProgramNode.create(DrTechDroneNodes.BREAK_BLOCK, 220, 40);
        DroneProgramNode pickup = node(DrTechDroneNodes.PICKUP_DROPPED_ITEMS, 410, 40,
                configNumberAndInt("Radius", 4.0D, "MaxAmount", 1_000_000));
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 600, 40);
        return graph("Region Mining", new Object[] {start, area, mine, pickup, end,
                edge(start, "next", mine, "in"), edge(area, "value", mine, "area"),
                edge(mine, "next", pickup, "in"), edge(pickup, "next", end, "in")});
    }

    private static DroneProgramGraph itemLogistics() {
        return twoPointTransfer("Item Logistics", DrTechDroneNodes.IMPORT_ITEMS, DrTechDroneNodes.EXPORT_ITEMS,
                transferConfig(256, 64));
    }

    private static DroneProgramGraph fluidLogistics() {
        return twoPointTransfer("Fluid Logistics", DrTechDroneNodes.IMPORT_FLUID, DrTechDroneNodes.EXPORT_FLUID,
                configInt("MaxAmount", 16_000));
    }

    private static DroneProgramGraph euLogistics() {
        return twoPointTransfer("EU Logistics", DrTechDroneNodes.IMPORT_EU, DrTechDroneNodes.EXPORT_EU,
                configInt("MaxEU", 32_768));
    }

    private static DroneProgramGraph gtMaintenance() {
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 20, 40);
        DroneProgramNode target = coordinate(20, 130, 0, 0, 0);
        NBTTagCompound repairConfig = new NBTTagCompound();
        repairConfig.setBoolean("RequireAll", true);
        DroneProgramNode repair = node(DrTechDroneNodes.REPAIR_MACHINE, 220, 40, repairConfig);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 410, 40);
        return graph("GT Maintenance", new Object[] {start, target, repair, end,
                edge(start, "next", repair, "in"), edge(target, "value", repair, "target"),
                edge(repair, "next", end, "in")});
    }

    private static DroneProgramGraph fleetHaul() {
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 20, 40);
        DroneProgramNode source = coordinate(20, 130, 0, 0, 0);
        DroneProgramNode target = coordinate(190, 130, 8, 0, 0);
        DroneProgramNode dock = DroneProgramNode.create(DrTechDroneNodes.FIND_NEAREST_DOCK, 540, 140);
        DroneProgramNode load = node(DrTechDroneNodes.IMPORT_ITEMS, 200, 40, transferConfig(576, 64));
        DroneProgramNode unload = node(DrTechDroneNodes.EXPORT_ITEMS, 390, 40, transferConfig(576, 64));
        DroneProgramNode ret = DroneProgramNode.create(DrTechDroneNodes.RETURN_TO_DOCK, 570, 40);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 750, 40);
        return graph("Fleet Haul", new Object[] {start, source, target, dock, load, unload, ret, end,
                edge(start, "next", load, "in"), edge(source, "value", load, "target"),
                edge(load, "next", unload, "in"), edge(target, "value", unload, "target"),
                edge(unload, "next", ret, "in"), edge(dock, "value", ret, "target"),
                edge(ret, "next", end, "in")});
    }

    private static DroneProgramGraph twoPointTransfer(String name, ResourceLocation importType,
            ResourceLocation exportType, NBTTagCompound transferConfiguration) {
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 20, 40);
        DroneProgramNode source = coordinate(20, 130, 0, 0, 0);
        DroneProgramNode target = coordinate(190, 130, 8, 0, 0);
        DroneProgramNode load = node(importType, 210, 40, transferConfiguration.copy());
        DroneProgramNode unload = node(exportType, 410, 40, transferConfiguration.copy());
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 610, 40);
        return graph(name, new Object[] {start, source, target, load, unload, end,
                edge(start, "next", load, "in"), edge(source, "value", load, "target"),
                edge(load, "next", unload, "in"), edge(target, "value", unload, "target"),
                edge(unload, "next", end, "in")});
    }

    private static DroneProgramNode coordinate(int x, int y, int px, int py, int pz) {
        return node(DrTechDroneNodes.COORDINATE, x, y, configInt("X", px, "Y", py, "Z", pz));
    }

    private static DroneProgramNode area(int x, int y, int x1, int y1, int z1, int x2, int y2, int z2) {
        return node(DrTechDroneNodes.AREA, x, y,
                configInt("X1", x1, "Y1", y1, "Z1", z1, "X2", x2, "Y2", y2, "Z2", z2));
    }

    private static DroneProgramGraph graph(String name, Object[] nodesAndEdges) {
        List<DroneProgramNode> nodes = new ArrayList<>();
        List<DroneProgramEdge> edges = new ArrayList<>();
        for (Object value : nodesAndEdges) {
            if (value instanceof DroneProgramEdge) edges.add((DroneProgramEdge) value);
            else nodes.add((DroneProgramNode) value);
        }
        return new DroneProgramGraph(UUID.randomUUID(), name, 0L, nodes, edges);
    }

    private static DroneProgramEdge edge(DroneProgramNode source, String sourcePort, DroneProgramNode target,
            String targetPort) { return DroneProgramEdge.create(source.getId(), sourcePort, target.getId(), targetPort); }

    private static DroneProgramNode node(ResourceLocation type, int x, int y, NBTTagCompound config) {
        return new DroneProgramNode(UUID.randomUUID(), type, x, y, config);
    }
    private static NBTTagCompound configInt(String key, int value) { NBTTagCompound n = new NBTTagCompound(); n.setInteger(key, value); return n; }
    private static NBTTagCompound configInt(String a, int av, String b, int bv, String c, int cv) {
        NBTTagCompound n = new NBTTagCompound(); n.setInteger(a,av); n.setInteger(b,bv); n.setInteger(c,cv); return n;
    }
    private static NBTTagCompound configInt(String a, int av, String b, int bv, String c, int cv, String d, int dv, String e, int ev, String f, int fv) {
        NBTTagCompound n = new NBTTagCompound(); n.setInteger(a,av); n.setInteger(b,bv); n.setInteger(c,cv); n.setInteger(d,dv); n.setInteger(e,ev); n.setInteger(f,fv); return n;
    }
    private static NBTTagCompound configDouble(String key, double value) { NBTTagCompound n = new NBTTagCompound(); n.setDouble(key, value); return n; }
    private static NBTTagCompound configString(String key, String value) { NBTTagCompound n = new NBTTagCompound(); n.setString(key, value); return n; }
    private static NBTTagCompound configNumberAndInt(String numberKey, double number, String intKey, int integer) {
        NBTTagCompound n = new NBTTagCompound(); n.setDouble(numberKey, number); n.setInteger(intKey, integer); return n;
    }
    private static NBTTagCompound transferConfig(int maxAmount, int batchSize) {
        NBTTagCompound n = new NBTTagCompound();
        n.setInteger("MaxAmount", maxAmount); n.setInteger("BatchSize", batchSize);
        n.setString("Direction", "AUTO"); n.setString("SearchMode", "NEAREST");
        return n;
    }
    private static ResourceLocation id(String path) { return new ResourceLocation("drtech", path); }
}
