/**
 * Author: Chenyang Dong
 * Student ID: 1074314
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class TicTacToeServer {
    private volatile boolean shouldExit = false;

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        // Check if the correct number of arguments are provided
        if (args.length != 2) {
            System.out.println("Please provide the required arguments with <server ip> <server port>.");
            System.exit(1);
        }

        String ip;
        try {
            ip = args[0];
            // Validate ip address
            if (!ip.equals("localhost")) {
                System.err.println("Invalid server ip address.");
                System.exit(1);
            }
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("Exception: " + e.getMessage());
            System.exit(1);
        }
        int port = 4444;
        try {
            port = Integer.parseInt(args[1]);
            // Validate port number
            if (port < 0 || port > 65535) {
                System.err.println("Invalid port number. Port must be between 0 and 65535.");
                System.exit(1);
            }
        } catch (NumberFormatException e) {
            // e.printStackTrace();
            System.out.println("Invalid port number. Port must be integer between 0 and 65535.");
            System.exit(1);
        }

        TicTacToeServer server = new TicTacToeServer();
        try (ServerSocket listeningSocket = new ServerSocket(port)) {
            new TicTacToeGame();
            System.out.println("Server listening on port " + listeningSocket.getLocalPort() + " for a connection");

            // Listens for user input to exit gracefully
            Thread userInputThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (!server.shouldExit) {
                    String userInput = scanner.nextLine();
                    if ("exit".equalsIgnoreCase(userInput)) {
                        server.shouldExit = true;
                        System.out.println("Exiting gracefully...");
                        Thread.currentThread().interrupt();
                        System.exit(0);
                    }
                }
                scanner.close();
            });
            userInputThread.start();

            while (!server.shouldExit) {
                Socket socket = listeningSocket.accept();
                TicTacToeServerConnection connection = new TicTacToeServerConnection(socket);
                connection.start();
            }
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("IOException: " + e.getMessage());
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("Exception: " + e.getMessage());
        }
    }
}