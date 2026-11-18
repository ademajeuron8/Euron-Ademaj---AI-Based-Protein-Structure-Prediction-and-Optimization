public class BeamSearchResult {
    private final Protein bestProtein;
    private final long expandedStates;
    private final long generatedCandidates;
    private final long retainedCandidates;
    private final long prunedCandidates;
    private final int completedLevels;
    private final StopReason stopReason;

    public BeamSearchResult(Protein bestProtein, long expandedStates, long generatedCandidates,
                            long retainedCandidates, long prunedCandidates, int completedLevels) {
        this(bestProtein, expandedStates, generatedCandidates, retainedCandidates, prunedCandidates,
                completedLevels, bestProtein == null ? StopReason.NO_CANDIDATES : StopReason.COMPLETED);
    }

    public BeamSearchResult(Protein bestProtein, long expandedStates, long generatedCandidates,
                            long retainedCandidates, long prunedCandidates, int completedLevels,
                            StopReason stopReason) {
        this.bestProtein = bestProtein == null ? null : bestProtein.copy();
        this.expandedStates = expandedStates;
        this.generatedCandidates = generatedCandidates;
        this.retainedCandidates = retainedCandidates;
        this.prunedCandidates = prunedCandidates;
        this.completedLevels = completedLevels;
        this.stopReason = stopReason;
    }

    public Protein getBestProtein() {
        return bestProtein == null ? null : bestProtein.copy();
    }

    public long getExpandedStates() {
        return expandedStates;
    }

    public long getGeneratedCandidates() {
        return generatedCandidates;
    }

    public long getRetainedCandidates() {
        return retainedCandidates;
    }

    public long getPrunedCandidates() {
        return prunedCandidates;
    }

    public int getCompletedLevels() {
        return completedLevels;
    }

    public StopReason getStopReason() {
        return stopReason;
    }
}
