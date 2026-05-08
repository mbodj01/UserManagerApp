package sn.esp.ui;

import java.util.List;
import java.util.Scanner;

import sn.esp.model.User;
import sn.esp.service.UserService;


public class ConsoleUI {

    private static final String SEPARATOR = "═══════════════════════════════════════════════════════";
    private static final String LINE      = "───────────────────────────────────────────────────────";
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";

    private static final int MAX_TENTATIVES = 3;

    private final UserService userService;
    private final Scanner     scanner;
    private String            loginCourant;

    public ConsoleUI(UserService userService) {
        this.userService = userService;
        this.scanner     = new Scanner(System.in);
    }


    public void start() {
        printBanner();
        if (!authentifier()) {
            printError("Accès refusé. Application fermée.");
            return;
        }
        menuPrincipal();
        printInfo("Au revoir, " + loginCourant + " !");
    }


    private boolean authentifier() {
        for (int tentative = 1; tentative <= MAX_TENTATIVES; tentative++) {
            println(CYAN + "\n  Tentative " + tentative + "/" + MAX_TENTATIVES + RESET);

            print("  Login       : ");
            String login = scanner.nextLine().trim();

            print("  Mot de passe : ");
            String motDePasse = lireMotDePasse();

            printInfo("Vérification en cours...");
            String role = userService.authentifier(login, motDePasse);

            if (role == null) {
                printError("Identifiants incorrects ou serveur inaccessible.");
                continue;
            }

            if (!userService.isAdmin(role)) {
                printError("Accès refusé — rôle insuffisant : " + role + " (ADMIN requis).");
                return false;
            }

            printSuccess("Authentifié avec succès ! Bienvenue " + login + " (" + role + ").");
            loginCourant = login;
            return true;
        }
        return false;
    }


    private void menuPrincipal() {
        boolean running = true;
        while (running) {
            printHeader("MENU PRINCIPAL");
            println("  " + BOLD + "1." + RESET + "  Lister les utilisateurs");
            println("  " + BOLD + "2." + RESET + "  Ajouter un utilisateur");
            println("  " + BOLD + "3." + RESET + "  Modifier un utilisateur");
            println("  " + BOLD + "4." + RESET + "  Supprimer un utilisateur");
            println("  " + BOLD + "0." + RESET + "  Quitter");
            println(LINE);
            print("  Choix : ");

            switch (scanner.nextLine().trim()) {
                case "1" -> listerUtilisateurs();
                case "2" -> ajouterUtilisateur();
                case "3" -> modifierUtilisateur();
                case "4" -> supprimerUtilisateur();
                case "0" -> running = false;
                default  -> printError("Option invalide.");
            }
        }
    }


    private void listerUtilisateurs() {
        printHeader("LISTE DES UTILISATEURS");
        List<User> users = userService.listerUtilisateurs();
        if (users.isEmpty()) {
            printWarning("Aucun utilisateur trouvé.");
        } else {
            printTableHeader();
            users.forEach(u -> println("  " + u));
            printTableFooter();
            println("  Total : " + BOLD + users.size() + RESET + " utilisateur(s)");
        }
        pause();
    }

    private void ajouterUtilisateur() {
        printHeader("AJOUTER UN UTILISATEUR");

        print("  Login       : ");
        String login = scanner.nextLine().trim();

        String motDePasse = saisirMotDePasseAvecConfirmation();

        print("  Email       : ");
        String email = scanner.nextLine().trim();

        String role = selectionnerRole();

        afficherRecap(0, login, email, role);

        if (confirmer("Confirmer l'ajout ?")) {
            boolean ok = userService.ajouterUtilisateur(login, motDePasse, email, role);
            if (ok) printSuccess("Utilisateur '" + login + "' ajouté avec succès.");
            else    printError("Échec de l'ajout (login déjà utilisé ?).");
        } else {
            printWarning("Opération annulée.");
        }
        pause();
    }

