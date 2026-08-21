package com.drppp.drtech.common.multiblock.mover;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.DrtConfig;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/** Bounded in-memory transaction timings used for server-side mover benchmarking. */
public final class MoverPerformanceMetrics {
    private static final int CAPACITY = 128;
    private static final Deque<Sample> SAMPLES = new ArrayDeque<>(CAPACITY);

    private MoverPerformanceMetrics() {
    }

    public static Builder begin(MoverSession session) {
        return new Builder(session);
    }

    public static synchronized void record(Sample sample) {
        while (SAMPLES.size() >= CAPACITY) SAMPLES.removeFirst();
        SAMPLES.addLast(sample);
        if (sample.totalMillis >= DrtConfig.MultiblockMover.performanceWarnMillis) {
            DrTechMain.LOGGER.warn("Slow multiblock mover transaction {}: blocks={}, tiles={}, success={}, " +
                            "total={}ms [selection={} capture={} validate={} snapshot={} journal={} " +
                            "commit={} rollback={}]",
                    sample.transactionId, sample.blockCount, sample.tileEntityCount, sample.success,
                    sample.totalMillis, sample.selectionCaptureMillis, sample.sourceCaptureMillis,
                    sample.validationMillis, sample.worldSnapshotMillis, sample.journalMillis,
                    sample.commitMillis, sample.rollbackMillis);
        }
    }

    public static synchronized Summary summarize() {
        if (SAMPLES.isEmpty()) return Summary.EMPTY;
        int successes = 0;
        int failures = 0;
        int rollbacks = 0;
        long total = 0L;
        long selection = 0L;
        long capture = 0L;
        long validation = 0L;
        long snapshot = 0L;
        long journal = 0L;
        long commit = 0L;
        long rollback = 0L;
        long max = 0L;
        List<Long> totals = new ArrayList<>(SAMPLES.size());
        for (Sample sample : SAMPLES) {
            if (sample.success) successes++; else failures++;
            if (sample.rollbackAttempted) rollbacks++;
            total += sample.totalMillis;
            selection += sample.selectionCaptureMillis;
            capture += sample.sourceCaptureMillis;
            validation += sample.validationMillis;
            snapshot += sample.worldSnapshotMillis;
            journal += sample.journalMillis;
            commit += sample.commitMillis;
            rollback += sample.rollbackMillis;
            max = Math.max(max, sample.totalMillis);
            totals.add(sample.totalMillis);
        }
        Collections.sort(totals);
        int p95Index = Math.max(0, (int) Math.ceil(totals.size() * 0.95D) - 1);
        int count = SAMPLES.size();
        return new Summary(count, successes, failures, rollbacks,
                total / count, totals.get(p95Index), max,
                selection / count, capture / count, validation / count,
                snapshot / count, journal / count, commit / count, rollback / count);
    }

    public static synchronized int clear() {
        int count = SAMPLES.size();
        SAMPLES.clear();
        return count;
    }

    public static final class Builder {
        private final UUID transactionId;
        private final int blockCount;
        private final int tileEntityCount;
        private final long selectionCaptureNanos;
        private final long startedNanos = System.nanoTime();
        private long sourceCaptureNanos;
        private long validationNanos;
        private long worldSnapshotNanos;
        private long journalNanos;
        private long commitNanos;
        private long rollbackNanos;
        private boolean rollbackAttempted;
        private boolean success;

        private Builder(MoverSession session) {
            transactionId = session.getId();
            blockCount = session.getSnapshot().getBlockCount();
            tileEntityCount = session.getSnapshot().getTileEntityCount();
            selectionCaptureNanos = session.getCaptureNanos();
        }

        public void sourceCapture(long nanos) { sourceCaptureNanos = nanos; }
        public void validation(long nanos) { validationNanos = nanos; }
        public void worldSnapshot(long nanos) { worldSnapshotNanos = nanos; }
        public void journal(long nanos) { journalNanos = nanos; }
        public void commit(long nanos) { commitNanos = nanos; }

        public void rollback(long nanos) {
            rollbackAttempted = true;
            rollbackNanos = nanos;
        }

        public Sample finish(boolean successful) {
            success = successful;
            return new Sample(this, System.nanoTime() - startedNanos);
        }
    }

    public static final class Sample {
        public final UUID transactionId;
        public final int blockCount;
        public final int tileEntityCount;
        public final boolean success;
        public final boolean rollbackAttempted;
        public final long totalMillis;
        public final long selectionCaptureMillis;
        public final long sourceCaptureMillis;
        public final long validationMillis;
        public final long worldSnapshotMillis;
        public final long journalMillis;
        public final long commitMillis;
        public final long rollbackMillis;

        private Sample(Builder builder, long totalNanos) {
            transactionId = builder.transactionId;
            blockCount = builder.blockCount;
            tileEntityCount = builder.tileEntityCount;
            success = builder.success;
            rollbackAttempted = builder.rollbackAttempted;
            totalMillis = millis(totalNanos);
            selectionCaptureMillis = millis(builder.selectionCaptureNanos);
            sourceCaptureMillis = millis(builder.sourceCaptureNanos);
            validationMillis = millis(builder.validationNanos);
            worldSnapshotMillis = millis(builder.worldSnapshotNanos);
            journalMillis = millis(builder.journalNanos);
            commitMillis = millis(builder.commitNanos);
            rollbackMillis = millis(builder.rollbackNanos);
        }
    }

    public static final class Summary {
        private static final Summary EMPTY = new Summary(0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        public final int count;
        public final int successes;
        public final int failures;
        public final int rollbacks;
        public final long averageTotalMillis;
        public final long p95TotalMillis;
        public final long maxTotalMillis;
        public final long averageSelectionMillis;
        public final long averageCaptureMillis;
        public final long averageValidationMillis;
        public final long averageSnapshotMillis;
        public final long averageJournalMillis;
        public final long averageCommitMillis;
        public final long averageRollbackMillis;

        private Summary(int count, int successes, int failures, int rollbacks,
                        long averageTotalMillis, long p95TotalMillis, long maxTotalMillis,
                        long averageSelectionMillis, long averageCaptureMillis,
                        long averageValidationMillis, long averageSnapshotMillis,
                        long averageJournalMillis, long averageCommitMillis,
                        long averageRollbackMillis) {
            this.count = count;
            this.successes = successes;
            this.failures = failures;
            this.rollbacks = rollbacks;
            this.averageTotalMillis = averageTotalMillis;
            this.p95TotalMillis = p95TotalMillis;
            this.maxTotalMillis = maxTotalMillis;
            this.averageSelectionMillis = averageSelectionMillis;
            this.averageCaptureMillis = averageCaptureMillis;
            this.averageValidationMillis = averageValidationMillis;
            this.averageSnapshotMillis = averageSnapshotMillis;
            this.averageJournalMillis = averageJournalMillis;
            this.averageCommitMillis = averageCommitMillis;
            this.averageRollbackMillis = averageRollbackMillis;
        }
    }

    private static long millis(long nanos) {
        return nanos <= 0L ? 0L : nanos / 1_000_000L;
    }
}
