package sn.esp;

import java.util.Scanner;

import sn.esp.client.SoapClient;
import sn.esp.service.UserService;
import sn.esp.ui.ConsoleUI;


public class Main {

    private static final String DEFAULT_URL =
            "http://localhost:8080/ProjetArchi/ws/utilisateur";

    public static void main(String[] args) {
        String soapUrl;
        String jeton;

        if (args.length >= 2) {
            soapUrl = args[0];
            jeton   = args[1];
        } else {
            Scanner sc = new Scanner(System.in);
            System.out.println("\n  ── Configuration ──────────────────────────────");
            System.out.print("  URL SOAP [" + DEFAULT_URL + "] : ");
            String input = sc.nextLine().trim();
            soapUrl = input.isEmpty() ? DEFAULT_URL : input;

            System.out.print("  Jeton d'authentification (généré par l'admin) : ");
            jeton = sc.nextLine().trim();

            if (jeton.isEmpty()) {
                System.err.println("  [ERREUR] Le jeton est obligatoire.");
                System.exit(1);
            }
        }

        SoapClient  client  = new SoapClient(soapUrl, jeton);
        UserService service = new UserService(client);
        ConsoleUI   ui      = new ConsoleUI(service);

        ui.start();
    }
}