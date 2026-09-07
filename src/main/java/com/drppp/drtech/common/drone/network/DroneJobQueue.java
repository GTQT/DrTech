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
    private static final int MAX_JOBS = 2_048;
    private static final int PRUNE_TO_JOBS = 1_792;
    private final Map<UUID, DroneJob> jobs = new LinkedHashMap<>();
    private static final Comparator<DroneJob> NEXT_JOB_ORDER = Comparator
            .comparingInt(DroneJob::getPriority).reversed()
            .thenComparingLong(DroneJob::getSubmittedTick)
            .thenComparing(job -> job.getJobId().toString());

    public boolean submit(DroneJob job) {
        if (job == null || jobs.containsKey(job.getJobId())) return false;
        if (jobs.size() >= MAX_JOBS) pruneTerminalHistory(PRUNE_TO_JOBS);
        if (jobs.size() >= MAX_JOBS) return false;
        jobs.put(job.getJobId(), job);
        return true;
    }

    private void pruneTerminalHistory(int targetSize) {
        if (jobs.size() <= targetSize) return;
        List<DroneJob> removable = new ArrayList<>();
        for (DroneJob job : jobs.values()) {
            DroneJob.State state = job.getState();
            if ((state == DroneJob.State.COMPLETED || state == DroneJob.State.FAILED
                    || state == DroneJob.State.CANCELLED)
                    && job.getLogisticsStage() != DroneJob.LogisticsStage.RETURNING) removable.add(job);
        }
        removable.sort(Comparator.comparingLong(DroneJob::getSubmittedTick)
                .thenComparing(job -> job.getJobId().toString()));
        for (DroneJob job : removable) {
            if (jobs.size() <= targetSize) break;
            jobs.remove(job.getJobId());
        }
    }

    public Optional<DroneJob> takeNext(long worldTime) {
        return jobs.values().stream().filter(job -> job.isEligible(worldTime)).min(NEXT_JOB_ORDER)
                .filter(job -> job.start(worldTime));
    }

    public Optional<DroneJob> takeNextLogistics(long worldTime) {
        return jobs.values().stream().filter(DroneJob::isLogisticsJob)
                .filter(job -> job.isEligible(worldTime)).min(NEXT_JOB_ORDER)
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

    public boolean defer(UUID jobId, long worldTime, String reason, long delayTicks) {
        DroneJob job = jobs.get(jobId);
        return job != null && job.defer(worldTime, reason, delayTicks);
    }

    public boolean cancel(UUID jobId) {
        DroneJob job = jobs.get(jobId);
        return job != null && job.cancel();
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
        for (int index = 0; index < Math.min(MAX_JOBS, list.tagCount()); index++) {
            DroneJob job = DroneJob.readFromNbt(list.getCompoundTagAt(index));
            if (job == null || jobs.containsKey(job.getJobId())) continue;
            job.recoverAfterLoad(worldTime);
            jobs.put(job.getJobId(), job);
        }
    }
}
