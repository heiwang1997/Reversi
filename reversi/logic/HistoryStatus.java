package reversi.logic;

/**
 * Huang Jiahui, 2014011330, 2016/7/30 0030.
 */
class HistoryStatus {
    private Owner[][] board = new Owner[8][8];
    private Owner player; /* who will deal with this situation */

    HistoryStatus(Owner[][] board, Owner player) {
        for (int i = 0; i < 8; ++ i) {
            System.arraycopy(board[i], 0, this.board[i], 0, 8);
        }
        this.player = player;
    }

    HistoryStatus() {}

    Owner getPlayer() {
        return player;
    }

    void setPlayer(Owner player) { this.player = player; }

    void copyTo(Owner[][] dest) {
        for (int i = 0; i < 8; ++ i) {
            System.arraycopy(this.board[i], 0, dest[i], 0, 8);
        }
    }

    final Owner get(int row, int col) {
        return board[row][col];
    }

    void set(int row, int col, Owner val) {
        board[row][col] = val;
    }
}
