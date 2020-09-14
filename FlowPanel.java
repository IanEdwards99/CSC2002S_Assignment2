package FlowSkeleton;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.JLabel;
/** This class implements an object to paint a grey scale image of terrain heights and a blue water images over that. This is the thread class that will constantly evaluate each position in the grid for changes and update water positions by transfering water to the nearest lowest neighbour. */
public class FlowPanel extends JPanel implements Runnable {
	Terrain land; Water water;
	int lo; int hi;
	public Boolean stop = false; Boolean paused = true;
	//public static AtomicInteger count;
	//public static JLabel counter;
/** Constructor for the FlowPanel class which initializes local variables.
 * 
 * @param terrain a terrain object storing heights of terrain in a 2D array.
 * @param water a water object storing heights of water surfaces in a 2D array.
 * @param lo a lower bound for the range for which the object must loop through.
 * @param hi an upper bound for the range for which the object must loop through.
 */
	FlowPanel(Terrain terrain, Water water, int lo, int hi) {
		land=terrain;
		this.water = water;
		this.lo = lo;
		this.hi = hi;
		//count = new AtomicInteger(0);
	}
		
	// responsible for painting the terrain and water
	// as images
	/** paintComponent called on repaint() draws graphics onto the java swing frame upon being called (thus updates water position visually)
	 */
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
	/** When a thread starts, its run method will run indefinitely, until the program ends. It features a while loop that either continues to the next loop iteration if paused is set, or evaluates water positions and transfers water if it is not paused. */
	public void run() {	
		// display loop here
		// to do: this should be controlled by the GUI
		// to allow stopping and starting
		int i = lo;
		int[] pos = new int[2]; //int[0] is x and int[1] is y.
		int[] nextBasin = new int[2];

		while (i < hi){
			if (paused == true){
				try {
					Thread.sleep(1); //continue meant program never caught the buttonClick event. With sleep, there are unqueued moments to detect a button click.
				} catch (InterruptedException e) {}
			}
			else {
					nextBasin[0] = 0; nextBasin[1] = 0;
					synchronized (this) { //allows safe concurrent access to land and water objects, specfically prevents interleavings.
					land.getPermute(i, pos); //using i-th index iterating through linear array of positions, return in pos x and y coordinates.
					if (water.getDepth(pos[0],pos[1]) > 0){ //could syncrhonize after here but bad interleavings can occur where both threads evaluate true then one thread updates the depth such that it would no longer be true...
						
							if (pos[0] == 0 || pos[0] == land.getDimX()-1 || pos[1] == 0 || pos[1] == land.getDimY()-1){ //if water on border, decolor, decrease depth and water surface level.
								water.decolorImage(pos[0], pos[1]);
							}
							else{
								nextBasin = determineLowNearby(pos[0], pos[1]); //find the lowest neighbour.
								water.colorImage(nextBasin[0], nextBasin[1]);
								water.decolorImage(pos[0], pos[1]);
							}
						}
					}
					i++;
					//counter.setText(Integer.toString(count.getAndIncrement()));
				}
				
			
		}
	    
	}
/** The SetPause method is used to change the boolean variable in this object and thus thread in order to "pause" the program and make the while loop loop through iterations of no functionality in order to simulate a pause but with threads still running.
 * 
 * @param paused boolean passed in from the button click handler.
 */
	public void SetPause(boolean paused){
		this.paused = paused;
	}
	/** Similar to SetPause, this method is used to unpause the run method. 
	 * @param stop is the boolean passed in from the play button handler.
	*/
	public void setRun(Boolean stop){
		this.stop = stop;
	}
	/** determineLowNearby simplifies the code in the run method by putting it in another method which determines which grid position around a given position, is the lowest neighbour. 
	 * @param x is the x position in question.
	 * @param y is the y position of the grid in question.
	*/
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