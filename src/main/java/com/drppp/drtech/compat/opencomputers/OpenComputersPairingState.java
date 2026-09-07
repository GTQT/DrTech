package com.drppp.drtech.compat.opencomputers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Persistent, owner-scoped credentials for OC block components. Plaintext tokens are never saved. */
public final class OpenComputersPairingState extends WorldSavedData {
    public static final String DATA_NAME = "drtech_opencomputers_pairing";
    private static final int MAX_PAIRINGS = 1024;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, Pairing> pairings = new LinkedHashMap<>();
    private final OpenComputersAuditLog auditLog = new OpenComputersAuditLog();

    public OpenComputersPairingState() { this(DATA_NAME); }
    public OpenComputersPairingState(String name) { super(name); }

    public static OpenComputersPairingState get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        OpenComputersPairingState state = (OpenComputersPairingState) storage.getOrLoadData(
                OpenComputersPairingState.class, DATA_NAME);
        if (state == null) {
            state = new OpenComputersPairingState();
            storage.setData(DATA_NAME, state);
        }
        return state;
    }

    public synchronized String rotate(int dimension, BlockPos position, String component, UUID owner,
            long worldTime) {
        if (position == null || owner == null || !OpenComputersComponentIds.isKnown(component)) return "";
        String key = key(dimension, position, component);
        Pairing current = pairings.get(key);
        if (current != null && !owner.equals(current.owner)) return "";
        if (current == null && pairings.size() >= MAX_PAIRINGS) return "";
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        StringBuilder token = new StringBuilder(48);
        for (byte value : bytes) token.append(String.format("%02x", value & 0xff));
        pairings.put(key, new Pairing(owner, digest(token.toString()), Math.max(0L, worldTime)));
        markDirty();
        return token.toString();
    }

    public synchronized boolean revoke(int dimension, BlockPos position, String component, UUID owner) {
        String key = key(dimension, position, component);
        Pairing current = pairings.get(key);
        if (current == null || owner == null || !owner.equals(current.owner)) return false;
        // Revoking a credential must not release an owned programmer/controller to another player.
        pairings.put(key, new Pairing(owner, new byte[0], current.created));
        markDirty();
        return true;
    }

    @Nullable
    public synchronized UUID authenticate(int dimension, BlockPos position, String component, String token) {
        Pairing pairing = pairings.get(key(dimension, position, component));
        if (pairing == null || pairing.hash.length != 32 || token == null || token.length() > 128) return null;
        byte[] supplied = digest(token);
        return MessageDigest.isEqual(pairing.hash, supplied) ? pairing.owner : null;
    }

    public synchronized boolean isPaired(int dimension, BlockPos position, String component) {
        Pairing pairing = pairings.get(key(dimension, position, component));
        return pairing != null && pairing.hash.length == 32;
    }

    @Nullable
    public synchronized UUID ownerFor(int dimension, BlockPos position, String component) {
        Pairing pairing = pairings.get(key(dimension, position, component));
        return pairing == null || pairing.hash.length != 32 ? null : pairing.owner;
    }

    public synchronized boolean removeDevice(int dimension, BlockPos position, String component) {
        if (pairings.remove(key(dimension, position, component)) == null) return false;
        markDirty();
        return true;
    }

    public synchronized void audit(UUID caller, String component, String method, boolean success, long worldTime) {
        auditLog.record(caller, component, method, success, worldTime);
        markDirty();
    }

    @Override
    public synchronized void readFromNBT(NBTTagCompound compound) {
        pairings.clear();
        NBTTagList list = compound.getTagList("Pairings", 10);
        for (int index = 0; index < list.tagCount() && pairings.size() < MAX_PAIRINGS; index++) {
            NBTTagCompound tag = list.getCompoundTagAt(index);
            try {
                String key = tag.getString("Key");
                UUID owner = UUID.fromString(tag.getString("Owner"));
                byte[] hash = tag.getByteArray("Hash");
                if (!key.isEmpty() && (hash.length == 0 || hash.length == 32)) pairings.put(key,
                        new Pairing(owner, hash.clone(), tag.getLong("Created")));
            } catch (IllegalArgumentException ignored) { }
        }
        auditLog.readFromNbt(compound.getTagList("Audit", 10));
    }

    @Override
    public synchronized NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<String, Pairing> entry : pairings.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Key", entry.getKey());
            tag.setString("Owner", entry.getValue().owner.toString());
            tag.setByteArray("Hash", entry.getValue().hash);
            tag.setLong("Created", entry.getValue().created);
            list.appendTag(tag);
        }
        compound.setTag("Pairings", list);
        compound.setTag("Audit", auditLog.writeToNbt());
        return compound;
    }

    private static String key(int dimension, BlockPos position, String component) {
        return dimension + ":" + position.toLong() + ":" + (component == null ? "" : component);
    }

    private static byte[] digest(String token) {
        try { return MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }

    private static final class Pairing {
        private final UUID owner;
        private final byte[] hash;
        private final long created;
        private Pairing(UUID owner, byte[] hash, long created) {
            this.owner = owner; this.hash = hash; this.created = Math.max(0L, created);
        }
    }
}
