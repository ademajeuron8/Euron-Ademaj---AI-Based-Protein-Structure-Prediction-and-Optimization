public class AnnealingResult {
    private final Protein bestProtein;
    private final int attempts;
    private final int validCandidates;
    private final int acceptedMoves;
    private final StopReason stopReason;

    public AnnealingResult(Protein bestProtein, int attempts, int validCandidates, int acceptedMoves) {
        this(bestProtein, attempts, validCandidates, acceptedMoves, StopReason.ITERATION_LIMIT);
    }

    public AnnealingResult(Protein bestProtein, int attempts, int validCandidates, int acceptedMoves,
                           StopReason stopReason) {
        this.bestProtein = bestProtein.copy();
        this.attempts = attempts;
        this.validCandidates = validCandidates;
        this.acceptedMoves = acceptedMoves;
        this.stopReason = stopReason;
    }

    public Protein getBestProtein() {
        return bestProtein.copy();
    }

    public int getAttempts() {
        return attempts;
    }

    public int getValidCandidates() {
        return validCandidates;
    }

    public int getAcceptedMoves() {
        return acceptedMoves;
    }

    public StopReason getStopReason() {
        return stopReason;
    }
}
