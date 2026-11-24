import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.LongSupplier;

public class BeamSearch {
    private static final int[][] AXIS_PERMUTATIONS = {
            {0, 1, 2}, {0, 2, 1}, {1, 0, 2},
            {1, 2, 0}, {2, 0, 1}, {2, 1, 0}
    };
    private static final int[] PERMUTATION_SIGNS = {1, -1, -1, 1, 1, -1};

    // null do te thote se deget e ruajtura ngecen, jo se eshte provuar
    // mosekzistenca e nje strukture te vlefshme.
    public static Protein search(String hpSequence, int beamWidth) {
        return searchWithStats(hpSequence, beamWidth).getBestProtein();
    }

    public static Protein search(String hpSequence, int beamWidth, long timeLimitMillis) {
        return searchWithStats(hpSequence, beamWidth, timeLimitMillis).getBestProtein();
    }

    public static Protein search(String hpSequence, int beamWidth, boolean removeRotatedCopies) {
        return searchWithStats(hpSequence, beamWidth, 0, removeRotatedCopies).getBestProtein();
    }

    public static Protein search(String hpSequence, int beamWidth, long timeLimitMillis,
                                 boolean removeRotatedCopies) {
        return searchWithStats(hpSequence, beamWidth, timeLimitMillis, removeRotatedCopies).getBestProtein();
    }

    public static Protein search(String hpSequence, int beamWidth, long timeLimitMillis,
                                 boolean removeRotatedCopies, int contactWeight) {
        return searchWithStats(hpSequence, beamWidth, timeLimitMillis, removeRotatedCopies, contactWeight).getBestProtein();
    }

    public static BeamSearchResult searchWithStats(String hpSequence, int beamWidth) {
        return searchWithStats(hpSequence, beamWidth, 0);
    }

    public static BeamSearchResult searchWithStats(String hpSequence, int beamWidth, long timeLimitMillis) {
        return searchWithStats(hpSequence, beamWidth, timeLimitMillis, false);
    }

    public static BeamSearchResult searchWithStats(String hpSequence, int beamWidth, boolean removeRotatedCopies) {
        return searchWithStats(hpSequence, beamWidth, 0, removeRotatedCopies);
    }

    public static BeamSearchResult searchWithStats(String hpSequence, int beamWidth, long timeLimitMillis,
                                                   boolean removeRotatedCopies) {
        return searchWithStats(hpSequence, beamWidth, timeLimitMillis, removeRotatedCopies, 1);
    }

    public static BeamSearchResult searchWithStats(String hpSequence, int beamWidth, long timeLimitMillis,
                                                   boolean removeRotatedCopies, int contactWeight) {
        return searchWithStats(hpSequence, beamWidth, timeLimitMillis, removeRotatedCopies, contactWeight, System::nanoTime);
    }

    static BeamSearchResult searchWithStats(String hpSequence, int beamWidth, long timeLimitMillis,
                                            LongSupplier nanoTime) {
        return searchWithStats(hpSequence, beamWidth, timeLimitMillis, false, nanoTime);
    }

    static BeamSearchResult searchWithStats(String hpSequence, int beamWidth, long timeLimitMillis,
                                            boolean removeRotatedCopies, LongSupplier nanoTime) {
        return searchWithStats(hpSequence, beamWidth, timeLimitMillis, removeRotatedCopies, 1, nanoTime);
    }

    static BeamSearchResult searchWithStats(String hpSequence, int beamWidth, long timeLimitMillis,
                                            boolean removeRotatedCopies, int contactWeight, LongSupplier nanoTime) {
        SearchTimeLimit limit = new SearchTimeLimit(timeLimitMillis, nanoTime);
        if (beamWidth <= 0) {
            throw new IllegalArgumentException("Beam width duhet te jete pozitiv.");
        }
        if (contactWeight <= 0) {
            throw new IllegalArgumentException("Pesha e kontaktit H-H duhet te jete pozitive.");
        }
        Protein initial = new Protein(hpSequence);
        ArrayList<Protein> beam = new ArrayList<>();
        beam.add(initial);
        long expandedStates = 0;
        long generatedCandidates = 0;
        long retainedCandidates = 0;
        long rotatedDuplicates = 0;
        long prunedCandidates = 0;
        int completedLevels = 0;
        int maxCandidatesBeforeSelection = 0;
        long extensionProposals = 0;
        long collisions = 0;
        long outOfRange = 0;

        for (int step = 0; step < hpSequence.length(); step++) {
            if (limit.isExpired()) {
                return new BeamSearchResult(null, expandedStates, generatedCandidates,
                        retainedCandidates, rotatedDuplicates, prunedCandidates, completedLevels,
                        StopReason.TIME_LIMIT, maxCandidatesBeforeSelection, extensionProposals, collisions, outOfRange);
            }
            ArrayList<Protein> candidates = new ArrayList<>();
            for (Protein protein : beam) {
                expandedStates++;
                ExtensionBatch extensions = protein.createExtensionsWithStats();
                candidates.addAll(extensions.candidates());
                extensionProposals += extensions.proposals();
                collisions += extensions.collisionProposals();
                outOfRange += extensions.outOfRangeProposals();
            }
            generatedCandidates += candidates.size();
            // Mat listen e perbashket para heqjes se kopjeve dhe para kufirit te gjeresise.
            maxCandidatesBeforeSelection = Math.max(maxCandidatesBeforeSelection, candidates.size());
            if (candidates.isEmpty()) {
                return new BeamSearchResult(null, expandedStates, generatedCandidates,
                        retainedCandidates, rotatedDuplicates, prunedCandidates, completedLevels,
                        StopReason.NO_CANDIDATES, maxCandidatesBeforeSelection, extensionProposals, collisions, outOfRange);
            }
            if (removeRotatedCopies) {
                int beforeRemoval = candidates.size();
                candidates = removeRotatedDuplicates(candidates, step + 1);
                rotatedDuplicates += beforeRemoval - candidates.size();
            }
            beam = selectBestCandidates(candidates, beamWidth, contactWeight);
            retainedCandidates += beam.size();
            prunedCandidates += candidates.size() - beam.size();
            completedLevels++;
        }

        return new BeamSearchResult(beam.get(0), expandedStates, generatedCandidates,
                retainedCandidates, rotatedDuplicates, prunedCandidates, completedLevels,
                StopReason.COMPLETED, maxCandidatesBeforeSelection, extensionProposals, collisions, outOfRange);
    }

