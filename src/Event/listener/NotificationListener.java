package Event.listener;

import Event.*;
import Event.EventListener;
import MVC.Notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationListener implements EventListener<Event> {
    private final Map<Integer, List<Notification>> userNotifications;

    public NotificationListener() {
        this.userNotifications = new ConcurrentHashMap<>();
    }

    @Override
    public void onEvent(Event event) {
        Notification notification = createNotification(event);
        if (notification != null) {
            addNotification(notification);
        }
    }

    @Override
    public String[] getEventTypes() {
        return new String[]{
                "NOTE_CREATED", "NOTE_UPDATED", "NOTE_DELETED",
                "BULLETIN_GENERATED",
                "ETUDIANT_CREATED", "ENSEIGNANT_CREATED"
        };
    }
    @Override
    public String getName() {
        return "NotificationListener";
    }

    private Notification createNotification(Event event) {
        Notification notification = new Notification();
        notification.setEventId(event.getEventId());
        notification.setEventType(event.getEventType());
        notification.setCreatedAt(event.getTimestamp());

        switch (event.getEventType()) {
            case "NOTE_CREATED" -> {
                NoteCreatedEvent e = (NoteCreatedEvent) event;
                notification.setMessage(String.format(
                        "Nouvelle note en %s : %.2f/20",
                        e.getMatiere(), e.getNote().getValeur()
                ));
                notification.setType(Notification.Type.SUCCESS);
                notification.setUserId(e.getNote().getIdEtudiant());
                return notification;
            }

            case "NOTE_UPDATED" -> {
                NoteUpdatedEvent e = (NoteUpdatedEvent) event;
                notification.setMessage(String.format(
                        "Note modifiée en %s : %.2f → %.2f",
                        e.getMatiere(), e.getAncienneValeur(), e.getNouvelleValeur()
                ));
                notification.setType(Notification.Type.INFO);
                notification.setUserId(e.getNote().getIdEtudiant());
                return notification;
            }


            case "BULLETIN_GENERATED" -> {
                BulletinGeneratedEvent e = (BulletinGeneratedEvent) event;
                notification.setMessage(String.format(
                        "Nouveau bulletin disponible - %s - Moyenne: %.2f/20",
                        e.getBulletin().getPeriode(),
                        e.getBulletin().getMoyenne() != null ? e.getBulletin().getMoyenne() : 0.0
                ));
                notification.setType(Notification.Type.SUCCESS);
                notification.setUserId(e.getBulletin().getIdEtudiant());
                return notification;
            }

            case "ETUDIANT_CREATED" -> {
                EtudiantCreatedEvent e = (EtudiantCreatedEvent) event;
                notification.setMessage(String.format(
                        "Bienvenue sur la plateforme ! Votre compte a été créé avec succès."
                ));
                notification.setType(Notification.Type.SUCCESS);
                notification.setUserId(e.getEtudiant().getIdEtudiant());
                return notification;
            }

            case "ENSEIGNANT_CREATED" -> {
                EnseignantCreatedEvent e = (EnseignantCreatedEvent) event;
                notification.setMessage(String.format(
                        "Bienvenue sur la plateforme ! Votre compte enseignant a été créé."
                ));
                notification.setType(Notification.Type.SUCCESS);
                notification.setUserId(e.getEnseignant().getIdEnseignant());
                return notification;
            }

            default -> {
                return null;
            }
        }
    }
    /**
     * Ajouter une notification pour un utilisateur
     */
    private void addNotification(Notification notification) {
        if (notification.getUserId() == null) return;

        userNotifications
                .computeIfAbsent(notification.getUserId(), k -> new ArrayList<>())
                .add(notification);

        System.out.println("📬 Notification créée pour l'utilisateur " + notification.getUserId());
    }

    public List<Notification> getNotificationsForUser(Integer userId) {
        return new ArrayList<>(userNotifications.getOrDefault(userId, Collections.emptyList()));
    }

    public List<Notification> getUnreadNotificationsForUser(Integer userId) {
        return getNotificationsForUser(userId).stream()
                .filter(n -> !n.isLu())
                .toList();
    }

    public void markAsRead(Integer userId, Integer notificationIndex) {
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications != null && notificationIndex < notifications.size()) {
            notifications.get(notificationIndex).setLu(true);
        }
    }
    public void markAllAsRead(Integer userId) {
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications != null) {
            notifications.forEach(n -> n.setLu(true));
        }
    }

    public void clearNotifications(Integer userId) {
        userNotifications.remove(userId);
    }

    public int countUnreadNotifications(Integer userId) {
        return (int) getNotificationsForUser(userId).stream()
                .filter(n -> !n.isLu())
                .count();
    }
}
