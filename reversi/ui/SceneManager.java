package reversi.ui;

import com.jfoenix.controls.JFXRippler;
import com.sun.istack.internal.Nullable;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import reversi.ui.SceneManageable;

import java.util.HashMap;

/**
 * Huang Jiahui, 2014011330, 2016/7/25 0025.
 */
public class SceneManager extends StackPane {
    private String currentSceneName = "";

    // store the scenes
    private HashMap<String, Pane> scenes = new HashMap<>();
    // store controllers related to the scene.
    private HashMap<String, SceneManageable> controllers = new HashMap<>();
    // universal head hint
    private HeadHint headHint;

    public SceneManager(Stage controlledStage) {
        controlledStage.setScene(new Scene(this));
        getStylesheets().add(getClass().getResource("/reversi/resources/css/reversi-main.css").toExternalForm());
        setPrefWidth(1280);
        setPrefHeight(720);
    }

    public void addCommonBackground() {
        FlowingTiles backgroundTile = new FlowingTiles("/reversi/resources/images/bg_tile.jpg");
        backgroundTile.prefWidthProperty().bind(widthProperty());

        Pane ripplePane = new Pane();
        ripplePane.prefHeightProperty().bind(heightProperty());
        ripplePane.prefWidthProperty().bind(widthProperty());
        JFXRippler rippler = new JFXRippler(ripplePane);

        ImageView blackDecorator = new ImageView("/reversi/resources/images/black.png");
        blackDecorator.fitWidthProperty().bind(widthProperty());
        blackDecorator.fitHeightProperty().bind(heightProperty());

        headHint = new HeadHint(this);

        getChildren().addAll(backgroundTile, rippler, blackDecorator, headHint);
        setAlignment(headHint, Pos.TOP_RIGHT);

    }

    private void addScene(String name, Pane scene, SceneManageable controller) {
        scenes.put(name, scene);
        controllers.put(name, controller);
        scene.prefHeightProperty().bind(heightProperty());
        scene.prefWidthProperty().bind(widthProperty());
    }

    public boolean loadScene(String name, String resource) {
        try {
            FXMLLoader myLoader = new
                    FXMLLoader(getClass().getResource(resource));
            Parent loadedPane = myLoader.load();
            SceneManageable sceneController = myLoader.getController();
            sceneController.setSceneManager(this);
            addScene(name, (Pane) loadedPane, sceneController);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean setScene(final String name) {
        if (scenes.get(name) == null) {
            System.out.println("screen hasn't been loaded!\n");
            return false;
        }
        getChildren().remove(scenes.get(currentSceneName));
        getChildren().add(scenes.get(name));
        controllers.get(name).onSwitchedTo(currentSceneName);
        currentSceneName = name;
        // brings headHint to top (have the largest index-z value)
        getChildren().remove(headHint);
        getChildren().add(headHint);
        return true;
    }

    public boolean switchScene(final String name) {

        if (scenes.get(name) == null) {
            System.out.println("screen hasn't been loaded!\n");
            return false;
        }

        // a simple fade in/out animation between scenes.
        DoubleProperty oldOpacity = scenes.get(currentSceneName).opacityProperty();
        Timeline fadeOut = new Timeline(new KeyFrame(Duration.ZERO,
                new KeyValue(oldOpacity, 1.0)),
                new KeyFrame(new Duration(200), event -> {
                    setScene(name);
                    DoubleProperty newOpacity = scenes.get(currentSceneName).opacityProperty();
                    Timeline fadeIn = new Timeline(new KeyFrame(Duration.ZERO,
                            new KeyValue(newOpacity, 0.0)),
                            new KeyFrame(new Duration(200),
                            new KeyValue(newOpacity, 1.0)));
                    fadeIn.play();
                }, new KeyValue(oldOpacity, 0.0)));
        fadeOut.play();
        return true;

    }

    // for future use
    public boolean unloadScene(String name) {
        if (scenes.remove(name) == null) {
            System.out.println("Scene didn't exist");
            return false;
        } else {
            controllers.remove(name);
            return true;
        }
    }

    public void showHeadHint(String message, @Nullable String image) {
        headHint.show(message, image);
    }

}
