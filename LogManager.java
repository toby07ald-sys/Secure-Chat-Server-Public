import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogManager {
private static final String LOG_FILE = "ChatLogs.csv";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // synchronized makes allows for multiple threads
    public static synchronized void logActivity(String sender, String receiver, String content) {
        // fileWriter with 'true' append mode so we do not overwrite old logs
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            
            String timestamp = LocalDateTime.now().format(formatter);
            
            // strip commas out of messages for the csv storage
            String safeContent = content.replace(",", " "); 
            
            out.println(sender + "," + receiver + "," + safeContent + "," + timestamp);
            
        } catch (IOException e) {
            System.out.println("Log Error: " + e.getMessage());
        }
    }
    //new method for admin reads chatlogs and returns it as a list fo strings 
    public static synchronized List<String> getChatLogs(){
        List<String> logs = new ArrayList<>();
        File file = new File(LOG_FILE);

        if (!file.exists()){
            logs.add("no chat logs found");
            return logs;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = br.readLine()) != null ){
                logs.add(line);
            }
            
        } catch (IOException e) {
            logs.add("Error reading logs:" + e.getMessage());
        }
        return logs;

    }
}
