package reversi.logic;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import reversi.ai.AIService;
import reversi.net.NetEvent;
import reversi.net.NetMessage;
import reversi.net.ReceiveMessageService;
import reversi.net.SendMessageService;
import reversi.ui.SceneGameController;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Huang Jiahui, 2014011330, 2016/7/26 0026.
 */
final public class GameLogic {

    // 8 directions for flip search.
    private static final int[] DR = {1, 1, 0, -1, -1, -1, 0, 1};
    private static final int[] DC = {0, 1, 1, 1, 0, -1, -1, -1};

    private GameType gameType;
    private Owner[][] board = new Owner[8][8];

    // indicates whether current player can chess here.
    private boolean[][] hintBoard = new boolean[8][8];
    private Owner currentPlayer;

    // when playing online game, the player user is.
    private Owner netSelfPlayer;
    private Owner winner = Owner.NONE;
    private int blackCount;
    private int whiteCount;
    private SceneGameController UIController;
    private Timeline secondTimer;
    private int currentTimeLeft;

    private int whiteWithdrawCount = 0;
    private int blackWithdrawCount = 0;

    private Socket socket;
    private ReceiveMessageService receiveMessageService;
    private SendMessageService sendMessageService;
    private AIService aiService = new AIService();

    private LinkedList<HistoryStatus> historyStatusList = new LinkedList<>();
    private FileHandler fileHandler = new FileHandler();

    GameLogic() {
        secondTimer = new Timeline(new KeyFrame(Duration.seconds(1.0),
                event -> onOneSecondPassed()));
        secondTimer.setCycleCount(Animation.INDEFINITE);
    }

    void initialize(GameType type) {
        secondTimer.stop();

        this.gameType = type;
        this.winner = Owner.NONE;
        this.whiteWithdrawCount = 0;
        this.blackWithdrawCount = 0;

        for (int i = 0; i < 8; ++ i)
            for (int j = 0; j < 8; ++ j) {
                board[i][j] = Owner.NONE;
                hintBoard[i][j] = false;
            }
        board[3][3] = Owner.WHITE;
        board[4][4] = Owner.WHITE;
        board[3][4] = Owner.BLACK;
        board[4][3] = Owner.BLACK;

        currentPlayer = Owner.BLACK;
        blackCount = 2;
        whiteCount = 2;

        historyStatusList.clear();
        historyStatusList.add(new HistoryStatus(this.board, Owner.BLACK));

        updateHintBoard();
        if ((gameType == GameType.ONLINE_GAME && currentPlayer != netSelfPlayer) ||
                checkShouldAIMove())
            UIController.setHintEnabled(false);
        else
            UIController.setHintEnabled(true);

        UIController.updateBoard();

        if (gameType == GameType.ONLINE_GAME) {
            currentTimeLeft = AppLogic.getSecsThink();
        } else {
            currentTimeLeft = AppLogic.localThinkTime;
        }
        UIController.updateTimeCount(currentTimeLeft);

        if (checkShouldAIMove())
            AIMove();

        secondTimer.playFromStart();
    }

    private void AIMove() {
        aiService.setOnSucceeded(event ->
                chessAction(aiService.getValue().row, aiService.getValue().col));
        aiService.start(board, currentPlayer, AppLogic.AILevel);
    }

    private boolean checkShouldAIMove() {
        return (gameType == GameType.LOCAL_GAME_AI && currentPlayer == AppLogic.AIPlayer);
    }

    public int getBlackCount() {
        return blackCount;
    }

    public int getWhiteCount() {
        return whiteCount;
    }

    public void pauseCountDown() {
        secondTimer.pause();
    }

    public void resumeCountDown() {
        secondTimer.play();
    }

    private void onOneSecondPassed() {
        // when playing online games, timer is defined by Black owner
        if (gameType == GameType.ONLINE_GAME && netSelfPlayer == Owner.WHITE)
            return;
        currentTimeLeft -= 1;
        // assert self player is black
        if (gameType == GameType.ONLINE_GAME)
            sendMessage(new NetMessage(NetEvent.TIC, String.valueOf(currentTimeLeft)));
        UIController.updateTimeCount(currentTimeLeft);
        if (currentTimeLeft == 0) {
            UIController.notifyTimeUp();
            startNewRound();
        }
    }

