/*************************************************************************
 * Copyright (C) 2017, Jeffrey W. Martin "Cuchaz"
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License Version 2 with
 * the classpath exception, as published by the Free Software Foundation.
 * 
 * See LICENSE.txt in the project root folder for the full license.
 *************************************************************************/
#version 150

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

in vec3 inPos;
in vec3 inColor;

out vec3 passColor;

void main(void) {

    gl_Position = projection*view*model*vec4(inPos, 1.0);
	
	passColor = inColor;
}
