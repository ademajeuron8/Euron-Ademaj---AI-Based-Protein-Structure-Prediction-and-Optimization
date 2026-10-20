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
        Protein validProtein = new Protein();
        validProtein.addPosition(new Position(0, 0, 0));
        validProtein.addPosition(new Position(1, 0, 0));
        validProtein.addPosition(new Position(1, 1, 0));
        validProtein.addPosition(new Position(0, 1, 0));
        System.out.println("Struktura e vlefshme: " + validProtein.isValid());

        Protein overlappingProtein = new Protein();
        overlappingProtein.addPosition(new Position(0, 0, 0));
        overlappingProtein.addPosition(new Position(1, 0, 0));
        overlappingProtein.addPosition(new Position(0, 0, 0));
        System.out.println("Struktura me mbivendosje: " + overlappingProtein.isValid());

        Protein disconnectedProtein = new Protein();
        disconnectedProtein.addPosition(new Position(0, 0, 0));
        disconnectedProtein.addPosition(new Position(2, 0, 0));
        System.out.println("Struktura me lidhje te shkeputur: " + disconnectedProtein.isValid());

        Protein emptyProtein = new Protein();
        System.out.println("Struktura bosh: " + emptyProtein.isValid());

        Protein singlePositionProtein = new Protein();
        singlePositionProtein.addPosition(new Position(0, 0, 0));
        System.out.println("Struktura me nje pozicion: " + singlePositionProtein.isValid());
    }
}
