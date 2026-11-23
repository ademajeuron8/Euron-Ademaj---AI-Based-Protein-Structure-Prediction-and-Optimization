import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExperimentRunner {
    static final int CONTACT_WEIGHT = 1;
    static final double INITIAL_TEMPERATURE = 2.0;
    static final double COOLING_RATE = 0.995;
    static final long TIME_LIMIT_MILLIS = 10;
    private static final long[] SEEDS = {2025, 2026, 2027, 2028, 2029};
    private static final String[] SEQUENCE_IDS = {"short", "medium", "long"};
    private static final String[] SEQUENCES = {"HHHH", "HHPHPHHPPHPH", "HHPHPHHPPHPHHHPHPPHHPPHP"};

    // iterations/seed perdoren nga SA; beamWidth/removeRotatedCopies nga Beam.
    record Trial(String group, String sequenceId, String sequence, String algorithm, int iterations,
                 Long seed, int beamWidth, boolean removeRotatedCopies, long timeLimitMillis) { }

    public static void main(String[] args) {
        Path root = args.length == 0 ? Path.of("experiments") : Path.of(args[0]);
        try {
            Path report = runExperiments(root);
            System.out.println("Rezultatet CSV: " + report.resolveSibling("results.csv"));
            System.out.println("Raporti HTML: " + report);
        } catch (IOException e) {
            System.out.println("Eksperimentet nuk u ruajten: " + e.getMessage());
        }
    }

    public static Path runExperiments(Path root) throws IOException {
        System.out.println("Ngrohja e JVM-se: nje kerkim SA dhe nje Beam, jashte rezultateve.");
        runTrial(new Trial("warmup", "medium", SEQUENCES[1], "SA", 1000, SEEDS[0], 0, false, 0));
        runTrial(new Trial("warmup", "medium", SEQUENCES[1], "Beam", 0, null, 30, true, 0));
        ArrayList<ExperimentRow> rows = new ArrayList<>();
        List<Trial> plan = createPlan();
        for (int i = 0; i < plan.size(); i++) {
            Trial trial = plan.get(i);
            rows.add(runTrial(trial));
            System.out.println((i + 1) + "/" + plan.size() + ": " + trial.group() + " "
                    + trial.sequenceId() + " " + trial.algorithm());
        }
        Path absoluteRoot = root.toAbsolutePath().normalize();
        Files.createDirectories(absoluteRoot);
        String prefix = "run-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + "-";
        Path directory = Files.createTempDirectory(absoluteRoot, prefix);
        Path csv = directory.resolve("results.csv");
        ExperimentRow.writeCsv(csv, rows);
        Path report = directory.resolve("report.html");
        // Raporti e lexon CSV-ne e saposhkruar, jo nje liste tjeter matjesh.
        ExperimentReport.write(csv, report);
        System.out.println("U ruajten " + rows.size() + " prova te matura; ngrohja nuk perfshihet.");
        return report;
    }

    static List<Trial> createPlan() {
        ArrayList<Trial> plan = new ArrayList<>();
        for (long limit : new long[]{0, TIME_LIMIT_MILLIS}) {
            String group = limit == 0 ? "baseline" : "time_limited";
            for (int i = 0; i < SEQUENCES.length; i++) {
                for (long seed : SEEDS) {
                    plan.add(new Trial(group, SEQUENCE_IDS[i], SEQUENCES[i], "SA", 1000, seed, 0, false, limit));
                }
                for (int width : new int[]{10, 30}) {
                    plan.add(new Trial(group, SEQUENCE_IDS[i], SEQUENCES[i], "Beam", 0, null, width, true, limit));
                }
            }
        }
        // Krahasimi me kopjet ndezur eshte rreshti baseline medium / B=30.
        plan.add(new Trial("rotation", "medium", SEQUENCES[1], "Beam", 0, null, 30, false, 0));
        for (int iterations : new int[]{100, 500, 1000}) {
            plan.add(new Trial("progress", "long", SEQUENCES[2], "SA", iterations, SEEDS[0], 0, false, 0));
        }
        return plan;
    }

    static Protein initialProtein(String sequence) {
        Protein initial = new Protein(sequence);
        for (int i = 0; i < sequence.length(); i++) {
            initial.addPosition(new Position(i, 0, 0));
        }
        return initial;
    }

    static ExperimentRow runTrial(Trial trial) {
        if (trial.algorithm().equals("SA")) {
            Protein initial = initialProtein(trial.sequence());
            Random random = new Random(trial.seed());
            long start = System.nanoTime();
            AnnealingResult result = SimulatedAnnealing.optimizeWithStats(initial, trial.iterations(),
                    INITIAL_TEMPERATURE, COOLING_RATE, random, trial.timeLimitMillis(), CONTACT_WEIGHT);
            long elapsed = System.nanoTime() - start;
            return ExperimentRow.fromAnnealing(trial, CONTACT_WEIGHT, INITIAL_TEMPERATURE, COOLING_RATE, elapsed, result);
        }
        if (!trial.algorithm().equals("Beam")) {
            throw new IllegalArgumentException("Algoritem i panjohur: " + trial.algorithm());
        }
        long start = System.nanoTime();
        BeamSearchResult result = BeamSearch.searchWithStats(trial.sequence(), trial.beamWidth(),
                trial.timeLimitMillis(), trial.removeRotatedCopies(), CONTACT_WEIGHT);
        long elapsed = System.nanoTime() - start;
        return ExperimentRow.fromBeam(trial, CONTACT_WEIGHT, elapsed, result);
    }
}
