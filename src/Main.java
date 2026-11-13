import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

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
        String inputType;
        String sequenceInput;
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Zgjidh llojin e sekuences:");
            System.out.println("1. Sekuence HP");
            System.out.println("2. Sekuence aminoacidesh");
            if (!scanner.hasNextLine()) {
                return;
            }
            inputType = scanner.nextLine().trim();
            if (!inputType.equals("1") && !inputType.equals("2")) {
                System.out.println("Zgjedhje e pavlefshme. Zgjidh 1 ose 2.");
                return;
            }
            if (inputType.equals("1")) {
                System.out.println("Shkruaj sekuencen HP (vetem H dhe P):");
            } else {
                System.out.println("Shkruaj sekuencen e aminoacideve:");
            }
            if (!scanner.hasNextLine()) {
                return;
            }
            sequenceInput = scanner.nextLine();
        }

        String hpSequence;
        Protein protein;
        try {
            if (inputType.equals("1")) {
                hpSequence = sequenceInput.trim().toUpperCase(Locale.ROOT);
            } else {
                hpSequence = AminoAcidConverter.toHp(sequenceInput);
            }
            protein = new Protein(hpSequence);
        } catch (IllegalArgumentException e) {
            System.out.println("Sekuence e pavlefshme: " + e.getMessage());
            return;
        }

        System.out.println("Lloji i sekuences: " + (inputType.equals("1") ? "HP" : "Aminoacide"));
        if (inputType.equals("2")) {
            System.out.println("Sekuenca e aminoacideve: " + sequenceInput.trim().toUpperCase(Locale.ROOT));
        }
        System.out.println("Sekuenca HP: " + hpSequence);
        System.out.println("Gjatesia: " + hpSequence.length());
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
