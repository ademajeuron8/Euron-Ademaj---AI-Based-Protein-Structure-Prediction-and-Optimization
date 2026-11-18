import java.util.Random;
import java.util.function.LongSupplier;

public class SimulatedAnnealing {
    public static Protein optimize(Protein initial, int maxIterations, double initialTemperature,
                                   double coolingRate, Random random) {
        return optimizeWithStats(initial, maxIterations, initialTemperature, coolingRate, random).getBestProtein();
    }

    public static Protein optimize(Protein initial, int maxIterations, double initialTemperature,
                                   double coolingRate, Random random, long timeLimitMillis) {
        return optimizeWithStats(initial, maxIterations, initialTemperature, coolingRate, random,
                timeLimitMillis).getBestProtein();
    }

    public static AnnealingResult optimizeWithStats(Protein initial, int maxIterations, double initialTemperature,
                                                   double coolingRate, Random random) {
        return optimizeWithStats(initial, maxIterations, initialTemperature, coolingRate, random, 0);
    }

    public static AnnealingResult optimizeWithStats(Protein initial, int maxIterations, double initialTemperature,
                                                   double coolingRate, Random random, long timeLimitMillis) {
        return optimizeWithStats(initial, maxIterations, initialTemperature, coolingRate, random,
                timeLimitMillis, System::nanoTime);
    }

    // Burimi i kohes mund te zevendesohet nga testet ne te njejten pakete.
    static AnnealingResult optimizeWithStats(Protein initial, int maxIterations, double initialTemperature,
                                            double coolingRate, Random random, long timeLimitMillis,
                                            LongSupplier nanoTime) {
        SearchTimeLimit limit = new SearchTimeLimit(timeLimitMillis, nanoTime);
        if (initial == null) {
            throw new IllegalArgumentException("Proteina fillestare nuk mund te jete null.");
        }
        if (random == null) {
            throw new IllegalArgumentException("Random nuk mund te jete null.");
        }
        if (!initial.isComplete()) {
            throw new IllegalArgumentException("Proteina fillestare duhet te jete e plote dhe e vlefshme.");
        }
        if (maxIterations < 0) {
            throw new IllegalArgumentException("Numri i perpjekjeve nuk mund te jete negativ.");
        }
        if (initialTemperature < 0 || !Double.isFinite(initialTemperature)) {
            throw new IllegalArgumentException("Temperatura fillestare duhet te jete e fundme dhe jo negative.");
        }
        if (!Double.isFinite(coolingRate) || coolingRate <= 0 || coolingRate >= 1) {
            throw new IllegalArgumentException("Koeficienti i ftohjes duhet te jete i fundem dhe midis 0 dhe 1.");
        }

        Protein current = initial.copy();
        int currentEnergy = current.calculateEnergy();
        Protein best = current.copy();
        int bestEnergy = currentEnergy;
        double temperature = initialTemperature;
        int attempts = 0;
        int validCandidates = 0;
        int acceptedMoves = 0;

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            if (limit.isExpired()) {
                return new AnnealingResult(best, attempts, validCandidates, acceptedMoves, StopReason.TIME_LIMIT);
            }
            attempts++;
            Protein candidate = current.createRandomNeighbor(random);
            if (candidate != null) {
                validCandidates++;
                int candidateEnergy = candidate.calculateEnergy();
                if (shouldAccept(currentEnergy, candidateEnergy, temperature, random)) {
                    current = candidate;
                    currentEnergy = candidateEnergy;
                    acceptedMoves++;
                    if (currentEnergy < bestEnergy) {
                        best = current.copy();
                        bestEnergy = currentEnergy;
                    }
                }
            }

            // Cdo perpjekje numeron dhe ftoh, edhe kur kandidati eshte null.
            temperature *= coolingRate;
        }

        return new AnnealingResult(best, attempts, validCandidates, acceptedMoves, StopReason.ITERATION_LIMIT);
    }

    public static boolean shouldAccept(int currentEnergy, int candidateEnergy,
                                       double temperature, Random random) {
        if (random == null) {
            throw new IllegalArgumentException("Random nuk mund te jete null.");
        }
        if (temperature < 0 || !Double.isFinite(temperature)) {
            throw new IllegalArgumentException("Temperatura duhet te jete e fundme dhe jo negative.");
        }

        long delta = (long) candidateEnergy - currentEnergy;
        if (delta <= 0) {
            return true;
        }
        if (temperature == 0) {
            return false;
        }

        double probability = Math.exp(-((double) delta) / temperature);
        return random.nextDouble() < probability;
    }
}
