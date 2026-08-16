package com.drppp.drtech.common.drone.program.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** UI-only editor overlays kept separate from executable graph state. */
public final class DroneEditorOverlay {
    private final List<Group> groups = new ArrayList<>();
    private final List<Annotation> annotations = new ArrayList<>();
    private final List<TimelineEntry> timeline = new ArrayList<>();
    public void addGroup(String id, String title, int x, int y, int width, int height) { groups.add(new Group(id, title, x, y, Math.max(32, width), Math.max(24, height))); }
    public void addAnnotation(String id, String text, int x, int y) { annotations.add(new Annotation(id, text, x, y)); }
    public void addTimelineEntry(long tick, String nodeId, String status) { if (timeline.size() >= 256) timeline.remove(0); timeline.add(new TimelineEntry(tick, nodeId, status)); }
    public List<Group> getGroups() { return Collections.unmodifiableList(groups); }
    public List<Annotation> getAnnotations() { return Collections.unmodifiableList(annotations); }
    public List<TimelineEntry> getTimeline() { return Collections.unmodifiableList(timeline); }
    public static final class Group { public final String id, title; public final int x, y, width, height; Group(String id,String title,int x,int y,int width,int height){this.id=id;this.title=title;this.x=x;this.y=y;this.width=width;this.height=height;} }
    public static final class Annotation { public final String id, text; public final int x, y; Annotation(String id,String text,int x,int y){this.id=id;this.text=text==null?"":text;x=Math.max(-100000,x);y=Math.max(-100000,y);this.x=x;this.y=y;} }
    public static final class TimelineEntry { public final long tick; public final String nodeId, status; TimelineEntry(long tick,String nodeId,String status){this.tick=Math.max(0,tick);this.nodeId=nodeId;this.status=status;} }
}
