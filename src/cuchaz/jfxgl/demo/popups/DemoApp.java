/*************************************************************************
 * Copyright (C) 2017, Jeffrey W. Martin "Cuchaz"
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License Version 2 with
 * the classpath exception, as published by the Free Software Foundation.
 * 
 * See LICENSE.txt in the project root folder for the full license.
 *************************************************************************/
package cuchaz.jfxgl.demo.popups;

import java.io.IOException;

import cuchaz.jfxgl.CalledByEventsThread;
import cuchaz.jfxgl.CalledByMainThread;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DemoApp extends Application {
		
	@CalledByMainThread
	public DemoApp() {
		// nothing to do
	}
	
	@Override
	@CalledByEventsThread
	public void start(Stage stage)
	throws IOException {
		
		// load the main fxml
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("Main.fxml"));
		Scene scene = new Scene(loader.load());
		stage.setScene(scene);
		
		// set transparency for ui overlay
		scene.setFill(null);
		stage.initStyle(StageStyle.TRANSPARENT);
		
		// the window is actually already showing, but JavaFX doesn't know that yet
		// so make JavaFX catch up by "showing" the window
		stage.show();
	}
}
