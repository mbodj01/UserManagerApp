package sn.esp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sn.esp.model.User;


public class SoapClient {

    private static final String DEFAULT_SOAP_URL = "http://localhost:8080/ProjetArchi/ws/utilisateur";
    private static final String SOAP_NAMESPACE   = "http://webservices.archi.com/utilisateur";

    private final String soapUrl;
    private final String authToken;

    public SoapClient(String authToken) {
        this(DEFAULT_SOAP_URL, authToken);
    }

    public SoapClient(String soapUrl, String authToken) {
        this.soapUrl   = soapUrl;
        this.authToken = authToken;
    }


    public String authentifier(String login, String motDePasse) {
        String body = buildAuthentifierBody(login, motDePasse);
        String response = sendSoapRequest("authentifier", body);
        if (response == null) return null;
        String role = extractTag(response, "role");
        if (role == null) role = extractTag(response, "Role");
        if (role == null) role = extractTag(response, "return");
        return role;
    }

    public List<User> listerUtilisateurs() {
        String body = buildTokenOnlyBody("listerUtilisateurs");
        String response = sendSoapRequest("listerUtilisateurs", body);
        if (response == null) return new ArrayList<>();
        return parseUserList(response);
    }

    public boolean ajouterUtilisateur(User user) {
        String response = sendSoapRequest("ajouterUtilisateur", buildUserBody("ajouterUtilisateur", user));
        return isSuccess(response);
    }

    public boolean modifierUtilisateur(User user) {
        String response = sendSoapRequest("modifierUtilisateur", buildUserBody("modifierUtilisateur", user));
        return isSuccess(response);
    }

    public boolean supprimerUtilisateur(int userId) {
        String response = sendSoapRequest("supprimerUtilisateur", buildSupprimerBody(userId));
        return isSuccess(response);
    }


    private String buildAuthentifierBody(String login, String motDePasse) {
        return soapEnvelope(
            "<tns:authentifier>" +
                "<login>"      + escapeXml(login)      + "</login>" +
                "<motDePasse>" + escapeXml(motDePasse) + "</motDePasse>" +
            "</tns:authentifier>"
        );
    }

    private String buildTokenOnlyBody(String operation) {
        return soapEnvelope(
            "<tns:" + operation + ">" +
                "<jeton>" + escapeXml(authToken) + "</jeton>" +
            "</tns:" + operation + ">"
        );
    }

    private String buildUserBody(String operation, User user) {
        return soapEnvelope(
            "<tns:" + operation + ">" +
                "<jeton>"      + escapeXml(authToken)      + "</jeton>" +
                (user.getId() > 0 ? "<id>" + user.getId() + "</id>" : "") +
                "<login>"      + escapeXml(user.getLogin())    + "</login>" +
                "<motDePasse>" + escapeXml(user.getPassword()) + "</motDePasse>" +
                "<email>"      + escapeXml(user.getEmail())    + "</email>" +
                "<role>"       + escapeXml(user.getRole())     + "</role>" +
            "</tns:" + operation + ">"
        );
    }

    private String buildSupprimerBody(int userId) {
        return soapEnvelope(
            "<tns:supprimerUtilisateur>" +
                "<jeton>" + escapeXml(authToken) + "</jeton>" +
                "<id>" + userId + "</id>" +
            "</tns:supprimerUtilisateur>"
        );
    }

    private String soapEnvelope(String content) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soapenv:Envelope " +
                   "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                   "xmlns:tns=\"" + SOAP_NAMESPACE + "\">" +
               "<soapenv:Header/>" +
               "<soapenv:Body>" + content + "</soapenv:Body>" +
               "</soapenv:Envelope>";
    }


    private String sendSoapRequest(String action, String soapBody) {
        try {
            URL url = new URL(soapUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            conn.setRequestProperty("SOAPAction", "\"" + SOAP_NAMESPACE + "/" + action + "\"");
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(10_000);

            try (OutputStream os = conn.getOutputStream();
                 PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"))) {
                pw.print(soapBody);
                pw.flush();
            }

            int statusCode = conn.getResponseCode();
            InputStream is = (statusCode >= 400) ? conn.getErrorStream() : conn.getInputStream();
            if (is == null) return null;

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append("\n");
            }
            return sb.toString();

        } catch (IOException e) {
            System.err.println("[SOAP ERROR] " + e.getMessage());
            return null;
        }
    }


    private List<User> parseUserList(String xml) {
        List<User> users = new ArrayList<>();
        String[] tags = {"utilisateur", "Utilisateur", "user", "return", "item"};
        for (String tag : tags) {
            Pattern p = Pattern.compile("<" + tag + ">(.*?)</" + tag + ">", Pattern.DOTALL);
            Matcher m = p.matcher(xml);
            boolean found = false;
            while (m.find()) {
                found = true;
                String block = m.group(1);
                User u = new User();
                String idStr = extractTag(block, "id");
                if (idStr != null) {
                    try { u.setId(Integer.parseInt(idStr.trim())); } catch (NumberFormatException ignored) {}
                }
                u.setLogin(extractTagSafe(block, "login"));
                u.setEmail(extractTagSafe(block, "email"));
                u.setRole(extractTagSafe(block, "role"));
                users.add(u);
            }
            if (found) break;
        }
        return users;
    }

    private String extractTag(String xml, String tag) {
        Pattern p = Pattern.compile(
            "<(?:[^:>]+:)?" + Pattern.quote(tag) + "(?:\\s[^>]*)?>(.*?)</(?:[^:>]+:)?" + Pattern.quote(tag) + ">",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(xml);
        return m.find() ? m.group(1).trim() : null;
    }

    private String extractTagSafe(String xml, String tag) {
        String v = extractTag(xml, tag);
        return v != null ? v : "";
    }

    private boolean isSuccess(String response) {
        if (response == null) return false;
        String lower = response.toLowerCase();
        return lower.contains("<success>true</success>")
            || lower.contains("<statut>ok</statut>")
            || lower.contains("<return>true</return>")
            || lower.contains("<result>success</result>")
            || (lower.contains("response>") && !lower.contains("fault"));
    }

    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }

    public String getSoapUrl()   { return soapUrl;   }
    public String getAuthToken() { return authToken; }
}