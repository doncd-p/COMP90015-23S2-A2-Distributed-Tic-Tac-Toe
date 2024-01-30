/**
 * Author: Chenyang Dong
 * Student ID: 1074314
 */

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TicTacToeGame {
    public static int matchId = 0;
    public static ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();
    public static ConcurrentLinkedDeque<Player> waitingPool = new ConcurrentLinkedDeque<>();
    public static ConcurrentHashMap<Integer, MatchData> matches = new ConcurrentHashMap<>();

    public synchronized static int generateMatchId() {
        matchId++;
        return matchId;
    }
}
