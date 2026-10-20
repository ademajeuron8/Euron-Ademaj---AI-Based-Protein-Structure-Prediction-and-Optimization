class Position {
    int x;
    int y;
    int z;

    Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    boolean isNeighbor(Position other) {
        int distance = Math.abs(x - other.x)
                     + Math.abs(y - other.y)
                     + Math.abs(z - other.z);

        return distance == 1;
    }

    boolean isSamePosition(Position other) {
        return x == other.x && y == other.y && z == other.z;
    }
}

public class Main {
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
