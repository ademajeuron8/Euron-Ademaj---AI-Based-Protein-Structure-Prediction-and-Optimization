import java.util.ArrayList;

// Propozimi i origjines ose gjashte drejtimet e nje prindi ne Beam.
record ExtensionBatch(ArrayList<Protein> candidates, int proposals, int collisionProposals,
                      int outOfRangeProposals) { }
