package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.Sender;

import onlinedicegameclient.Client;

public class Window extends Thread {
    private JFrame frame;
    public Client client;
    
    private ConcurrentLinkedQueue<String> incommingMessageQueue, outgoingMessageQueue;
    
    public Window(Client client,
            ConcurrentLinkedQueue<String> incommingMessageQueue,
            ConcurrentLinkedQueue<String> outgoingMessageQueue) {
        this.client = client;
        this.incommingMessageQueue = incommingMessageQueue;
        this.outgoingMessageQueue = outgoingMessageQueue;
        this.start();
    }
    
    @Override
    public void run() {
        JTextArea tf = makeUI();
        tf.append(Sender.HELP_TEXT);
        String p1, p2;
        int failed = 0;
        do {
            if(failed > 0) {
                tf.append("\nUnknown host - Try again");
            }
            p1 = null;
            p2 = null;
            tf.append("\n\nEnter IP of server: ");
            while(p1 == null) {
                if(!outgoingMessageQueue.isEmpty()) {
                    p1 = outgoingMessageQueue.poll();
                    tf.append(p1);
                }
            }
            tf.append("\nEnter username: ");
            while(p2 == null) {
                if(!outgoingMessageQueue.isEmpty()) {    
                    p2 = outgoingMessageQueue.poll();
                    tf.append(p2);
                }
            }
            failed++;
        } while (!client.makeConnection(new String[] {p1, p2}));
        tf.append("\n");
        while(true) {
            if(!incommingMessageQueue.isEmpty()) {
                String input = incommingMessageQueue.poll();
                if(input != null) {
                    tf.append("\n" + input);
                    tf.setCaretPosition(tf.getDocument().getLength());
                }
            }
        }
    }
    
    public JTextArea makeUI() {
        frame = new JFrame();
        frame.setTitle("DiceGame Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        JTextArea textField = new JTextArea(40,40);
        textField.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textField);
        frame.add(scrollPane,BorderLayout.CENTER);
        JPanel pane = new JPanel();
        pane.setLayout(new FlowLayout());
        JTextField inputField = new JTextField(50);
        pane.add(inputField);
        JButton send = new JButton("SEND");
        send.addActionListener( e -> {
            String data = inputField.getText();
            outgoingMessageQueue.add(data);
            inputField.setText("");
            inputField.requestFocus();
        });
        frame.getRootPane().setDefaultButton(send);
        pane.add(send);
        frame.add(pane,BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        inputField.requestFocus();
        return textField;
    }
}
