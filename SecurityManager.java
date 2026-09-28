//I1 - takes a user password and irreversibly scrambles it into a fixed-length signature
//I2 - random sequence of bytes added to a password right before it goes into the MessageDigest
//I3 - translates raw bytes produced from hashing into standard, readable text characters
//I3 - process the data safely without corrupting

// Hashing (SHA-256) is for Passwords: The One-Way Street
//Encryption (AES) is for Messages: The Two-Way Street
import java.io.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
// allows for methods encrypt AES and decryptAES
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SecurityManager {
    // secret key for AES-128 encryption
    private static final byte[] AESKEY = "CourseworkKey123".getBytes();
    public static String encryptAES(String data){
        try{
            SecretKeySpec secretKey = new SecretKeySpec(AESKEY, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        }catch (Exception e){
            System.out.print("Encyrption Error" + e.getMessage());
            return null;
        }
    }

    public static String decryptAES(String encryptedData) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(AESKEY, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted);
        } catch (Exception e) {
            //if it fails return null
            return null;
        }
    }
    // Lockout settings: 3 attempts, locked for 60 seconds
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION = 60000; 
    private static final String USERFILE = "Users.csv";
    
    // This acts as our secure user database
    public static ConcurrentHashMap<String, UserAccount> userDatabase = new ConcurrentHashMap<>();

    static{
        loadUsersFromFile();
        //default test account
        if (!userDatabase.containsKey("toby")){
            registerUser("toby", "password123");
        }
        //default admin account
        if (!userDatabase.containsKey("admin")){
            registerUser("admin", "admin123");
        }
    }
    //loads users from csv to the server 
    private static void loadUsersFromFile(){
        File file = new File(USERFILE);
        if (!file.exists())return; // if it doesnt exist then it skips

        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3){
                    String username = parts [0];
                    String hash = parts[1];
                    // decrypt the salt from text back into bytes
                    byte[] salt = Base64.getDecoder().decode(parts[2]);

                    userDatabase.put(username, new UserAccount(username, hash, salt));
                }
            }
            System.out.println("Loaded user database from " + USERFILE);
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }
    // save new user to csv file
    private static synchronized void saveUserToFile(String username, String hash, byte[] salt) {
        try (FileWriter fw = new FileWriter(USERFILE, true );
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw)){

            //encrypt the yte array salt into base64 string so it can be saved 
            String saltString = Base64.getEncoder().encodeToString(salt);
            out.println(username + "," + hash + "," + saltString);
        } catch (IOException e) {
            System.out.println("Error saving the user: " + e.getMessage());
        }
    }

    // Generates a random cryptographic salt
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    // Hashes the password with the salt using SHA-256
    public static String hashSHA256(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Hashing error", e);
        }
    }

    public static String registerUser (String username, String password){
        if (userDatabase.containsKey(username)){
            return "USERNAME TAKEN";
        }
        byte[] salt = generateSalt();
        String hashedPassword = hashSHA256(password, salt);

        userDatabase.put(username, new UserAccount(username, hashedPassword, salt));
        saveUserToFile(username, hashedPassword, salt);
        return "SUCCESS";
    }

    // The main authentication gateway
    public static String verifyLogin(String username, String passwordAttempt) {
        UserAccount user = userDatabase.get(username);
        
        if (user == null) {
            return "USER_NOT_FOUND";
        }

        if (user.isLocked()) {
            return "ACCOUNT_LOCKED";
        }

        String attemptHash = hashSHA256(passwordAttempt, user.getSalt());

        if (attemptHash.equals(user.getPasswordHash())) {
            user.resetAttempts();
            return "SUCCESS";
        } else {
            user.incrementFailedAttempts();
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setLockoutTimestamp(System.currentTimeMillis() + LOCKOUT_DURATION);
                return "ACCOUNT_LOCKED";
            }
            return "INVALID_PASSWORD";
        }
    }
}