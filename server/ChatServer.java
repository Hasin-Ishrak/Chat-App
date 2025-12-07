package server;


import common.IOUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChatServer {
private static final int PORT = 5000;
private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());


public static void main(String[] args) {
System.out.println("Chat Server started on port " + PORT);
try (ServerSocket serverSocket = new ServerSocket(PORT)) {
while (true) {
Socket socket = serverSocket.accept();
System.out.println("New client connected: " + socket.getRemoteSocketAddress());
ClientHandler handler = new ClientHandler(socket);
clients.add(handler);
new Thread(handler).start();
}
} catch (IOException e) {
System.err.println("Server error: " + e.getMessage());
}
}


public static void broadcast(String message) {
    synchronized (clients) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }
}

static class ClientHandler implements Runnable {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final String name;

    ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.in = IOUtil.readerFromSocket(socket);
        this.out = IOUtil.writerFromSocket(socket);

        out.println("Enter your name:");
        String n = in.readLine();
        if (n == null || n.trim().isEmpty()) n = "User" + socket.getPort();
        this.name = n.trim();

        System.out.println(name + " joined.");
        broadcast("System: " + name + " joined the chat!");
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(name + ": " + line);
                broadcast(name + ": " + line);  // send to everyone
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + name);
        } finally {
            try { socket.close(); } catch (IOException ignore) {}
            clients.remove(this);
            broadcast("System: " + name + " left the chat.");
        }
    }

    void sendMessage(String message) {
        out.println(message);
    }
}
}