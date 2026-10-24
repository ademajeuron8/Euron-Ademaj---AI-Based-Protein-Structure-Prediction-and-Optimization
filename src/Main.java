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
        Protein protein = new Protein("HHHH");
        for (int i = 0; i < 4; i++) {
            protein.addPosition(new Position(i, 0, 0));
        }

        System.out.println("Rrotullimi rreth Z u pranua: "
                + protein.tryPivotRotation(1, 'Z', 1));
        for (int i = 0; i < 4; i++) {
            Position position = protein.getPosition(i);
            System.out.println("Pozicioni " + i + ": (" + position.x
                    + "," + position.y + "," + position.z + ")");
        }
        System.out.println("Struktura e plote dhe e vlefshme: " + protein.isComplete());
        System.out.println("Energjia: " + protein.calculateEnergy());
    }
}
