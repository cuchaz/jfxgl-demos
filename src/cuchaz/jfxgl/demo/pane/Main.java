/*************************************************************************
 * Copyright (C) 2017, Jeffrey W. Martin "Cuchaz"
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License Version 2 with
 * the classpath exception, as published by the Free Software Foundation.
 * 
 * See LICENSE.txt in the project root folder for the full license.
 *************************************************************************/
package cuchaz.jfxgl.demo.pane;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;

import cuchaz.jfxgl.JFXGL;
import cuchaz.jfxgl.JFXGLLauncher;
import cuchaz.jfxgl.demo.LWJGLDebug;

public class Main {
	
	public static void main(String[] args) {
		JFXGLLauncher.launchMain(Main.class, args);
	}
	
	public static void jfxglmain(String[] args)
	throws Exception {
		
		// init GLFW
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {
			throw new Error("Can't initialize GLFW");
		}
		
		// create the window
		long hwnd = GLFW.glfwCreateWindow(600, 338, "JFXGL Demo - OpenGLPane", MemoryUtil.NULL, MemoryUtil.NULL);
		if (hwnd <= 0) {
			throw new Error("Can't create GLFW window");
		}
		
		// init opengl
		GLFW.glfwMakeContextCurrent(hwnd);
		GL.createCapabilities();
		Callback debugProc = LWJGLDebug.enableDebugging();
		
		// disable frame limiters (like vsync)
		GLFW.glfwSwapInterval(0);
		
		try {
			
			// start the app
			DemoApp app = new DemoApp();
			JFXGL.start(hwnd, args, app);
			
			// render loop
			JFXGL.renderLoop();
			
		} finally {
			
			// cleanup
			JFXGL.terminate();
			debugProc.free();
			Callbacks.glfwFreeCallbacks(hwnd);
			GLFW.glfwDestroyWindow(hwnd);
			GLFW.glfwTerminate();
			GLFW.glfwSetErrorCallback(null).free();
		}
	}
}
