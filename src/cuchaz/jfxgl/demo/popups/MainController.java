package cuchaz.jfxgl.demo.popups;


import cuchaz.jfxgl.CalledByEventsThread;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;

public class MainController {

	@FXML private ChoiceBox<String> choice;
	@FXML private Button dialogButton;
	
	@FXML
	@CalledByEventsThread
	public void initialize() {
		
		choice.getItems().setAll("Cheese", "Beer", "Cake");
		choice.getSelectionModel().select(0);
		
		dialogButton.setOnAction((e) -> {
			Dialog<?> dialog = new Dialog<>();
			dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
			dialog.setContentText("Hello World!");
			dialog.show();
		});
	}
}
