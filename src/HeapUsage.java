import java.lang.management.ManagementFactory;

final class HeapUsage {
    static long usedBytes() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    static double toMiB(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }
}
