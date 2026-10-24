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
