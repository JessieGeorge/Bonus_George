public class UCQ {
	
	// 256 color values, 3 because we store R,G,B for each color
	static int[][] LUT = new int[256][3];
	
	public static void initLUT() {
		System.out.println();
		System.out.println("LUT by UCQ");
		System.out.println("Index	R	G	B");
		System.out.println("-------------------------------------------------");
		
		for (int i = 0; i <= 255; i++) {
			String binaryIndex = Integer.toBinaryString(i);
			// 8-bit
			String binaryIndexPadded = String.format("%8s", binaryIndex)
					.replaceAll(" ", "0");
			
			// BI stands for binary index
			String redBI = binaryIndexPadded.substring(0, 3);
			String greenBI = binaryIndexPadded.substring(3, 6);
			String blueBI = binaryIndexPadded.substring(6, 8);
			
			int redInt = Integer.parseInt(redBI, 2);
			int greenInt = Integer.parseInt(greenBI, 2);
			int blueInt = Integer.parseInt(blueBI, 2);
			
			// representative color in the center of the range
			int red = redInt * 32 + 16;
			int green = greenInt * 32 + 16;
			int blue = blueInt * 64 + 32;
			
			LUT[i][0] = red;
			LUT[i][1] = green;
			LUT[i][2] = blue;
			
			System.out.println(i + "\t" + red + "\t" + green + "\t" + blue);
		}
		System.out.println();
	}
	
	public static MImage createIndexImage(MImage img, String imageShortName) {
		int w = img.getW();
		int h = img.getH();
		int[] rgb = new int[3];
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				
				img.getPixel(x, y, rgb);
				
				int redIndex = (int)Math.floor(rgb[0] / 32.0);
				int greenIndex = (int)Math.floor(rgb[1] / 32.0);
				int blueIndex = (int)Math.floor(rgb[2] / 64.0);
				
				// BI stands for binary index
				String redBI = Integer.toBinaryString(redIndex);
				String greenBI = Integer.toBinaryString(greenIndex);
				String blueBI = Integer.toBinaryString(blueIndex);
				
				// 3-bit red, 3-bit green, 2-bit blue
				String redBIPadded = String.format("%3s", redBI)
						.replaceAll(" ", "0");
				String greenBIPadded = String.format("%3s", greenBI)
						.replaceAll(" ", "0");
				String blueBIPadded = String.format("%2s", blueBI)
						.replaceAll(" ", "0");
				
				// look up table binary index
				String lutBI = redBIPadded + greenBIPadded + blueBIPadded;
				
				int lutIndex = Integer.parseInt(lutBI, 2);
				
				// make gray-scale
				rgb[0] = lutIndex;
				rgb[1] = lutIndex;
				rgb[2] = lutIndex;
				
				img.setPixel(x, y, rgb);
			}
		}
		
		// Save it into another PPM file.
		img.write2PPM(imageShortName + "-index.ppm");
		
		return img;
	}
	
	public static void createQuantizedImage(MImage indexImage, 
			String imageShortName) {
		
		int w = indexImage.getW();
		int h = indexImage.getH();
		int[] rgb = new int[3];
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				indexImage.getPixel(x, y, rgb);
				int gray = rgb[0];
				
				rgb[0] = LUT[gray][0];
				rgb[1] = LUT[gray][1];
				rgb[2] = LUT[gray][2];
				
				indexImage.setPixel(x, y, rgb);
			}
		}
		
		// Save it into another PPM file.
		indexImage.write2PPM(imageShortName + "-QT8.ppm");
		
	}
	
	public static void main(MImage img, String imageShortName) {
		System.out.println("Performing Uniform Color Quantization ...");
		
		initLUT();
		
		MImage indexImage = createIndexImage(img, imageShortName);
		
		createQuantizedImage(indexImage, imageShortName);
	}

}
