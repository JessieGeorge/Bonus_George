Jessie George
CS4551 Bonus Project

This project performs uniform color quantization based on 
user input and error diffusion. 
It takes a .ppm image as input through command line arguments.
The output .ppm image is stored in current directory.

--
CS4551_George.java is the main class.

main function reads the input .ppm file. 
If that file does not exist, it prints an error and exits the program.

getImageShortName function removes the leading path and .ppm extension
from the image file name, which is helpful later when we write output files.

getUserBits function gets user input for number of 
index bits for each color channel.

menu function prints menu.
If the user picks first option on the menu, 
we send 3, 3, 2 to the GenericUCQ constructor for the 
number of index bits for R, G, B channels respectively.
If the user picks second option on the menu,
we send user input for number of index bits for each channel.
If the user picks third option, we quit program.

--
GenericUCQ.java is for Generic Uniform Color Quantization

Constructor initializes number of index bits for each color channel,
some helper variables, and the quantization steps.

process function is the controller.

initLUT function initializes the look up table. 
Quantize R,G,B using the equation from Canvas and store in LUT.
Print the LUT.

isValidPos function returns whether the given coordinates exist in image.

clip function clips pixel value to be in range [0, 255].

errorDiffuse function updates four future neighbors based on Floyd weight factor.
It uses the isValidPos and clip functions as helpers.

createQuantizedImage function:
Copies of the original image is created for editing
so that the original image is never altered,
and so that edits don't interfere with each other.
For each pixel of the copy image,
	Get the quantized R,G,B values from the LUT.
	Update quantized image.
	Calculate error of original - quantized for each color channel.
	Call helper function to error diffuse four future neighbors.
Output is written to [InputFileName]-QT-[number of bits for rgb color channels].ppm

--
MImage.java is the utility class.

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