    private void modifierUtilisateur() {
        printHeader("MODIFIER UN UTILISATEUR");

        List<User> users = userService.listerUtilisateurs();
        if (users.isEmpty()) { printWarning("Aucun utilisateur."); pause(); return; }
        printTableHeader();
        users.forEach(u -> println("  " + u));
        printTableFooter();

        int id = lireEntier("  ID à modifier : ");

        print("  Nouveau login       : ");
        String login = scanner.nextLine().trim();

        print("  Nouveau mot de passe (Entrée = inchangé) : ");
        String motDePasse = lireMotDePasse();

        print("  Nouvel email        : ");
        String email = scanner.nextLine().trim();

        String role = selectionnerRole();

        if (confirmer("Confirmer la modification ?")) {
            boolean ok = userService.modifierUtilisateur(id, login, motDePasse, email, role);
            if (ok) printSuccess("Utilisateur #" + id + " modifié.");
            else    printError("Échec — l'ID existe-t-il ?");
        } else {
            printWarning("Annulé.");
        }
        pause();
    }

    private void supprimerUtilisateur() {
        printHeader("SUPPRIMER UN UTILISATEUR");

        List<User> users = userService.listerUtilisateurs();
        if (users.isEmpty()) { printWarning("Aucun utilisateur."); pause(); return; }
        printTableHeader();
        users.forEach(u -> println("  " + u));
        printTableFooter();

        int id = lireEntier("  ID à supprimer : ");
        printWarning("⚠  Action irréversible !");

        if (confirmer("Confirmer la suppression de l'utilisateur #" + id + " ?")) {
            boolean ok = userService.supprimerUtilisateur(id);
            if (ok) printSuccess("Utilisateur #" + id + " supprimé.");
            else    printError("Échec — l'ID est-il correct ?");
        } else {
            printWarning("Annulé.");
        }
        pause();
    }

    private String selectionnerRole() {
        println("  Rôles disponibles :");
        println("    1. ADMIN");
        println("    2. USER");
        println("    3. MODERATEUR");
        while (true) {
            print("  Choisissez (1-3) : ");
            switch (scanner.nextLine().trim()) {
                case "1" -> { return "ADMIN";      }
                case "2" -> { return "USER";       }
                case "3" -> { return "MODERATEUR"; }
                default  -> printError("Entrez 1, 2 ou 3.");
            }
        }
    }

    private String saisirMotDePasseAvecConfirmation() {
        while (true) {
            print("  Mot de passe : ");
            String p1 = lireMotDePasse();
            print("  Confirmer   : ");
            String p2 = lireMotDePasse();
            if (p1.equals(p2)) return p1;
            printError("Les mots de passe ne correspondent pas.");
        }
    }

    private String lireMotDePasse() {
        java.io.Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword();
            return pwd != null ? new String(pwd) : "";
        }
        return scanner.nextLine(); // fallback IDE
    }

    private int lireEntier(String prompt) {
        while (true) {
            print(prompt);
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { printError("Entrez un nombre entier."); }
        }
    }

    private boolean confirmer(String message) {
        print("  " + YELLOW + message + " [o/N] : " + RESET);
        String r = scanner.nextLine().trim().toLowerCase();
        return r.equals("o") || r.equals("oui");
    }

    private void pause() {
        println("");
        print("  Appuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }

    private void afficherRecap(int id, String login, String email, String role) {
        println("\n  ── Récapitulatif ──────────────────────");
        if (id > 0) println("  ID     : " + id);
        println("  Login  : " + login);
        println("  Email  : " + email);
        println("  Rôle   : " + role);
        println("  ───────────────────────────────────────");
    }


    private void printTableHeader() {
        println("\n  " + LINE);
        println(String.format("  | %-4s | %-20s | %-30s | %-12s |",
                "ID", "LOGIN", "EMAIL", "ROLE"));
        println("  " + LINE);
    }

    private void printTableFooter() {
        println("  " + LINE + "\n");
    }


    private void printBanner() {
        println(CYAN + "\n" + SEPARATOR);
        println("   Projet Architecture Logicielle — ESP/UCAD · DIT2");
        println("   Gestion des Utilisateurs via Service SOAP");
        println(SEPARATOR + RESET + "\n");
    }

    private void printHeader(String title) {
        println("\n" + CYAN + SEPARATOR);
        println("   " + BOLD + title + RESET + CYAN);
        println(SEPARATOR + RESET);
    }

    private void printSuccess(String m) { println(GREEN  + "  ✔  " + m + RESET); }
    private void printError(String m)   { println(RED    + "  ✘  " + m + RESET); }
    private void printInfo(String m)    { println(CYAN   + "  ℹ  " + m + RESET); }
    private void printWarning(String m) { println(YELLOW + "  ⚠  " + m + RESET); }
    private void println(String s)      { System.out.println(s); }
    private void print(String s)        { System.out.print(s); System.out.flush(); }
}