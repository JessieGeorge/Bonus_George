public class Grayscale {
	
	public static void main(MImage img, String imageShortName) {
		
		System.out.println("Converting " + img.getName() + " to Gray-scale ...");
		
		int w = img.getW();
		int h = img.getH();
		int[] rgb = new int[3];
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				img.getPixel(x, y, rgb);
				int gray = (int) Math.round(0.299 * rgb[0] 
						+ 0.587 * rgb[1] 
						+ 0.114 * rgb[2]);
				rgb[0] = gray;
				rgb[1] = gray;
				rgb[2] = gray;
				img.setPixel(x, y, rgb);
			}
		}
		
		// Save it into another PPM file.
		img.write2PPM(imageShortName + "-gray.ppm");
	}
}
