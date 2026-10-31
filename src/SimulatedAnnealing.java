import java.util.Random;

public class SimulatedAnnealing {
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
