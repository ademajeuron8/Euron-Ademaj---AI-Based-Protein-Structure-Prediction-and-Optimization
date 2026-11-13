public class AminoAcidConverter {
    // Konventa e thjeshtuar HP e ketij projekti, jo klasifikim universal:
    // H: AVILMFWYC; P: DEGHKNPQRST.
    public static String toHp(String aminoAcidSequence) {
        if (aminoAcidSequence == null) {
            throw new IllegalArgumentException("Sekuenca e aminoacideve nuk mund te jete null.");
        }

        String sequence = aminoAcidSequence.trim();
        if (sequence.isEmpty()) {
            throw new IllegalArgumentException("Sekuenca e aminoacideve nuk mund te jete bosh.");
        }

        String allowed = "ACDEFGHIKLMNPQRSTVWYacdefghiklmnpqrstvwy";
        String hydrophobic = "AVILMFWYC";
        StringBuilder hp = new StringBuilder(sequence.length());
        for (int i = 0; i < sequence.length(); i++) {
            char original = sequence.charAt(i);
            // Kontrollo simbolin origjinal ndaj alfabetit ASCII para normalizimit.
            if (allowed.indexOf(original) < 0) {
                String invalid = new String(Character.toChars(sequence.codePointAt(i)));
                throw new IllegalArgumentException("Karakteri '" + invalid + "' ne pozicionin "
                        + (i + 1) + " nuk eshte aminoacid i lejuar.");
            }
            char aminoAcid = Character.toUpperCase(original);
            hp.append(hydrophobic.indexOf(aminoAcid) >= 0 ? 'H' : 'P');
        }
        return hp.toString();
    }
}
