public class GenericUCQ {
	
	// 256 color values, 3 because we store R,G,B for each color
	static int[][] LUT = new int[256][3];
	
	// number of index bits for a channel, named as per Canvas
	int nr, ng, nb;
	
	// used for naming output images
	String channelBits;
	
	int totalBits;
	
	// quantization steps for a channel
	int quantr, quantg, quantb;
	
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
		channelBits = "r" + nr + "g" + ng + "b" + nb;
		this.totalBits = nr + ng + nb;
		
		this.quantr = (int)Math.pow(2, 8 - nr);
		this.quantg = (int)Math.pow(2, 8 - ng);
		this.quantb = (int)Math.pow(2, 8 - nb);
	}
	
	public void initLUT() {
		System.out.println();
		System.out.println("LUT by UCQ");
		System.out.println("Index	R	G	B");
		System.out.println("-------------------------------------------------");
		
		for (int i = 0; i <= 255; i++) {
			String binaryIndex = Integer.toBinaryString(i);
			// making binaryIndex have totalBits number of bits
			String formatter = "%" + totalBits + "s";
			String binaryIndexPadded = String.format(formatter, binaryIndex)
					.replaceAll(" ", "0");
			
			// BI stands for binary index
			String redBI = binaryIndexPadded.substring(0, nr);
			String greenBI = binaryIndexPadded.substring(nr, nr + ng);
			String blueBI = binaryIndexPadded.substring(nr + ng, totalBits);
			
			// parse binary string to decimal integer
			int redInt = Integer.parseInt(redBI, 2);
			int greenInt = Integer.parseInt(greenBI, 2);
			int blueInt = Integer.parseInt(blueBI, 2);
			
			// representative color in the center of the range
			int red = redInt * quantr + (quantr / 2);
			int green = greenInt * quantg + (quantg / 2);
			int blue = blueInt * quantb + (quantb / 2);
			
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
	
	public MImage createIndexImage(MImage img, String imageShortName) {
		
		System.out.println("\nMaking a copy of original image to edit"
				+ " for error diffusion.");
		MImage copyImg = new MImage(originalImageName);
		
		System.out.println("\nMaking a copy of original image to edit"
				+ " for grayscale index image.");
		MImage indexImg = new MImage(originalImageName);
		
		int w = copyImg.getW();
		int h = copyImg.getH();
		int[] rgb = new int[3];
		
		int originalRed, originalGreen, originalBlue;
		int quantizedRed, quantizedGreen, quantizedBlue;
		int errorRed, errorGreen, errorBlue;
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				
				copyImg.getPixel(x, y, rgb);
				
				originalRed = rgb[0];
				originalGreen = rgb[1];
				originalBlue = rgb[2];
				
				int redIndex = (int)Math.floor(rgb[0] / quantr);
				int greenIndex = (int)Math.floor(rgb[1] / quantg);
				int blueIndex = (int)Math.floor(rgb[2] / quantb);
				
				// BI stands for binary index
				String redBI = Integer.toBinaryString(redIndex);
				String greenBI = Integer.toBinaryString(greenIndex);
				String blueBI = Integer.toBinaryString(blueIndex);
				
				// format color channels based on specified number of index bits
				String redformatter = "%" + nr + "s";
				String greenformatter = "%" + ng + "s";
				String blueformatter = "%" + nb + "s";
				String redBIPadded = String.format(redformatter, redBI)
						.replaceAll(" ", "0");
				String greenBIPadded = String.format(greenformatter, greenBI)
						.replaceAll(" ", "0");
				String blueBIPadded = String.format(blueformatter, blueBI)
						.replaceAll(" ", "0");
				
				// look up table binary index
				String lutBI = redBIPadded + greenBIPadded + blueBIPadded;
				
				int lutIndex = Integer.parseInt(lutBI, 2);
				
				quantizedRed = LUT[lutIndex][0];
				quantizedGreen = LUT[lutIndex][1];
				quantizedBlue = LUT[lutIndex][2];
				
				errorRed = originalRed - quantizedRed;
				errorGreen = originalGreen - quantizedGreen;
				errorBlue = originalBlue - quantizedBlue;
				
				errorDiffuse(copyImg, x, y, errorRed, errorGreen, errorBlue);
				
				// make gray-scale
				rgb[0] = lutIndex;
				rgb[1] = lutIndex;
				rgb[2] = lutIndex;
				
				indexImg.setPixel(x, y, rgb);
			}
		}
		
		// Save it into another PPM file.
		indexImg.write2PPM(imageShortName + "-index-" + channelBits + ".ppm");
		
		return indexImg;
	}
	
	public void createQuantizedImage(MImage indexImg, 
			String imageShortName) {
		
		int w = indexImg.getW();
		int h = indexImg.getH();
		int[] rgb = new int[3];
		
		System.out.println("\nMaking a copy of original image to edit"
				+ " for quantized image.");
		MImage quantizedImg = new MImage(originalImageName);
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				indexImg.getPixel(x, y, rgb);
				int gray = rgb[0];
				
				rgb[0] = LUT[gray][0];
				rgb[1] = LUT[gray][1];
				rgb[2] = LUT[gray][2];
				
				quantizedImg.setPixel(x, y, rgb);
			}
		}
		
		// Save it into another PPM file.
		quantizedImg.write2PPM(imageShortName + "-QT-" + channelBits + ".ppm");
		
	}
	
	public void process(MImage img, String imageShortName) {
		
		originalImageName = img.getName();
		
		initLUT();
		
		MImage indexImg = createIndexImage(img, imageShortName);

		createQuantizedImage(indexImg, imageShortName);
	}

}
