import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

public class BeamSearch {
    // null do te thote se deget e ruajtura ngecen, jo se eshte provuar
    // mosekzistenca e nje strukture te vlefshme.
    public static Protein search(String hpSequence, int beamWidth) {
        return searchWithStats(hpSequence, beamWidth).getBestProtein();
    }

    public static Protein search(String hpSequence, int beamWidth, long timeLimitMillis) {
        return searchWithStats(hpSequence, beamWidth, timeLimitMillis).getBestProtein();
    }

    public static BeamSearchResult searchWithStats(String hpSequence, int beamWidth) {
        return searchWithStats(hpSequence, beamWidth, 0);
    }

    public static BeamSearchResult searchWithStats(String hpSequence, int beamWidth, long timeLimitMillis) {
        return searchWithStats(hpSequence, beamWidth, timeLimitMillis, System::nanoTime);
    }

    static BeamSearchResult searchWithStats(String hpSequence, int beamWidth, long timeLimitMillis,
                                            LongSupplier nanoTime) {
        SearchTimeLimit limit = new SearchTimeLimit(timeLimitMillis, nanoTime);
        if (beamWidth <= 0) {
            throw new IllegalArgumentException("Beam width duhet te jete pozitiv.");
        }
        Protein initial = new Protein(hpSequence);
        ArrayList<Protein> beam = new ArrayList<>();
        beam.add(initial);
        long expandedStates = 0;
        long generatedCandidates = 0;
        long retainedCandidates = 0;
        long prunedCandidates = 0;
        int completedLevels = 0;

        for (int step = 0; step < hpSequence.length(); step++) {
            if (limit.isExpired()) {
                return new BeamSearchResult(null, expandedStates, generatedCandidates,
                        retainedCandidates, prunedCandidates, completedLevels, StopReason.TIME_LIMIT);
            }
            ArrayList<Protein> candidates = new ArrayList<>();
            for (Protein protein : beam) {
                expandedStates++;
                candidates.addAll(protein.createExtensions());
            }
            generatedCandidates += candidates.size();
            if (candidates.isEmpty()) {
                return new BeamSearchResult(null, expandedStates, generatedCandidates,
                        retainedCandidates, prunedCandidates, completedLevels, StopReason.NO_CANDIDATES);
            }
            beam = selectBestCandidates(candidates, beamWidth);
            retainedCandidates += beam.size();
            prunedCandidates += candidates.size() - beam.size();
            completedLevels++;
        }

        return new BeamSearchResult(beam.get(0), expandedStates, generatedCandidates,
                retainedCandidates, prunedCandidates, completedLevels, StopReason.COMPLETED);
    }

    // Ne Beam Search krahasohen kandidate me te njejten sekuence HP
    // dhe te njejtin numer pozicionesh te vendosura.
    public static ArrayList<Protein> selectBestCandidates(List<Protein> candidates, int beamWidth) {
        if (candidates == null) {
            throw new IllegalArgumentException("Lista e kandidateve nuk mund te jete null.");
        }
        if (beamWidth <= 0) {
            throw new IllegalArgumentException("Beam width duhet te jete pozitiv.");
        }

        ArrayList<CandidateEnergy> ranked = new ArrayList<>();
        for (Protein candidate : candidates) {
            if (candidate == null) {
                throw new IllegalArgumentException("Kandidati nuk mund te jete null.");
            }
            int energy = candidate.calculateEnergy();
            ranked.add(new CandidateEnergy(candidate, energy));
        }

        // List.sort ruan rendin fillestar kur energjite jane te barabarta.
        ranked.sort((first, second) -> Integer.compare(first.energy, second.energy));
        ArrayList<Protein> selected = new ArrayList<>();
        int count = Math.min(beamWidth, ranked.size());
        for (int i = 0; i < count; i++) {
            selected.add(ranked.get(i).protein.copy());
        }
        return selected;
    }

    private static class CandidateEnergy {
        private final Protein protein;
        private final int energy;

        CandidateEnergy(Protein protein, int energy) {
            this.protein = protein;
            this.energy = energy;
        }
    }
}
