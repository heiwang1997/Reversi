package reversi.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * Huang Jiahui, 2014011330, 2016/7/25 0025.
 */
class FlowingTiles extends Pane {
    private ImageView tile1;
    private ImageView tile2;
    private Timeline pathTransition;

    FlowingTiles(String tileUrlString) {
        tile1 = new ImageView(tileUrlString);
        tile1.setPreserveRatio(true);
        tile2 = new ImageView(tileUrlString);
        tile2.setPreserveRatio(true);
        getChildren().add(tile1);
        getChildren().add(tile2);

        // when the window is resized, restart scrolling animation.
        widthProperty().addListener((observable, oldValue, newValue) -> restartAnimation());
        heightProperty().addListener((observable, oldValue, newValue) -> restartAnimation());
    }

    private void restartAnimation() {
        if (getWidth() == 0 || getHeight() == 0)
            return;

        if (pathTransition != null)
            pathTransition.stop();

        double tileHeight = (tile1.getImage().getHeight() /
                tile1.getImage().getWidth()) * getWidth();
        tile1.setFitWidth(getWidth());
        tile1.setY(0);
        tile2.setFitWidth(getWidth());
        tile2.setY(tileHeight);

        DoubleProperty layoutY = translateYProperty();
        pathTransition = new Timeline(new KeyFrame(Duration.ZERO,
                new KeyValue(layoutY, 0)),
                new KeyFrame(Duration.millis(40000),
                        event -> restartAnimation(), new KeyValue(layoutY, - tileHeight)));

        pathTransition.play();
    }

}
