import java.util.ArrayList;

public class Protein {
    private final String hpSequence;
    private ArrayList<Position> positions = new ArrayList<>();

    public Protein(String hpSequence) {
        if (hpSequence == null || hpSequence.isEmpty()) {
            throw new IllegalArgumentException("Sekuenca H/P nuk mund te jete null ose bosh.");
        }

        for (int i = 0; i < hpSequence.length(); i++) {
            char category = hpSequence.charAt(i);
            if (category != 'H' && category != 'P') {
                throw new IllegalArgumentException("Sekuenca H/P lejon vetem shkronjat e medha H dhe P.");
            }
        }

        this.hpSequence = hpSequence;
    }

    public void addPosition(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("Pozicioni nuk mund te jete null.");
        }

        if (positions.size() >= hpSequence.length()) {
            throw new IllegalStateException("Numri i pozicioneve nuk mund te tejkaloje gjatesine e sekuences H/P.");
        }

        positions.add(position);
    }

    public Position getPosition(int index) {
        Position position = positions.get(index);
        return new Position(position.x, position.y, position.z);
    }

    public Protein copy() {
        Protein copiedProtein = new Protein(hpSequence);
        for (Position position : positions) {
            copiedProtein.addPosition(new Position(position.x, position.y, position.z));
        }
        return copiedProtein;
    }

    public boolean tryMoveLast(Position newPosition) {
        if (newPosition == null) {
            throw new IllegalArgumentException("Pozicioni i ri nuk mund te jete null.");
        }

        if (!isComplete()) {
            throw new IllegalStateException("Levizja kerkon nje proteine te plote dhe te vlefshme.");
        }

        if (positions.size() < 2) {
            return false;
        }

        int lastIndex = positions.size() - 1;
        if (positions.get(lastIndex).isSamePosition(newPosition)) {
            return false;
        }

        if (!positions.get(lastIndex - 1).isNeighbor(newPosition)) {
            return false;
        }

        for (int i = 0; i < lastIndex; i++) {
            if (positions.get(i).isSamePosition(newPosition)) {
                return false;
            }
        }

        // Zevendeso pozicionin vetem pasi te kalojne te gjitha kontrollet.
        positions.set(lastIndex, new Position(newPosition.x, newPosition.y, newPosition.z));
        return true;
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

    public boolean isComplete() {
        return isValid() && positions.size() == hpSequence.length();
    }

    public int calculateEnergy() {
        if (!isValid()) {
            throw new IllegalStateException("Energjia nuk mund te llogaritet per nje strukture te pavlefshme.");
        }

        int energy = 0;

        // Numero vetem kontaktet H-H qe nuk jane lidhje te drejtperdrejta.
        for (int i = 0; i < positions.size(); i++) {
            for (int j = i + 2; j < positions.size(); j++) {
                if (hpSequence.charAt(i) == 'H'
                        && hpSequence.charAt(j) == 'H'
                        && positions.get(i).isNeighbor(positions.get(j))) {
                    energy--;
                }
            }
        }

        return energy;
    }
}
