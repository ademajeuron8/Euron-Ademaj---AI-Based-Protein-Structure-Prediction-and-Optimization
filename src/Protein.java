import java.util.ArrayList;
import java.util.Random;

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

        positions.add(new Position(position.x, position.y, position.z));
    }

    public Position getPosition(int index) {
        Position position = positions.get(index);
        return new Position(position.x, position.y, position.z);
    }

    public String getHpSequence() {
        return hpSequence;
    }

    public Protein copy() {
        Protein copiedProtein = new Protein(hpSequence);
        for (Position position : positions) {
            copiedProtein.addPosition(new Position(position.x, position.y, position.z));
        }
        return copiedProtein;
    }

    public boolean tryMoveLast(Position newPosition) {
        return moveLastStatus(newPosition) == MoveProposal.Status.VALID;
    }

    private MoveProposal.Status moveLastStatus(Position newPosition) {
        if (newPosition == null) {
            throw new IllegalArgumentException("Pozicioni i ri nuk mund te jete null.");
        }

        if (!isComplete()) {
            throw new IllegalStateException("Levizja kerkon nje proteine te plote dhe te vlefshme.");
        }

        if (positions.size() < 2) {
            return MoveProposal.Status.NO_MOVE;
        }

        int lastIndex = positions.size() - 1;
        if (positions.get(lastIndex).isSamePosition(newPosition)) {
            return MoveProposal.Status.NO_CHANGE;
        }

        if (!positions.get(lastIndex - 1).isNeighbor(newPosition)) {
            return MoveProposal.Status.DISCONNECTED;
        }

        for (int i = 0; i < lastIndex; i++) {
            if (positions.get(i).isSamePosition(newPosition)) {
                return MoveProposal.Status.COLLISION;
            }
        }

        // Zevendeso pozicionin vetem pasi te kalojne te gjitha kontrollet.
        positions.set(lastIndex, new Position(newPosition.x, newPosition.y, newPosition.z));
        return MoveProposal.Status.VALID;
    }

    public boolean tryPivotRotation(int pivotIndex, char axis, int quarterTurns) {
        return pivotRotationStatus(pivotIndex, axis, quarterTurns) == MoveProposal.Status.VALID;
    }

    private MoveProposal.Status pivotRotationStatus(int pivotIndex, char axis, int quarterTurns) {
        if (pivotIndex < 0 || pivotIndex >= positions.size()) {
            throw new IllegalArgumentException("Indeksi i pivot-it eshte jashte listes.");
        }
        if (axis != 'X' && axis != 'Y' && axis != 'Z') {
            throw new IllegalArgumentException("Boshti duhet te jete X, Y ose Z.");
        }
        if (quarterTurns < 1 || quarterTurns > 3) {
            throw new IllegalArgumentException("Numri i rrotullimeve 90 grade duhet te jete 1, 2 ose 3.");
        }
        if (!isComplete()) {
            throw new IllegalStateException("Rrotullimi kerkon nje proteine te plote dhe te vlefshme.");
        }
        if (pivotIndex == positions.size() - 1) {
            return MoveProposal.Status.NO_MOVE;
        }

        Protein candidate = copy();
        Position pivot = positions.get(pivotIndex);
        boolean changed = false;

        for (int i = pivotIndex + 1; i < positions.size(); i++) {
            Position position = positions.get(i);
            // long shmang overflow gjate zbritjes dhe rrotullimit.
            long dx = (long) position.x - pivot.x;
            long dy = (long) position.y - pivot.y;
            long dz = (long) position.z - pivot.z;

            for (int turn = 0; turn < quarterTurns; turn++) {
                long oldDx = dx;
                long oldDy = dy;
                long oldDz = dz;

                if (axis == 'X') {
                    dy = -oldDz;
                    dz = oldDy;
                } else if (axis == 'Y') {
                    dx = oldDz;
                    dz = -oldDx;
                } else {
                    dx = -oldDy;
                    dy = oldDx;
                }
            }

            long newX = pivot.x + dx;
            long newY = pivot.y + dy;
            long newZ = pivot.z + dz;
            if (newX < Integer.MIN_VALUE || newX > Integer.MAX_VALUE
                    || newY < Integer.MIN_VALUE || newY > Integer.MAX_VALUE
                    || newZ < Integer.MIN_VALUE || newZ > Integer.MAX_VALUE) {
                return MoveProposal.Status.OUT_OF_RANGE;
            }

            Position rotated = new Position((int) newX, (int) newY, (int) newZ);
            candidate.positions.set(i, rotated);
            if (!position.isSamePosition(rotated)) {
                changed = true;
            }
        }

        if (!changed) {
            return MoveProposal.Status.NO_CHANGE;
        }
        // Rrotullimi rigid ruan lidhjet; pa overflow, pavlefshmeria ketu eshte mbivendosje.
        if (!candidate.isValid()) {
            return MoveProposal.Status.COLLISION;
        }

        // Origjinali ndryshohet vetem pas validimit; prefiksi mbetet i paprekur.
        for (int i = pivotIndex + 1; i < positions.size(); i++) {
            positions.set(i, candidate.positions.get(i));
        }
        return MoveProposal.Status.VALID;
    }

    public Protein createRandomNeighbor(Random random) {
        return createRandomNeighborWithStatus(random).candidate();
    }

    MoveProposal createRandomNeighborWithStatus(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("Random nuk mund te jete null.");
        }
        if (!isComplete()) {
            throw new IllegalStateException("Kandidati kerkon nje proteine te plote dhe te vlefshme.");
        }
        if (positions.size() < 2) {
            return new MoveProposal(null, MoveProposal.Status.NO_MOVE);
        }

        Protein candidate = copy();
        // Zgjidh vetem nje lloj levizjeje dhe bej vetem nje perpjekje.
        if (random.nextInt(2) == 0) {
            Position previous = positions.get(positions.size() - 2);
            long x = previous.x;
            long y = previous.y;
            long z = previous.z;
            int direction = random.nextInt(6);

            if (direction == 0) {
                x++;
            } else if (direction == 1) {
                x--;
            } else if (direction == 2) {
                y++;
            } else if (direction == 3) {
                y--;
            } else if (direction == 4) {
                z++;
            } else {
                z--;
            }

            if (x < Integer.MIN_VALUE || x > Integer.MAX_VALUE
                    || y < Integer.MIN_VALUE || y > Integer.MAX_VALUE
                    || z < Integer.MIN_VALUE || z > Integer.MAX_VALUE) {
                return new MoveProposal(null, MoveProposal.Status.OUT_OF_RANGE);
            }
            MoveProposal.Status status = candidate.moveLastStatus(new Position((int) x, (int) y, (int) z));
            return new MoveProposal(status == MoveProposal.Status.VALID ? candidate : null, status);
        } else {
            int pivotIndex = random.nextInt(positions.size() - 1);
            char[] axes = {'X', 'Y', 'Z'};
            char axis = axes[random.nextInt(axes.length)];
            int quarterTurns = random.nextInt(3) + 1;
            MoveProposal.Status status = candidate.pivotRotationStatus(pivotIndex, axis, quarterTurns);
            return new MoveProposal(status == MoveProposal.Status.VALID ? candidate : null, status);
        }
    }

    public ArrayList<Protein> createExtensions() {
        return createExtensionsWithStats().candidates();
    }

    ExtensionBatch createExtensionsWithStats() {
        ArrayList<Protein> extensions = new ArrayList<>();
        if (positions.isEmpty()) {
            Protein first = copy();
            first.addPosition(new Position(0, 0, 0));
            extensions.add(first);
            return new ExtensionBatch(extensions, 1, 0, 0);
        }
        if (!isValid()) {
            throw new IllegalStateException("Zgjatja kerkon nje strukture te vlefshme.");
        }
        if (positions.size() == hpSequence.length()) {
            return new ExtensionBatch(extensions, 0, 0, 0);
        }

        Position last = positions.get(positions.size() - 1);
        int[][] directions = {
                {1, 0, 0}, {-1, 0, 0}, {0, 1, 0},
                {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
        };
        int proposals = 0;
        int collisions = 0;
        int outOfRange = 0;
        for (int[] direction : directions) {
            proposals++;
            long x = (long) last.x + direction[0];
            long y = (long) last.y + direction[1];
            long z = (long) last.z + direction[2];
            if (x < Integer.MIN_VALUE || x > Integer.MAX_VALUE
                    || y < Integer.MIN_VALUE || y > Integer.MAX_VALUE
                    || z < Integer.MIN_VALUE || z > Integer.MAX_VALUE) {
                outOfRange++;
                continue;
            }

            Position next = new Position((int) x, (int) y, (int) z);
            boolean occupied = false;
            for (Position position : positions) {
                if (position.isSamePosition(next)) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) {
                Protein extension = copy();
                extension.addPosition(next);
                extensions.add(extension);
            } else {
                collisions++;
            }
        }
        return new ExtensionBatch(extensions, proposals, collisions, outOfRange);
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
        return Math.toIntExact(calculateEnergy(1));
    }

    public long calculateEnergy(int contactWeight) {
        if (contactWeight <= 0) {
            throw new IllegalArgumentException("Pesha e kontaktit H-H duhet te jete pozitive.");
        }
        if (!isValid()) {
            throw new IllegalStateException("Energjia nuk mund te llogaritet per nje strukture te pavlefshme.");
        }

        long contacts = 0;

        // Numero vetem kontaktet H-H qe nuk jane lidhje te drejtperdrejta.
        for (int i = 0; i < positions.size(); i++) {
            for (int j = i + 2; j < positions.size(); j++) {
                if (isHhContact(i, j)) {
                    contacts++;
                }
            }
        }

        return -((long) contactWeight) * contacts;
    }

    // Kthe indekset e kontakteve, pa ekspozuar listen e brendshme te pozicioneve.
    public ArrayList<int[]> getHhContacts() {
        if (!isValid()) {
            throw new IllegalStateException("Kontaktet kerkojne nje strukture te vlefshme.");
        }
        ArrayList<int[]> contacts = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            for (int j = i + 2; j < positions.size(); j++) {
                if (isHhContact(i, j)) {
                    contacts.add(new int[]{i, j});
                }
            }
        }
        return contacts;
    }

    private boolean isHhContact(int first, int second) {
        return second > first + 1
                && hpSequence.charAt(first) == 'H' && hpSequence.charAt(second) == 'H'
                && positions.get(first).isNeighbor(positions.get(second));
    }
}
