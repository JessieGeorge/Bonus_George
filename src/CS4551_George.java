import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CS4551_George {
	
	public static String getImageShortName(MImage img) {
		// remove leading path and .ppm from image file name
		
		String origName = img.getName();
		int len = origName.length();
		int filepathEnd = origName.lastIndexOf("\\");
		String shortName = origName.substring(filepathEnd + 1, len-4);
		return shortName;
	}
	
	public static void menu(MImage img) throws IOException {
		
		String shortName = getImageShortName(img);
		
		BufferedReader br = new BufferedReader(
				new InputStreamReader(System.in));
				
		String message = "Main Menu-----------------------------------\n"
				+ "1. 8-bit UCQ and Error Diffusion\n"
				+ "2. Generic UCQ and Error Diffusion\n"
				+ "3. Quit\n"
				+ "Please enter the task number [1-3]:";
		
		int choice = 0;
		
		while (choice != 3) {
			System.out.println(message);
			choice = Integer.parseInt(br.readLine());
			
			MImage copyImg = null; // initialize
			if (choice > 0 && choice < 3) {
				System.out.println("\nMaking a copy "
						+ "of original image to edit.");
				copyImg = new MImage(img.getName());
			}
			
			switch(choice) {	
				case 1:
					UCQ.main(copyImg, shortName);
					break;
					
				case 2:
					// TODO: Fix for generic
					UCQ.main(copyImg, shortName);
					break;
					
				case 3:
					System.exit(0);
					break;
					
				default:
					System.out.println("ERROR: Invalid choice. "
							+ "Please choose from menu options.");
					choice = 0;
			}
			
			System.out.println(" ");
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		// the program expects one command line argument
		// if there is no command line argument, exit the program
		if (args.length != 1) {
			usage();
			System.exit(1);
		}

		System.out.println("--Welcome to Multimedia Software System--");
		
		File f = new File(args[0]);
		if (!f.exists()) {
			System.out.println("ERROR: Image file " + args[0] + 
					" does not exist.");
			System.out.println();
			System.exit(1);
		}
		
		// Create an Image object with the input PPM file name.
		MImage img = new MImage(args[0]);
		System.out.println(img);
		
		System.out.println(" ");
		menu(img);
		
		System.out.println("--Good Bye--");
	}

	public static void usage() {
		System.out.println("\nUsage: java CS4551_George [input_ppm_file]\n");
	}
}
