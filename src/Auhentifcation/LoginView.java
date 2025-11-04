package Auhentifcation;

import MVC.User;

import java.util.Optional;
import java.util.Scanner;

public class LoginView {
    private final AuthenticationService authService;
    private final Scanner scanner;

    public LoginView(AuthenticationService authService, Scanner scanner) {
        this.authService = authService;
        this.scanner = scanner;
    }

    public Optional<User> afficherLogin(){
        int tentatives = 0;
        int maxTentatives = 3;

        while (tentatives < maxTentatives){
            afficherEcranLogin();

            System.out.print("Login (ou 'q' pour quitter) : ");
            String login = scanner.nextLine().trim();

            if ("q".equalsIgnoreCase(login)) {
                return Optional.empty();
            }

            if (login.isEmpty()) {
                System.out.println(" Le login ne peut pas être vide.\n");
                continue;
            }
            System.out.print("Mot de passe : ");
            String password = scanner.nextLine();

            try {
                Optional<User> user = authService.authenticate(login, password);

                if (user.isPresent()) {
                    afficherBienvenue(user.get());
                    return user;
                } else {
                    tentatives++;
                    int restantes = maxTentatives - tentatives;

                    if (restantes > 0) {
                        System.out.println("\n Login ou mot de passe incorrect.");
                        System.out.println("   Tentatives restantes : " + restantes + "\n");
                    }
                }

            } catch (IllegalStateException e) {
                System.out.println("\n " + e.getMessage() + "\n");
                tentatives++;
            }
        }
        System.out.println("\nNombre maximum de tentatives atteint.");
        System.out.println("Veuillez contacter un administrateur.\n");
        return Optional.empty();

    }

    private void afficherEcranLogin(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                ║");
        System.out.println("║                      AUTHENTIFICATION                          ║");
        System.out.println("║                                                                ║");
        System.out.println("║              Système de Gestion Scolaire                       ║");
        System.out.println("║                                                                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private void afficherBienvenue(User user){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                       CONNEXION RÉUSSIE                        ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Bienvenue, %-50s ║%n", user.getNomComplet());
        System.out.printf("║  Rôle : %-54s ║%n", user.getRole().getLibelle());

        if (user.getDerniereConnexion() != null) {
            System.out.printf("║  Dernière connexion : %-39s  ║%n",
                    user.getDerniereConnexion().toLocalDateTime().toString());
        }

        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        attendreEntree();
    }
    private void attendreEntree() {
        System.out.print("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }

}
