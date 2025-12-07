package client_gui;

import common.IOUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatClientGUI {

    private JFrame frame;
    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JButton sendButton;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final Map<String, Color> userColors = new HashMap<>();
    private final String[] bubbleColors = {
            "#FFCDD2", "#C8E6C9", "#BBDEFB", "#FFE0B2", "#D1C4E9", "#B2EBF2"
    };
    private int colorIndex = 0;
    private String myUsername;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientGUI().start());
    }

    private void start() {
        JPanel loginPanel = new JPanel(new GridLayout(0, 1));
        JTextField ipField = new JTextField("<SERVER_IP>");
        JTextField nameField = new JTextField("User");

        loginPanel.add(new JLabel("Server IP:"));
        loginPanel.add(ipField);
        loginPanel.add(new JLabel("Your Name:"));
        loginPanel.add(nameField);

        if (JOptionPane.showConfirmDialog(null, loginPanel, "Connect", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) return;

        myUsername = nameField.getText().trim();
        String host = ipField.getText().trim();

        buildGUI();
        frame.setVisible(true);

        try {
            socket = new Socket(host, 5000);
            in = IOUtil.readerFromSocket(socket);
            out = IOUtil.writerFromSocket(socket);

            in.readLine();      // server: "Enter your name:"
            out.println(myUsername);

            Thread reader = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        appendMessage(line);
                    }
                } catch (IOException e) {
                    appendMessage("System: Disconnected from server.");
                }
            });

            reader.setDaemon(true);
            reader.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Connection failed: " + e.getMessage());
            frame.dispose();
        }
    }

    private void buildGUI() {
        frame = new JFrame("Chat - " + myUsername);
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(180, 220, 255)); // light skyblue

        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(new Color(180, 220, 255));
        messagesPanel.add(Box.createVerticalGlue());

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(180, 220, 255));

        inputField = new JTextField();
        sendButton = new JButton("Send");

        sendButton.addActionListener(e -> send());
        inputField.addActionListener(e -> send());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { closeConnection(); }
        });
    }

    private void send() {
        if (out == null) return;
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        out.println(text);
        inputField.setText("");
    }

  private void appendMessage(String msg) {
    SwingUtilities.invokeLater(() -> {
        String user = "System";
        String text = msg;

        if (msg.contains(":")) {
            int i = msg.indexOf(":");
            user = msg.substring(0, i).trim();
            text = msg.substring(i + 1).trim();
        }

        boolean isMe = user.equals(myUsername);

        if (!userColors.containsKey(user)) {
            userColors.put(user, Color.decode(
                bubbleColors[colorIndex++ % bubbleColors.length]
            ));
        }

        MessageBubble bubble = new MessageBubble(user, text, userColors.get(user), isMe);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new FlowLayout(
                isMe ? FlowLayout.RIGHT : FlowLayout.LEFT,
                5,   // horizontal gap
                2    // vertical gap
        ));
        wrapper.setBackground(new Color(180, 220, 255));
        wrapper.add(bubble);

        // ⭐ Insert message ABOVE the glue (so newest stays at bottom)
        int insertIndex = messagesPanel.getComponentCount() - 1;
        messagesPanel.add(wrapper, insertIndex);

        messagesPanel.revalidate();
        messagesPanel.repaint();

        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    });
}
    private void closeConnection() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    // --------------------------------------------------------------------

    static class MessageBubble extends JPanel {

    public MessageBubble(String user, String text, Color color, boolean isMe) {
        setLayout(new BorderLayout());
        setBackground(color);
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // padding
        setOpaque(false); // bubble drawn manually

        JLabel label = new JLabel("<html><b>" + user + ":</b> " + text + "</html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));

        add(label, BorderLayout.CENTER);
    }

    @Override
    public Dimension getPreferredSize() {
        // limit bubble width to look nice
        int maxWidth = 250;
        Dimension size = super.getPreferredSize();
        if (size.width > maxWidth) size.width = maxWidth;
        return size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        super.paintComponent(g);
    }
}

}
