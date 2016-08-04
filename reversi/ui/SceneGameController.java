package reversi.ui;

import com.jfoenix.controls.*;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.animation.*;
import javafx.beans.binding.DoubleBinding;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import reversi.logic.AppLogic;
import reversi.logic.GameLogic;
import reversi.logic.GameType;
import reversi.logic.Owner;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Huang Jiahui, 2014011330, 2016/7/26 0026.
 */
public class SceneGameController implements Initializable, SceneManageable {
    private SceneManager sceneManager;
    private GameLogic gameLogic;
    private static final String COL_NAME = "ABCDEFGH";
    private int savedCount = 1;

    // ----------------------------------------------
    // For the chat window (Html reader)
    private final static String BLACK_HISTORY_FORMAT = "<font color=\"black\" face=\"rockwell\"><b>+ %s</b></font><br>";
    private final static String WHITE_HISTORY_FORMAT = "<font color=\"DarkGray\" face=\"rockwell\"><b>+ %s</b></font><br>";
    private final static String SYSTEM_MESSAGE_FORMAT = "<font color=\"#900C3F\" face=\"rockwell\">-- %s --</font><br>";
    private final static String SEND_MESSAGE_FORMAT = "<font color=\"DarkGreen\" face=\"rockwell\">%s</font><br>";
    private final static String RECEIVE_MESSAGE_FORMAT = "<font color=\"DarkBlue\" face=\"rockwell\">%s</font><br>";
    // javascript for the page to scroll automatically to bottom.
    private final static String HTML_HEADER = "<html>\n" +
            "<head>\n" +
            "\t<script language=\"javascript\" type=\"text/javascript\">\n" +
            "\tfunction toBottom() {\n" +
            "\t\twindow.scrollTo(0, document.body.scrollHeight);\n" +
            "\t}\n" +
            "\t</script>\n" +
            "</head>\n" +
            "<body onload='toBottom()'>";
    private final static String HTML_SUFFIX = "</body></html>";
    // -----------------------------------------------

    @FXML private JFXButton exitButton;
    @FXML private GridPane root;
    @FXML private JFXButton saveButton;
    @FXML private StackPane boardPane;
    @FXML private JFXButton undoButton;
    @FXML private GridPane infoPane;
    @FXML private JFXButton newGameButton;
    @FXML private JFXButton openButton;
    @FXML private JFXButton settingsButton;
    @FXML private GridPane buttonPane;
    @FXML private StackPane overlayPane;
    @FXML private WebView chatWebView;
    @FXML private JFXButton chatSendButton;
    @FXML private JFXTextField chatTextField;
    @FXML private AnchorPane saveDialogPane;
    @FXML private JFXCheckBox addDateInfoCheckBox;
    @FXML private JFXTextField fileNameTextField;
    @FXML private Label timeLeftLabel;
    @FXML private Label blackCountLabel;
    @FXML private Label whiteCountLabel;
    @FXML private Separator timeSeparator;
    @FXML private Circle blackCircle;
    @FXML private Circle whiteCircle;

    private ReversiBoard gameBoard;

    private String chatContent = "";

    private WebEngine chatLoadEngine;

    private JFXDialog withdrawDialog;
    private JFXDialog mNewDialog;
    private JFXDialog disconnectDialog;

    private ScaleTransition whiteNotifyAnimation;
    private ScaleTransition blackNotifyAnimation;

    @FXML
    void newGameClicked() {
        if (gameLogic.getGameType() != GameType.ONLINE_GAME)
            gameLogic.pauseCountDown();
        JFXDialog newDialog = makeDialog("Confirm", "Are you sure you want to start a new game?",
                true, event1 -> {
                    if (gameLogic.getGameType() == GameType.ONLINE_GAME) {
                        gameLogic.sendRestartRequest();
                        sceneManager.showHeadHint("Request Sent.", "ok_h.png");
                    } else {
                        AppLogic.restartGame();
                    }
                }, true);
        newDialog.setOnDialogClosed(event -> {
            if (gameLogic.getGameType() != GameType.ONLINE_GAME)
                gameLogic.resumeCountDown();
        });
        newDialog.show();
    }

