package reversi.application;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import reversi.logic.AppLogic;
import reversi.ui.SceneManager;


/**
 * Huang Jiahui, 2014011330, 2016/7/23 0023.
 *
 * Entrance of the program.
 */
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager sceneManager = new SceneManager(primaryStage);
        AppLogic.setSceneManager(sceneManager);
        AppLogic.appStart();
        primaryStage.setOnCloseRequest(event -> AppLogic.appExit());

        primaryStage.setTitle("Reversi (build 1.0)");
        primaryStage.show();
    }

}
