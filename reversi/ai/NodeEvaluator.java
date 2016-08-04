package reversi.ai;

import reversi.logic.Owner;

/**
 * Huang Jiahui, 2014011330, 2016/8/4 0004.
 *
 * Evaluate a node by simply adding the num of chess in corner and sides.
 */
class NodeEvaluator {
    private static Owner thisOwner;
    private static Owner opponentOwner;

    private static final int[] CORNER_X = {0, 0, 7, 7};
    private static final int[] CORNER_Y = {0, 7, 0, 7};

    static void setOwner(Owner owner) {
        thisOwner = owner;
        opponentOwner = (owner == Owner.BLACK) ? Owner.WHITE : Owner.BLACK;
    }

    static int evaluate(TreeNode node) {
        int validSteps = node.getChildrenCount();
        int cornerPoint = cornerPoint(node.state);
        int sidePoint = sidePoint(node.state);

        return (validSteps + cornerPoint * 100 + sidePoint);
    }

    private static int cornerPoint(Owner[][] board) {
        int point = 0;
        for (int i = 0; i < 4; ++ i) {
            if (board[CORNER_X[i]][CORNER_Y[i]] == thisOwner)
                ++ point;
            if (board[CORNER_X[i]][CORNER_Y[i]] == opponentOwner)
                -- point;
        }
        return point;
    }

    private static int sidePoint(Owner[][] board) {
        int point = 0;
        for (int i = 0; i < 8; i += 7) {
            for (int j = 1; j < 7; ++ j) {
                if (board[i][j] == thisOwner) ++ point;
                if (board[j][i] == thisOwner) ++ point;
                if (board[i][j] == opponentOwner) --point;
                if (board[j][i] == opponentOwner) --point;
            }
        }
        return point;
    }
}
