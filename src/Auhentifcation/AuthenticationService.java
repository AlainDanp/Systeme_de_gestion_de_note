package Auhentifcation;

import MVC.Role;
import MVC.User;

import java.util.Optional;

public interface AuthenticationService {

    Optional<User>  authenticate(String login, String password);
    void logout();
    Optional<User> getCurrentUser();
    boolean isAuthenticated();
    boolean hasRole(Role role);
    void changePassword(String oldPassword, String newPassword);
}
