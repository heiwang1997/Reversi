package reversi.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.util.Duration;



/**
 * Huang Jiahui, 2014011330, 2016/8/1 0001.
 */
class HeadHint extends Region {
    private Label textLabel = new Label("");

    HeadHint(SceneManager manager) {
        getStyleClass().add("head-hint");
        getChildren().add(textLabel);

        translateYProperty().bind(manager.heightProperty().multiply(0.1));
        textLabel.translateYProperty().bind(heightProperty().multiply(0.5).
                subtract(textLabel.getFont().getSize() + 3));
        textLabel.prefWidthProperty().bind(widthProperty());
        textLabel.setAlignment(Pos.CENTER);
        textLabel.setGraphicTextGap(5);
        setMaxWidth(0);

        setMaxHeight(55);
    }

    void show(String message, String image) {
        textLabel.setText(message);
        double currentWidth = message.length() * textLabel.getFont().getSize() + 20;
        if (image != null) {
            ImageView icon = new ImageView(getClass().getResource("/reversi/resources/icons/" + image).
                    toExternalForm());
            textLabel.setGraphic(icon);
            currentWidth += 32;
        }
        setMaxWidth(currentWidth);

        Timeline transition = new Timeline(new KeyFrame(Duration.ZERO,
                        new KeyValue(translateXProperty(), currentWidth, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(400),
                        new KeyValue(translateXProperty(), 0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(1600),
                        new KeyValue(translateXProperty(), 0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(2000),
                        new KeyValue(translateXProperty(), currentWidth, Interpolator.EASE_BOTH)));
        transition.play();
    }
}
