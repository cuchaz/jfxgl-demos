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

import java.io.File;

import org.jerkar.api.depmanagement.JkDependencies;
import org.jerkar.api.depmanagement.JkModuleId;
import org.jerkar.api.depmanagement.JkVersion;
import org.jerkar.api.file.JkFileTreeSet;
import org.jerkar.api.java.JkJavaCompiler;
import org.jerkar.tool.JkProject;
import org.jerkar.tool.builtins.eclipse.JkBuildPluginEclipse;
import org.jerkar.tool.builtins.javabuild.JkJavaBuild;
import org.jerkar.tool.builtins.javabuild.JkJavaPacker;

public class Build extends JkJavaBuild {
	
	@JkProject("../JFXGL")
	cuchaz.jfxgl.Build jfxgl;
	
	private JkBuildPluginEclipse eclipse;
	
	public Build() {
		// tell the eclipse plugin to use the special JDK without JavaFX
		// NOTE: you should create a JRE in the  eclipse workspace needs to have a JRE with this name!
		eclipse = new JkBuildPluginEclipse();
		eclipse.setStandardJREContainer("openjdk-8u121-noFX");
		plugins.configure(eclipse);
	}
	
	@Override
	public JkModuleId moduleId() {
		return JkModuleId.of("cuchaz", "jfxgl-demos");
	}
	
	@Override
	public JkVersion version() {
		return JkVersion.name("0.1");
	}

	@Override
	public String javaSourceVersion() {
		return JkJavaCompiler.V8;
	}

	@Override
	public JkDependencies dependencies() {
		return JkDependencies.builder()
			
			.on(jfxgl.asJavaDependency())
			
			// OpenJFX modules (already compiled)
			.on(projectClasses("../openjfx/modules/controls")).scope(PROVIDED)
			.on(projectClasses("../openjfx/modules/fxml")).scope(PROVIDED)
			.on(projectClasses("../openjfx/modules/graphics")).scope(PROVIDED)
			.on(projectClasses("../openjfx/modules/base")).scope(PROVIDED)
			
			.on("org.joml:joml:1.9.2")
			.on(cuchaz.jfxgl.Build.lwjgl("3.1.1", "glfw", "jemalloc", "opengl"))
			
			.build();
	}
	
	private File projectClasses(String path) {
		File projectDir = new File(path);
		File classesDir = new File(projectDir, "bin");
		eclipse.addProjectFromClasses(classesDir, projectDir);
		return classesDir;
	}
	
	@Override
	public JkFileTreeSet editedSources() {
		return JkFileTreeSet.of(file("src"));
	}
	
	@Override
	public JkFileTreeSet editedResources() {
		return JkFileTreeSet.of(file("resources"));
	}
	
	@Override
	protected JkJavaPacker createPacker() {
		return JkJavaPacker.builder(this)
			.includeVersion(true)
			.doJar(true)
			.doSources(true)
			.doFatJar(true)
			.extraFilesInJar(JkFileTreeSet.of(baseDir().include("LICENSE.txt")))
			.build();
	}
}

