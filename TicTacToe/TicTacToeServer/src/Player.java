
/**
 * Author: Chenyang Dong
 * Student ID: 1074314
 */

import java.io.BufferedWriter;

public class Player {
    public String username;
    public int rank;
    public BufferedWriter output;

    public Player(String username, BufferedWriter output) {
        this.username = username;
        this.output = output;
        this.rank = 0;
    }

    public Player() {

    }
}
