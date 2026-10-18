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
}

public class Main {
    public static void main(String[] args) {
        Position h1 = new Position(0, 0, 0);
        Position h2 = new Position(1, 0, 0);
        Position h3 = new Position(1, 1, 0);

        System.out.println("H1 dhe H2 jane fqinje: "
                + h1.isNeighbor(h2));

        System.out.println("H1 dhe H3 jane fqinje: "
                + h1.isNeighbor(h3));
    }
}
