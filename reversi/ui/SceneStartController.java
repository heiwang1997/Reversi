package reversi.ui;

import com.jfoenix.controls.*;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.util.Duration;
import reversi.logic.AppLogic;
import reversi.logic.Owner;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Huang Jiahui, 2014011330, 2016/7/25 0025.
 */
public class SceneStartController implements Initializable, SceneManageable {

    @FXML private ImageView mainTitle;

    @FXML private AnchorPane gameDetailPane;

    @FXML private JFXToggleButton colorToggleButton;

    @FXML private JFXSlider thinkTimeSlider;

    @FXML private StackPane root;

    @FXML private Label blackLabel;

    @FXML private Label whiteLabel;

    @FXML private Label thinkTimeLabel;

    @FXML private JFXRadioButton AIBlackRadioButton;

    @FXML private AnchorPane AIPane;

    @FXML private JFXRadioButton AIWhiteRadioButton;

    @FXML private JFXToggleButton AIToggleButton;

    private static final String REVERSI_HELP = "\nGame Version: build 1.0\n" +
            "\n" +
            "Programmer & Designer: Huang Jiahui, Tsinghua University.\n" +
            "\n" +
            "A chess game written in JAVA(FX).\n" +
            "\n" +
            "Reversi is a strategy board game for two players, played on an 8×8 uncheckered board. There are sixty-four identical game pieces called disks (often spelled \"discs\"), which are light on one side and dark on the other. Players take turns placing disks on the board with their assigned color facing up. During a play, any disks of the opponent's color that are in a straight line and bounded by the disk just placed and another disk of the current player's color are turned over to the current player's color.\n" +
            "\n" +
            "The object of the game is to have the majority of disks turned to display your color when the last playable empty square is filled.\n" +
            "\n" +
            "Find more about this game, please visit https://en.wikipedia.org/wiki/Reversi.";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root.getChildren().remove(gameDetailPane);
        root.getChildren().remove(AIPane);
        ToggleGroup AIColorToggleGroup = new ToggleGroup();
        AIBlackRadioButton.setToggleGroup(AIColorToggleGroup);
        AIWhiteRadioButton.setToggleGroup(AIColorToggleGroup);
        AIBlackRadioButton.disableProperty().bind(AIToggleButton.selectedProperty().not());
        AIWhiteRadioButton.disableProperty().bind(AIToggleButton.selectedProperty().not());
        AIBlackRadioButton.setSelected(true);

