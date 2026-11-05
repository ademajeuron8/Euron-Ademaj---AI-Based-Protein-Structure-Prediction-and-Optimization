import java.util.ArrayList;
import java.util.List;

public class BeamSearch {
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
