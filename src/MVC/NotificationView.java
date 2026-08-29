package MVC;

import Event.EventDispatcher;
import Event.listener.NotificationListener;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Vue pour afficher et gérer les notifications
 */
public class NotificationView {

    private final NotificationListener notificationListener;
    private final Scanner scanner;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public NotificationView(NotificationListener notificationListener, Scanner scanner) {
        this.notificationListener = notificationListener;
        this.scanner = scanner;
    }

    /**
     * Afficher le centre de notifications pour un utilisateur
     */
    public void afficherNotifications(Integer userId) {
        while (true) {
            List<Notification> notifications = notificationListener.getNotificationsForUser(userId);
            int unreadCount = notificationListener.countUnreadNotifications(userId);

            afficherMenu(notifications.size(), unreadCount);
            String choix = scanner.nextLine().trim();

            if (!traiterChoix(choix, userId, notifications)) {
                break;
            }
        }
    }

    private void afficherMenu(int totalCount, int unreadCount) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    CENTRE DE NOTIFICATIONS                     ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  📬 Total        : %-44d║%n", totalCount);
        System.out.printf("║  🔔 Non lues     : %-44d║%n", unreadCount);
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  1. 📋 Voir toutes les notifications                          ║");
        System.out.println("║  2. 🔔 Voir les notifications non lues                        ║");
        System.out.println("║  3. ✅ Marquer toutes comme lues                              ║");
        System.out.println("║  4. 🗑️  Effacer toutes les notifications                       ║");
        System.out.println("║  0. ⬅️  Retour                                                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.print("Votre choix > ");
    }

    private boolean traiterChoix(String choix, Integer userId, List<Notification> notifications) {
        switch (choix) {
            case "1":
                afficherListeNotifications(notifications, false);
                break;
            case "2":
                List<Notification> unread = notificationListener.getUnreadNotificationsForUser(userId);
                afficherListeNotifications(unread, true);
                break;
            case "3":
                notificationListener.markAllAsRead(userId);
                System.out.println("\n✅ Toutes les notifications ont été marquées comme lues.");
                attendreEntree();
                break;
            case "4":
                System.out.print("\n⚠️  Confirmer la suppression de toutes les notifications ? (o/N) : ");
                String confirmation = scanner.nextLine().trim().toLowerCase();
                if ("o".equals(confirmation) || "oui".equals(confirmation)) {
                    notificationListener.clearNotifications(userId);
                    System.out.println("✅ Toutes les notifications ont été supprimées.");
                } else {
                    System.out.println("❌ Suppression annulée.");
                }
                attendreEntree();
                break;
            case "0":
                return false;
            default:
                System.out.println("❌ Choix invalide.");
                attendreEntree();
        }
        return true;
    }

    private void afficherListeNotifications(List<Notification> notifications, boolean unreadOnly) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.printf("║     %s                                 ║%n",
                unreadOnly ? "NOTIFICATIONS NON LUES" : "TOUTES LES NOTIFICATIONS");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        if (notifications.isEmpty()) {
            System.out.println("\n Aucune notification.");
        } else {
            for (int i = 0; i < notifications.size(); i++) {
                Notification n = notifications.get(i);
                afficherNotification(n, i + 1);
            }
        }

        attendreEntree();
    }

    private void afficherNotification(Notification notification, int index) {
        String statusIcon = notification.isLu() ? "✓" : "●";

        System.out.println("\n┌────────────────────────────────────────────────────────────────┐");
        System.out.printf("│ %s #%-2d %s [%s]%n",
                statusIcon, index, notification.getIcon(), notification.getType());
        System.out.println("├────────────────────────────────────────────────────────────────┤");
        System.out.printf("│ %s%n", wrapText(notification.getMessage(), 62));
        System.out.printf("│ 📅 %s%n", notification.getCreatedAt().format(dtf));
        System.out.println("└────────────────────────────────────────────────────────────────┘");
    }

    private String wrapText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return String.format("%-" + maxLength + "s", text);
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Afficher un badge de notifications non lues
     */
    public String getBadge(Integer userId) {
        int unreadCount = notificationListener.countUnreadNotifications(userId);
        if (unreadCount == 0) {
            return "";
        }
        return String.format(" 🔔 (%d)", unreadCount);
    }

    private void attendreEntree() {
        System.out.print("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }
}