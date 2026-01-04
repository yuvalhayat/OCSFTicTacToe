package il.cshaifasweng.OCSFMediatorExample.server;
import java.util.Random;

public class ticTacToeGame {
    //X always goes first
    private int playerXId;
    private int playerOId;

    private int availableSquares = 9;
    private char[][] board = new char[3][3];
    private char whoseTurn;

    public ticTacToeGame(int player1Id,int player2Id){
        whoseTurn = 'X';
        Random random = new Random();
        boolean randomBoolean =  random.nextBoolean();
        if(randomBoolean){
            playerXId = player1Id;
            playerOId = player2Id;
        }
        else{
            playerXId = player2Id;
            playerOId = player1Id;
        }
    }

    public GameStatus playMove(char symbol, int row, int col){
        if(!isMoveValid(symbol,row,col)){
            return GameStatus.invalidMove;
        }

        board[row][col]  = symbol;
        availableSquares--;
        whoseTurn = symbol == 'X' ? 'O' : 'X';

        if(hasPlayerWon('X'))
            return GameStatus.playerXWon;

        if(hasPlayerWon('O'))
            return GameStatus.playerOWon;

        if(availableSquares==0)
            return GameStatus.draw;

        return GameStatus.gameContinue;
    }

    public boolean isMoveValid(char symbol,int row,int col){
        if(symbol!=this.getWhoseTurn()){
            return false;
        }
        if((row<0 || row>=3)||(col<0 || col>=3)){
            return false;
        }
        if(board[row][col]!=0){
            return false;
        }
        return true;
    }

    public boolean hasPlayerWon(char symbol){
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == symbol && board[i][1] == symbol && board[i][2] == symbol) {
                return true;
            }
        }

        // Check all three columns
        for (int j = 0; j < 3; j++) {
            if (board[0][j] == symbol && board[1][j] == symbol && board[2][j] == symbol) {
                return true;
            }
        }

        // Check the main diagonal (top-left to bottom-right)
        if (board[0][0] == symbol && board[1][1] == symbol && board[2][2] == symbol) {
            return true;
        }

        // Check the anti-diagonal (top-right to bottom-left)
        if(board[0][2] == symbol && board[1][1] == symbol && board[2][0] == symbol) {
            return true;
        }

        return false;
    }

    public int getPlayerXId() {return playerXId;}
    public int getPlayerOId() {return playerOId;}
    public char getWhoseTurn() {return whoseTurn;}
    public char getSymbolOfPlayer(int playerId) {
        return playerId == playerXId ? 'X':'O';
    }

    /** next:
     *  at the beginning:create game and check the symbol of each player and send it to them
     *  in each iteration:
     *  tell both whose turn is it X
     *  when we get a move request,we check if it's valid and if it's that player turn v
     *  (latter the last one is true we dismiss the request entirly) v
     *  otherwise,we send both player the move x
     *  if it was a win/loss/draw,we send the players the win/loss msg v
     * */
}
