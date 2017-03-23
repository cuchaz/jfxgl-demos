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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cuchaz.jfxgl.CalledByMainThread;
import cuchaz.jfxgl.InAppGLContext;
import cuchaz.jfxgl.prism.JFXGLContext;
import cuchaz.jfxgl.prism.TexturedQuad;

public class ImageRenderer {
	
	private static final File File = new File(System.getProperty("user.home"), "sandy-beach-scaled.jpg");
	// or use any image file you want

	private int width;
	private int height;
	private int texId;
	private TexturedQuad quad;
	private TexturedQuad.Shader shader;
	
	@InAppGLContext
	@CalledByMainThread
	public ImageRenderer(JFXGLContext context)
	throws IOException {
		
		// load the image
		BufferedImage image = ImageIO.read(File);
		width = image.getWidth();
		height = image.getHeight();
		
		// upload to a texture
		texId = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
		ByteBuffer buf = BufferUtils.createByteBuffer(width*height*3);
		buf.clear();
		buf.put(((DataBufferByte)image.getRaster().getDataBuffer()).getData());
		buf.flip();
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL12.GL_BGR, GL11.GL_UNSIGNED_BYTE, buf);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		
		// init the quad
		shader = new TexturedQuad.Shader(context);
		quad = new TexturedQuad(0, 0, width, height, texId, shader);
	}
	
	public void render(int viewWidth, int viewHeight) {
		shader.bind();
		shader.setViewPos(0, 0);
		shader.setViewSize(viewWidth, viewHeight);
		shader.setYFlip(true);
		quad.render();
	}
	
	@InAppGLContext
	@CalledByMainThread
	public void cleanup() {
		if (texId != 0) {
			GL11.glDeleteTextures(texId);
		}
	}
}
