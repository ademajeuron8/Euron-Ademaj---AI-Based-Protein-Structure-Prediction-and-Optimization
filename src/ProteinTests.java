import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
