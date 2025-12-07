import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;
    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to chat server.");

            Thread readerThread = new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = in.readLine()) != null) {
                        System.out.println(serverMsg);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();
            String input;
            while ((input = console.readLine()) != null) {
                out.println(input);
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }
}
