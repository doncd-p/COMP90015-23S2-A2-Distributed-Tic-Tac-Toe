/**
 * Author: Chenyang Dong
 * Student ID: 1074314
 */

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class TicTacToeServerConnection extends Thread {
    public BufferedReader input;
    public BufferedWriter output;
    public MatchData matchData;
    public Countdown countdown;

    private final Random random = new Random();

    class HeartbeatThread extends Thread {
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    output.write("heartbeat");
                    output.newLine();
                    output.flush();
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                // e.printStackTrace();
                System.out.println("Exception: " + e.getMessage());
                countdown = new Countdown();
                countdown.start();
            }
        }
    }

    class Countdown {
        int seconds = 30;
        boolean cancelled = false;
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (matchData != null && matchData.isResumed) {
                    timer.cancel();
                    cancelled = true;
                } else if (seconds > 0 && !cancelled) {
                    System.out.println("Time to reconnect left: " + seconds + "s");
                    seconds--;

                    JSONObject overResponse = new JSONObject();
                    overResponse.put("type", "wait");

                    try {
                        matchData.playerX.output.write(overResponse.toJSONString());
                        matchData.playerX.output.newLine();
                        matchData.playerX.output.flush();
                    } catch (IOException e) {
                        // e.printStackTrace();
                        System.out.println("IOException: " + e.getMessage());
                    } catch (Exception e) {
                        // e.printStackTrace();
                        System.out.println("Exception: " + e.getMessage());
                    }

                    try {
                        matchData.playerO.output.write(overResponse.toJSONString());
                        matchData.playerO.output.newLine();
                        matchData.playerO.output.flush();
                    } catch (IOException e) {
                        // e.printStackTrace();
                        System.out.println("IOException: " + e.getMessage());
                    } catch (Exception e) {
                        // e.printStackTrace();
                        System.out.println("Exception: " + e.getMessage());
                    }

                } else {
                    System.out.println("Timeout to reconnect");
                    timer.cancel();
                    cancelled = true;

                    Player playerX = TicTacToeGame.players.get(matchData.playerX.username);
                    Player playerO = TicTacToeGame.players.get(matchData.playerO.username);
                    playerX.rank += 2;
                    playerO.rank += 2;
                    TicTacToeGame.players.put(matchData.playerX.username, playerX);
                    TicTacToeGame.players.put(matchData.playerO.username, playerO);

                    JSONObject overResponse = new JSONObject();
                    overResponse.put("type", "over");
                    overResponse.put("winner", "draw");

                    try {
                        overResponse.put("new rank", playerX.rank);
                        matchData.playerX.output.write(overResponse.toJSONString());
                        matchData.playerX.output.newLine();
                        matchData.playerX.output.flush();
                    } catch (IOException e) {
                        // e.printStackTrace();
                        System.out.println("IOException: " + e.getMessage());
                    }

                    try {
                        overResponse.put("new rank", playerO.rank);
                        matchData.playerO.output.write(overResponse.toJSONString());
                        matchData.playerO.output.newLine();
                        matchData.playerO.output.flush();
                    } catch (IOException e) {
                        // e.printStackTrace();
                        System.out.println("IOException: " + e.getMessage());
                    }

                    TicTacToeGame.matches.remove(matchData.matchId);
                    matchData = null;
                }
            }
        };

        public void start() {
            if (matchData != null) {
                if (!matchData.isOver) {
                    matchData.isResumed = false;
                    System.out.println("New timer for 30s reconnection starts");
                    timer.scheduleAtFixedRate(task, 0, 1000);
                } else {
                    matchData = null;
                }
            } else {
                Player player = TicTacToeGame.waitingPool.poll();
                if (player != null) {
                    System.out.println("Kick player " + player.username + " out of waiting room");
                }
            }
        }
    }

    public TicTacToeServerConnection(Socket socket) throws IOException {
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public void run() {
        System.out.println("Connection starts");
        String receivedData;
        try {
            while ((receivedData = input.readLine()) != null) {
                if (!receivedData.contains("heartbeat")) {
                    System.out.println("Message from client: " + receivedData);

                    JSONParser parser = new JSONParser();
                    // Attempt to convert received data to JSON
                    JSONObject request = (JSONObject) parser.parse(receivedData);
                    handleRequest(request);
                }
            }
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("IOException: " + e.getMessage());
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public void handleRequest(JSONObject request) {
        String type = request.get("type").toString();
        String username = request.get("username").toString();

        switch (type) {
            case "player" -> {

                try {
                    matchData = null;
                    for (Map.Entry<Integer, MatchData> match : TicTacToeGame.matches.entrySet()) {
                        if (username.equals(match.getValue().playerX.username)
                                || username.equals(match.getValue().playerO.username)) {
                            matchData = match.getValue();
                            break;
                        }
                    }

                    // Check if it is a new match
                    if (matchData == null) {
                        Player player;
                        player = TicTacToeGame.players.get(username);

                        // Check if the player exists
                        if (player != null) {
                            player.output = output;
                        } else {
                            player = new Player(username, output);
                            TicTacToeGame.players.put(username, player);
                            System.out.println("New Player " + player.username + " added to TicTacToe game");
                        }

                        // Check if there is any player in the waiting room
                        if (TicTacToeGame.waitingPool.isEmpty()) {
                            TicTacToeGame.waitingPool.add(player);

                            JSONObject response = new JSONObject();
                            response.put("type", "player");
                            response.put("playerRank", player.rank);
                            sendMessage(output, response.toJSONString());
                            System.out.println("Player " + player.username + " added to waiting room");
                        } else {
                            Player opponent = TicTacToeGame.waitingPool.poll();
                            int matchId = TicTacToeGame.generateMatchId();
                            System.out.printf("Match #%s between %s and %s started%n", matchId, player.username,
                                    opponent.username);

                            // Determine the player's symbol (who plays first) randomly
                            String playerSymbol = random.nextBoolean() ? "O" : "X";
                            String opponentSymbol = playerSymbol.equals("O") ? "X" : "O";

                            MatchData newMatchData;
                            if (playerSymbol.equals("X")) {
                                // MatchData(matchId, playerX, playO)
                                newMatchData = new MatchData(matchId, player, opponent);
                            } else {
                                newMatchData = new MatchData(matchId, opponent, player);
                            }
                            matchData = newMatchData;

                            // Create a response JSON object
                            JSONObject responseToPlayer = createMatchResponse(player, opponent, playerSymbol, matchId);
                            JSONObject responseToOpponent = createMatchResponse(opponent, player, opponentSymbol,
                                    matchId);

                            // Send the response to both players
                            sendMessage(player.output, responseToPlayer.toString());
                            sendMessage(opponent.output, responseToOpponent.toString());

                            TicTacToeGame.matches.put(matchId, newMatchData);
                        }
                    } else { // old match waiting reconnection
                        System.out.printf("Match #%s between %s and %s resumed%n", matchData.matchId,
                                matchData.playerO.username, matchData.playerX.username);
                        String symbol;
                        matchData.isResumed = true;

                        Player player = new Player(username, output);
                        Player opponent;
                        MatchData newMatchData = matchData;
                        int rank;
                        if (matchData.playerO.username.equals(username)) {
                            symbol = "O";
                            opponent = matchData.playerX;
                            rank = matchData.playerO.rank;
                            newMatchData.playerO = player;
                            newMatchData.playerO.rank = rank;
                        } else {
                            symbol = "X";
                            opponent = matchData.playerO;
                            rank = matchData.playerX.rank;
                            newMatchData.playerX = player;
                            newMatchData.playerX.rank = rank;
                        }
                        TicTacToeGame.matches.put(matchData.matchId, newMatchData);

                        JSONObject response = new JSONObject();
                        try {
                            response.put("type", "old match");
                            response.put("matchId", matchData.matchId);
                            response.put("playerUsername", username);
                            response.put("playerRank", rank);
                            response.put("playerSymbol", symbol);
                            response.put("opponentUsername", opponent.username);
                            response.put("opponentRank", opponent.rank);
                            response.put("opponentSymbol", symbol.equals("O") ? "X" : "O");
                            response.put("playsO", matchData.getPositions("O"));
                            response.put("playsX", matchData.getPositions("X"));

                            sendMessage(player.output, response.toJSONString());
                        } catch (Exception e) {
                            // e.printStackTrace();
                            System.out.println("Exception: " + e.getMessage());
                        }

                        JSONObject responseToOpponent = new JSONObject();
                        try {
                            responseToOpponent.put("type", "resume");
                            response.put("playerSymbol", symbol.equals("O") ? "X" : "O");
                            responseToOpponent.put("playsO", matchData.getPositions("O"));
                            responseToOpponent.put("playsX", matchData.getPositions("X"));

                            sendMessage(opponent.output, responseToOpponent.toJSONString());
                        } catch (Exception e) {
                            // e.printStackTrace();
                            System.out.println("Exception: " + e.getMessage());
                        }
                    }
                    HeartbeatThread heartbeat = new HeartbeatThread();
                    heartbeat.start();
                } catch (Exception e) {
                    // e.printStackTrace();
                    System.out.println("Exception: " + e.getMessage());
                }
            }
            case "play" -> {
                int matchId = Integer.parseInt(request.get("matchId").toString());
                String playerUsername = request.get("username").toString();
                String symbol = request.get("symbol").toString();
                String position = request.get("position").toString();
                matchData = TicTacToeGame.matches.get(matchId);

                JSONObject response = new JSONObject();
                try {
                    response.put("type", "play");
                    response.put("matchId", matchId);
                    response.put("symbol", symbol);
                    response.put("position", position);
                    if (symbol.equals("O")) {
                        sendMessage(matchData.playerX.output, response.toJSONString());
                    } else {
                        sendMessage(matchData.playerO.output, response.toJSONString());
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                    System.out.println("Exception: " + e.getMessage());
                }

                // Check if the game is over after this play action
                String boardStatus = matchData.updateBoard(symbol, position);
                if (!boardStatus.equals("continue")) {
                    Player playerX = TicTacToeGame.players.get(matchData.playerX.username);
                    Player playerO = TicTacToeGame.players.get(matchData.playerO.username);

                    JSONObject overResponse = new JSONObject();
                    try {
                        overResponse.put("type", "over");
                        if (boardStatus.equals("win")) {
                            overResponse.put("winner", playerUsername);
                            overResponse.put("symbol", symbol);
                            updateRankWin(symbol, playerX, playerO);
                        } else {
                            // draw
                            overResponse.put("winner", "draw");
                            playerX.rank += 2;
                            playerO.rank += 2;
                        }

                        overResponse.put("new rank", playerX.rank);
                        sendMessage(matchData.playerX.output, overResponse.toJSONString());

                        overResponse.put("new rank", playerO.rank);
                        sendMessage(matchData.playerO.output, overResponse.toJSONString());

                        // Update the players information and clear the match data
                        TicTacToeGame.players.put(playerX.username, playerX);
                        TicTacToeGame.players.put(playerO.username, playerO);
                        TicTacToeGame.matches.remove(matchData.matchId);
                        matchData = null;
                    } catch (Exception e) {
                        // e.printStackTrace();
                        System.out.println("Exception: " + e.getMessage());
                    }

                }

            }
            case "chat" -> {
                int matchId = Integer.parseInt(request.get("matchId").toString());
                String playerUsername = request.get("username").toString();
                String symbol = request.get("symbol").toString();
                int rank = Integer.parseInt(request.get("rank").toString());
                String text = request.get("text").toString();
                matchData = TicTacToeGame.matches.get(matchId);

                JSONObject response = new JSONObject();
                try {
                    response.put("type", "chat");
                    response.put("opponentUsername", playerUsername);
                    response.put("opponentRank", rank);
                    response.put("text", text);

                    if (symbol.equals("O")) {
                        sendMessage(matchData.playerX.output, response.toJSONString());
                    } else {
                        sendMessage(matchData.playerO.output, response.toJSONString());
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                    System.out.println("Exception: " + e.getMessage());
                }
            }
            case "quit" -> {
                int matchId = Integer.parseInt(request.get("matchId").toString());
                String loserSymbol = request.get("symbol").toString();

                if (!loserSymbol.equals("")) {
                    matchData = TicTacToeGame.matches.get(matchId);
                    Player playerX = TicTacToeGame.players.get(matchData.playerX.username);
                    Player playerO = TicTacToeGame.players.get(matchData.playerO.username);

                    String winnerSymbol = (loserSymbol.equals("X")) ? "O" : "X";
                    String winnerUsername = (loserSymbol.equals("X")) ? playerO.username : playerX.username;

                    updateRankWin(loserSymbol, playerO, playerX);

                    JSONObject overResponse = new JSONObject();
                    try {
                        overResponse.put("type", "over");
                        overResponse.put("winner", winnerUsername);
                        overResponse.put("symbol", winnerSymbol);

                        if (loserSymbol.equals("X")) {
                            overResponse.put("new rank", playerO.rank);
                            sendMessage(matchData.playerO.output, overResponse.toJSONString());
                        } else if (loserSymbol.equals("O")) {
                            overResponse.put("new rank", playerX.rank);
                            sendMessage(matchData.playerX.output, overResponse.toJSONString());
                        }

                        // Update the players information and clear the match data
                        TicTacToeGame.players.put(playerX.username, playerX);
                        TicTacToeGame.players.put(playerO.username, playerO);
                        TicTacToeGame.matches.remove(matchData.matchId);
                        // matchData = null;
                    } catch (Exception e) {
                        // e.printStackTrace();
                        System.out.println("Exception: " + e.getMessage());
                    }
                } else {
                    // check whether is exiting after a match or in waiting
                    if (matchId != -1) {
                        System.out.println("Player " + username + " leaves the waiting room");
                        TicTacToeGame.waitingPool.poll();
                    }
                    Player player1 = new Player();
                    Player player2 = new Player();
                    MatchData psedoMatchData = new MatchData(-1, player1, player2);
                    matchData = psedoMatchData;
                }
                matchData.isOver = true;
            }
            default -> System.out.println("Request type: " + type);
        }
    }

    private void updateRankWin(String symbol, Player playerX, Player playerO) {
        if (symbol.equals("X")) {
            // Player X wins
            playerX.rank += 5;
            playerO.rank = Math.max(playerO.rank - 5, 0);
        } else if (symbol.equals("O")) {
            // Player O wins
            playerO.rank += 5;
            playerX.rank = Math.max(playerX.rank - 5, 0);
        }
    }

    public static void sendMessage(BufferedWriter output, String sendData) throws IOException {
        output.write(sendData);
        output.newLine(); // Add a newline character to indicate the end of the data
        output.flush(); // Flush the writer to ensure data is sent
        System.out.println("Sent data: " + sendData);
    }

    private JSONObject createMatchResponse(Player player, Player opponent, String symbol, int matchId) {
        JSONObject response = new JSONObject();
        response.put("type", "new match");
        response.put("matchId", matchId);
        response.put("playerUsername", player.username);
        response.put("playerRank", player.rank);
        response.put("playerSymbol", symbol);
        response.put("opponentUsername", opponent.username);
        response.put("opponentRank", opponent.rank);
        response.put("opponentSymbol", symbol.equals("O") ? "X" : "O");
        return response;
    }

}
