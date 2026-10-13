package utils;

import dk.bugelhartmann.UserDTO;
import exceptions.EntityException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import security.dao.UserDAO;
import security.entities.User;

public class AuthUtil {
    public static User getLoggedInUser(Context ctx, UserDAO userDAO) {
        //I tokenet ligger emailen i username-feltet
        //Grundet at det ikke er min kode, og fra kursusmateriale tør jeg ikke at røre for meget ved den
        //Så længe den virker :)
        UserDTO loggedUser = ctx.attribute("user");
        User user = userDAO.findByEmail(loggedUser.getUsername());
        if(user == null){
            throw new EntityException(HttpStatus.UNAUTHORIZED, "You do not have access to this page or you need to login");
        }
        return user;
    }
}
