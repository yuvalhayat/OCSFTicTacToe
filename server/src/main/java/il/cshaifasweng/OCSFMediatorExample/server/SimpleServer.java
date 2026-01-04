package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.MoveRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.Response;
import il.cshaifasweng.OCSFMediatorExample.entities.SymbolRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.ArrayList;

import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;

import static il.cshaifasweng.OCSFMediatorExample.entities.Response.*;
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
                SubscribedClient connection = new SubscribedClient(client);
                SubscribersList.add(connection);
                System.out.println("Sub list size: "+SubscribersList.size());
                try {
                    if(SubscribersList.size()==1){
                        Warning warning = new Warning(Response.waitInQueue,null);
                        client.sendToClient(warning);
                        System.out.println("Sub list size == 1 ");

                    }
                    else{
                        this.setUpNewGame();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if(msgString.startsWith("remove client")){
                if(!SubscribersList.isEmpty()){
                    for(SubscribedClient subscribedClient: SubscribersList){
                        if(subscribedClient.getClient().equals(client)){
                            SubscribersList.remove(subscribedClient);
                            Warning warning = new Warning(Response.waitInQueue,null);
                            sendToAllClients(warning);
                            break;
                        }
                    }
                }
                System.out.println("Sub list size: "+SubscribersList.size());
            }
        }
        else if(msg instanceof MoveRequest){
            System.out.println("got move request");

            MoveRequest move = (MoveRequest)msg;
            int row = move.getRow();
            int col = move.getCol();
            char symbol = move.getSymbol();
            if(!game.isMoveValid(symbol,row,col)){
                System.out.println("invalid move by " + symbol + " " + row + " " + col);
                return;
            }
            GameStatus status = game.playMove(symbol,row,col);
            Warning warning = new Warning(Response.updateGame,move);
            sendToAllClients(warning);

            try {
                if(status==GameStatus.playerXWon || status==GameStatus.playerOWon){
                    int winningPlayerIndex;
                    int losingPlayerIndex;
                    if( status==GameStatus.playerXWon){
                        winningPlayerIndex = getPlayerIndex('X');
                        losingPlayerIndex = getPlayerIndex('O');
                    }
                    else{
                        winningPlayerIndex = getPlayerIndex('O');
                        losingPlayerIndex = getPlayerIndex('X');
                    }
                    ConnectionToClient winningPlayer = SubscribersList.get(winningPlayerIndex).getClient();
                    ConnectionToClient losingPlayer = SubscribersList.get(losingPlayerIndex).getClient();

                    Warning winningWarning = new Warning(Response.showWinningScreen,null);
                    Warning losingWarning = new Warning(Response.showLosingScreen,null);

                    winningPlayer.sendToClient(winningWarning);
                    losingPlayer.sendToClient(losingWarning);

                    Thread.sleep(secondsToWaitBetweenGames * 1000);
                    setUpNewGame();
                }
                else if(status==GameStatus.draw){
                    System.out.println("draw");

                    warning = new Warning(Response.showDrawingScreen,null);
                    sendToAllClients(warning);
                    Thread.sleep(secondsToWaitBetweenGames * 1000);
                    setUpNewGame();
                }
                else if (status==GameStatus.gameContinue){
                    System.out.println("game continue");

                    warning =  new Warning(Response.whoseTurn,game.getWhoseTurn());
                    sendToAllClients(warning);
                }
                else{
                    System.err.println("invalid move by " + symbol + " " + row + " " + col);
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }


        }
        else{
            System.err.println("wrong message format");

        }
	}
    
	public void sendToAllClients(Warning warning) {
		try {
			for (SubscribedClient subscribedClient : SubscribersList) {
				subscribedClient.getClient().sendToClient(warning);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

    private char getClientSymbol(int index ){
        return game.getSymbolOfPlayer(index);
    }

    private void setUpNewGame() throws IOException{
        System.out.println("Starting game ");

        Warning warning = new Warning(Response.gameScreen,null);
        this.sendToAllClients(warning);
        ConnectionToClient client1 = SubscribersList.get(0).getClient();
        ConnectionToClient client2 = SubscribersList.get(1).getClient();

        game = new ticTacToeGame(0, 1);

        char symbol1 = getClientSymbol(0);
        char symbol2 = getClientSymbol(1);

        SymbolRequest symbolRequest1 = new SymbolRequest(symbol1,symbol2);
        SymbolRequest symbolRequest2 = new SymbolRequest(symbol2,symbol1);

        warning = new Warning(Response.setSymbol,symbolRequest1);
        client1.sendToClient(warning);

        warning = new Warning(Response.setSymbol,symbolRequest2);
        client2.sendToClient(warning);

        warning =  new Warning(Response.whoseTurn,game.getWhoseTurn());
        sendToAllClients(warning);
    }

    private int getPlayerIndex(char Symbol){
        if(Symbol==getClientSymbol(0)){
            return 0;
        }
        return 1;
    }
}
