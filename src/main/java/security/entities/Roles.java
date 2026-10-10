package security.entities;
import io.javalin.security.RouteRole;

public enum Roles implements RouteRole {
    ANYONE(1),
    USER(2),
    ADMIN(3);

    private final int rank;

    Roles(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

}
