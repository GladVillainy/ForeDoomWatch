package security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.JOSEException;
import jakarta.persistence.EntityManagerFactory;
import security.dto.AuthDTO;
import exceptions.ApiException;
import exceptions.ValidationException;
import security.dao.ISecurityDAO;
import security.dao.UserDAO;
import security.entities.Roles;
import utils.Utils;
import config.HibernateConfig;
import security.entities.User;
import security.exceptions.NotAuthorizedException;
import dk.bugelhartmann.ITokenSecurity;
import dk.bugelhartmann.TokenSecurity;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Purpose: To handle security in the API
 * Author: Thomas Hartmann
 * Edited by Lucas
 */
public class SecurityController {
    ObjectMapper objectMapper = new ObjectMapper();
    ITokenSecurity tokenSecurity = new TokenSecurity();
    private static ISecurityDAO securityDAO;
    private static SecurityController instance;
    private static Logger logger = LoggerFactory.getLogger(SecurityController.class);

    public SecurityController() { }

    public static SecurityController getInstance() { // Singleton because we don't want multiple instances of the same class
        if (instance == null) {
            instance = new SecurityController();
            securityDAO = new UserDAO(HibernateConfig.getEntityManagerFactory());
        }
        return instance;
    }

    //Ændret: til REST Assured
    public static SecurityController getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new SecurityController();
            securityDAO = new UserDAO(emf);
        }
        return instance;
    }


    public Handler login() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode(); // for sending json messages back to the client
            // ÆNDRET: login med email og password
            AuthDTO user = ctx.bodyAsClass(AuthDTO.class);
            ValidationException.requireValue(user.password(), "password", "User");
            // ÆNDRET: getVerifiedUser returnerer User
            User verifiedUser = securityDAO.getVerifiedUser(user.email(), user.password());
            String token = createToken(new UserDTO(verifiedUser.getEmail(), verifiedUser.getRolesAsStrings()));

            ctx.status(200).json(returnObject
                    .put("token", token)
                    .put("username", verifiedUser.getUsername()));
        };
    }


    public Handler register() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            // ÆNDRET: Ingen try/catch (DAO kaster ApiException 409)
            AuthDTO userInput = ctx.bodyAsClass(AuthDTO.class);
            User created = securityDAO.createUser(userInput.username(), userInput.email(), userInput.password());

            String token = createToken(new UserDTO(created.getEmail(), created.getRolesAsStrings()));
            ctx.status(HttpStatus.CREATED).json(returnObject
                    .put("token", token)
                    .put("username", created.getUsername())
                    .put("email", created.getEmail()));
        };
    }


    public Handler authenticate() throws UnauthorizedResponse {

        ObjectNode returnObject = objectMapper.createObjectNode();
        return (ctx) -> {
            // This is a preflight request => OK
            if (ctx.method().toString().equals("OPTIONS")) {
                ctx.status(200);
                return;
            }
            String header = ctx.header("Authorization");
            if (header == null) {
                throw new UnauthorizedResponse("Authorization header missing");
            }

            String[] headerParts = header.split(" ");
            if (headerParts.length != 2) {
                throw new UnauthorizedResponse("Authorization header malformed");
            }

            String token = headerParts[1];
            UserDTO verifiedTokenUser = verifyToken(token);

            if (verifiedTokenUser == null) {
                throw new UnauthorizedResponse("Invalid User or Token");
            }
            logger.info("User verified: " + verifiedTokenUser);
            ctx.attribute("user", verifiedTokenUser);
        };
    }


    // Check if the user's roles contain any of the allowed roles
    public boolean authorize(UserDTO user, Set<RouteRole> allowedRoles) {
        if (user == null) {
            throw new UnauthorizedResponse("You need to log in, dude!");
        }
        Set<String> roleNames = allowedRoles.stream()
                .map(RouteRole::toString)  // Convert RouteRoles to  Set of Strings
                .collect(Collectors.toSet());
        return user.getRoles().stream()
                .map(String::toUpperCase)
                .anyMatch(roleNames::contains);
    }


    public String createToken(UserDTO user) {
        try {
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            // ÆNDRET: SECRET_KEY fra miljøvariabel
            String SECRET_KEY = System.getenv("SECRET_KEY");

            if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
                throw new IllegalStateException("SECRET_KEY is missing");
            }

            if (System.getenv("DEPLOYED") != null) {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
            } else {
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
            }
            return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            // ÆNDRET: ApiException tager HttpStatus
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create token");
        }
    }


    public UserDTO verifyToken(String token) {
        // ÆNDRET: SECRET_KEY fra miljøvariabel
        String SECRET = System.getenv("SECRET_KEY");

        try {
            if (tokenSecurity.tokenIsValid(token, SECRET) && tokenSecurity.tokenNotExpired(token)) {
                return tokenSecurity.getUserWithRolesFromToken(token);
            } else {
                throw new NotAuthorizedException(403, "Token is not valid");
            }
        } catch (ParseException | JOSEException | NotAuthorizedException e) {
            e.printStackTrace();
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized. Could not verify token");
        }
    }

    public @NotNull Handler addRole() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            // get the role from the body. the json is {"role": "manager"}.
            // We need to get the role from the body and the username from the token
            String newRole = ctx.bodyAsClass(ObjectNode.class).get("role").asText();
            UserDTO user = ctx.attribute("user");

            // ÆNDRET: DAO tager (username, Roles).
            Roles role;
            try {
                role = Roles.valueOf(newRole.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown role: " + newRole);
            }
            securityDAO.addUserRole(user.getUsername(), role);
            ctx.status(200).json(returnObject.put("msg", "Role " + newRole + " added to user"));
        };
    }

    // Health check for the API. Used in deployment
    public void healthCheck(@NotNull Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }
}