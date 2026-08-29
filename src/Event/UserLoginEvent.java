package Event;

import MVC.Role;

public class UserLoginEvent extends BaseEvent{

    private final Role role;

    public UserLoginEvent(Integer userId, String userName, Role role) {
        super(userId, userName);
        this.role = role;
    }

    @Override
    public String getEventType() {
        return "USER_LOGIN";
    }

    @Override
    public String getDescription() {
        return String.format("Connexion réussie - Rôle: %s", role.getLibelle());
    }
    public Role getRole() {
        return role;
    }
}
