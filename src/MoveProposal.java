// Nje perpjekje e vetme e SA; statusi shpjegon edhe rastet pa kandidat.
record MoveProposal(Protein candidate, Status status) {
    enum Status {
        VALID, COLLISION, DISCONNECTED, NO_CHANGE, NO_MOVE, OUT_OF_RANGE
    }
}
