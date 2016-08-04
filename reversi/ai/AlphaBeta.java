package reversi.ai;

import reversi.logic.Owner;

/**
 * Huang Jiahui, 2014011330, 2016/8/4 0004.
 */
class AlphaBeta {

    private static int alphaBeta(TreeNode node, int depth, int alpha, int beta, boolean isMax,
                          boolean isRoot, ChessMove bestMove) {

        if (depth == 0 || node.isTerminal())
            return NodeEvaluator.evaluate(node);
        if (isMax) {
            int v = Integer.MIN_VALUE;
            while (node.hasMoreChildren()) {
                TreeNode childNode = node.getNextChildren();

                int abResult = alphaBeta(childNode, depth - 1, alpha, beta, false, false, bestMove);
                if (abResult > v) {
                    v = abResult;
                    if (isRoot) {
                        bestMove.row = childNode.parentMove.row;
                        bestMove.col = childNode.parentMove.col;
                    }
                }

                alpha = Math.max(alpha, v);
                if (beta <= alpha)
                    break; // cut edge
            }
            node.releaseChildren();
            return v;
        } else {
            int v = Integer.MAX_VALUE;
            while (node.hasMoreChildren()) {
                TreeNode childNode = node.getNextChildren();
                v = Math.min(v, alphaBeta(childNode, depth - 1, alpha, beta, true, false, bestMove));
                beta = Math.min(beta, v);
                if (beta <= alpha)
                    break;  // cut edge
            }
            node.releaseChildren();
            return v;
        }
    }

    static ChessMove getNextStep(Owner[][] board, Owner currentPlayer, int depth) {
        TreeNode root = new TreeNode();
        for (int i = 0; i < 8; ++ i) {
            System.arraycopy(board[i], 0, root.state[i], 0, 8);
        }
        root.currentPlayer = currentPlayer;

        NodeEvaluator.setOwner(currentPlayer);

        ChessMove bestMove = new ChessMove(0, 0);
        alphaBeta(root, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true, true, bestMove);
        return bestMove;
    }
}
