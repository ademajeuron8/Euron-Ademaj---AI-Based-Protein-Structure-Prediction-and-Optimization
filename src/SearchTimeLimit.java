import java.util.function.LongSupplier;

final class SearchTimeLimit {
    private final long limitMillis;
    private final LongSupplier nanoTime;
    private final long startedAt;

    SearchTimeLimit(long limitMillis, LongSupplier nanoTime) {
        if (limitMillis < 0) {
            throw new IllegalArgumentException("Kufiri kohor nuk mund te jete negativ.");
        }
        if (nanoTime == null) {
            throw new IllegalArgumentException("Burimi i kohes nuk mund te jete null.");
        }
        this.limitMillis = limitMillis;
        this.nanoTime = nanoTime;
        this.startedAt = limitMillis == 0 ? 0 : nanoTime.getAsLong();
    }

    boolean isExpired() {
        if (limitMillis == 0) {
            return false;
        }
        long elapsedNanos = nanoTime.getAsLong() - startedAt;
        // Pjesetimi shmang tejmbushjen nga shumezimi i kufirit ne ms me 1_000_000.
        return elapsedNanos / 1_000_000L >= limitMillis;
    }
}
