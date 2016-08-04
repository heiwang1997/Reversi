package reversi.ui;

import javafx.animation.*;
import javafx.beans.property.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import reversi.logic.Owner;

/**
 * Huang Jiahui, 2014011330, 2016/7/26 0026.
 */
class ReversiCell extends Region {
    private ObjectProperty<Owner> ownerProperty = new
            SimpleObjectProperty<>(this, "owner", Owner.NONE);
    private BooleanProperty currentHintedProperty = new
            SimpleBooleanProperty(this, "hinted", true);
    private boolean hintEnabled;
    private boolean hinted = false;

    private int row;
    private int column;
    private Timeline lightAnimation;
    private Timeline disableAnimation;
    private Timeline enableAnimation;
    private ScaleTransition chessInAnimation;
    private ScaleTransition chessOutAnimation;
    private Timeline swipeAnimation;

    private boolean inRegion = false;

    private Region blinker;
    private Region disabler;
    private Circle chess;

    private ObjectProperty<Owner> ownerProperty() {
        return ownerProperty;
    }

    public Owner getOwner() {
        return ownerProperty.get();
    }

    public void setOwner(Owner owner) {
        ownerProperty.set(owner);
    }

    void setHintEnabled(boolean enabled) {
        hintEnabled = enabled;
        if (hintEnabled)
            currentHintedProperty.set(hinted);
        else
            currentHintedProperty.set(true);
    }

    void setHinted(boolean hinted) {
        this.hinted = hinted;
        if (hintEnabled)
            currentHintedProperty.set(hinted);
    }

    void fadeInAnimation() {
        disabler.setOpacity(0);
        DoubleProperty scaleProperty = scaleYProperty();
        Timeline fadeIn = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(scaleProperty, 0.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(200), new KeyValue(scaleProperty, 1.2, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(300), new KeyValue(scaleProperty, 1.0, Interpolator.EASE_BOTH)));
        fadeIn.play();
    }

    void fadeOutAnimation() {
        swipeAnimation.play();
    }

    private void createAnimations() {
        DoubleProperty opacityProperty = blinker.opacityProperty();
        lightAnimation = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(opacityProperty, 0.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(400), new KeyValue(opacityProperty, 0.42, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(800), new KeyValue(opacityProperty, 0.0, Interpolator.EASE_BOTH)));
        lightAnimation.setOnFinished(event -> {
            if (inRegion) lightAnimation.play();
        });

        chessInAnimation = new ScaleTransition(Duration.millis(280), chess);
        chessInAnimation.setFromX(0.0);
        chessInAnimation.setFromY(0.0);
        chessInAnimation.setToX(1.0);
        chessInAnimation.setToY(1.0);
        chessInAnimation.setInterpolator(Interpolator.EASE_BOTH);

        chessOutAnimation = new ScaleTransition(Duration.millis(280), chess);
        chessOutAnimation.setFromX(1.0);
        chessOutAnimation.setFromY(1.0);
        chessOutAnimation.setToX(0.0);
        chessOutAnimation.setToY(0.0);
        chessOutAnimation.setInterpolator(Interpolator.EASE_BOTH);

        DoubleProperty disOpacityProperty = opacityProperty();

        disableAnimation = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(disOpacityProperty, 1.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(600), new KeyValue(disOpacityProperty, 0.3, Interpolator.EASE_BOTH)));

        enableAnimation = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(disOpacityProperty, 0.3, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(600), new KeyValue(disOpacityProperty, 1.0, Interpolator.EASE_BOTH)));


        DoubleProperty swiperOpacityProperty = disabler.opacityProperty();
        int startMillis = (row + column) * 70;
        swipeAnimation = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(swiperOpacityProperty, 0.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(startMillis), new KeyValue(swiperOpacityProperty, 0.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(startMillis + 300), new KeyValue(swiperOpacityProperty, 0.6, Interpolator.EASE_BOTH)));

    }

    ReversiCell(int row, int column) {
        this.row = row;
        this.column = column;
        scaleXProperty().bind(scaleYProperty());
        getStyleClass().add("reversi-cell");
        if ((row + column) % 2 == 0)
            getStyleClass().add("reversi-cell-even");
        else
            getStyleClass().add("reversi-cell-odd");

        this.blinker = new Region();
        blinker.prefHeightProperty().bind(heightProperty());
        blinker.prefWidthProperty().bind(widthProperty());
        blinker.getStyleClass().add("reversi-cell-blinker");
        blinker.setOpacity(0.0);

        this.disabler = new Region();
        disabler.prefHeightProperty().bind(heightProperty());
        disabler.prefWidthProperty().bind(widthProperty());
        disabler.getStyleClass().add("reversi-cell-disabler");
        disabler.setOpacity(0.0);

        this.chess = new Circle();
        chess.radiusProperty().bind(heightProperty().multiply(0.4));
        chess.translateXProperty().bind(heightProperty().multiply(0.5));
        chess.translateYProperty().bind(chess.translateXProperty());
        chess.setScaleX(0.0);
        chess.setScaleY(0.0);

        getChildren().add(blinker);
        getChildren().add(chess);
        getChildren().add(disabler);

        ownerProperty().addListener((observable, oldValue, newValue) -> onOwnerPropertyChanged(oldValue, newValue));
        currentHintedProperty.addListener(((observable, oldValue, newValue) -> onHintedPropertyChanged(newValue)));

        setHintEnabled(false);
        prefHeightProperty().bind(prefWidthProperty());
        setOnMouseEntered(this::onMouseEntered);
        setOnMouseExited(this::onMouseExited);
        createAnimations();
    }


    private void onMouseExited(MouseEvent event) {
        inRegion = false;
    }

    private void onMouseEntered(MouseEvent event) {
        inRegion = true;
        lightAnimation.play();
    }

    private void onOwnerPropertyChanged(Owner oldValue, Owner newValue) {
        if (oldValue != Owner.NONE) {
            chessOutAnimation.play();
            chessOutAnimation.setOnFinished(event -> displayChess(newValue));
        } else {
            displayChess(newValue);
        }
    }

    private void onHintedPropertyChanged(boolean newValue) {
        if (newValue)
            enableAnimation.play();
        else
            disableAnimation.play();
    }


    private void displayChess(Owner newValue) {
        this.chess.getStyleClass().remove("reversi-chess-black");
        this.chess.getStyleClass().remove("reversi-chess-white");
        if (newValue == Owner.NONE) return; // probably should never happen.
        if (newValue == Owner.WHITE)
            this.chess.getStyleClass().add("reversi-chess-white");
        else
            this.chess.getStyleClass().add("reversi-chess-black");
        chessInAnimation.play();
    }
}
