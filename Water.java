package FlowSkeleton;

import java.awt.image.*;

import FlowSkeleton.Terrain;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.awt.Color;

public class Water {
    int dimx, dimy; // data dimensions does not need to be atomic as they are never written to by threads
    BufferedImage img; // greyscale image for displaying the terrain top-down
    int [][] depths;
    float [][] waterSurf;
    
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
     
	public BufferedImage getImage() {
        return img;
    }

    synchronized public void colorImage(int x, int y){ //Use method to paint a single point rather than paint whole image each time. Could also have method to alter depths and water surface then repaint entire image each timestep.
        Color col = Color.BLUE;
        img.setRGB(x,y, col.getRGB());
        depths[x][y] += 1;
        waterSurf[x][y] += 0.01;
    }

    synchronized public void decolorImage(int x, int y){
        img.setRGB(x,y, Color.TRANSLUCENT);
        if (depths[x][y] > 0){ //decolor only parts of image that have color...
        depths[x][y] -= 1;
        waterSurf[x][y] -= 0.01;}
    }

    void deriveImage(){
        img = new BufferedImage(dimy, dimx, BufferedImage.TYPE_INT_ARGB);
        for(int x=0; x < dimx; x++){
            for(int y=0; y < dimy; y++) {
                    img.setRGB(x,y,Color.TRANSLUCENT); //Image just needs to be translucent to begin with.
            }
        }
    }

    public void reset(Terrain terrain){
        for(int x=0; x < dimx; x++){
            for(int y=0; y < dimy; y++) {
                depths[x][y] = 0;
                waterSurf[x][y] = terrain.getHeight(x, y);
                decolorImage(x, y);
            }
        }
    }

    public void calcWaterSurf(Terrain terrain){
        for(int x=0; x < dimx; x++)
            for(int y=0; y < dimy; y++) {
                waterSurf[x][y] = (float)(depths[x][y]*0.01) + terrain.getHeight(x, y);
            }
    }

    synchronized public float getWaterSurf(int x, int y){
        return waterSurf[x][y];
    }
    synchronized public int getDepth(int x, int y){
        return depths[x][y];
    }
}
