
package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.IOException;

import il.cshaifasweng.OCSFMediatorExample.entities.MoveRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.SymbolRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class gameScreenController {

    @FXML private Label statusLabel;
    private char mySymbol;
    private char opponentSymbol;

    private SimpleClient client;

    @FXML private GridPane board;

    private Button[][] boardButtons = new Button[3][3];


    @FXML
    /*
    * send a move request to the server after clicking a button
    * */
    void handleMove(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        try {
            SimpleClient.getClient().sendToServer(getMoveFromButton(clicked));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @FXML
    public void initialize() {
        for (Node node : board.getChildren()) {
            Button btn = (Button) node;

            int row = GridPane.getRowIndex(btn) == null ? 0 : GridPane.getRowIndex(btn);
            int col = GridPane.getColumnIndex(btn) == null ? 0 : GridPane.getColumnIndex(btn);

            boardButtons[row][col] = btn;
        }
    }

    /*
    * get a symbol that indicate whose move is it and update the text at the bottom to tell the player whose move is it
    * */
    void handleWhoseTurn(char symbol) {
        if(mySymbol == symbol) {
            statusLabel.setText("It's your turn!\nRemember,you are "+mySymbol);
        }
        else {
            statusLabel.setText("It's your opponent's turn!\nRemember, you are "+mySymbol);
        }
    }


    /*
    * set the symbol of the player for the rest of the game
    * */
    void handleSetSymbol(SymbolRequest symbolRequest) {
        this.mySymbol = symbolRequest.getMySymbol();
        this.opponentSymbol = symbolRequest.getOpponentSymbol();
    }

    /*
     * get a new move and update the board according to that move
     * */
    void handleMoveRequest(MoveRequest moveRequest) {
        int row = moveRequest.getRow();
        int col = moveRequest.getCol();
        char symbol = moveRequest.getSymbol();

        Button btn = boardButtons[row][col];
        btn.setText(String.valueOf(symbol));
        btn.setDisable(true);
    }

    private MoveRequest getMoveFromButton(Button button){
        String id = button.getId();

        int row = id.charAt(3) - '0';
        int col = id.charAt(4) - '0';
        return new MoveRequest(row,col,mySymbol);
    }
}