    @FXML
    void openClicked() {
        gameLogic.pauseCountDown();
        JFXDialog prompt = new JFXDialog();
        prompt.setDialogContainer(overlayPane);

        JFXDialogLayout promptLayout = new JFXDialogLayout();
        JFXListView<Label> listView = new JFXListView<>();
        listView.setPrefHeight(350);

        String[] availableList = AppLogic.getFilenameList();

        for (String fileName : availableList) {
            listView.getItems().add(new Label(fileName));
        }

        promptLayout.setHeading(new Label("Load Game"));
        promptLayout.setBody(listView);

        JFXButton sureButton = new JFXButton("LOAD");
        sureButton.getStyleClass().add("dialog-accept");
        sureButton.setOnMouseClicked(event1 -> {
            if (listView.getSelectionModel().getSelectedItem() == null)
                return;
            AppLogic.openFile(listView.getSelectionModel().getSelectedItem().getText());
            prompt.close();
        });
        JFXButton denyButton = new JFXButton("CANCEL");
        denyButton.setOnMouseClicked(event1 -> prompt.close());
        promptLayout.setActions(sureButton, denyButton);

        prompt.setOnDialogClosed(event -> gameLogic.resumeCountDown());
        prompt.setContent(promptLayout);

        prompt.show();
    }

    @FXML
    void saveClicked() {
        gameLogic.pauseCountDown();
        refreshSaveDialog();
        JFXDialog prompt = new JFXDialog();
        prompt.setDialogContainer(overlayPane);

        JFXDialogLayout promptLayout = new JFXDialogLayout();
        promptLayout.setHeading(new Label("Save Game"));
        promptLayout.setBody(saveDialogPane);


        JFXButton sureButton = new JFXButton("SAVE");
        sureButton.getStyleClass().add("dialog-accept");
        sureButton.setOnMouseClicked(event1 -> {
            if (!fileNameTextField.validate()) return;
            if (gameLogic.saveGame(getSaveFileName())) {
                ++savedCount;
                sceneManager.showHeadHint("File Saved!", "ok_h.png");
            } else {
                sceneManager.showHeadHint("Save Failed.", "error_h.png");
            }

            prompt.close();
        });
        JFXButton denyButton = new JFXButton("CANCEL");
        denyButton.setOnMouseClicked(event1 -> prompt.close());
        promptLayout.setActions(sureButton, denyButton);

        prompt.setContent(promptLayout);
        prompt.setOnDialogClosed(event -> gameLogic.resumeCountDown());
        prompt.show();
    }

    @FXML
    void undoClicked() {
        boolean result = gameLogic.checkCanWithdrawChess();
        if (!result) {
            makeDialog("Sorry", "You cannot retract your chess now.",
                    false, event1 -> {
                    }, true).show();
        } else {
            if (gameLogic.getGameType() == GameType.ONLINE_GAME) {
                gameLogic.sendWithdrawRequest();
                sceneManager.showHeadHint("Request Sent.", "ok_h.png");
            } else {
                gameLogic.withdrawChess();
            }
        }
    }

    @FXML
    void exitClicked() {
        if (gameLogic.getGameType() != GameType.ONLINE_GAME) {
            gameLogic.pauseCountDown();
            final boolean[] isAccept = {false};
            JFXDialog exitDialog = makeDialog("Confirm", "Are you sure you want to end this game?",
                    true, event1 -> {
                        isAccept[0] = true;
                        AppLogic.stopGame();
                    }, true);
            exitDialog.setOnDialogClosed(event -> {
                if (!isAccept[0])
                    gameLogic.resumeCountDown();
            });
            exitDialog.show();
        } else
            makeDialog("Confirm", "Are you sure you want to end this game?",
                true, event1 -> AppLogic.stopGame(), true).show();
    }

