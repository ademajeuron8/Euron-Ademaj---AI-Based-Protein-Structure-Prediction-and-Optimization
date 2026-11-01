import java.util.Random;

public class SimulatedAnnealing {
    public static Protein optimize(Protein initial, int maxIterations, double initialTemperature,
                                   double coolingRate, Random random) {
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

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            Protein candidate = current.createRandomNeighbor(random);
            if (candidate != null) {
                int candidateEnergy = candidate.calculateEnergy();
                if (shouldAccept(currentEnergy, candidateEnergy, temperature, random)) {
                    current = candidate;
                    currentEnergy = candidateEnergy;
                    if (currentEnergy < bestEnergy) {
                        best = current.copy();
                        bestEnergy = currentEnergy;
                    }
                }
            }

            // Cdo perpjekje numeron dhe ftoh, edhe kur kandidati eshte null.
            temperature *= coolingRate;
        }

        return best;
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
