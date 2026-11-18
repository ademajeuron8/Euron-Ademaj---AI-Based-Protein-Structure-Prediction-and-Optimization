public enum StopReason {
    ITERATION_LIMIT("Kufiri i iteracioneve"),
    COMPLETED("Kerkimi perfundoi"),
    NO_CANDIDATES("Nuk mbeten kandidate"),
    TIME_LIMIT("Kufiri kohor");

    private final String description;

    StopReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
