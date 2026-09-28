// setup variables and variable types
// set up methods to get variables
public class UserAccount {
    private String username;
    private String passwordHash;
    private byte[] salt;
    private int failedLoginAttempts;
    private long lockoutTimestamp; // holds longer numbers then int can
    
    public UserAccount(String username, String passwordHash, byte[] salt){
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.failedLoginAttempts = 0;
        this.lockoutTimestamp = 0;
    }
    public String getUsername() {return username; }
    public String getPasswordHash() {return passwordHash; }
    public byte[] getSalt() {return salt;}

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void incrementFailedAttempts() {this.failedLoginAttempts++; } //adds 1 to the count
    public void resetAttempts() {this.failedLoginAttempts = 0; }

    public void setLockoutTimestamp(long timestamp){ this.lockoutTimestamp = timestamp; }

    // check is account is locked out 
    public boolean isLocked(){
        if (lockoutTimestamp == 0){
            return false;
        }
        // if the timer has passed unlock account
        //gets current time in milliseconds
        if  (System.currentTimeMillis() > lockoutTimestamp){
            this.lockoutTimestamp = 0;
            this.failedLoginAttempts = 0;
            return false;
        }
        return true;
    }

}
