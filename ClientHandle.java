// ClientHandle logic flow:
//The Server waits for a connection.
//Client 1 connects.
//The Server creates a new ClientHandle thread specifically for Client 1 and gives it to the ExecutorService to run.
//The Server immediately loops back to step 1 to wait for Client 2, while Client 1 chats in the background on its own thread.
import java.io.*;
import java.net.*;

public class ClientHandle implements Runnable { // must contain a run method - like signing a contract
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    //SecurityManager and username variables here later
    public ClientHandle(Socket socket) {
        this.clientSocket = socket;
    }
    // brings server messages so they can send properly to clients
    public String getUsername(){
        return username;
    }
    public void sendMessageToClient(String msg){
        out.println(msg);
    }

    // Closes the socket from the server side, forcing the thread to shut down
    public void forceDisconnect() {
        try {
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error disconnecting client.");
        }
    }

    @Override // gives an error before code compiles
    public void run(){
        try{
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // authenticate / register 
            out.println("Welcome! Type 1 to login or 2 to Register:");
            //instantly decrypt 
            String choice = SecurityManager.decryptAES(in.readLine());

            // asking user for username
            out.println("Enter your Username: ");
            //instantly decrypt
            username = SecurityManager.decryptAES(in.readLine());

            if (username == null || username.contains(",")){
                out.println("Server: Invalid username. Commas not allowed");
                return; // drops connection
            }
            out.println("Enter your password:");
            String password = SecurityManager.decryptAES(in.readLine());

            if (choice != null && choice.trim().equals("2")){
                //Registration
                String regStatus = SecurityManager.registerUser(username, password);
                if (!regStatus.equals("SUCCESS")){
                    out.println("Registration Failed " + regStatus);
                    return;
                }
                out.print("Registration Successful. Logging in...");
            }else{
                //login
                String authStatus = SecurityManager.verifyLogin(username, password);
                if (!authStatus.equals("SUCCESS")){
                     //tells them why they have been kicked
                    out.println("Authentication failed:" + authStatus);
                    LogManager.logActivity("System", username, "Failed Login Attempt" + authStatus);
                    return;
                }
            }
            // login success 
            //add to hashmap and announce to server
            Server.activeUsers.put(username, this);
            LogManager.logActivity("System", "All", username + " has joined the server.");
            
            // Announce to the group 
            Server.routeGroupMessage(SecurityManager.encryptAES(username + " has joined the chat!"), username);
            out.println("Welcome to the server! Type 'leave' to exit.");
            out.println("Tip: To send a private message, type: /msg username message");

            // admin logic
            if (username.equalsIgnoreCase("admin")) {

                out.println(SecurityManager.encryptAES("ADMIN AUTHENTICATED: Fetching Server Logs..."));
 
                
                // Get the logs from LogManager and send them securely to the admin
                java.util.List<String> logs = LogManager.getChatLogs();
                for (String logLine : logs) {
                    out.println(SecurityManager.encryptAES(logLine));
                }
                
                out.println(SecurityManager.encryptAES("END OF LOGS. Entering chat mode."));

            }
            

            String clientMessage;
            
            // main chat
            while ((clientMessage = in.readLine()) != null) {
                if (clientMessage.equalsIgnoreCase("leave")){
                    break;
                }
                
                String decryptedMessage = SecurityManager.decryptAES(clientMessage);
                if (decryptedMessage == null || decryptedMessage.contains(",")) {
                    out.println(SecurityManager.encryptAES("Server: Message refused. Commas or invalid encryption detected."));
                    continue; 
                }

                //  online users
                if (decryptedMessage.trim().equalsIgnoreCase("/who")) {
                    String onlineUsers = String.join(", ", Server.activeUsers.keySet());
                    out.println(SecurityManager.encryptAES("Server: Online users (" + Server.activeUsers.size() + "): " + onlineUsers));
                    continue; 
                }
                

                // admin kick
                if (decryptedMessage.trim().startsWith("/kick ")) {
                    if (username.equalsIgnoreCase("admin")) {
                        // Extracts the username they typed after "/kick "
                        String targetUser = decryptedMessage.trim().substring(6);
                        
                        LogManager.logActivity(username, targetUser, "ADMIN KICK EXECUTED");
                        
                        boolean kicked = Server.kickClient(targetUser);
                        if (kicked) {
                            out.println(SecurityManager.encryptAES("Server: Successfully kicked " + targetUser));
                        } else {
                            out.println(SecurityManager.encryptAES("Server: User '" + targetUser + "' not found."));
                        }
                    } else {
                        // Standard users get a permission denied message
                        out.println(SecurityManager.encryptAES("Server: Permission denied. You do not have admin rights."));
                    }
                    continue; 
                }

                // private messaging logic
                if (decryptedMessage.trim().startsWith("/msg ")) {
                    String[] parts = decryptedMessage.trim().split("\\s+", 3);
                    
                    if (parts.length == 3) {
                        String targetUser = parts[1]; 
                        String privateMessage = parts[2];
                        
                        LogManager.logActivity(username, targetUser, privateMessage);

                        String formattedPrivate = "(Private) " + username + " whispers: " + privateMessage;
                        boolean success = Server.routePrivateMessage(SecurityManager.encryptAES(formattedPrivate), targetUser);
                        
                        if (!success) {
                            out.println(SecurityManager.encryptAES("Server: User '" + targetUser + "' is not online."));
                        } else {
                            out.println(SecurityManager.encryptAES("You whispered to " + targetUser + ": " + privateMessage));
                        }
                    } else {
                        out.println(SecurityManager.encryptAES("Server: Invalid command format. Use: /msg username message"));
                    }
                    continue; 
                } 
                
                System.out.println(username + ": " + decryptedMessage);
                
                LogManager.logActivity(username, "All", decryptedMessage);
                
                String outgoingMessage = username + " says: " + decryptedMessage;
                Server.routeGroupMessage(SecurityManager.encryptAES(outgoingMessage), username); 
            }
            
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            // remove them from the hashmap and announce they left
            if (username != null && Server.activeUsers.containsKey(username)) {
                Server.removeClient(username);
                LogManager.logActivity("System", "All", username + " has disconnected.");
                Server.routeGroupMessage(SecurityManager.encryptAES(username + " has disconnected."), username);
            }
            //clean up resources - force closes everything - stops everything from crashing
            try {
                if (in != null) in.close(); 
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            }catch (IOException e) {
                e.printStackTrace(); // tells you where the problem is @w3schools
            }
        }
    }
}