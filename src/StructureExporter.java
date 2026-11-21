import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class StructureExporter {
    // Merr vetem rezultatet e gjetura; nuk nis asnje kerkim te ri.
    public static Path export(Path exportsRoot, Protein sa, Protein beam, int contactWeight,
                              StopReason beamStopReason) throws IOException {
        if (exportsRoot == null || contactWeight <= 0) {
            throw new IllegalArgumentException("Dosja duhet te jete jo null dhe pesha duhet te jete pozitive.");
        }
        boolean hasSa = sa != null && sa.isComplete();
        boolean hasBeam = beam != null && beam.isComplete();
        if (!hasSa && !hasBeam) {
            throw new IllegalArgumentException("Nuk ka strukture te plote dhe te vlefshme per eksport.");
        }
        String sequence = (hasSa ? sa : beam).getHpSequence();
        if (hasSa && hasBeam && !sequence.equals(beam.getHpSequence())) {
            throw new IllegalArgumentException("Rezultatet duhet te kene te njejten sekuence HP.");
        }
        String beamNote = hasBeam ? "" : "Beam Search nuk ka strukture te plote. Arsyeja: "
                + (beamStopReason == null ? "Nuk ka rezultat." : beamStopReason.getDescription() + ".");
        StringBuilder data = new StringBuilder("{\"sequence\":").append(quote(sequence))
                .append(",\"contactWeight\":").append(contactWeight)
                .append(",\"missingBeamReason\":").append(quote(beamNote)).append(",\"structures\":[");
        if (hasSa) {
            data.append(structureData("Simulated Annealing", "sa.csv", sa, contactWeight));
        }
        if (hasBeam) {
            if (hasSa) {
                data.append(',');
            }
            data.append(structureData("Beam Search", "beam.csv", beam, contactWeight));
        }
        data.append("]}");

        Path root = exportsRoot.toAbsolutePath().normalize();
        Files.createDirectories(root);
        String prefix = "run-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + "-";
        Path directory = Files.createTempDirectory(root, prefix);
        if (hasSa) {
            writeNew(directory.resolve("sa.csv"), csv(sa));
        }
        if (hasBeam) {
            writeNew(directory.resolve("beam.csv"), csv(beam));
        }
        Path html = directory.resolve("structure.html");
        writeNew(html, StructureViewer.html(data.toString()));
        return html;
    }

    private static void writeNew(Path file, String text) throws IOException {
        Files.writeString(file, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    private static String csv(Protein protein) {
        String sequence = protein.getHpSequence();
        StringBuilder csv = new StringBuilder("index,type,x,y,z\n");
        for (int i = 0; i < sequence.length(); i++) {
            Position p = protein.getPosition(i);
            csv.append(i).append(',').append(sequence.charAt(i)).append(',')
                    .append(p.x).append(',').append(p.y).append(',').append(p.z).append('\n');
        }
        return csv.toString();
    }

    private static String structureData(String name, String file, Protein protein, int weight) {
        StringBuilder data = new StringBuilder("{\"name\":").append(quote(name))
                .append(",\"csv\":").append(quote(file))
                // Energjia ruhet si tekst qe JavaScript te mos humbase shifrat e nje long te madh.
                .append(",\"energy\":").append(quote(Long.toString(protein.calculateEnergy(weight))))
                .append(",\"positions\":[");
        int count = protein.getHpSequence().length();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                data.append(',');
            }
            Position p = protein.getPosition(i);
            data.append('[').append(p.x).append(',').append(p.y).append(',').append(p.z).append(']');
        }
        data.append("],\"bonds\":[");
        for (int i = 0; i < count - 1; i++) {
            if (i > 0) {
                data.append(',');
            }
            data.append('[').append(i).append(',').append(i + 1).append(']');
        }
        data.append("],\"contacts\":[");
        ArrayList<int[]> contacts = protein.getHhContacts();
        for (int i = 0; i < contacts.size(); i++) {
            if (i > 0) {
                data.append(',');
            }
            int[] pair = contacts.get(i);
            data.append('[').append(pair[0]).append(',').append(pair[1]).append(']');
        }
        return data.append("]}").toString();
    }

    private static String quote(String text) {
        return '"' + text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n").replace("<", "\\u003c") + '"';
    }
}