    @FXML
    void settingsClicked() {
        gameLogic.pauseCountDown();
        AppLogic.gameSettings();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        overlayPane.getChildren().remove(saveDialogPane);

        root.getColumnConstraints().get(0).prefWidthProperty().
                bind(root.widthProperty().multiply(0.05));
        root.getColumnConstraints().get(1).prefWidthProperty().
                bind(root.widthProperty().multiply(0.6));
        root.getColumnConstraints().get(2).prefWidthProperty().
                bind(root.widthProperty().multiply(0.33));
        root.getColumnConstraints().get(3).prefWidthProperty().
                bind(root.widthProperty().multiply(0.1));

        infoPane.getRowConstraints().get(0).prefHeightProperty().
                bind(infoPane.heightProperty().multiply(0.2));
        infoPane.getRowConstraints().get(1).prefHeightProperty().
                bind(infoPane.heightProperty().multiply(0.4));
        infoPane.getRowConstraints().get(2).prefHeightProperty().
                bind(infoPane.heightProperty().multiply(0.4));

        gameBoard = new ReversiBoard(this);
        gameBoard.maxHeightProperty().bind(boardPane.heightProperty().multiply(0.88));
        boardPane.getChildren().add(gameBoard);
        boardPane.setAlignment(Pos.CENTER);

        initButtons();

        gameLogic = AppLogic.getGameLogic();
        gameLogic.setUIController(this);

        RequiredFieldValidator requiredFieldValidator = new RequiredFieldValidator();
        requiredFieldValidator.setMessage("Please input a file name.");

        fileNameTextField.getValidators().add(requiredFieldValidator);
        fileNameTextField.textProperty().addListener((observable, oldValue, newValue)
                -> fileNameTextField.validate());

        chatSendButton.setGraphic(new ImageView(getClass().getResource("/reversi/resources/icons/send.png").toExternalForm()));

        chatLoadEngine = chatWebView.getEngine();
        chatLoadEngine.setJavaScriptEnabled(true);

        timeSeparator.prefWidthProperty().bind(infoPane.widthProperty().multiply(0.15));

        whiteNotifyAnimation = new ScaleTransition(Duration.millis(500), whiteCircle);
        whiteNotifyAnimation.setByX(0.3);
        whiteNotifyAnimation.setByY(0.3);
        whiteNotifyAnimation.setCycleCount(Animation.INDEFINITE);
        whiteNotifyAnimation.setAutoReverse(true);
        whiteNotifyAnimation.setInterpolator(Interpolator.EASE_BOTH);

        blackNotifyAnimation = new ScaleTransition(Duration.millis(500), blackCircle);
        blackNotifyAnimation.setByX(0.3);
        blackNotifyAnimation.setByY(0.3);
        blackNotifyAnimation.setCycleCount(Animation.INDEFINITE);
        blackNotifyAnimation.setAutoReverse(true);
        blackNotifyAnimation.setInterpolator(Interpolator.EASE_BOTH);

        withdrawDialog = new JFXDialog();
        withdrawDialog.setOverlayClose(false);
        withdrawDialog.setDialogContainer(overlayPane);

        JFXDialogLayout withdrawPromptLayout = new JFXDialogLayout();
        withdrawPromptLayout.setHeading(new Label("Withdraw Request"));
        withdrawPromptLayout.setBody(new Label("Your opponent want to undo previous moves."));

        JFXButton withdrawSureButton = new JFXButton("ALLOW");
        withdrawSureButton.getStyleClass().add("dialog-accept");
        withdrawSureButton.setOnMouseClicked(event -> {
            gameLogic.acceptWithdrawRequest();
            withdrawDialog.close();
        });
        JFXButton withdrawDenyButton = new JFXButton("DENY");
        withdrawDenyButton.setOnMouseClicked(event1 -> {
            gameLogic.denyWithdrawRequest();
            withdrawDialog.close();
        });
        withdrawPromptLayout.setActions(withdrawSureButton, withdrawDenyButton);

        withdrawDialog.setContent(withdrawPromptLayout);

        mNewDialog = new JFXDialog();
        mNewDialog.setOverlayClose(false);
        mNewDialog.setDialogContainer(overlayPane);

        JFXDialogLayout promptLayout = new JFXDialogLayout();
        promptLayout.setHeading(new Label("Restart Request"));
        promptLayout.setBody(new Label("Your opponent want to restart the game."));

        JFXButton sureButton = new JFXButton("ALLOW");
        sureButton.getStyleClass().add("dialog-accept");
        sureButton.setOnMouseClicked(event -> {
            gameLogic.acceptRestartRequest();
            mNewDialog.close();
        });
        JFXButton denyButton = new JFXButton("DENY");
        denyButton.setOnMouseClicked(event1 -> {
            gameLogic.denyRestartRequest();
            mNewDialog.close();
        });
        promptLayout.setActions(sureButton, denyButton);

        mNewDialog.setContent(promptLayout);

        disconnectDialog = makeDialog("Disconnected", "You have been disconnected.", false,
                event -> AppLogic.disconnectCallback(), false);

    }

