
package sn.esp.model;


public class User {

    private int id;
    private String login;
    private String password;
    private String email;
    private String role; 

    public User() {}

    public User(int id, String login, String password, String email, String role) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.email = email;
        this.role = role;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return String.format("| %-4d | %-20s | %-30s | %-10s |",
                id, login, email, role);
    }
}