public class AnnealingResult {
    private final Protein bestProtein;
    private final int attempts;
    private final int validCandidates;
    private final int acceptedMoves;
    private final StopReason stopReason;
    private final int collisionProposals;
    private final int disconnectedProposals;
    private final int unchangedProposals;
    private final int noMoveProposals;
    private final int outOfRangeProposals;

    public AnnealingResult(Protein bestProtein, int attempts, int validCandidates, int acceptedMoves) {
        this(bestProtein, attempts, validCandidates, acceptedMoves, StopReason.ITERATION_LIMIT);
    }

    public AnnealingResult(Protein bestProtein, int attempts, int validCandidates, int acceptedMoves,
                           StopReason stopReason) {
        this(bestProtein, attempts, validCandidates, acceptedMoves, stopReason, 0, 0, 0, 0, 0);
    }

    public AnnealingResult(Protein bestProtein, int attempts, int validCandidates, int acceptedMoves,
                           StopReason stopReason, int collisionProposals, int disconnectedProposals,
                           int unchangedProposals, int noMoveProposals, int outOfRangeProposals) {
        this.bestProtein = bestProtein.copy();
        this.attempts = attempts;
        this.validCandidates = validCandidates;
        this.acceptedMoves = acceptedMoves;
        this.stopReason = stopReason;
        this.collisionProposals = collisionProposals;
        this.disconnectedProposals = disconnectedProposals;
        this.unchangedProposals = unchangedProposals;
        this.noMoveProposals = noMoveProposals;
        this.outOfRangeProposals = outOfRangeProposals;
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

    public int getGeometricallyInvalidProposals() {
        return collisionProposals + disconnectedProposals;
    }

    public int getCollisionProposals() {
        return collisionProposals;
    }

    public int getDisconnectedProposals() {
        return disconnectedProposals;
    }

    public int getUnchangedProposals() {
        return unchangedProposals;
    }

    public int getNoMoveProposals() {
        return noMoveProposals;
    }

    public int getOutOfRangeProposals() {
        return outOfRangeProposals;
    }
}
