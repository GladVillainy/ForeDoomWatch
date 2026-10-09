package security.dao;

import security.entities.Roles;
import security.entities.User;

public interface ISecurityDAO {
    User findByEmail(String email);
    User getVerifiedUser(String email, String password);
    User createUser(String username, String email, String password);
    User addUserRole(String email, Roles role);
}