package com.drppp.drtech.common.drone.program.edit;

/** Pure acknowledgement policy for keeping a client drag preview while the server edit is in flight. */
public final class DroneDragPreviewPolicy {

    private DroneDragPreviewPolicy() {}

    public static boolean shouldKeep(long currentRevision, long sourceRevision,
            int serverX, int serverY, int previewX, int previewY, long now, long expiresAt) {
        if (serverX == previewX && serverY == previewY) return false;
        if (currentRevision > sourceRevision) return false;
        return now < expiresAt;
    }
}
