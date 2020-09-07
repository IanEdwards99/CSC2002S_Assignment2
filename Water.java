package FlowSkeleton;

import java.awt.image.*;
import java.awt.Color;

public class Water {
    int dimx, dimy; // data dimensions
    BufferedImage img; // greyscale image for displaying the terrain top-down
    float [][] depths;
    float [][] waterSurf;
    
    public Water(int dimx, int dimy){
        this.dimx = dimx;
        this.dimy = dimy;
        depths = new float[dimx][dimy];
        waterSurf = new float[dimx][dimy];
        for(int x=0; x < dimx; x++)
			for(int y=0; y < dimy; y++) {
                depths[x][y] = 0;
            }
    }
     
	public BufferedImage getImage() {
        return img;
    }

    void colorImage(int x, int y){
        Color col = Color.BLUE;
        img.setRGB(x,y, col.getRGB());
    }

    void decolorImage(int x, int y){
        img.setRGB(x,y, Color.TRANSLUCENT);
    }


    void deriveImage(){
        img = new BufferedImage(dimy, dimx, BufferedImage.TYPE_INT_ARGB);
        for(int x=0; x < dimx; x++){
            for(int y=0; y < dimy; y++) {
                Color col;
                if (depths[x][y] > 0)
                {
                    col = Color.BLUE;
                    img.setRGB(x, y, col.getRGB());
                } 
                else
                {
                    img.setRGB(x,y,Color.TRANSLUCENT);
                }
            }
        }
    }

    public void updateDepth(int x, int y, int depth){ 
        depths[x][y] = depth;
        // for (int i=0; i < 3; i++){
        //     img.setRGB(x+i,y+i, 254);
        //     img.setRGB(x-i,y-i, 254);
        // }
    }

    public void reset(){
        for(int x=0; x < dimx; x++){
            for(int y=0; y < dimy; y++) {
                depths[x][y] = 0;
                waterSurf[x][y] = 0;
                decolorImage(x, y);
            }
        }
    }
}
