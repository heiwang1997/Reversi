package reversi.ui;

import com.jfoenix.controls.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import reversi.logic.AppLogic;
import reversi.net.ClientListenTask;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.TreeMap;

/**
 * Huang Jiahui, 2014011330, 2016/8/1 0001.
 */
public class SceneClientController implements Initializable, SceneManageable {

    private class SLabel extends Label {
        String info;

        SLabel(String text) {
            super(text);
        }
    }

    private SceneManager sceneManager;

    @FXML private JFXButton cancelButton;

    @FXML private JFXButton refreshButton;

    @FXML private JFXButton IPButton;

    @FXML private StackPane root;

    @FXML private JFXButton joinButton;

    @FXML private JFXListView<SLabel> serverListView;

    @FXML private AnchorPane IPPane;

    @FXML private JFXTextField IPTextField;

    private ClientListenTask clientListenTask;
    private TreeMap<String, Integer> clientList;

    @FXML
    void joinButtonClicked() {
        try {
            if (serverListView.getSelectionModel().getSelectedItem() == null)
                return;
            String selectedInfo = serverListView.getSelectionModel().getSelectedItem().info;
            String IPAddress = selectedInfo.split(" ")[2];
            boolean thisOwnerIsBlack = selectedInfo.split(" ")[0].charAt(0) == '0';
            int thinkTime = Integer.parseInt(selectedInfo.split(" ")[1]);
            AppLogic.setOwnerIsBlack(thisOwnerIsBlack);
            AppLogic.startConnection(IPAddress);
            AppLogic.setSecsThink(thinkTime);
            stopClientListener();
        } catch (Exception ignored)
        // When UDP does not work, show user empty screen instead of error.
        {}
    }

    @FXML
    void IPButtonClicked() {
        JFXDialog prompt = new JFXDialog();
        prompt.setDialogContainer(root);

        JFXDialogLayout promptLayout = new JFXDialogLayout();
        promptLayout.setHeading(new Label("Please Enter Server's IP Address"));
        promptLayout.setBody(IPPane);

        JFXButton sureButton = new JFXButton("CONNECT");
        sureButton.getStyleClass().add("dialog-accept");
        sureButton.setOnMouseClicked(event1 -> {
            stopClientListener();
            prompt.close();
            AppLogic.startConnection(IPTextField.getText());
        });
        JFXButton denyButton = new JFXButton("CANCEL");
        denyButton.setOnMouseClicked(event1 -> prompt.close());
        promptLayout.setActions(sureButton, denyButton);

        prompt.setContent(promptLayout);

        prompt.show();
    }

    @FXML
    void refreshButtonClicked() {
        refreshButton.setDisable(true);
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(200), event1 -> refreshButton.setDisable(false)));
        timeline.play();
    }

    @FXML
    void cancelButtonClicked() {
        try {
            stopClientListener();
        } catch (Exception ignored) {} // same as above.
        sceneManager.switchScene("start");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root.getChildren().remove(IPPane);

        clientList = new TreeMap<>();

        joinButton.setGraphic(new ImageView(getClass().getResource("/reversi/resources/icons/join.png").toExternalForm()));
        cancelButton.setGraphic(new ImageView(getClass().getResource("/reversi/resources/icons/undo.png").toExternalForm()));
        IPButton.setGraphic(new ImageView(getClass().getResource("/reversi/resources/icons/ip.png").toExternalForm()));
        refreshButton.setGraphic(new ImageView(getClass().getResource("/reversi/resources/icons/refresh.png").toExternalForm()));

    }

    @Override
    public void setSceneManager(SceneManager manager) {
        this.sceneManager = manager;
    }

    @Override
    public void onSwitchedTo(String caller) {
        // start udp listen.
        clientListenTask = new ClientListenTask(clientList);
        new Thread(clientListenTask).start();
        clientListenTask.setOnServerListRefreshed(event -> onServerListRefreshed());
    }

    private void stopClientListener() {
        clientListenTask.cancel();
        clientList.clear();
        onServerListRefreshed();
    }

    private void onServerListRefreshed() {
        serverListView.getItems().clear();
        for (String serverName : clientList.keySet()) {
            SLabel timeLabel = new SLabel(getDisplayedInfo(serverName));
            serverListView.getItems().add(timeLabel);
            timeLabel.info = serverName;
        }
    }

    private String getDisplayedInfo(String raw) {
        String[] infoPack = raw.split(" ");
        return "Host: " + infoPack[2] + " [Think time = " + infoPack[1] + " seconds, Player = " +
                ((infoPack[0].charAt(0) == '1')? "BLACK" : "WHITE") + "] - Game Version Matched.";
    }
}
