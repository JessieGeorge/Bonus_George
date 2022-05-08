import java.io.File;

public class OrderedDithering {
	
	static MImage grayImg;
	
	public static void setGrayImg(MImage originalImg, String imageShortName) {
		
		String grayImgPath = imageShortName + "-gray.ppm";
		File grayFile = new File(grayImgPath);
		
		System.out.println("Checking if gray-scale image "
				+ "already exists " + grayImgPath);
		if (!grayFile.exists()) {
			System.out.println("Gray-scale image does not exist. "
					+ "Generating it.");
			Grayscale.main(originalImg, imageShortName);
		} else {
			System.out.println("Gray-scale image exists.");
		}
		
		grayImg = new MImage(grayImgPath); 		
	}
	
	public static void main(MImage img, String imageShortName) {
		
		setGrayImg(img, imageShortName);
		
		System.out.println("Applying Ordered Dithering to gray-scale image ...");
		
		int k = 4;
		int[][] D = {{0, 8, 2, 10},
					 {12, 4, 14, 6},
					 {3, 11, 1, 9},
					 {15, 7, 13, 5}};
		
		int w = grayImg.getW();
		int h = grayImg.getH();
		int[] rgb = new int[3];
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				
				int i = x % k;
				int j = y % k;
				
				grayImg.getPixel(x, y, rgb);
				
				// Remap pixel values from 0-255 range into 0-16 range.
				// k^2 + 1 = 17
				int scaledGray = rgb[0] * 17 / 256;
				
				if (scaledGray > D[j][i]) {
					// white
					rgb[0] = 255;
					rgb[1] = 255;
					rgb[2] = 255;
					
				} else {
					rgb[0] = 0;
					rgb[1] = 0;
					rgb[2] = 0;
				}
				
				grayImg.setPixel(x, y, rgb);
			}
		}
		
		// Save it into another PPM file.
		grayImg.write2PPM(imageShortName + "-OD4.ppm");
	}
}
