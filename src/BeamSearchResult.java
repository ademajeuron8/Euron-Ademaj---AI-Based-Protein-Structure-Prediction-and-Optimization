public class BeamSearchResult {
    private final Protein bestProtein;
    private final long expandedStates;
    private final long generatedCandidates;
    private final long retainedCandidates;
    private final long rotatedDuplicates;
    private final long prunedCandidates;
    private final int completedLevels;
    private final StopReason stopReason;
    private final int maxCandidatesBeforeSelection;
    private final long extensionProposals;
    private final long collisionProposals;
    private final long outOfRangeProposals;

    public BeamSearchResult(Protein bestProtein, long expandedStates, long generatedCandidates,
                            long retainedCandidates, long prunedCandidates, int completedLevels) {
        this(bestProtein, expandedStates, generatedCandidates, retainedCandidates, prunedCandidates,
                completedLevels, bestProtein == null ? StopReason.NO_CANDIDATES : StopReason.COMPLETED);
    }

    public BeamSearchResult(Protein bestProtein, long expandedStates, long generatedCandidates,
                            long retainedCandidates, long prunedCandidates, int completedLevels,
                            StopReason stopReason) {
        this(bestProtein, expandedStates, generatedCandidates, retainedCandidates, 0,
                prunedCandidates, completedLevels, stopReason);
    }

    public BeamSearchResult(Protein bestProtein, long expandedStates, long generatedCandidates,
                            long retainedCandidates, long rotatedDuplicates, long prunedCandidates,
                            int completedLevels, StopReason stopReason) {
        this(bestProtein, expandedStates, generatedCandidates, retainedCandidates, rotatedDuplicates,
                prunedCandidates, completedLevels, stopReason, 0);
    }

    public BeamSearchResult(Protein bestProtein, long expandedStates, long generatedCandidates,
                            long retainedCandidates, long rotatedDuplicates, long prunedCandidates,
                            int completedLevels, StopReason stopReason, int maxCandidatesBeforeSelection) {
        this(bestProtein, expandedStates, generatedCandidates, retainedCandidates, rotatedDuplicates,
                prunedCandidates, completedLevels, stopReason, maxCandidatesBeforeSelection, 0, 0, 0);
    }

    public BeamSearchResult(Protein bestProtein, long expandedStates, long generatedCandidates,
                            long retainedCandidates, long rotatedDuplicates, long prunedCandidates,
                            int completedLevels, StopReason stopReason, int maxCandidatesBeforeSelection,
                            long extensionProposals, long collisionProposals, long outOfRangeProposals) {
        this.bestProtein = bestProtein == null ? null : bestProtein.copy();
        this.expandedStates = expandedStates;
        this.generatedCandidates = generatedCandidates;
        this.retainedCandidates = retainedCandidates;
        this.rotatedDuplicates = rotatedDuplicates;
        this.prunedCandidates = prunedCandidates;
        this.completedLevels = completedLevels;
        this.stopReason = stopReason;
        this.maxCandidatesBeforeSelection = maxCandidatesBeforeSelection;
        this.extensionProposals = extensionProposals;
        this.collisionProposals = collisionProposals;
        this.outOfRangeProposals = outOfRangeProposals;
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

    public long getRotatedDuplicates() {
        return rotatedDuplicates;
    }

    public int getCompletedLevels() {
        return completedLevels;
    }

    public StopReason getStopReason() {
        return stopReason;
    }

    public int getMaxCandidatesBeforeSelection() {
        return maxCandidatesBeforeSelection;
    }

    public long getExtensionProposals() {
        return extensionProposals;
    }

    public long getGeometricallyInvalidProposals() {
        return collisionProposals;
    }

    public long getCollisionProposals() {
        return collisionProposals;
    }

    public long getOutOfRangeProposals() {
        return outOfRangeProposals;
    }

    public Double getEliminatedCandidatesPercentage() {
        if (generatedCandidates == 0) {
            return null;
        }
        return 100.0 * ((double) rotatedDuplicates + prunedCandidates) / generatedCandidates;
    }
}
