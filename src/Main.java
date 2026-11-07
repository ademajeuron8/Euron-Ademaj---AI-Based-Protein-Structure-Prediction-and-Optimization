import java.util.Random;

class Position {
    int x;
    int y;
    int z;

    Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    boolean isNeighbor(Position other) {
        long distance = Math.abs((long) x - other.x)
                      + Math.abs((long) y - other.y)
                      + Math.abs((long) z - other.z);

        return distance == 1;
    }

    boolean isSamePosition(Position other) {
        return x == other.x && y == other.y && z == other.z;
    }
}

public class Main {
    public static void main(String[] args) {
        String hpSequence = "HHHH";
        Protein protein = new Protein(hpSequence);
        for (int i = 0; i < hpSequence.length(); i++) {
            protein.addPosition(new Position(i, 0, 0));
        }

        System.out.println("Simulated Annealing");
        System.out.println("Energjia fillestare: " + protein.calculateEnergy());
        Protein best = SimulatedAnnealing.optimize(protein, 1000, 2.0, 0.995, new Random(2025));
        showResult(best, hpSequence);

        System.out.println();
        System.out.println("Beam Search");
        System.out.println("Gjeresia: 30");
        Protein beamResult = BeamSearch.search(hpSequence, 30);
        if (beamResult == null) {
            System.out.println("Kerkimi nuk arriti te ndertoje nje strukture te plote.");
        } else {
            showResult(beamResult, hpSequence);
        }
    }

    private static void showResult(Protein result, String hpSequence) {
        System.out.println("Energjia e gjetur: " + result.calculateEnergy());
        for (int i = 0; i < hpSequence.length(); i++) {
            Position position = result.getPosition(i);
            System.out.println("Pozicioni " + i + ": (" + position.x
                    + "," + position.y + "," + position.z + ")");
        }
        System.out.println("E plote: " + result.isComplete());
        System.out.println("E vlefshme: " + result.isValid());
    }
}