    void setNetSelfPlayer(Owner netSelfPlayer) {
        this.netSelfPlayer = netSelfPlayer;
    }

    private void initializeWithFile(GameType type, String filename) {
        secondTimer.stop();

        this.gameType = type;
        this.winner = Owner.NONE;
        this.whiteWithdrawCount = 0;
        this.blackWithdrawCount = 0;

        historyStatusList.clear();
        fileHandler.loadFromFile(filename);
        historyStatusList = fileHandler.getLoadedHistoryStatus();

        currentPlayer = historyStatusList.get(0).getPlayer();
        historyStatusList.get(0).copyTo(this.board);

        updateHintBoard();
        updateCount();

        if (checkShouldAIMove())
            UIController.setHintEnabled(false);
        else
            UIController.setHintEnabled(true);
        UIController.updateBoard();
        UIController.systemMessage("File Loaded!");

        currentTimeLeft = AppLogic.localThinkTime;
        UIController.updateTimeCount(currentTimeLeft);

        if (checkShouldAIMove())
            AIMove();

        secondTimer.playFromStart();

    }

    void initializeNetService(Socket socket) {
        this.socket = socket;
        receiveMessageService = new ReceiveMessageService(socket);
        receiveMessageService.setOnSucceeded(event -> onReceiveMessage());
        receiveMessageService.setOnFailed(event -> onDisconnected());
        sendMessageService = new SendMessageService(socket);
        receiveMessageService.start();
        sendMessageService.start();
    }

    Owner getWinner() {
        return winner;
    }

    private void onDisconnected() {
        stopNetService();
        UIController.onDisconnected();
    }

