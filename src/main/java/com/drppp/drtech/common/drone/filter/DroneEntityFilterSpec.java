package com.drppp.drtech.common.drone.filter;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Bounded entity selector shared by sensing, interaction, and transport actions. */
public final class DroneEntityFilterSpec {
    public static final int MAX_RULES = 64;
    public static final int MAX_IDS = 64;
    public static final int MAX_NAMES = 64;
    private final List<String> whitelist;
    private final List<String> blacklist;
    private final List<String> names;
    private final UUID entityUuid;
    private final UUID ownerUuid;
    private final Boolean animals;
    private final Boolean monsters;
    private final Boolean adult;
    private final float minHealth;
    private final float maxHealth;
    private final boolean allowBosses;
    private final boolean allowTransport;

    private final DroneFilterMode mode;
    private final List<ResourceLocation> entityIds;

    public DroneEntityFilterSpec(DroneFilterMode mode, List<ResourceLocation> entityIds) {
        this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null, null, null, null,
                0F, Float.MAX_VALUE, true, true, mode, entityIds);
    }

    public DroneEntityFilterSpec(List<String> whitelist, List<String> blacklist, List<String> names,
            @Nullable UUID entityUuid, @Nullable UUID ownerUuid, @Nullable Boolean animals, @Nullable Boolean monsters,
            @Nullable Boolean adult, float minHealth, float maxHealth, boolean allowBosses, boolean allowTransport) {
        this(whitelist, blacklist, names, entityUuid, ownerUuid, animals, monsters, adult,
                minHealth, maxHealth, allowBosses, allowTransport, DroneFilterMode.WHITELIST, null);
    }

    public DroneEntityFilterSpec(List<String> whitelist, List<String> blacklist, List<String> names,
            @Nullable UUID entityUuid, @Nullable UUID ownerUuid, @Nullable Boolean animals, @Nullable Boolean monsters,
            @Nullable Boolean adult, float minHealth, float maxHealth, boolean allowBosses, boolean allowTransport,
            DroneFilterMode mode, List<ResourceLocation> entityIds) {
        this.mode = mode == null ? DroneFilterMode.WHITELIST : mode;
        ArrayList<ResourceLocation> ids = new ArrayList<>();
        if (entityIds != null) for (ResourceLocation id : entityIds)
            if (id != null && !ids.contains(id) && ids.size() < MAX_RULES) ids.add(id);
        this.entityIds = Collections.unmodifiableList(ids);
        this.whitelist = bounded(whitelist, MAX_IDS);
        this.blacklist = bounded(blacklist, MAX_IDS);
        this.names = bounded(names, MAX_NAMES);
        this.entityUuid = entityUuid; this.ownerUuid = ownerUuid;
        this.animals = animals; this.monsters = monsters; this.adult = adult;
        this.minHealth = Math.max(0F, Math.min(minHealth, maxHealth));
        this.maxHealth = Math.max(this.minHealth, maxHealth);
        this.allowBosses = allowBosses; this.allowTransport = allowTransport;
    }

    public static DroneEntityFilterSpec any() { return new DroneEntityFilterSpec(Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList(), null, null, null, null, null, 0F, Float.MAX_VALUE, false, false); }
    public boolean matches(Entity entity) {
        if (!(entity instanceof EntityLivingBase)) return false;
        EntityLivingBase living = (EntityLivingBase) entity;
        ResourceLocation id = EntityList.getKey(entity);
        String idText = id == null ? "" : id.toString();
        if (!entityIds.isEmpty()) {
            boolean found = entityIds.contains(id);
            if (!mode.apply(found)) return false;
        }
        if (!whitelist.isEmpty() && !whitelist.contains(idText)) return false;
        if (blacklist.contains(idText) || (id != null && blacklist.contains(id.getPath()))) return false;
        if (entityUuid != null && !entityUuid.equals(entity.getUniqueID())) return false;
        if (!names.isEmpty() && !names.contains(entity.getName())) return false;
        if (ownerUuid != null && (!(entity instanceof IEntityOwnable) || !ownerUuid.equals(((IEntityOwnable) entity).getOwnerId()))) return false;
        if (animals != null && animals != (entity instanceof EntityAnimal)) return false;
        if (monsters != null && monsters != (entity instanceof IMob)) return false;
        if (adult != null && entity instanceof EntityAgeable && adult == ((EntityAgeable) entity).isChild()) return false;
        if (living.getHealth() < minHealth || living.getHealth() > maxHealth) return false;
        if (!allowBosses && isBoss(entity)) return false;
        return allowTransport || !(entity instanceof EntityTameable && ((EntityTameable) entity).isTamed());
    }
    public List<String> getWhitelist() { return whitelist; }
    public List<String> getBlacklist() { return blacklist; }
    public List<String> getNames() { return names; }
    @Nullable public UUID getEntityUuid() { return entityUuid; }
    @Nullable public UUID getOwnerUuid() { return ownerUuid; }
    @Nullable public Boolean getAnimals() { return animals; }
    @Nullable public Boolean getMonsters() { return monsters; }
    @Nullable public Boolean getAdult() { return adult; }
    public float getMinHealth() { return minHealth; }
    public float getMaxHealth() { return maxHealth; }
    public boolean isAllowBosses() { return allowBosses; }
    public boolean isAllowTransport() { return allowTransport; }
    public DroneFilterMode getMode() { return mode; }
    public List<ResourceLocation> getEntityIds() { return entityIds; }
    public DroneEntityFilterSpec withModeAndEntityIds(DroneFilterMode newMode, List<ResourceLocation> newEntityIds) {
        return new DroneEntityFilterSpec(whitelist, blacklist, names, entityUuid, ownerUuid, animals, monsters,
                adult, minHealth, maxHealth, allowBosses, allowTransport, newMode, newEntityIds);
    }
    public DroneEntityFilterSpec withAdvanced(List<String> newNames, @Nullable UUID newEntityUuid,
            @Nullable UUID newOwnerUuid, @Nullable Boolean newAnimals, @Nullable Boolean newMonsters,
            @Nullable Boolean newAdult, float newMinHealth, float newMaxHealth,
            boolean newAllowBosses, boolean newAllowTransport) {
        return new DroneEntityFilterSpec(whitelist, blacklist, newNames, newEntityUuid, newOwnerUuid,
                newAnimals, newMonsters, newAdult, newMinHealth, newMaxHealth,
                newAllowBosses, newAllowTransport, mode, entityIds);
    }
    public NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("Mode", mode.name());
        NBTTagList ids = new NBTTagList(); for (ResourceLocation id : entityIds) { NBTTagCompound item = new NBTTagCompound(); item.setString("Value", id.toString()); ids.appendTag(item); } tag.setTag("EntityIds", ids);
        writeList(tag, "Whitelist", whitelist); writeList(tag, "Blacklist", blacklist); writeList(tag, "Names", names);
        if (entityUuid != null) tag.setString("EntityUuid", entityUuid.toString()); if (ownerUuid != null) tag.setString("OwnerUuid", ownerUuid.toString());
        if (animals != null) { tag.setBoolean("Animals", animals); tag.setBoolean("AnimalsSet", true); } if (monsters != null) { tag.setBoolean("Monsters", monsters); tag.setBoolean("MonstersSet", true); }
        if (adult != null) { tag.setBoolean("Adult", adult); tag.setBoolean("AdultSet", true); } tag.setFloat("MinHealth", minHealth); tag.setFloat("MaxHealth", maxHealth); tag.setBoolean("AllowBosses", allowBosses); tag.setBoolean("AllowTransport", allowTransport); return tag;
    }
    public static DroneEntityFilterSpec readFromNbt(@Nullable NBTTagCompound tag) { if (tag == null) return any(); ArrayList<ResourceLocation> ids = new ArrayList<>(); NBTTagList idList = tag.getTagList("EntityIds", 10); for (int i = 0; i < idList.tagCount() && ids.size() < MAX_RULES; i++) try { ids.add(new ResourceLocation(idList.getCompoundTagAt(i).getString("Value"))); } catch (RuntimeException ignored) { } return new DroneEntityFilterSpec(readList(tag, "Whitelist", MAX_IDS), readList(tag, "Blacklist", MAX_IDS), readList(tag, "Names", MAX_NAMES), uuid(tag, "EntityUuid"), uuid(tag, "OwnerUuid"), tag.getBoolean("AnimalsSet") ? tag.getBoolean("Animals") : null, tag.getBoolean("MonstersSet") ? tag.getBoolean("Monsters") : null, tag.getBoolean("AdultSet") ? tag.getBoolean("Adult") : null, tag.getFloat("MinHealth"), tag.hasKey("MaxHealth", 5) ? tag.getFloat("MaxHealth") : Float.MAX_VALUE, tag.hasKey("AllowBosses", 1) ? tag.getBoolean("AllowBosses") : false, tag.hasKey("AllowTransport", 1) ? tag.getBoolean("AllowTransport") : false, DroneFilterMode.fromName(tag.getString("Mode")), ids); }
    private static boolean isBoss(Entity entity) {
        return entity instanceof EntityLivingBase && !((EntityLivingBase) entity).isNonBoss()
                || entity instanceof EntityDragon || entity instanceof EntityWither;
    }
    private static List<String> bounded(List<String> values, int max) { ArrayList<String> out = new ArrayList<>(); if (values != null) for (String value : values) if (value != null && !value.trim().isEmpty() && out.size() < max) out.add(value.trim().substring(0, Math.min(128, value.trim().length()))); return Collections.unmodifiableList(out); }
    private static void writeList(NBTTagCompound tag, String key, List<String> values) { NBTTagList list = new NBTTagList(); for (String value : values) { NBTTagCompound item = new NBTTagCompound(); item.setString("Value", value); list.appendTag(item); } tag.setTag(key, list); }
    private static List<String> readList(NBTTagCompound tag, String key, int max) { ArrayList<String> out = new ArrayList<>(); NBTTagList list = tag.getTagList(key, 10); for (int i = 0; i < list.tagCount() && out.size() < max; i++) { String value = list.getCompoundTagAt(i).getString("Value"); if (!value.trim().isEmpty()) out.add(value.trim()); } return out; }
    private static UUID uuid(NBTTagCompound tag, String key) { try { return tag.hasKey(key, 8) ? UUID.fromString(tag.getString(key)) : null; } catch (IllegalArgumentException ignored) { return null; } }
}
