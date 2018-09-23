package algorithmPackage;

import com.krystian.checkers.Pawn;
import com.krystian.checkers.PlayableTile;
import java.util.ArrayList;


public class GameTree {
    private int rootMetrics = 0; //white to brown point ratio in the current board state
    private GameNode currentNode;
    private GameNode previousNode;
    public ArrayList<GameNode> gameNodeList = new ArrayList<>();
    public ArrayList<GameNode[]> gameBranch = new ArrayList<>();

    public GameTree(PlayableTile[] currentBoard, ArrayList<Pawn> whitePawn, ArrayList<Pawn> brownPawn) {
        calculateMetrics(currentBoard);
        currentNode = new GameNode(0, 0, 0, null, null, currentBoard, whitePawn, brownPawn); //root - present state of the board
        previousNode = currentNode;
        gameNodeList.add(currentNode);
    }

    private void calculateMetrics(PlayableTile[] currentBoard) {
        for(PlayableTile tile : currentBoard) {
            if(tile.getIsTaken() == 1) rootMetrics--;
            else if(tile.getIsTaken() == -1) rootMetrics++;
            else if(tile.getIsTaken() == 2) rootMetrics -=3;
            else if(tile.getIsTaken() == -2) rootMetrics =+3;
        }
    }

    public GameNode getCurrentNode() {return currentNode;}
    public GameNode getPreviousNode() {return previousNode;}
}
