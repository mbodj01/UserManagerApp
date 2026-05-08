package sn.esp.service;

import java.util.List;

import sn.esp.client.SoapClient;
import sn.esp.model.User;

public class UserService {

    private final SoapClient soapClient;

    public UserService(SoapClient soapClient) {
        this.soapClient = soapClient;
    }

    public String authentifier(String login, String motDePasse) {
        return soapClient.authentifier(login, motDePasse);
    }

    public boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public List<User> listerUtilisateurs() {
        return soapClient.listerUtilisateurs();
    }

    public boolean ajouterUtilisateur(String login, String motDePasse, String email, String role) {
        return soapClient.ajouterUtilisateur(new User(0, login, motDePasse, email, role));
    }

    public boolean modifierUtilisateur(int id, String login, String motDePasse, String email, String role) {
        return soapClient.modifierUtilisateur(new User(id, login, motDePasse, email, role));
    }

    public boolean supprimerUtilisateur(int userId) {
        return soapClient.supprimerUtilisateur(userId);
    }
}
