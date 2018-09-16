/*******************************************************************************
 * Title:        BattleRobots
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 ******************************************************************************/

// Changes:
// Assignment#2.2 - 2.x = keyword "Assignment2". Corresponds to HW Assignment doc
// Assignment#3 = keyword "Assignment3".

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;  // javax.swing is required for JApplet

// Assignment1: BattleRobots inherits JApplet and uses the interfaces 'Runnable' (used for applets)
// 'ActionListener' (used for mouse actions) and 'KeyListener' (used for keyboard actions).
// 'JApplet' is required if you want to create an Applet
public class BattleRobots extends JApplet implements Runnable, ActionListener, KeyListener
{

        // Class Variables are defined here

        // Assignment2.10: We still have a problem.  The reference to the Graphics object
        // is passed to the BattleRobots object as a parameter of its update() and paint()
        // methods, but the value is not retained.  Add a Graphics object
        private Graphics theG;

        // How do I know if 'CellSpace' is a new Class or part of an existing Java Class?
        private CellSpace cellSpace = null;
        //
        private int cellSize;
        private int cellCols;
        private int cellRows;

        // next, come Thread variables
        private Thread gameThread = null;
        private int genTime;
        private final String nextText = "Next";
        private final String startText = "Start";
        private final String stopText = "Stop";
        private Label timeLabel;
        private JButton startStopButton;
        private JButton nextButton;

        private boolean shuttingDown = false;

        // Syntax: the method init() is needed for Applets.
        public void init()
        {
                String param;

                // set background
                setBackground( new Color( 0x999999 ) );

                // read parameters from HTML
                param = getParameter("cellsize");
                if ( param == null) {
                        cellSize = 10;
                } else
                        cellSize = Integer.valueOf( param ).intValue();

                param = getParameter("cellcols");
                if ( param == null ) {
                        cellCols = 100;
                } else
                        cellCols = Integer.valueOf( param ).intValue();

                param = getParameter("cellrows");
                if ( param == null ) {
                        cellRows = 100;
                } else
                        cellRows = Integer.valueOf( param ).intValue();

                param = getParameter("gentime");
                if ( param == null ) {
                        genTime = 500;
                } else
                        genTime = Integer.valueOf( param ).intValue();

                // Syntax: Instanciate new object called 'cellSpace' from 'CellSpace'-Class
                cellSpace = new CellSpace( cellSize, cellCols, cellRows );

                // create components and add them to container
                // Syntax: Instanciate new object called 'timeLabel' from 'Label'-Class
                timeLabel = new Label( "Time: 0           " );

                // Syntax: Instanciate new object called 'startStopButton' from 'JButton'-Class
                // JButton class
                startStopButton = new JButton( startText );
                // Syntax: Instanciate new object called 'nextButton' from 'JButtion'-Class
                nextButton = new JButton( nextText );

                JPanel controls = new JPanel(new BorderLayout());
                controls.add( nextButton );
                controls.add( startStopButton );
                controls.add( timeLabel );

                //
                Container c = getContentPane();
                c.setLayout(new BorderLayout());
                c.add( "South", controls );
                c.add( "Center", cellSpace );

                startStopButton.addActionListener(this);
                nextButton.addActionListener(this);
        }

        // no start() to prevent starting immediately
        public void start2() {
                shuttingDown = false;
                if(gameThread == null) {
                        gameThread = new Thread(this);
                        gameThread.start();
                }
        }

        public void shutDown() {
                if(gameThread != null) {
                        shuttingDown = true;
                        gameThread = null;
                }
        }

        public void run() {
                addKeyListener(this);
                if (cellSpace == null) {
                  cellSpace = new CellSpace( cellSize, cellCols, cellRows );
                }

                while (gameThread != null && !shuttingDown) {
                        cellSpace.next();
                        cellSpace.repaint();
                        showTime();
                        try {
                                gameThread.sleep( genTime );
                        } catch (InterruptedException e){}
                }
        }

        public void keyTyped(KeyEvent keyEvent) {
                int keyCode = keyEvent.getKeyCode();
                switch ( keyCode ) {
                case KeyEvent.VK_DOWN:
                        genTime -= 50;
                        if ( genTime < 0 )
                                genTime = 0;
                        break;
                case KeyEvent.VK_UP:
                        genTime += 50;
                        break;
                default:
                        break; // nothing for us here
                }
                showStatus( "Delay is "+genTime+" ms" );
        }

        public void keyPressed(KeyEvent keyEvent) {
        }

        public void keyReleased(KeyEvent keyEvent) {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            if(actionEvent.getSource() == nextButton) // next
            {
                    cellSpace.next();
                    cellSpace.repaint();
                    showTime();
            }
            else if((actionEvent.getSource()) == startStopButton) // start or stop
            {
                if(startStopButton.getText().equals(startText))  //start
                {
                    start2();
                    startStopButton.setText( stopText );
                }
                else if(startStopButton.getText().equals(stopText))  //stop
                {
                    shutDown();
                    startStopButton.setText( startText );
                }
            }
        }

        public String getAppletInfo()
        {
                return "BattleRobots";
        }

        // Assignment2.9c:  (See 2.9a and 2.9b) To resolve this problem, add an access
        // method to the BattleRobots class such that we can get the Graphics
        // object reference.  Call the method "getTheG()" and be careful that you
        // add it to the BattleRobots class, not the CellSpace inner class.
        public Graphics getTheG()
        {
          return theG;
        }

        // show the time
        public void showTime() {
                timeLabel.setText( "Time: "+cellSpace.time );
        }

