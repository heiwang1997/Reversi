package reversi.ai;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import reversi.logic.Owner;

/**
 * Huang Jiahui, 2014011330, 2016/8/4 0004.
 */
public class AIService extends Service<ChessMove> {
    private Owner[][] board;
    private Owner currentPlayer;
    private int depth;

    // start Service. Run on a new thread.
    public void start(Owner[][] board, Owner currentPlayer, int depth) {
        this.board = board;
        this.currentPlayer = currentPlayer;
        this.depth = depth;
        super.restart();
    }

    @Override
    protected Task<ChessMove> createTask() {
        return new Task<ChessMove>() {
            @Override
            protected ChessMove call() throws Exception {
                Thread.sleep(1000);
                return AlphaBeta.getNextStep(board, currentPlayer, depth);
            }
        };
    }
}
