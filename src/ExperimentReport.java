import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class ExperimentReport {
    private record Bar(String label, Double value, String color) { }

    public static void write(Path csv, Path html) throws IOException {
        List<ExperimentRow> rows = ExperimentRow.readCsv(csv);
        StringBuilder page = new StringBuilder("""
                <!doctype html><html lang="sq"><head><meta charset="UTF-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>Eksperimentet · Protein folding</title>
                <style>
                :root{font-family:system-ui,"Segoe UI",sans-serif;color:#e6edf7;background:#0b111b;color-scheme:dark}
                *{box-sizing:border-box}body{max-width:1280px;padding:34px 24px;margin:auto;line-height:1.55}
                h1{font-size:34px;margin:8px 0}h2{font-size:25px;margin:38px 0 15px}h3{margin:0 0 8px}p{color:#b3c2d5}
                .eyebrow{color:#65dfbc;letter-spacing:2px;font-size:12px}a{color:#88e8cd;text-underline-offset:4px}
                nav{display:flex;flex-wrap:wrap;gap:18px;margin:22px 0}.note,article{background:#121e2d;border:1px solid #2a3a4e;border-radius:14px;padding:22px;margin:18px 0}
                article p{margin:6px 0 12px}code{overflow-wrap:anywhere;color:#d8e8fc}.charts{display:grid;grid-template-columns:1fr 1fr;gap:22px}
                .chart h4{font-size:14px;margin:8px 0;color:#c4d3e6}svg{width:100%;height:auto;display:block}svg text{font-family:system-ui,sans-serif;font-size:12px;fill:#dbe7f5}
                .scroll{overflow-x:auto;border:1px solid #2a3a4e;border-radius:10px}table{border-collapse:collapse;white-space:nowrap;width:100%;font-size:13px}
                th,td{text-align:left;padding:10px 13px;border-bottom:1px solid #28364a}th{background:#1c2b40;color:#e5eefb}td{color:#bdcde0}tbody tr:nth-child(even){background:#132031}
                .raw table{font-size:11px}details summary{cursor:pointer;font-weight:650;padding:12px 0}.muted{font-size:13px;color:#92a8c3}
                @media(max-width:800px){body{padding:20px 14px}.charts{grid-template-columns:1fr}h1{font-size:28px}article{padding:15px}}
                </style></head><body>
                <p class="eyebrow">PROTEIN FOLDING · MATJE REALE</p><h1>Simulated Annealing dhe Beam Search</h1>
                <p>Raporti gjenerohet nga <a href="results.csv">results.csv</a>. Vlera më negative e energjisë tregon më shumë kontakte H–H për të njëjtën sekuencë dhe peshë.</p>
                <nav><a href="#baseline">Pa kufi kohor</a><a href="#time_limited">Me kufi kohor</a><a href="#progress">Përmirësimi i SA</a><a href="#rotation">Kopjet e rrotulluara</a><a href="#metrics">Metrikat</a><a href="#raw">Të gjitha provat</a></nav>
                <div class="note">
                <strong>Si u matën</strong>
                <p>Para provave bëhet një kërkim ngrohjeje për secilin algoritëm; këto dy kërkime nuk regjistrohen. Matja me System.nanoTime përfshin vetëm thirrjen e kërkimit, përfshirë validimin e tij të brendshëm. Ndërtimi i vijës fillestare, Random, shkrimi i CSV-së dhe gjenerimi i raportit mbeten jashtë intervalit.</p>
                <p>SA përdor të njëjtën vijë fillestare për çdo seed. Raportohen energjia më e mirë dhe mediana nga pesë seed-et, ndërsa për kohën përdoret mediana. Beam pa kufi kohor është determinist: për secilin konfigurim regjistrohet një provë; ndryshimet e kohës nuk janë zgjidhje të ndryshme.</p>
                <p>Kufiri kohor kontrollohet midis hapave; hapi i nisur përfundon, ndaj koha totale mund ta kalojë kufirin. SA mund të kthejë strukturë të plotë edhe kur ndalet nga koha. Beam pa strukturë të plotë ka energji dhe kontakte bosh në CSV, dhe shfaqet si “Pa strukturë”.</p>
                <p><code>max_candidates_before_selection</code> është maksimumi i kandidatëve të mbledhur nga të gjithë prindërit në një nivel, para heqjes së kopjeve dhe para beamWidth. Ky është tregues i madhësisë së listës, jo MB apo matje e gjithë memories: nuk përfshin prindërit, kopjet, çelësat dhe shpenzimet e JVM-së. Për SA kjo fushë mbetet bosh.</p>
                <p>Për të dy algoritmet, MemoryMXBean jep heap-in e përdorur të JVM-së menjëherë para/pas thirrjes së kërkimit (MiB = 1 048 576 byte). Leximet janë jashtë matjes së kohës; eksporti, CSV-ja dhe grafikët nuk përfshihen në intervalin e kërkimit. Heap-i ndahet me pjesën tjetër të programit, përfshin objekte ende të pambledhura nga GC dhe mund të ulet. Këto dy lexime dhe diferenca e tyre nuk matin pikun ose memorien ekskluzive të algoritmit. Nuk detyrohet GC.</p>
                <p>SA përmirëson një strukturë të plotë gjatë iteracioneve; grafiku i përmirësimit tregon best-in e plotë. Beam ndërton prefikse deri në nivelin e fundit: energjia e një prefiksi nuk është e barasvlershme me energjinë finale. Grafikët e krahasimit përdorin vetëm rezultate të plota.</p>
                <p>Kohët ndikohen nga JIT, GC, rendi i provave dhe ngarkesa e kompjuterit. Një ngrohje dhe një provë Beam për konfigurim nuk përbëjnë një benchmark të plotë statistikor. Energjitë janë rezultate të kërkimit të kryer; energjia më e ulët e gjetur nuk pretendohet minimum global.</p>
                </div>
                """);
        page.append(comparison(rows, "baseline", "Krahasimi bazë · pa kufi kohor"));
        page.append(comparison(rows, "time_limited", "Krahasimi shtesë · kufi i veçantë për çdo kërkim"));
        page.append(progress(rows));
        page.append(rotations(rows));
        page.append(metrics(rows));
        page.append("<section id=\"raw\"><h2>Të gjitha provat</h2><p>").append(rows.size())
                .append(" rreshta të matur. Fushat e pazbatueshme mbeten bosh; koha është në ms dhe kufiri 0 do të thotë pa kufi.</p>")
                .append("<details><summary>Hap tabelën e plotë të CSV-së</summary><div class=\"scroll raw\"><table><thead><tr>");
        for (String column : ExperimentRow.COLUMNS) {
            page.append("<th>").append(escape(column)).append("</th>");
        }
        page.append("</tr></thead><tbody>");
        for (ExperimentRow row : rows) {
            page.append("<tr>");
            for (String column : ExperimentRow.COLUMNS) {
                page.append("<td>").append(escape(row.get(column))).append("</td>");
            }
            page.append("</tr>");
        }
        page.append("</tbody></table></div></details></section></body></html>");
        Files.writeString(html, page.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    private static String metrics(List<ExperimentRow> rows) {
        StringBuilder html = new StringBuilder("""
                <section id="metrics"><h2>Propozimet dhe memoria</h2>
                <p>Një propozim SA është një përpjekje për lëvizje, edhe kur nuk ka lëvizje të mundshme. Një propozim Beam është një drejtim nga një prind; vendosja fillestare në origjinë numërohet si një propozim. Numërohen ngjarje, jo forma unike. Kandidatët e vlefshëm të Beam përfshijnë prefikset, kopjet e rrotulluara dhe kandidatët e larguar nga gjerësia.</p>
                <p>Pavlefshmëria gjeometrike përfshin përplasjen ose lidhjen e këputur. Lëvizja pa ndryshim, mungesa e lëvizjes dhe koordinatat jashtë int raportohen veçmas. Refuzimi nga temperatura në SA mbetet kandidat i vlefshëm.</p>
                <p>Përqindja e kandidatëve të gjeneruar që u eliminuan = 100 × (kopjet e rrotulluara + largimet nga gjerësia) / kandidatët e vlefshëm të gjeneruar, në të gjitha nivelet e përfunduara. Emëruesi zero jep N/A. Kjo nuk është përqindje e gjithë hapësirës së mundshme dhe nuk zbatohet për refuzimet e SA.</p>
                <div class="scroll"><table><thead><tr><th>Prova</th>
                """);
        String[] columns = {"proposals", "valid_proposals", "geometrically_invalid_proposals", "collision_proposals",
                "disconnected_proposals", "unchanged_proposals", "no_move_proposals", "out_of_range_proposals",
                "temperature_rejected_candidates", "heap_used_before_mib", "heap_used_after_mib",
                "max_candidates_before_selection", "generated_candidates_eliminated_percent"};
        String[] labels = {"Propozime", "Të vlefshme", "Gjeometrikisht të pavlefshme", "Përplasje",
                "Lidhje të këputura", "Pa ndryshim", "Pa lëvizje", "Jashtë int", "Refuzuar nga temperatura",
                "Heap JVM para (MiB)", "Heap JVM pas (MiB)", "Maks. kandidatë në pritje",
                "Përqindja e kandidatëve të gjeneruar që u eliminuan"};
        for (String label : labels) {
            html.append("<th>").append(label).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        for (ExperimentRow row : rows) {
            String label = row.get("group") + " · " + row.get("sequence_id") + " · " + row.get("algorithm")
                    + (row.get("algorithm").equals("SA") ? " · seed=" + row.get("seed")
                    + " · I=" + row.get("max_iterations") : " · B=" + row.get("beam_width")
                    + " · rrotullime=" + row.get("remove_rotated_copies"));
            html.append("<tr><td>").append(escape(label)).append("</td>");
            for (String column : columns) {
                String value = row.get(column);
                if (column.equals("generated_candidates_eliminated_percent")
                        && !value.isEmpty() && !value.equals("N/A")) {
                    value += "%";
                }
                html.append("<td>").append(escape(value.isEmpty() ? "—" : value)).append("</td>");
            }
            html.append("</tr>");
        }
        return html.append("</tbody></table></div></section>").toString();
    }

    private static String comparison(List<ExperimentRow> rows, String group, String title) {
        StringBuilder html = new StringBuilder("<section id=\"").append(group).append("\"><h2>")
                .append(title).append("</h2>");
        LinkedHashSet<String> sequences = new LinkedHashSet<>();
        for (ExperimentRow row : rows) {
            if (row.get("group").equals(group)) {
                sequences.add(row.get("sequence"));
            }
        }
        for (String sequence : sequences) {
            List<ExperimentRow> selected = rows.stream().filter(r -> r.get("group").equals(group)
                    && r.get("sequence").equals(sequence)).toList();
            ExperimentRow first = selected.get(0);
            List<ExperimentRow> sa = selected.stream().filter(r -> r.get("algorithm").equals("SA")).toList();
            ArrayList<Bar> energy = new ArrayList<>();
            ArrayList<Bar> time = new ArrayList<>();
            StringBuilder summary = new StringBuilder("<div class=\"scroll\"><table><thead><tr><th>Algoritmi</th><th>Energjia më e mirë</th><th>Mediana e energjisë</th><th>Koha ms</th><th>Maks. kandidatë</th><th>Të plota / prova</th></tr></thead><tbody>");
            if (!sa.isEmpty()) {
                List<Double> energies = sa.stream().filter(r -> !r.get("energy").isEmpty())
                        .map(r -> Double.parseDouble(r.get("energy"))).toList();
                energy.add(new Bar("SA · më e mira", energies.stream().min(Double::compare).orElse(null), "#52d8b5"));
                energy.add(new Bar("SA · medianë (" + sa.size() + ")", energies.isEmpty() ? null : median(energies), "#9ae9d3"));
                time.add(new Bar("SA · medianë (" + sa.size() + ")",
                        median(sa.stream().map(r -> Double.parseDouble(r.get("elapsed_ms"))).toList()), "#52d8b5"));
                summary.append("<tr><td>SA</td><td>").append(energy.get(0).value() == null ? "Pa strukturë" : number(energy.get(0).value()))
                        .append("</td><td>").append(energies.isEmpty() ? "Pa strukturë" : number(median(energies)))
                        .append("</td><td>").append(decimal(time.get(0).value())).append(" (medianë)</td><td>—</td><td>")
                        .append(energies.size()).append(" / ").append(sa.size()).append("</td></tr>");
            }
            for (ExperimentRow beam : selected.stream().filter(r -> r.get("algorithm").equals("Beam")).toList()) {
                String label = "Beam · B=" + beam.get("beam_width");
                String color = beam.get("beam_width").equals("10") ? "#7bb2ff" : "#baa0ff";
                energy.add(new Bar(label, beam.get("energy").isEmpty() ? null : Double.parseDouble(beam.get("energy")), color));
                time.add(new Bar(label, Double.parseDouble(beam.get("elapsed_ms")), color));
                summary.append("<tr><td>").append(label).append("</td><td>")
                        .append(beam.get("energy").isEmpty() ? "Pa strukturë" : beam.get("energy"))
                        .append("</td><td>—</td><td>").append(beam.get("elapsed_ms"))
                        .append("</td><td>").append(beam.get("max_candidates_before_selection"))
                        .append("</td><td>").append(beam.get("energy").isEmpty() ? "0 / 1" : "1 / 1").append("</td></tr>");
            }
            html.append("<article><h3>").append(escape(first.get("sequence_id"))).append(" · n=")
                    .append(first.get("length")).append("</h3><p><code>").append(sequence).append("</code></p><p class=\"muted\">Pesha H–H: ")
                    .append(first.get("contact_weight")).append(" · Kufiri për secilin kërkim: ")
                    .append(first.get("time_limit_ms").equals("0") ? "pa kufi" : first.get("time_limit_ms") + " ms")
                    .append("</p><div class=\"charts\"><div class=\"chart\"><h4>Energjia · më negative është më e ulët</h4>")
                    .append(bars(energy, true)).append("<p class=\"muted\">Gjatësia e shtyllës paraqet |E|; etiketa jep energjinë me shenjë.</p></div>")
                    .append("<div class=\"chart\"><h4>Koha e kërkimit · ms</h4>").append(bars(time, false))
                    .append("<p class=\"muted\">Koha shfaqet edhe kur kërkimi nuk jep strukturë të plotë.</p></div></div>")
                    .append(summary).append("</tbody></table></div></article>");
        }
        return html.append("</section>").toString();
    }

    static double median(List<Double> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Mediana kerkon te pakten nje vlere.");
        }
        ArrayList<Double> sorted = new ArrayList<>(values);
        sorted.sort(Double::compare);
        int middle = sorted.size() / 2;
        return sorted.size() % 2 == 1 ? sorted.get(middle) : (sorted.get(middle - 1) + sorted.get(middle)) / 2;
    }

    private static String bars(List<Bar> bars, boolean energy) {
        double maximum = 1;
        for (Bar bar : bars) {
            if (bar.value() != null) {
                maximum = Math.max(maximum, Math.abs(bar.value()));
            }
        }
        StringBuilder svg = new StringBuilder("<svg role=\"img\" viewBox=\"0 0 620 ")
                .append(30 + bars.size() * 42).append("\"><title>")
                .append(energy ? "Energjia sipas algoritmit" : "Koha në milisekonda sipas algoritmit").append("</title>");
        for (int i = 0; i < bars.size(); i++) {
            Bar bar = bars.get(i);
            int y = 15 + i * 42;
            svg.append("<g data-value=\"").append(bar.value() == null ? "missing" : bar.value())
                    .append("\"><text x=\"2\" y=\"").append(y + 16).append("\">").append(escape(bar.label())).append("</text>");
            if (bar.value() != null) {
                svg.append("<rect x=\"190\" y=\"").append(y).append("\" width=\"")
                        .append(decimal(Math.abs(bar.value()) / maximum * 290)).append("\" height=\"24\" rx=\"4\" fill=\"")
                        .append(bar.color()).append("\"/>");
            }
            svg.append("<text x=\"495\" y=\"").append(y + 16).append("\">")
                    .append(bar.value() == null ? "Pa strukturë" : (energy ? number(bar.value()) : decimal(bar.value()) + " ms"))
                    .append("</text></g>");
        }
        return svg.append("</svg>").toString();
    }

    private static String progress(List<ExperimentRow> allRows) {
        List<ExperimentRow> rows = allRows.stream().filter(r -> r.get("group").equals("progress"))
                .sorted(Comparator.comparingInt(r -> Integer.parseInt(r.get("max_iterations")))).toList();
        if (rows.isEmpty()) {
            return "";
        }
        StringBuilder html = new StringBuilder("<section id=\"progress\"><h2>Si përmirësohet SA</h2><p>Seed: ")
                .append(rows.get(0).get("seed")).append(" · <code>").append(rows.get(0).get("sequence"))
                .append("</code>. Çdo pikë rinis nga e njëjta vijë dhe me të njëjtin seed; përdoret best, jo gjendja current. ")
                .append("Këto janë prefikse të së njëjtës seri zgjedhjesh dhe ftohjeje, pa kufi kohor.</p>");
        double minimum = rows.stream().mapToDouble(r -> Double.parseDouble(r.get("energy"))).min().orElse(0);
        double maximum = rows.stream().mapToDouble(r -> Double.parseDouble(r.get("energy"))).max().orElse(0);
        int maxIterations = Integer.parseInt(rows.get(rows.size() - 1).get("max_iterations"));
        StringBuilder points = new StringBuilder();
        StringBuilder labels = new StringBuilder();
        for (ExperimentRow row : rows) {
            double x = 70 + 450.0 * Integer.parseInt(row.get("max_iterations")) / maxIterations;
            double y = maximum == minimum ? 90 : 35 + (maximum - Double.parseDouble(row.get("energy"))) / (maximum - minimum) * 100;
            points.append(decimal(x)).append(',').append(decimal(y)).append(' ');
            labels.append("<circle cx=\"").append(decimal(x)).append("\" cy=\"").append(decimal(y))
                    .append("\" r=\"5\" fill=\"#65dfbc\"/><text x=\"").append(decimal(x - 10)).append("\" y=\"")
                    .append(decimal(y - 12)).append("\">E=").append(row.get("energy")).append("</text><text x=\"")
                    .append(decimal(x - 10)).append("\" y=\"175\">").append(row.get("max_iterations")).append("</text>");
        }
        html.append("<article><svg role=\"img\" viewBox=\"0 0 620 200\"><title>Energjia më e mirë deri në numrin e përpjekjeve</title>")
                .append("<path d=\"M60 20V155H550\" fill=\"none\" stroke=\"#637b9a\"/><polyline fill=\"none\" stroke=\"#65dfbc\" stroke-width=\"3\" points=\"")
                .append(points).append("\"/>").append(labels).append("<text x=\"440\" y=\"198\">Përpjekje</text></svg></article>")
                .append("<div class=\"scroll\"><table><thead><tr><th>Përpjekje</th><th>Energjia më e mirë deri këtu</th><th>Koha ms</th></tr></thead><tbody>");
        for (ExperimentRow row : rows) {
            html.append("<tr><td>").append(row.get("max_iterations")).append("</td><td>").append(row.get("energy"))
                    .append("</td><td>").append(row.get("elapsed_ms")).append("</td></tr>");
        }
        return html.append("</tbody></table></div></section>").toString();
    }

    private static String rotations(List<ExperimentRow> allRows) {
        List<ExperimentRow> off = allRows.stream().filter(r -> r.get("group").equals("rotation")).toList();
        if (off.isEmpty()) {
            return "";
        }
        ExperimentRow reference = off.get(0);
        ArrayList<ExperimentRow> rows = new ArrayList<>(allRows.stream().filter(r -> r.get("group").equals("baseline")
                && r.get("algorithm").equals("Beam") && r.get("sequence").equals(reference.get("sequence"))
                && r.get("beam_width").equals(reference.get("beam_width"))).toList());
        rows.addAll(off);
        StringBuilder html = new StringBuilder("<section id=\"rotation\"><h2>Kopjet e rrotulluara · B=")
                .append(reference.get("beam_width")).append("</h2><p><code>").append(reference.get("sequence"))
                .append("</code>. Krahasohen dy kërkime pa kufi, me të njëjtën gjerësi dhe peshë. Heqja e simetrive mund të ndryshojë degët që ruhen.</p>")
                .append("<div class=\"scroll\"><table><thead><tr><th>Heqja e kopjeve</th><th>Energjia</th><th>Koha ms</th><th>Kopje të hequra gjithsej</th><th>Maksimum kandidatësh para përzgjedhjes</th></tr></thead><tbody>");
        for (ExperimentRow row : rows) {
            html.append("<tr><td>").append(row.get("remove_rotated_copies").equals("true") ? "Ndezur" : "Fikur")
                    .append("</td><td>").append(row.get("energy").isEmpty() ? "Pa strukturë" : row.get("energy"))
                    .append("</td><td>").append(row.get("elapsed_ms")).append("</td><td>").append(row.get("rotated_duplicates"))
                    .append("</td><td>").append(row.get("max_candidates_before_selection")).append("</td></tr>");
        }
        return html.append("</tbody></table></div></section>").toString();
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    private static String number(double value) {
        return value == Math.rint(value) ? String.format(Locale.ROOT, "%.0f", value) : decimal(value);
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
