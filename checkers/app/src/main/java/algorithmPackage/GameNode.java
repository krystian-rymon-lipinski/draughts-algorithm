package algorithmPackage;

import com.krystian.checkers.Pawn;
import com.krystian.checkers.PlayableTile;

import java.util.ArrayList;

import static android.R.transition.move;

public class GameNode {
    private Pawn pawn; //which pawn has been moved
    public ArrayList<Integer> moveList; //destination of chosen pawn
    private PlayableTile[] boardState; //possible state of the board in the future
    public ArrayList<Pawn> whiteState; //pawns in specific state
    public ArrayList<Pawn> brownState; //to prevent getting non-existing pawn
    private int metrics; //white to brown pawns ratio (queen is worth 3 points)
    private int id; //to distinct nodes with no need of comparing board states
    private int link; //to set previous state
    private int level; //to establish how deep is analysis

    public GameNode(int id, int link, int level,
                    Pawn pawn, ArrayList<Integer> moveList,
                    PlayableTile[] boardState, ArrayList<Pawn> whiteState, ArrayList<Pawn> brownState) {
        this.id = id;
        this.link = link;
        this.level = level;
        this.pawn = pawn;
        this.moveList = moveList;
        this.boardState = boardState;
        this.whiteState = whiteState;
        this.brownState = brownState;
    }

    public Pawn getPawn() {return pawn;}
    public int getMove() {return move;}
    public void setPawn(Pawn pawn) {this.pawn = pawn;}
    public void setMove(ArrayList<Integer> move) {this.moveList = moveList;}

    public int getId() {return id;}
    public int getLink() {return link;}
    public int getLevel() {return level;}
    public PlayableTile[] getBoardState() {return boardState;}
    public int getMetrics() {return metrics;}

    public void setBoardState(PlayableTile[] boardState) {this.boardState = boardState;}
    public void setMetrics(int metrics) {this.metrics = metrics;}
    public void setId(int id) {this.id = id;}
    public void setLink(int link) {this.link = link;}
    public void setLevel(int level) {this.level = level;}
}