    void stopNetService() {
        receiveMessageService.cancel();
        sendMessageService.cancel();
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("WARNING: Socket Close Error.");
        }
    }

    public GameType getGameType() {
        return gameType;
    }

    private void sendMessage(NetMessage messageToSend) {
        sendMessageService.sendMessage(messageToSend);
    }

    private void onReceiveMessage() {
        NetMessage receivedMessage = receiveMessageService.getValue();
        switch (receivedMessage.getEvent()) {
            case CHESS: {
                String[] content = receivedMessage.getContent().split(" ");
                chessAction(Integer.parseInt(content[0]),
                        Integer.parseInt(content[1]));
                break;
            }
            case WITHDRAW: {
                char optCode = receivedMessage.getContent().charAt(0);
                if (optCode == '0')
                    UIController.promptWithdraw();
                else if (optCode == '1') {
                    withdrawChess();
                    UIController.onWithdrawSucceed();
                } else if (optCode == '2')
                    UIController.onWithdrawFailed();
                break;
            }
            case TIC: {
                int timeLeft;
                try {
                    timeLeft = Integer.parseInt(receivedMessage.getContent());
                } catch (Exception ignored) {
                    timeLeft = currentTimeLeft - 1;
                }
                currentTimeLeft = timeLeft;
                UIController.updateTimeCount(currentTimeLeft);
                if (currentTimeLeft == 0) {
                    UIController.notifyTimeUp();
                    startNewRound();
                }
                break;
            }
            case NEW: {
                char optCode = receivedMessage.getContent().charAt(0);
                if (optCode == '0')
                    UIController.promptNew();
                else if (optCode == '1') {
                    AppLogic.restartGame();
                    UIController.onNewSucceed();
                } else if (optCode == '2')
                    UIController.onNewFailed();
                break;
            }
            case MESSAGE: {
                UIController.onChatMessage(receivedMessage.getContent());
            }
            default:
                break;
        }
    }

    public void setUIController(SceneGameController controller) {
        this.UIController = controller;
    }

    private void updateCount() {
        whiteCount = 0;
        blackCount = 0;
        for (int i = 0; i < 8; ++ i)
            for (int j = 0; j < 8; ++ j) {
                if (board[i][j] == Owner.BLACK) ++ blackCount;
                if (board[i][j] == Owner.WHITE) ++ whiteCount;
            }
    }

    private Owner getOpponentOwner() {
        return (currentPlayer == Owner.BLACK) ? Owner.WHITE : Owner.BLACK;
    }

    /**
     * update hint board for current player.
     */
    private int updateHintBoard() {
        int availableSteps = 0;
        for (int i = 0; i < 8; ++ i)
            for (int j = 0; j < 8; ++ j) {
                hintBoard[i][j] = calculateHintCell(i, j);
                if (hintBoard[i][j]) ++ availableSteps;
            }
        return availableSteps;
    }

    private boolean calculateHintCell(int row, int col) {
        if (board[row][col] != Owner.NONE) return false;

        Owner opponentPlayer = getOpponentOwner();

        for (int i = 0; i < 8; ++ i) {
            int flipCount = getDirectionFlipCount(row, col, DR[i], DC[i], opponentPlayer);
            if (flipCount != -1) return true;
        }

        return false;
    }

    private int getDirectionFlipCount(int row, int col, int dr, int dc,
                                      Owner opponentPlayer) {
        int count = 0;
        while ((row + dr) < 8 && (row + dr) >= 0 &&
                (col + dc) < 8 && (col + dc) >= 0) {
            row += dr;
            col += dc;
            if (board[row][col] == currentPlayer) {
                if (count != 0) return count;
                else return -1;
            } else if (board[row][col] == opponentPlayer) {
                count += 1;
            } else if (board[row][col] == Owner.NONE) {
                return -1;
            }
        }
        return -1;
    }

    public Owner getBoard(int row, int col) {
        return board[row][col];
    }

    public boolean getHint(int row, int col) {
        return hintBoard[row][col];
    }

    public Owner getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean currentPlayerCanChess() {
        return (currentPlayer == netSelfPlayer);
    }

    public boolean chessAction(int row, int col) {
        if (!hintBoard[row][col]) return false;

        // should notify other player.
        if (gameType == GameType.ONLINE_GAME) {
            if (currentPlayer == netSelfPlayer) {
                sendMessage(new NetMessage(NetEvent.CHESS, row + " " + col));
            }
        }

        board[row][col] = currentPlayer;
        UIController.addChessHistory(currentPlayer, row, col);
        Owner opponentPlayer = getOpponentOwner();

        for (int i = 0; i < 8; ++ i) {
            int flipCount = getDirectionFlipCount(row, col, DR[i], DC[i], opponentPlayer);
            if (flipCount != -1) {
                for (int j = 1; j <= flipCount; ++ j)
                    board[row + j * DR[i]][col + j * DC[i]] = currentPlayer;
            }
        }
        updateCount();

        startNewRound();
        return true;
    }


    /**
     * must be called before each round start.
     */
    private GameStatus prepareNewRound() {
        updateCount();
        int opponentSteps = updateHintBoard();
        currentPlayer = getOpponentOwner();
        int availableSteps = updateHintBoard();

        if (availableSteps == 0) {
            // test whether game is over by probing
            //   whether opponent has next step
            if (opponentSteps == 0) {
                if (blackCount == whiteCount)
                    return GameStatus.TIE;
                if (blackCount > whiteCount)
                    return GameStatus.BLACK_WIN;
                else
                    return GameStatus.WHITE_WIN;
            } else
                return GameStatus.BLOCK;
        }
        return GameStatus.NORMAL;
    }

    private void startNewRound() {
        if (aiService.isRunning())
            aiService.cancel();

        GameStatus gameStatus = prepareNewRound();
        historyStatusList.addFirst(new HistoryStatus(this.board, currentPlayer));
        if ((gameType == GameType.ONLINE_GAME && currentPlayer != netSelfPlayer) ||
                checkShouldAIMove())
            UIController.setHintEnabled(false);
        else
            UIController.setHintEnabled(true);
        UIController.updateBoard();

        if (gameType == GameType.ONLINE_GAME) {
            currentTimeLeft = AppLogic.getSecsThink();
        } else {
            currentTimeLeft = AppLogic.localThinkTime;
        }
        UIController.updateTimeCount(currentTimeLeft);
        secondTimer.stop();

        if (checkShouldAIMove())
            AIMove();

        secondTimer.playFromStart();

        switch (gameStatus) {
            case BLACK_WIN:
                winner = Owner.BLACK;
                UIController.onGameOver();
                break;
            case WHITE_WIN:
                winner = Owner.WHITE;
                UIController.onGameOver();
                break;
            case BLOCK:
                UIController.notifyBlock(currentPlayer);
                startNewRound();
                break;
            case TIE:
                winner = Owner.NONE;
                UIController.onGameOver();
                break;
            case NORMAL:
                break;
        }
    }

    public boolean checkCanWithdrawChess() {
        if (gameType == GameType.ONLINE_GAME && !currentPlayerCanChess())
            return false;
        if (gameType == GameType.LOCAL_GAME_AI && currentPlayer == AppLogic.AIPlayer)
            return false;
        if (gameType != GameType.ONLINE_GAME) {
            if (currentPlayer == Owner.BLACK && blackWithdrawCount == 2)
                return false;
            if (currentPlayer == Owner.WHITE && whiteWithdrawCount == 2)
                return false;
        }
        Iterator<HistoryStatus> it = historyStatusList.iterator();
        it.next();
        boolean canWithdraw = false;
        while (it.hasNext()) {
            if (it.next().getPlayer() == currentPlayer) {
                canWithdraw = true;
                break;
            }
        }
        return !(!canWithdraw || (currentPlayer == Owner.BLACK && historyStatusList.size() == 1));
    }

    public void withdrawChess() {
        if (gameType != GameType.ONLINE_GAME) {
            if (currentPlayer == Owner.WHITE)
                ++ whiteWithdrawCount;
            else
                ++ blackWithdrawCount;
        }

        Iterator<HistoryStatus> it = historyStatusList.iterator();
        it.next(); it.remove();
        while (it.hasNext()) {
            HistoryStatus status = it.next();
            it.remove();
            if (status.getPlayer() == currentPlayer) {
                status.copyTo(this.board);
                break;
            }
        }
        currentPlayer = getOpponentOwner();
        startNewRound();
    }

    public void sendWithdrawRequest() {
        sendMessage(new NetMessage(NetEvent.WITHDRAW, "0"));
    }

    public void denyWithdrawRequest() {
        sendMessage(new NetMessage(NetEvent.WITHDRAW, "2"));
    }

    public void acceptWithdrawRequest() {
        sendMessage(new NetMessage(NetEvent.WITHDRAW, "1"));
        withdrawChess();
    }

    public void sendRestartRequest() {
        sendMessage(new NetMessage(NetEvent.NEW, "0"));
    }

    public void denyRestartRequest() {
        sendMessage(new NetMessage(NetEvent.NEW, "2"));
    }

    public void acceptRestartRequest() {
        sendMessage(new NetMessage(NetEvent.NEW, "1"));
        AppLogic.restartGame();
    }

    void exitGame() {
        if (gameType == GameType.ONLINE_GAME)
            stopNetService();
        if (gameType == GameType.LOCAL_GAME_AI)
            aiService.cancel();
        secondTimer.stop();
        UIController.closeRelativeWindows();
    }

    void restartGame() {
        initialize(gameType);
    }

    String[] getFilenameList() {
        return fileHandler.getAvailableFileList();
    }

    void loadGame(String filename) {
        initializeWithFile(gameType, filename);
    }

    public boolean saveGame(String filename) {
        boolean result = fileHandler.saveToFile(filename, this.historyStatusList);
        if (result)
            UIController.systemMessage("Game Saved!");
        return result;
    }

    public void sendTextMessage(String message) {
        if (gameType != GameType.ONLINE_GAME)
            return;
        sendMessage(new NetMessage(NetEvent.MESSAGE, message));
    }

}
