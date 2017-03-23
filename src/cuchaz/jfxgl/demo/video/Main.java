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

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;

import cuchaz.jfxgl.JFXGL;
import cuchaz.jfxgl.demo.LWJGLDebug;
import cuchaz.jfxgl.prism.JFXGLContext;

public class Main {
	
	public static void main(String[] args)
	throws Exception {
		
		// init GLFW
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {
			throw new Error("Can't initialize GLFW");
		}
		
		// create the window
		int[] windowSize = { 600, 338 };
		long hwnd = GLFW.glfwCreateWindow(windowSize[0], windowSize[1], "JFXGL Demo", MemoryUtil.NULL, MemoryUtil.NULL);
		if (hwnd <= 0) {
			throw new Error("Can't create GLFW window");
		}
		
		// init opengl
		GLFW.glfwMakeContextCurrent(hwnd);
		GL.createCapabilities();
		Callback debugProc = LWJGLDebug.enableDebugging();
		
		// update the GL viewport when the window changes
		GLFWWindowSizeCallbackI windowSizeCallback = (long hwndAgain, int width, int height) -> {
			windowSize[0] = width;
			windowSize[1] = height;
			GL11.glViewport(0, 0, width, height);
		};
		GLFW.glfwSetWindowSizeCallback(hwnd, windowSizeCallback);
		
		GL11.glClearColor(0f, 0f, 0f, 1.0f);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		ImageRenderer image = null;
		VideoRenderer video = null;
		
		try {
			
			// start the app
			DemoApp app = new DemoApp();
			JFXGLContext context = JFXGL.start(hwnd, args, app);
			
			// init the image renderer
			image = new ImageRenderer(context);
			
			// init the video renderer
			video = new VideoRenderer(context);
			app.controller.init(video.getNumFrames(), video.isPlaying());
			
			// render loop
			while (!GLFW.glfwWindowShouldClose(hwnd)) {
				
				// clear the framebuf
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
				
				// sync with UI
				video.setPlaying(app.controller.update(video.getFrame()));
				
				// render
				image.render(windowSize[0], windowSize[1]);
				video.render(windowSize[0], windowSize[1]);

				// do JavaFX stuff
				JFXGL.render();
				
				GLFW.glfwSwapBuffers(hwnd);
				GLFW.glfwPollEvents();
			}
			
		} finally {
			
			// cleanup
			if (video != null) {
				video.cleanup();
			}
			JFXGL.terminate();
			debugProc.free();
			Callbacks.glfwFreeCallbacks(hwnd);
			GLFW.glfwDestroyWindow(hwnd);
			GLFW.glfwTerminate();
			GLFW.glfwSetErrorCallback(null).free();
		}
	}
}