// simply waits for a connection
// creates a ClientHandle
// and gives it to the pool to run
import java.net.*; 
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
public class Server {
    // pool of ten threads at a time
    private static final int MaxClients = 10;
    private static ExecutorService pool = Executors.newFixedThreadPool(MaxClients);

    //storing username and thread handling specific user
    public static ConcurrentHashMap<String, ClientHandle> activeUsers = new ConcurrentHashMap<>();// similar to a python dict
    public static void main(String[] args) {
    
        try (ServerSocket serverSocket = new ServerSocket(6666)){ // declaring in the try block java automatically closes
            System.out.println("Server started. Waiting for a client...");

            // server loops continiously to accept new connections
            while (true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("A new client Connected!");

                ClientHandle clientThread = new ClientHandle(clientSocket);
                pool.execute(clientThread);
            }

            }catch (IOException e){
                e.printStackTrace();
            }   
    
    }
    // loop through everyone in the map and sends them a message except the sender
    public static void routeGroupMessage (String message, String senderUsername) {
        for (ClientHandle client : activeUsers.values()) {
            if (!client.getUsername().equals(senderUsername)){
                client.sendMessageToClient(message);
            }
        }
    }
    //route a message to single specific client
    public static boolean routePrivateMessage(String message, String targetUsername) {
        ClientHandle targetClient = activeUsers.get(targetUsername);

        if (targetClient != null){
            targetClient.sendMessageToClient(message);
            return true;
        }
        return false;
    }
    public static boolean kickClient (String usernameToKick){
        ClientHandle targetClient = activeUsers.get(usernameToKick);

        if (targetClient != null){
            targetClient.sendMessageToClient(SecurityManager.encryptAES("Server: You have been kicked by the administrator"));
            targetClient.forceDisconnect();
            return true;
        }
        return false;
    }
    //removing user when disconencted
    public static void removeClient(String username){
        activeUsers.remove(username);
        System.out.println(username + " has left the server");

    }
}


