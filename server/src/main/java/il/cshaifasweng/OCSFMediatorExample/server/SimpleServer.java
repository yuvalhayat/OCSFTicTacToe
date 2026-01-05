package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.MoveRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.Response;
import il.cshaifasweng.OCSFMediatorExample.entities.SymbolRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.ArrayList;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;

import il.cshaifasweng.OCSFMediatorExample.server.GameStatus.*;

public class SimpleServer extends AbstractServer {
	private static ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();

    private ticTacToeGame game;
    private int secondsToWaitBetweenGames = 5;

    public SimpleServer(int port) {
		super(port);
		
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

        if (msg instanceof String) {
            String msgString = msg.toString();
            if(msgString.startsWith("add client")){

                System.out.println("-----------------------------------------");
                System.out.println("Adding client");

                SubscribedClient connection = new SubscribedClient(client);
                SubscribersList.add(connection);

                System.out.println("Subscriber's list size: "+SubscribersList.size());

                try {
                    if(SubscribersList.size()==1){
                        Message message = new Message(Response.waitInQueue,null);
                        client.sendToClient(message);

                        System.out.println("sending " + client.toString() + " to queue");

                    }
                    else{
                        this.setUpNewGame();
                        System.out.println("New Game Started");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if(msgString.startsWith("remove client")){
                if(!SubscribersList.isEmpty()){

                    System.out.println("-----------------------------------------");
                    System.out.println("removing client");

                    for(SubscribedClient subscribedClient: SubscribersList){
                        if(subscribedClient.getClient().equals(client)){
                            SubscribersList.remove(subscribedClient);
                            System.out.println("Subscriber's list size: "+SubscribersList.size());

                            System.out.println("sending all other clients to queue");
                            Message message = new Message(Response.waitInQueue,null);
                            sendToAllClients(message);
                            break;
                        }
                    }
                }
            }
        }
        else if(msg instanceof MoveRequest){

            MoveRequest move = (MoveRequest)msg;
            int row = move.getRow();
            int col = move.getCol();
            char symbol = move.getSymbol();

            //we dismiss the request if it was sent by a player not on his turn
            if(game.getWhoseTurn()!=symbol){return;}


            System.out.println("-----------------------------------------");
            System.out.println("Got move request by "+ symbol + " at (" + row + "," + col+")");

            GameStatus status = game.playMove(symbol,row,col);
            Message message = new Message(Response.updateGame,move);
            sendToAllClients(message);

            try {
                if(status==GameStatus.playerXWon || status==GameStatus.playerOWon){
                    int winningPlayerIndex;
                    int losingPlayerIndex;
                    if( status==GameStatus.playerXWon){
                        System.out.println("Player X won");

                        winningPlayerIndex = getPlayerIndex('X');
                        losingPlayerIndex = getPlayerIndex('O');
                    }
                    else{
                        System.out.println("Player O won");
                        winningPlayerIndex = getPlayerIndex('O');
                        losingPlayerIndex = getPlayerIndex('X');
                    }

                    ConnectionToClient winningPlayer = SubscribersList.get(winningPlayerIndex).getClient();
                    ConnectionToClient losingPlayer = SubscribersList.get(losingPlayerIndex).getClient();

                    Message winningMessage = new Message(Response.showWinningScreen,null);
                    Message losingMessage = new Message(Response.showLosingScreen,null);

                    winningPlayer.sendToClient(winningMessage);
                    losingPlayer.sendToClient(losingMessage);

                    Thread.sleep(secondsToWaitBetweenGames * 1000);
                    System.out.println("Restarting the game");
                    setUpNewGame();
                }
                else if(status==GameStatus.draw){
                    System.out.println("Game ended in a draw");

                    message = new Message(Response.showDrawingScreen,null);
                    sendToAllClients(message);

                    Thread.sleep(secondsToWaitBetweenGames * 1000);
                    System.out.println("Restarting the game");
                    setUpNewGame();
                }
                else if (status==GameStatus.gameContinue){
                    System.out.println("The game continues");

                    message =  new Message(Response.whoseTurn,game.getWhoseTurn());
                    sendToAllClients(message);
                }
                else{
                    System.err.println("Invalid move");
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
        else{
            System.err.println("Wrong message format");

        }
	}
    
	public void sendToAllClients(Message message) {
		try {
			for (SubscribedClient subscribedClient : SubscribersList) {
				subscribedClient.getClient().sendToClient(message);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

    /*
    * get the symbol of the client in a certain index in the list
    * */
    private char getClientSymbol(int index ){
        return game.getSymbolOfPlayer(index);
    }

    /*
     * set up a new game and send the players massages for the set up of the game
     * */
    private void setUpNewGame() throws IOException{
        Message message = new Message(Response.gameScreen,null);
        this.sendToAllClients(message);
        ConnectionToClient client1 = SubscribersList.get(0).getClient();
        ConnectionToClient client2 = SubscribersList.get(1).getClient();

        game = new ticTacToeGame(0, 1);

        char symbol1 = getClientSymbol(0);
        char symbol2 = getClientSymbol(1);

        SymbolRequest symbolRequest1 = new SymbolRequest(symbol1,symbol2);
        SymbolRequest symbolRequest2 = new SymbolRequest(symbol2,symbol1);

        message = new Message(Response.setSymbol,symbolRequest1);
        client1.sendToClient(message);

        message = new Message(Response.setSymbol,symbolRequest2);
        client2.sendToClient(message);

        message =  new Message(Response.whoseTurn,game.getWhoseTurn());
        sendToAllClients(message);
    }

    /*
    * get a symbol of one of the player and return the index of the player in the list
    * whose symbol equal to the argument
    * */
    private int getPlayerIndex(char Symbol){
        if(Symbol==getClientSymbol(0)){
            return 0;
        }
        return 1;
    }
}
