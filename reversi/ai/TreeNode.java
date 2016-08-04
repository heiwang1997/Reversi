package reversi.ai;

import reversi.logic.Owner;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Huang Jiahui, 2014011330, 2016/8/4 0004.
 *
 * Tree node in Alpha-Beta pruning
 */
class TreeNode {
    private static final int[] DR = {1, 1, 0, -1, -1, -1, 0, 1};
    private static final int[] DC = {0, 1, 1, 1, 0, -1, -1, -1};

    Owner[][] state = new Owner[8][8];
    Owner currentPlayer;
    ChessMove parentMove = null;

    // iterate children list with hasNext() and next()
    private int currentChildrenIndex = 0;
    private ArrayList<TreeNode> children = null;

    private boolean calculateHintCell(int row, int col) {
        if (state[row][col] != Owner.NONE) return false;

        Owner opponentPlayer = getOpponentOwner(currentPlayer);

        for (int i = 0; i < 8; ++ i) {
            int flipCount = getDirectionFlipCount(row, col, DR[i], DC[i], opponentPlayer);
            if (flipCount != -1) return true;
        }

        return false;
    }

    private int getDirectionFlipCount(int row, int col, int dr, int dc,
                                      Owner opponentPlayer) {
        int count = 0;
        while ((row + dr) < 8 && (row + dr) >= 0 &&
                (col + dc) < 8 && (col + dc) >= 0) {
            row += dr;
            col += dc;
            if (state[row][col] == currentPlayer) {
                if (count != 0) return count;
                else return -1;
            } else if (state[row][col] == opponentPlayer) {
                count += 1;
            } else if (state[row][col] == Owner.NONE) {
                return -1;
            }
        }
        return -1;
    }

    private Owner getOpponentOwner(Owner thisOwner) {
        if (thisOwner == Owner.WHITE)
            return Owner.BLACK;
        else
            return Owner.WHITE;
    }

    private Owner[][] getResultBoard(int row, int col) {

        Owner[][] result = new Owner[8][8];

        for (int i = 0; i < 8; ++ i) {
            System.arraycopy(state[i], 0, result[i], 0, 8);
        }

        result[row][col] = currentPlayer;

        Owner opponentPlayer = getOpponentOwner(currentPlayer);

        for (int i = 0; i < 8; ++ i) {
            int flipCount = getDirectionFlipCount(row, col, DR[i], DC[i], opponentPlayer);
            if (flipCount != -1) {
                for (int j = 1; j <= flipCount; ++ j)
                    result[row + j * DR[i]][col + j * DC[i]] = currentPlayer;
            }
        }

        return result;
    }

    private void expand() {
        children = new ArrayList<>();

        LinkedList<ChessMove> availableMoves = new LinkedList<>();
        for (int i = 0; i < 8; ++ i) {
            for (int j = 0; j < 8; ++ j) {
                if (calculateHintCell(i, j))
                    availableMoves.addLast(new ChessMove(i, j));
            }
        }

        for (ChessMove move : availableMoves) {
            TreeNode node = new TreeNode();
            node.state = getResultBoard(move.row, move.col);
            node.currentPlayer = getOpponentOwner(currentPlayer);
            node.parentMove = new ChessMove(move.row, move.col);
            children.add(node);
        }

    }

    boolean hasMoreChildren() {
        if (children == null)
            expand();
        return currentChildrenIndex != children.size();
    }

    TreeNode getNextChildren() {
        if (children == null)
            expand();
        return children.get(currentChildrenIndex++);
    }

    boolean isTerminal() {
        if (children == null)
            expand();
        return (children.size() == 0);
    }

    int getChildrenCount() {
        if (children == null)
            expand();
        return children.size();
    }

    void releaseChildren() {
        children.clear();
    }
}
