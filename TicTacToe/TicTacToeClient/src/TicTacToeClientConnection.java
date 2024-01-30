/**
 * Author: Chenyang Dong
 * Student ID: 1074314
 */

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class TicTacToeClientConnection extends Thread {
    public TicTacToeClient ticTacToeClient;

    class HeartbeatThread extends Thread {
        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    TicTacToeClient.output.write("heartbeat");
                    TicTacToeClient.output.newLine();
                    TicTacToeClient.output.flush();
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                System.out.println("Server not available. Exit in 5 seconds.");
                ticTacToeClient.gui.updateTextAreaStatus("Server not available.\nExit in 5 seconds.");
                try {
                    Thread.sleep(5000); // Exit the crashed server after 5 seconds
                } catch (Exception err) {
                    // err.printStackTrace();
                    System.out.println("Exception: " + err);
                }
                System.exit(0);
            }
        }
    }

    static class Countdown {
        TicTacToeClient ticTacToeClient;

        Countdown(TicTacToeClient ticTacToeClient) {
            this.ticTacToeClient = ticTacToeClient;
        }

        int seconds = 20;
        boolean cancelled = false;
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (seconds > 0 && !cancelled) {
                    System.out.println("Time to pass left: " + seconds + "s");
                    seconds--;
                    ticTacToeClient.gui.updateTextFieldTimer(seconds);
                } else if (cancelled) {
                    System.out.println("Play took " + (20 - seconds) + "s");
                    seconds = 20;
                    ticTacToeClient.gui.updateTextFieldTimer(seconds);
                    timer.cancel();
                } else {
                    System.out.println("Time's up");
                    ticTacToeClient.randomPlay();
                    seconds = 20;
                    ticTacToeClient.gui.updateTextFieldTimer(seconds);
                    timer.cancel();
                }
            }
        };

        public void start() {
            timer.scheduleAtFixedRate(task, 0, 1000);
        }
    }

    public TicTacToeClientConnection(TicTacToeClient ticTacToeClient) {
        this.ticTacToeClient = ticTacToeClient;
    }

    @Override
    public void run() {
        try {
            ticTacToeClient.newPlayer();
            HeartbeatThread heartbeat = new HeartbeatThread();
            heartbeat.start();

            // Wait until the true response
            String response;
            while ((response = TicTacToeClient.input.readLine()) != null) {
                if (response.contains("heartbeat")) {
                    // System.out.println("heartbeat");
                    continue;
                } else {
                    System.out.println("Message from server: " + response);

                    // Parse the response as JSON
                    JSONParser parser = new JSONParser();
                    try {
                        JSONObject jsonResponse = (JSONObject) parser.parse(response);
                        String type = jsonResponse.get("type").toString();
                        if (jsonResponse.containsKey("matchId")) {
                            TicTacToeClient.matchId = Integer.parseInt(jsonResponse.get("matchId").toString());
                        }
                        if (jsonResponse.containsKey("playerRank")) {
                            TicTacToeClient.rank = Integer.parseInt(jsonResponse.get("playerRank").toString());
                        }
                        if (jsonResponse.containsKey("playerSymbol")) {
                            TicTacToeClient.symbol = jsonResponse.get("playerSymbol").toString();
                        }
                        if (jsonResponse.containsKey("opponentUsername")) {
                            TicTacToeClient.opponent.username = jsonResponse.get("opponentUsername").toString();
                        }
                        if (jsonResponse.containsKey("opponentRank")) {
                            TicTacToeClient.opponent.rank = Integer
                                    .parseInt(jsonResponse.get("opponentRank").toString());
                        }
                        if (jsonResponse.containsKey("opponentSymbol")) {
                            TicTacToeClient.opponentSymbol = jsonResponse.get("opponentSymbol").toString();
                        }

                        switch (type) {
                            case "player" -> {
                                System.out.println("Joined the TicTacToe game.");
                                ticTacToeClient.gui.updateTextAreaUser(String.format("Welcome %s!\nRANK#%d",
                                        TicTacToeClient.username, TicTacToeClient.rank));
                            }
                            case "new match" -> {
                                ticTacToeClient.gui.enableInteraction();
                                ticTacToeClient.gui.updateTextAreaUser(String.format("Welcome %s!\nRANK#%d",
                                        TicTacToeClient.username, TicTacToeClient.rank));

                                // Player with symbol O plays first
                                if (TicTacToeClient.symbol.equals("O")) {
                                    TicTacToeClient.isMyTurn = true;
                                    TicTacToeClient.countdown = new Countdown(ticTacToeClient);
                                    TicTacToeClient.countdown.start();
                                    System.out
                                            .println(TicTacToeClient.username + "'s symbol: " + TicTacToeClient.symbol);
                                    ticTacToeClient.gui.updateTextAreaStatus(String.format("RANK#%d %s's turn(%s)",
                                            TicTacToeClient.rank, TicTacToeClient.username, TicTacToeClient.symbol));
                                } else {
                                    TicTacToeClient.isMyTurn = false;
                                    System.out
                                            .println(TicTacToeClient.username + "'s symbol: " + TicTacToeClient.symbol);
                                    ticTacToeClient.gui.updateTextAreaStatus(
                                            String.format("RANK#%d %s's turn(%s)", TicTacToeClient.opponent.rank,
                                                    TicTacToeClient.opponent.username, TicTacToeClient.opponentSymbol));
                                }
                            }
                            case "old match" -> {
                                String playsX = jsonResponse.get("playsX").toString();
                                String playsO = jsonResponse.get("playsO").toString();

                                ticTacToeClient.resumeMatch(playsX, playsO);
                            }
                            case "play" -> {
                                String position = jsonResponse.get("position").toString();
                                ticTacToeClient.setPlay(position);
                            }
                            case "chat" -> {
                                String text = jsonResponse.get("text").toString();
                                String opponentUsername = jsonResponse.get("opponentUsername").toString();
                                int opponentRank = Integer.parseInt(jsonResponse.get("opponentRank").toString());
                                ticTacToeClient.gui.updateTextAreaChat(
                                        String.format("RANK#%d %s: %s", opponentRank, opponentUsername, text));
                            }
                            case "over" -> {
                                TicTacToeClient.rank = Integer.parseInt(jsonResponse.get("new rank").toString());
                                ticTacToeClient.gui.disableInteraction();
                                if (TicTacToeClient.countdown != null) {
                                    TicTacToeClient.countdown.cancelled = true;
                                }
                                String winner = jsonResponse.get("winner").toString();
                                if (winner.equals(TicTacToeClient.username)) {
                                    ticTacToeClient.gui.updateTextAreaStatus(String.format("Player %s wins!", winner));

                                    int overDialog = JOptionPane.showConfirmDialog(ticTacToeClient.gui,
                                            "You win! Find a new match?", "WIN", JOptionPane.YES_NO_OPTION);
                                    ticTacToeClient.overOption(overDialog);
                                } else if (winner.equals("draw")) {
                                    ticTacToeClient.gui.updateTextAreaStatus("Match Drawn");

                                    int overDialog = JOptionPane.showConfirmDialog(ticTacToeClient.gui,
                                            "Draw! Find a new match?", "DRAW", JOptionPane.YES_NO_OPTION);
                                    ticTacToeClient.overOption(overDialog);
                                } else {
                                    ticTacToeClient.gui.updateTextAreaStatus(String.format("Player %s wins!", winner));

                                    int overDialog = JOptionPane.showConfirmDialog(ticTacToeClient.gui,
                                            "You lose! Find a new match?", "LOSE", JOptionPane.YES_NO_OPTION);
                                    ticTacToeClient.overOption(overDialog);
                                }
                            }
                            case "wait" -> {
                                ticTacToeClient.gui.updateTextAreaStatus("Waiting for opponent to reconnect...");
                                ticTacToeClient.gui.disableInteraction();
                                if (TicTacToeClient.countdown != null) {
                                    TicTacToeClient.countdown.cancelled = true;
                                }
                                ticTacToeClient.gui.updateTextFieldTimer(20);
                            }
                            case "resume" -> {
                                ticTacToeClient.gui.enableInteraction();

                                String playsX = jsonResponse.get("playsX").toString();
                                String playsO = jsonResponse.get("playsO").toString();

                                char[] x = playsX.toCharArray();
                                char[] o = playsO.toCharArray();

                                // Disable cells that have been clicked
                                HashMap<String, JButton> cellMap = TicTacToeClient.cellMap;
                                for (char buttonNumber : x) {
                                    cellMap.get(String.valueOf(buttonNumber)).setEnabled(false);
                                }
                                for (char buttonNumber : o) {
                                    cellMap.get(String.valueOf(buttonNumber)).setEnabled(false);
                                }

                                if (((x.length < o.length) && TicTacToeClient.symbol.equals("X"))
                                        || ((x.length == o.length) && TicTacToeClient.symbol.equals("O"))) {
                                    ticTacToeClient.gui.updateTextAreaStatus(String.format("RANK#%d %s's turn(%s)",
                                            TicTacToeClient.rank, TicTacToeClient.username, TicTacToeClient.symbol));
                                    TicTacToeClient.isMyTurn = true;
                                    TicTacToeClient.countdown = new TicTacToeClientConnection.Countdown(
                                            ticTacToeClient);
                                    TicTacToeClient.countdown.start();
                                } else {
                                    ticTacToeClient.gui.updateTextAreaStatus(
                                            String.format("RANK#%d %s's turn(%s)", TicTacToeClient.opponent.rank,
                                                    TicTacToeClient.opponent.username, TicTacToeClient.opponentSymbol));
                                }

                            }
                            default -> System.out.println("Message type: " + type);
                        }

                    } catch (ParseException e) {
                        // Handle parsing error
                        // e.printStackTrace();
                        System.out.println("ParseException: " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("Exception: " + e.getMessage());
        }
    }

}
