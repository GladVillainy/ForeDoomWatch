package routes;

import controller.FindingController;
import io.javalin.apibuilder.EndpointGroup;
import security.entities.Roles;

import static io.javalin.apibuilder.ApiBuilder.*;

public class FindingRoutes {

    private final FindingController findingController;

    public FindingRoutes(FindingController findingController) {
        this.findingController = findingController;
    }

    public EndpointGroup getRoutes() {
        return () -> {
            put("/{id}/status", findingController::updateStatus, Roles.USER);

            get("/user/{id}/status/asc", findingController::sortStatusByAscending, Roles.USER);
            get("/user/{id}/status/desc", findingController::sortStatusByDescending, Roles.USER);
            get("/user/{id}/cvss/asc", findingController::sortCvssByAscending, Roles.USER);
            get("/user/{id}/cvss/desc", findingController::sortCvssByDescending, Roles.USER);

        };
    }
}