import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 5000;
    private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    public static void main(String[] args) {
        System.out.println("Chat Server started on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
    public static void broadcast(String message, ClientHandler excludeClient) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != excludeClient) {
                    client.sendMessage(message);
                }
            }
        }
    }
    public static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String name = "User";
        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Enter your name:");
                String nameInput = in.readLine();
                if (nameInput != null && !nameInput.trim().isEmpty()) {
                    name = nameInput.trim();
                }
                System.out.println(name + " joined.");
                broadcast(name + " joined the chat!", this);

            } catch (IOException e) {
                System.out.println("Client setup error: " + e.getMessage());
            }
        }
        @Override
        public void run() {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    System.out.println(name + ": " + message);
                    broadcast(name + ": " + message, this);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + name);
            } finally {
                try { socket.close(); } catch (IOException ignore) {}
                clients.remove(this);
                broadcast(name + " left the chat.", this);
            }
        }
        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
