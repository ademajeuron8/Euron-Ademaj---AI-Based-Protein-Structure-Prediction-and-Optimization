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
        String hpSequence;
        Protein protein;
        int maxIterations;
        double initialTemperature;
        double coolingRate;
        long seed;
        int beamWidth;
        long timeLimitMillis;
        boolean removeRotatedCopies;
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
            System.out.println("Shtyp Enter per te ruajtur vleren ne kllapa. Numrat dhjetore shkruhen me pike.");

            String parameter = "Numri i perpjekjeve per Simulated Annealing";
            try {
                String value = readParameter(scanner, parameter, "1000");
                if (value == null) {
                    return;
                }
                maxIterations = Integer.parseInt(value);
                if (maxIterations < 0) {
                    throw new IllegalArgumentException("Duhet te jete jo negativ.");
                }

                parameter = "Temperatura fillestare";
                value = readParameter(scanner, parameter, "2.0");
                if (value == null) {
                    return;
                }
                initialTemperature = Double.parseDouble(value);
                if (!Double.isFinite(initialTemperature) || initialTemperature < 0) {
                    throw new IllegalArgumentException("Duhet te jete nje numer i fundem jo negativ.");
                }

                parameter = "Faktori i ftohjes";
                value = readParameter(scanner, parameter, "0.995");
                if (value == null) {
                    return;
                }
                coolingRate = Double.parseDouble(value);
                if (!Double.isFinite(coolingRate) || coolingRate <= 0 || coolingRate >= 1) {
                    throw new IllegalArgumentException("Duhet te jete nje numer i fundem midis 0 dhe 1, pa kufijte.");
                }

                parameter = "Seed per Simulated Annealing";
                value = readParameter(scanner, parameter, "2025");
                if (value == null) {
                    return;
                }
                seed = Long.parseLong(value);

                parameter = "Gjeresia e Beam Search";
                value = readParameter(scanner, parameter, "30");
                if (value == null) {
                    return;
                }
                beamWidth = Integer.parseInt(value);
                if (beamWidth <= 0) {
                    throw new IllegalArgumentException("Duhet te jete pozitive.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Vlere e pavlefshme per " + parameter
                        + ": numri nuk eshte i vlefshem ose eshte jashte kufijve te lejuar.");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("Vlere e pavlefshme per " + parameter + ": " + e.getMessage());
                return;
            }
            Long limit = readTimeLimit(scanner);
            if (limit == null) {
                return;
            }
            timeLimitMillis = limit;
            Boolean rotationChoice = readRotationChoice(scanner);
            if (rotationChoice == null) {
                return;
            }
            removeRotatedCopies = rotationChoice;
        }

        System.out.println("Parametrat e perdorur:");
        System.out.println("SA: perpjekje=" + maxIterations + ", temperatura=" + initialTemperature
                + ", ftohja=" + coolingRate + ", seed=" + seed);
        System.out.println("Beam Search: gjeresia=" + beamWidth
                + ", heqja e kopjeve te rrotulluara=" + (removeRotatedCopies ? "po" : "jo"));
        System.out.println("Kufiri kohor per secilin algoritëm: "
                + (timeLimitMillis == 0 ? "pa kufi" : timeLimitMillis + " ms"));
        for (int i = 0; i < hpSequence.length(); i++) {
            protein.addPosition(new Position(i, 0, 0));
        }

        System.out.println("Simulated Annealing");
        System.out.println("Energjia fillestare: " + protein.calculateEnergy());
        Random random = new Random(seed);
        long saStart = System.nanoTime();
        AnnealingResult saStats = SimulatedAnnealing.optimizeWithStats(protein, maxIterations, initialTemperature,
                coolingRate, random, timeLimitMillis);
        long saElapsedNanos = System.nanoTime() - saStart;
        Protein best = saStats.getBestProtein();
        showResult(best, hpSequence);
        System.out.println("Perpjekjet: " + saStats.getAttempts());
        System.out.println("Kandidatet e vlefshem: " + saStats.getValidCandidates());
        System.out.println("Levizjet e pranuara: " + saStats.getAcceptedMoves());
        System.out.println("Perpjekjet pa kandidat: " + (saStats.getAttempts() - saStats.getValidCandidates()));
        System.out.println("Kandidatet e refuzuar nga rregulli i pranimit: "
                + (saStats.getValidCandidates() - saStats.getAcceptedMoves()));
        System.out.println("Arsyeja e ndalimit: " + saStats.getStopReason().getDescription());
        System.out.printf(Locale.ROOT, "Koha e ekzekutimit: %.3f ms%n", saElapsedNanos / 1_000_000.0);

        System.out.println();
        System.out.println("Beam Search");
        System.out.println("Gjeresia: " + beamWidth);
        long beamStart = System.nanoTime();
        BeamSearchResult beamStats = BeamSearch.searchWithStats(hpSequence, beamWidth, timeLimitMillis,
                removeRotatedCopies);
        long beamElapsedNanos = System.nanoTime() - beamStart;
        Protein beamResult = beamStats.getBestProtein();
        if (beamResult == null) {
            System.out.println("Kerkimi nuk arriti te ndertoje nje strukture te plote.");
        } else {
            showResult(beamResult, hpSequence);
        }
        System.out.println("Strukturat e zgjeruara: " + beamStats.getExpandedStates());
        System.out.println("Kandidatet e gjeneruar: " + beamStats.getGeneratedCandidates());
        System.out.println("Kandidatet e ruajtur gjithsej gjate niveleve: " + beamStats.getRetainedCandidates());
        System.out.println("Kopjet e hequra nga rrotullimi: " + beamStats.getRotatedDuplicates());
        System.out.println("Kandidatet e larguar nga kufiri i gjeresise: " + beamStats.getPrunedCandidates());
        System.out.println("Nivelet e perfunduara: " + beamStats.getCompletedLevels());
        System.out.println("Arsyeja e ndalimit: " + beamStats.getStopReason().getDescription());
        System.out.printf(Locale.ROOT, "Koha e ekzekutimit: %.3f ms%n", beamElapsedNanos / 1_000_000.0);
    }

    private static Boolean readRotationChoice(Scanner scanner) {
        while (true) {
            System.out.println("Të hiqen kopjet e rrotulluara në Beam Search? [P/j]");
            if (!scanner.hasNextLine()) {
                return null;
            }
            String choice = scanner.nextLine().trim();
            if (choice.isEmpty() || choice.equalsIgnoreCase("p")) {
                return true;
            }
            if (choice.equalsIgnoreCase("j")) {
                return false;
            }
            System.out.println("Zgjedhje e pavlefshme. Shkruaj P ose j.");
        }
    }

    private static Long readTimeLimit(Scanner scanner) {
        System.out.println("Enter ose 0 do te thote pa kufi kohor.");
        while (true) {
            System.out.println("Kufiri kohor për secilin algoritëm në ms [Enter = pa kufi]:");
            if (!scanner.hasNextLine()) {
                return null;
            }
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                return 0L;
            }
            try {
                long limit = Long.parseLong(value);
                if (limit < 0) {
                    throw new IllegalArgumentException("Kufiri nuk mund te jete negativ.");
                }
                return limit;
            } catch (IllegalArgumentException e) {
                System.out.println("Kufiri kohor duhet te jete numer i plote jo negativ ne ms. Provo perseri.");
            }
        }
    }

    private static String readParameter(Scanner scanner, String name, String defaultValue) {
        System.out.println(name + " [" + defaultValue + "]:");
        if (!scanner.hasNextLine()) {
            return null;
        }
        String value = scanner.nextLine().trim();
        return value.isEmpty() ? defaultValue : value;
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
