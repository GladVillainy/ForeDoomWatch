package entities;

public enum FindingStatus {

    OPEN(1),
    IN_PROGRESS(2),
    RESOLVED(3);

    private final int order;

    FindingStatus(int order) {
        this.order = order;
    }
    public int getOrder() {
        return order;
    }
}
