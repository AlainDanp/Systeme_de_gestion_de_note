package Event;

/**
 * Événement déclenché lors de la déconnexion d'un utilisateur
 */
public class UserLogoutEvent extends BaseEvent {

    public UserLogoutEvent(Integer userId, String userName) {
        super(userId, userName);
    }

    @Override
    public String getEventType() {
        return "USER_LOGOUT";
    }

    @Override
    public String getDescription() {
        return "Déconnexion";
    }
}