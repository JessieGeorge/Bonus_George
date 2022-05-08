public class GenericUCQ {
	
	// 256 color values, 3 because we store R,G,B for each color
	static int[][] LUT = new int[256][3];
	
	// number of index bits for a channel, named as per Canvas
	int nr, ng, nb;
	int totalBits;
	
	// quantization steps for a channel
	int quantr, quantg, quantb;
	
	public GenericUCQ(int nr, int ng, int nb) {
		this.nr = nr;
		this.ng = ng;
		this.nb = nb; 
		this.totalBits = nr + ng + nb;
		
		this.quantr = (int)Math.pow(2, 8 - nr);
		this.quantg = (int)Math.pow(2, 8 - ng);
		this.quantb = (int)Math.pow(2, 8 - nb);
	}
	
	public void initLUT() {
		System.out.println("totalBits = " + totalBits);
		
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
			
			/*
			System.out.println("formatter = " + formatter); // REMOVETHIS
			System.out.println("binaryIndex = " + binaryIndex); /// REMOVETHIS
			*/
			
			System.out.println("binaryIndexPadded = " + binaryIndexPadded); /// REMOVETHIS
			
			//System.exit(1); // REMOVETHIS
			
			
			// BI stands for binary index
			String redBI = binaryIndexPadded.substring(0, nr);
			String greenBI = binaryIndexPadded.substring(nr, nr + ng);
			String blueBI = binaryIndexPadded.substring(nr + ng, totalBits);
			
			/*
			System.out.println("redBI = " + redBI); /// REMOVETHIS
			System.out.println("greenBI = " + greenBI); /// REMOVETHIS
			System.out.println("blueBI = " + blueBI); /// REMOVETHIS
			*/
			
			// parse binary string to decimal integer
			int redInt = Integer.parseInt(redBI, 2);
			int greenInt = Integer.parseInt(greenBI, 2);
			int blueInt = Integer.parseInt(blueBI, 2);
			
			/*
			System.out.println("redInt = " + redInt); /// REMOVETHIS
			System.out.println("greenInt = " + greenInt); /// REMOVETHIS
			System.out.println("blueInt = " + blueInt); /// REMOVETHIS
			*/
			
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
	
	public MImage createIndexImage(MImage img, String imageShortName) {
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
	
	public void createQuantizedImage(MImage indexImage, 
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
	
	public void process(MImage img, String imageShortName) {
		System.out.println("Performing Uniform Color Quantization ...");
		
		this.initLUT();
		System.exit(1); // REMOVETHIS
		
		MImage indexImage = this.createIndexImage(img, imageShortName);
		
		this.createQuantizedImage(indexImage, imageShortName);
	}

}
