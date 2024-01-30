/**
 * Author: Chenyang Dong
 * Student ID: 1074314
 */

public class MatchData {
    public int matchId;
    public String[][] board = new String[3][3];
    public Player playerX;
    public Player playerO;
    public int availablePlays = 9;
    public boolean isResumed = false;
    public boolean isOver = false;

    public MatchData(int matchId, Player playerX, Player playerO) {
        this.matchId = matchId;
        this.playerX = playerX;
        this.playerO = playerO;
        initializeBoard();
    }

    private void initializeBoard() {
        // Initialize the game board with empty cell
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = "";
            }
        }
    }

    public String updateBoard(String symbol, String position) {
        int row = (Integer.parseInt(position) - 1) / 3;
        int col = (Integer.parseInt(position) - 1) % 3;
        board[row][col] = symbol;
        availablePlays--;

        return checkForStatus();
    }

    private String checkForStatus() {
        boolean hasWinner = checkForWin(); // Check for a win
        if (hasWinner) {
            return "win";
        }

        boolean isDraw = checkForDraw(); // Check for a draw
        if (isDraw) {
            return "draw";
        }
        return "continue";
    }

    private boolean checkForWin() {
        // Check rows, columns, and diagonals for a win
        for (int i = 0; i < 3; i++) {
            if (board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2]) && !board[i][0].isEmpty()) {
                return true; // Row win
            }
            if (board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i]) && !board[0][i].isEmpty()) {
                return true; // Column win
            }
        }
        if (board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2]) && !board[0][0].isEmpty()) {
            return true; // Diagonal (top-left to bottom-right) win
        }
        if (board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0]) && !board[0][2].isEmpty()) {
            return true; // Diagonal (top-right to bottom-left) win
        }
        return false; // No winner
    }

    private boolean checkForDraw() {
        // Check if there is a draw when all cells are filled
        return availablePlays == 0;
    }

    public String getPositions(String symbol) {
        StringBuilder positions = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].equals(symbol)) {
                    int position = i * 3 + j + 1;
                    positions.append(position);
                }
            }
        }
        return positions.toString();
    }
}