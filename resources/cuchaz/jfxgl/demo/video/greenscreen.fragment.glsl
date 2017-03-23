/*************************************************************************
 * Copyright (C) 2017, Jeffrey W. Martin "Cuchaz"
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License Version 2 with
 * the classpath exception, as published by the Free Software Foundation.
 * 
 * See LICENSE.txt in the project root folder for the full license.
 *************************************************************************/
#version 130

uniform sampler2D colorSampler;

in vec2 passTexCoord;

out vec4 outColor;

const float thresholdSensitivity = 0.1f;
const float smoothing = 0.1f;

const vec3 colorToReplace = vec3(41f/255f, 162f/255f, 91f/255f);
const float maskY = 0.2989 * colorToReplace.r + 0.5866 * colorToReplace.g + 0.1145 * colorToReplace.b;
const float maskCr = 0.7132 * (colorToReplace.r - maskY);
const float maskCb = 0.5647 * (colorToReplace.b - maskY);


void main(void) {

	vec4 textureColor = texture(colorSampler, passTexCoord);
	
	// make green pixels transparent
	float Y = 0.2989 * textureColor.r + 0.5866 * textureColor.g + 0.1145 * textureColor.b;
	float Cr = 0.7132 * (textureColor.r - Y);
	float Cb = 0.5647 * (textureColor.b - Y);
	float blendValue = smoothstep(thresholdSensitivity, thresholdSensitivity + smoothing, distance(vec2(Cr, Cb), vec2(maskCr, maskCb)));
	
	outColor = vec4(textureColor.rgb*blendValue, blendValue);
}
