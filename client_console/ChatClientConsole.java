package client_console;
import common.IOUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClientConsole {
private static final String HOST="SERVER_IP"; 
private static final int PORT=5050;

public static void main(String[] args) {
 String host = HOST;
 if (args.length >= 1) host=args[0];
  try (Socket socket=new Socket(host,PORT);
 BufferedReader console=new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
 BufferedReader in=IOUtil.readerFromSocket(socket);
 PrintWriter out=IOUtil.writerFromSocket(socket)) {

 System.out.println("Connected to chat server "+host+":"+PORT);
 String serverPrompt=in.readLine();
 System.out.println(serverPrompt);
 String name =console.readLine();
  out.println(name);

Thread reader=new Thread(() -> {
  try {
   String msg;
  while ((msg=in.readLine())!=null) {
   System.out.println(msg);
 }
} catch (IOException e) {
   System.out.println("Disconnected from server.");
}
});
   reader.setDaemon(true);
   reader.start();

  String input;
while ((input = console.readLine()) != null) {
   out.println(input);
}

} catch (IOException e) {
   System.err.println("Connection error: " + e.getMessage());
  }
 }
}