/** The flow class creates the GUI (setting up the JFrame etc), adds various buttons and the mouseListener. Also initializes and starts thread simulation. */

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;

public class Flow {
	static long startTime = 0;
	static int frameX;
	static int frameY;
	static FlowPanel[] fp; 
	static int blocksize = 10;
	static int threadNr = 4;
	static Boolean stopped = false;
	static int count = 0;
	static JLabel counter;

	// start timer
	/** Creates a tick method used alongside the tock method to calculate how long an operation will take.
	 */
	private static void tick(){
		startTime = System.currentTimeMillis();
	}
	
	// stop timer, return time elapsed in seconds
	private static float tock(){
		return (System.currentTimeMillis() - startTime) / 1000.0f; 
	}
	
	/** setupGUI creates the GUI, starts the threads and implements a mouseListener and various button functionality.
	 * 
	 * @param frameX Width of frame.
	 * @param frameY Height of frame.
	 * @param landdata terrain object storing the heights of terrain in a 2D grid and builds a grayscale image from it.
	 * @param water Water object storing water surface heights at each terrain position in a 2D grid, used to draw blue blocks of water.
	 */
	public static void setupGUI(int frameX,int frameY,Terrain landdata, Water water) {

		Dimension fsize = new Dimension(800, 800);
    	JFrame frame = new JFrame("Waterflow"); 
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.getContentPane().setLayout(new BorderLayout());
    	
      	JPanel g = new JPanel();
        g.setLayout(new BoxLayout(g, BoxLayout.PAGE_AXIS)); 
		g.add(fp[0]);
	    
		// to do: add a MouseListener, buttons and ActionListeners on those buttons
		JPanel b = new JPanel();
	    b.setLayout(new BoxLayout(b, BoxLayout.LINE_AXIS));
		JButton endB = new JButton("End");;
		JButton resetB = new JButton("Reset");
		JButton pauseB = new JButton("Pause");
		JButton playB = new JButton("Play");
		JLabel label = new JLabel();
		counter = new JLabel();

		g.addMouseListener(new MouseInputAdapter(){
			public void mousePressed(MouseEvent me){ 
				int x = me.getX();
				int y = me.getY();
				for (int i = -blocksize/2; i <= blocksize/2; i++){
					for (int j = -blocksize/2; j <= blocksize/2; j++){water.colorImage(x+i, y+j); Water.waterclick++; Water.watermove++; }
				}
				g.repaint();
				//Line added to check conservation of water.
				//System.out.println("Water on click: " + Water.waterclick + " Water in grid: " + Water.watermove + " Water edges: " + Water.wateredge + " Grid and Edges: " + (Water.wateredge + Water.watermove));
			}
		});
		// add the listener to the jbutton to handle the "pressed" event
		endB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				for (int k=0; k<threadNr; k++){
				fp[k].setRun(false);}
				stopped = true;
				frame.dispose();
				System.exit(0);
			}
		});
		resetB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				for (int k=0; k<threadNr; k++){
				fp[k].SetPause(true);}
				water.reset(landdata);
				count = 0;
				counter.setText(Integer.toString(count));
				g.repaint();
				//Variables added to check conservation of water.
				// Water.waterclick = 0;
				// Water.wateredge = 0;
				// Water.watermove = 0;
			}
		});
		playB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				for (int k=0; k<threadNr; k++){
					fp[k].SetPause(false);
					}
			}
		});
		pauseB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				for (int k=0; k<threadNr; k++){
					fp[k].SetPause(true);
					//Line added to check conservation of water.
					//System.out.println("Water on click: " + Water.waterclick + " Water in grid: " + Water.watermove + " Water edges: " + Water.wateredge + " Grid and Edges: " + (Water.wateredge + Water.watermove));
					}
			}
		});
		label.setText("Simulation step:");
		counter.setText("0");

		b.add(resetB);
		b.add(pauseB);
		b.add(playB);
		b.add(endB);
		b.add(label);
		b.add(counter);
		g.add(b);
    	
		frame.setSize(frameX, frameY+50);	// a little extra space at the bottom for buttons
      	frame.setLocationRelativeTo(null);  // center window on screen
      	frame.add(g); //add contents to window
        frame.setContentPane(g);
		frame.setVisible(true);
	}
	
		/**
		 * main method reads in heights from input file and builds a new terrain and water object as well as gets the required dimensions followed by building a GUI for the program.
		 * @param args takes in the textfile of data containing the terrain heights.
		 */
	public static void main(String[] args) {
		Terrain landdata = new Terrain();
		

		// check that number of command line arguments is correct
		if(args.length != 1)
		{
			System.out.println("Incorrect number of command line arguments. Should have form: java -jar flow.java intputfilename");
			System.exit(0);
		}
				
		// landscape information from file supplied as argument
		landdata.readData(args[0]);
		
		//Initialize
		frameX = landdata.getDimX();
		frameY = landdata.getDimY();
		Water water = new Water(frameX, frameY);
		water.deriveImage();
		water.calcWaterSurf(landdata);
		MakeThreadObjects(landdata, water);
		SwingUtilities.invokeLater(()->setupGUI(frameX, frameY, landdata, water));
		Thread [] fpt = new Thread[threadNr];

		//Start simulation:
		while (!stopped){
			for (int k = 0; k < threadNr; k++){
				fpt[k] = new Thread(fp[k]);
				fpt[k].start(); //Thread will start off with run method in PAUSED state.
			}
			for (int k = 0; k < threadNr; k++){ 
				try
				{ 
					fpt[k].join();
					
				} 
				catch(Exception ex) {}
			}
			count++;
			counter.setText(Integer.toString(count));
			fp[0].repaint();
		}
	}
	public static void MakeThreadObjects(Terrain landdata, Water water){
		fp = new FlowPanel[4];
		int hi = 0;

		for (int k=0; k<threadNr; k++){
			if (k == threadNr - 1)
				hi = ((k+1)*landdata.dim()/threadNr) - 1;
			else hi = ((k+1)*landdata.dim()/threadNr);

			fp[k] = new FlowPanel(landdata, water, (k*landdata.dim()/(threadNr)), hi);
			fp[k].setPreferredSize(new Dimension(frameX,frameY));
		}
	}

}
