package com.drppp.drtech.common.drone.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Server-owned fleet job scheduler with deterministic priority, timeout, and retry handling. */
public final class DroneJobQueue {
    private final Map<UUID, DroneJob> jobs = new LinkedHashMap<>();
    private static final Comparator<DroneJob> NEXT_JOB_ORDER = Comparator
            .comparingInt(DroneJob::getPriority).reversed()
            .thenComparingLong(DroneJob::getSubmittedTick)
            .thenComparing(job -> job.getJobId().toString());

    public boolean submit(DroneJob job) {
        if (job == null || jobs.containsKey(job.getJobId())) return false;
        jobs.put(job.getJobId(), job);
        return true;
    }

    public Optional<DroneJob> takeNext(long worldTime) {
        return jobs.values().stream().filter(job -> job.isEligible(worldTime)).min(NEXT_JOB_ORDER)
                .filter(job -> job.start(worldTime));
    }

    public void tick(long worldTime) {
        for (DroneJob job : jobs.values()) {
            if (job.hasTimedOut(worldTime)) job.fail(worldTime, "TIMEOUT");
        }
    }

    public boolean complete(UUID jobId) {
        DroneJob job = jobs.get(jobId);
        if (job == null || job.getState() != DroneJob.State.RUNNING) return false;
        job.complete();
        return true;
    }

    public boolean fail(UUID jobId, long worldTime, String reason) {
        DroneJob job = jobs.get(jobId);
        if (job == null || job.getState() != DroneJob.State.RUNNING) return false;
        job.fail(worldTime, reason);
        return true;
    }

    public boolean cancel(UUID jobId) {
        DroneJob job = jobs.get(jobId);
        if (job == null) return false;
        job.cancel();
        return true;
    }

    public Optional<DroneJob> get(UUID jobId) { return Optional.ofNullable(jobs.get(jobId)); }

    public Collection<DroneJob> snapshot() {
        List<DroneJob> result = new ArrayList<>(jobs.values());
        result.sort(NEXT_JOB_ORDER);
        return Collections.unmodifiableList(result);
    }

    NBTTagList writeToNbt() {
        NBTTagList list = new NBTTagList();
        for (DroneJob job : snapshot()) list.appendTag(job.writeToNbt());
        return list;
    }

    void readFromNbt(NBTTagList list, long worldTime) {
        jobs.clear();
        for (int index = 0; index < Math.min(2048, list.tagCount()); index++) {
            DroneJob job = DroneJob.readFromNbt(list.getCompoundTagAt(index));
            if (job == null || jobs.containsKey(job.getJobId())) continue;
            job.recoverAfterLoad(worldTime);
            jobs.put(job.getJobId(), job);
        }
    }
}
