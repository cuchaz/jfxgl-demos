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

public class FrameTimer {

	private static final long NSpMS = 1000000;
	private static final long NSpS = NSpMS*1000;
	private static final long UpdateIntervalNS = 1000*NSpMS; // 1 second
	private static final int MinNumFrames = 10;

	private int numFrames;
	private long startTime;
	private boolean isFirst;
	
	public float fps;
	
	public FrameTimer() {
		numFrames = 0;
		startTime = 0;
		isFirst = true;
		fps = 0;
	}

	public void update() {
		
		long now = System.nanoTime();

		if (isFirst) {
			isFirst = false;
			numFrames = 0;
			startTime = now;
			return;
		}
		
		// look, a frame!
		numFrames++;

		// should we update our estimate?
		long elapsed = now - startTime;
		if (elapsed > UpdateIntervalNS && numFrames > MinNumFrames) {
			
			// yup
			fps = (float)numFrames*NSpS/elapsed;
			
			// and reset the counters
			startTime = now;
			numFrames = 0;
		}
	}
}
