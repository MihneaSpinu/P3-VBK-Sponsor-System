package p3project.classes;

public class JWT {
    private String payload;
    private String hash;

    protected JWT() {};

    public JWT signJWT(User user) {
        JWT jwt = new JWT();
        this.payload = user.getId().toString();
        // this.hash = bcryptHash(JWTsecret, payload);
        return jwt;
    }

    public boolean verifyJWT(JWT token) {
        // String hash = bcryptHash(JWTsecret, payload);
        // if(bcryptCompare(hash, JWT.hash)) return true;
        return false;
    }
}
