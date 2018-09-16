package algorithmPackage;

import com.krystian.checkers.GameActivity;
import com.krystian.checkers.PlayableTile;

public class GameTree {
    private PlayableTile[] currentBoard = new PlayableTile[50];
    private int metrics = 0;
    private GameActivity gameActivity;
    //private PlayableTile[] board = gameActivity.
    public GameTree(PlayableTile[] currentBoard) {
        this.currentBoard = currentBoard;
        calculateMetrics();
    }

    private void calculateMetrics() {
        for(PlayableTile tile : currentBoard) {
            if(tile.getIsTaken() == 1) metrics--;
            else if(tile.getIsTaken() == -1) metrics++;
            else if(tile.getIsTaken() == 2) metrics -=3;
            else if(tile.getIsTaken() == -2) metrics =+3;
        }
    }

    public PlayableTile[] getCurrentBoard() { return currentBoard; }
    public int getMetrics() { return metrics; }

    public void setCurrentBoard(PlayableTile[] currentBoard) { this.currentBoard = currentBoard; }
    public void setMetrics(int metrics) { this.metrics = metrics; }
}