    // Kandidatet vijne nga i njejti nivel dhe kane te njejten sekuence HP.
    // Lista ruan kandidatin e pare dhe koordinatat e tij; ndryshon vetem celesi i krahasimit.
    static ArrayList<Protein> removeRotatedDuplicates(List<Protein> candidates, int positionCount) {
        HashSet<String> seenShapes = new HashSet<>();
        ArrayList<Protein> unique = new ArrayList<>();
        for (Protein candidate : candidates) {
            if (seenShapes.add(rotationKey(candidate, positionCount))) {
                unique.add(candidate);
            }
        }
        return unique;
    }

    private static String rotationKey(Protein protein, int positionCount) {
        long[][] steps = new long[positionCount - 1][3];
        Position previous = protein.getPosition(0);
        for (int i = 1; i < positionCount; i++) {
            Position next = protein.getPosition(i);
            steps[i - 1][0] = (long) next.x - previous.x;
            steps[i - 1][1] = (long) next.y - previous.y;
            steps[i - 1][2] = (long) next.z - previous.z;
            previous = next;
        }

        String smallest = null;
        for (int permutation = 0; permutation < AXIS_PERMUTATIONS.length; permutation++) {
            int[] axes = AXIS_PERMUTATIONS[permutation];
            for (int signX = -1; signX <= 1; signX += 2) {
                for (int signY = -1; signY <= 1; signY += 2) {
                    // Shenja e trete siguron determinant +1: 6 * 4 rrotullime, pa pasqyrime.
                    int signZ = PERMUTATION_SIGNS[permutation] * signX * signY;
                    StringBuilder key = new StringBuilder();
                    for (long[] step : steps) {
                        key.append(signX * step[axes[0]]).append(',')
                                .append(signY * step[axes[1]]).append(',')
                                .append(signZ * step[axes[2]]).append(';');
                    }
                    String rotated = key.toString();
                    if (smallest == null || rotated.compareTo(smallest) < 0) {
                        smallest = rotated;
                    }
                }
            }
        }
        return smallest;
    }

    // Ne Beam Search krahasohen kandidate me te njejten sekuence HP
    // dhe te njejtin numer pozicionesh te vendosura.
    public static ArrayList<Protein> selectBestCandidates(List<Protein> candidates, int beamWidth) {
        return selectBestCandidates(candidates, beamWidth, 1);
    }

    public static ArrayList<Protein> selectBestCandidates(List<Protein> candidates, int beamWidth, int contactWeight) {
        if (candidates == null) {
            throw new IllegalArgumentException("Lista e kandidateve nuk mund te jete null.");
        }
        if (beamWidth <= 0) {
            throw new IllegalArgumentException("Beam width duhet te jete pozitiv.");
        }
        if (contactWeight <= 0) {
            throw new IllegalArgumentException("Pesha e kontaktit H-H duhet te jete pozitive.");
        }

        ArrayList<CandidateEnergy> ranked = new ArrayList<>();
        for (Protein candidate : candidates) {
            if (candidate == null) {
                throw new IllegalArgumentException("Kandidati nuk mund te jete null.");
            }
            long energy = candidate.calculateEnergy(contactWeight);
            ranked.add(new CandidateEnergy(candidate, energy));
        }

        // List.sort ruan rendin fillestar kur energjite jane te barabarta.
        ranked.sort((first, second) -> Long.compare(first.energy, second.energy));
        ArrayList<Protein> selected = new ArrayList<>();
        int count = Math.min(beamWidth, ranked.size());
        for (int i = 0; i < count; i++) {
            selected.add(ranked.get(i).protein.copy());
        }
        return selected;
    }

    private static class CandidateEnergy {
        private final Protein protein;
        private final long energy;

        CandidateEnergy(Protein protein, long energy) {
            this.protein = protein;
            this.energy = energy;
        }
    }
}
