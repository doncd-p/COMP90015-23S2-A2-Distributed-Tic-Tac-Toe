# COMP90015-23S2-A2-Distributed-Tic-Tac-Toe

## Overview

This project implements a distributed Tic-Tac-Toe gaming system that allows multiple players to connect, play, and engage in real-time chatting. The system enables concurrent interactions between the server and clients, providing a seamless and responsive gaming experience. Communication between the client and server can be achieved using either Java sockets or RMI, based on the chosen implementation approach.

## System Components

### Server Component

The server employs a multi-threaded architecture to efficiently manage player interactions and game sessions. Each thread represents a server connection dedicated to communicating with a client, handling player connections, match pairing, and game session management.

### Client Component

The client application establishes a connection with the server using a specified username, marking the player's presence in the system. The client acts as a bridge for sending and receiving data between the user interface and the server. Specialized threads manage server connections, monitor server availability, and ensure timely responses.

### TCP/IP Communication via Sockets

Client-server communication relies on the TCP/IP protocol facilitated by sockets, ensuring reliable and ordered data transmission.

### JSON Message Exchange Protocol

JSON is chosen as the message exchange protocol between the server and client, facilitating meaningful and structured data exchange. Requests and responses are formatted as JSON objects, simplifying data handling and allowing clear communication between the client and server.

## Class Structure

### Server Class Structure

The UML Class Diagram (Figure 1) illustrates the structure of the Server program. Core components include `TicTacToeServer`, `TicTacToeServerConnection`, `TicTacToeGame`, `Player`, and `MatchData` classes. These classes work together to oversee game operations, manage connections, and maintain game state and player information.

### Client Class Structure

The UML Class Diagram (Figure 2) depicts the structure of the Client program. Key components include `TicTacToeClient`, `TicTacToeClientConnection`, `Player`, and `GUI` classes. The `TicTacToeClientConnection` class manages communication with the server, while the `GUI` class provides an intuitive interface for user interactions.

## Interaction Diagram

The Interaction/Sequence Diagram (Figure 3) illustrates seamless communication between the client application, server, and other clients during various phases of gameplay, including match finding, player actions, and handling disconnects.

## Critical Analysis

### Functionality

All functional requirements have been successfully implemented, providing a seamless gaming experience. Key functionalities include match finding, turn-based gameplay, real-time chat, client-side timeouts, and fault tolerance mechanisms.

### Error Handling

The system promptly responds with informative error messages when users initiate incorrect requests, enhancing the user experience and guiding users towards correct interactions.

### Advantages of the System

-   Utilization of TCP sockets ensures reliable and ordered data transmission.
-   Real-time gaming experience with integrated chat functionality.
-   Robust fault tolerance mechanisms effectively handle server and client disconnections.
-   User-friendly GUI ensures a smooth and intuitive gaming experience.

## Future Improvements

-   Explore the use of RMI for communication to potentially simplify certain aspects of the distributed system.
-   Implement mechanisms to store match data in a database in case of server downtime for enhanced robustness.
-   Address the uniqueness of usernames within the system to prevent multiple players from using the same username.

## Conclusion

The distributed Tic-Tac-Toe gaming system facilitates multiple players connecting and playing in real-time with features like chat communication, client-side timeouts, and fault tolerance. The system ensures reliable communication and effective error management for a smooth gaming experience.