	private class CellSpace extends JPanel
	{
          public int time;
          private int cellSize;
          private int cellRows;
          private int cellCols;
          //Assignment2.2: Modify the BattleRobots class such that the "cells" array is a two
          //dimensional array of Robot intead of boolean.  JR changed from "private boolean cells[][]"
          private Robot cells[][];
          private Image offScreenImage = null;
          private Graphics offScreenGraphics;

          private MouseListener mouseListener = new MouseListener() {
            public void mouseClicked(MouseEvent mouseEvent) {
             int x = mouseEvent.getX();
             int y = mouseEvent.getY();

             // Assignment2.5: In the MouseListener inner class within the CellSpace class, the "toggling"
             // of the elements of the cells array needs to be modified.  (This toggling is performed when
             // the user clicks on a cell with the mouse cursor/button.). If the array element is null,
             // meaning there isn't a reference to a Robot object there, then create a Robot object and assign
             // its reference to the specified array element.
             // JR changed from "cells[x / cellSize][y / cellSize] = !cells[x / cellSize][y / cellSize];"
             if (cells[x / cellSize][y / cellSize] == null)
                 // Assignment2.15: Now go back and modify the "toggling" code so that the statement:
                 // "cells[x / cellSize][y / cellSize] = new Robot();"
                 // becomes:...
                 cells[x / cellSize][y / cellSize] = new Robot(BattleRobots.this);
                 // Note we are preceeding the "this" keyword with the BattleRobots class specification
                 // We need to do this since the "this reference is being used inside and inner inner class
                 // and we need to specify the "this" of the enclosing object, rather than the inner object
                 // Don't worry if this is unclear to you at this time. We cover it later.
             else
                cells[x / cellSize][y / cellSize] = null;
            repaint();
            }

            public void mouseEntered(MouseEvent mouseEvent) {
            }
            public void mouseExited(MouseEvent mouseEvent) {
            }
            public void mousePressed(MouseEvent mouseEvent) {
            }
            public void mouseReleased(MouseEvent mouseEvent) {
            }
          };

          public CellSpace( int inpCellSize, int inpCellCols, int inpCellRows ) {
                // Assignment2.3: In the CellSpace constructor, you will need to modify the initialization
                // of the array.  In other words, make sure that you are creating a two dimensional array
                // of references to Robot objects, not booleans.
                // JR changed from "cells = new boolean[inpCellCols][inpCellRows]"
                cells = new Robot[inpCellCols][inpCellRows];
                cellSize = inpCellSize;
                cellCols = inpCellCols;
                cellRows = inpCellRows;
                setBounds(0, 0, cellSize*cellCols-1, cellSize*cellRows-1);
                clear();
                addMouseListener(mouseListener);
          }

          // Assignment2.12: Since the parameter name of the update()  method is "theG", you should change that
          // parameter name to "g" instead, so that you don't hide the instance variables.  In this case,
          // hiding the instance variable doesn't cause a problem, but it's best to avoid potential problems
          // by not hiding instance variables in the first place.
          public synchronized void update( Graphics g )
          {
                // Assignment2.11: now ensure that the first line of both the update() and paint()
                // methods assigns the parameter value to the instance variable theG
                theG = g;

                Dimension dimension = getSize();
                if((offScreenImage == null) ) {
                        offScreenImage = createImage( dimension.width, dimension.height );
                        offScreenGraphics = offScreenImage.getGraphics();
                }
                paint(offScreenGraphics);
                theG.drawImage( offScreenImage, 0, 0, null );
          }

          public void paint(Graphics g) {

                // Assignment2.11: now ensure that the first line of both the update() and paint()
                // methods assigns the parameter value to the instance variable theG
                theG = g;

                // draw background
                g.setColor( Color.gray );
                g.fillRect( 0, 0, cellSize*cellCols-1, cellSize*cellRows-1 );
                // draw grid
                g.setColor( getBackground() );
                for( int x = 1; x < cellCols; x++ ) {
                        g.drawLine( x*cellSize-1, 0, x*cellSize-1, cellSize*cellRows-1 );
                }
                for( int y = 1; y < cellRows; y++ ) {
                        g.drawLine( 0, y*cellSize-1, cellSize*cellCols-1, y*cellSize-1 );
                }
                // draw populated cells
                g.setColor( Color.yellow );
                for( int y = 0; y < cellRows; y++ ) {
                        for( int x = 0; x < cellCols; x++ ) {
                                // Assignment2.6: In CellSpace's paint() method, instead of
                                // "if(cells[x][y])  {g.fillRect....
                                // We are temporarily commenting out this line. The strategy is to make
                                // a set of changes that will compile clean, then make another set of changes
                                // building incrementing, rather than making all hte changes at one time
                                // and then slogging through a large set of compile errors.
                                if ( cells[x][y] != null ) {
                                        // Assignment2.7: With the method render() we are moving the responsibility
                                        // of drawing the Robot object out of the BattleRobots class and into the Robot class
                                        // which is where it should be.
                                        cells[x][y].render(x*cellSize, y*cellSize, cellSize-1, cellSize-1);
                                        //g.fillRect( x*cellSize, y*cellSize, cellSize-1, cellSize-1 );
                                }
                        }
                }
          }

          // clears the cells
          public synchronized void clear() {
                time = 0;
                for( int x = 0; x < cellCols; x++ ) {
                        for( int y = 0; y < cellRows; y++ ) {
                                // Assignment2.4: Now modify the clear() method such that the array
                                // elements are set to "null" instead of "false".
                                // JR changed from "cells[x][y] = false;
                                cells[x][y] = null;
                        }
                }
          }

          // create next battle configuration
          public synchronized void next() {
                time++;
          }
	}  // end of class CellSpace
}
