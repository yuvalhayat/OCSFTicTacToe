package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.MoveRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.Response;
import il.cshaifasweng.OCSFMediatorExample.entities.SymbolRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static il.cshaifasweng.OCSFMediatorExample.entities.Response.*;

/**
 * JavaFX App
 */
public class App extends Application {

    private Stage stage;
    private static FXMLLoader  fxmlLoader ;
    private static final String FXML_PATH = "il/cshaifasweng/OCSFMediatorExample/client/";

    //private static Scene scene;

    private SimpleClient client;

    @Override
    public void start(Stage stage) throws IOException {
    	EventBus.getDefault().register(this);
    	client = SimpleClient.getClient();
    	client.openConnection();
        this.stage = stage;
        /*
        scene = new Scene(loadFXML("queueScreen"), 640, 480);
        stage.setScene(scene);
        stage.show();*/
        client.sendToServer("add client");
    }

    /*
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }*/

    private static Parent loadFXML(String fxml) throws IOException {
        fxmlLoader = new FXMLLoader(App.class.getResource( fxml + ".fxml"));
        return fxmlLoader.load();
    }
    
    

    @Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
    	EventBus.getDefault().unregister(this);
        client.sendToServer("remove client");
        client.closeConnection();
		super.stop();
	}


    public void showScreen(String fxml) {
        try {
            URL url = getClass().getResource(fxml+".fxml");
            //System.out.println("Loading FXML: " + fxml + " → " + url);
           // System.out.println(url==null?"null":url.toString());
            if (url == null) {
                throw new IllegalStateException(
                        "FXML not found on classpath at: " + fxml +
                                "\nMake sure it is under src/main/resources/" + fxml
                );
            }

            //fxmlLoader = new FXMLLoader(url);

            Parent root = loadFXML(fxml);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            if (!stage.isShowing()) stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxml, e);
        }
    }

    /*
    * TODO:
    *  1.add setter method in gamescreen controller for the shape of the player
    *  2.finish the if else statements,for
    *  3.in App.java:
    *  change start() so it send to server that it has connected (and not load anything at the moment)
    *  4.in handleMessageFromClient in simpleServer implement:
    *  if it got sent: new client and the current client counter is 0
    *  (client counter is a synchronized variable in simpleServer) ,send waitInQueue to it and increment counter
    *  ,send waitInQueue for it,otherwise,send everyone gameScreen
    *  when it get sent
    *  logic that get get the indexs from user
    *  5
    *
    *
    *
    *
    *
    *
    * */
    @Subscribe
    public void onMessageEvent(MessageEvent event) throws IOException {
        Message message = event.getMessage();
        Response response = message.getResponse();

        if(response==showWinningScreen)
        {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.INFORMATION,
                        "You won!"
                );
                alert.show();
            });
        }
        else if(response==showLosingScreen){
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.INFORMATION,
                        "You lost :("
                );
                alert.show();
            });
        }
        else if(response==showDrawingScreen){
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.INFORMATION,
                        "The game ended in a draw"
                );
                alert.show();
            });
        }
        else if(response==waitInQueue){
            Platform.runLater(() -> {
                showScreen("queueScreen");
            });

        }
        else if(response==gameScreen){
            Platform.runLater(() -> {
                showScreen("gameScreen");
            });
        }
        else if(response==setSymbol){
            Platform.runLater(() -> {
                gameScreenController controller = fxmlLoader.getController();
                SymbolRequest symbolRequest = (SymbolRequest) message.getData();
                controller.handleSetSymbol(symbolRequest);
            });
        }
        else if (response==updateGame){
            Platform.runLater(() -> {
                gameScreenController controller = fxmlLoader.getController();
                MoveRequest moveRequest = (MoveRequest) message.getData();
                controller.handleMoveRequest(moveRequest);
            });
        }
        else if (response==whoseTurn){
            Platform.runLater(() -> {
                gameScreenController controller = fxmlLoader.getController();
                char symbol = (char) message.getData();
                controller.handleWhoseTurn(symbol);
            });
        }
        else{
            System.err.println("message in the wrong format recived");
        }
    	
    }

	public static void main(String[] args) {
        launch();
    }

}