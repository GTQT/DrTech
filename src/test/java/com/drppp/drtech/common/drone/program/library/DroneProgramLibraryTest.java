package com.drppp.drtech.common.drone.program.library;

import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneProgramLibraryTest {

    @Test
    void directoryIsOwnerIsolatedAndSortedByMostRecentWrite() {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        DroneProgramLibrary library = new DroneProgramLibrary();
        DroneProgramGraph older = new DroneProgramGraph("Older");
        DroneProgramGraph newer = new DroneProgramGraph("Newer");
        library.register(owner, older, 10L);
        library.register(owner, newer, 20L);
        library.register(other, new DroneProgramGraph("Private"), 30L);

        List<DroneProgramLibraryRecord> visible = library.listForOwner(owner);
        assertEquals(2, visible.size());
        assertEquals(newer.getProgramId(), visible.get(0).getProgramId());
        assertEquals(older.getProgramId(), visible.get(1).getProgramId());
    }

    @Test
    void exactRevisionLockRejectsStaleOrForeignReferences() {
        UUID owner = UUID.randomUUID();
        DroneProgramGraph graph = new DroneProgramGraph("Worker");
        DroneProgramLibrary library = new DroneProgramLibrary();
        assertTrue(library.register(owner, graph, 10L));
        assertTrue(library.resolve(owner, graph.getProgramId(), graph.getRevision()).isPresent());
        assertFalse(library.resolve(owner, graph.getProgramId(), graph.getRevision() + 1L).isPresent());
        assertFalse(library.resolve(UUID.randomUUID(), graph.getProgramId(), graph.getRevision()).isPresent());

        graph.rename("Worker v2");
        assertTrue(library.register(owner, graph, 20L));
        assertFalse(library.resolve(owner, graph.getProgramId(), graph.getRevision() - 1L).isPresent());
        assertEquals("Worker v2", library.resolve(owner, graph.getProgramId(), graph.getRevision())
                .orElseThrow(AssertionError::new).getName());
    }

    @Test
    void validatedSnapshotsRoundTripWithoutSharingMutableGraphs() {
        UUID owner = UUID.randomUUID();
        DroneProgramGraph graph = new DroneProgramGraph("Saved");
        DroneProgramLibrary source = new DroneProgramLibrary();
        source.register(owner, graph, 42L);
        graph.rename("Mutated Outside");

        NBTTagCompound saved = source.writeToNBT(new NBTTagCompound());
        DroneProgramLibrary restored = new DroneProgramLibrary();
        restored.readFromNBT(saved);

        assertEquals(1, restored.size());
        DroneProgramLibraryRecord record = restored.listForOwner(owner).get(0);
        assertEquals("Saved", record.getName());
        assertEquals(42L, record.getUpdatedAt());
    }
}
