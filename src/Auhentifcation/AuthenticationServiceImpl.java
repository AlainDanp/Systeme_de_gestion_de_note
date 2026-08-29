package Auhentifcation;

import Admin.dao.AdminDao;
import Admin.model.Admin;
import Event.EventDispatcher;
import Event.UserLoginEvent;
import Event.UserLogoutEvent;
import MVC.PasswordUtil;
import MVC.Role;
import MVC.User;
import gestion_Enseignant.dao.EnseignantDao;
import gestion_Enseignant.model.Enseignant;
import gestion_Etudiant.dao.EtudiantDao;
import gestion_Etudiant.model.Etudiant;

import java.util.Optional;

public class AuthenticationServiceImpl implements AuthenticationService {

    private final AdminDao adminDao;
    private final EnseignantDao enseignantDao;
    private final EtudiantDao etudiantDao;
    private final EventDispatcher eventDispatcher;

    private User currentUser = null;

    public AuthenticationServiceImpl(AdminDao adminDao,EnseignantDao enseignantDao, EtudiantDao etudiantDao) {
        this.enseignantDao = enseignantDao;
        this.etudiantDao = etudiantDao;
        this.adminDao = adminDao;
        this.eventDispatcher = EventDispatcher.getInstance();
    }

    @Override
    public Optional<User> authenticate(String login, String password) {
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }

        // Chercher d'abord dans les enseignants
        Optional<Admin> admin = adminDao.findByLogin(login);
        if (admin.isPresent()) {
            Admin a = admin.get();

            if (!a.isActif()) {
                throw new IllegalStateException("Compte désactivé");
            }

            if (PasswordUtil.verifyPassword(password, a.getPasswordHash())) {
                adminDao.updateDerniereConnexion(a.getIdAdmin());
                currentUser = a;

                //  Déclencher l'événement USER_LOGIN
                UserLoginEvent event = new UserLoginEvent(a.getId(), a.getNomComplet(), a.getRole());
                eventDispatcher.dispatch(event);

                System.out.println(" Connexion réussie : " + a.getNomComplet() + " (Administrateur)");
                return Optional.of(a);
            }
        }

        Optional<Enseignant> enseignant = enseignantDao.findByLogin(login);
        if (enseignant.isPresent()) {
            Enseignant e = enseignant.get();

            // Vérifier si le compte est actif
            if (!e.isActif()) {
                throw new IllegalStateException("Compte désactivé");
            }

            // Vérifier le mot de passe
            if (PasswordUtil.verifyPassword(password, e.getPasswordHash())) {
                // Mettre à jour la dernière connexion
                if (PasswordUtil.besoinDeMigration(e.getPasswordHash())) {
                    String nouveau = PasswordUtil.hashPassword(password);
                    enseignantDao.updatePassword(e.getIdEnseignant(), nouveau);
                    e.setPasswordHash(nouveau);
                }
                enseignantDao.updateDerniereConnexion(e.getIdEnseignant());

                // Stocker dans la session
                currentUser = e;

                UserLoginEvent event = new UserLoginEvent(e.getId(), e.getNomComplet(), e.getRole());
                eventDispatcher.dispatch(event);

                System.out.println(" Connexion réussie : " + e.getNomComplet() + " (Enseignant)");
                return Optional.of(e);
            }
        }

        // Chercher dans les étudiants
        Optional<Etudiant> etudiant = etudiantDao.findByLogin(login);
        if (etudiant.isPresent()) {
            Etudiant e = etudiant.get();


            // Vérifier le mot de passe
            if (PasswordUtil.verifyPassword(password, e.getPasswordHash())) {
                if (!e.isActif()) {
                    throw new IllegalStateException("Compte désactivé");
                }
                // Mettre à jour la dernière connexion
                etudiantDao.updateDerniereConnexion(e.getIdEtudiant());

                // Stocker dans la session
                currentUser = e;

                //  Déclencher l'événement USER_LOGIN
                UserLoginEvent event = new UserLoginEvent(e.getId(), e.getNomComplet(), e.getRole());
                eventDispatcher.dispatch(event);

                System.out.println(" Connexion réussie : " + e.getNomComplet() + " (Étudiant)");
                return Optional.of(e);
            }
        }

        System.out.println(" Échec de connexion pour : " + login);
        return Optional.empty();
    }

    @Override
    public void logout() {
        if (currentUser != null) {

            UserLogoutEvent event = new UserLogoutEvent(currentUser.getId(), currentUser.getNomComplet());
            eventDispatcher.dispatch(event);

            System.out.println(" Déconnexion de : " + currentUser.getNomComplet());
            currentUser = null;
        }
    }

    public class GenererHash {
        public static void main(String[] args) {
            System.out.println(MVC.PasswordUtil.hashPassword("Test1234"));
        }
    }

    @Override
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    @Override
    public boolean isAuthenticated() {
        return currentUser != null;
    }


    @Override
    public boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        if (currentUser == null) {
            throw new IllegalStateException("Aucun utilisateur connecté");
        }

        // Vérifier l'ancien mot de passe
        if (!PasswordUtil.verifyPassword(oldPassword, currentUser.getPasswordHash())) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect");
        }

        // Valider le nouveau mot de passe
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit contenir au moins 6 caractères");
        }

        // Hacher et enregistrer
        String newHash = PasswordUtil.hashPassword(newPassword);

        if (currentUser.getRole() == Role.ADMIN) {
            adminDao.updatePassword(currentUser.getId(), newHash);
        } else if (currentUser.getRole() == Role.ENSEIGNANT) {
            enseignantDao.updatePassword(currentUser.getId(), newHash);
        } else if (currentUser.getRole() == Role.ETUDIANT) {
            etudiantDao.updatePassword(currentUser.getId(), newHash);
        }

        currentUser.setPasswordHash(newHash);
        System.out.println("Mot de passe changé avec succès");
    }
}