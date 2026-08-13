package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.program.codec.DroneProgramFormatException;
import com.drppp.drtech.common.drone.program.codec.DroneProgramNbtCodec;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneProgramNbtCodecTest {

    @Test
    void roundTripsStableIdsLayoutConfigurationAndRevision() throws Exception {
        DroneProgramGraph source = new DroneProgramGraph("矿区巡检");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 12, -40);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 320, 96);
        source.addNode(start);
        source.addNode(end);
        source.addEdge(DroneProgramEdge.create(start.getId(), "next", end.getId(), "in"));
        NBTTagCompound configuration = new NBTTagCompound();
        configuration.setString("Comment", "server-authoritative");
        source.configureNode(end.getId(), configuration);

        DroneProgramGraph decoded = DroneProgramNbtCodec.read(DroneProgramNbtCodec.write(source));

        assertEquals(source.getProgramId(), decoded.getProgramId());
        assertEquals(source.getRevision(), decoded.getRevision());
        assertEquals(source.getName(), decoded.getName());
        assertEquals(2, decoded.getNodes().size());
        assertEquals(1, decoded.getEdges().size());
        assertEquals(12, decoded.getNode(start.getId()).getX());
        assertEquals("server-authoritative", decoded.getNode(end.getId()).getConfiguration().getString("Comment"));
    }

    @Test
    void rejectsUnsupportedSchema() {
        NBTTagCompound malformed = new NBTTagCompound();
        malformed.setInteger("Schema", 99);

        DroneProgramFormatException exception = assertThrows(DroneProgramFormatException.class,
                () -> DroneProgramNbtCodec.read(malformed));

        assertTrue(exception.getMessage().contains("schema"));
    }

    @Test
    void migratesSchemaOneWithoutMutatingTheStoredPayload() throws Exception {
        DroneProgramGraph source = new DroneProgramGraph("legacy");
        source.addNode(DroneProgramNode.create(DrTechDroneNodes.START, 0, 0));
        NBTTagCompound legacy = DroneProgramNbtCodec.write(source);
        legacy.setInteger("Schema", 1);

        DroneProgramGraph decoded = DroneProgramNbtCodec.read(legacy);

        assertEquals(1, legacy.getInteger("Schema"));
        assertEquals(DroneProgramNbtCodec.SCHEMA_VERSION,
                DroneProgramNbtCodec.write(decoded).getInteger("Schema"));
    }

    @Test
    void preservesMissingThirdPartyNodeForEditorRecovery() throws Exception {
        ResourceLocation missingType = new ResourceLocation("missing_mod", "custom_action");
        DroneProgramGraph source = new DroneProgramGraph("recoverable");
        DroneProgramNode missing = DroneProgramNode.create(missingType, 20, 30);
        source.addNode(missing);

        DroneProgramGraph decoded = DroneProgramNbtCodec.read(DroneProgramNbtCodec.write(source));

        assertEquals(missingType, decoded.getNode(missing.getId()).getType());
    }

    @Test
    void returnedConfigurationIsDefensivelyCopied() {
        NBTTagCompound original = new NBTTagCompound();
        original.setInteger("Value", 1);
        DroneProgramNode node = new DroneProgramNode(java.util.UUID.randomUUID(), DrTechDroneNodes.NUMBER, 0, 0,
                original);

        NBTTagCompound firstRead = node.getConfiguration();
        firstRead.setInteger("Value", 2);

        assertEquals(1, node.getConfiguration().getInteger("Value"));
    }

    @Test
    void roundTripsNodeAliasAsPartOfItsConfiguration() throws Exception {
        DroneProgramGraph source = new DroneProgramGraph("labeled");
        DroneProgramNode node = DroneProgramNode.create(DrTechDroneNodes.WAIT, 0, 0);
        NBTTagCompound configuration = node.getConfiguration();
        configuration.setString("Label", "温室等待");
        source.addNode(node.withConfiguration(configuration));

        DroneProgramGraph decoded = DroneProgramNbtCodec.read(DroneProgramNbtCodec.write(source));

        assertEquals("温室等待", decoded.getNode(node.getId()).getConfiguration().getString("Label"));
    }
}
