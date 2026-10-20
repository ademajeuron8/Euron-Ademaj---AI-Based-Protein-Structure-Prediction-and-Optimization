import java.util.ArrayList;

public class Protein {
    private ArrayList<Position> positions = new ArrayList<>();

    public void addPosition(Position position) {
        positions.add(position);
    }

    public boolean isValid() {
        if (positions.isEmpty()) {
            return false;
        }

        // Kontrollo secilin cift pozicionesh vetem nje here.
        for (int i = 0; i < positions.size(); i++) {
            for (int j = i + 1; j < positions.size(); j++) {
                if (positions.get(i).isSamePosition(positions.get(j))) {
                    return false;
                }
            }
        }

        // Kontrollo lidhjet mes pozicioneve te njepasnjeshme.
        for (int i = 0; i < positions.size() - 1; i++) {
            if (!positions.get(i).isNeighbor(positions.get(i + 1))) {
                return false;
            }
        }

        return true;
    }
}
