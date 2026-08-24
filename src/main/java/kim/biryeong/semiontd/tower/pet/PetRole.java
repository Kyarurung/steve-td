package kim.biryeong.semiontd.tower.pet;

public enum PetRole {
    BUTLER(true),
    TRAINER(true),
    KEEPER(true),
    DOG(false),
    CAT(false),
    BIRD(false);

    private final boolean owner;

    PetRole(boolean owner) {
        this.owner = owner;
    }

    public boolean isOwner() {
        return owner;
    }

    public boolean isCompanion() {
        return !owner;
    }
}
