/*************************************************************************
 * Copyright (C) 2017, Jeffrey W. Martin "Cuchaz"
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License Version 2 with
 * the classpath exception, as published by the Free Software Foundation.
 * 
 * See LICENSE.txt in the project root folder for the full license.
 *************************************************************************/
package cuchaz.jfxgl.demo;

import java.io.IOException;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import cuchaz.jfxgl.CalledByEventsThread;
import cuchaz.jfxgl.CalledByMainThread;
import cuchaz.jfxgl.JFXGL;
import cuchaz.jfxgl.controls.OpenGLPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HelloWorldPane {
	
	public static void main(String[] args)
	throws Exception {
		
		// create a window using GLFW
		GLFW.glfwInit();
		long hwnd = GLFW.glfwCreateWindow(300, 169, "JFXGL", MemoryUtil.NULL, MemoryUtil.NULL);
		
		// init OpenGL
		GLFW.glfwMakeContextCurrent(hwnd);
		GL.createCapabilities();
		
		try {
			
			// start the JavaFX app
			JFXGL.start(hwnd, args, new HelloWorldPaneApp());
			
			// render loop
			while (!GLFW.glfwWindowShouldClose(hwnd)) {
				
				// render the JavaFX UI
				JFXGL.render();
				
				GLFW.glfwSwapBuffers(hwnd);
				GLFW.glfwPollEvents();
			}
			
		} finally {
			
			// cleanup
			JFXGL.terminate();
			Callbacks.glfwFreeCallbacks(hwnd);
			GLFW.glfwDestroyWindow(hwnd);
			GLFW.glfwTerminate();
		}
	}
	
	public static class HelloWorldPaneApp extends Application {
		
		private OpenGLPane glpane;
		
		@Override
		@CalledByEventsThread
		public void start(Stage stage)
		throws IOException {
	
			// create the UI
			glpane = new OpenGLPane();
			glpane.setRenderer(() -> render());
			glpane.getChildren().add(new Label("Hello World!"));
			stage.setScene(new Scene(glpane));
		}
		
		@CalledByMainThread
		private void render() {
			
			GL11.glClearColor(0.8f, 0.5f, 0.5f, 1f);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		}
	}
}
