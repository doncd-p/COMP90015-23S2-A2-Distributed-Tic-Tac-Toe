/**
 * Author: Chenyang Dong
 * Student ID: 1074314
 */

import javax.swing.*;
import java.awt.event.*;
import java.util.HashMap;

public class GUI extends JFrame {
    private JPanel panelMain;
    private JButton buttonQuit;
    private JTextArea textAreaChat;
    private JTextField textFieldChat;
    private JTextField textFieldTimer;
    private JTextArea textAreaUser;
    private JTextArea textAreaStatus;
    private JButton button1;
    private JButton button7;
    private JButton button4;
    private JButton button2;
    private JButton button5;
    private JButton button8;
    private JButton button3;
    private JButton button6;
    private JButton button9;

    public GUI(TicTacToeClient ticTacToeClient) {
        setContentPane(panelMain);
        setTitle("Distributed Tic-Tac-Toe");
        setSize(900, 700);
        setLocationRelativeTo(null); // Center the frame on the screen
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        HashMap<String, JButton> cellMap = new HashMap<>();
        cellMap.put("1", button1);
        cellMap.put("2", button2);
        cellMap.put("3", button3);
        cellMap.put("4", button4);
        cellMap.put("5", button5);
        cellMap.put("6", button6);
        cellMap.put("7", button7);
        cellMap.put("8", button8);
        cellMap.put("9", button9);
        TicTacToeClient.cellMap = cellMap;

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ticTacToeClient.play(button1, "1");
            }
        });

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ticTacToeClient.play(button2, "2");
            }
        });

        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ticTacToeClient.play(button3, "3");
            }
        });

        button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ticTacToeClient.play(button4, "4");
            }
        });

        button5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ticTacToeClient.play(button5, "5");
            }
        });

        button6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ticTacToeClient.play(button6, "6");
            }
        });

        button7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ticTacToeClient.play(button7, "7");
            }
        });

        button8.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ticTacToeClient.play(button8, "8");
            }
        });

        button9.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ticTacToeClient.play(button9, "9");
            }
        });
        textFieldChat.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (textFieldChat.getText().equals("Type your message here")) {
                    textFieldChat.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (textFieldChat.getText().isEmpty()) {
                    textFieldChat.setText("Type your message here");
                }
            }
        });
        textFieldChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        buttonQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ticTacToeClient.quit();
            }
        });
        textFieldChat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String message = textFieldChat.getText().trim();
                    if (!message.isEmpty()) {
                        ticTacToeClient.chat(message);
                        textFieldChat.setText("");
                    }
                }
            }
        });
    }

    public void updateTextAreaUser(String userInfo) {
        textAreaUser.setText(userInfo);
    }

    public void updateTextAreaStatus(String newText) {
        textAreaStatus.setText(newText);
    }

    public void updateTextFieldTimer(int time) {
        textFieldTimer.setText(Integer.toString(time));
    }

    public void updateTextAreaChat(String newText) {
        if (newText != null && !newText.trim().isEmpty()) {
            String currentText = textAreaChat.getText();
            String[] lines = currentText.split("\n");

            // Keep the latest 10 messages (9 old message left)
            int linesToKeep = Math.min(9, lines.length);
            StringBuilder newTextBuilder = new StringBuilder();
            for (int i = lines.length - linesToKeep; i < lines.length; i++) {
                if (!lines[i].trim().isEmpty()) {
                    newTextBuilder.append(lines[i]);
                    if (i < lines.length - 1) {
                        newTextBuilder.append("\n");
                    }
                }
            }

            // Append the new message
            if (newTextBuilder.length() > 0) {
                newTextBuilder.append("\n");
            }
            newTextBuilder.append(newText);

            // Update the text area
            textAreaChat.setText(newTextBuilder.toString());
        }
    }

    public void resetTextAreaChat() {
        textAreaChat.setText("");
    }

    public void enableInteraction() {
        button1.setEnabled(true);
        button2.setEnabled(true);
        button3.setEnabled(true);
        button4.setEnabled(true);
        button5.setEnabled(true);
        button6.setEnabled(true);
        button7.setEnabled(true);
        button8.setEnabled(true);
        button9.setEnabled(true);

        textFieldChat.setEnabled(true);
    }

    public void disableInteraction() {
        disableCells();
        textFieldChat.setEnabled(false);
    }

    public void disableCells() {
        button1.setEnabled(false);
        button2.setEnabled(false);
        button3.setEnabled(false);
        button4.setEnabled(false);
        button5.setEnabled(false);
        button6.setEnabled(false);
        button7.setEnabled(false);
        button8.setEnabled(false);
        button9.setEnabled(false);
    }

    public void resetCell() {
        button1.setIcon(null);
        button2.setIcon(null);
        button3.setIcon(null);
        button4.setIcon(null);
        button5.setIcon(null);
        button6.setIcon(null);
        button7.setIcon(null);
        button8.setIcon(null);
        button9.setIcon(null);
    }
}
