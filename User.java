public class User {
    private boolean admin;
    private String name;
    private String password;

    // Constructor
    public User(String name, String password, boolean admin) {
        this.name = name;
        this.password = password;
        this.admin = admin;
    }

   
    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
        public String toString() {
        return "User{name='" + name + "', admin=" + admin + "}";
    }
}
