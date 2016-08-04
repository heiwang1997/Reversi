package reversi.logic;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import reversi.net.ClientConnectionTask;
import reversi.net.LocalServerService;
import reversi.ui.SceneManager;

import java.io.*;
import java.util.TreeMap;

/**
 * Huang Jiahui, 2014011330, 2016/7/26 0026.
 *
 * The main class which controls logic of the program
 *
 * NOTE: Everything is static for convenience and flexibility.
 */
public class AppLogic {
    private static GameLogic gameLogic = new GameLogic();
    private static SceneManager sceneManager;

    // info for online game
    private static int secsThink = 2;
    private static boolean ownerIsBlack = true;

    // service when creating the game
    private static LocalServerService localServerService;

    // service when the client is trying to connect to host
    private static ClientConnectionTask clientConnectionTask;

    private static boolean currentConnectionIsServer;

    // whether the Settings Scene is prompted.
    private static boolean isSetting = false;
    private static MediaPlayer bgmPlayer;

    // User Preferences.
    public static int localThinkTime = 20;
    private static boolean bgmOn = true;
    public static int AILevel = 3;
    public static Owner AIPlayer = Owner.BLACK;

    public static boolean isBgmOn() {
        return bgmOn;
    }

    public static void setBgmOn(boolean bgmOn) {
        AppLogic.bgmOn = bgmOn;
        if (bgmOn)
            bgmPlayer.play();
        else
            bgmPlayer.pause();
    }

    private static void loadPreference() {
        File preferenceFile = new File("./reversi.user");
        if (!preferenceFile.exists()) {
            savePreference();
            return;
        }
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(preferenceFile));
            localThinkTime = in.readInt();
            bgmOn = in.readBoolean();
            AILevel = in.readInt();
            in.close();
        } catch (IOException ignored) {
        }
    }

    public static void savePreference() {
        File preferenceFile = new File("./reversi.user");
        DataOutputStream out;
        try {
            out = new DataOutputStream(new FileOutputStream(preferenceFile));
            out.writeInt(localThinkTime);
            out.writeBoolean(bgmOn);
            out.writeInt(AILevel);
            out.close();
        } catch (IOException ignored) {
        }

    }

    public static boolean getIsSetting() {
        return isSetting;
    }

    public static void setIsSetting(boolean isSetting) {
        AppLogic.isSetting = isSetting;
    }

    public static void gameSettings() {
        setIsSetting(true);
        sceneManager.switchScene("settings");
    }

    public static Owner getWinner() {
        return gameLogic.getWinner();
    }

    public static boolean isCurrentConnectionIsServer() {
        return currentConnectionIsServer;
    }

    public static TreeMap<String, Integer> serverList;

    public static boolean getOwnerIsBlack() {
        return ownerIsBlack;
    }

    public static void setOwnerIsBlack(boolean ownerIsBlack) {
        AppLogic.ownerIsBlack = ownerIsBlack;
    }

    public static int getSecsThink() {
        return secsThink;
    }

    public static void setSecsThink(int secsThink) {
        AppLogic.secsThink = secsThink;
    }

    public static void appExit() {
        if (gameLogic.getGameType() == GameType.ONLINE_GAME)
            gameLogic.stopNetService();
        // the AI Service is not stopped because it will automatically.
        System.exit(0);
    }

    public static void appStart() {
        loadPreference();
        bgmPlayer = new MediaPlayer(new Media(sceneManager.getClass().
                getResource("/reversi/resources/bgm/theme.mp3").toExternalForm()));
        bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        if (bgmOn)
            bgmPlayer.play();
        sceneManager.loadScene("start", "/reversi/resources/fxmls/StartScene.fxml");
        sceneManager.loadScene("game", "/reversi/resources/fxmls/GameScene.fxml");
        sceneManager.loadScene("wait", "/reversi/resources/fxmls/WaitScene.fxml");
        sceneManager.loadScene("client", "/reversi/resources/fxmls/ClientScene.fxml");
        sceneManager.loadScene("result", "/reversi/resources/fxmls/ResultScene.fxml");
        sceneManager.loadScene("settings", "/reversi/resources/fxmls/SettingsScene.fxml");
        sceneManager.addCommonBackground();
        sceneManager.setScene("start");
    }

    public static GameLogic getGameLogic() {
        return gameLogic;
    }

    public static void startLocalGameWithoutAI() {
        gameLogic.initialize(GameType.LOCAL_GAME);
        sceneManager.switchScene("game");
    }

    public static void startLocalGameWithAI() {
        gameLogic.initialize(GameType.LOCAL_GAME_AI);
        sceneManager.switchScene("game");
    }

    public static void restartGame() {
        gameLogic.restartGame();
        sceneManager.switchScene("game");
    }

    public static void stopGame() {
        gameLogic.exitGame();
        sceneManager.switchScene("start");
    }

    public static String[] getFilenameList() {
        return gameLogic.getFilenameList();
    }

    public static void openFile(String filename) {
        try {
            gameLogic.loadGame(filename);
        } catch (Exception e) {
            sceneManager.showHeadHint("Invalid Game File.", null);
            return;
        }
        sceneManager.switchScene("game");
        sceneManager.showHeadHint("File Loaded!", null);
    }

    public static void startServer() {
        currentConnectionIsServer = true;
        sceneManager.switchScene("wait");
        localServerService = new LocalServerService();
        localServerService.setOnSucceeded(event -> connectionEstablished());
        localServerService.setOnFailed(event -> connectionFailed());
        localServerService.start();
    }

    public static void cancelConnection() {
        if (currentConnectionIsServer) {
            localServerService.cancel();
            sceneManager.switchScene("start");
            sceneManager.showHeadHint("Server Creation Cancelled.", null);
        } else {
            clientConnectionTask.cancel(true);
            startClient();
            sceneManager.showHeadHint("Connection Cancelled.", null);
        }
    }

    public static void startClient() {
        // client task is done within the client scene,
        // although this is not a ui should do, but can make
        // the structure clearer.
        sceneManager.switchScene("client");
    }

    // only for client
    public static void startConnection(String hostName) {
        currentConnectionIsServer = false;
        sceneManager.switchScene("wait");
        clientConnectionTask = new ClientConnectionTask(hostName);
        clientConnectionTask.setOnFailed(event -> connectionFailed());
        clientConnectionTask.setOnSucceeded(event -> connectionEstablished());
        new Thread(clientConnectionTask).start();
    }

    private static void connectionFailed() {
        sceneManager.switchScene("start");
        sceneManager.showHeadHint("Connection Failed!", "error_h.png");
    }

    private static void connectionEstablished() {
        gameLogic.setNetSelfPlayer(AppLogic.ownerIsBlack ? Owner.BLACK : Owner.WHITE);
        if (currentConnectionIsServer)
            gameLogic.initializeNetService(localServerService.getValue());
        else
            gameLogic.initializeNetService(clientConnectionTask.getValue());
        gameLogic.initialize(GameType.ONLINE_GAME);
        sceneManager.switchScene("game");
        sceneManager.showHeadHint("Connected.", "ok_h.png");
    }

    public static void disconnectCallback() {
        if (gameLogic.getGameType() == GameType.ONLINE_GAME && !isCurrentConnectionIsServer())
            startClient();
        else
            sceneManager.switchScene("start");
    }

    public static void setSceneManager(SceneManager sceneManager) {
        AppLogic.sceneManager = sceneManager;
    }

    public static void endGame() {
        gameLogic.exitGame();
        sceneManager.switchScene("result");
    }

}
