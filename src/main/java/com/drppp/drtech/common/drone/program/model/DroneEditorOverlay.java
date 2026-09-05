package com.drppp.drtech.common.drone.program.model;

import com.drppp.drtech.common.drone.program.edit.DroneGroupLayout;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Bounded, non-executable editor presentation persisted separately from runtime graph semantics. */
public final class DroneEditorOverlay {
    public static final int MAX_GROUPS = 128;
    public static final int MAX_ANNOTATIONS = 128;
    public static final int MAX_TIMELINE_ENTRIES = 256;
    private static final int MAX_COORDINATE = 1_000_000;
    private final List<Group> groups;
    private final List<Annotation> annotations;
    private final List<TimelineEntry> timeline;

    public DroneEditorOverlay() { this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()); }

    private DroneEditorOverlay(Collection<Group> groups, Collection<Annotation> annotations,
            Collection<TimelineEntry> timeline) {
        this.groups = bounded(groups, MAX_GROUPS);
        this.annotations = bounded(annotations, MAX_ANNOTATIONS);
        this.timeline = boundedTail(timeline, MAX_TIMELINE_ENTRIES);
    }

    public DroneEditorOverlay copy() { return new DroneEditorOverlay(groups, annotations, timeline); }
    public List<Group> getGroups() { return groups; }
    public List<Annotation> getAnnotations() { return annotations; }
    public List<TimelineEntry> getTimeline() { return timeline; }

    /** Mirrors legacy editor-only nodes while preserving the independent debug timeline. */
    public DroneEditorOverlay withLayoutFromNodes(Collection<DroneProgramNode> nodes) {
        List<Group> nextGroups = new ArrayList<>();
        List<Annotation> nextAnnotations = new ArrayList<>();
        if (nodes != null) for (DroneProgramNode node : nodes) {
            NBTTagCompound config = node.getConfiguration();
            if (node.getType().equals(DrTechDroneNodes.GROUP) && nextGroups.size() < MAX_GROUPS) {
                nextGroups.add(new Group(node.getId().toString(), config.getString("Title"), node.getX(), node.getY(),
                        DroneGroupLayout.width(node), DroneGroupLayout.height(node),
                        config.getString("Color"), config.getBoolean("Collapsed")));
            } else if (node.getType().equals(DrTechDroneNodes.COMMENT)
                    && nextAnnotations.size() < MAX_ANNOTATIONS) {
                nextAnnotations.add(new Annotation(node.getId().toString(), config.getString("Text"),
                        node.getX(), node.getY()));
            }
        }
        return new DroneEditorOverlay(nextGroups, nextAnnotations, timeline);
    }

    public DroneEditorOverlay withTimeline(NBTTagList trace) {
        List<TimelineEntry> entries = new ArrayList<>();
        if (trace != null) for (int index = Math.max(0, trace.tagCount() - MAX_TIMELINE_ENTRIES);
                index < trace.tagCount(); index++) {
            NBTTagCompound tag = trace.getCompoundTagAt(index);
            String status = boundedText(tag.getString("Text"), 160);
            if (!status.isEmpty()) entries.add(new TimelineEntry(tag.getLong("Tick"),
                    boundedText(tag.getString("Node"), 36), status));
        }
        return new DroneEditorOverlay(groups, annotations, entries);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound root = new NBTTagCompound();
        NBTTagList groupTags = new NBTTagList();
        for (Group group : groups) groupTags.appendTag(group.write());
        root.setTag("Groups", groupTags);
        NBTTagList annotationTags = new NBTTagList();
        for (Annotation annotation : annotations) annotationTags.appendTag(annotation.write());
        root.setTag("Annotations", annotationTags);
        NBTTagList timelineTags = new NBTTagList();
        for (TimelineEntry entry : timeline) timelineTags.appendTag(entry.write());
        root.setTag("Timeline", timelineTags);
        return root;
    }

    public static DroneEditorOverlay readFromNbt(NBTTagCompound root) {
        if (root == null) return new DroneEditorOverlay();
        NBTTagList groupTags = root.getTagList("Groups", 10);
        NBTTagList annotationTags = root.getTagList("Annotations", 10);
        NBTTagList timelineTags = root.getTagList("Timeline", 10);
        if (groupTags.tagCount() > MAX_GROUPS || annotationTags.tagCount() > MAX_ANNOTATIONS
                || timelineTags.tagCount() > MAX_TIMELINE_ENTRIES) {
            throw new IllegalArgumentException("Editor overlay exceeds its entry limit");
        }
        List<Group> groups = new ArrayList<>();
        for (int index = 0; index < groupTags.tagCount(); index++) groups.add(Group.read(groupTags.getCompoundTagAt(index)));
        List<Annotation> annotations = new ArrayList<>();
        for (int index = 0; index < annotationTags.tagCount(); index++) annotations.add(Annotation.read(annotationTags.getCompoundTagAt(index)));
        List<TimelineEntry> timeline = new ArrayList<>();
        for (int index = 0; index < timelineTags.tagCount(); index++) timeline.add(TimelineEntry.read(timelineTags.getCompoundTagAt(index)));
        return new DroneEditorOverlay(groups, annotations, timeline);
    }

    private static <T> List<T> bounded(Collection<T> source, int maximum) {
        List<T> result = new ArrayList<>();
        if (source != null) for (T value : source) if (value != null && result.size() < maximum) result.add(value);
        return Collections.unmodifiableList(result);
    }

    private static <T> List<T> boundedTail(Collection<T> source, int maximum) {
        List<T> values = source == null ? Collections.emptyList() : new ArrayList<>(source);
        return Collections.unmodifiableList(new ArrayList<>(values.subList(Math.max(0, values.size() - maximum), values.size())));
    }

    private static int coordinate(int value) { return Math.max(-MAX_COORDINATE, Math.min(MAX_COORDINATE, value)); }
    private static String boundedText(String value, int maximum) {
        String safe = value == null ? "" : value.replace('\r', ' ').replace('\n', ' ').trim();
        return safe.substring(0, Math.min(maximum, safe.length()));
    }

    public static final class Group {
        private final String id, title, color;
        private final int x, y, width, height;
        private final boolean collapsed;
        Group(String id, String title, int x, int y, int width, int height, String color, boolean collapsed) {
            this.id = boundedText(id, 64); this.title = boundedText(title, 128); this.color = boundedText(color, 16);
            this.x = coordinate(x); this.y = coordinate(y); this.width = Math.max(32, Math.min(4096, width));
            this.height = Math.max(24, Math.min(4096, height)); this.collapsed = collapsed;
        }
        public String getId() { return id; } public String getTitle() { return title; }
        public int getX() { return x; } public int getY() { return y; }
        public int getWidth() { return width; } public int getHeight() { return height; }
        public String getColor() { return color; } public boolean isCollapsed() { return collapsed; }
        NBTTagCompound write() { NBTTagCompound tag = new NBTTagCompound(); tag.setString("Id", id);
            tag.setString("Title", title); tag.setString("Color", color); tag.setInteger("X", x); tag.setInteger("Y", y);
            tag.setInteger("Width", width); tag.setInteger("Height", height); tag.setBoolean("Collapsed", collapsed); return tag; }
        static Group read(NBTTagCompound tag) { return new Group(tag.getString("Id"), tag.getString("Title"),
                tag.getInteger("X"), tag.getInteger("Y"), tag.getInteger("Width"), tag.getInteger("Height"),
                tag.getString("Color"), tag.getBoolean("Collapsed")); }
    }

    public static final class Annotation {
        private final String id, text; private final int x, y;
        Annotation(String id, String text, int x, int y) { this.id = boundedText(id, 64);
            this.text = boundedText(text, 1024); this.x = coordinate(x); this.y = coordinate(y); }
        public String getId() { return id; } public String getText() { return text; }
        public int getX() { return x; } public int getY() { return y; }
        NBTTagCompound write() { NBTTagCompound tag = new NBTTagCompound(); tag.setString("Id", id);
            tag.setString("Text", text); tag.setInteger("X", x); tag.setInteger("Y", y); return tag; }
        static Annotation read(NBTTagCompound tag) { return new Annotation(tag.getString("Id"), tag.getString("Text"),
                tag.getInteger("X"), tag.getInteger("Y")); }
    }

    public static final class TimelineEntry {
        private final long tick; private final String nodeId, status;
        TimelineEntry(long tick, String nodeId, String status) { this.tick = Math.max(0L, tick);
            this.nodeId = boundedText(nodeId, 36); this.status = boundedText(status, 160); }
        public long getTick() { return tick; } public String getNodeId() { return nodeId; }
        public String getStatus() { return status; }
        NBTTagCompound write() { NBTTagCompound tag = new NBTTagCompound(); tag.setLong("Tick", tick);
            tag.setString("Node", nodeId); tag.setString("Status", status); return tag; }
        static TimelineEntry read(NBTTagCompound tag) { return new TimelineEntry(tag.getLong("Tick"),
                tag.getString("Node"), tag.getString("Status")); }
    }
}
