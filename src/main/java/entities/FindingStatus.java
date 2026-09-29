package entities;

public enum FindingStatus {

    OPEN(1),
    IN_PROGRESS(2),
    RESOVLED(3);

    private final int status;

    FindingStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
