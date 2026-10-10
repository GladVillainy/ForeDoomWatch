package security.routes;

import io.javalin.apibuilder.EndpointGroup;
import security.controller.SecurityController;
import security.entities.Roles;

import static io.javalin.apibuilder.ApiBuilder.post;

public class SecurityRoutes {

    private final SecurityController securityController = SecurityController.getInstance();

    public EndpointGroup getRoutes() {
        return () -> {
            post("/login", securityController.login(), Roles.ANYONE);
            post("/register", securityController.register(), Roles.ANYONE);
        };
    }
}