package cuchaz.jfxgl.demo.popups;

import cuchaz.jfxgl.CalledByEventsThread;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

public class MainController {

	@FXML private ChoiceBox<String> choice;
	
	@FXML
	@CalledByEventsThread
	public void initialize() {
		choice.getItems().setAll("Cheese", "Beer", "Cake");
		choice.getSelectionModel().select(0);
	}
}
