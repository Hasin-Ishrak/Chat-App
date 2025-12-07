// Path: common/IOUtil.java
package common;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


/**
* Small I/O helpers so server & clients share consistent behavior.
*/
public final class IOUtil {
private IOUtil() {}


public static BufferedReader readerFromSocket(Socket s) throws java.io.IOException {
return new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
}


public static PrintWriter writerFromSocket(Socket s) throws java.io.IOException {
return new PrintWriter(s.getOutputStream(), true, StandardCharsets.UTF_8);
}
}