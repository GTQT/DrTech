package com.drppp.drtech.common.drone.program.library;

import com.drppp.drtech.common.drone.program.codec.DroneProgramFormatException;
import com.drppp.drtech.common.drone.program.codec.DroneProgramNbtCodec;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** One validated program snapshot owned by a player. */
public final class DroneProgramLibraryRecord {

    private final UUID ownerId;
    private final DroneProgramGraph graph;
    private final long updatedAt;

    public DroneProgramLibraryRecord(UUID ownerId, DroneProgramGraph graph, long updatedAt) {
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId");
        this.graph = Objects.requireNonNull(graph, "graph").copy();
        this.updatedAt = Math.max(0L, updatedAt);
    }

    public UUID getOwnerId() { return ownerId; }
    public UUID getProgramId() { return graph.getProgramId(); }
    public String getName() { return graph.getName(); }
    public long getRevision() { return graph.getRevision(); }
    public int getNodeCount() { return graph.getNodes().size(); }
    public int getEdgeCount() { return graph.getEdges().size(); }
    public long getUpdatedAt() { return updatedAt; }
    public DroneProgramGraph getGraph() { return graph.copy(); }
    public String getSignature() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(DroneProgramNbtCodec.write(graph).toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte value : digest) result.append(String.format("%02x", value & 0xff));
            return result.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Owner", ownerId.toString());
        tag.setLong("UpdatedAt", updatedAt);
        tag.setTag("Program", DroneProgramNbtCodec.write(graph));
        return tag;
    }

    @Nullable
    public static DroneProgramLibraryRecord readFromNbt(NBTTagCompound tag) {
        if (!tag.hasKey("Owner", 8) || !tag.hasKey("Program", 10)) return null;
        try {
            UUID owner = UUID.fromString(tag.getString("Owner"));
            DroneProgramGraph graph = DroneProgramNbtCodec.read(tag.getCompoundTag("Program"));
            return new DroneProgramLibraryRecord(owner, graph, tag.getLong("UpdatedAt"));
        } catch (IllegalArgumentException | DroneProgramFormatException ignored) {
            return null;
        }
    }
}
