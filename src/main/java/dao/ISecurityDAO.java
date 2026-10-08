package dao;

import entities.Roles;
import entities.User;

public interface ISecurityDAO {
    User findByUsername(String username);
    User getVerifiedUser(String username, String password);
    User createUser(String username, String email, String password);
    User addUserRole(String username, Roles role);
}
