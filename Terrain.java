//package FlowSkeleton;

import java.io.File;
import java.awt.image.*;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
/** Creates a terrain object which loads in the textfile contents into a single dimensional array, creates a greyscale bufferedimage object. */
public class Terrain {

	float [][] height; // regular grid of height values
	int dimx, dimy; // data dimensions
	BufferedImage img; // greyscale image for displaying the terrain top-down

	ArrayList<Integer> permute;	// permuted list of integers in range [0, dimx*dimy)
	
	// overall number of elements in the height grid
	int dim(){
		return dimx*dimy;
	}
	
	// get x-dimensions (number of columns)
	int getDimX(){
		return dimx;
	}
	
	// get y-dimensions (number of rows)
	int getDimY(){
		return dimy;
	}
	
	// get greyscale image
	public BufferedImage getImage() {
		  return img;
	}
	
	// convert linear position into 2D location in grid
	synchronized void locate(int pos, int [] ind)
	{
		ind[0] = (int) pos / dimy; // x
		ind[1] = pos % dimy; // y	
	}
// to paint on mouseclick or any other event.
	void changeImage(int x, int y){
		Color blue = Color.BLUE;
		img.setRGB(x,y, blue.getRGB());
	}
	
	// convert height values to greyscale colour and populate an image
	void deriveImage()
	{
		img = new BufferedImage(dimy, dimx, BufferedImage.TYPE_INT_ARGB);
		float maxh = -10000.0f, minh = 10000.0f;
		
		// determine range of heights
		for(int x=0; x < dimx; x++)
			for(int y=0; y < dimy; y++) {
				float h = height[x][y];
				if(h > maxh)
					maxh = h;
				if(h < minh)
					minh = h;
			}
		
		for(int x=0; x < dimx; x++)
			for(int y=0; y < dimy; y++) {
				 // find normalized height value in range
				 float val = (height[x][y] - minh) / (maxh - minh);
				 //System.out.println("val = " + val);
				 Color col = new Color(val, val, val, 1.0f);
				 img.setRGB(x, y, col.getRGB());
			}
	}
	
	// generate a permuted list of linear index positions to allow a random
	// traversal over the terrain
	void genPermute() {
		permute = new ArrayList<Integer>();
		for(int idx = 0; idx < dim(); idx++)
			permute.add(idx);
		java.util.Collections.shuffle(permute);
	}
	
	// find permuted 2D location from a linear index in the
	// range [0, dimx*dimy)
	synchronized void getPermute(int i, int [] loc) {
		locate(permute.get(i), loc);
	}
	
	// read in terrain from file
	void readData(String fileName){ 
		try{ 
			Scanner sc = new Scanner(new File(fileName));
			
			// read grid dimensions
			// x and y correpond to columns and rows, respectively.
			// Using image coordinate system where top left is (0, 0).
			dimy = sc.nextInt(); 
			dimx = sc.nextInt();
			
			// populate height grid
			height = new float[dimx][dimy];
			
			for(int y = 0; y < dimy; y++){
				for(int x = 0; x < dimx; x++)	
					height[x][y] = sc.nextFloat();
				}
				
			sc.close(); 
			
			// create randomly permuted list of indices for traversal 
			genPermute(); 
			
			// generate greyscale heightfield image
			deriveImage();
		} 
		catch (IOException e){ 
			System.out.println("Unable to open input file "+fileName);
			e.printStackTrace();
		}
		catch (java.util.InputMismatchException e){ 
			System.out.println("Malformed input file "+fileName);
			e.printStackTrace();
		}
	}
	/** return the height of the terrain in a given x and y position, when looping through the heights to determine water surface at each point.
	 * @param x is the x position in question.
	 * @param y is the y position of the grid in question.
	 * @return height at the inputted position.
	 */
	public float getHeight(int x, int y){
		return height[x][y];
	}
}