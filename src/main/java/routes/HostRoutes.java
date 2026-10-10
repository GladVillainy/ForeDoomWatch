package routes;

import controller.HostController;
import io.javalin.apibuilder.EndpointGroup;
import security.entities.Roles;

import static io.javalin.apibuilder.ApiBuilder.*;

public class HostRoutes {

    private final HostController hostController;

    public HostRoutes(HostController hostController) {
        this.hostController = hostController;
    }

    public EndpointGroup getRoutes() {
        return () -> {
            post("/", hostController::create, Roles.USER);
            get("/", hostController::getAll, Roles.USER);
            get("/{id}", hostController::getById, Roles.USER);
            put("/{id}", hostController::update, Roles.USER);
            delete("/{id}", hostController::delete, Roles.USER);
        };
    }
}
