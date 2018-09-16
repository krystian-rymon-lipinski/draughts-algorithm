package algorithmPackage;

import com.krystian.checkers.Pawn;

public class GameNode {
    private Pawn pawn; //which pawn has been moved
    private int move; //destination of chosen pawn

    public GameNode(Pawn pawn, int move) {
        this.pawn = pawn;
        this.move = move;
    }

    public Pawn getPawn() {return pawn;}
    public int getMove() {return move;}
    public void setPawn(Pawn pawn) {this.pawn = pawn;}
    public void setMove(int move) {this.move = move;}
}


