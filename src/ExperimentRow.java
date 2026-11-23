import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

// Nje rresht CSV. Fushat qe nuk zbatohen mbeten bosh, jo zero.
final class ExperimentRow {
    static final List<String> COLUMNS = List.of("group", "sequence_id", "sequence", "length", "algorithm",
            "contact_weight", "max_iterations", "initial_temperature", "cooling_rate", "seed", "beam_width",
            "remove_rotated_copies", "time_limit_ms", "energy", "hh_contacts", "elapsed_ms", "status", "stop_reason",
            "attempts", "valid_candidates", "accepted_moves", "expanded_states", "generated_candidates",
            "retained_candidates", "rotated_duplicates", "pruned_candidates", "completed_levels",
            "max_candidates_before_selection");
    private final LinkedHashMap<String, String> values = new LinkedHashMap<>();

    private ExperimentRow() {
        for (String column : COLUMNS) {
            values.put(column, "");
        }
    }

    String get(String column) {
        return values.get(column);
    }

    private void put(String column, Object value) {
        values.put(column, value == null ? "" : value.toString());
    }

    private static ExperimentRow base(ExperimentRunner.Trial trial, int weight, long elapsedNanos,
                                       Protein best, StopReason reason) {
        ExperimentRow row = new ExperimentRow();
        row.put("group", trial.group());
        row.put("sequence_id", trial.sequenceId());
        row.put("sequence", trial.sequence());
        row.put("length", trial.sequence().length());
        row.put("algorithm", trial.algorithm());
        row.put("contact_weight", weight);
        row.put("time_limit_ms", trial.timeLimitMillis());
        row.put("elapsed_ms", String.format(Locale.ROOT, "%.6f", elapsedNanos / 1_000_000.0));
        boolean complete = best != null && best.isComplete();
        row.put("status", complete ? "COMPLETE" : "NO_COMPLETE_STRUCTURE");
        row.put("stop_reason", reason.name());
        if (complete) {
            row.put("energy", best.calculateEnergy(weight));
            row.put("hh_contacts", best.getHhContacts().size());
        }
        return row;
    }

    static ExperimentRow fromAnnealing(ExperimentRunner.Trial trial, int weight, double temperature,
                                       double coolingRate, long elapsedNanos, AnnealingResult result) {
        ExperimentRow row = base(trial, weight, elapsedNanos, result.getBestProtein(), result.getStopReason());
        row.put("max_iterations", trial.iterations());
        row.put("initial_temperature", temperature);
        row.put("cooling_rate", coolingRate);
        row.put("seed", trial.seed());
        row.put("attempts", result.getAttempts());
        row.put("valid_candidates", result.getValidCandidates());
        row.put("accepted_moves", result.getAcceptedMoves());
        return row;
    }

    static ExperimentRow fromBeam(ExperimentRunner.Trial trial, int weight, long elapsedNanos, BeamSearchResult result) {
        ExperimentRow row = base(trial, weight, elapsedNanos, result.getBestProtein(), result.getStopReason());
        row.put("beam_width", trial.beamWidth());
        row.put("remove_rotated_copies", trial.removeRotatedCopies());
        row.put("expanded_states", result.getExpandedStates());
        row.put("generated_candidates", result.getGeneratedCandidates());
        row.put("retained_candidates", result.getRetainedCandidates());
        row.put("rotated_duplicates", result.getRotatedDuplicates());
        row.put("pruned_candidates", result.getPrunedCandidates());
        row.put("completed_levels", result.getCompletedLevels());
        row.put("max_candidates_before_selection", result.getMaxCandidatesBeforeSelection());
        return row;
    }

    static void writeCsv(Path file, List<ExperimentRow> rows) throws IOException {
        StringBuilder csv = new StringBuilder(String.join(",", COLUMNS)).append('\n');
        for (ExperimentRow row : rows) {
            // Vlerat jane vetem etiketa fikse, HP, enum-e dhe numra: nuk permbajne presje ose rreshta te rinj.
            csv.append(String.join(",", row.values.values())).append('\n');
        }
        Files.writeString(file, csv, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    static List<ExperimentRow> readCsv(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !lines.get(0).equals(String.join(",", COLUMNS))) {
            throw new IOException("Kolonat e results.csv nuk perputhen me formatin e eksperimenteve.");
        }
        ArrayList<ExperimentRow> rows = new ArrayList<>();
        for (int line = 1; line < lines.size(); line++) {
            String[] fields = lines.get(line).split(",", -1);
            if (fields.length != COLUMNS.size()) {
                throw new IOException("Numer i pasakte kolonash ne rreshtin " + (line + 1));
            }
            ExperimentRow row = new ExperimentRow();
            for (int i = 0; i < fields.length; i++) {
                row.put(COLUMNS.get(i), fields[i]);
            }
            rows.add(row);
        }
        return rows;
    }
}
