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
        return result;
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
    private static NBTTagCompound configInt(String a, int av, String b, int bv, String c, int cv, String d, int dv, String e, int ev, String f, int fv) {
        NBTTagCompound n = new NBTTagCompound(); n.setInteger(a,av); n.setInteger(b,bv); n.setInteger(c,cv); n.setInteger(d,dv); n.setInteger(e,ev); n.setInteger(f,fv); return n;
    }
    private static NBTTagCompound configDouble(String key, double value) { NBTTagCompound n = new NBTTagCompound(); n.setDouble(key, value); return n; }
    private static NBTTagCompound configString(String key, String value) { NBTTagCompound n = new NBTTagCompound(); n.setString(key, value); return n; }
    private static ResourceLocation id(String path) { return new ResourceLocation("drtech", path); }
}
