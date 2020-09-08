package FlowSkeleton;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.awt.Graphics;
import javax.swing.JPanel;

public class FlowPanel extends JPanel implements Runnable {
	Terrain land; Water water;
	int lo; int hi;
	Boolean stop = false, paused = true;

	FlowPanel(Terrain terrain, Water water, int lo, int hi) {
		land=terrain;
		this.water = water;
		this.lo = lo;
		this.hi = hi;
	}
		
	// responsible for painting the terrain and water
	// as images
	@Override
    protected void paintComponent(Graphics g) { //doesnt need to be synchronized as it is only called for a single thread as it will paint the shared water image.
		int width = getWidth();
		int height = getHeight();
		super.paintComponent(g);
		
		// draw the landscape in greyscale as an image
		if (land.getImage() != null){
			g.drawImage(land.getImage(), 0, 0, null);
			g.drawImage(water.getImage(),0,0,null);
		}
	}
	
	public void run() {	
		// display loop here
		// to do: this should be controlled by the GUI
		// to allow stopping and starting
		int[] pos = new int[2]; //int[0] is x and int[1] is y.
		int i = 0;
		int[] nextBasin = new int[2];

		while (!stop){
			if (paused == true){
				try {
					Thread.sleep(100); //continue meant program never caught the buttonClick event. With sleep, there are unqueued moments to detect a button click.
				} catch (InterruptedException e) {}
			}
			else {
					nextBasin[0] = 0; nextBasin[1] = 0;
					land.getPermute(i, pos); //using i-th index iterating through linear array of positions, return in pos x and y coordinates.
					if (water.getDepth(pos[0],pos[1]) > 0){
						synchronized (this) {
							if (pos[0] == 0 || pos[0] == land.getDimX()-1 || pos[1] == 0 || pos[1] == land.getDimY()-1){ //if water on border, decolor, decrease depth and water surface level.
								water.decolorImage(pos[0], pos[1]);
								repaint();
							}
							else{
								nextBasin = determineLowNearby(pos[0], pos[1]); //find the lowest neighbour.
								water.colorImage(nextBasin[0], nextBasin[1]);
								water.decolorImage(pos[0], pos[1]);
								repaint();
							}
						}
					}
				
				if (i >= lo && i < hi) //land.dim() -1
					i++;
				else i = lo;
				
			}
			
		}
	    
	}

	public void SetPause(boolean paused){
		this.paused = paused;
	}
	public void setRun(Boolean stop){
		this.stop = stop;
	}
	public int[] determineLowNearby(int x, int y){
		int[] output = new int[2];
		output[0] = x; output[1] = y;
		float level = 0;
		//System.out.println("x: " + x + " y: " + y);
		for (int i = -1; i < 2; i++){ //loop over its neighbours (x+1, x-1, y+1, y-1 etc)
			for (int j = -1; j < 2; j++){
				level = water.getWaterSurf(x+i, y+j);
				if (water.getWaterSurf(x,y) > level){ //check if there is a lower point nearby...
					if (water.getWaterSurf(output[0],output[1]) > level) //is this point the LOWEST?
						output[0] = x+i; output[1] = y+j;
				}
			}
		}
		return output;
	}
}