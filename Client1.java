import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Client1 {
    // key has to be 16 charecters long
    private static final byte[] AESKEY = "CourseworkKey123".getBytes();

    //client encryption 
    public static String encryptAES(String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(AESKEY, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception e) { return null; }
    }

    //client decryption
    public static String decryptAES (String encryptedData){
        try {
            SecretKeySpec secretKey = new SecretKeySpec(AESKEY, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
        } catch (Exception e) { return null; }
    }
    public static void main(String[] args) {
        try {
            // Connect to the server on the matching port (6666)
            Socket socket = new Socket("127.0.0.1", 6666);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner consoleInput = new Scanner(System.in);

            // Thread constantly runs reading server messages
            Thread listener = new Thread(new Runnable() {
                @Override 
                public void run() {
                    try{
                        String serverMessage;
                        //keep printing messages when they come 
                        while ((serverMessage = in.readLine()) != null) {
                            //try and decrypt message
                            String decrypted = decryptAES(serverMessage);
                            //if decrypted
                            if (decrypted != null){
                                System.out.println(decrypted);
                            } else {
                            System.out.println(serverMessage);
                            }
                        }
                        } catch (IOException e) {
                        }
                    }
                });
                //background listening thread
                listener.start();

                // main program handles typing
                String clientMessage;
                while (true){
                    clientMessage = consoleInput.nextLine();

                    //bye to leave
                    if (clientMessage.equalsIgnoreCase("leave")) {
                        out.println("leave");
                        System.out.println("====================================");
                        System.out.println("You have successfully left the chat.");
                        System.out.println("Closing connection...");
                        System.out.println("====================================");
                        break;
                    }
                    // encrypt before sends
                    String encryptedMessage = encryptAES(clientMessage);
                    out.println(encryptedMessage);
                }
                //clean up
                in.close();
                out.close();
                socket.close();
                consoleInput.close();

            } catch (IOException e) {
                System.err.println("could not connect to Server");
            }
        }
}   
           