    @Override
    public void setSceneManager(SceneManager manager) {
        this.sceneManager = manager;
    }

    @Override
    public void onSwitchedTo(String caller) {
        if (AppLogic.getIsSetting()) {
            AppLogic.setIsSetting(false);
            gameLogic.resumeCountDown();
            return;
        }

        gameBoard.animateIn();
        saveButton.setDisable(gameLogic.getGameType() == GameType.ONLINE_GAME);
        openButton.setDisable(gameLogic.getGameType() == GameType.ONLINE_GAME);
        settingsButton.setDisable(gameLogic.getGameType() == GameType.ONLINE_GAME);

        chatContent = "";
        systemMessage("New Game Started!");
    }

    private void initButtons() {
        newGameButton.setGraphic(new ImageView("/reversi/resources/icons/new.png"));
        openButton.setGraphic(new ImageView("/reversi/resources/icons/open.png"));
        saveButton.setGraphic(new ImageView("/reversi/resources/icons/save.png"));
        undoButton.setGraphic(new ImageView("/reversi/resources/icons/undo.png"));
        exitButton.setGraphic(new ImageView("/reversi/resources/icons/exit.png"));
        settingsButton.setGraphic(new ImageView("/reversi/resources/icons/settings.png"));

        JFXButton[] buttonList = new JFXButton[6];
        buttonList[0] = newGameButton;
        buttonList[1] = openButton;
        buttonList[2] = saveButton;
        buttonList[3] = undoButton;
        buttonList[4] = exitButton;
        buttonList[5] = settingsButton;

        DoubleBinding buttonSizeProperty = buttonPane.heightProperty().multiply(0.3);
        for (int i = 0; i < 6; ++i) {
            buttonList[i].setButtonType(JFXButton.ButtonType.RAISED);
            buttonList[i].setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px");
            buttonList[i].setFont(new Font("rockwell", 16));
            buttonList[i].prefWidthProperty().bind(buttonList[i].prefHeightProperty().multiply(1.2));
            buttonList[i].prefHeightProperty().bind(buttonSizeProperty);
        }
    }

    public void updateBoard() {
        for (int i = 0; i < 8; ++i)
            for (int j = 0; j < 8; ++j) {
                gameBoard.changeOwner(i, j, gameLogic.getBoard(i, j));
                if (gameLogic.getBoard(i, j) == Owner.NONE)
                    gameBoard.setHinted(i, j, gameLogic.getHint(i, j));
                else
                    gameBoard.setHinted(i, j, true);
            }
        undoButton.setDisable(!gameLogic.checkCanWithdrawChess());
        if (gameLogic.getCurrentPlayer() == Owner.WHITE) {
            blackNotifyAnimation.stop();
            blackCircle.setScaleX(1.0);
            blackCircle.setScaleY(1.0);
            whiteNotifyAnimation.play();
        } else {
            whiteNotifyAnimation.stop();
            whiteCircle.setScaleX(1.0);
            whiteCircle.setScaleY(1.0);
            blackNotifyAnimation.play();
        }
        whiteCountLabel.setText(String.valueOf(gameLogic.getWhiteCount()));
        blackCountLabel.setText(String.valueOf(gameLogic.getBlackCount()));
        withdrawDialog.close();
        mNewDialog.close();
    }

    void onChessEvent(int row, int col) {
        if (gameLogic.getGameType() == GameType.ONLINE_GAME &&
                !gameLogic.currentPlayerCanChess())
            return;
        if (gameLogic.getGameType() == GameType.LOCAL_GAME_AI &&
                gameLogic.getCurrentPlayer() == AppLogic.AIPlayer)
            return;
        gameLogic.chessAction(row, col);
    }

    public void addChessHistory(Owner owner, int row, int col) {
        String displayStr = (owner == Owner.BLACK) ? "Black" : "White";
        displayStr += (" : " + COL_NAME.charAt(row) + String.valueOf(col + 1));
        if (owner == Owner.BLACK)
            appendChatField(String.format(BLACK_HISTORY_FORMAT, displayStr));
        else
            appendChatField(String.format(WHITE_HISTORY_FORMAT, displayStr));
    }

