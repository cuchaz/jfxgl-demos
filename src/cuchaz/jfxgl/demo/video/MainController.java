/*************************************************************************
 * Copyright (C) 2017, Jeffrey W. Martin "Cuchaz"
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License Version 2 with
 * the classpath exception, as published by the Free Software Foundation.
 * 
 * See LICENSE.txt in the project root folder for the full license.
 *************************************************************************/
package cuchaz.jfxgl.demo.video;

import cuchaz.jfxgl.CalledByEventsThread;
import cuchaz.jfxgl.CalledByMainThread;
import cuchaz.jfxgl.JFXGL;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;

public class MainController {
	
	@FXML private CheckBox playCheck;
	@FXML private Slider timeSlider;
	
	private long lastUIUpdateMs;
	
	public volatile boolean isPlaying;

	@FXML
	@CalledByEventsThread
	public void initialize() {
		
		// listen for events
		playCheck.selectedProperty().addListener((observed, oldVal, newVal) -> {
			isPlaying = newVal;
		});
		
		// init defaults
		timeSlider.disableProperty().set(true);
	}
	
	@CalledByMainThread
	public void init(long numFrames, boolean isPlaying) {
		
		this.isPlaying = isPlaying;
		
		// update the UI
		JFXGL.runOnEventsThread(() -> {
			playCheck.selectedProperty().set(isPlaying);
			timeSlider.minProperty().set(0);
			timeSlider.maxProperty().set(numFrames);
			timeSlider.valueProperty().set(0);
		});
	}
	
	@CalledByMainThread
	public boolean update(long frame) {
		
		long nowMs = System.nanoTime()/1000/1000;
		
		// update the UI sometimes
		long elapsedMs = nowMs - lastUIUpdateMs;
		if (elapsedMs > 16) {
			lastUIUpdateMs = nowMs;
			JFXGL.runOnEventsThread(() -> {
				
				// sync the slider
				timeSlider.valueProperty().set(frame);
			});
		}
		
		return isPlaying;
	}
}
