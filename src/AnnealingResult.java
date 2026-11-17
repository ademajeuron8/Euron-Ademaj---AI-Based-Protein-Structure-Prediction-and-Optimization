public class AnnealingResult {
    private final Protein bestProtein;
    private final int attempts;
    private final int validCandidates;
    private final int acceptedMoves;

    public AnnealingResult(Protein bestProtein, int attempts, int validCandidates, int acceptedMoves) {
        this.bestProtein = bestProtein.copy();
        this.attempts = attempts;
        this.validCandidates = validCandidates;
        this.acceptedMoves = acceptedMoves;
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
}
