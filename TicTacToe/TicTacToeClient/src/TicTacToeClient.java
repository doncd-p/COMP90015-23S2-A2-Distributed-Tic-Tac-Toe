/**
 * Author: Chenyang Dong
 * Student ID: 1074314
 */

import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class TicTacToeClient {

    public static String username;
    public static String ip;
    public static int port;

    public static BufferedReader input;

    public static BufferedWriter output;

    public static int matchId;
    public static int rank;

    public static String symbol = "";

    public static String opponentSymbol = "";

    public static boolean isMyTurn;

    public GUI gui;

    public static Player opponent = new Player();

    public static HashMap<String, JButton> cellMap = new HashMap<>();

    public static TicTacToeClientConnection.Countdown countdown;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Please provide the required arguments with <username> <server address> <port>.");
            System.exit(1);
        }
        username = args[0];
        ip = args[1];
        port = 4444;
        try {
            port = Integer.parseInt(args[2]);
            // Validate port number
            if (port < 0 || port > 65535) {
                System.err.println("Invalid port number. Port must be between 0 and 65535.");
                System.exit(1);
            }
        } catch (NumberFormatException e) {
            // e.printStackTrace();
            System.out.println("Exception: " + e.getMessage());
            System.exit(1);
        }

        Socket socket;
        try {
            socket = new Socket(ip, port);
            // Get the input/output streams for reading/writing data from/to the socket
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("IOException: " + e.getMessage());
        }

        EventQueue.invokeLater(() -> {
            try {
                new TicTacToeClient();
            } catch (Exception e) {
                // e.printStackTrace();
                System.out.println("Exception: " + e.getMessage());
            }
        });
    }

    public TicTacToeClient() {
        gui = new GUI(this); // Initialize the GUI
        gui.updateTextAreaUser(String.format("Welcome %s!\nRANK#%d", username, rank));
        TicTacToeClientConnection connection = new TicTacToeClientConnection(this);
        connection.start();
    }

    public void communicate(String sendData) {
        try {
            System.out.println("Sent Data: " + sendData);
            output.write(sendData);
            output.newLine();
            output.flush();
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public void newPlayer() {
        JSONObject request = new JSONObject();
        request.put("type", "player");
        request.put("username", username);

        communicate(request.toJSONString());
    }

    public void play(JButton cell, String position) {
        JSONObject request = new JSONObject();
        request.put("type", "play");
        request.put("username", username);
        request.put("matchId", matchId);
        request.put("symbol", symbol);
        request.put("position", position);

        if (isMyTurn) {
            addIcon(cell, symbol);

            gui.updateTextAreaStatus(
                    String.format("RANK#%d %s's turn(%s)", opponent.rank, opponent.username, opponentSymbol));
            communicate(request.toJSONString());
        }

        isMyTurn = false;
        if (countdown != null) {
            countdown.cancelled = true;
        }
    }

    public void randomPlay() {
        if (isMyTurn) {
            ArrayList<String> availablePositions = new ArrayList<>();

            // Find all available positions
            for (int i = 1; i <= 9; i++) {
                String position = Integer.toString(i);
                JButton cell = cellMap.get(position);
                if (cell.isEnabled()) {
                    availablePositions.add(position);
                }
            }

            // Shuffle the list of available positions
            Collections.shuffle(availablePositions);

            if (!availablePositions.isEmpty()) {
                String position = availablePositions.get(0);
                JButton cell = cellMap.get(position);
                play(cell, position);
            }
        }
    }

    public void setPlay(String position) {
        JButton cell = cellMap.get(position);
        addIcon(cell, opponentSymbol);

        gui.updateTextAreaStatus(String.format("RANK#%d %s's turn(%s)", rank, username, symbol));
        isMyTurn = true;
        countdown = new TicTacToeClientConnection.Countdown(this);
        countdown.start();
    }

    public void addIcon(JButton cell, String symbol) {
        // Load the image based on the symbol
        InputStream inputStream = this.getClass().getResourceAsStream(symbol + ".png");
        ImageIcon icon = null;
        if (inputStream != null) {
            try {
                BufferedImage image = ImageIO.read(inputStream);
                icon = new ImageIcon(image);
            } catch (IOException e) {
                // e.printStackTrace();
                System.out.println("IOException: " + e.getMessage());
            }
        } else {
            System.out.println("Resource not found for " + symbol);
        }

        // Get the button's dimensions
        int buttonWidth = cell.getWidth();
        int buttonHeight = cell.getHeight();

        // Scale the image to fit the button's size
        Image scaledImage = icon.getImage().getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_SMOOTH);

        // Set the scaled image as the button's icon
        cell.setIcon(new ImageIcon(scaledImage));
        cell.setEnabled(false);
    }

    public void chat(String text) {
        JSONObject request = new JSONObject();
        request.put("type", "chat");
        request.put("username", username);
        request.put("matchId", matchId);
        request.put("rank", rank);
        request.put("symbol", symbol);
        request.put("text", text);

        communicate(request.toJSONString());

        gui.updateTextAreaChat(String.format("RANK#%d %s: %s", rank, username, text));
    }

    public void quit() {
        JSONObject request = new JSONObject();
        request.put("type", "quit");
        request.put("username", username);
        request.put("matchId", matchId);
        request.put("symbol", symbol);

        communicate(request.toJSONString());
        System.exit(0);
    }

    public void overOption(int overDialog) {
        if (overDialog == JOptionPane.YES_OPTION) {
            refreshInfo();
            newPlayer();
        } else {
            JSONObject request = new JSONObject();
            request.put("type", "quit");
            request.put("username", TicTacToeClient.username);
            request.put("matchId", -1);
            request.put("symbol", "");

            communicate(request.toJSONString());
            System.exit(0);
        }
    }

    public void resumeMatch(String playsX, String playsO) {
        gui.enableInteraction();
        gui.updateTextAreaUser(String.format("Welcome %s!\nRANK#%d",
                TicTacToeClient.username, TicTacToeClient.rank));

        char[] x = playsX.toCharArray();
        char[] o = playsO.toCharArray();
        for (char position : x) {
            JButton cell = cellMap.get(Character.toString(position));
            addIcon(cell, "X");
        }
        for (char position : o) {
            JButton cell = cellMap.get(Character.toString(position));
            addIcon(cell, "O");
        }

        if (((x.length < o.length) && symbol.equals("X")) || ((x.length == o.length) && symbol.equals("O"))) {
            gui.updateTextAreaStatus(String.format("RANK#%d %s's turn(%s)", rank, username, symbol));
            TicTacToeClient.isMyTurn = true;
            countdown = new TicTacToeClientConnection.Countdown(this);
            countdown.start();
        } else {
            gui.updateTextAreaStatus(
                    String.format("RANK#%d %s's turn(%s)", opponent.rank, opponent.username, opponentSymbol));
        }
    }

    public void refreshInfo() {
        symbol = "";
        opponentSymbol = "";
        opponent = new Player();
        gui.updateTextAreaUser(String.format("Welcome %s!\nRANK#%d", username, rank));
        gui.updateTextAreaStatus("Finding Player...");
        gui.resetTextAreaChat();
        gui.resetCell();
    }

}
