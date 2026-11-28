import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.LongSupplier;
import java.util.stream.Stream;

public class ProteinTests {
    public static void main(String[] args) {
        Protein lineProtein = new Protein("HHHH");
        lineProtein.addPosition(new Position(0, 0, 0));
        lineProtein.addPosition(new Position(1, 0, 0));
        lineProtein.addPosition(new Position(2, 0, 0));
        lineProtein.addPosition(new Position(3, 0, 0));
        showProtein("HHHH ne vije", lineProtein);

        Protein validProtein = createSquare("HHHH");
        showProtein("HHHH ne katror", validProtein);
        showProtein("HPPH ne katror", createSquare("HPPH"));
        showProtein("HPPP ne katror", createSquare("HPPP"));
        showProtein("PPPP ne katror", createSquare("PPPP"));

        Protein adjacentProtein = new Protein("HH");
        adjacentProtein.addPosition(new Position(0, 0, 0));
        adjacentProtein.addPosition(new Position(1, 0, 0));
        showProtein("HH me lidhje te drejtperdrejte", adjacentProtein);

        Protein partialProtein = new Protein("HHHH");
        partialProtein.addPosition(new Position(0, 0, 0));
        partialProtein.addPosition(new Position(1, 0, 0));
        showProtein("HHHH me dy pozicione", partialProtein);
        showProtein("HHHHHH me katror te pjesshem", createSquare("HHHHHH"));

        Protein overlappingProtein = new Protein("HHH");
        overlappingProtein.addPosition(new Position(0, 0, 0));
        overlappingProtein.addPosition(new Position(1, 0, 0));
        overlappingProtein.addPosition(new Position(0, 0, 0));
        showProtein("Struktura me mbivendosje", overlappingProtein);

        Protein disconnectedProtein = new Protein("HH");
        disconnectedProtein.addPosition(new Position(0, 0, 0));
        disconnectedProtein.addPosition(new Position(2, 0, 0));
        showProtein("Struktura me lidhje te shkeputur", disconnectedProtein);

        Protein emptyProtein = new Protein("H");
        showProtein("Struktura bosh", emptyProtein);

        Protein singlePositionProtein = new Protein("H");
        singlePositionProtein.addPosition(new Position(0, 0, 0));
        showProtein("Struktura me nje pozicion", singlePositionProtein);

        // Secili perjashtim kapet qe demonstrimi te vazhdoje.
        String[] invalidSequences = {null, "", "HPX", "hp", "H P"};
        for (String sequence : invalidSequences) {
            try {
                new Protein(sequence);
                System.out.println("GABIM: u pranua sekuenca e pavlefshme: " + sequence);
            } catch (IllegalArgumentException e) {
                System.out.println("Sekuenca e pavlefshme [" + sequence + "] u refuzua: " + e.getMessage());
            }
        }

        Protein limitedProtein = new Protein("H");
        try {
            limitedProtein.addPosition(null);
            System.out.println("GABIM: u pranua pozicioni null.");
        } catch (IllegalArgumentException e) {
            System.out.println("Pozicioni null u refuzua: " + e.getMessage());
        }

        limitedProtein.addPosition(new Position(0, 0, 0));
        try {
            limitedProtein.addPosition(new Position(1, 0, 0));
            System.out.println("GABIM: u tejkalua gjatesia e sekuences.");
        } catch (IllegalStateException e) {
            System.out.println("Pozicioni shtese u refuzua: " + e.getMessage());
        }
        showProtein("Struktura pas shtimit te refuzuar", limitedProtein);

        demonstrateCopiesAndMoves();
        demonstratePivotRotations();
        testPositionSafety();
        testRandomNeighbors();
        testRandomNeighborSeeds();
        testRandomNeighborLimits();
        testAcceptanceRule();
        testOptimizationParameters();
        testOptimizationResults();
        testOptimizationControlledSteps();
        testExtensions();
        testBeamSelection();
        testBeamSearch();
        testAminoAcidConverter();
        testAnnealingStats();
        testBeamSearchStats();
        testAnnealingTimeLimits();
        testBeamTimeLimits();
        testBeamRotatedShapes();
        testBeamRotationSearch();
        testWeightedEnergy();
        testWeightedAnnealing();
        testWeightedBeamSearch();
        testStructureExport();
        testExportedExperimentReport();
        testExperimentPlan();
        testExperimentResults();
        testProposalMetrics();
    }

    private static void demonstrateCopiesAndMoves() {
        Position[] initialPositions = {
                new Position(0, 0, 0),
                new Position(1, 0, 0),
                new Position(1, 1, 0),
                new Position(1, 2, 0)
        };

        Protein original = new Protein("HHHH");
        for (Position position : initialPositions) {
            original.addPosition(new Position(position.x, position.y, position.z));
        }
        check(original.isValid() && original.isComplete() && original.calculateEnergy() == 0,
                "Struktura fillestare eshte e vlefshme, e plote dhe ka energji 0");

        Protein copiedProtein = original.copy();
        check(copiedProtein != original, "Kopja eshte objekt tjeter Protein");
        checkCoordinates(copiedProtein, initialPositions, "Kopja ruan koordinatat fillestare");

        Position proposedPosition = new Position(0, 1, 0);
        check(copiedProtein.tryMoveLast(proposedPosition), "Levizja ne (0,1,0) pranohet");
        check(copiedProtein.isValid() && copiedProtein.isComplete() && copiedProtein.calculateEnergy() == -1,
                "Kopja e perkulur eshte e vlefshme, e plote dhe ka energji -1");

        Position[] bentPositions = {
                new Position(0, 0, 0),
                new Position(1, 0, 0),
                new Position(1, 1, 0),
                new Position(0, 1, 0)
        };
        checkCoordinates(copiedProtein, bentPositions, "Vetem pozicioni i fundit i kopjes leviz");
        checkCoordinates(original, initialPositions, "Origjinali ruan te gjitha koordinatat");
        check(original.isComplete() && original.calculateEnergy() == 0, "Origjinali ruan energjine 0");

        proposedPosition.x = 9;
        checkCoordinates(copiedProtein, bentPositions, "Pozicioni i pranuar nuk ndan referencen e propozimit");

        check(copiedProtein.tryMoveLast(new Position(1, 2, 0)), "Kthimi ne (1,2,0) pranohet");
        check(copiedProtein.isComplete() && copiedProtein.calculateEnergy() == 0,
                "Lejohet edhe nje levizje e vlefshme qe e rrit energjine ne 0");
        checkCoordinates(copiedProtein, initialPositions, "Kopja u kthye ne koordinatat fillestare");

        Position[] rejectedPositions = {
                new Position(1, 0, 0),
                new Position(3, 3, 0),
                new Position(1, 2, 0)
        };
        String[] rejectionReasons = {"pozicion i zene", "lidhje e shkeputur", "pozicion i pandryshuar"};
        for (int i = 0; i < rejectedPositions.length; i++) {
            Protein candidate = original.copy();
            check(!candidate.tryMoveLast(rejectedPositions[i]), "Refuzohet: " + rejectionReasons[i]);
            checkCoordinates(candidate, initialPositions, "Koordinatat ruhen pas refuzimit: " + rejectionReasons[i]);
            check(candidate.isComplete() && candidate.calculateEnergy() == 0,
                    "Vlefshmeria dhe energjia 0 ruhen pas refuzimit: " + rejectionReasons[i]);
        }

        Protein singleProtein = new Protein("H");
        singleProtein.addPosition(new Position(0, 0, 0));
        check(!singleProtein.tryMoveLast(new Position(1, 0, 0)), "Nje aminoacid i vetem nuk leviz");
        checkCoordinates(singleProtein, new Position[]{new Position(0, 0, 0)}, "Pozicioni i vetem mbetet i pandryshuar");
        check(singleProtein.isComplete() && singleProtein.calculateEnergy() == 0, "Proteina me nje aminoacid mbetet e vlefshme");

        try {
            original.tryMoveLast(null);
            throw new AssertionError("Pozicioni null duhet te refuzohet.");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: Levizja me null u refuzua: " + e.getMessage());
        }
        checkCoordinates(original, initialPositions, "Propozimi null nuk ndryshon origjinalin");

        Protein partialProtein = new Protein("HHHH");
        partialProtein.addPosition(new Position(0, 0, 0));
        partialProtein.addPosition(new Position(1, 0, 0));
        try {
            partialProtein.tryMoveLast(new Position(0, 1, 0));
            throw new AssertionError("Levizja e struktures se paplote duhet te refuzohet.");
        } catch (IllegalStateException e) {
            System.out.println("OK: Levizja e struktures se paplote u refuzua: " + e.getMessage());
        }

        Protein partialCopy = partialProtein.copy();
        check(partialCopy.isValid() && !partialCopy.isComplete() && partialCopy.calculateEnergy() == 0,
                "Kopjohet edhe nje strukture e pjesshme");
        checkCoordinates(partialCopy, new Position[]{new Position(0, 0, 0), new Position(1, 0, 0)},
                "Kopja e pjesshme ruan koordinatat");
        partialCopy.addPosition(new Position(1, 1, 0));
        partialCopy.addPosition(new Position(0, 1, 0));
        check(partialCopy.isComplete() && partialCopy.calculateEnergy() == -1, "Kopja e pjesshme ruan sekuencen HHHH");
        check(partialProtein.isValid() && !partialProtein.isComplete() && partialProtein.calculateEnergy() == 0,
                "Shtimi ne listen e kopjes nuk ndryshon origjinalin e pjesshem");
        checkCoordinates(partialProtein, new Position[]{new Position(0, 0, 0), new Position(1, 0, 0)},
                "Origjinali i pjesshem ruan koordinatat");

        Protein emptySource = new Protein("HH");
        Protein emptyCopy = emptySource.copy();
        check(!emptyCopy.isValid() && !emptyCopy.isComplete(), "Kopjohet edhe struktura bosh");
        emptyCopy.addPosition(new Position(0, 0, 0));
        check(!emptySource.isValid() && emptyCopy.isValid(), "Kopja bosh ka liste te pavarur");

        Protein mutableSource = new Protein("HHHH");
        Position[] suppliedPositions = new Position[initialPositions.length];
        for (int i = 0; i < initialPositions.length; i++) {
            Position position = initialPositions[i];
            suppliedPositions[i] = new Position(position.x, position.y, position.z);
            mutableSource.addPosition(suppliedPositions[i]);
        }
        Protein independentCopy = mutableSource.copy();
        for (int i = 0; i < suppliedPositions.length; i++) {
            suppliedPositions[i].x += 10;
            checkCoordinates(independentCopy, initialPositions, "Kopja nuk ndan objektin Position me indeks " + i);
            suppliedPositions[i].x -= 10;
        }

        Position readPosition = original.getPosition(0);
        readPosition.x = 9;
        checkCoordinates(original, initialPositions, "Leximi i pozicionit nuk ekspozon objektin e brendshem");
        check(original.isComplete() && original.calculateEnergy() == 0, "Origjinali perfundon i pandryshuar me energji 0");

        Protein invalidProtein = new Protein("HH");
        invalidProtein.addPosition(new Position(0, 0, 0));
        invalidProtein.addPosition(new Position(2, 0, 0));
        try {
            invalidProtein.tryMoveLast(new Position(1, 0, 0));
            throw new AssertionError("Levizja e struktures se pavlefshme duhet te refuzohet.");
        } catch (IllegalStateException e) {
            System.out.println("OK: Struktura e plote por e pavlefshme u refuzua: " + e.getMessage());
        }
    }

    private static void testPositionSafety() {
        Position suppliedPosition = new Position(0, 0, 0);
        Protein protein = new Protein("H");
        protein.addPosition(suppliedPosition);
        suppliedPosition.x = 5;
        suppliedPosition.y = -3;
        suppliedPosition.z = 9;
        check(protein.getPosition(0).isSamePosition(new Position(0, 0, 0)),
                "addPosition: ndryshimi i objektit burim nuk ndryshon proteinen");

        Position origin = new Position(0, 0, 0);
        check(origin.isNeighbor(new Position(1, 0, 0)), "isNeighbor: fqinj ne drejtimin +X");
        check(origin.isNeighbor(new Position(0, 0, -1)), "isNeighbor: fqinj ne drejtimin -Z");
        check(!origin.isNeighbor(new Position(0, 0, 0)), "isNeighbor: i njejti pozicion nuk eshte fqinj");
        check(!origin.isNeighbor(new Position(1, 1, 0)), "isNeighbor: diagonalja nuk eshte fqinje");

        Position minimum = new Position(Integer.MIN_VALUE, 0, 0);
        Position maximum = new Position(Integer.MAX_VALUE, 0, 0);
        check(!minimum.isNeighbor(maximum), "isNeighbor: MIN drejt MAX nuk jane fqinje");
        check(!maximum.isNeighbor(minimum), "isNeighbor: MAX drejt MIN nuk jane fqinje");
        check(minimum.isNeighbor(new Position(Integer.MIN_VALUE + 1, 0, 0)),
                "isNeighbor: ruhet fqinjesia prane MIN");
        check(maximum.isNeighbor(new Position(Integer.MAX_VALUE - 1, 0, 0)),
                "isNeighbor: ruhet fqinjesia prane MAX");
        check(!origin.isNeighbor(new Position(Integer.MAX_VALUE, Integer.MAX_VALUE, 3)),
                "isNeighbor: shuma e distances nuk peson overflow");

        Protein disconnected = new Protein("HH");
        disconnected.addPosition(minimum);
        disconnected.addPosition(maximum);
        check(!disconnected.isValid(), "isValid: lidhja mes MIN dhe MAX eshte e shkeputur");
    }

