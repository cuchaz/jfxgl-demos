/*************************************************************************
 * Copyright (C) 2017, Jeffrey W. Martin "Cuchaz"
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License Version 2 with
 * the classpath exception, as published by the Free Software Foundation.
 * 
 * See LICENSE.txt in the project root folder for the full license.
 *************************************************************************/
package cuchaz.jfxgl.demo.overlay;

import cuchaz.jfxgl.CalledByEventsThread;
import cuchaz.jfxgl.CalledByMainThread;
import cuchaz.jfxgl.JFXGL;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class MainController {
	
	@FXML private CheckBox spinCheck;
	@FXML private Slider rotationSlider;
	@FXML private Label fpsLabel;
	
	private long startTimeMs;
	private long lastUIUpdateMs;
	
	public volatile boolean isSpinning;
	public volatile float rotationRadians;

	@FXML
	@CalledByEventsThread
	public void initialize() {
		
		// listen for events
		spinCheck.selectedProperty().addListener((observed, oldVal, newVal) -> {
			isSpinning = newVal;
			rotationSlider.disableProperty().set(isSpinning);
		});
		rotationSlider.valueProperty().addListener((observed, oldVal, newVal) -> {
			rotationRadians = (float)Math.toRadians(newVal.doubleValue());
		});
		
		// init defaults
		isSpinning = true;
		spinCheck.selectedProperty().set(isSpinning);
		
		// start the timer
		startTimeMs = System.nanoTime()/1000/1000;
	}
	
	@CalledByMainThread
	public void update(float fps) {
		
		long nowMs = System.nanoTime()/1000/1000;
		
		if (isSpinning) {
			float elapsedS = (float)(nowMs - startTimeMs)/1000;
			rotationRadians = (float)(elapsedS*Math.PI);
		}
		
		// update the UI sometimes
		long elapsedMs = nowMs - lastUIUpdateMs;
		if (elapsedMs > 16) {
			lastUIUpdateMs = nowMs;
			JFXGL.runOnEventsThread(() -> {
				
				// sync the slider
				float degrees = (float)Math.toDegrees(rotationRadians);
				while (degrees < -180) {
					degrees += 360;
				}
				while (degrees > 180) {
					degrees -= 360;
				}
				rotationSlider.valueProperty().set(degrees);
				
				// update fps
				fpsLabel.textProperty().set(String.format("FPS: %.1f", fps));
			});
		}
	}
}
