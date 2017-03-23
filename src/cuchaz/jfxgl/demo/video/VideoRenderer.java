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
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cuchaz.jfxgl.CalledByMainThread;
import cuchaz.jfxgl.InAppGLContext;
import cuchaz.jfxgl.prism.JFXGLContext;
import cuchaz.jfxgl.prism.TexturedQuad;
import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.Global;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

public class VideoRenderer {
	
	private static final File File = new File(System.getProperty("user.home"), "zebra-tail-format.m4v");
	// or use any video file you want
	
	private static final int BufferedFrames = 2;
	private static final Rational TimeBaseMS = Rational.make(1, 1000);
	
	private static class StreamInfo {
		public int index;
		public DemuxerStream stream;
		public Decoder decoder;
		public long startTime;
		public Rational timeBase;
		public long numFrames;
	}
	
	private class GPUFrame {
		
		public int texId;
		public long timeMs;
		
		public GPUFrame(int width, int height) {
			texId = GL11.glGenTextures();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, 0);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			timeMs = 0;
		}
		
		public void upload(ByteBuffer data) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, cpuframe.getWidth(), cpuframe.getHeight(), 0, GL12.GL_BGR, GL11.GL_UNSIGNED_BYTE, data);
		}
		
		public void render() {
			quad.texId = texId;
			quad.render();
		}
		
		public void cleanup() {
			if (texId != 0) {
				GL11.glDeleteTextures(texId);
			}
		}
	}
	
	private Demuxer demuxer;
	private StreamInfo stream;
	private MediaPicture picture;
	private MediaPictureConverter pictureConverter;
	private BufferedImage cpuframe;
	private ByteBuffer cpuframe2;
	
	private MediaPacket packet;
	private int packetOffset = 0;
	
	private List<GPUFrame> gpuframes;
	private long renderFrame;
	private long loadFrame;
	private TexturedQuad quad;
	private TexturedQuad.Shader shader;
	
	private long startTimeMs;
	private boolean isPlaying;

	@InAppGLContext
	@CalledByMainThread
	public VideoRenderer(JFXGLContext context) {
		
		try {
			
			// open the video file
			demuxer = Demuxer.make();
			demuxer.open(File.getAbsolutePath(), null, false, true, null, null);
			
			// find the first video stream
			stream = findFirstVideoStream(demuxer);
			if (stream == null) {
				throw new Error("can't find video stream");
			}
			
			// get video metadata
			stream.decoder.open(null, null);
			int width = stream.decoder.getWidth();
			int height = stream.decoder.getHeight();
			
			// allocate CPU buffers
			picture = MediaPicture.make(width, height, stream.decoder.getPixelFormat());
			pictureConverter = MediaPictureConverterFactory.createConverter(MediaPictureConverterFactory.HUMBLE_BGR_24, picture);
			cpuframe = null;
			cpuframe2 = BufferUtils.createByteBuffer(width*height*3 + 16);
			
			// allocate GPU frame
			gpuframes = new ArrayList<>();
			for (int i=0; i<BufferedFrames; i++) {
				gpuframes.add(new GPUFrame(width, height));
			}
			
			// allocate OpenGL stuff
			shader = new TexturedQuad.Shader(
				context,
				TexturedQuad.Shader.class.getResource("vertex.glsl"),
				VideoRenderer.class.getResource("greenscreen.fragment.glsl")
			);
			quad = new TexturedQuad(0, 0, width, height, 0, shader);
			
			// prep for packet reading
			packet = MediaPacket.make();
			packetOffset = 0;
			
			// fill the buffer
			for (int i=0; i<BufferedFrames; i++) {
				readFrame(gpuframes.get(i));
			}
			renderFrame = 0;
			loadFrame = BufferedFrames - 1;
			isPlaying = true;
			
		} catch (Exception ex) {
			throw new Error(ex);
		}
	}
	
	private StreamInfo findFirstVideoStream(Demuxer demuxer2)
	throws IOException, InterruptedException {
		
		StreamInfo info = new StreamInfo();
		info.startTime = Global.NO_PTS;
		
		int numStreams = demuxer.getNumStreams();
		for (int i=0; i<numStreams; i++) {
			info.index = i;
			info.stream = demuxer.getStream(i);
			info.startTime = info.stream.getStartTime();
			info.timeBase = info.stream.getTimeBase();
			info.decoder = info.stream.getDecoder();
			info.numFrames = info.stream.getNumFrames();
			if (info.decoder != null && info.decoder.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO) {
				return info;
			}
		}
		
		return null;
	}

	private boolean readFrame(GPUFrame gpuframe) {
		try {
			
			while (true) {
				
				// read a packet if needed
				if (packetOffset == 0) {
					boolean wasRead = demuxer.read(packet) >= 0;
					if (!wasRead) {
						return false;
					}
				}
				
				// skip packets that aren't from our stream
				if (packet.getStreamIndex() != stream.index) {
					continue;
				}
				
				// is there anything left to decode?
				if (packetOffset >= packet.getSize()) {
					packetOffset = 0;
					continue;
				}
					
				// decode packet until we get a frame
				do {
					packetOffset += stream.decoder.decode(picture, packet, packetOffset);
					
					if (picture.isComplete()) {
						
						// convert frame to BGR format
						cpuframe = pictureConverter.toImage(cpuframe, picture);
					
						// copy image data to direct buffer
						// TODO: get rid of this extra buffer copy somehow?
						cpuframe2.clear();
						cpuframe2.put(((DataBufferByte)cpuframe.getRaster().getDataBuffer()).getData());
						cpuframe2.flip();
						
						// upload to GPU
						gpuframe.upload(cpuframe2);
						gpuframe.timeMs = TimeBaseMS.rescale(picture.getTimeStamp() - stream.startTime, stream.timeBase);
					
						return true;
					}
				} while (packetOffset < packet.getSize());
				
				packetOffset = 0;
			}
			
		} catch (Exception ex) {
			throw new Error(ex);
		}
	}
	
	private int wrapFrame(long frame) {
		return (int)(frame % gpuframes.size());
	}
	
	private GPUFrame currentFrame() {
		return gpuframes.get(wrapFrame(renderFrame));
	}
	
	private GPUFrame nextFrame() {
		return gpuframes.get(wrapFrame(renderFrame + 1));
	}

	@InAppGLContext
	@CalledByMainThread
	public void render(int width, int height) {
		
		if (isPlaying) {
		
			// what time is it?
			long nowMs = System.currentTimeMillis();
			long elapsedMs;
			if (startTimeMs == 0) {
				startTimeMs = nowMs;
				elapsedMs = 0;
			} else {
				elapsedMs = nowMs - startTimeMs;
			}
			
			// should we advance to the next frame?
			if (elapsedMs >= nextFrame().timeMs) {
				renderFrame++;
				
				// adjust the time to match this frame
				elapsedMs = currentFrame().timeMs;
				startTimeMs = nowMs - elapsedMs;
			}
			
			// should we read a frame?
			if (loadFrame - renderFrame < BufferedFrames - 1) {
				
				boolean wasRead = readFrame(gpuframes.get(wrapFrame(++loadFrame)));
				
				// ran out of frames, rewind
				if (!wasRead) {
					try {
						demuxer.close();
						demuxer = Demuxer.make();
						demuxer.open(File.getAbsolutePath(), null, false, true, null, null);
					} catch (IOException | InterruptedException ex) {
						throw new Error(ex);
					}
					renderFrame = 0;
					loadFrame = 0;
				}
			}
		}
			
		// render the current frame
		shader.bind();
		shader.setViewPos(0, 0);
		shader.setViewSize(width, height);
		shader.setYFlip(true);
		currentFrame().render();
	}
	
	@InAppGLContext
	@CalledByMainThread
	public void cleanup() {
		
		for (GPUFrame gpuframe : gpuframes) {
			gpuframe.cleanup();
		}
		gpuframes.clear();
		
		if (demuxer != null) {
			try {
				demuxer.close();
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		}
	}
	
	public long getFrame() {
		return renderFrame;
	}
	
	public long getNumFrames() {
		return stream.numFrames;
	}

	public boolean isPlaying() {
		return isPlaying;
	}
	public void setPlaying(boolean val) {
		isPlaying = val;
	}
}