    private static void testRandomNeighbors() {
        Position[] initial = {
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(1, 2, 0)
        };
        Position[] square = {
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(0, 1, 0)
        };
        Protein original = createProtein("HHHH", initial);
        boolean nullRejected = false;
        try {
            original.createRandomNeighbor(null);
        } catch (IllegalArgumentException e) {
            nullRejected = true;
        }
        check(nullRejected, "Random neighbor: refuzohet Random null");
        checkCoordinates(original, initial, "Random neighbor: null nuk ndryshon origjinalin");

        String[] sequences = {"H", "HHHH", "HH", "HHH"};
        Position[][] invalidPositions = {
                {},
                {new Position(0, 0, 0), new Position(1, 0, 0)},
                {new Position(0, 0, 0), new Position(2, 0, 0)},
                {new Position(0, 0, 0), new Position(1, 0, 0), new Position(0, 0, 0)}
        };
        for (int i = 0; i < sequences.length; i++) {
            Protein invalid = createProtein(sequences[i], invalidPositions[i]);
            boolean rejected = false;
            try {
                invalid.createRandomNeighbor(new ControlledRandom());
            } catch (IllegalStateException e) {
                rejected = true;
            }
            check(rejected, "Random neighbor: refuzohet gjendja e pavlefshme ose e paplote " + i);
            checkCoordinates(invalid, invalidPositions[i], "Random neighbor: gjendja e refuzuar ruhet " + i);
        }

        Position[] singlePosition = {new Position(0, 0, 0)};
        Protein single = createProtein("H", singlePosition);
        check(single.createRandomNeighbor(new ControlledRandom()) == null,
                "Random neighbor: nje pozicion kthen null pa zgjedhje te rastit");
        checkCoordinates(single, singlePosition, "Random neighbor: pozicioni i vetem ruhet");

        Protein endpoint = checkRandomAttempt(original, new int[]{0, 1}, initial, square,
                "Random endpoint pranohet");
        checkRandomAttempt(original, new int[]{1, 1, 0, 0}, initial, new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 0, 1), new Position(1, 0, 2)
        }, "Random pivot X 90 pranohet");
        checkRandomAttempt(original, new int[]{0, 2}, initial, null,
                "Random endpoint pa ndryshim refuzohet");
        checkRandomAttempt(original, new int[]{0, 3}, initial, null,
                "Random endpoint i zene refuzohet");

        Position[] straight = {
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(2, 0, 0), new Position(3, 0, 0)
        };
        Protein line = createProtein("HHHH", straight);
        checkRandomAttempt(line, new int[]{1, 1, 0, 0}, straight, null,
                "Random pivot pa ndryshim refuzohet");
        checkRandomAttempt(line, new int[]{1, 1, 2, 1}, straight, null,
                "Random pivot me perplasje refuzohet");
        checkRandomAttempt(line, new int[]{1, 0, 1, 1}, straight, new Position[]{
                new Position(0, 0, 0), new Position(-1, 0, 0),
                new Position(-2, 0, 0), new Position(-3, 0, 0)
        }, "Random pivot ne indeksin 0 rreth Y 180 pranohet");

        Protein folded = createProtein("HHHH", square);
        Protein higherEnergyEndpoint = checkRandomAttempt(folded, new int[]{0, 2}, square, initial,
                "Random endpoint me energji me te larte pranohet");
        Protein higherEnergyPivot = checkRandomAttempt(folded, new int[]{1, 2, 2, 2}, square, initial,
                "Random pivot ne indeksin e parafundit rreth Z 270 pranohet");
        check(folded.calculateEnergy() == -1 && higherEnergyEndpoint.calculateEnergy() == 0
                        && higherEnergyPivot.calculateEnergy() == 0,
                "Random neighbor: energjia nuk perdoret per te refuzuar kandidatin");

        Position[] pair = {new Position(0, 0, 0), new Position(1, 0, 0)};
        Position[] directions = {
                new Position(1, 0, 0), new Position(-1, 0, 0),
                new Position(0, 1, 0), new Position(0, -1, 0),
                new Position(0, 0, 1), new Position(0, 0, -1)
        };
        Protein twoPositions = createProtein("HH", pair);
        for (int direction = 0; direction < directions.length; direction++) {
            Position[] expected = direction == 0 ? null : new Position[]{pair[0], directions[direction]};
            checkRandomAttempt(twoPositions, new int[]{0, direction}, pair, expected,
                    "Random endpoint drejtimi " + direction);
        }

        check(endpoint.tryMoveLast(new Position(1, 1, 1)), "Random neighbor: kandidati mund te ndryshohet veqmas");
        checkCoordinates(original, initial, "Random neighbor: ndryshimi i kandidatit nuk prek origjinalin");
        check(original.tryMoveLast(new Position(0, 1, 0)), "Random neighbor: origjinali mund te ndryshohet veqmas");
        checkCoordinates(endpoint, new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(1, 1, 1)
        }, "Random neighbor: ndryshimi i origjinalit nuk prek kandidatin");
    }

    private static void testRandomNeighborSeeds() {
        Position[] initial = {
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(1, 2, 0)
        };
        Protein first = createProtein("HPPH", initial);
        Protein second = createProtein("HPPH", initial);
        Protein snapshot = createProtein("HPPH", initial);
        Random firstRandom = new Random(2025);
        Random secondRandom = new Random(2025);
        boolean repeatable = true;
        boolean validCandidates = true;
        boolean originalsUnchanged = true;
        int candidateCount = 0;
        int nullCount = 0;

        for (int attempt = 0; attempt < 128; attempt++) {
            Protein firstCandidate = first.createRandomNeighbor(firstRandom);
            Protein secondCandidate = second.createRandomNeighbor(secondRandom);
            repeatable &= (firstCandidate == null) == (secondCandidate == null);
            if (firstCandidate != null && secondCandidate != null) {
                repeatable &= sameCoordinates(firstCandidate, secondCandidate, initial.length)
                        && firstCandidate.calculateEnergy() == secondCandidate.calculateEnergy();
            }
            if (firstCandidate == null) {
                nullCount++;
            } else {
                candidateCount++;
                validCandidates &= firstCandidate != first && firstCandidate.isValid()
                        && firstCandidate.isComplete() && !sameCoordinates(firstCandidate, snapshot, initial.length);
            }
            if (secondCandidate != null) {
                validCandidates &= secondCandidate != second && secondCandidate.isValid()
                        && secondCandidate.isComplete() && !sameCoordinates(secondCandidate, snapshot, initial.length);
            }
            originalsUnchanged &= sameCoordinates(first, snapshot, initial.length)
                    && sameCoordinates(second, snapshot, initial.length);
        }

        check(repeatable, "Random neighbor: dy Random me seed 2025 japin seri identike prej 128 thirrjesh");
        check(validCandidates && candidateCount > 0, "Random neighbor: cdo kandidat eshte i vlefshem, i plote dhe i ndryshuar");
        check(nullCount > 0, "Random neighbor: seria perfshin edhe perpjekje te refuzuara");
        check(originalsUnchanged, "Random neighbor: origjinalet ruhen pas cdo thirrjeje ne seri");
    }

    private static void testRandomNeighborLimits() {
        for (int axis = 0; axis < 3; axis++) {
            for (int boundary = 0; boundary < 2; boundary++) {
                int[] previous = {0, 0, 0};
                previous[axis] = boundary == 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                int[] last = previous.clone();
                last[axis] += boundary == 0 ? 1 : -1;
                Position[] initial = {
                        new Position(previous[0], previous[1], previous[2]),
                        new Position(last[0], last[1], last[2])
                };
                Protein original = createProtein("HH", initial);
                int outwardDirection = 2 * axis + (boundary == 0 ? 1 : 0);
                checkRandomAttempt(original, new int[]{0, outwardDirection}, initial, null,
                        "Random endpoint jashte int, boshti " + axis + ", kufiri " + boundary);

                int perpendicularAxis = (axis + 1) % 3;
                int[] proposed = previous.clone();
                proposed[perpendicularAxis] = 1;
                checkRandomAttempt(original, new int[]{0, 2 * perpendicularAxis}, initial, new Position[]{
                        initial[0], new Position(proposed[0], proposed[1], proposed[2])
                }, "Random endpoint i vlefshem prane kufirit, boshti " + axis + ", kufiri " + boundary);
            }
        }

        Position[] nearMaximum = {
                new Position(Integer.MAX_VALUE, 0, 0), new Position(Integer.MAX_VALUE - 1, 0, 0)
        };
        Protein original = createProtein("HH", nearMaximum);
        checkRandomAttempt(original, new int[]{1, 0, 2, 1}, nearMaximum, null,
                "Random pivot jashte int refuzohet");
        checkRandomAttempt(original, new int[]{1, 0, 2, 0}, nearMaximum, new Position[]{
                nearMaximum[0], new Position(Integer.MAX_VALUE, -1, 0)
        }, "Random pivot i vlefshem prane kufirit pranohet");
    }

    private static Protein checkRandomAttempt(Protein original, int[] choices, Position[] initial,
                                             Position[] expected, String description) {
        ControlledRandom random = new ControlledRandom(choices);
        Protein candidate = original.createRandomNeighbor(random);
        check(random.index == choices.length, description + ": vetem nje perpjekje");
        checkCoordinates(original, initial, description + ": origjinali ruhet");
        if (expected == null) {
            check(candidate == null, description + ": kthehet null");
        } else {
            check(candidate != null && candidate != original && candidate.isValid() && candidate.isComplete()
                            && !sameCoordinates(candidate, original, initial.length),
                    description + ": kandidat i plote, i vlefshem dhe i ndryshuar");
            checkCoordinates(candidate, expected, description + ": koordinatat e kandidatit");
        }
        return candidate;
    }

    private static boolean sameCoordinates(Protein first, Protein second, int count) {
        for (int i = 0; i < count; i++) {
            if (!first.getPosition(i).isSamePosition(second.getPosition(i))) {
                return false;
            }
        }
        return true;
    }

    private static class ControlledRandom extends Random {
        private final int[] choices;
        private int index;

        ControlledRandom(int... choices) {
            super(0);
            this.choices = choices;
        }

        @Override
        public int nextInt(int bound) {
            if (index >= choices.length) {
                throw new AssertionError("Zgjedhje shtese e rastit: nuk lejohet riprovimi.");
            }
            int choice = choices[index++];
            if (choice < 0 || choice >= bound) {
                throw new AssertionError("Zgjedhja e kontrolluar eshte jashte kufirit " + bound);
            }
            return choice;
        }
    }

    private static void testAcceptanceRule() {
        ControlledDoubleRandom noDraws = new ControlledDoubleRandom();
        check(SimulatedAnnealing.shouldAccept(-2, -3, 2, noDraws),
                "SA acceptance: permiresimi pranohet pa nextDouble");
        check(SimulatedAnnealing.shouldAccept(-3, -3, 2, noDraws),
                "SA acceptance: barazimi pranohet pa nextDouble");
        check(SimulatedAnnealing.shouldAccept(-2, -3, 0, noDraws),
                "SA acceptance: permiresimi pranohet ne temperature zero");
        check(SimulatedAnnealing.shouldAccept(-3, -3, 0, noDraws),
                "SA acceptance: barazimi pranohet ne temperature zero");
        check(!SimulatedAnnealing.shouldAccept(-3, -2, 0, noDraws),
                "SA acceptance: perkeqesimi refuzohet ne temperature zero pa nextDouble");

        ControlledDoubleRandom acceptRandom = new ControlledDoubleRandom(0.5);
        check(SimulatedAnnealing.shouldAccept(-3, -2, 2, acceptRandom) && acceptRandom.index == 1,
                "SA acceptance: 0.5 pranohet me nje nextDouble");
        ControlledDoubleRandom rejectRandom = new ControlledDoubleRandom(0.7);
        check(!SimulatedAnnealing.shouldAccept(-3, -2, 2, rejectRandom) && rejectRandom.index == 1,
                "SA acceptance: 0.7 refuzohet me nje nextDouble");
        double probability = Math.exp(-1.0 / 2.0);
        ControlledDoubleRandom equalProbability = new ControlledDoubleRandom(probability);
        check(!SimulatedAnnealing.shouldAccept(-3, -2, 2, equalProbability) && equalProbability.index == 1,
                "SA acceptance: vlera e barabarte me probabilitetin refuzohet");

        boolean nullRejected = false;
        try {
            SimulatedAnnealing.shouldAccept(-2, -3, 2, null);
        } catch (IllegalArgumentException e) {
            nullRejected = true;
        }
        check(nullRejected, "SA acceptance: Random null refuzohet edhe per permiresim");

        double[] invalidTemperatures = {-1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double temperature : invalidTemperatures) {
            boolean rejected = false;
            try {
                SimulatedAnnealing.shouldAccept(-2, -3, temperature, noDraws);
            } catch (IllegalArgumentException e) {
                rejected = true;
            }
            check(rejected, "SA acceptance: temperatura " + temperature + " refuzohet edhe per permiresim");
        }

        ControlledDoubleRandom extremeWorsening = new ControlledDoubleRandom(0.0);
        check(!SimulatedAnnealing.shouldAccept(Integer.MIN_VALUE, Integer.MAX_VALUE, 2, extremeWorsening)
                        && extremeWorsening.index == 1,
                "SA acceptance: MIN drejt MAX refuzohet pa overflow, edhe kur random jep zero");
        check(SimulatedAnnealing.shouldAccept(Integer.MAX_VALUE, Integer.MIN_VALUE, 2, noDraws),
                "SA acceptance: MAX drejt MIN pranohet pa overflow dhe pa nextDouble");

        double fullEnergyRange = (double) ((long) Integer.MAX_VALUE - Integer.MIN_VALUE);
        ControlledDoubleRandom acceptExtreme = new ControlledDoubleRandom(0.3);
        check(SimulatedAnnealing.shouldAccept(Integer.MIN_VALUE, Integer.MAX_VALUE, fullEnergyRange, acceptExtreme)
                        && acceptExtreme.index == 1,
                "SA acceptance: diferenca e plote int lejon pranimin kur random eshte nen exp(-1)");
        ControlledDoubleRandom rejectExtreme = new ControlledDoubleRandom(0.4);
        check(!SimulatedAnnealing.shouldAccept(Integer.MIN_VALUE, Integer.MAX_VALUE, fullEnergyRange, rejectExtreme)
                        && rejectExtreme.index == 1,
                "SA acceptance: diferenca e plote int refuzohet kur random eshte mbi exp(-1)");

        check(!SimulatedAnnealing.shouldAccept(-3, -2, -0.0, noDraws),
                "SA acceptance: zero me shenje negative trajtohet si zero");
        ControlledDoubleRandom tinyTemperature = new ControlledDoubleRandom(0.0);
        check(!SimulatedAnnealing.shouldAccept(-3, -2, Double.MIN_VALUE, tinyTemperature)
                        && tinyTemperature.index == 1,
                "SA acceptance: temperatura pozitive minimale lejohet dhe probabiliteti zero refuzon");
        ControlledDoubleRandom largeTemperature = new ControlledDoubleRandom(0.7);
        check(SimulatedAnnealing.shouldAccept(-3, -2, Double.MAX_VALUE, largeTemperature)
                        && largeTemperature.index == 1,
                "SA acceptance: temperatura maksimale e fundme lejohet");
    }

    private static class ControlledDoubleRandom extends Random {
        private final double[] values;
        private int index;

        ControlledDoubleRandom(double... values) {
            super(0);
            this.values = values;
        }

        @Override
        public double nextDouble() {
            if (index >= values.length) {
                throw new AssertionError("nextDouble nuk duhet te thirret pa nje vlere te parashikuar.");
            }
            return values[index++];
        }
    }

    private static void testOptimizationParameters() {
        Protein valid = createSquare("HHHH");
        Random unusedRandom = new ControlledAnnealingRandom(new int[0]);
        checkInvalidOptimization(null, 0, 2, 0.5, unusedRandom, "proteina null");
        checkInvalidOptimization(valid, 0, 2, 0.5, null, "Random null");
        checkInvalidOptimization(new Protein("H"), 0, 2, 0.5, unusedRandom, "struktura bosh");
        checkInvalidOptimization(createProtein("HHHH", new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0)
        }), 0, 2, 0.5, unusedRandom, "struktura e paplote");
        checkInvalidOptimization(createProtein("HH", new Position[]{
                new Position(0, 0, 0), new Position(2, 0, 0)
        }), 0, 2, 0.5, unusedRandom, "lidhja e shkeputur");
        checkInvalidOptimization(createProtein("HHH", new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0), new Position(0, 0, 0)
        }), 0, 2, 0.5, unusedRandom, "mbivendosja");
        checkInvalidOptimization(valid, -1, 2, 0.5, unusedRandom, "numri negativ i perpjekjeve");

        double[] invalidTemperatures = {-1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double temperature : invalidTemperatures) {
            checkInvalidOptimization(valid, 0, temperature, 0.5, unusedRandom, "temperatura " + temperature);
        }
        double[] invalidRates = {-0.1, 0, 1, 1.1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double rate : invalidRates) {
            checkInvalidOptimization(valid, 0, 2, rate, unusedRandom, "koeficienti i ftohjes " + rate);
        }
        checkCoordinates(valid, new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(0, 1, 0)
        }, "SA optimize: parametrat e refuzuar nuk ndryshojne origjinalin");
    }

    private static void checkInvalidOptimization(Protein initial, int iterations, double temperature,
                                                 double rate, Random random, String description) {
        boolean rejected = false;
        try {
            SimulatedAnnealing.optimize(initial, iterations, temperature, rate, random);
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        check(rejected, "SA optimize: refuzohet " + description);
    }

    private static void testOptimizationResults() {
        Position[] initialPositions = {
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(1, 2, 0)
        };
        Protein initial = createProtein("HHHH", initialPositions);
        ControlledAnnealingRandom zeroRandom = new ControlledAnnealingRandom(new int[0]);
        Protein zeroResult = SimulatedAnnealing.optimize(initial, 0, 2, 0.995, zeroRandom);
        check(zeroResult != initial && sameCoordinates(zeroResult, initial, initialPositions.length),
                "SA optimize: zero iteracione japin objekt te ri me koordinata te njejta");
        check(zeroResult.isComplete() && zeroResult.calculateEnergy() == initial.calculateEnergy(),
                "SA optimize: zero iteracione ruajne vlefshmerine dhe energjine");
        check(zeroRandom.intIndex == 0 && zeroRandom.doubleIndex == 0,
                "SA optimize: zero iteracione nuk perdorin rastesi");
        check(zeroResult.tryMoveLast(new Position(0, 1, 0)), "SA optimize: kopja me zero iteracione ndryshohet veqmas");
        checkCoordinates(initial, initialPositions, "SA optimize: ndryshimi i kopjes nuk prek origjinalin");

        Protein identicalInitial = createProtein("HHHH", initialPositions);
        Protein best = SimulatedAnnealing.optimize(initial, 1000, 2, 0.995, new Random(2025));
        Protein repeated = SimulatedAnnealing.optimize(identicalInitial, 1000, 2, 0.995, new Random(2025));
        check(best.isValid() && best.isComplete() && best.calculateEnergy() <= initial.calculateEnergy()
                        && repeated.isValid() && repeated.isComplete()
                        && repeated.calculateEnergy() <= identicalInitial.calculateEnergy(),
                "SA optimize: rezultatet jane te vlefshme, te plota dhe jo me keq se fillimi");
        check(sameCoordinates(best, repeated, initialPositions.length)
                        && best.calculateEnergy() == repeated.calculateEnergy(),
                "SA optimize: seed dhe parametrat identike japin rezultat identik");
        check(best != initial && repeated != identicalInitial && best != repeated,
                "SA optimize: rezultatet jane objekte te pavarura");
        checkCoordinates(initial, initialPositions, "SA optimize: ekzekutimi i pare nuk ndryshon inputin");
        checkCoordinates(identicalInitial, initialPositions, "SA optimize: ekzekutimi i dyte nuk ndryshon inputin");

        Position[] singlePosition = {new Position(0, 0, 0)};
        Protein single = createProtein("H", singlePosition);
        ControlledAnnealingRandom singleRandom = new ControlledAnnealingRandom(new int[0]);
        Protein singleResult = SimulatedAnnealing.optimize(single, 7, 2, 0.5, singleRandom);
        check(singleResult != single && singleResult.isValid() && singleResult.isComplete()
                        && singleResult.calculateEnergy() == 0,
                "SA optimize: nje pozicion perfundon normalisht me propozime null");
        checkCoordinates(singleResult, singlePosition, "SA optimize: rezultati me nje pozicion ruan koordinatat");
        checkCoordinates(single, singlePosition, "SA optimize: origjinali me nje pozicion ruhet");
        check(singleRandom.intIndex == 0 && singleRandom.doubleIndex == 0,
                "SA optimize: propozimet per nje pozicion nuk kerkojne rastesi");
    }

    private static void testOptimizationControlledSteps() {
        Position[] bent = {
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(1, 2, 0)
        };
        Position[] square = {
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(0, 1, 0)
        };
        Protein initial = createProtein("HHHH", bent);
        // Permiresim ne katror, perkeqesim i pranuar, pastaj propozim pa ndryshim nga current.
        ControlledAnnealingRandom bestRandom = new ControlledAnnealingRandom(new int[]{0, 1, 0, 2, 0, 2}, 0.0);
        Protein best = SimulatedAnnealing.optimize(initial, 3, 2, 0.5, bestRandom);
        check(bestRandom.intIndex == 6 && bestRandom.doubleIndex == 1,
                "SA optimize: kryhen saktesisht 3 perpjekje dhe current pranon perkeqesimin");
        check(best.isComplete() && best.calculateEnergy() == -1,
                "SA optimize: kthehet best me energji -1, edhe kur current perfundon me energji 0");
        checkCoordinates(best, square, "SA optimize: ruhet struktura me e mire");
        checkCoordinates(initial, bent, "SA optimize: hapat e kontrolluar nuk ndryshojne inputin");

        Protein folded = createProtein("HHHH", square);
        // Hapi i pare eshte null. Me T=1 dhe me pas T=0.5, vlera 0.5 refuzon dy perkeqesimet.
        ControlledAnnealingRandom coolingRandom = new ControlledAnnealingRandom(new int[]{0, 1, 0, 2, 0, 2}, 0.5, 0.5);
        Protein cooled = SimulatedAnnealing.optimize(folded, 3, 2, 0.5, coolingRandom);
        check(coolingRandom.intIndex == 6 && coolingRandom.doubleIndex == 2,
                "SA optimize: ftohja ndodh edhe pas null dhe te dy perkeqesimet refuzohen");
        checkCoordinates(cooled, square, "SA optimize: ftohja ruan strukturen e pritur");
        checkCoordinates(folded, square, "SA optimize: ftohja nuk ndryshon inputin");

        // Me temperaturen fillestare 2, vlera 0.5 pranon perkeqesimin ne hapin e pare.
        ControlledAnnealingRandom firstTemperature = new ControlledAnnealingRandom(new int[]{0, 2, 0, 2}, 0.5);
        Protein firstResult = SimulatedAnnealing.optimize(folded, 2, 2, 0.5, firstTemperature);
        check(firstTemperature.intIndex == 4 && firstTemperature.doubleIndex == 1,
                "SA optimize: hapi i pare perdor temperaturen fillestare dhe hapi pasues numeron null");
        checkCoordinates(firstResult, square, "SA optimize: best ruhet pas perkeqesimit te pare");

        ControlledAnnealingRandom underflowRandom = new ControlledAnnealingRandom(new int[]{0, 1, 0, 2});
        Protein underflowResult = SimulatedAnnealing.optimize(folded, 2, Double.MIN_VALUE, 0.5, underflowRandom);
        check(underflowRandom.intIndex == 4 && underflowRandom.doubleIndex == 0,
                "SA optimize: vazhdon pas renies se temperatures ne zero dhe refuzon pa nextDouble");
        checkCoordinates(underflowResult, square, "SA optimize: temperatura zero ruan best");

        ControlledAnnealingRandom zeroRandom = new ControlledAnnealingRandom(new int[]{0, 2});
        Protein zeroResult = SimulatedAnnealing.optimize(folded, 1, 0, 0.5, zeroRandom);
        check(zeroRandom.intIndex == 2 && zeroRandom.doubleIndex == 0 && zeroResult.isComplete(),
                "SA optimize: temperatura fillestare zero lejohet dhe nuk kerkon nextDouble");
        checkCoordinates(zeroResult, square, "SA optimize: perkeqesimi ne temperature zero refuzohet");
        checkCoordinates(folded, square, "SA optimize: inputi ruhet pas te gjitha rasteve te kontrolluara");
    }

    private static class ControlledAnnealingRandom extends Random {
        private final int[] choices;
        private final double[] values;
        private int intIndex;
        private int doubleIndex;

        ControlledAnnealingRandom(int[] choices, double... values) {
            super(0);
            this.choices = choices;
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            if (intIndex >= choices.length) {
                throw new AssertionError("Zgjedhje int shtese gjate optimizimit.");
            }
            int choice = choices[intIndex++];
            if (choice < 0 || choice >= bound) {
                throw new AssertionError("Zgjedhja int e optimizimit eshte jashte kufirit " + bound);
            }
            return choice;
        }

        @Override
        public double nextDouble() {
            if (doubleIndex >= values.length) {
                throw new AssertionError("nextDouble shtese gjate optimizimit.");
            }
            return values[doubleIndex++];
        }
    }

    private static void testExtensions() {
        Position origin = new Position(0, 0, 0);
        Protein empty = new Protein("HHH");
        ArrayList<Protein> first = empty.createExtensions();
        checkExtensionList(first, new Position[0], new Position[]{origin}, false,
                "Extensions: struktura bosh jep vetem origjinen");
        check(!hasPosition(empty, 0), "Extensions: struktura fillestare mbetet bosh");

        Protein one = first.get(0);
        Position[] directions = {
                new Position(1, 0, 0), new Position(-1, 0, 0),
                new Position(0, 1, 0), new Position(0, -1, 0),
                new Position(0, 0, 1), new Position(0, 0, -1)
        };
        ArrayList<Protein> fromOrigin = one.createExtensions();
        checkExtensionList(fromOrigin, new Position[]{origin}, directions, false,
                "Extensions: gjashte zgjatje nga origjina ne rendin e drejtimeve");
        ArrayList<Protein> repeated = one.createExtensions();
        boolean sameOrder = repeated.size() == fromOrigin.size();
        for (int i = 0; sameOrder && i < repeated.size(); i++) {
            sameOrder = sameCoordinates(fromOrigin.get(i), repeated.get(i), 2);
        }
        check(sameOrder, "Extensions: thirrjet e perseritura ruajne rendin");

        Protein east = fromOrigin.get(0);
        Protein west = fromOrigin.get(1);
        Position[] eastPrefix = {origin, directions[0]};
        ArrayList<Protein> completed = east.createExtensions();
        checkExtensionList(completed, eastPrefix, new Position[]{
                new Position(2, 0, 0), new Position(1, 1, 0), new Position(1, -1, 0),
                new Position(1, 0, 1), new Position(1, 0, -1)
        }, true, "Extensions: pese zgjatje te plota, pa mbivendosje me origjinen");
        check(completed.get(0).createExtensions().isEmpty(), "Extensions: struktura e plote jep liste bosh");
        check(!hasPosition(east, 2) && !hasPosition(one, 1),
                "Extensions: gjenerimi nuk shton pozicione tek origjinali");

        east.addPosition(new Position(2, 0, 0));
        check(east.isComplete() && !hasPosition(one, 1) && !hasPosition(west, 2),
                "Extensions: ndryshimi i nje kandidati nuk prek origjinalin ose kandidatin tjeter");
        one.addPosition(new Position(0, 1, 0));
        checkCoordinates(west, new Position[]{origin, directions[1]},
                "Extensions: ndryshimi i origjinalit nuk prek kopjen");

        Protein[] invalid = {
                createProtein("HHH", new Position[]{origin, directions[0], origin}),
                createProtein("HHH", new Position[]{origin, new Position(2, 0, 0)})
        };
        String[] reasons = {"mbivendosjen", "lidhjen e shkeputur"};
        for (int i = 0; i < invalid.length; i++) {
            boolean rejected = false;
            try {
                invalid[i].createExtensions();
            } catch (IllegalStateException e) {
                rejected = true;
            }
            check(rejected, "Extensions: refuzon " + reasons[i]);
        }

        Position[] trappedPositions = {
                new Position(1, 0, 0), new Position(1, 1, 0),
                new Position(0, 1, 0), new Position(0, 1, 1),
                new Position(0, 0, 1), new Position(-1, 0, 1),
                new Position(-1, 0, 0), new Position(-1, -1, 0),
                new Position(0, -1, 0), new Position(0, -1, -1),
                new Position(0, 0, -1), origin
        };
        Protein trapped = createProtein("H".repeat(trappedPositions.length + 1), trappedPositions);
        check(trapped.isValid() && !trapped.isComplete() && trapped.createExtensions().isEmpty(),
                "Extensions: skaji i bllokuar jep liste bosh");

        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        Position[] boundary = {new Position(max, min, max)};
        checkExtensionList(createProtein("HH", boundary).createExtensions(), boundary, new Position[]{
                new Position(max - 1, min, max), new Position(max, min + 1, max),
                new Position(max, min, max - 1)
        }, true, "Extensions: drejtimet jashte kufijve MAX/MIN/MAX kapercen");
        Position[] oppositeBoundary = {new Position(min, max, min)};
        checkExtensionList(createProtein("HH", oppositeBoundary).createExtensions(), oppositeBoundary, new Position[]{
                new Position(min + 1, max, min), new Position(min, max - 1, min),
                new Position(min, max, min + 1)
        }, true, "Extensions: drejtimet jashte kufijve MIN/MAX/MIN kapercen");
    }

    private static void checkExtensionList(ArrayList<Protein> extensions, Position[] prefix,
                                           Position[] expectedNext, boolean complete, String description) {
        boolean matches = extensions.size() == expectedNext.length;
        for (int i = 0; matches && i < extensions.size(); i++) {
            Protein candidate = extensions.get(i);
            matches = candidate.isValid() && candidate.isComplete() == complete
                    && hasPosition(candidate, prefix.length) && !hasPosition(candidate, prefix.length + 1)
                    && candidate.getPosition(prefix.length).isSamePosition(expectedNext[i]);
            for (int j = 0; matches && j < prefix.length; j++) {
                matches = candidate.getPosition(j).isSamePosition(prefix[j]);
            }
        }
        check(matches, description);
    }

    private static boolean hasPosition(Protein protein, int index) {
        try {
            protein.getPosition(index);
            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    private static void testBeamSelection() {
        Position[] linePositions = {
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(2, 0, 0), new Position(3, 0, 0)
        };
        Protein line = createProtein("HHHH", linePositions);
        Protein square = createSquare("HHHH");
        Protein bent = createProtein("HHHH", new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(1, 2, 0)
        });
        Protein translatedSquare = createProtein("HHHH", new Position[]{
                new Position(4, 0, 0), new Position(5, 0, 0),
                new Position(5, 1, 0), new Position(4, 1, 0)
        });
        check(line.calculateEnergy() == 0 && bent.calculateEnergy() == 0
                        && square.calculateEnergy() == -1 && translatedSquare.calculateEnergy() == -1,
                "Beam selection: strukturat e testit kane energjite e njohura");

        // Lista e pandryshueshme kontrollon edhe qe perzgjedhja nuk e rendit hyrjen ne vend.
        List<Protein> input = List.of(line, square, bent, translatedSquare);
        Protein[] originalOrder = {line, square, bent, translatedSquare};
        Protein[] snapshots = {line.copy(), square.copy(), bent.copy(), translatedSquare.copy()};
        checkInvalidBeamSelection(null, 1, "listen null");
        checkInvalidBeamSelection(input, 0, "beam width zero");
        checkInvalidBeamSelection(input, -1, "beam width negativ");
        ArrayList<Protein> withNull = new ArrayList<>(input);
        withNull.add(null);
        checkInvalidBeamSelection(withNull, 1, "elementin null edhe pas kandidateve te vlefshem");

        ArrayList<Protein> empty = new ArrayList<>();
        ArrayList<Protein> emptyResult = BeamSearch.selectBestCandidates(empty, 2);
        check(emptyResult != empty && emptyResult.isEmpty(), "Beam selection: hyrja bosh jep liste te re bosh");

        ArrayList<Protein> bestOne = BeamSearch.selectBestCandidates(input, 1);
        check(bestOne.size() == 1 && bestOne.get(0) != square && bestOne.get(0).calculateEnergy() == -1
                        && sameCoordinates(bestOne.get(0), square, 4),
                "Beam selection: beam width 1 zgjedh katrorin e pare me energji -1");
        ArrayList<Protein> all = BeamSearch.selectBestCandidates(input, 10);
        Protein[] expectedOrder = {square, translatedSquare, line, bent};
        boolean orderedCopies = all != input && all.size() == expectedOrder.length;
        for (int i = 0; orderedCopies && i < all.size(); i++) {
            orderedCopies = all.get(i) != expectedOrder[i] && sameCoordinates(all.get(i), expectedOrder[i], 4);
        }
        check(orderedCopies, "Beam selection: width i madh kthen te gjithe, te renditur dhe me barazime te qendrueshme");
        boolean inputUnchanged = input.size() == originalOrder.length;
        for (int i = 0; inputUnchanged && i < input.size(); i++) {
            inputUnchanged = input.get(i) == originalOrder[i] && sameCoordinates(input.get(i), snapshots[i], 4);
        }
        check(inputUnchanged, "Beam selection: hyrja ruan rendin, objektet dhe koordinatat");

        check(all.get(0).tryMoveLast(new Position(1, 2, 0)) && sameCoordinates(square, snapshots[1], 4)
                        && sameCoordinates(all.get(1), translatedSquare, 4),
                "Beam selection: ndryshimi i rezultatit nuk prek hyrjen ose rezultatin tjeter");
        check(square.tryMoveLast(new Position(1, 2, 0)) && sameCoordinates(bestOne.get(0), snapshots[1], 4),
                "Beam selection: ndryshimi i hyrjes nuk prek kopjen e perzgjedhur");

        Protein duplicate = createSquare("HHHH");
        ArrayList<Protein> duplicateResults = BeamSearch.selectBestCandidates(List.of(duplicate, duplicate), 2);
        check(duplicateResults.size() == 2 && duplicateResults.get(0) != duplicateResults.get(1)
                        && duplicateResults.get(0) != duplicate && duplicateResults.get(1) != duplicate
                        && duplicateResults.get(0).tryMoveLast(new Position(1, 2, 0))
                        && duplicateResults.get(1).calculateEnergy() == -1 && duplicate.calculateEnergy() == -1,
                "Beam selection: edhe referencat e perseritura japin kopje te pavarura");

        Protein partialLine = createProtein("HHHHHH", linePositions);
        Protein partialSquare = createSquare("HHHHHH");
        ArrayList<Protein> partialResult = BeamSearch.selectBestCandidates(List.of(partialLine, partialSquare), 1);
        check(partialResult.size() == 1 && partialResult.get(0).isValid() && !partialResult.get(0).isComplete()
                        && !hasPosition(partialResult.get(0), 4) && sameCoordinates(partialResult.get(0), partialSquare, 4),
                "Beam selection: lejohen kandidatet e pjesshem me te njejten sekuence dhe gjatesi te vendosur");

        Protein[] invalid = {
                new Protein("HHHH"),
                createProtein("HHHH", new Position[]{
                        new Position(0, 0, 0), new Position(1, 0, 0),
                        new Position(0, 0, 0), new Position(1, 0, 0)
                }),
                createProtein("HHHH", new Position[]{
                        new Position(0, 0, 0), new Position(1, 0, 0),
                        new Position(2, 0, 0), new Position(4, 0, 0)
                })
        };
        String[] reasons = {"struktura bosh", "mbivendosja", "lidhja e shkeputur"};
        for (int i = 0; i < invalid.length; i++) {
            boolean rejected = false;
            try {
                BeamSearch.selectBestCandidates(List.of(invalid[i]), 1);
            } catch (IllegalStateException e) {
                rejected = true;
            }
            check(rejected, "Beam selection: calculateEnergy refuzon " + reasons[i]);
        }
    }

    private static void checkInvalidBeamSelection(List<Protein> candidates, int width, String description) {
        boolean rejected = false;
        try {
            BeamSearch.selectBestCandidates(candidates, width);
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        check(rejected, "Beam selection: refuzon " + description);
    }

    private static void testBeamSearch() {
        String[] invalidSequences = {null, "", "HPX", "hp", "H P"};
        for (String sequence : invalidSequences) {
            checkInvalidBeamSearch(sequence, 10, "sekuencen [" + sequence + "]");
        }
        checkInvalidBeamSearch("HHHH", 0, "beam width zero");
        checkInvalidBeamSearch("HHHH", -1, "beam width negativ");

        Protein single = BeamSearch.search("H", 10);
        check(single != null && single.isComplete() && single.isValid() && single.calculateEnergy() == 0
                        && single.getPosition(0).isSamePosition(new Position(0, 0, 0)),
                "Beam search: H jep strukture te plote ne origjine me energji 0");
        Protein pair = BeamSearch.search("HH", 10);
        check(pair != null && pair.isComplete() && pair.isValid() && pair.calculateEnergy() == 0
                        && pair.getPosition(0).isNeighbor(pair.getPosition(1)),
                "Beam search: HH jep dy pozicione fqinje me energji 0");
        Protein best = BeamSearch.search("HHHH", 30);
        check(best != null && best.isComplete() && best.isValid() && best.calculateEnergy() == -1,
                "Beam search: HHHH me width 30 gjen strukture te plote me energji -1");
        Protein polar = BeamSearch.search("PPPP", 10);
        check(polar != null && polar.isComplete() && polar.isValid() && polar.calculateEnergy() == 0,
                "Beam search: PPPP jep strukture te plote me energji 0");
        Protein mixed = BeamSearch.search("HPPH", 10);
        check(mixed != null && mixed.isComplete() && mixed.isValid(),
                "Beam search: sekuenca e perzier HPPH jep strukture te plote dhe te vlefshme");
        Protein narrow = BeamSearch.search("HHHH", 1);
        check(narrow != null && narrow.isComplete() && narrow.isValid(),
                "Beam search: width 1 perfundon shembullin e shkurter");

        Protein repeated = BeamSearch.search("HHHH", 30);
        check(repeated != null && repeated.isComplete() && sameCoordinates(best, repeated, 4)
                        && best.calculateEnergy() == repeated.calculateEnergy(),
                "Beam search: thirrjet identike japin koordinata dhe energji identike");
        Protein snapshot = repeated.copy();
        check(best != repeated && best.tryPivotRotation(0, 'Z', 1)
                        && !sameCoordinates(best, repeated, 4) && sameCoordinates(repeated, snapshot, 4),
                "Beam search: ndryshimi i nje rezultati nuk prek rezultatin e thirrjes tjeter");
    }

    private static void checkInvalidBeamSearch(String sequence, int width, String description) {
        boolean rejected = false;
        try {
            BeamSearch.search(sequence, width);
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        check(rejected, "Beam search: refuzon " + description);
    }

    private static void testAminoAcidConverter() {
        check(AminoAcidConverter.toHp("AGVL").equals("HPHH"), "Amino converter: AGVL jep HPHH");
        check(AminoAcidConverter.toHp("AVILMFWYC").equals("HHHHHHHHH"),
                "Amino converter: nente simbolet hidrofobike japin nente H");
        check(AminoAcidConverter.toHp("DEGHKNPQRST").equals("PPPPPPPPPPP"),
                "Amino converter: njembedhjete simbolet polare japin njembedhjete P");
        check(AminoAcidConverter.toHp("HP").equals("PP"), "Amino converter: histidina dhe prolina japin PP");
        check(AminoAcidConverter.toHp("  agvl  ").equals("HPHH"),
                "Amino converter: pranon shkronja te vogla dhe heq vetem hapesirat anesore");

        String[] invalidSequences = {null, "", "   ", "AG VL", "AG1L", "AG!L", "B", "J", "O", "U", "X", "Z", "A\u017FG"};
        for (String sequence : invalidSequences) {
            boolean rejected = false;
            try {
                AminoAcidConverter.toHp(sequence);
            } catch (IllegalArgumentException e) {
                rejected = true;
            }
            check(rejected, "Amino converter: refuzon [" + sequence + "]");
        }

        boolean correctMessage = false;
        try {
            AminoAcidConverter.toHp("  AgxVL  ");
        } catch (IllegalArgumentException e) {
            correctMessage = e.getMessage().contains("'x'") && e.getMessage().contains("pozicionin 3 ");
        }
        check(correctMessage, "Amino converter: gabimi tregon karakterin origjinal x dhe pozicionin 3 pas trim");

        String hpSequence = AminoAcidConverter.toHp("AGVL");
        Protein protein = new Protein(hpSequence);
        for (int i = 0; i < hpSequence.length(); i++) {
            protein.addPosition(new Position(i, 0, 0));
        }
        check(protein.isValid() && protein.isComplete() && protein.calculateEnergy() == 0,
                "Amino converter: rezultati perdoret nga Protein me gjatesine e ruajtur");
    }

    private static void testAnnealingStats() {
        Position[] initialPositions = {
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(1, 2, 0)
        };
        Protein initial = createProtein("HHHH", initialPositions);
        ControlledAnnealingRandom zeroRandom = new ControlledAnnealingRandom(new int[0]);
        AnnealingResult zero = SimulatedAnnealing.optimizeWithStats(initial, 0, 2, 0.5, zeroRandom);
        Protein zeroBest = zero.getBestProtein();
        check(zero.getAttempts() == 0 && zero.getValidCandidates() == 0 && zero.getAcceptedMoves() == 0,
                "SA stats: zero iteracione japin tre numerues zero");
        check(zeroBest != initial && zeroBest != zero.getBestProtein() && zeroBest.isComplete()
                        && sameCoordinates(zeroBest, initial, initialPositions.length),
                "SA stats: rezultati me zero iteracione eshte kopje e pavarur e fillimit");
        check(zeroRandom.intIndex == 0 && zeroRandom.doubleIndex == 0,
                "SA stats: zero iteracione nuk konsumojne Random");
        check(zeroBest.tryMoveLast(new Position(0, 1, 0))
                        && sameCoordinates(zero.getBestProtein(), initial, initialPositions.length),
                "SA stats: ndryshimi i kopjes nga getter nuk prek rezultatin e ruajtur");

        Protein constructorSource = initial.copy();
        AnnealingResult stored = new AnnealingResult(constructorSource, 0, 0, 0);
        check(constructorSource.tryMoveLast(new Position(0, 1, 0))
                        && sameCoordinates(stored.getBestProtein(), initial, initialPositions.length),
                "SA stats: konstruktori ruan kopje te pavarur te proteines");

        Protein single = createProtein("H", new Position[]{new Position(0, 0, 0)});
        ControlledAnnealingRandom singleRandom = new ControlledAnnealingRandom(new int[0]);
        AnnealingResult singleStats = SimulatedAnnealing.optimizeWithStats(single, 10, 2, 0.5, singleRandom);
        check(singleStats.getAttempts() == 10 && singleStats.getValidCandidates() == 0
                        && singleStats.getAcceptedMoves() == 0 && singleRandom.intIndex == 0 && singleRandom.doubleIndex == 0,
                "SA stats: nje pozicion numeron 10 perpjekje pa kandidate ose pranime");
        Protein singleBest = singleStats.getBestProtein();
        check(singleBest != single && singleBest.isComplete() && singleBest.calculateEnergy() == 0
                        && sameCoordinates(singleBest, single, 1),
                "SA stats: propozimet null ruajne strukturen me nje pozicion");

        // Pa ndryshim (null), permiresim, refuzim, perkeqesim i pranuar, barazim i pranuar.
        ControlledAnnealingRandom controlled = new ControlledAnnealingRandom(
                new int[]{0, 2, 0, 1, 0, 2, 0, 2, 0, 4}, 0.5, 0.0);
        AnnealingResult counted = SimulatedAnnealing.optimizeWithStats(initial, 5, 2, 0.5, controlled);
        check(counted.getAttempts() == 5 && counted.getValidCandidates() == 4 && counted.getAcceptedMoves() == 3,
                "SA stats: numerohen null, refuzimi dhe pranimet me permiresim, perkeqesim e barazim");
        check(controlled.intIndex == 10 && controlled.doubleIndex == 2,
                "SA stats: numerimi nuk ndryshon thirrjet e kontrolluara te Random");
        Protein countedBest = counted.getBestProtein();
        check(countedBest.isComplete() && countedBest.calculateEnergy() == -1
                        && sameCoordinates(countedBest, createSquare("HHHH"), initialPositions.length),
                "SA stats: ruhet best pas pranimit te perkeqesimit dhe barazimit");

        for (long seed : new long[]{2025, -7}) {
            AnnealingResult normal = SimulatedAnnealing.optimizeWithStats(initial, 100, 2, 0.995, new Random(seed));
            Protein best = normal.getBestProtein();
            check(normal.getAttempts() == 100 && normal.getAcceptedMoves() >= 0
                            && normal.getAcceptedMoves() <= normal.getValidCandidates()
                            && normal.getValidCandidates() <= normal.getAttempts()
                            && best.isComplete() && best.calculateEnergy() <= initial.calculateEnergy(),
                    "SA stats: invarianti i numeruesve dhe cilesia e rezultatit ruhen per seed " + seed);
        }

        Random oldApiRandom = new Random(2025);
        Random statsApiRandom = new Random(2025);
        Protein oldApiBest = SimulatedAnnealing.optimize(initial, 200, 2, 0.995, oldApiRandom);
        AnnealingResult detailed = SimulatedAnnealing.optimizeWithStats(initial, 200, 2, 0.995, statsApiRandom);
        Protein statsApiBest = detailed.getBestProtein();
        check(sameCoordinates(oldApiBest, statsApiBest, initialPositions.length)
                        && oldApiBest.calculateEnergy() == statsApiBest.calculateEnergy(),
                "SA stats: optimize dhe optimizeWithStats japin te njejtin best me seed identik");
        check(oldApiRandom.nextLong() == statsApiRandom.nextLong(),
                "SA stats: dy API-te ruajne te njejten vazhdimesi te Random");
        checkCoordinates(initial, initialPositions, "SA stats: inputi mbetet i pandryshuar pas te gjitha thirrjeve");
    }

    private static void testBeamSearchStats() {
        BeamSearchResult single = BeamSearch.searchWithStats("H", 2);
        check(single.getExpandedStates() == 1 && single.getGeneratedCandidates() == 1
                        && single.getRetainedCandidates() == 1 && single.getPrunedCandidates() == 0
                        && single.getCompletedLevels() == 1,
                "Beam stats: H me width 2 numeron 1 zgjerim, 1 gjenerim, 1 ruajtje dhe 0 largime");
        Protein singleBest = single.getBestProtein();
        check(singleBest != null && singleBest.isComplete() && singleBest.calculateEnergy() == 0
                        && singleBest.getPosition(0).isSamePosition(new Position(0, 0, 0)),
                "Beam stats: origjina perfshihet ne numerim dhe rezultati eshte i plote");

        BeamSearchResult pair = BeamSearch.searchWithStats("HH", 2);
        check(pair.getExpandedStates() == 2 && pair.getGeneratedCandidates() == 7
                        && pair.getRetainedCandidates() == 3 && pair.getPrunedCandidates() == 4
                        && pair.getCompletedLevels() == 2,
                "Beam stats: HH me width 2 numeron 2 zgjerime, 7 gjenerime, 3 ruajtje dhe 4 largime");
        Protein pairBest = pair.getBestProtein();
        check(pairBest != null && pairBest.isComplete() && pairBest.calculateEnergy() == 0
                        && pairBest.getPosition(0).isNeighbor(pairBest.getPosition(1)),
                "Beam stats: HH jep rezultat te plote dhe te vlefshem");

        for (int width : new int[]{1, 30}) {
            Protein oldApiBest = BeamSearch.search("HHHH", width);
            BeamSearchResult detailed = BeamSearch.searchWithStats("HHHH", width);
            Protein statsApiBest = detailed.getBestProtein();
            check(oldApiBest != null && statsApiBest != null && statsApiBest.isComplete()
                            && sameCoordinates(oldApiBest, statsApiBest, 4)
                            && oldApiBest.calculateEnergy() == statsApiBest.calculateEnergy(),
                    "Beam stats: search dhe searchWithStats japin rezultat identik me width " + width);
            check(detailed.getGeneratedCandidates() == detailed.getRetainedCandidates() + detailed.getPrunedCandidates()
                            && detailed.getExpandedStates() >= detailed.getCompletedLevels()
                            && detailed.getRetainedCandidates() >= detailed.getCompletedLevels()
                            && detailed.getPrunedCandidates() >= 0 && detailed.getCompletedLevels() == 4,
                    "Beam stats: invarianti i numeruesve ruhet me width " + width);
        }

        Protein source = createSquare("HHHH");
        BeamSearchResult stored = new BeamSearchResult(source, 2, 7, 3, 4, 2);
        check(source.tryMoveLast(new Position(1, 2, 0)) && stored.getBestProtein().calculateEnergy() == -1,
                "Beam stats: konstruktori kopjon proteinen dhe nuk ndan objektin burim");
        Protein received = stored.getBestProtein();
        check(received.tryMoveLast(new Position(1, 2, 0)) && stored.getBestProtein().calculateEnergy() == -1,
                "Beam stats: ndryshimi i kopjes nga getter nuk prek rezultatin e ruajtur");

        BeamSearchResult noResult = new BeamSearchResult(null, 4_000_000_000L, 7_000_000_000L,
                3_000_000_000L, 4_000_000_000L, 2);
        check(noResult.getBestProtein() == null && noResult.getExpandedStates() == 4_000_000_000L
                        && noResult.getGeneratedCandidates() == 7_000_000_000L
                        && noResult.getRetainedCandidates() == 3_000_000_000L
                        && noResult.getPrunedCandidates() == 4_000_000_000L && noResult.getCompletedLevels() == 2,
                "Beam stats: rezultati pa proteine lexohet normalisht dhe ruan numeruesit long");
    }

    private static void testAnnealingTimeLimits() {
        Position[] initialPositions = {
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(1, 2, 0)
        };
        Protein initial = createProtein("HHHH", initialPositions);
        ControlledAnnealingRandom noMoves = new ControlledAnnealingRandom(new int[0]);
        AnnealingResult immediate = SimulatedAnnealing.optimizeWithStats(initial, 5, 2, 0.5, noMoves,
                1, new ControlledNanoClock(0, 1_000_000));
        check(immediate.getStopReason() == StopReason.TIME_LIMIT && immediate.getAttempts() == 0
                        && immediate.getValidCandidates() == 0 && immediate.getAcceptedMoves() == 0,
                "Time limit: SA skadon para perpjekjes se pare pa konsumuar Random");
        Protein initialCopy = immediate.getBestProtein();
        check(initialCopy != initial && initialCopy.isComplete()
                        && sameCoordinates(initialCopy, initial, initialPositions.length),
                "Time limit: SA pa perpjekje ruan kopje te pavarur te fillimit");

        ControlledNanoClock zeroClock = new ControlledNanoClock(0, 1_000_000);
        AnnealingResult zero = SimulatedAnnealing.optimizeWithStats(initial, 0, 2, 0.5, noMoves, 1, zeroClock);
        check(zero.getStopReason() == StopReason.ITERATION_LIMIT && zero.getAttempts() == 0 && zeroClock.index == 1,
                "Time limit: zero iteracione ndalen nga kufiri i iteracioneve");

        ControlledAnnealingRandom moves = new ControlledAnnealingRandom(new int[]{0, 1, 0, 2}, 0.0);
        ControlledNanoClock afterTwo = new ControlledNanoClock(0, 0, 500_000, 1_000_000);
        AnnealingResult stopped = SimulatedAnnealing.optimizeWithStats(initial, 5, 2, 0.5, moves, 1, afterTwo);
        check(stopped.getStopReason() == StopReason.TIME_LIMIT && stopped.getAttempts() == 2
                        && stopped.getValidCandidates() == 2 && stopped.getAcceptedMoves() == 2
                        && moves.intIndex == 4 && moves.doubleIndex == 1 && afterTwo.index == 4,
                "Time limit: SA kontrollon kohen vetem para perpjekjeve dhe perfundon dy hapa");
        check(stopped.getBestProtein().calculateEnergy() == -1
                        && sameCoordinates(stopped.getBestProtein(), createSquare("HHHH"), 4),
                "Time limit: SA kthen best edhe pasi eshte pranuar nje perkeqesim");
        checkCoordinates(initial, initialPositions, "Time limit: SA nuk ndryshon inputin");

        Random oldRandom = new Random(2025);
        Random unlimitedRandom = new Random(2025);
        AnnealingResult oldApi = SimulatedAnnealing.optimizeWithStats(initial, 100, 2, 0.995, oldRandom);
        ControlledNanoClock unusedClock = new ControlledNanoClock();
        AnnealingResult unlimited = SimulatedAnnealing.optimizeWithStats(initial, 100, 2, 0.995,
                unlimitedRandom, 0, unusedClock);
        check(sameCoordinates(oldApi.getBestProtein(), unlimited.getBestProtein(), 4)
                        && oldApi.getBestProtein().calculateEnergy() == unlimited.getBestProtein().calculateEnergy()
                        && oldApi.getAttempts() == unlimited.getAttempts()
                        && oldApi.getValidCandidates() == unlimited.getValidCandidates()
                        && oldApi.getAcceptedMoves() == unlimited.getAcceptedMoves()
                        && unlimited.getStopReason() == StopReason.ITERATION_LIMIT && unusedClock.index == 0
                        && oldRandom.nextLong() == unlimitedRandom.nextLong(),
                "Time limit: SA pa kufi ruan koordinatat, energjine, statistikat dhe serine Random");
        check(sameCoordinates(SimulatedAnnealing.optimize(initial, 100, 2, 0.995, new Random(2025), 0),
                        unlimited.getBestProtein(), 4),
                "Time limit: varianti optimize me kufi zero eshte i perputhshem");

        Protein single = createProtein("H", new Position[]{new Position(0, 0, 0)});
        AnnealingResult huge = SimulatedAnnealing.optimizeWithStats(single, 1, 2, 0.5, noMoves,
                Long.MAX_VALUE, new ControlledNanoClock(0, Long.MAX_VALUE));
        check(huge.getAttempts() == 1 && huge.getStopReason() == StopReason.ITERATION_LIMIT,
                "Time limit: kufiri Long.MAX_VALUE ms nuk tejmbushet gjate krahasimit");
        AnnealingResult wrapped = SimulatedAnnealing.optimizeWithStats(single, 1, 2, 0.5, noMoves,
                1, new ControlledNanoClock(Long.MAX_VALUE - 499_999, Long.MIN_VALUE + 500_000));
        check(wrapped.getAttempts() == 0 && wrapped.getStopReason() == StopReason.TIME_LIMIT,
                "Time limit: diferenca nanoTime trajton kalimin permes kufirit long");

        boolean negativeRejected = false;
        try {
            SimulatedAnnealing.optimizeWithStats(initial, 0, 2, 0.5, noMoves, -1);
        } catch (IllegalArgumentException e) {
            negativeRejected = true;
        }
        check(negativeRejected, "Time limit: SA refuzon kufirin negativ");
    }

    private static void testBeamTimeLimits() {
        BeamSearchResult immediate = BeamSearch.searchWithStats("HHHH", 2, 1,
                new ControlledNanoClock(0, 1_000_000));
        check(immediate.getBestProtein() == null && immediate.getStopReason() == StopReason.TIME_LIMIT
                        && immediate.getExpandedStates() == 0 && immediate.getGeneratedCandidates() == 0
                        && immediate.getRetainedCandidates() == 0 && immediate.getPrunedCandidates() == 0
                        && immediate.getCompletedLevels() == 0,
                "Time limit: Beam skadon para nivelit te pare pa rezultat te pjesshem");

        ControlledNanoClock afterThree = new ControlledNanoClock(0, 0, 100_000, 200_000, 1_000_000);
        BeamSearchResult partial = BeamSearch.searchWithStats("HHHH", 2, 1, afterThree);
        check(partial.getBestProtein() == null && partial.getStopReason() == StopReason.TIME_LIMIT
                        && partial.getCompletedLevels() == 3 && partial.getExpandedStates() == 4
                        && partial.getGeneratedCandidates() == 17 && partial.getRetainedCandidates() == 5
                        && partial.getPrunedCandidates() == 12 && afterThree.index == 5,
                "Time limit: Beam perfundon te gjithe prinderit e nivelit dhe nuk kthen strukture te pjesshme");

        ControlledNanoClock lastLevel = new ControlledNanoClock(0, 0, 0, 1_000_000);
        BeamSearchResult complete = BeamSearch.searchWithStats("HH", 2, 1, lastLevel);
        check(complete.getBestProtein() != null && complete.getBestProtein().isComplete()
                        && complete.getStopReason() == StopReason.COMPLETED && complete.getCompletedLevels() == 2
                        && complete.getExpandedStates() == 2 && complete.getGeneratedCandidates() == 7
                        && complete.getRetainedCandidates() == 3 && complete.getPrunedCandidates() == 4
                        && lastLevel.index == 3,
                "Time limit: perfundimi i nivelit te fundit ka perparesi dhe nuk kontrollohet perseri koha");

        BeamSearchResult oldApi = BeamSearch.searchWithStats("HHHH", 30);
        ControlledNanoClock unusedClock = new ControlledNanoClock();
        BeamSearchResult unlimited = BeamSearch.searchWithStats("HHHH", 30, 0, unusedClock);
        check(sameCoordinates(oldApi.getBestProtein(), unlimited.getBestProtein(), 4)
                        && oldApi.getBestProtein().calculateEnergy() == unlimited.getBestProtein().calculateEnergy()
                        && oldApi.getExpandedStates() == unlimited.getExpandedStates()
                        && oldApi.getGeneratedCandidates() == unlimited.getGeneratedCandidates()
                        && oldApi.getRetainedCandidates() == unlimited.getRetainedCandidates()
                        && oldApi.getPrunedCandidates() == unlimited.getPrunedCandidates()
                        && oldApi.getCompletedLevels() == unlimited.getCompletedLevels()
                        && unlimited.getStopReason() == StopReason.COMPLETED && unusedClock.index == 0,
                "Time limit: Beam pa kufi ruan rezultatin, rendin dhe te gjitha statistikat");
        check(sameCoordinates(BeamSearch.search("HHHH", 30, 0), unlimited.getBestProtein(), 4),
                "Time limit: varianti search me kufi zero eshte i perputhshem");
        BeamSearchResult noCandidates = new BeamSearchResult(null, 4, 7, 3, 4, 2);
        check(noCandidates.getStopReason() == StopReason.NO_CANDIDATES && noCandidates.getBestProtein() == null,
                "Time limit: mungesa e kandidateve ka arsye te ndryshme nga skadimi");

        ControlledNanoClock separateStarts = new ControlledNanoClock(0, 2_000_000, 2_000_000, 2_000_000);
        Protein single = createProtein("H", new Position[]{new Position(0, 0, 0)});
        AnnealingResult firstSearch = SimulatedAnnealing.optimizeWithStats(single, 1, 2, 0.5,
                new ControlledAnnealingRandom(new int[0]), 1, separateStarts);
        BeamSearchResult secondSearch = BeamSearch.searchWithStats("H", 2, 1, separateStarts);
        check(firstSearch.getStopReason() == StopReason.TIME_LIMIT && secondSearch.getStopReason() == StopReason.COMPLETED
                        && secondSearch.getBestProtein().isComplete() && separateStarts.index == 4,
                "Time limit: secili algoritem nis kufirin e vet edhe kur perdor te njejten ore");

        boolean negativeRejected = false;
        try {
            BeamSearch.searchWithStats("HH", 2, -1);
        } catch (IllegalArgumentException e) {
            negativeRejected = true;
        }
        check(negativeRejected, "Time limit: Beam refuzon kufirin negativ");
    }

    private static void testBeamRotatedShapes() {
        Protein line = createProtein("HHH", new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0), new Position(2, 0, 0)
        });
        Protein elbow = createProtein("HHH", new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0), new Position(1, 1, 0)
        });
        Protein rotatedLine = createProtein("HHH", new Position[]{
                new Position(0, 0, 0), new Position(0, 0, 1), new Position(0, 0, 2)
        });
        ArrayList<Protein> distinct = BeamSearch.removeRotatedDuplicates(List.of(line, elbow, rotatedLine), 3);
        check(line.calculateEnergy() == 0 && elbow.calculateEnergy() == 0 && distinct.size() == 2
                        && distinct.get(0) == line && distinct.get(1) == elbow,
                "Beam rotations: vija dhe forma L kane energji te njejte por mbeten forma te ndryshme");
        ArrayList<Protein> selected = BeamSearch.selectBestCandidates(distinct, 2);
        check(sameCoordinates(selected.get(0), line, 3) && sameCoordinates(selected.get(1), elbow, 3),
                "Beam rotations: ruhet rendi i pare edhe pas perzgjedhjes me energji te barabarta");

        Protein spatial = createProtein("HHHH", new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(1, 1, 1)
        });
        // Krijo orientime duke perdorur rrotullimet ekzistuese, jo algoritmin e celesit.
        char[] baseAxes = {'Z', 'Z', 'Z', 'Z', 'Y', 'Y'};
        int[] baseTurns = {0, 2, 1, 3, 1, 3};
        char[] spinAxes = {'X', 'X', 'Y', 'Y', 'Z', 'Z'};
        ArrayList<Protein> rotations = new ArrayList<>();
        boolean allDifferent = true;
        for (int direction = 0; direction < baseAxes.length; direction++) {
            Protein base = spatial.copy();
            if (baseTurns[direction] != 0) {
                allDifferent &= base.tryPivotRotation(0, baseAxes[direction], baseTurns[direction]);
            }
            for (int turns = 0; turns < 4; turns++) {
                Protein rotated = base.copy();
                if (turns != 0) {
                    allDifferent &= rotated.tryPivotRotation(0, spinAxes[direction], turns);
                }
                for (Protein previous : rotations) {
                    allDifferent &= !sameCoordinates(previous, rotated, 4);
                }
                rotations.add(rotated);
            }
        }
        check(allDifferent && rotations.size() == 24,
                "Beam rotations: shembulli 3D ka 24 orientime te ndryshme ne test");
        ArrayList<Protein> oneShape = BeamSearch.removeRotatedDuplicates(rotations, 4);
        check(oneShape.size() == 1 && oneShape.get(0) == rotations.get(0)
                        && sameCoordinates(oneShape.get(0), spatial, 4),
                "Beam rotations: te 24 rrotullimet 3D njihen dhe ruhet kandidati i pare pa rrotulluar koordinatat");

        Protein mirrored = createProtein("HHHH", new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(1, 1, -1)
        });
        check(BeamSearch.removeRotatedDuplicates(List.of(spatial, mirrored), 4).size() == 2,
                "Beam rotations: pasqyrimi i nje zinxhiri 3D nuk hiqet si rrotullim");

        Protein bent = createProtein("HHHH", new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(2, 0, 0), new Position(2, 1, 0)
        });
        Protein reversed = createProtein("HHHH", new Position[]{
                new Position(2, 1, 0), new Position(2, 0, 0),
                new Position(1, 0, 0), new Position(0, 0, 0)
        });
        check(BeamSearch.removeRotatedDuplicates(List.of(bent, reversed), 4).size() == 2,
                "Beam rotations: rendi i sekuences nuk kthehet mbrapsht");

        int maximum = Integer.MAX_VALUE;
        Protein translated = createProtein("HHHH", new Position[]{
                new Position(maximum - 1, maximum - 1, maximum - 1),
                new Position(maximum, maximum - 1, maximum - 1),
                new Position(maximum, maximum, maximum - 1), new Position(maximum, maximum, maximum)
        });
        Protein snapshot = translated.copy();
        ArrayList<Protein> translatedShape = BeamSearch.removeRotatedDuplicates(List.of(translated, spatial), 4);
        check(translatedShape.size() == 1 && translatedShape.get(0) == translated
                        && sameCoordinates(translated, snapshot, 4) && sameCoordinates(spatial, rotations.get(0), 4),
                "Beam rotations: hapat shmangin zhvendosjen dhe ruajne koordinatat prane kufirit int");
    }

    private static void testBeamRotationSearch() {
        BeamSearchResult enabled = BeamSearch.searchWithStats("HH", 30, 0, true);
        check(enabled.getCompletedLevels() == 2 && enabled.getExpandedStates() == 2
                        && enabled.getGeneratedCandidates() == 7 && enabled.getRetainedCandidates() == 2
                        && enabled.getRotatedDuplicates() == 5 && enabled.getPrunedCandidates() == 0,
                "Beam rotations: HH aktiv jep gjithsej 7 gjenerime, 2 ruajtje, 5 kopje dhe 0 largime nga gjeresia");
        BeamSearchResult disabled = BeamSearch.searchWithStats("HH", 30, 0, false);
        check(disabled.getGeneratedCandidates() == 7 && disabled.getRetainedCandidates() == 7
                        && disabled.getRotatedDuplicates() == 0 && disabled.getPrunedCandidates() == 0,
                "Beam rotations: HH joaktiv ruan te gjashte drejtimet e nivelit te dyte");
        check(enabled.getBestProtein().isComplete() && enabled.getBestProtein().calculateEnergy() == 0
                        && sameCoordinates(enabled.getBestProtein(), disabled.getBestProtein(), 2)
                        && enabled.getBestProtein().getPosition(1).isSamePosition(new Position(1, 0, 0)),
                "Beam rotations: HH ruan drejtimin e pare +X dhe energjine 0");

        BeamSearchResult narrowEnabled = BeamSearch.searchWithStats("HH", 1, true);
        BeamSearchResult narrowDisabled = BeamSearch.searchWithStats("HH", 1, false);
        check(narrowEnabled.getRotatedDuplicates() == 5 && narrowEnabled.getPrunedCandidates() == 0
                        && narrowDisabled.getRotatedDuplicates() == 0 && narrowDisabled.getPrunedCandidates() == 5,
                "Beam rotations: kopjet hiqen perpara gjeresise dhe numerohen veqmas");

        BeamSearchResult spatialForms = BeamSearch.searchWithStats("PPPP", 30, true);
        check(spatialForms.getCompletedLevels() == 4 && spatialForms.getExpandedStates() == 5
                        && spatialForms.getGeneratedCandidates() == 22 && spatialForms.getRetainedCandidates() == 11
                        && spatialForms.getRotatedDuplicates() == 11 && spatialForms.getPrunedCandidates() == 0
                        && spatialForms.getBestProtein().calculateEnergy() == 0,
                "Beam rotations: zgjatjet e te gjithe prinderve bashkohen dhe perfshijne te 7 format me kater pika");
        BeamSearchResult folded = BeamSearch.searchWithStats("HHHH", 30, true);
        check(folded.getBestProtein().isComplete() && folded.getBestProtein().isValid()
                        && folded.getBestProtein().calculateEnergy() == -1,
                "Beam rotations: perzgjedhja sipas energjise vazhdon te gjeje katrorin HHHH");

        for (int width : new int[]{1, 30}) {
            BeamSearchResult oldApi = BeamSearch.searchWithStats("HPPHH", width);
            BeamSearchResult off = BeamSearch.searchWithStats("HPPHH", width, false);
            check(sameCoordinates(oldApi.getBestProtein(), off.getBestProtein(), 5)
                            && oldApi.getBestProtein().calculateEnergy() == off.getBestProtein().calculateEnergy()
                            && oldApi.getExpandedStates() == off.getExpandedStates()
                            && oldApi.getGeneratedCandidates() == off.getGeneratedCandidates()
                            && oldApi.getRetainedCandidates() == off.getRetainedCandidates()
                            && oldApi.getPrunedCandidates() == off.getPrunedCandidates()
                            && oldApi.getCompletedLevels() == off.getCompletedLevels()
                            && off.getRotatedDuplicates() == 0 && off.getStopReason() == oldApi.getStopReason(),
                    "Beam rotations: opsioni i fikur ruan API-ne e vjeter me width " + width);
            BeamSearchResult on = BeamSearch.searchWithStats("HPPHH", width, true);
            check(on.getBestProtein().isComplete() && on.getBestProtein().isValid()
                            && on.getGeneratedCandidates() == on.getRetainedCandidates()
                            + on.getRotatedDuplicates() + on.getPrunedCandidates(),
                    "Beam rotations: invarianti i tre numeruesve ruhet me width " + width);
        }

        ControlledNanoClock beforeFirst = new ControlledNanoClock(0, 1_000_000);
        BeamSearchResult immediate = BeamSearch.searchWithStats("HHH", 30, 1, true, beforeFirst);
        check(immediate.getStopReason() == StopReason.TIME_LIMIT && immediate.getBestProtein() == null
                        && immediate.getCompletedLevels() == 0 && immediate.getGeneratedCandidates() == 0
                        && immediate.getRotatedDuplicates() == 0,
                "Beam rotations: skadimi para nivelit te pare ruan numeruesit zero");
        ControlledNanoClock afterTwo = new ControlledNanoClock(0, 0, 500_000, 1_000_000);
        BeamSearchResult timed = BeamSearch.searchWithStats("HHH", 30, 1, true, afterTwo);
        check(timed.getStopReason() == StopReason.TIME_LIMIT && timed.getBestProtein() == null
                        && timed.getCompletedLevels() == 2 && timed.getGeneratedCandidates() == 7
                        && timed.getRetainedCandidates() == 2 && timed.getRotatedDuplicates() == 5
                        && timed.getPrunedCandidates() == 0 && afterTwo.index == 4,
                "Beam rotations: kufiri kohor perfundon nivelin bashke me heqjen e kopjeve pa kthyer rezultat te pjesshem");
        ControlledNanoClock finalLevel = new ControlledNanoClock(0, 0, 0, 1_000_000);
        BeamSearchResult complete = BeamSearch.searchWithStats("HH", 30, 1, true, finalLevel);
        check(complete.getStopReason() == StopReason.COMPLETED && complete.getBestProtein().isComplete()
                        && complete.getRotatedDuplicates() == 5 && finalLevel.index == 3,
                "Beam rotations: niveli i fundit i perfunduar raportohet si sukses");
        ControlledNanoClock unusedClock = new ControlledNanoClock();
        BeamSearchResult unlimited = BeamSearch.searchWithStats("HHHH", 30, 0, true, unusedClock);
        check(unusedClock.index == 0 && sameCoordinates(unlimited.getBestProtein(), folded.getBestProtein(), 4)
                        && sameCoordinates(BeamSearch.search("HHHH", 30, true), folded.getBestProtein(), 4)
                        && sameCoordinates(BeamSearch.search("HHHH", 30, 0, true), folded.getBestProtein(), 4),
                "Beam rotations: variantet publike dhe burimi i kohes ruajne sjelljen pa kufi");
    }

    private static void testWeightedEnergy() {
        Protein square = createSquare("HHHH");
        Protein line = createProtein("HHHH", new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(2, 0, 0), new Position(3, 0, 0)
        });
        check(square.calculateEnergy() == -1 && square.calculateEnergy(1) == -1L
                        && square.calculateEnergy(2) == -2L,
                "Weighted energy: katrori HHHH ka vetem nje kontakt, me energji -1 ose -2");
        check(line.calculateEnergy(1) == 0 && line.calculateEnergy(2) == 0,
                "Weighted energy: vija HHHH ka energji zero per te dyja peshat");
        Protein rectangle = createProtein("HHHHHH", new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0), new Position(2, 0, 0),
                new Position(2, 1, 0), new Position(1, 1, 0), new Position(0, 1, 0)
        });
        check(rectangle.calculateEnergy() == -2 && rectangle.calculateEnergy(Integer.MAX_VALUE) == -4_294_967_294L,
                "Weighted energy: dy kontakte me peshen maksimale llogariten ne long pa overflow int");
        Protein pair = createProtein("HH", new Position[]{new Position(0, 0, 0), new Position(1, 0, 0)});
        check(createSquare("HPPH").calculateEnergy(2) == -2 && createSquare("HPPP").calculateEnergy(2) == 0
                        && createSquare("PPPP").calculateEnergy(2) == 0 && pair.calculateEnergy(2) == 0,
                "Weighted energy: ruhen kategorite H/P dhe perjashtimi i lidhjeve te drejtperdrejta");

        for (int weight : new int[]{0, -1}) {
            boolean energyRejected = false;
            boolean annealingRejected = false;
            boolean beamRejected = false;
            boolean selectionRejected = false;
            try {
                square.calculateEnergy(weight);
            } catch (IllegalArgumentException e) {
                energyRejected = true;
            }
            try {
                SimulatedAnnealing.optimizeWithStats(square, 0, 2, 0.995, new Random(2025), 0, weight);
            } catch (IllegalArgumentException e) {
                annealingRejected = true;
            }
            try {
                BeamSearch.searchWithStats("HHHH", 30, 0, true, weight);
            } catch (IllegalArgumentException e) {
                beamRejected = true;
            }
            try {
                BeamSearch.selectBestCandidates(List.of(), 30, weight);
            } catch (IllegalArgumentException e) {
                selectionRejected = true;
            }
            check(energyRejected && annealingRejected && beamRejected && selectionRejected,
                    "Weighted energy: pesha " + weight + " refuzohet ne llogaritje dhe algoritme");
        }
    }

    private static void testWeightedAnnealing() {
        Protein square = createSquare("HHHH");
        check(SimulatedAnnealing.shouldAccept(square.calculateEnergy(1), 0L, 2, new ControlledDoubleRandom(0.5))
                        && !SimulatedAnnealing.shouldAccept(square.calculateEnergy(2), 0L, 2,
                        new ControlledDoubleRandom(0.5)),
                "Weighted SA: humbja e nje kontakti me T=2 dhe random=0.5 pranohet me peshe 1 por jo me 2");
        // I njejti propozim e hap katrorin ne (1,2,0), duke humbur kontaktin H-H.
        ControlledAnnealingRandom unitRandom = new ControlledAnnealingRandom(new int[]{0, 2}, 0.5);
        ControlledAnnealingRandom doubleRandom = new ControlledAnnealingRandom(new int[]{0, 2}, 0.5);
        AnnealingResult unit = SimulatedAnnealing.optimizeWithStats(square, 1, 2, 0.995, unitRandom, 0, 1);
        AnnealingResult doubled = SimulatedAnnealing.optimizeWithStats(square, 1, 2, 0.995, doubleRandom, 0, 2);
        check(unit.getAttempts() == 1 && doubled.getAttempts() == 1
                        && unit.getValidCandidates() == 1 && doubled.getValidCandidates() == 1
                        && unit.getAcceptedMoves() == 1 && doubled.getAcceptedMoves() == 0
                        && unitRandom.intIndex == 2 && doubleRandom.intIndex == 2
                        && unitRandom.doubleIndex == 1 && doubleRandom.doubleIndex == 1,
                "Weighted SA: cikli perdor realisht peshen ne vendimin e pranimit");
        check(sameCoordinates(unit.getBestProtein(), square, 4)
                        && sameCoordinates(doubled.getBestProtein(), square, 4)
                        && unit.getBestProtein().calculateEnergy(1) == -1
                        && doubled.getBestProtein().calculateEnergy(2) == -2 && square.calculateEnergy() == -1,
                "Weighted SA: best dhe origjinali ruhen pas pranimit ose refuzimit te perkeqesimit");
        check(SimulatedAnnealing.shouldAccept(Long.MIN_VALUE, Long.MIN_VALUE + 1, 2, new ControlledDoubleRandom(0.5))
                        && !SimulatedAnnealing.shouldAccept(Long.MIN_VALUE, Long.MIN_VALUE + 1, 2,
                        new ControlledDoubleRandom(0.7))
                        && !SimulatedAnnealing.shouldAccept(Long.MIN_VALUE, Long.MAX_VALUE, 2,
                        new ControlledDoubleRandom(0.0))
                        && SimulatedAnnealing.shouldAccept(Long.MAX_VALUE, Long.MIN_VALUE, 2,
                        new ControlledDoubleRandom()),
                "Weighted SA: diferencat long ruhen sakte dhe nuk tejmbushen ne skaje");

        for (long seed : new long[]{2025, -7}) {
            Random oldRandom = new Random(seed);
            Random weightedRandom = new Random(seed);
            AnnealingResult oldApi = SimulatedAnnealing.optimizeWithStats(square, 100, 2, 0.995, oldRandom);
            AnnealingResult weighted = SimulatedAnnealing.optimizeWithStats(square, 100, 2, 0.995,
                    weightedRandom, 0, 1);
            check(sameCoordinates(oldApi.getBestProtein(), weighted.getBestProtein(), 4)
                            && oldApi.getBestProtein().calculateEnergy() == weighted.getBestProtein().calculateEnergy(1)
                            && oldApi.getAttempts() == weighted.getAttempts()
                            && oldApi.getValidCandidates() == weighted.getValidCandidates()
                            && oldApi.getAcceptedMoves() == weighted.getAcceptedMoves()
                            && oldApi.getStopReason() == weighted.getStopReason()
                            && oldRandom.nextLong() == weightedRandom.nextLong(),
                    "Weighted SA: pesha 1 ruan koordinatat, statistikat dhe serine Random me seed " + seed);
        }
        Protein wrapperBest = SimulatedAnnealing.optimize(square, 100, 2, 0.995, new Random(2025), 0, 2);
        check(wrapperBest.isComplete() && wrapperBest.calculateEnergy(2) == -2,
                "Weighted SA: varianti optimize ruan rezultatin me energji te peshuar");
        AnnealingResult timed = SimulatedAnnealing.optimizeWithStats(square, 5, 2, 0.995,
                new ControlledAnnealingRandom(new int[0]), 1, 2, new ControlledNanoClock(0, 1_000_000));
        check(timed.getStopReason() == StopReason.TIME_LIMIT && timed.getAttempts() == 0
                        && timed.getBestProtein().calculateEnergy(2) == -2,
                "Weighted SA: kufiri kohor ruan strukturen dhe energjine e peshuar para hapit te pare");
    }

    private static void testWeightedBeamSearch() {
        Protein line = createProtein("HHHHHH", new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0), new Position(2, 0, 0),
                new Position(3, 0, 0), new Position(4, 0, 0), new Position(5, 0, 0)
        });
        Protein oneContact = createProtein("HHHHHH", new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0), new Position(1, 1, 0),
                new Position(0, 1, 0), new Position(0, 1, 1), new Position(0, 1, 2)
        });
        Protein rectangle = createProtein("HHHHHH", new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0), new Position(2, 0, 0),
                new Position(2, 1, 0), new Position(1, 1, 0), new Position(0, 1, 0)
        });
        Protein tiedRectangle = rectangle.copy();
        tiedRectangle.tryPivotRotation(0, 'Z', 1);
        List<Protein> candidates = List.of(line, rectangle, oneContact, tiedRectangle);
        for (int weight : new int[]{2, Integer.MAX_VALUE}) {
            ArrayList<Protein> selected = BeamSearch.selectBestCandidates(candidates, 4, weight);
            check(sameCoordinates(selected.get(0), rectangle, 6)
                            && sameCoordinates(selected.get(1), tiedRectangle, 6)
                            && sameCoordinates(selected.get(2), oneContact, 6)
                            && sameCoordinates(selected.get(3), line, 6)
                            && selected.get(0).calculateEnergy(weight) == -2L * weight
                            && selected.get(2).calculateEnergy(weight) == -1L * weight,
                    "Weighted Beam: renditja dhe barazimet ruhen me peshe " + weight + " duke perdorur long");
        }
        for (boolean removeCopies : new boolean[]{false, true}) {
            BeamSearchResult reference = BeamSearch.searchWithStats("HHPHPHH", 30, 0, removeCopies);
            for (int weight : new int[]{2, Integer.MAX_VALUE}) {
                BeamSearchResult weighted = BeamSearch.searchWithStats("HHPHPHH", 30, 0, removeCopies, weight);
                check(sameCoordinates(reference.getBestProtein(), weighted.getBestProtein(), 7)
                                && weighted.getBestProtein().calculateEnergy(weight)
                                == (long) reference.getBestProtein().calculateEnergy() * weight
                                && reference.getExpandedStates() == weighted.getExpandedStates()
                                && reference.getGeneratedCandidates() == weighted.getGeneratedCandidates()
                                && reference.getRetainedCandidates() == weighted.getRetainedCandidates()
                                && reference.getPrunedCandidates() == weighted.getPrunedCandidates()
                                && reference.getRotatedDuplicates() == weighted.getRotatedDuplicates()
                                && reference.getCompletedLevels() == weighted.getCompletedLevels()
                                && reference.getStopReason() == weighted.getStopReason(),
                        "Weighted Beam: pesha " + weight + " nuk ndryshon strukturen ose statistikat, kopjet=" + removeCopies);
            }
        }
        Protein best = BeamSearch.search("HHHH", 30, 0, true, 2);
        check(best.isComplete() && best.calculateEnergy(2) == -2
                        && sameCoordinates(best, BeamSearch.search("HHHH", 30, true), 4),
                "Weighted Beam: varianti search ruan katrorin me energji -2");
        ControlledNanoClock afterTwo = new ControlledNanoClock(0, 0, 500_000, 1_000_000);
        BeamSearchResult timed = BeamSearch.searchWithStats("HHHH", 30, 1, true, 2, afterTwo);
        check(timed.getStopReason() == StopReason.TIME_LIMIT && timed.getBestProtein() == null
                        && timed.getCompletedLevels() == 2 && timed.getGeneratedCandidates() == 7
                        && timed.getRetainedCandidates() == 2 && timed.getRotatedDuplicates() == 5
                        && timed.getPrunedCandidates() == 0 && afterTwo.index == 4,
                "Weighted Beam: pesha ruan kufirin kohor dhe numerimin e kopjeve te rrotulluara");
    }

    private static void testStructureExport() {
        Path temporary;
        try {
            temporary = Files.createTempDirectory("protein-export-tests-");
        } catch (IOException e) {
            throw new AssertionError("Nuk u krijua dosja e perkohshme e testeve.", e);
        }
        try {
            Protein square = createSquare("HHHH");
            Protein snapshot = square.copy();
            Path html = StructureExporter.export(temporary, square, square, 2, StopReason.COMPLETED);
            List<String> expected = List.of("index,type,x,y,z", "0,H,0,0,0", "1,H,1,0,0",
                    "2,H,1,1,0", "3,H,0,1,0");
            check(Files.readAllLines(html.resolveSibling("sa.csv")).equals(expected),
                    "Export: CSV e SA ka kolonat e sakta dhe 4 rreshta koordinatash origjinale");
            check(Files.readAllLines(html.resolveSibling("beam.csv")).equals(expected),
                    "Export: CSV e Beam ruan te njejtin rend te indekseve nga zero");
            String data = exportedData(html);
            check(data.contains("\"sequence\":\"HHHH\"") && data.contains("\"contactWeight\":2")
                            && data.contains("\"bonds\":[[0,1],[1,2],[2,3]]")
                            && data.contains("\"contacts\":[[0,3]]")
                            && data.indexOf("\"energy\":\"-2\"") != data.lastIndexOf("\"energy\":\"-2\"")
                            && data.contains("\"name\":\"Simulated Annealing\"") && data.contains("\"name\":\"Beam Search\""),
                    "Export: katrori ne HTML ka 3 lidhje, vetem kontaktin 0-3 dhe energji -2 per te dy rezultatet");
            String page = Files.readString(html);
            check(!page.contains("http:") && !page.contains("https:")
                            && !page.contains("fetch(") && page.contains("id=\"result\"")
                            && page.contains("id=\"reset\"") && page.contains("id=\"structure\""),
                    "Export: HTML permban te dhenat dhe kontrollet pa burime te jashtme");
            check(sameCoordinates(square, snapshot, 4) && square.calculateEnergy(2) == -2,
                    "Export: ruajtja nuk ndryshon proteinen ose energjine");
            Path again = StructureExporter.export(temporary, square, square, 2, StopReason.COMPLETED);
            check(!again.getParent().equals(html.getParent()) && Files.readString(html).equals(page)
                            && Files.readAllLines(html.resolveSibling("sa.csv")).equals(expected),
                    "Export: ruajtjet e perseritura krijojne dosje te reja pa mbishkruar skedaret");

            Protein mixed = createProtein("HPPH", new Position[]{new Position(10, -2, 4), new Position(11, -2, 4),
                    new Position(11, -1, 4), new Position(10, -1, 4)});
            Path mixedHtml = StructureExporter.export(temporary, mixed, null, 2, StopReason.TIME_LIMIT);
            check(Files.readAllLines(mixedHtml.resolveSibling("sa.csv")).equals(List.of("index,type,x,y,z",
                            "0,H,10,-2,4", "1,P,11,-2,4", "2,P,11,-1,4", "3,H,10,-1,4")),
                    "Export: CSV ruan tipet H/P dhe koordinatat larg origjines pa normalizim");
            check(!Files.exists(mixedHtml.resolveSibling("beam.csv"))
                            && exportedData(mixedHtml).contains("Kufiri kohor")
                            && !exportedData(mixedHtml).contains("\"name\":\"Beam Search\""),
                    "Export: kur Beam mungon, ruhet vetem SA dhe shenohet arsyeja e ndalimit");

            ArrayList<int[]> contacts = square.getHhContacts();
            contacts.get(0)[0] = 99;
            contacts.clear();
            check(square.getHhContacts().size() == 1 && square.getHhContacts().get(0)[0] == 0
                            && square.getHhContacts().get(0)[1] == 3 && square.calculateEnergy(2) == -2,
                    "Export: kontaktet vijne nga modeli dhe lista e kthyer nuk ndryshon gjendjen");
            Protein partial = new Protein("HHHH");
            partial.addPosition(new Position(0, 0, 0));
            Protein invalid = createProtein("HHHH", new Position[]{new Position(0, 0, 0), new Position(1, 0, 0),
                    new Position(0, 0, 0), new Position(0, 1, 0)});
            for (Protein unavailable : new Protein[]{partial, invalid}) {
                Path skipped = StructureExporter.export(temporary, square, unavailable, 2, StopReason.TIME_LIMIT);
                check(!Files.exists(skipped.resolveSibling("beam.csv"))
                                && exportedData(skipped).contains("nuk ka strukture te plote"),
                        "Export: rezultati jo i plote ose i pavlefshem nuk eksportohet si rezultat final");
            }
            Protein single = createProtein("H", new Position[]{new Position(0, 0, 0)});
            Path singleHtml = StructureExporter.export(temporary, single, null, 2, StopReason.TIME_LIMIT);
            check(exportedData(singleHtml).contains("\"bonds\":[],\"contacts\":[]")
                            && exportedData(singleHtml).contains("\"energy\":\"0\""),
                    "Export: nje pozicion i vetem ka zero lidhje, kontakte dhe energji");

            boolean invalidWeight = false;
            try {
                StructureExporter.export(temporary.resolve("bad-weight"), square, null, 0, StopReason.TIME_LIMIT);
            } catch (IllegalArgumentException e) {
                invalidWeight = true;
            }
            check(invalidWeight && !Files.exists(temporary.resolve("bad-weight")),
                    "Export: pesha e pavlefshme refuzohet para krijimit te skedareve");
            boolean noResult = false;
            try {
                StructureExporter.export(temporary.resolve("no-result"), partial, null, 2, StopReason.TIME_LIMIT);
            } catch (IllegalArgumentException e) {
                noResult = true;
            }
            check(noResult && !Files.exists(temporary.resolve("no-result")),
                    "Export: pa asnje rezultat te plote nuk krijohet dosje");
            boolean mismatch = false;
            try {
                StructureExporter.export(temporary.resolve("mismatch"), square, mixed, 2, StopReason.COMPLETED);
            } catch (IllegalArgumentException e) {
                mismatch = true;
            }
            check(mismatch && !Files.exists(temporary.resolve("mismatch")),
                    "Export: sekuencat e rezultateve duhet te perputhen");
            Path occupied = temporary.resolve("occupied");
            Files.writeString(occupied, "mos ndrysho");
            boolean ioFailure = false;
            try {
                StructureExporter.export(occupied, square, null, 2, StopReason.TIME_LIMIT);
            } catch (IOException e) {
                ioFailure = true;
            }
            check(ioFailure && Files.readString(occupied).equals("mos ndrysho"),
                    "Export: gabimi i shkrimit raportohet pa mbishkruar skedarin ekzistues");
        } catch (IOException e) {
            throw new AssertionError("Deshtim gjate testit te eksportit.", e);
        } finally {
            try (Stream<Path> files = Files.walk(temporary)) {
                for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(file);
                }
            } catch (IOException e) {
                throw new AssertionError("Nuk u pastrua dosja e perkohshme e testit.", e);
            }
        }
    }

    private static void testExportedExperimentReport() {
        Path temporary;
        try {
            temporary = Files.createTempDirectory("protein-report-export-tests-");
        } catch (IOException e) {
            throw new AssertionError("Nuk u krijua dosja e perkohshme e raportit.", e);
        }
        try {
            Path project = temporary.resolve("project");
            Path exports = project.resolve("exports");
            Path experiments = project.resolve("experiments");
            Protein square = createSquare("HHHH");
            Path withoutReport = StructureExporter.export(exports, square, square, 1, StopReason.COMPLETED);
            String fallback = Files.readString(withoutReport);
            check(fallback.contains("Ekzekuto ExperimentRunner dhe eksporto sërish pamjen 3D")
                            && fallback.contains("href=\"#experiment-statistics\"")
                            && fallback.contains("href=\"#view-3d\"")
                            && !fallback.contains("<iframe"),
                    "Report export: pa experiments ruhen navigimi dhe pamja 3D; seksioni shfaq udhezimin");
            check(!Files.exists(withoutReport.resolveSibling("report.html"))
                            && !Files.exists(withoutReport.resolveSibling("results.csv")),
                    "Report export: nuk krijohen raporte ose CSV boshe si zevendesim");

            Path missingCsv = experiments.resolve("run-missing-csv");
            Files.createDirectories(missingCsv);
            Files.writeString(missingCsv.resolve("report.html"), "<html>pa CSV</html>");
            Path incompleteOnly = StructureExporter.export(exports, square, square, 1, StopReason.COMPLETED);
            check(Files.readString(incompleteOnly).contains("Ekzekuto ExperimentRunner dhe eksporto sërish pamjen 3D"),
                    "Report export: vetem nje report.html pa results.csv nuk eshte raport i perfunduar");

            Path older = writeTestExperimentReport(experiments.resolve("run-z-old"),
                    ExperimentRunner.initialProtein("HHHH"), 1000);
            Path latest = writeTestExperimentReport(experiments.resolve("run-a-new"), square, 2000);
            String sourceReport = Files.readString(latest);
            String sourceCsv = Files.readString(latest.resolveSibling("results.csv"));
            // Datat kontrollohen nga testi, pa sleep dhe pa varesi nga shpejtesia e kompjuterit.
            Files.setLastModifiedTime(missingCsv.resolve("report.html"), FileTime.fromMillis(5000));
            Path missingReport = experiments.resolve("run-missing-report");
            Files.createDirectories(missingReport);
            Files.copy(latest.resolveSibling("results.csv"), missingReport.resolve("results.csv"));
            Path truncated = experiments.resolve("run-truncated");
            Files.createDirectories(truncated);
            Files.copy(latest.resolveSibling("results.csv"), truncated.resolve("results.csv"));
            Files.writeString(truncated.resolve("report.html"), "<html>ende duke u shkruar");
            Files.setLastModifiedTime(truncated.resolve("report.html"), FileTime.fromMillis(6000));
            Path emptyCsv = experiments.resolve("run-empty-csv");
            Files.createDirectories(emptyCsv);
            Files.writeString(emptyCsv.resolve("results.csv"), "");
            Files.writeString(emptyCsv.resolve("report.html"), "<html>CSV bosh</html>");
            Files.setLastModifiedTime(emptyCsv.resolve("report.html"), FileTime.fromMillis(7000));
            Path brokenUtf8 = experiments.resolve("run-broken-utf8");
            Files.createDirectories(brokenUtf8);
            Files.copy(latest.resolveSibling("results.csv"), brokenUtf8.resolve("results.csv"));
            Files.write(brokenUtf8.resolve("report.html"), new byte[]{(byte) 0xC3});
            Files.setLastModifiedTime(brokenUtf8.resolve("report.html"), FileTime.fromMillis(8000));

            Path html = StructureExporter.export(exports, square, square, 1, StopReason.COMPLETED);
            String page = Files.readString(html);
            check(Files.mismatch(latest, html.resolveSibling("report.html")) == -1
                            && !Files.readString(older).equals(Files.readString(html.resolveSibling("report.html"))),
                    "Report export: zgjidhet raporti i perfunduar sipas dates, jo emrit ose nje shkrimi te pjesshem");
            check(Files.mismatch(latest.resolveSibling("results.csv"), html.resolveSibling("results.csv")) == -1,
                    "Report export: results.csv kopjohet nga e njejta prove si report.html");
            check(page.contains("id=\"report-link\" class=\"report-button\" href=\"#experiment-statistics\"")
                            && page.contains("Rikthe pamjen</button><a")
                            && page.contains("id=\"back-to-3d\" class=\"report-button\" href=\"#view-3d\"")
                            && !page.contains("target=\"_blank\"")
                            && !page.contains("Ekzekuto ExperimentRunner dhe eksporto sërish pamjen 3D"),
                    "Report export: butoni prane reset dhe lidhja e kthimit perdorin ankorat e se njejtes faqe");
            check(page.contains("<iframe id=\"experiment-report\" src=\"report.html\"")
                            && page.contains("Statistikat e eksperimenteve")
                            && page.contains("disa prova të Simulated Annealing dhe Beam Search")
                            && page.indexOf("<section id=\"experiment-statistics\"") > page.indexOf("</main>"),
                    "Report export: raporti ekzistues integrohet poshte 3D-se dhe shpjegohet krahasimi i disa provave");
            check(!page.contains(temporary.toString()) && !page.contains("file:") && !page.contains("http:"),
                    "Report export: pamja nuk ka shtigje absolute ose varesi nga nje server");
            check(Files.readString(latest).equals(sourceReport)
                            && Files.readString(latest.resolveSibling("results.csv")).equals(sourceCsv),
                    "Report export: raporti dhe CSV-ja origjinale mbeten te pandryshuara");

            String proteinData = exportedData(html);
            Path relocatedParent = temporary.resolve("another-computer");
            Files.createDirectories(relocatedParent);
            Path relocated = Files.move(html.getParent(), relocatedParent.resolve("export"));
            Files.move(experiments, temporary.resolve("original-experiments-unavailable"));
            Path relocatedHtml = relocated.resolve("structure.html");
            java.net.URI down = relocatedHtml.toUri().resolve("#experiment-statistics");
            java.net.URI up = down.resolve("#view-3d");
            check(down.getPath().equals(relocatedHtml.toUri().getPath())
                            && up.getPath().equals(relocatedHtml.toUri().getPath())
                            && down.getFragment().equals("experiment-statistics") && up.getFragment().equals("view-3d")
                            && page.contains("id=\"experiment-statistics\"") && page.contains("id=\"view-3d\""),
                    "Report export: te dy drejtimet e navigimit mbeten brenda structure.html pas zhvendosjes");
            Path reportLink = Path.of(relocatedHtml.toUri().resolve("report.html"));
            Path csvLink = Path.of(reportLink.toUri().resolve("results.csv"));
            check(reportLink.equals(relocated.resolve("report.html")) && Files.readString(reportLink).equals(sourceReport),
                    "Report export: raporti i integruar gjendet pas zhvendosjes pa experiments origjinale");
            check(Files.readString(reportLink).contains("href=\"results.csv\"")
                            && csvLink.equals(relocated.resolve("results.csv")) && Files.readString(csvLink).equals(sourceCsv),
                    "Report export: lidhja e CSV-se ne raport vazhdon te funksionoje offline pas zhvendosjes");
            check(exportedData(relocatedHtml).equals(proteinData) && sameCoordinates(square, createSquare("HHHH"), 4),
                    "Report export: koordinatat dhe te dhenat 3D ruhen gjate eksportit dhe zhvendosjes");
        } catch (IOException e) {
            throw new AssertionError("Deshtim gjate testit te raportit ne eksport.", e);
        } finally {
            try (Stream<Path> files = Files.walk(temporary)) {
                for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(file);
                }
            } catch (IOException e) {
                throw new AssertionError("Nuk u pastrua dosja e perkohshme e raportit.", e);
            }
        }
    }

    private static Path writeTestExperimentReport(Path directory, Protein result, long finishedMillis) throws IOException {
        Files.createDirectories(directory);
        ExperimentRunner.Trial trial = new ExperimentRunner.Trial("baseline", "short", "HHHH", "SA", 0, 2025L, 0, false, 0);
        ExperimentRow row = ExperimentRow.fromAnnealing(trial, 1, 2, 0.995, 0,
                new AnnealingResult(result, 0, 0, 0));
        Path csv = directory.resolve("results.csv");
        ExperimentRow.writeCsv(csv, List.of(row));
        Path report = directory.resolve("report.html");
        ExperimentReport.write(csv, report);
        Files.setLastModifiedTime(report, FileTime.fromMillis(finishedMillis));
        return report;
    }

    private static String exportedData(Path html) throws IOException {
        String page = Files.readString(html);
        String marker = "<script id=\"protein-data\" type=\"application/json\">";
        int start = page.indexOf(marker) + marker.length();
        return page.substring(start, page.indexOf("</script>", start));
    }

    private static void testExperimentPlan() {
        List<ExperimentRunner.Trial> plan = ExperimentRunner.createPlan();
        check(plan.size() == 46 && plan.stream().noneMatch(t -> t.group().equals("warmup")),
                "Experiments: plani ka 46 prova dhe nuk perfshin ngrohjen");
        List<String> sequences = plan.stream().filter(t -> t.group().equals("baseline"))
                .map(ExperimentRunner.Trial::sequence).distinct().toList();
        check(sequences.size() == 3 && sequences.get(0).length() == 4
                        && sequences.get(1).length() == 12 && sequences.get(2).length() == 24,
                "Experiments: ruhen sekuenca fikse me gjatesi 4, 12 dhe 24");
        for (String sequence : sequences) {
            List<ExperimentRunner.Trial> baseline = plan.stream().filter(t -> t.group().equals("baseline")
                    && t.sequence().equals(sequence)).toList();
            List<ExperimentRunner.Trial> timed = plan.stream().filter(t -> t.group().equals("time_limited")
                    && t.sequence().equals(sequence)).toList();
            List<Long> seeds = baseline.stream().filter(t -> t.algorithm().equals("SA"))
                    .map(ExperimentRunner.Trial::seed).toList();
            check(baseline.size() == 7 && timed.size() == 7 && seeds.equals(List.of(2025L, 2026L, 2027L, 2028L, 2029L))
                            && timed.stream().filter(t -> t.algorithm().equals("SA")).map(ExperimentRunner.Trial::seed).toList().equals(seeds)
                            && baseline.stream().allMatch(t -> t.timeLimitMillis() == 0)
                            && timed.stream().allMatch(t -> t.timeLimitMillis() == 10)
                            && baseline.stream().filter(t -> t.algorithm().equals("Beam")).map(ExperimentRunner.Trial::beamWidth).toList().equals(List.of(10, 30)),
                    "Experiments: pese seed-e dhe dy gjeresi me kufi te njejte per n=" + sequence.length());
        }
        ExperimentRunner.Trial rotation = plan.stream().filter(t -> t.group().equals("rotation")).findFirst().orElseThrow();
        check(!rotation.removeRotatedCopies() && rotation.timeLimitMillis() == 0
                        && plan.stream().anyMatch(t -> t.group().equals("baseline") && t.algorithm().equals("Beam")
                        && t.sequence().equals(rotation.sequence()) && t.beamWidth() == rotation.beamWidth() && t.removeRotatedCopies()),
                "Experiments: krahasimi i rrotullimeve ka te njejten sekuence dhe gjeresi");
        List<ExperimentRunner.Trial> progress = plan.stream().filter(t -> t.group().equals("progress")).toList();
        check(progress.stream().map(ExperimentRunner.Trial::iterations).toList().equals(List.of(100, 500, 1000))
                        && progress.stream().map(ExperimentRunner.Trial::seed).distinct().count() == 1
                        && progress.stream().map(ExperimentRunner.Trial::sequence).distinct().count() == 1
                        && progress.stream().allMatch(t -> t.timeLimitMillis() == 0),
                "Experiments: progresi perdor te njejtin seed dhe sekuence ne 100, 500 dhe 1000 hapa");
        Protein initial = ExperimentRunner.initialProtein(sequences.get(2));
        boolean straight = initial.isComplete();
        for (int i = 0; i < sequences.get(2).length(); i++) {
            straight &= initial.getPosition(i).isSamePosition(new Position(i, 0, 0));
        }
        check(straight && initial.calculateEnergy() == 0,
                "Experiments: cdo prove SA nis nga e njejta vije me energji zero");
    }

    private static void testExperimentResults() {
        Path temporary;
        try {
            temporary = Files.createTempDirectory("protein-experiment-tests-");
        } catch (IOException e) {
            throw new AssertionError("Nuk u krijua dosja e perkohshme e eksperimenteve.", e);
        }
        try {
            BeamSearchResult hh = BeamSearch.searchWithStats("HH", 30, true);
            BeamSearchResult hhh = BeamSearch.searchWithStats("HHH", 2, false);
            check(hh.getMaxCandidatesBeforeSelection() == 6 && hh.getRotatedDuplicates() == 5
                            && hhh.getMaxCandidatesBeforeSelection() == 10,
                    "Experiments: maksimumi numeron kandidatet e te gjithe prinderve para heqjes se kopjeve");
            ExperimentRunner.Trial saTrial = new ExperimentRunner.Trial("baseline", "short", "HHHH", "SA", 1000, 2025L, 0, false, 0);
            ExperimentRunner.Trial beamTrial = new ExperimentRunner.Trial("baseline", "short", "HHHH", "Beam", 0, null, 30, true, 0);
            ExperimentRow realSa = ExperimentRunner.runTrial(saTrial);
            ExperimentRow realBeam = ExperimentRunner.runTrial(beamTrial);
            check(realSa.get("energy").equals("-1") && realBeam.get("energy").equals("-1")
                            && realSa.get("hh_contacts").equals("1") && realBeam.get("hh_contacts").equals("1")
                            && realSa.get("status").equals("COMPLETE") && realBeam.get("status").equals("COMPLETE"),
                    "Experiments: kerkimet reale per HHHH me peshe 1 japin nje kontakt dhe energji -1");
            check(realSa.get("seed").equals("2025") && realSa.get("attempts").equals("1000")
                            && realSa.get("max_candidates_before_selection").isEmpty()
                            && realBeam.get("seed").isEmpty() && realBeam.get("attempts").isEmpty()
                            && realBeam.get("max_candidates_before_selection").equals("10"),
                    "Experiments: ruhen parametrat dhe numeruesit; fushat e pazbatueshme jane bosh");
            for (ExperimentRow row : List.of(realSa, realBeam)) {
                check(!row.get("heap_used_before_mib").isEmpty() && !row.get("heap_used_after_mib").isEmpty()
                                && Double.parseDouble(row.get("heap_used_before_mib")) >= 0
                                && Double.parseDouble(row.get("heap_used_after_mib")) >= 0,
                        "Metrics: " + row.get("algorithm") + " ruan dy lexime reale te heap-it ne MiB pa prag performance");
                check(Long.parseLong(row.get("proposals")) == Long.parseLong(row.get("valid_proposals"))
                                + Long.parseLong(row.get("geometrically_invalid_proposals"))
                                + Long.parseLong(row.get("out_of_range_proposals"))
                                + (row.get("algorithm").equals("SA") ? Long.parseLong(row.get("unchanged_proposals"))
                                + Long.parseLong(row.get("no_move_proposals")) : 0),
                        "Metrics: " + row.get("algorithm") + " regjistron ndarjen e plote te propozimeve ne CSV");
            }
            BeamSearchResult missing = BeamSearch.searchWithStats("HHHH", 30, 1, true, 1,
                    new ControlledNanoClock(0, 1_000_000));
            ExperimentRunner.Trial timedTrial = new ExperimentRunner.Trial("time_limited", "short", "HHHH", "Beam", 0, null, 30, true, 1);
            ExperimentRow missingRow = ExperimentRow.fromBeam(timedTrial, 1, 2_000_000, missing);
            check(missing.getEliminatedCandidatesPercentage() == null
                            && missingRow.get("generated_candidates_eliminated_percent").equals("N/A")
                            && realSa.get("generated_candidates_eliminated_percent").isEmpty(),
                    "Metrics: emëruesi zero jep N/A; refuzimeve SA nuk u caktohet perqindja e Beam");
            check(missingRow.get("energy").isEmpty() && missingRow.get("hh_contacts").isEmpty()
                            && missingRow.get("status").equals("NO_COMPLETE_STRUCTURE")
                            && missingRow.get("stop_reason").equals("TIME_LIMIT")
                            && missing.getMaxCandidatesBeforeSelection() == 0,
                    "Experiments: mungesa e zgjidhjes ka energji bosh dhe arsye kohore pa varesi nga shpejtesia");
            BeamSearchResult partial = BeamSearch.searchWithStats("HHHH", 30, 1, true, 1,
                    new ControlledNanoClock(0, 0, 500_000, 1_000_000));
            check(partial.getBestProtein() == null && partial.getMaxCandidatesBeforeSelection() == 6
                            && partial.getCompletedLevels() == 2 && partial.getStopReason() == StopReason.TIME_LIMIT,
                    "Experiments: maksimumi i kandidateve ruhet edhe kur ndalimi vjen pas disa niveleve");

            ArrayList<ExperimentRow> rows = new ArrayList<>();
            // Kohe fikse vetem per testimin e CSV-se dhe medianes, jo matje te algoritmit.
            long[] elapsed = {5_000_000, 1_000_000, 3_000_000, 2_000_000, 4_000_000};
            for (int i = 0; i < 5; i++) {
                Protein result = i % 2 == 0 ? createSquare("HHHH") : ExperimentRunner.initialProtein("HHHH");
                ExperimentRunner.Trial trial = new ExperimentRunner.Trial("baseline", "short", "HHHH", "SA", 1000, 2025L + i, 0, false, 0);
                rows.add(ExperimentRow.fromAnnealing(trial, 1, 2, 0.995, elapsed[i],
                        new AnnealingResult(result, 1000, 100, 50, StopReason.ITERATION_LIMIT)));
            }
            rows.add(ExperimentRow.fromBeam(beamTrial, 1, 7_000_000, BeamSearch.searchWithStats("HHHH", 30, true)));
            rows.add(missingRow);
            long previous = 0;
            for (int attempts : new int[]{100, 500, 1000}) {
                ExperimentRunner.Trial trial = new ExperimentRunner.Trial("progress", "short", "HHHH", "SA", attempts, 2025L, 0, false, 0);
                ExperimentRow row = ExperimentRunner.runTrial(trial);
                long energy = Long.parseLong(row.get("energy"));
                check(energy <= previous && row.get("attempts").equals(Integer.toString(attempts)),
                        "Experiments: best nuk perkeqesohet per te njejtin seed deri ne " + attempts + " perpjekje");
                previous = energy;
                rows.add(row);
            }
            rows.add(ExperimentRunner.runTrial(new ExperimentRunner.Trial("rotation", "short", "HHHH", "Beam", 0, null, 30, false, 0)));
            ExperimentRunner.Trial metricTrial = new ExperimentRunner.Trial("metrics", "short", "HHHH", "SA", 1, 2025L, 0, false, 0);
            AnnealingResult rejectedCandidate = SimulatedAnnealing.optimizeWithStats(createSquare("HHHH"), 1, 2, 0.99,
                    new ControlledAnnealingRandom(new int[]{0, 2}, 0.7));
            ExperimentRow metricRow = ExperimentRow.fromAnnealing(metricTrial, 1, 2, 0.99, 0,
                    rejectedCandidate, 5L * 1_048_576, 3L * 1_048_576);
            rows.add(metricRow);
            check(metricRow.get("heap_used_before_mib").equals("5.000000")
                            && metricRow.get("heap_used_after_mib").equals("3.000000")
                            && metricRow.get("valid_proposals").equals("1")
                            && metricRow.get("temperature_rejected_candidates").equals("1")
                            && metricRow.get("geometrically_invalid_proposals").equals("0"),
                    "Metrics: CSV dallon refuzimin e vlefshem dhe ruan heap-in edhe kur leximi pas eshte me i ulet");
            Path csv = temporary.resolve("results.csv");
            ExperimentRow.writeCsv(csv, rows);
            String before = Files.readString(csv);
            List<ExperimentRow> read = ExperimentRow.readCsv(csv);
            boolean matches = read.size() == rows.size();
            for (int i = 0; i < rows.size(); i++) {
                for (String column : ExperimentRow.COLUMNS) {
                    matches &= rows.get(i).get(column).equals(read.get(i).get(column));
                }
            }
            check(matches && read.get(0).get("elapsed_ms").equals("5.000000")
                            && read.get(6).get("energy").isEmpty(),
                    "Experiments: CSV ruan sakte te gjitha fushat, kohen dhe energjine bosh gjate rileximit");
            check(ExperimentReport.median(List.of(-1.0, 0.0, -1.0, 0.0, -1.0)) == -1
                            && ExperimentReport.median(List.of(5.0, 1.0, 3.0, 2.0, 4.0)) == 3,
                    "Experiments: mediana e pese seed-eve llogaritet pas renditjes se vlerave");
            Path report = temporary.resolve("report.html");
            ExperimentReport.write(csv, report);
            String html = Files.readString(report);
            check(html.contains("data-value=\"-1.0\"") && html.contains("<td>-1</td><td>1</td>")
                            && html.contains("3.000 ms") && html.contains("<tr><td>SA</td><td>-1</td><td>-1</td>")
                            && Files.readString(csv).equals(before),
                    "Experiments: tabela dhe grafiket e raportit perdorin energjine, kontaktet dhe medianen nga CSV");
            check(html.contains("data-value=\"missing\"") && html.contains("Pa strukturë")
                            && html.contains("<td>NO_COMPLETE_STRUCTURE</td><td>TIME_LIMIT</td>"),
                    "Experiments: raporti dallon mungesen e struktures nga energjia zero");
            check(html.contains("<svg") && !html.contains("http:") && !html.contains("https:")
                            && !html.contains("src=") && html.contains("max_candidates_before_selection")
                            && html.contains("jo MB") && html.contains("minimum global"),
                    "Experiments: raporti eshte offline dhe shpjegon treguesin e memories e kufijte e perfundimeve");
            check(html.contains("Përqindja e kandidatëve të gjeneruar që u eliminuan")
                            && html.contains("<td>N/A</td>") && html.contains("Heap JVM para (MiB)")
                            && html.contains("<td>5.000000</td><td>3.000000</td>") && html.contains("memorien ekskluzive")
                            && html.contains("heap_used_after_mib"),
                    "Metrics: raporti shfaq N/A dhe njesite e kufizimet e matjes se perbashket te memories");
            Path malformed = temporary.resolve("malformed.csv");
            Files.writeString(malformed, "gabim\n");
            boolean rejected = false;
            try {
                ExperimentRow.readCsv(malformed);
            } catch (IOException e) {
                rejected = true;
            }
            check(rejected, "Experiments: CSV me kolona te gabuara nuk interpretohet ne heshtje");
        } catch (IOException e) {
            throw new AssertionError("Deshtim gjate testimit te eksperimenteve.", e);
        } finally {
            try (Stream<Path> files = Files.walk(temporary)) {
                for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(file);
                }
            } catch (IOException e) {
                throw new AssertionError("Nuk u pastrua dosja e perkohshme e eksperimenteve.", e);
            }
        }
    }

    private static void testProposalMetrics() {
        Protein bent = createProtein("HHHH", new Position[]{new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(1, 2, 0)});
        Protein snapshot = bent.copy();
        MoveProposal collision = bent.createRandomNeighborWithStatus(new ControlledAnnealingRandom(new int[]{0, 3}));
        MoveProposal unchanged = bent.createRandomNeighborWithStatus(new ControlledAnnealingRandom(new int[]{0, 2}));
        MoveProposal valid = bent.createRandomNeighborWithStatus(new ControlledAnnealingRandom(new int[]{0, 1}));
        check(collision.status() == MoveProposal.Status.COLLISION && collision.candidate() == null,
                "Metrics: endpoint-i mbi aminoacidin e dyte eshte perplasje");
        check(unchanged.status() == MoveProposal.Status.NO_CHANGE && unchanged.candidate() == null,
                "Metrics: endpoint-i ne vendin aktual eshte pa ndryshim, jo perplasje");
        check(valid.status() == MoveProposal.Status.VALID && valid.candidate().isComplete()
                        && sameCoordinates(bent, snapshot, 4),
                "Metrics: propozimi i vlefshem krijon kandidat dhe ruan origjinalin");
        Protein line = ExperimentRunner.initialProtein("HHHH");
        check(line.createRandomNeighborWithStatus(new ControlledAnnealingRandom(new int[]{1, 1, 2, 1})).status()
                        == MoveProposal.Status.COLLISION,
                "Metrics: perplasja e segmentit pivot dallohet");
        check(line.createRandomNeighborWithStatus(new ControlledAnnealingRandom(new int[]{1, 0, 0, 0})).status()
                        == MoveProposal.Status.NO_CHANGE,
                "Metrics: rrotullimi rreth boshtit te vijes eshte pa ndryshim");
        ControlledAnnealingRandom choices = new ControlledAnnealingRandom(new int[]{0, 3, 0, 2, 0, 1, 0, 2}, 0.7);
        AnnealingResult result = SimulatedAnnealing.optimizeWithStats(bent, 4, 2, 0.99, choices);
        check(result.getAttempts() == 4 && result.getValidCandidates() == 2 && result.getAcceptedMoves() == 1
                        && result.getCollisionProposals() == 1 && result.getGeometricallyInvalidProposals() == 1
                        && result.getUnchangedProposals() == 1 && result.getDisconnectedProposals() == 0
                        && result.getNoMoveProposals() == 0 && result.getOutOfRangeProposals() == 0,
                "Metrics: SA numeron perplasjen, pa ndryshim, pranimin dhe refuzimin nga temperatura veçmas");
        check(result.getBestProtein().calculateEnergy() == -1 && choices.intIndex == 8 && choices.doubleIndex == 1
                        && sameCoordinates(bent, snapshot, 4),
                "Metrics: numerimi nuk shton thirrje Random dhe ruan best-in e origjinalin");
        AnnealingResult rejected = SimulatedAnnealing.optimizeWithStats(createSquare("HHHH"), 1, 2, 0.99,
                new ControlledAnnealingRandom(new int[]{0, 2}, 0.7));
        check(rejected.getValidCandidates() == 1 && rejected.getAcceptedMoves() == 0
                        && rejected.getGeometricallyInvalidProposals() == 0 && rejected.getUnchangedProposals() == 0,
                "Metrics: nje kandidat i refuzuar nga temperatura mbetet gjeometrikisht i vlefshem");
        AnnealingResult single = SimulatedAnnealing.optimizeWithStats(ExperimentRunner.initialProtein("H"),
                3, 2, 0.99, new ControlledAnnealingRandom(new int[0]));
        check(single.getAttempts() == 3 && single.getNoMoveProposals() == 3
                        && single.getGeometricallyInvalidProposals() == 0 && single.getUnchangedProposals() == 0,
                "Metrics: proteina me nje pozicion nuk ka levizje, pa perplasje ose rastesi");
        Protein boundary = createProtein("HH", new Position[]{new Position(Integer.MAX_VALUE, 0, 0),
                new Position(Integer.MAX_VALUE, 1, 0)});
        AnnealingResult outside = SimulatedAnnealing.optimizeWithStats(boundary, 1, 2, 0.99,
                new ControlledAnnealingRandom(new int[]{0, 0}));
        check(outside.getOutOfRangeProposals() == 1 && outside.getCollisionProposals() == 0
                        && outside.getGeometricallyInvalidProposals() == 0,
                "Metrics: tejkalimi int raportohet veç nga perplasja gjeometrike");
        AnnealingResult stopped = SimulatedAnnealing.optimizeWithStats(bent, 4, 2, 0.99,
                new ControlledAnnealingRandom(new int[]{0, 3, 0, 2}), 1, 1,
                new ControlledNanoClock(0, 0, 0, 1_000_000));
        check(stopped.getAttempts() == 2 && stopped.getCollisionProposals() == 1
                        && stopped.getUnchangedProposals() == 1 && stopped.getStopReason() == StopReason.TIME_LIMIT,
                "Metrics: SA ruan ndarjen e propozimeve edhe ne ndalim kohor");

        ExtensionBatch first = new Protein("HH").createExtensionsWithStats();
        check(first.proposals() == 1 && first.candidates().size() == 1 && first.collisionProposals() == 0,
                "Metrics: origjina e Beam numeron nje propozim te vlefshem");
        Protein edge = new Protein("HH");
        edge.addPosition(new Position(Integer.MAX_VALUE, 0, 0));
        ExtensionBatch edgeBatch = edge.createExtensionsWithStats();
        check(edgeBatch.proposals() == 6 && edgeBatch.candidates().size() == 5
                        && edgeBatch.outOfRangeProposals() == 1 && edgeBatch.collisionProposals() == 0,
                "Metrics: gjashte drejtimet numerohen edhe kur njeri tejkalon int");
        BeamSearchResult hhh = BeamSearch.searchWithStats("HHH", 2, false);
        check(hhh.getExtensionProposals() == 19 && hhh.getGeneratedCandidates() == 17
                        && hhh.getCollisionProposals() == 2 && hhh.getGeometricallyInvalidProposals() == 2
                        && hhh.getOutOfRangeProposals() == 0,
                "Metrics: Beam numeron kthimet mbi prinderit si perplasje gjate gjenerimit");
        check(hhh.getGeneratedCandidates() == hhh.getRetainedCandidates() + hhh.getRotatedDuplicates()
                        + hhh.getPrunedCandidates(),
                "Metrics: largimet nga gjeresia mbeten propozime te vlefshme");
        BeamSearchResult hh = BeamSearch.searchWithStats("HH", 30, true);
        check(hh.getGeneratedCandidates() == 7 && hh.getRotatedDuplicates() == 5
                        && Math.abs(hh.getEliminatedCandidatesPercentage() - 100.0 * 5 / 7) < 1e-10,
                "Metrics: perqindja totale HH perfshin origjinen, jo vetem gjashte drejtimet");
        BeamSearchResult timed = BeamSearch.searchWithStats("HHHH", 2, 1, false, 1,
                new ControlledNanoClock(0, 0, 0, 0, 1_000_000));
        check(timed.getExtensionProposals() == hhh.getExtensionProposals()
                        && timed.getCollisionProposals() == hhh.getCollisionProposals()
                        && timed.getBestProtein() == null && timed.getStopReason() == StopReason.TIME_LIMIT,
                "Metrics: Beam numeron nivelin e perfunduar para kontrollit pasues te kohes");
        check(HeapUsage.toMiB(1_048_576) == 1.0 && HeapUsage.toMiB(524_288) == 0.5,
                "Metrics: MiB konvertohet nga byte me 1024 ne katror");
    }

    private static class ControlledNanoClock implements LongSupplier {
        private final long[] values;
        private int index;

        ControlledNanoClock(long... values) {
            this.values = values;
        }

        @Override
        public long getAsLong() {
            if (index >= values.length) {
                throw new AssertionError("Lexim shtese i kohes gjate testit.");
            }
            return values[index++];
        }
    }

    private static void checkCoordinates(Protein protein, Position[] expectedPositions, String description) {
        for (int i = 0; i < expectedPositions.length; i++) {
            if (!protein.getPosition(i).isSamePosition(expectedPositions[i])) {
                throw new AssertionError(description + ": indeksi " + i);
            }
        }
        System.out.println("OK: " + description);
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        System.out.println("OK: " + description);
    }

    private static void demonstratePivotRotations() {
        Position[] straight = {
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(2, 0, 0), new Position(3, 0, 0)
        };
        Position[] bent = {
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 1, 0), new Position(1, 2, 0)
        };

        Protein aroundZ = createProtein("HHHH", straight);
        check(aroundZ.tryPivotRotation(1, 'Z', 1), "Pivot A: pranohet Z 90 grade");
        checkCoordinates(aroundZ, bent, "Pivot A: koordinatat rreth Z");
        check(aroundZ.isValid() && aroundZ.isComplete() && aroundZ.calculateEnergy() == 0,
                "Pivot A: struktura e plote dhe e vlefshme, energjia 0");

        Protein collision = createProtein("HHHH", straight);
        check(!collision.tryPivotRotation(1, 'Z', 2), "Pivot B: refuzohet mbivendosja");
        checkCoordinates(collision, straight, "Pivot B: refuzimi ruan te gjitha koordinatat");
        check(collision.isComplete() && collision.calculateEnergy() == 0, "Pivot B: ruhen vlefshmeria dhe energjia");

        Protein aroundY = createProtein("HHHH", straight);
        check(aroundY.tryPivotRotation(1, 'Y', 1), "Pivot C: pranohet Y 90 grade");
        checkCoordinates(aroundY, new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 0, -1), new Position(1, 0, -2)
        }, "Pivot C: koordinatat perfshijne z negativ");
        check(aroundY.isComplete() && aroundY.calculateEnergy() == 0, "Pivot C: vlefshmeria dhe energjia");

        Protein aroundX = createProtein("HHHH", bent);
        check(aroundX.tryPivotRotation(1, 'X', 1), "Pivot D: pranohet X 90 grade");
        checkCoordinates(aroundX, new Position[]{
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(1, 0, 1), new Position(1, 0, 2)
        }, "Pivot D: koordinatat perfshijne z pozitiv");
        check(aroundX.isComplete() && aroundX.calculateEnergy() == 0, "Pivot D: vlefshmeria dhe energjia");

        Protein inverse = createProtein("HHHH", straight);
        check(inverse.tryPivotRotation(1, 'Z', 1), "Pivot E: rrotullimi fillestar");
        check(inverse.tryPivotRotation(1, 'Z', 3), "Pivot E: rrotullimi i kundert me 270 grade");
        checkCoordinates(inverse, straight, "Pivot E: rikthim i sakte ne koordinatat fillestare");

        for (int turns = 1; turns <= 3; turns++) {
            Protein unchanged = createProtein("HHHH", straight);
            check(!unchanged.tryPivotRotation(1, 'X', turns), "Pivot F: pa ndryshim rreth X, quarterTurns=" + turns);
            checkCoordinates(unchanged, straight, "Pivot F: ruhen koordinatat");
            check(unchanged.isComplete() && unchanged.calculateEnergy() == 0, "Pivot F: ruhen vlefshmeria dhe energjia");
        }

        // Kolonat jane pivotIndex, axis (si karakter), quarterTurns.
        int[][] invalidParameters = {
                {-1, 'Z', 1}, {4, 'Z', 1}, {1, 'x', 1}, {1, 'A', 1},
                {1, 'Z', 0}, {1, 'Z', -1}, {1, 'Z', 4}
        };
        for (int[] parameters : invalidParameters) {
            Protein invalidArgument = createProtein("HHHH", straight);
            boolean rejected = false;
            try {
                invalidArgument.tryPivotRotation(parameters[0], (char) parameters[1], parameters[2]);
            } catch (IllegalArgumentException e) {
                rejected = true;
            }
            check(rejected, "Pivot G: refuzohen parametrat (" + parameters[0]
                    + "," + (char) parameters[1] + "," + parameters[2] + ")");
            checkCoordinates(invalidArgument, straight, "Pivot G: parametrat e gabuar nuk ndryshojne koordinatat");
        }

        Position[] partialPositions = {new Position(0, 0, 0), new Position(1, 0, 0)};
        Protein partial = createProtein("HHHH", partialPositions);
        boolean partialRejected = false;
        try {
            partial.tryPivotRotation(0, 'Z', 1);
        } catch (IllegalStateException e) {
            partialRejected = true;
        }
        check(partialRejected, "Pivot G: refuzohet struktura e paplote");
        checkCoordinates(partial, partialPositions, "Pivot G: prefiksi mbetet i pandryshuar");
        check(partial.isValid() && !partial.isComplete() && partial.calculateEnergy() == 0,
                "Pivot G: gjendja e pjesshme ruhet");

        Position[] disconnectedPositions = {
                new Position(0, 0, 0), new Position(1, 0, 0),
                new Position(2, 0, 0), new Position(4, 0, 0)
        };
        Protein invalidStructure = createProtein("HHHH", disconnectedPositions);
        boolean invalidRejected = false;
        try {
            invalidStructure.tryPivotRotation(1, 'Z', 1);
        } catch (IllegalStateException e) {
            invalidRejected = true;
        }
        check(invalidRejected, "Pivot G: refuzohet struktura e plote por e pavlefshme");
        checkCoordinates(invalidStructure, disconnectedPositions, "Pivot G: struktura e pavlefshme nuk ndryshohet");

        Protein lastPivot = createProtein("HHHH", straight);
        check(!lastPivot.tryPivotRotation(3, 'Z', 1), "Pivot G: pivot-i i fundit nuk ka segment");
        checkCoordinates(lastPivot, straight, "Pivot G: pivot-i i fundit nuk ndryshon strukturen");

        Protein single = createProtein("H", new Position[]{new Position(0, 0, 0)});
        check(!single.tryPivotRotation(0, 'Y', 1), "Pivot G: nje aminoacid i vetem nuk rrotullohet");
        check(single.isComplete() && single.calculateEnergy() == 0, "Pivot G: proteina me nje aminoacid ruhet");

        Protein original = createProtein("HHHH", straight);
        Protein rotatedCopy = original.copy();
        check(rotatedCopy.tryPivotRotation(1, 'Z', 1), "Pivot: rrotullimi i kopjes pranohet");
        checkCoordinates(rotatedCopy, bent, "Pivot: kopja ka koordinatat e rrotulluara");
        checkCoordinates(original, straight, "Pivot: origjinali nuk ndikohet nga kopja");
        check(original.isComplete() && original.calculateEnergy() == 0, "Pivot: energjia e origjinalit ruhet");

        Protein halfTurn = createProtein("HHHH", straight);
        check(halfTurn.tryPivotRotation(0, 'Z', 2), "Pivot: 180 grade lejohet kur nuk ka perplasje");
        checkCoordinates(halfTurn, new Position[]{
                new Position(0, 0, 0), new Position(-1, 0, 0),
                new Position(-2, 0, 0), new Position(-3, 0, 0)
        }, "Pivot: koordinatat e rrotullimit 180 grade");

        Protein translated = createProtein("HHHH", new Position[]{
                new Position(3, -2, 3), new Position(4, -2, 3),
                new Position(5, -2, 3), new Position(6, -2, 3)
        });
        check(translated.tryPivotRotation(1, 'Z', 1), "Pivot: pranohet pivot larg origjines");
        checkCoordinates(translated, new Position[]{
                new Position(3, -2, 3), new Position(4, -2, 3),
                new Position(4, -1, 3), new Position(4, 0, 3)
        }, "Pivot: perdoren koordinatat relative ndaj pivot-it");

        Protein energyChange = createProtein("HHHH", bent);
        check(energyChange.tryPivotRotation(2, 'Z', 1) && energyChange.calculateEnergy() == -1,
                "Pivot: pranohet rrotullimi qe ul energjine");
        check(energyChange.tryPivotRotation(2, 'Z', 3) && energyChange.calculateEnergy() == 0,
                "Pivot: pranohet edhe rrotullimi qe e rrit energjine");
        checkCoordinates(energyChange, bent, "Pivot: kthimi pas ndryshimit te energjise");

        Protein mixedSequence = createProtein("HPPP", bent);
        check(mixedSequence.tryPivotRotation(2, 'Z', 1) && mixedSequence.calculateEnergy() == 0,
                "Pivot: ruhet sekuenca HPPP dhe numri i pozicioneve");
        check(mixedSequence.isComplete(), "Pivot: sekuenca mbetet e plotesuar");

        Position[] nearLimit = {
                new Position(Integer.MAX_VALUE, 0, 0), new Position(Integer.MAX_VALUE - 1, 0, 0)
        };
        Protein overflow = createProtein("HH", nearLimit);
        check(!overflow.tryPivotRotation(0, 'Z', 2), "Pivot: refuzohet koordinata qe del jashte int");
        checkCoordinates(overflow, nearLimit, "Pivot: refuzimi i overflow nuk ndryshon origjinalin");
        check(overflow.isComplete() && overflow.calculateEnergy() == 0, "Pivot: gjendja ruhet pas overflow");

        System.out.println("Kontrollet e pivot rotation perfunduan me sukses.");
    }

    private static Protein createProtein(String hpSequence, Position[] coordinates) {
        Protein protein = new Protein(hpSequence);
        for (Position position : coordinates) {
            protein.addPosition(new Position(position.x, position.y, position.z));
        }
        return protein;
    }

    private static Protein createSquare(String hpSequence) {
        Protein protein = new Protein(hpSequence);
        protein.addPosition(new Position(0, 0, 0));
        protein.addPosition(new Position(1, 0, 0));
        protein.addPosition(new Position(1, 1, 0));
        protein.addPosition(new Position(0, 1, 0));
        return protein;
    }

    private static void showProtein(String description, Protein protein) {
        System.out.println(description + ": isValid=" + protein.isValid()
                + ", isComplete=" + protein.isComplete());
        try {
            System.out.println("Energjia: " + protein.calculateEnergy());
        } catch (IllegalStateException e) {
            System.out.println("Energjia u refuzua: " + e.getMessage());
        }
    }
}
