public class GenericUCQ {
	
	// number of index bits for a channel, named as per Canvas
	int nr, ng, nb;
	
	// used for naming output images
	String channelBits;
	
	int totalBits;
	
	int numColors;
	
	// look up table
	int[][] LUT;
	
	// quantization steps for a channel
	double quantr, quantg, quantb;
	
	// Floyd weight factors for error diffusion
	final double rightFloyd = 7/16.0;
	final double bottomLeftFloyd = 3/16.0;
	final double bottomMidFloyd = 5/16.0;
	final double bottomRightFloyd = 1/16.0;
	
	String originalImageName;
	
	public GenericUCQ(int nr, int ng, int nb) {
		this.nr = nr;
		this.ng = ng;
		this.nb = nb; 
		this.channelBits = "r" + nr + "g" + ng + "b" + nb;
		this.totalBits = nr + ng + nb;
		
		// Each color channel is max 8 bits
		// So there are 2^8 values of red, 2^8 values of green, 2^8 values of blue 
		this.numColors = 256;
		
		// 3 because we store R,G,B values for each color
		this.LUT = new int[numColors][3];
		
		this.quantr = Math.pow(2, 8 - nr);
		this.quantg = Math.pow(2, 8 - ng);
		this.quantb = Math.pow(2, 8 - nb);
	}
	
	public void initLUT() {
		
		System.out.println();
		System.out.println("LUT by UCQ");
		System.out.println("Index	R	G	B");
		System.out.println("-------------------------------------------------");
		
		for (int i = 0; i < numColors; i++) {
			
			// quantize
			// pick representative color in the center of the range
			int red = (int)(Math.floor(i/quantr) * quantr + (quantr / 2));
			int green = (int)(Math.floor(i/quantg) * quantg + (quantg / 2));
			int blue = (int)(Math.floor(i/quantb) * quantb + (quantb / 2));
			
			LUT[i][0] = red;
			LUT[i][1] = green;
			LUT[i][2] = blue;
			
			System.out.println(i + "\t" + red + "\t" + green + "\t" + blue);
		}
		System.out.println();
	}
	
	public boolean isValidPos(MImage img, int x, int y) {
		int w = img.getW();
		int h = img.getH();
		
		return (x >= 0 && x < w) && (y >= 0 && y < h);
	}
	
	public int[] clip(int[] rgb) {
		
		for (int i = 0; i < 3; i++) {
			int val = rgb[i];
			
			if (val < 0) {
				rgb[i] = 0;
			} else if (val > 255) {
				rgb[i] = 255;
			} // else rgb[i] stays the same because it's in range
		}
		
		return rgb;
	}
	
	public void errorDiffuse(MImage img, int x, int y, 
			int errorRed, int errorGreen, int errorBlue) {
		
		// btw img.setPixel updates the img globally, not just in this function
		
		int[] rgb = new int[3];
		
		// right pixel
		if (isValidPos(img, x + 1, y)) {
			img.getPixel(x + 1, y, rgb);
			rgb[0] = rgb[0] + (int)Math.round(rightFloyd * errorRed);
			rgb[1] = rgb[1] + (int)Math.round(rightFloyd * errorGreen);
			rgb[2] = rgb[2] + (int)Math.round(rightFloyd * errorBlue);
			rgb = clip(rgb);
			img.setPixel(x + 1, y, rgb);
		}
		
		// bottom mid pixel
		if (isValidPos(img, x, y + 1)) {
			img.getPixel(x, y + 1, rgb);
			rgb[0] = rgb[0] + (int)Math.round(bottomMidFloyd * errorRed);
			rgb[1] = rgb[1] + (int)Math.round(bottomMidFloyd * errorGreen);
			rgb[2] = rgb[2] + (int)Math.round(bottomMidFloyd * errorBlue);
			rgb = clip(rgb);
			img.setPixel(x, y + 1, rgb);
		} else {
			/* Don't bother checking rest of bottom. 
			 * We know mid is a valid column, because we're currently in it.
			 * isValidPos returned false, which means bottom is invalid.
			 * i.e we're on the lowest row of the image, and we can't go lower.
			 */
			return;
		}
		
		// bottom left pixel
		if (isValidPos(img, x - 1, y + 1)) {
			img.getPixel(x - 1, y + 1, rgb);
			rgb[0] = rgb[0] + (int)Math.round(bottomLeftFloyd * errorRed);
			rgb[1] = rgb[1] + (int)Math.round(bottomLeftFloyd * errorGreen);
			rgb[2] = rgb[2] + (int)Math.round(bottomLeftFloyd * errorBlue);
			rgb = clip(rgb);
			img.setPixel(x - 1, y + 1, rgb);
		}
		
		// bottom right pixel
		if (isValidPos(img, x + 1, y + 1)) {
			img.getPixel(x + 1, y + 1, rgb);
			rgb[0] = rgb[0] + (int)Math.round(bottomRightFloyd * errorRed);
			rgb[1] = rgb[1] + (int)Math.round(bottomRightFloyd * errorGreen);
			rgb[2] = rgb[2] + (int)Math.round(bottomRightFloyd * errorBlue);
			rgb = clip(rgb);
			img.setPixel(x + 1, y + 1, rgb);
		}
	}
	
	public void createQuantizedImage(String imageShortName) {
		
		System.out.println("\nMaking a copy of original image to edit"
				+ " for error diffusion.");
		MImage copyImg = new MImage(originalImageName);
		
		System.out.println("\nMaking a copy of original image to edit"
				+ " for quantized image.");
		MImage quantizedImg = new MImage(originalImageName);
		
		int w = copyImg.getW();
		int h = copyImg.getH();
		int[] rgb = new int[3];
		
		int originalRed, originalGreen, originalBlue;
		int quantizedRed, quantizedGreen, quantizedBlue;
		int errorRed, errorGreen, errorBlue;
		
		int[] quantizedRGB = new int[3];
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				
				copyImg.getPixel(x, y, rgb);
				
				originalRed = rgb[0];
				originalGreen = rgb[1];
				originalBlue = rgb[2];
				
				quantizedRed = LUT[originalRed][0];
				quantizedGreen = LUT[originalGreen][1];
				quantizedBlue = LUT[originalBlue][2];
				
				quantizedRGB[0] = quantizedRed;
				quantizedRGB[1] = quantizedGreen;
				quantizedRGB[2] = quantizedBlue;
				quantizedImg.setPixel(x, y, quantizedRGB);
				
				errorRed = originalRed - quantizedRed;
				errorGreen = originalGreen - quantizedGreen;
				errorBlue = originalBlue - quantizedBlue;
				
				errorDiffuse(copyImg, x, y, errorRed, errorGreen, errorBlue);
			}
		}
		
		// Save it into another PPM file.
		quantizedImg.write2PPM(imageShortName + "-QT-" + channelBits + ".ppm");
	}
	
	public void process(MImage img, String imageShortName) {
		
		originalImageName = img.getName();
		
		initLUT();

		createQuantizedImage(imageShortName);
	}

}
