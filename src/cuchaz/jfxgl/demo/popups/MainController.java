package cuchaz.jfxgl.demo.popups;


import java.io.File;
import java.util.List;

import cuchaz.jfxgl.CalledByEventsThread;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainController {

	@FXML private ChoiceBox<String> choice;
	@FXML private Button dialogButton;
	@FXML private Button pickFileButton;
	@FXML private Button pickFilesButton;
	@FXML private Button saveFileButton;
	@FXML private Button pickDirButton;
	
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
		
		pickFileButton.setOnAction((e) -> {
			File file = new FileChooser().showOpenDialog(null);
			System.out.println("picked file: " + file);
		});
		
		pickFilesButton.setOnAction((e) -> {
			FileChooser chooser = new FileChooser();
			chooser.setInitialDirectory(new File(System.getProperty("user.home")));
			chooser.getExtensionFilters().setAll(
				new ExtensionFilter("Images", "png", "jpg"),
				new ExtensionFilter("Cheeses", "red", "green")
			);
			List<File> files = chooser.showOpenMultipleDialog(null);
			System.out.println("picked files: " + files);
		});
		
		saveFileButton.setOnAction((e) -> {
			FileChooser chooser = new FileChooser();
			chooser.setInitialDirectory(new File(System.getProperty("user.home")));
			File file = chooser.showSaveDialog(null);
			System.out.println("saved file: " + file);
		});
		
		pickDirButton.setOnAction((e) -> {
			File dir = new DirectoryChooser().showDialog(null);
			System.out.println("picked folder: " + dir);
		});
	}
}