        changeColorOpacity();
        thinkTimeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                thinkTimeLabel.setText("Think Time: (" + newValue.intValue() + "s)"));
        colorToggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                changeColorOpacity());

        mainTitle = new ImageView("/reversi/resources/images/title_build.png");
        mainTitle.setPreserveRatio(true);
        mainTitle.translateYProperty().bind(root.heightProperty().divide(-4));
        mainTitle.scaleXProperty().bind(mainTitle.scaleYProperty());
        mainTitle.setOnMouseClicked(event -> showHelp());

        VBox buttonListLayout = getButtonVBox();

        root.getChildren().add(mainTitle);
        root.getChildren().add(buttonListLayout);

        titleAnimation();
    }

    private void changeColorOpacity() {
        if (colorToggleButton.isSelected()) {
            whiteLabel.setOpacity(1);
            blackLabel.setOpacity(0.4);
        } else {
            whiteLabel.setOpacity(0.4);
            blackLabel.setOpacity(1);
        }
    }

    private VBox getButtonVBox() {
        JFXButton localGameButton = new JFXButton("Local Game");
        localGameButton.setGraphic(new ImageView("/reversi/resources/icons/localgame.png"));
        localGameButton.setOnMouseClicked(event -> localGameClicked());

        JFXButton onlineGameButton = new JFXButton("Online Game");
        onlineGameButton.setGraphic(new ImageView("/reversi/resources/icons/onlinegame.png"));
        onlineGameButton.setOnMouseClicked(event -> onlineGameClicked());

        JFXButton exitButton = new JFXButton("Exit");
        exitButton.setGraphic(new ImageView("/reversi/resources/icons/exit.png"));
        exitButton.setOnMouseClicked(event -> exitButtonClicked());

        JFXButton settingsButton = new JFXButton();
        settingsButton.setGraphic(new ImageView("/reversi/resources/icons/settings.png"));
        settingsButton.setButtonType(JFXButton.ButtonType.RAISED);
        settingsButton.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 30px");
        settingsButton.setPrefSize(60, 60);
        settingsButton.setOnMouseClicked(event -> AppLogic.gameSettings());

        JFXButton[] buttonList = new JFXButton[3];
        buttonList[0] = localGameButton;
        buttonList[1] = onlineGameButton;
        buttonList[2] = exitButton;
        for (int i = 0; i < 3; ++ i) {
            buttonList[i].setButtonType(JFXButton.ButtonType.RAISED);
            buttonList[i].setGraphicTextGap(20);
            buttonList[i].setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 60px");
            buttonList[i].setFont(new Font("rockwell", 20));
            buttonList[i].setPrefSize(300, 60);
        }
        exitButton.setPrefSize(220, 60);

        HBox exitSettingsLayout = new HBox(15.0, exitButton, settingsButton);
        exitSettingsLayout.setAlignment(Pos.CENTER);

        VBox buttonListLayout = new VBox(15.0, localGameButton, onlineGameButton, exitSettingsLayout);
        buttonListLayout.translateYProperty().bind(root.heightProperty().divide(2));
        buttonListLayout.setAlignment(Pos.TOP_CENTER);

        return buttonListLayout;
    }

    private void titleAnimation() {
        DoubleProperty titleRotation = mainTitle.rotateProperty();
        Timeline rotateTransition = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(titleRotation, -2, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(800),
                        new KeyValue(titleRotation, 2, Interpolator.EASE_BOTH)));
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setAutoReverse(true);
        rotateTransition.play();
    }

    @Override
    public void setSceneManager(SceneManager manager) {
    }

    @Override
    public void onSwitchedTo(String caller) {
        if (AppLogic.getIsSetting())
            AppLogic.setIsSetting(false);
    }

    private void exitButtonClicked() {
        JFXDialog exitPrompt = new JFXDialog();
        exitPrompt.setDialogContainer(root);

        JFXDialogLayout exitLayout = new JFXDialogLayout();
        exitLayout.setHeading(new Label("Message"));
        exitLayout.setBody(new Label("Are you sure you want to exit?"));

        JFXButton sureButton = new JFXButton("OK");
        sureButton.getStyleClass().add("dialog-accept");
        sureButton.setOnMouseClicked(event -> AppLogic.appExit());
        JFXButton denyButton = new JFXButton("Cancel");
        denyButton.setOnMouseClicked(event -> exitPrompt.close());

        exitLayout.setActions(sureButton, denyButton);
        exitPrompt.setContent(exitLayout);

        exitPrompt.show();
    }

    private void localGameClicked() {
        JFXDialog prompt = new JFXDialog();
        prompt.setDialogContainer(root);

        JFXDialogLayout promptLayout = new JFXDialogLayout();

        promptLayout.setHeading(new Label("Select AI Options:"));
        promptLayout.setBody(AIPane);

        JFXButton sureButton = new JFXButton("NEXT");
        sureButton.getStyleClass().add("dialog-accept");
        sureButton.setOnMouseClicked(event1 -> {
            if (AIToggleButton.isSelected()) {
                AppLogic.AIPlayer = (AIBlackRadioButton.isSelected()) ? Owner.BLACK : Owner.WHITE;
                AppLogic.startLocalGameWithAI();
            } else {
                AppLogic.startLocalGameWithoutAI();
            }
            prompt.close();
        });
        JFXButton denyButton = new JFXButton("CANCEL");
        denyButton.setOnMouseClicked(event1 -> prompt.close());
        promptLayout.setActions(sureButton, denyButton);

        prompt.setContent(promptLayout);

        prompt.show();
    }

    private void onlineGameClicked() {
        JFXDialog prompt = new JFXDialog();
        prompt.setDialogContainer(root);

        JFXDialogLayout promptLayout = new JFXDialogLayout();

        GridPane promptBody = new GridPane();
        JFXRadioButton serverButton = new JFXRadioButton("Create a Game (Server Mode)");
        JFXRadioButton clientButton = new JFXRadioButton("Join a Game (Client Mode)");
        ToggleGroup buttonToggleGroup = new ToggleGroup();
        serverButton.setToggleGroup(buttonToggleGroup);
        serverButton.setSelected(true);
        clientButton.setToggleGroup(buttonToggleGroup);
        promptBody.add(serverButton, 0, 0);
        promptBody.add(clientButton, 0, 1);
        promptBody.getStyleClass().add("select-game-mode");

        promptLayout.setHeading(new Label("Select a Online Game Mode:"));
        promptLayout.setBody(promptBody);

        JFXButton sureButton = new JFXButton("NEXT");
        sureButton.getStyleClass().add("dialog-accept");
        sureButton.setOnMouseClicked(event1 -> {
            if (serverButton.isSelected())
                showServerDialog();
            else
                showClientDialog();
            prompt.close();
        });
        JFXButton denyButton = new JFXButton("CANCEL");
        denyButton.setOnMouseClicked(event1 -> prompt.close());
        promptLayout.setActions(sureButton, denyButton);

        prompt.setContent(promptLayout);

        prompt.show();
    }

    private void showServerDialog() {
        JFXDialog prompt = new JFXDialog();
        prompt.setDialogContainer(root);
        JFXDialogLayout promptLayout = new JFXDialogLayout();
        promptLayout.setHeading(new Label("Choose Game Detail"));
        promptLayout.setBody(gameDetailPane);

        JFXButton sureButton = new JFXButton("NEXT");
        sureButton.getStyleClass().add("dialog-accept");
        sureButton.setOnMouseClicked(event1 -> {
            AppLogic.setOwnerIsBlack(!colorToggleButton.isSelected());
            AppLogic.setSecsThink((int)thinkTimeSlider.getValue());
            AppLogic.startServer();
            prompt.close();
        });
        JFXButton denyButton = new JFXButton("CANCEL");
        denyButton.setOnMouseClicked(event1 -> prompt.close());
        promptLayout.setActions(sureButton, denyButton);

        prompt.setContent(promptLayout);

        prompt.show();
    }

    private void showClientDialog() {
        AppLogic.startClient();
    }

    private void showHelp() {
        JFXDialog prompt = new JFXDialog();
        prompt.setDialogContainer(root);
        JFXDialogLayout promptLayout = new JFXDialogLayout();
        promptLayout.setHeading(new Label("About Reversi"));

        promptLayout.setBody(new Label(REVERSI_HELP));

        JFXButton sureButton = new JFXButton("ACCEPT");
        sureButton.getStyleClass().add("dialog-accept");
        sureButton.setOnMouseClicked(event1 -> prompt.close());
        promptLayout.setActions(sureButton);

        prompt.setContent(promptLayout);

        prompt.show();
    }

}
