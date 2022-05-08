Jessie George
CS4551 HW1

This project performs image conversion to grayscale image, or binary image 
using ordered dithering, or performs uniform color quantization, based on 
user input. It takes a .ppm image as input through command line arguments.
The output .ppm image is stored in current directory.

--
CS4551_George.java is the main class.

main function reads the input .ppm file. 
If that file does not exist, it prints an error and exits the program.

getImageShortName function removes the leading path and .ppm extension
from the image file name, which is helpful later when we write output files.

menu function prints menu and calls the other classes based on user's choice.
A copy of the original image is created, this is what we send to the conversion
classes so that the original image is never altered.

--
Grayscale.java converts the image to gray-scale using the formula:
int gray = (int) Math.round(0.299 * rgb[0] 
						+ 0.587 * rgb[1] 
						+ 0.114 * rgb[2]);
R,G,B is set to gray.
Output is written to [InputFileName]-gray.ppm

--
OrderedDithering.java converts to binary image using ordered dithering.

setGrayImg function checks if the gray-scale image already exists, 
else it creates one.

In the main function, the dithering matrix is set as follows:
int k = 4;
int[][] D = {{0, 8, 2, 10},
			 {12, 4, 14, 6},
			 {3, 11, 1, 9},
			 {15, 7, 13, 5}};
			 
We remap pixel values from 0-255 range into 0-16 range, using this formula:
int scaledGray = rgb[0] * 17 / 256;
where k^2 + 1 = 17

Then we compare to the appropriate cell of the dither matrix, and set the 
pixel to white or black.

Output is written to [InputFileName]-OD4.ppm

--
UCQ.java is for Uniform Color Quantization

initLUT function initializes the look up table. 
For each index 0-255,
	An 8-bit binary index is created.
	This is split into 3-bit red, 3-bit green, and 2-bit blue indices.
	Convert R, G, B indices to integer.
	Pick the representative color in the center of the range.
Print the LUT.

createIndexImage function creates a gray-scale index image.
For each pixel of the original image,
	Using the RGB values, create an 8-bit LUT index.
	Convert index to integer. Assign that value to R,G,B to make it gray.
Output is written to [InputFileName]-index.ppm

createQuantizedImage function creates the uniform color quantized image.
For each pixel of the index image,
	Get the LUT R,G,B values for that index and set pixel.
Output is written to [InputFileName]-QT8.ppm

--
MImage.java is the utility class.
Ducky.ppm is the example input.

--
Compile requirement
======================================
JDK Version 7.0 or above


Compile Instruction on Command Line:
======================================
javac CS4551_George.java MImage.java 
or 
javac *.java


Execution Instruction on Command Line:
======================================
java CS4551_George [inputfile]
e.g.
java CS4551_George Ducky.ppm
