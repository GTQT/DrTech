package com.drppp.drtech.Client.drone;

import com.drppp.drtech.common.drone.program.model.DroneArea;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneAreaPreviewTest {

    @Test
    void previewsConfiguredCuboidWithRuntimeDimensionsAndVolume() {
        NBTTagCompound config = new NBTTagCompound();
        config.setInteger("X1", 4);
        config.setInteger("Y1", 8);
        config.setInteger("Z1", 12);
        config.setInteger("X2", 6);
        config.setInteger("Y2", 9);
        config.setInteger("Z2", 15);
        DroneProgramNode areaNode = node(DrTechDroneNodes.AREA, config);
        DroneProgramGraph graph = new DroneProgramGraph("preview");
        graph.addNode(areaNode);
        DroneProgramCanvasWidget canvas = canvas(graph);

        assertTrue(canvas.focusNodeProperty(areaNode.getId(), "X1"));
        DroneArea preview = canvas.getSelectedAreaPreview();

        assertNotNull(preview);
        assertEquals(3, preview.getSizeX());
        assertEquals(2, preview.getSizeY());
        assertEquals(4, preview.getSizeZ());
        assertEquals(24L, preview.getVolume());
    }

    @Test
    void previewsSphereFromConnectedStaticCoordinateUsingRuntimeAlgorithm() {
        NBTTagCompound coordinateConfig = new NBTTagCompound();
        coordinateConfig.setInteger("X", 10);
        coordinateConfig.setInteger("Y", 20);
        coordinateConfig.setInteger("Z", 30);
        NBTTagCompound sphereConfig = new NBTTagCompound();
        sphereConfig.setInteger("Radius", 2);
        DroneProgramNode coordinate = node(DrTechDroneNodes.COORDINATE, coordinateConfig);
        DroneProgramNode sphere = node(DrTechDroneNodes.SPHERE_AREA, sphereConfig);
        DroneProgramGraph graph = new DroneProgramGraph("sphere preview");
        graph.addNode(coordinate);
        graph.addNode(sphere);
        graph.addEdge(DroneProgramEdge.create(coordinate.getId(), "value", sphere.getId(), "center"));
        DroneProgramCanvasWidget canvas = canvas(graph);

        assertTrue(canvas.focusNodeProperty(sphere.getId(), "Radius"));
        DroneArea preview = canvas.getSelectedAreaPreview();

        assertEquals(DroneArea.sphere(new BlockPos(10, 20, 30), 2, false), preview);
    }

    private static DroneProgramNode node(net.minecraft.util.ResourceLocation type, NBTTagCompound config) {
        return new DroneProgramNode(UUID.randomUUID(), type, 0, 0, config);
    }

    private static DroneProgramCanvasWidget canvas(DroneProgramGraph graph) {
        return new DroneProgramCanvasWidget(() -> graph, command -> {}, () -> true,
                Collections::emptySet, () -> null);
    }
}
