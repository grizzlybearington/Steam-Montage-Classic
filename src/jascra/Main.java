package jascra;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			AnchorPane root = (AnchorPane)FXMLLoader.load(getClass().getResource("SMM.fxml"));
			Scene scene = new Scene(root,502,297);
			scene.getStylesheets().add(getClass().getResource("SMM.css").toExternalForm());
			primaryStage.getIcons().add(new Image(getClass().getResource("images/smlogo-icon.png").toExternalForm()));
			primaryStage.setScene(scene);
			primaryStage.setTitle("Steam Montage");
			primaryStage.setResizable(false);
			primaryStage.sizeToScene();
			primaryStage.show(); 
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}