package FlowSkeleton;

/** The flow class creates the GUI (setting up the JFrame etc), adds various buttons and the mouseListener. Also initializes and starts thread simulation. */

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter; //bring in the MouseAdaptor package.
import java.awt.event.MouseEvent;
import java.awt.image.*; //for creating color for water
import java.awt.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class Flow {
	static long startTime = 0;
	static int frameX;
	static int frameY;
	static FlowPanel fp;
	final static int blocksize = 10;
	final static int threadNr = 4;


	// start timer
	private static void tick(){
		startTime = System.currentTimeMillis();
	}
	
	// stop timer, return time elapsed in seconds
	private static float tock(){
		return (System.currentTimeMillis() - startTime) / 1000.0f; 
	}
	
	public static void setupGUI(int frameX,int frameY,Terrain landdata, Water water) {

		Dimension fsize = new Dimension(800, 800);
    	JFrame frame = new JFrame("Waterflow"); 
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.getContentPane().setLayout(new BorderLayout());
    	
      	JPanel g = new JPanel();
        g.setLayout(new BoxLayout(g, BoxLayout.PAGE_AXIS)); 
   
		FlowPanel[] fp = new FlowPanel[4];
		int hi = 0;

		Thread [] fpt = new Thread[threadNr];
		for (int k=0; k<threadNr; k++){
			if (k == threadNr - 1)
				hi = ((k+1)*landdata.dim()/threadNr) - 1;
			else hi = ((k+1)*landdata.dim()/threadNr);
			fp[k] = new FlowPanel(landdata, water, (k*landdata.dim()/(threadNr)), hi);
			fp[k].setPreferredSize(new Dimension(frameX,frameY));
			fpt[k] = new Thread(fp[k]);
			
		}
		g.add(fp[0]);
	    
		// to do: add a MouseListener, buttons and ActionListeners on those buttons
		   
		
		JPanel b = new JPanel();
	    b.setLayout(new BoxLayout(b, BoxLayout.LINE_AXIS));
		JButton endB = new JButton("End");;
		JButton resetB = new JButton("Reset");
		JButton pauseB = new JButton("Pause");
		JButton playB = new JButton("Play");
		g.addMouseListener(new MouseInputAdapter(){
			public void mousePressed(MouseEvent me){ 
				int x = me.getX();
				int y = me.getY();
				for (int i = -blocksize/2; i <= blocksize/2; i++){
					for (int j = -blocksize/2; j <= blocksize/2; j++){water.colorImage(x+i, y+j);}
				}
				g.repaint();
			}
		});
		// add the listener to the jbutton to handle the "pressed" event
		endB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				// to do ask threads to stop
				for (int k=0; k<threadNr; k++){
				fp[k].setRun(false);}
				frame.dispose();
				//fpt.stop();
				System.exit(0);
			}
		});
		resetB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				for (int k=0; k<threadNr; k++){
				fp[k].SetPause(true);}
				water.reset(landdata);
				g.repaint();
			}
		});
		playB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				for (int k=0; k<threadNr; k++){
					fp[k].SetPause(false);}
			}
		});
		pauseB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				for (int k=0; k<threadNr; k++){
					fp[k].SetPause(true);}
			}
		});
		
		b.add(resetB);
		b.add(pauseB);
		b.add(playB);
		b.add(endB);
		g.add(b);
    	
		frame.setSize(frameX, frameY+50);	// a little extra space at the bottom for buttons
      	frame.setLocationRelativeTo(null);  // center window on screen
      	frame.add(g); //add contents to window
        frame.setContentPane(g);
		frame.setVisible(true);
		for (int k = 0; k < threadNr; k++){
			fpt[k].start(); //Thread will start off with run method in PAUSED state.
		}					//possibly make new method to create threads and ???
		
	}
	
		
	public static void main(String[] args) {
		Terrain landdata = new Terrain();
		

		// check that number of command line arguments is correct
		if(args.length != 1)
		{
			System.out.println("Incorrect number of command line arguments. Should have form: java -jar flow.java intputfilename");
			System.exit(0);
		}
				
		// landscape information from file supplied as argument
		// 
		landdata.readData(args[0]);
		
		
		frameX = landdata.getDimX();
		frameY = landdata.getDimY();
		Water water = new Water(frameX, frameY);
		water.deriveImage();
		water.calcWaterSurf(landdata);
		SwingUtilities.invokeLater(()->setupGUI(frameX, frameY, landdata, water));
		
		// to do: initialise and start simulation
	}

}
