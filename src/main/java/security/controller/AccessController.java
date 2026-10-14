package security.controller;

import dk.bugelhartmann.UserDTO;
import exceptions.ApiException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.security.RouteRole;
import security.entities.Roles;

import java.util.Set;

/**
 * Purpose: To handle security in the API at the route level
 *  Author: Jon Bertelsen
 *  Author: Edited by Lucas
 */

public class AccessController {

    SecurityController securityController = SecurityController.getInstance();

    public void accessHandler(Context ctx) {

        // If no roles are specified on the endpoint, then anyone can access the route
        if (ctx.routeRoles().isEmpty() || ctx.routeRoles().contains(Roles.ANYONE)){
            return;
        }

        // Check if the user is authenticated
        try {
            securityController.authenticate().handle(ctx);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Access denied due to invalid token or missing login");
        }

        // Check if the user has the necessary roles to access the route
        UserDTO user = ctx.attribute("user");
        Set<RouteRole> allowedRoles = ctx.routeRoles(); // roles allowed for the current route
        if (!securityController.authorize(user, allowedRoles)) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Unauthorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
        }
    }
}