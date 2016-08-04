package reversi.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import reversi.logic.AppLogic;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

/**
 * Huang Jiahui, 2014011330, 2016/8/1 0001.
 */
public class SceneWaitController implements Initializable, SceneManageable {

    @FXML private Label infoLabel;

    @FXML
    void cancelButtonClicked(ActionEvent event) {
        AppLogic.cancelConnection();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @Override
    public void setSceneManager(SceneManager manager) {

    }

    @Override
    public void onSwitchedTo(String caller) {
        if (AppLogic.isCurrentConnectionIsServer())
            try {
                infoLabel.setText("Creating Server...\nYour IP Address is: " + InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        else
            infoLabel.setText("Please Wait...");
    }
}
