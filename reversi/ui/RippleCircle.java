package reversi.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Huang Jiahui, 2014011330, 2016/8/3 0003.
 */
class RippleCircle extends Circle {
    private Timeline rippleAnimation;
    private boolean animationFinished;

    boolean getAnimationFinished() {
        return animationFinished;
    }

    RippleCircle() {
        animationFinished = true;
        rippleAnimation = new Timeline(new KeyFrame(Duration.ZERO,
                        new KeyValue(radiusProperty(), 0), new KeyValue(opacityProperty(), 0.8)),
                new KeyFrame(Duration.millis(2000),
                        new KeyValue(radiusProperty(), 160), new KeyValue(opacityProperty(), 0)));
        rippleAnimation.setOnFinished(event -> animationFinished = true);
    }

    void startAnimate() {
        rippleAnimation.stop();
        setFill(Color.rgb((int) (Math.random() * 255),
                (int) (Math.random() * 255), (int) (Math.random() * 255)));
        animationFinished = false;
        rippleAnimation.play();
    }
}
