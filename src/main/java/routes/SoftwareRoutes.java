package routes;

import controller.SoftwareController;
import io.javalin.apibuilder.EndpointGroup;
import security.entities.Roles;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SoftwareRoutes {

    private final SoftwareController softwareController;

    public SoftwareRoutes(SoftwareController softwareController) {
        this.softwareController = softwareController;
    }


    //Routes til software på en host, oprettes og hentes via hostens id
    public EndpointGroup getHostScopedRoutes() {
        return () -> {
            post("/", softwareController::create, Roles.USER);
            get("/", softwareController::getByHost, Roles.USER);
        };
    }

    //Routes til software direkte via softwarens eget id, med crud
    public EndpointGroup getRoutes() {
        return () -> {
            get("/", softwareController::getAll, Roles.USER);
            get("/{id}", softwareController::getById, Roles.USER);
            put("/{id}", softwareController::update, Roles.USER);
            delete("/{id}", softwareController::delete, Roles.USER);
            post("/{id}/scan", softwareController::scan, Roles.USER);
        };
    }
}