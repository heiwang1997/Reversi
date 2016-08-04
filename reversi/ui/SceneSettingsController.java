package reversi.ui;

import com.jfoenix.controls.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import reversi.logic.AppLogic;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Huang Jiahui, 2014011330, 2016/8/3 0003.
 */
public class SceneSettingsController implements Initializable, SceneManageable {

    @FXML
    private JFXSlider thinkTimeSlider;

    @FXML
    private StackPane root;

    @FXML
    private AnchorPane settingsPane;

    @FXML
    private JFXSlider AILevelSlider;

    @FXML
    private Label thinkTimeLabel;

    @FXML
    private JFXToggleButton BGMToggleButton;

    private JFXDialog settingsDialog;

    private String settingsCaller;

    private SceneManager sceneManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root.getChildren().remove(settingsPane);

        settingsDialog = new JFXDialog();

        settingsDialog.setOverlayClose(false);
        settingsDialog.setDialogContainer(root);

        JFXDialogLayout promptLayout = new JFXDialogLayout();
        promptLayout.setHeading(new Label("Settings"));
        promptLayout.setBody(settingsPane);

        JFXButton sureButton = new JFXButton("SAVE");
        sureButton.getStyleClass().add("dialog-accept");
        sureButton.setOnMouseClicked(event -> {
            applySettings();
            switchSceneBack();
            sceneManager.showHeadHint("Settings Saved!", "ok_h.png");
        });
        JFXButton denyButton = new JFXButton("CANCEL");
        denyButton.setOnMouseClicked(event1 -> switchSceneBack());
        promptLayout.setActions(sureButton, denyButton);

        settingsDialog.setContent(promptLayout);

        thinkTimeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                thinkTimeLabel.setText("Think Time: (" + newValue.intValue() + "s)"));
        thinkTimeSlider.setValue(AppLogic.localThinkTime);

        BGMToggleButton.setSelected(AppLogic.isBgmOn());

        AILevelSlider.setValue(AppLogic.AILevel);
    }

    private void switchSceneBack() {
        settingsDialog.close();
        sceneManager.switchScene(settingsCaller);
    }

    private void applySettings() {
        AppLogic.localThinkTime = (int) thinkTimeSlider.getValue();
        AppLogic.setBgmOn(BGMToggleButton.isSelected());
        AppLogic.AILevel = (int) AILevelSlider.getValue();
        AppLogic.savePreference();
    }

    @Override
    public void setSceneManager(SceneManager manager) {
        this.sceneManager = manager;
    }

    @Override
    public void onSwitchedTo(String caller) {
        this.settingsCaller = caller;
        settingsDialog.show();
    }
}
