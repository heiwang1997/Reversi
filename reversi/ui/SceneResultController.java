package reversi.ui;

import com.jfoenix.controls.JFXButton;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import reversi.logic.AppLogic;
import reversi.logic.Owner;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Huang Jiahui, 2014011330, 2016/8/3 0003.
 */
public class SceneResultController implements Initializable, SceneManageable {

    private SceneManager sceneManager;

    @FXML private Label winnerLabel;

    @FXML private StackPane root;

    @FXML private JFXButton backButton;

    @FXML private Circle chessCircle;

    @FXML private Pane circleCanvas;

    private Timeline chessRotate;

    private RippleCircle[] rippleCircles = new RippleCircle[6];

    private int currentCheckIndex = 0;

    private boolean animationRecheck = false;

    @FXML
    void backButtonClicked() {
        sceneManager.switchScene("start");
        chessRotate.stop();
        animationRecheck = false;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        backButton.setGraphic(new ImageView(getClass().getResource("/reversi/resources/icons/undo.png").toExternalForm()));
        chessRotate = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(chessCircle.translateYProperty(), -10, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(600), new KeyValue(chessCircle.translateYProperty(), 10, Interpolator.EASE_BOTH)));
        chessRotate.setAutoReverse(true);
        chessRotate.setCycleCount(Animation.INDEFINITE);

        chessCircle.radiusProperty().bind(root.heightProperty().multiply(0.12));

        for (int i = 0; i < rippleCircles.length; ++ i) {
            RippleCircle newCircle = new RippleCircle();
            rippleCircles[i] = newCircle;
            circleCanvas.getChildren().add(newCircle);
        }
    }

    @Override
    public void setSceneManager(SceneManager manager) {
        sceneManager = manager;
    }

    @Override
    public void onSwitchedTo(String caller) {
        if (AppLogic.getWinner() == Owner.BLACK)
            chessCircle.getStyleClass().add("reversi-chess-black");
        else
            chessCircle.getStyleClass().add("reversi-chess-white");

        if (AppLogic.getWinner() == Owner.NONE)
            winnerLabel.setText("Tie!");

        currentCheckIndex = 0;
        startAnimation();
        animationRecheck = true;
        recheckCircle();
    }

    private void startAnimation() {
        double buttonY = -120;
        double titleY = 120;

        Timeline buttonTitleIn = new Timeline(new KeyFrame(Duration.ZERO,
                        new KeyValue(backButton.translateYProperty(), - buttonY),
                        new KeyValue(winnerLabel.translateYProperty(), - titleY)),
                new KeyFrame(Duration.millis(800),
                        new KeyValue(backButton.translateYProperty(), 0, Interpolator.EASE_OUT),
                        new KeyValue(winnerLabel.translateYProperty(), 0, Interpolator.EASE_OUT)));
        buttonTitleIn.play();
        chessRotate.play();
    }

    private void recheckCircle() {
        if (!animationRecheck) return;

        if (rippleCircles[currentCheckIndex].getAnimationFinished()) {
            rippleCircles[currentCheckIndex].setTranslateX(root.getWidth() * Math.random());
            rippleCircles[currentCheckIndex].setTranslateY(root.getHeight() * Math.random());
            rippleCircles[currentCheckIndex].startAnimate();
            currentCheckIndex = (currentCheckIndex + 1) % rippleCircles.length;
        }

        Timeline checkLag = new Timeline(new KeyFrame(Duration.millis(1000 * Math.random())));
        checkLag.setOnFinished(event -> recheckCircle());
        checkLag.play();
    }
}
