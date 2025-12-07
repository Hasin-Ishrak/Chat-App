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
            "#FAD4D8", "#F5E0B7", "#E8F0C1","#F1C6E7","#DCC9B6","#F4D7A0"   
    };
    private int colorIndex=0;
    private String myUsername;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientGUI().start());
    }
    private void start() {
        JPanel loginPanel = new JPanel(new GridLayout(5, 5));
        JTextField ipField = new JTextField("SERVER_IP");
        JTextField nameField = new JTextField("User");

        loginPanel.add(new JLabel("Server IP:"));
        loginPanel.add(ipField);
        loginPanel.add(new JLabel("User Name:"));
        loginPanel.add(nameField);

        if (JOptionPane.showConfirmDialog(null, loginPanel, "Connect", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) return;

        myUsername=nameField.getText().trim();
        String host= ipField.getText().trim();

        buildGUI();
        frame.setVisible(true);

        try {
            socket = new Socket(host, 5050);
            in=IOUtil.readerFromSocket(socket);
            out=IOUtil.writerFromSocket(socket);

            in.readLine();    // server: "Enter your name:"
            out.println(myUsername);

            Thread reader = new Thread(() -> {
                try {
                    String line;
                    while ((line=in.readLine()) != null) {
                        appendMessage(line);
                    }
                } catch (IOException e) {
                    appendMessage("System: Disconnected from server.");
                }
            });
            reader.setDaemon(true);
            reader.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame,"Connection failed: "+ e.getMessage());
            frame.dispose();
        }
    }

    private void buildGUI() {
        frame = new JFrame("Chat -> " + myUsername);
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(153, 204,255 ));
        frame.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(new Color(204, 220, 255));
        messagesPanel.add(Box.createVerticalGlue());

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(160, 160, 160));

        inputField = new RoundedTextField(20); 
        sendButton = new OvalButton("Send");
         sendButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
         sendButton.setContentAreaFilled(false);
        sendButton.setOpaque(true);
        sendButton.setBorderPainted(false);
        sendButton.setBackground(new Color(66, 133, 244));
        sendButton.setForeground(Color.WHITE);
        sendButton.putClientProperty("JButton.buttonType", "roundRect");

        sendButton.addActionListener(e -> send());
        inputField.addActionListener(e -> send());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);
        bottom.setPreferredSize(new Dimension(frame.getWidth(), 50));

        frame.add(scrollPane, BorderLayout.CENTER);
        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); 
         bottomWrapper.add(bottom, BorderLayout.CENTER);
         frame.add(bottomWrapper, BorderLayout.SOUTH);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { closeConnection(); }
        });
    }

    private void send() {
        if (out==null) return;
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
                5,   
                2    
        ));
        wrapper.setBackground(new Color(180, 220, 255));
        wrapper.add(bubble);
        int insertIndex = messagesPanel.getComponentCount() - 1;
        messagesPanel.add(wrapper, insertIndex);

        messagesPanel.revalidate();
        messagesPanel.repaint();

        SwingUtilities.invokeLater(() -> {
       JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
      });
    });
}
    private void closeConnection() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    static class MessageBubble extends JPanel {

    public MessageBubble(String user, String text, Color color, boolean isMe) {
        setLayout(new BorderLayout());
        setBackground(color);
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); 
        setOpaque(false); 

        JLabel label = new JLabel("<html><b>" + user + ":</b> " + text + "</html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));

        add(label, BorderLayout.CENTER);
    }

    @Override
    public Dimension getPreferredSize() {
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
static class RoundedTextField extends JTextField {
    private int radius;

    public RoundedTextField(int radius) {
        super();
        this.radius = radius;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE); 
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.GRAY); 
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
    }
}
static class OvalButton extends JButton {
    public OvalButton(String text) {
        super(text);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        super.paintComponent(g);
    }

    @Override
    public boolean contains(int x, int y) {
        return new java.awt.geom.RoundRectangle2D.Float(
            0, 0, getWidth(), getHeight(), getHeight(), getHeight()
        ).contains(x, y);
    }
}


}