    public void setHintEnabled(boolean enabled) {
        gameBoard.setHintEnabled(enabled);
    }

    private JFXDialog makeDialog(String title, String text, boolean deniable,
                            EventHandler<? super MouseEvent> eventHandler, boolean overlayReject) {
        JFXDialog prompt = new JFXDialog();
        prompt.setOverlayClose(overlayReject);
        prompt.setDialogContainer(overlayPane);

        JFXDialogLayout promptLayout = new JFXDialogLayout();
        promptLayout.setHeading(new Label(title));
        promptLayout.setBody(new Label(text));

        if (deniable) {
            JFXButton sureButton = new JFXButton("ACCEPT");
            sureButton.getStyleClass().add("dialog-accept");
            sureButton.setOnMouseClicked(event -> {
                eventHandler.handle(event);
                prompt.close();
            });
            JFXButton denyButton = new JFXButton("DENY");
            denyButton.setOnMouseClicked(event1 -> prompt.close());
            promptLayout.setActions(sureButton, denyButton);
        } else {
            JFXButton sureButton = new JFXButton("ACCEPT");
            sureButton.getStyleClass().add("dialog-accept");
            sureButton.setOnMouseClicked(event1 -> {
                eventHandler.handle(event1);
                prompt.close();
            });
            promptLayout.setActions(sureButton);
        }

        prompt.setContent(promptLayout);

        return prompt;
    }

    public void promptWithdraw() {
        withdrawDialog.show();
    }

    public void onWithdrawFailed() {
        sceneManager.showHeadHint("Request Denied!", "error_h.png");
    }

    public void onWithdrawSucceed() {
        sceneManager.showHeadHint("Request Accepted!", "ok_h.png");
    }

    public void promptNew() {
        mNewDialog.show();
    }

    public void onNewFailed() {
        sceneManager.showHeadHint("Request Denied!", "error_h.png");
    }

    public void onNewSucceed() {
        sceneManager.showHeadHint("Request Accepted!", "ok_h.png");
    }

    public void onDisconnected() {
        gameBoard.animateOut();
        gameLogic.pauseCountDown();
        disconnectDialog.show();
    }

    public void closeRelativeWindows() {
        disconnectDialog.close();
    }

    private String getSaveFileName() {
        String timeStr = "";
        if (addDateInfoCheckBox.isSelected())
            timeStr = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        return fileNameTextField.getText().concat("@").concat(timeStr);
    }

    private void refreshSaveDialog() {
        fileNameTextField.setText("MyGame" + String.valueOf(savedCount));
        addDateInfoCheckBox.setSelected(true);
    }

    public void onGameOver() {
        new Timeline(new KeyFrame(Duration.millis(500), event -> {
            gameBoard.animateOut();
            new Timeline(new KeyFrame(Duration.millis(1300), event1 ->
                    AppLogic.endGame())).play();
        })).play();
    }

    public void notifyBlock(Owner owner) {
        if (owner == Owner.BLACK)
            sceneManager.showHeadHint("Black Chess is Blocked!", null);
        else if (owner == Owner.WHITE)
            sceneManager.showHeadHint("White Chess is Blocked!", null);
    }

    public void notifyTimeUp() {
        sceneManager.showHeadHint("Time is UP!", "time_h.png");
    }

    @FXML
    void sendChat() {
        if (chatTextField.getText().length() == 0)
            return;
        appendChatField(String.format(SEND_MESSAGE_FORMAT, chatTextField.getText()));
        gameLogic.sendTextMessage(chatTextField.getText());
        chatTextField.clear();
    }

    public void onChatMessage(String msg) {
        appendChatField(String.format(RECEIVE_MESSAGE_FORMAT, msg));
    }

    public void systemMessage(String msg) {
        appendChatField(String.format(SYSTEM_MESSAGE_FORMAT, msg));
    }

    private void appendChatField(String text) {
        chatContent = chatContent.concat(text);
        chatLoadEngine.loadContent(HTML_HEADER + chatContent + HTML_SUFFIX);
    }

    public void updateTimeCount(int timer) {
        timeLeftLabel.setText(String.format("Time Left: %d s", timer));
    }
}
