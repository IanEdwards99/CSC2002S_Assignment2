package FlowSkeleton;

import java.awt.image.*;

import FlowSkeleton.Terrain;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.awt.Color;
/** Water class is used to create a water object to store water depths and water surface heights in a 2D grid identical in size to the grid made for the terrain object.
 * The class then can color/decolor parts of the water image and update depths accordingly.
 */
public class Water {
    int dimx, dimy; // data dimensions does not need to be atomic as they are never written to by threads
    BufferedImage img; // greyscale image for displaying the terrain top-down
    int [][] depths;
    float [][] waterSurf;
    /** instantiates the water object
     * @param dimx is the width of the required array.
     * @param dimy is the height of the required array.
     */
    public Water(int dimx, int dimy){
        this.dimx = dimx;
        this.dimy = dimy;
        depths = new int[dimx][dimy];
        waterSurf = new float[dimx][dimy];
        for(int x=0; x < dimx; x++)
			for(int y=0; y < dimy; y++) {
                depths[x][y] = 0;
            }
    }
     /** Returns the buffered image altered or generated in this class. */
	public BufferedImage getImage() {
        return img;
    }
/** A synchronized method colorImage updates water depth, surface and colors the buffered image at the position.
 * @param x is the x grid position in question.
 * @param y is the y grid position in question.
 */
    synchronized public void colorImage(int x, int y){ //Use method to paint a single point rather than paint whole image each time. Could also have method to alter depths and water surface then repaint entire image each timestep.
        Color col = Color.BLUE;
        img.setRGB(x,y, col.getRGB());
        depths[x][y] += 1;
        waterSurf[x][y] += 0.01;
    }
/** A synchronized method colorImage updates water depth, surface and colors the buffered image at the position.
 * @param x is the x grid position in question.
 * @param y is the y grid position in question.
 */
    synchronized public void decolorImage(int x, int y){
        img.setRGB(x,y, Color.TRANSLUCENT);
        if (depths[x][y] > 0){ //decolor only parts of image that have color...
        depths[x][y] -= 1;
        waterSurf[x][y] -= 0.01;}
    }
/** Creates a new image for the water object to be used there on out. Sets it as translucent as it is an empty water object at first with no water to display. */
    void deriveImage(){
        img = new BufferedImage(dimy, dimx, BufferedImage.TYPE_INT_ARGB);
        for(int x=0; x < dimx; x++){
            for(int y=0; y < dimy; y++) {
                    img.setRGB(x,y,Color.TRANSLUCENT); //Image just needs to be translucent to begin with.
            }
        }
    }
/** Method to easily reset the water object when the button reset is pushed.
 * @param terrain the terrain object being used on which the water is displayed and flowing.
 */
    public void reset(Terrain terrain){
        for(int x=0; x < dimx; x++){
            for(int y=0; y < dimy; y++) {
                depths[x][y] = 0;
                waterSurf[x][y] = terrain.getHeight(x, y);
                decolorImage(x, y);
            }
        }
    }
/** Determines the water surfaces for the entire water object.
 * @param terrain is the terrain object being used over which the water flows and exists/accumulates.
 */
    public void calcWaterSurf(Terrain terrain){
        for(int x=0; x < dimx; x++)
            for(int y=0; y < dimy; y++) {
                waterSurf[x][y] = (float)(depths[x][y]*0.01) + terrain.getHeight(x, y);
            }
    }
/** getWaterSurf returns a float value of the height of water surface of a grid position.
 * @param x grid x coordinate.
 * @param y grid y coordinate.
 * @return returns a float height value.
 */
    synchronized public float getWaterSurf(int x, int y){
        return waterSurf[x][y];
    }
    /** getDepth returns the depth of water above the terrain at each position.
 * @param x grid x coordinate.
 * @param y grid y coordinate.
 * @return returns a float height value.
 */
    synchronized public int getDepth(int x, int y){
        return depths[x][y];
    }
}
