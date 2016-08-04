package reversi.ui;


import javafx.scene.layout.GridPane;
import reversi.logic.Owner;

import java.util.LinkedList;

/**
 * Huang Jiahui, 2014011330, 2016/7/26 0026.
 */
class ReversiBoard extends GridPane {

    private static final double CELL_GAP = 5.0;
    private LinkedList<ReversiCell> reversiCells;
    private SceneGameController parent;

    ReversiBoard(SceneGameController parent) {
        setHgap(CELL_GAP);
        setVgap(CELL_GAP);
        this.parent = parent;
        maxWidthProperty().bind(maxHeightProperty());
        reversiCells = new LinkedList<>();
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                ReversiCell newCell = new ReversiCell(i, j);
                int finalI = i;
                int finalJ = j;
                newCell.setOnMouseClicked(event -> onChessEvent(finalI, finalJ));
                reversiCells.add(newCell);
                newCell.prefWidthProperty().bind(widthProperty().divide(8));
                add(newCell, i, j);
            }
        }
    }

    void animateIn() {
        reversiCells.forEach(ReversiCell::fadeInAnimation);
    }

    void animateOut() {
        reversiCells.forEach(ReversiCell::fadeOutAnimation);
    }

    void changeOwner(int row, int col, Owner newOwner) {
        reversiCells.get(row * 8 + col).setOwner(newOwner);
    }

    void setHintEnabled(boolean enabled) {
        for (ReversiCell cell : reversiCells) {
            cell.setHintEnabled(enabled);
        }
    }

    void setHinted(int row, int col, boolean hinted) {
        reversiCells.get(row * 8 + col).setHinted(hinted);
    }

    private void onChessEvent(int row, int col) {
        parent.onChessEvent(row, col);
    }
}
