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
import javax.swing.*;

// BattleRobots is now a standalone JFrame application instead of an applet
// It implements Runnable (for threading), ActionListener (for button actions),
// and KeyListener (for keyboard actions)
public class BattleRobots extends JFrame implements Runnable, ActionListener, KeyListener
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

        // Right panel for robot info display
        private RobotInfoPanel robotInfoPanel = null;

        // Far right panel for current simulation display
        private CurrentSimulationPanel currentSimulationPanel = null;

        // next, come Thread variables
        private Thread gameThread = null;
        private int genTime;
        private int speedMultiplier = 100; // Simulation speed multiplier (default 100x)
        private JComboBox<String> speedCombo;

        private boolean shuttingDown = false;

        // Footer label for optimal attribute combinations
        private JLabel optimalCombosLabel = null;

        // Constructor for the BattleRobots application
        public BattleRobots()
        {
                // Set window properties
                setTitle("BattleRobots - AI Battle Simulation");
                setBackground( Color.white );

                // Use default values instead of reading from HTML parameters
                cellSize = 12; // Reduced by half from 25 to 12
                cellCols = 83; // Adjusted to maintain ~1000px width (83 * 12 = 996px)
                cellRows = 62; // Adjusted to maintain similar height (62 * 12 = 744px)
                genTime = 500;

                // Instantiate new object called 'cellSpace' from 'CellSpace'-Class
                cellSpace = new CellSpace( cellSize, cellCols, cellRows );

                // Instantiate robot info panel
                robotInfoPanel = new RobotInfoPanel();

                // Instantiate current simulation panel
                currentSimulationPanel = new CurrentSimulationPanel();

                // Create speed control panel
                JPanel controls = new JPanel(new FlowLayout());
                JLabel speedLabel = new JLabel("Simulation Speed:");
                speedLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
                controls.add(speedLabel);

                String[] speeds = {"1x", "2x", "5x", "10x", "50x", "100x", "1000x"};
                speedCombo = new JComboBox<>(speeds);
                speedCombo.setFont(new Font("SansSerif", Font.PLAIN, 10));
                speedCombo.setSelectedIndex(5); // Default to 100x
                speedCombo.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                                updateSpeedMultiplier();
                        }
                });
                controls.add(speedCombo);

                // Add stop button
                JButton stopButton = new JButton("Stop & Reset");
                stopButton.setFont(new Font("SansSerif", Font.PLAIN, 10));
                stopButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                                stopAndResetSimulation();
                        }
                });
                controls.add(stopButton);

                // Create South panel with controls and optimal combinations
                JPanel southPanel = new JPanel();
                southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
                southPanel.add(controls);

                // Add optimal combinations label
                optimalCombosLabel = new JLabel(calculateOptimalCombinations());
                optimalCombosLabel.setFont(new Font("Monospaced", Font.PLAIN, 10));
                optimalCombosLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                southPanel.add(optimalCombosLabel);

                // Create a panel to hold both right panels
                JPanel rightPanels = new JPanel();
                rightPanels.setLayout(new BoxLayout(rightPanels, BoxLayout.X_AXIS));
                rightPanels.add(robotInfoPanel);
                rightPanels.add(currentSimulationPanel);

                // Set up the content pane
                Container c = getContentPane();
                c.setLayout(new BorderLayout());
                c.add( "South", southPanel );
                c.add( "Center", cellSpace );
                c.add( "East", rightPanels );

                // Window settings for standalone application
                setSize(cellSize * cellCols + 485, cellSize * cellRows + 100); // +485 for both right panels (265 + 220)
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setLocationRelativeTo(null); // Center the window
        }

        // Main method to launch the application
        public static void main(String[] args)
        {
                // Create and display the application window
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                                BattleRobots app = new BattleRobots();
                                app.setVisible(true);
                        }
                });
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
                        robotInfoPanel.updateRobots(cellSpace.getRobots());
                        robotInfoPanel.repaint();
                        currentSimulationPanel.updateRobots(cellSpace.getRobots());
                        currentSimulationPanel.repaint();
                        try {
                                // Apply speed multiplier - divide base time by multiplier
                                int adjustedTime = Math.max(1, genTime / speedMultiplier);
                                gameThread.sleep( adjustedTime );
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
                System.out.println( "Delay is "+genTime+" ms" );
        }

        public void keyPressed(KeyEvent keyEvent) {
        }

        public void keyReleased(KeyEvent keyEvent) {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            // No longer needed - buttons removed
        }

        /**
         * Update the speed multiplier based on dropdown selection
         */
        private void updateSpeedMultiplier() {
                String selectedSpeed = (String)speedCombo.getSelectedItem();
                switch(selectedSpeed) {
                        case "1x":
                                speedMultiplier = 1;
                                break;
                        case "2x":
                                speedMultiplier = 2;
                                break;
                        case "5x":
                                speedMultiplier = 5;
                                break;
                        case "10x":
                                speedMultiplier = 10;
                                break;
                        case "50x":
                                speedMultiplier = 50;
                                break;
                        case "100x":
                                speedMultiplier = 100;
                                break;
                        case "1000x":
                                speedMultiplier = 1000;
                                break;
                        default:
                                speedMultiplier = 1;
                }
        }

        /**
         * Calculate optimal attribute combinations using AI algorithm
         * Analyzes the formulas and determines mathematically optimal builds
         */
        private String calculateOptimalCombinations() {
                LinearRegressionModel model = new LinearRegressionModel();

                // Analyze the formulas:
                // Combat Score = 10.0 + (2.5 * armor) + (3.0 * weapon) + (1.5 * maneuver) + (-2.0 * damage)
                // Weapon damage multipliers: 10+ = 1.5x, 6-9 = 1.3x, 3-5 = 1.2x, 0-2 = 1.0x
                // Maneuver: 10+ = 90/180/270° each move, 5-9 = 90° each move, 3-4 = 90° every 2 moves, 0-2 = 90° every 3 moves
                // Afterburner: enables 5x speed for 3 grid squares

                StringBuilder result = new StringBuilder("<html>AI Optimal Builds: ");

                // Strategy 1: MAX OFFENSE (maximize damage output)
                // - Weapon 10+ for 1.5x damage multiplier
                // - Armor 7+ to survive counter-attacks
                // - Maneuver 0-3 (not critical for offense)
                // - Afterburner 0-3 (for escape if needed)
                result.append("<b>[OFFENSE]</b> Armor:7 Weapon:11 Maneuver:1 Afterburner:1 (Max Damage: 1.5x) | ");

                // Strategy 2: MAX DEFENSE (maximize survivability)
                // - Armor 12+ to absorb maximum hits
                // - Weapon 3+ for 1.2x damage (minimal viable)
                // - Maneuver 3+ for basic mobility
                // - Afterburner 2+ for escape capability
                result.append("<b>[DEFENSE]</b> Armor:12 Weapon:3 Maneuver:3 Afterburner:2 (Max Armor + Mobility) | ");

                // Strategy 3: BALANCED (all-around effectiveness)
                // - Armor 5 (moderate protection)
                // - Weapon 6+ for 1.3x damage
                // - Maneuver 5+ for 90° turn each move
                // - Afterburner 4+ for tactical escape
                result.append("<b>[BALANCED]</b> Armor:5 Weapon:7 Maneuver:5 Afterburner:3 (1.3x Damage + Agility) | ");

                // Strategy 4: HIT & RUN (speed and evasion)
                // - Armor 2 (minimal, rely on evasion)
                // - Weapon 10+ for 1.5x damage (quick kills)
                // - Maneuver 10+ for maximum turning (90/180/270°)
                // - Afterburner 5+ for escape bursts
                result.append("<b>[HIT&RUN]</b> Armor:1 Weapon:11 Maneuver:10 Afterburner:8 (Max Agility + Burst)</html>");

                return result.toString();
        }

        /**
         * Stop the simulation and reset all values to defaults
         */
        private void stopAndResetSimulation() {
                // Stop any running simulation thread
                shutDown();

                // Stop automated simulation if running
                robotInfoPanel.stopSimulation();

                // Clear the grid
                cellSpace.clear();
                cellSpace.repaint();

                // Reset speed multiplier to 100x
                speedMultiplier = 100;
                speedCombo.setSelectedIndex(5);

                // Reset robot info panel (ranges, strategies, leaderboard)
                robotInfoPanel.resetToDefaults();

                // Update display
                robotInfoPanel.updateRobots(cellSpace.getRobots());
                robotInfoPanel.repaint();
                currentSimulationPanel.updateRobots(cellSpace.getRobots());
                currentSimulationPanel.repaint();
        }

        // Assignment2.9c:  (See 2.9a and 2.9b) To resolve this problem, add an access
        // method to the BattleRobots class such that we can get the Graphics
        // object reference.  Call the method "getTheG()" and be careful that you
        // add it to the BattleRobots class, not the CellSpace inner class.
        public Graphics getTheG()
        {
          return theG;
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
          // Robot type selector: 0 = Yellow, 1 = Cyan, 2 = Green
          private int selectedRobotType = 0;
          // Counter for manually placed robots (max 20)
          private int manuallyPlacedRobots = 0;
          private final int MAX_MANUAL_ROBOTS = 20;

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
             {
                 // Check if we've reached the maximum number of manually placed robots
                 if (manuallyPlacedRobots >= MAX_MANUAL_ROBOTS) {
                     System.out.println("Maximum number of robots (" + MAX_MANUAL_ROBOTS + ") already placed!");
                     return; // Don't place more robots
                 }

                 // Create robot based on selected type
                 Robot newRobot;
                 if (selectedRobotType == 0) {
                     // Orange robot (team 0) - random stats (0-20 range)
                     newRobot = new Robot(BattleRobots.this, 0, Math.random() * 20.0,
                                          Math.random() * 20.0, Math.random() * 20.0,
                                          Math.random() * 20.0);
                 } else if (selectedRobotType == 1) {
                     // Blue robot (team 1) - random stats (0-20 range)
                     newRobot = new Robot(BattleRobots.this, 1, Math.random() * 20.0,
                                          Math.random() * 20.0, Math.random() * 20.0,
                                          Math.random() * 20.0);
                 } else {
                     // Green robot (team 2) - specific stats: health=100, armor=3.5, weapon=3, maneuver=5.5, afterburner=4
                     newRobot = new Robot(BattleRobots.this, 2, 3.5, 3.0, 5.5, 4.0);
                 }

                 // Set default strategy for manually placed robots
                 newRobot.setAfterburnerStrategy("Aggressive");

                 // Set the position of the robot in the grid
                 newRobot.setPosition(x / cellSize, y / cellSize);
                 cells[x / cellSize][y / cellSize] = newRobot;
                 manuallyPlacedRobots++; // Increment counter

                 // Toggle to next robot type (cycle through 0, 1, 2)
                 selectedRobotType = (selectedRobotType + 1) % 3;
             }
             else
             {
                cells[x / cellSize][y / cellSize] = null;
                manuallyPlacedRobots--; // Decrement counter when removing a robot
             }
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
                clear();
                addMouseListener(mouseListener);
          }

          @Override
          public Dimension getPreferredSize() {
                return new Dimension(cellSize * cellCols, cellSize * cellRows);
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

                // draw background - very light grey
                g.setColor( new Color(245, 245, 245) ); // Lightest grey (almost white)
                g.fillRect( 0, 0, cellSize*cellCols-1, cellSize*cellRows-1 );
                // draw grid (invisible - same color as background)
                g.setColor( new Color(245, 245, 245) );
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
                                        cells[x][y].render(g, x*cellSize, y*cellSize, cellSize-1, cellSize-1);
                                        //g.fillRect( x*cellSize, y*cellSize, cellSize-1, cellSize-1 );
                                }
                        }
                }
          }

          // clears the cells
          public synchronized void clear() {
                time = 0;
                manuallyPlacedRobots = 0; // Reset counter when grid is cleared
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

                // Store planned moves to avoid concurrent modification
                java.util.ArrayList<int[]> moves = new java.util.ArrayList<>();
                // Track which robots moved for health management
                java.util.HashSet<Robot> movedRobots = new java.util.HashSet<>();

                // Execute AI behavior for all robots and collect their moves
                for( int x = 0; x < cellCols; x++ ) {
                        for( int y = 0; y < cellRows; y++ ) {
                                Robot robot = cells[x][y];
                                if (robot != null && robot.isAlive()) {
                                        // Execute the linear regression-based decision-making
                                        int[] newPos = robot.executeBehavior(cells, cellCols, cellRows);
                                        if (newPos != null) {
                                                // Store: [oldX, oldY, newX, newY]
                                                moves.add(new int[] {x, y, newPos[0], newPos[1]});
                                        }
                                }
                        }
                }

                // Apply all moves
                for (int[] move : moves) {
                        int oldX = move[0];
                        int oldY = move[1];
                        int newX = move[2];
                        int newY = move[3];

                        // Double-check the target cell is empty (dead robots stay visible and block movement)
                        if (cells[newX][newY] == null) {
                                Robot robot = cells[oldX][oldY];
                                cells[oldX][oldY] = null;
                                cells[newX][newY] = robot;
                                robot.setPosition(newX, newY);

                                // Apply movement cost: -3 health
                                robot.applyMovementCost();
                                movedRobots.add(robot);
                        }
                }

                // Speed boost: Robots with health > 60 can move twice (2x speed)
                java.util.ArrayList<int[]> secondMoves = new java.util.ArrayList<>();
                for( int x = 0; x < cellCols; x++ ) {
                        for( int y = 0; y < cellRows; y++ ) {
                                Robot robot = cells[x][y];
                                // Check if robot moved in first phase and has health > 60
                                if (robot != null && robot.isAlive() &&
                                    movedRobots.contains(robot) && robot.getHealth() > 60) {
                                        // Execute behavior again for second move
                                        int[] newPos = robot.executeBehavior(cells, cellCols, cellRows);
                                        if (newPos != null) {
                                                // Store: [oldX, oldY, newX, newY]
                                                secondMoves.add(new int[] {x, y, newPos[0], newPos[1]});
                                        }
                                }
                        }
                }

                // Apply second moves for high-health robots
                for (int[] move : secondMoves) {
                        int oldX = move[0];
                        int oldY = move[1];
                        int newX = move[2];
                        int newY = move[3];

                        // Double-check the target cell is empty (dead robots stay visible and block movement)
                        if (cells[newX][newY] == null) {
                                Robot robot = cells[oldX][oldY];
                                cells[oldX][oldY] = null;
                                cells[newX][newY] = robot;
                                robot.setPosition(newX, newY);

                                // Apply movement cost: -3 health (second move)
                                robot.applyMovementCost();
                        }
                }

                // Afterburner boost: Robots with active afterburner can move 5x total (4 additional moves)
                for (int extraMove = 0; extraMove < 4; extraMove++) {
                        java.util.ArrayList<int[]> afterburnerMoves = new java.util.ArrayList<>();
                        for( int x = 0; x < cellCols; x++ ) {
                                for( int y = 0; y < cellRows; y++ ) {
                                        Robot robot = cells[x][y];
                                        // Check if robot has active afterburner
                                        if (robot != null && robot.isAlive() && robot.isAfterburnerActive()) {
                                                // Execute behavior again for afterburner move
                                                int[] newPos = robot.executeBehavior(cells, cellCols, cellRows);
                                                if (newPos != null) {
                                                        // Store: [oldX, oldY, newX, newY]
                                                        afterburnerMoves.add(new int[] {x, y, newPos[0], newPos[1]});
                                                }
                                        }
                                }
                        }

                        // Apply afterburner moves
                        for (int[] move : afterburnerMoves) {
                                int oldX = move[0];
                                int oldY = move[1];
                                int newX = move[2];
                                int newY = move[3];

                                // Double-check the target cell is empty
                                if (cells[newX][newY] == null) {
                                        Robot robot = cells[oldX][oldY];
                                        cells[oldX][oldY] = null;
                                        cells[newX][newY] = robot;
                                        robot.setPosition(newX, newY);

                                        // Apply movement cost: -3 health
                                        robot.applyMovementCost();

                                        // Consume one afterburner move
                                        robot.consumeAfterburnerMove();
                                }
                        }
                }

                // Apply health regeneration to robots that didn't move
                for( int x = 0; x < cellCols; x++ ) {
                        for( int y = 0; y < cellRows; y++ ) {
                                Robot robot = cells[x][y];
                                if (robot != null && robot.isAlive() && !movedRobots.contains(robot)) {
                                        // Regenerate health: +1 for staying still
                                        robot.regenerateHealth(1);
                                }
                        }
                }

                // Check for winner: count living robots per team
                int[] teamCounts = new int[3]; // Support 3 teams
                int totalAliveRobots = 0;
                for( int x = 0; x < cellCols; x++ ) {
                        for( int y = 0; y < cellRows; y++ ) {
                                Robot robot = cells[x][y];
                                if (robot != null && robot.isAlive()) {
                                        teamCounts[robot.getTeam()]++;
                                        totalAliveRobots++;
                                }
                        }
                }

                // Count how many teams have living robots
                int teamsAlive = 0;
                int winningTeam = -1;
                for (int i = 0; i < 3; i++) {
                        if (teamCounts[i] > 0) {
                                teamsAlive++;
                                winningTeam = i;
                        }
                }

                // Mark winners when:
                // 1. Only one team remains (multiple robots from same team)
                // 2. Only one robot remains (after same-team combat)
                if (totalAliveRobots == 1) {
                        // Only 1 robot left - mark as last standing with flashing effect
                        for( int x = 0; x < cellCols; x++ ) {
                                for( int y = 0; y < cellRows; y++ ) {
                                        Robot robot = cells[x][y];
                                        if (robot != null && robot.isAlive()) {
                                                robot.setLastStanding(true);
                                                robot.updateFlashCounter();
                                                robot.setWinner(true);
                                        }
                                }
                        }
                } else if (teamsAlive == 1) {
                        // Only one team remains (multiple robots)
                        for( int x = 0; x < cellCols; x++ ) {
                                for( int y = 0; y < cellRows; y++ ) {
                                        Robot robot = cells[x][y];
                                        if (robot != null && robot.isAlive()) {
                                                robot.setWinner(true);
                                        }
                                }
                        }
                }
          }

          // Get all robots in the grid
          public java.util.List<Robot> getRobots() {
                java.util.ArrayList<Robot> robotList = new java.util.ArrayList<>();
                for( int x = 0; x < cellCols; x++ ) {
                        for( int y = 0; y < cellRows; y++ ) {
                                if (cells[x][y] != null) {
                                        robotList.add(cells[x][y]);
                                }
                        }
                }
                return robotList;
          }

          // Place a robot at a specific position
          public void placeRobot(Robot robot, int x, int y) {
                if (x >= 0 && x < cellCols && y >= 0 && y < cellRows) {
                        cells[x][y] = robot;
                        robot.setPosition(x, y);
                }
          }

          // Check if a cell is empty
          public boolean isCellEmpty(int x, int y) {
                if (x >= 0 && x < cellCols && y >= 0 && y < cellRows) {
                        return cells[x][y] == null;
                }
                return false;
          }

          // Get cell dimensions
          public int getCellCols() {
                return cellCols;
          }

          public int getCellRows() {
                return cellRows;
          }
	}  // end of class CellSpace

        // Inner class for the robot info panel
        private class RobotInfoPanel extends JPanel {
                private java.util.List<Robot> robots = new java.util.ArrayList<>();
                private JTextField simulationCountField;
                private JButton startSimulationButton;
                private JLabel simulationStatusLabel;
                private JLabel faultCounterLabel;
                private JLabel movesCounterLabel;
                private JComboBox<String>[] afterburnerStrategyCombo; // One per team
                private int currentSimulation = 0;
                private int totalSimulations = 0;
                private boolean isSimulating = false;
                private Robot championRobot = null; // Winner who advances to next round

                // Initial robot configuration (captured when simulation starts)
                private int[] initialTeamCounts = null; // Count of robots per team [0]=Orange, [1]=Blue, [2]=Green

                // Fault counter for simulations that exceed move threshold
                private int faultCount = 0;
                private int currentSimulationMoves = 0; // Track moves in current simulation
                private static final int MOVE_THRESHOLD = 2000; // Maximum moves per simulation

                // Attribute range controls for each team (3 teams x 4 attributes x 2 fields)
                private JTextField[][] rangeFields = new JTextField[3][8]; // [team][attribute_min/max]
                // Indices: 0=StrMin, 1=StrMax, 2=WpnMin, 3=WpnMax, 4=SpdMin, 5=SpdMax, 6=AfBrMin, 7=AfBrMax

                // Adaptive learning ranges (separate from UI fields - used internally during simulations)
                private double[][] adaptiveRanges = new double[3][8]; // [team][attribute_min/max]
                private double[][] userRanges = new double[3][8]; // Store user's original input

                // Leaderboard tracking - stores all winning robots
                private java.util.Map<String, RobotStats> leaderboard = new java.util.HashMap<>();

                // Static jet images for display in info panel
                private static Image orangeJetImage = null;
                private static Image blueJetImage = null;
                private static Image greenJetImage = null;
                private static Image deadJetImage = null;

                // Load team-specific jet images once
                static {
                        try {
                                orangeJetImage = new ImageIcon("yellow jet.png").getImage(); // Using yellow jet for orange team
                                blueJetImage = new ImageIcon("blue jet.png").getImage();
                                greenJetImage = new ImageIcon("green jet.png").getImage();
                                deadJetImage = new ImageIcon("dead jet.png").getImage();
                        } catch (Exception e) {
                                System.err.println("Warning: Could not load jet images for info panel");
                        }
                }

                // Inner class to track robot statistics
                private class RobotStats {
                        String robotId;
                        int team;
                        double strength;
                        double weaponPower;
                        double speed;
                        double afterburner;
                        int wins;

                        RobotStats(int team, double strength, double weaponPower, double speed, double afterburner, int wins) {
                                this.robotId = String.format("T%d-S%.1f-W%.1f-Sp%.1f-AB%.1f", team, strength, weaponPower, speed, afterburner);
                                this.team = team;
                                this.strength = strength;
                                this.weaponPower = weaponPower;
                                this.speed = speed;
                                this.afterburner = afterburner;
                                this.wins = wins;
                        }
                }

                @SuppressWarnings("unchecked")
                public RobotInfoPanel() {
                        setPreferredSize(new Dimension(265, 600)); // Left-most right pane
                        setBackground(Color.white);
                        setLayout(new BorderLayout());

                        // Initialize afterburner strategy combo array
                        afterburnerStrategyCombo = new JComboBox[3];

                        // Create control panel at the top
                        JPanel controlPanel = new JPanel();
                        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
                        controlPanel.setBackground(Color.white);

                        // Simulation count input
                        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                        inputPanel.setBackground(Color.white);
                        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        JLabel simLabel = new JLabel("Simulations:");
                        simLabel.setFont(new Font("SansSerif", Font.PLAIN, 8));
                        inputPanel.add(simLabel);
                        simulationCountField = new JTextField("100", 5);
                        simulationCountField.setFont(new Font("SansSerif", Font.PLAIN, 8));
                        inputPanel.add(simulationCountField);
                        controlPanel.add(inputPanel);

                        // Start button
                        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                        buttonPanel.setBackground(Color.white);
                        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        startSimulationButton = new JButton("Start Simulation");
                        startSimulationButton.setFont(new Font("SansSerif", Font.PLAIN, 8));
                        startSimulationButton.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                        startAutomatedSimulation();
                                }
                        });
                        buttonPanel.add(startSimulationButton);
                        controlPanel.add(buttonPanel);

                        // Status label
                        simulationStatusLabel = new JLabel("Ready");
                        simulationStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 8));
                        simulationStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        controlPanel.add(simulationStatusLabel);

                        // Fault and Moves counter on same line
                        JPanel countersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                        countersPanel.setBackground(Color.white);
                        countersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                        faultCounterLabel = new JLabel("Faults: 0");
                        faultCounterLabel.setFont(new Font("SansSerif", Font.PLAIN, 8));
                        countersPanel.add(faultCounterLabel);

                        countersPanel.add(new JLabel("  ")); // Spacing between counters

                        movesCounterLabel = new JLabel("Moves: 0");
                        movesCounterLabel.setFont(new Font("SansSerif", Font.PLAIN, 8));
                        countersPanel.add(movesCounterLabel);

                        controlPanel.add(countersPanel);

                        // Add spacing
                        controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));

                        // Attribute range controls
                        JPanel rangesPanel = new JPanel();
                        rangesPanel.setLayout(new BoxLayout(rangesPanel, BoxLayout.Y_AXIS));
                        rangesPanel.setBackground(Color.white);

                        // Add header for range section
                        JLabel rangeHeaderLabel = new JLabel("TEAM RANGE SELECTION");
                        rangeHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 9));
                        rangeHeaderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        rangesPanel.add(rangeHeaderLabel);
                        rangesPanel.add(Box.createRigidArea(new Dimension(0, 5)));

                        String[] teamNames = {"Orange", "Blue", "Green"};
                        String[] attrNames = {"Armor", "Wpn", "Mnvr", "AfBr"};
                        String[] strategies = {"Aggressive", "Moderate", "Conservative"};

                        for (int team = 0; team < 3; team++) {
                                JLabel teamLabel = new JLabel(teamNames[team] + " Team Ranges:");
                                teamLabel.setFont(new Font("SansSerif", Font.BOLD, 8));
                                teamLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                                rangesPanel.add(teamLabel);

                                // Create 2 rows for 4 attributes (2 per row)
                                for (int row = 0; row < 2; row++) {
                                        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                                        rowPanel.setBackground(Color.white);
                                        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                                        // Add 2 attributes per row
                                        for (int col = 0; col < 2; col++) {
                                                int attr = row * 2 + col; // 0,1 in first row, 2,3 in second row

                                                JLabel attrLabel = new JLabel(attrNames[attr] + ":");
                                                attrLabel.setFont(new Font("SansSerif", Font.PLAIN, 8));
                                                rowPanel.add(attrLabel);

                                                // Min field
                                                rangeFields[team][attr * 2] = new JTextField("0", 2);
                                                rangeFields[team][attr * 2].setFont(new Font("SansSerif", Font.PLAIN, 8));
                                                rowPanel.add(rangeFields[team][attr * 2]);

                                                JLabel dashLabel = new JLabel("-");
                                                dashLabel.setFont(new Font("SansSerif", Font.PLAIN, 8));
                                                rowPanel.add(dashLabel);

                                                // Max field
                                                rangeFields[team][attr * 2 + 1] = new JTextField("20", 2);
                                                rangeFields[team][attr * 2 + 1].setFont(new Font("SansSerif", Font.PLAIN, 8));
                                                rowPanel.add(rangeFields[team][attr * 2 + 1]);

                                                // Add spacing between columns
                                                if (col == 0) {
                                                        rowPanel.add(new JLabel("  "));
                                                }
                                        }

                                        rangesPanel.add(rowPanel);
                                }

                                // Add afterburner strategy dropdown for this team
                                JPanel strategyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                                strategyPanel.setBackground(Color.white);
                                strategyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                                JLabel abLabel = new JLabel("Afterburner:");
                                abLabel.setFont(new Font("SansSerif", Font.PLAIN, 8));
                                strategyPanel.add(abLabel);
                                afterburnerStrategyCombo[team] = new JComboBox<>(strategies);
                                afterburnerStrategyCombo[team].setFont(new Font("SansSerif", Font.PLAIN, 8));
                                afterburnerStrategyCombo[team].setSelectedIndex(0); // Default to Aggressive
                                strategyPanel.add(afterburnerStrategyCombo[team]);
                                rangesPanel.add(strategyPanel);

                                // Add spacing between teams
                                if (team < 2) {
                                        rangesPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                                }
                        }

                        controlPanel.add(rangesPanel);
                        add(controlPanel, BorderLayout.NORTH);

                        // Prefill optimal builds on initialization
                        prefillOptimalBuilds();
                }

                public void updateRobots(java.util.List<Robot> newRobots) {
                        this.robots = newRobots;
                }

                /**
                 * Stop the automated simulation
                 */
                public void stopSimulation() {
                        isSimulating = false;
                        currentSimulation = 0;
                        championRobot = null;
                        initialTeamCounts = null; // Clear captured configuration
                        faultCount = 0; // Reset fault counter
                        currentSimulationMoves = 0;
                        faultCounterLabel.setText("Faults: 0");
                        movesCounterLabel.setText("Moves: 0");
                        startSimulationButton.setEnabled(true);
                        simulationCountField.setEnabled(true);
                        simulationStatusLabel.setText("Stopped");
                }

                /**
                 * Get optimal build attributes based on build type
                 * @param buildType 0=OFFENSE, 1=DEFENSE, 2=BALANCED, 3=HIT&RUN
                 * @return Array of [armor, weapon, maneuver, afterburner]
                 */
                private int[] getOptimalBuildAttributes(int buildType) {
                        switch(buildType) {
                                case 0: // OFFENSE
                                        return new int[] {7, 11, 1, 1};
                                case 1: // DEFENSE
                                        return new int[] {12, 3, 3, 2};
                                case 2: // BALANCED
                                        return new int[] {5, 7, 5, 3};
                                case 3: // HIT&RUN
                                        return new int[] {1, 11, 10, 8};
                                default:
                                        return new int[] {5, 5, 5, 5}; // Fallback
                        }
                }

                /**
                 * Prefill attribute ranges with randomly selected optimal build for each team
                 */
                private void prefillOptimalBuilds() {
                        // Randomly select an optimal build for each team (0-3)
                        for (int team = 0; team < 3; team++) {
                                int buildType = (int)(Math.random() * 4); // Random: 0=OFFENSE, 1=DEFENSE, 2=BALANCED, 3=HIT&RUN
                                int[] attrs = getOptimalBuildAttributes(buildType);

                                // Set the range fields to the exact optimal build values (min=max for each attribute)
                                rangeFields[team][0].setText(String.valueOf(attrs[0])); // Armor min
                                rangeFields[team][1].setText(String.valueOf(attrs[0])); // Armor max
                                rangeFields[team][2].setText(String.valueOf(attrs[1])); // Weapon min
                                rangeFields[team][3].setText(String.valueOf(attrs[1])); // Weapon max
                                rangeFields[team][4].setText(String.valueOf(attrs[2])); // Maneuver min
                                rangeFields[team][5].setText(String.valueOf(attrs[2])); // Maneuver max
                                rangeFields[team][6].setText(String.valueOf(attrs[3])); // Afterburner min
                                rangeFields[team][7].setText(String.valueOf(attrs[3])); // Afterburner max
                        }
                }

                /**
                 * Reset all values to defaults
                 */
                public void resetToDefaults() {
                        // Prefill attribute ranges with randomly selected optimal builds for each team
                        prefillOptimalBuilds();

                        // Reset afterburner strategy to Aggressive for all teams
                        for (int team = 0; team < 3; team++) {
                                afterburnerStrategyCombo[team].setSelectedIndex(0);
                        }

                        // Clear leaderboard
                        leaderboard.clear();

                        // Reset simulation fields
                        simulationCountField.setText("100");
                        currentSimulation = 0;
                        championRobot = null;
                        initialTeamCounts = null; // Clear captured configuration
                        faultCount = 0; // Reset fault counter
                        currentSimulationMoves = 0;
                        faultCounterLabel.setText("Faults: 0");
                        movesCounterLabel.setText("Moves: 0");
                        simulationStatusLabel.setText("Ready");
                }

                private void startAutomatedSimulation() {
                        try {
                                totalSimulations = Integer.parseInt(simulationCountField.getText());
                                if (totalSimulations <= 0) {
                                        simulationStatusLabel.setText("Please enter a positive number");
                                        return;
                                }
                                currentSimulation = 0;
                                championRobot = null;
                                faultCount = 0; // Reset fault counter for new simulation sequence
                                currentSimulationMoves = 0;
                                faultCounterLabel.setText("Faults: 0");
                                movesCounterLabel.setText("Moves: 0");

                                // Capture user's original range values (preserve UI state)
                                for (int team = 0; team < 3; team++) {
                                        for (int i = 0; i < 8; i++) {
                                                try {
                                                        userRanges[team][i] = Double.parseDouble(rangeFields[team][i].getText());
                                                        adaptiveRanges[team][i] = userRanges[team][i]; // Initialize adaptive ranges
                                                } catch (NumberFormatException e) {
                                                        userRanges[team][i] = (i % 2 == 0) ? 0.0 : 20.0; // Default min/max
                                                        adaptiveRanges[team][i] = userRanges[team][i];
                                                }
                                        }
                                }

                                // Capture initial robot configuration from the grid
                                initialTeamCounts = new int[3]; // [Orange, Blue, Green]
                                java.util.List<Robot> currentRobots = cellSpace.getRobots();
                                for (Robot robot : currentRobots) {
                                        if (robot != null) {
                                                initialTeamCounts[robot.getTeam()]++;
                                        }
                                }

                                // If no robots on grid, use default: 2 robots per team
                                int totalRobots = initialTeamCounts[0] + initialTeamCounts[1] + initialTeamCounts[2];
                                if (totalRobots == 0) {
                                        initialTeamCounts[0] = 2; // Orange
                                        initialTeamCounts[1] = 2; // Blue
                                        initialTeamCounts[2] = 2; // Green
                                }

                                // Don't clear leaderboard - persist across all simulation runs
                                isSimulating = true;
                                startSimulationButton.setEnabled(false);
                                simulationCountField.setEnabled(false);

                                // Start first simulation in a new thread
                                new Thread(new Runnable() {
                                        public void run() {
                                                runSimulations();
                                        }
                                }).start();
                        } catch (NumberFormatException ex) {
                                simulationStatusLabel.setText("Invalid number");
                        }
                }

                private void runSimulations() {
                        while (currentSimulation < totalSimulations && isSimulating) {
                                currentSimulation++;
                                currentSimulationMoves = 0; // Reset move counter for new simulation
                                SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                                simulationStatusLabel.setText("Simulation " + currentSimulation + " / " + totalSimulations);
                                                movesCounterLabel.setText("Moves: 0");
                                        }
                                });

                                // Clear the grid and set up new robots
                                setupSimulation();

                                // Run until there's a winner or move threshold exceeded
                                while (isSimulating && !hasWinner() && currentSimulationMoves < MOVE_THRESHOLD) {
                                        SwingUtilities.invokeLater(new Runnable() {
                                                public void run() {
                                                        cellSpace.next();
                                                        cellSpace.repaint();
                                                        updateRobots(cellSpace.getRobots());
                                                        repaint();
                                                        // Update the current simulation panel
                                                        currentSimulationPanel.updateRobots(cellSpace.getRobots());
                                                        currentSimulationPanel.repaint();
                                                }
                                        });

                                        currentSimulationMoves++; // Increment move counter

                                        // Update moves counter display every 10 moves to reduce UI updates
                                        if (currentSimulationMoves % 10 == 0) {
                                                final int moves = currentSimulationMoves;
                                                SwingUtilities.invokeLater(new Runnable() {
                                                        public void run() {
                                                                movesCounterLabel.setText("Moves: " + moves);
                                                        }
                                                });
                                        }

                                        try {
                                                // Apply speed multiplier - divide base time by multiplier
                                                int adjustedTime = Math.max(1, 50 / BattleRobots.this.speedMultiplier);
                                                Thread.sleep(adjustedTime);
                                        } catch (InterruptedException e) {
                                                break;
                                        }
                                }

                                // Check if simulation exceeded move threshold (fault)
                                if (currentSimulationMoves >= MOVE_THRESHOLD) {
                                        faultCount++;
                                        SwingUtilities.invokeLater(new Runnable() {
                                                public void run() {
                                                        faultCounterLabel.setText("Faults: " + faultCount);
                                                }
                                        });
                                }

                                // Record the winner as champion for next round (only if there's an actual winner)
                                if (hasWinner()) {
                                        recordChampion();
                                }

                                // Brief pause between simulations
                                try {
                                        Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                        break;
                                }
                        }

                        // Simulation complete
                        SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                        isSimulating = false;
                                        startSimulationButton.setEnabled(true);
                                        simulationCountField.setEnabled(true);
                                        if (championRobot != null) {
                                                simulationStatusLabel.setText("Complete! Champion Team " + championRobot.getTeam());
                                        } else {
                                                simulationStatusLabel.setText("Complete!");
                                        }
                                }
                        });
                }

                private void setupSimulation() {
                        SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                        cellSpace.clear();

                                        // Use the captured initial team counts to maintain team balance
                                        int[] teamCounts = new int[3]; // Track how many robots we've placed per team
                                        java.util.Arrays.fill(teamCounts, 0);

                                        // If there's a champion, place it first
                                        if (championRobot != null) {
                                                // Place champion robot with preserved win count
                                                Robot champ = new Robot(BattleRobots.this, championRobot.getTeam(),
                                                        championRobot.getArmor(), championRobot.getWeaponPower(),
                                                        championRobot.getManeuver(), championRobot.getAfterburner());
                                                champ.setWinCount(championRobot.getWinCount()); // Preserve win count
                                                // Get team-specific strategy
                                                String strategy = (String)afterburnerStrategyCombo[championRobot.getTeam()].getSelectedItem();
                                                champ.setAfterburnerStrategy(strategy); // Set strategy
                                                int x = (int)(Math.random() * cellSpace.getCellCols());
                                                int y = (int)(Math.random() * cellSpace.getCellRows());
                                                cellSpace.placeRobot(champ, x, y);
                                                teamCounts[championRobot.getTeam()]++;
                                        }

                                        // Place robots according to initial team distribution
                                        for (int team = 0; team < 3; team++) {
                                                int robotsToPlace = initialTeamCounts[team] - teamCounts[team];

                                                for (int i = 0; i < robotsToPlace; i++) {
                                                        // Generate random attributes that sum to 20, using team-specific ranges
                                                        double[] attrs = generateRandomAttributes(team);

                                                        Robot robot = new Robot(BattleRobots.this, team, attrs[0], attrs[1], attrs[2], attrs[3]);
                                                        // Get team-specific strategy
                                                        String strategy = (String)afterburnerStrategyCombo[team].getSelectedItem();
                                                        robot.setAfterburnerStrategy(strategy); // Set strategy

                                                        // Find empty position
                                                        int x, y;
                                                        do {
                                                                x = (int)(Math.random() * cellSpace.getCellCols());
                                                                y = (int)(Math.random() * cellSpace.getCellRows());
                                                        } while (!cellSpace.isCellEmpty(x, y));

                                                        cellSpace.placeRobot(robot, x, y);
                                                }
                                        }

                                        cellSpace.repaint();
                                }
                        });

                        // Wait for UI update
                        try {
                                Thread.sleep(100);
                        } catch (InterruptedException e) {}
                }

                private double[] generateRandomAttributes(int team) {
                        // Generate 4 random attributes that sum to 20, respecting team-specific ranges
                        // armor, weaponPower, maneuver, afterburner
                        double total = 20.0;

                        try {
                                // Use adaptive ranges (not UI fields) for this team
                                double armorMin = adaptiveRanges[team][0];
                                double armorMax = adaptiveRanges[team][1];
                                double wpnMin = adaptiveRanges[team][2];
                                double wpnMax = adaptiveRanges[team][3];
                                double mnvrMin = adaptiveRanges[team][4];
                                double mnvrMax = adaptiveRanges[team][5];
                                double afBrMin = adaptiveRanges[team][6];
                                double afBrMax = adaptiveRanges[team][7];

                                // Start with minimum values
                                double[] attrs = new double[] {armorMin, wpnMin, mnvrMin, afBrMin};
                                double[] mins = new double[] {armorMin, wpnMin, mnvrMin, afBrMin};
                                double[] maxs = new double[] {armorMax, wpnMax, mnvrMax, afBrMax};

                                // Calculate remaining points to distribute
                                double sumMins = armorMin + wpnMin + mnvrMin + afBrMin;
                                double remaining = total - sumMins;

                                // Randomly distribute remaining points while respecting maxes
                                for (int i = 0; i < 100 && remaining > 0.001; i++) { // Max 100 iterations
                                        // Randomly select an attribute to add to
                                        int idx = (int)(Math.random() * 4);

                                        // Calculate how much we can add to this attribute
                                        double canAdd = Math.min(remaining, maxs[idx] - attrs[idx]);

                                        if (canAdd > 0.001) {
                                                // Add a random amount (up to canAdd)
                                                double toAdd = Math.random() * canAdd;
                                                attrs[idx] += toAdd;
                                                remaining -= toAdd;
                                        }
                                }

                                // If there's still remaining (couldn't distribute), spread evenly
                                if (remaining > 0.001) {
                                        for (int i = 0; i < 4 && remaining > 0.001; i++) {
                                                double canAdd = Math.min(remaining, maxs[i] - attrs[i]);
                                                attrs[i] += canAdd;
                                                remaining -= canAdd;
                                        }
                                }

                                // Final adjustment: force sum to exactly 20 by adjusting the first non-maxed attribute
                                double currentSum = attrs[0] + attrs[1] + attrs[2] + attrs[3];
                                double adjustment = total - currentSum;
                                if (Math.abs(adjustment) > 0.001) {
                                        // Find first attribute that can be adjusted
                                        for (int i = 0; i < 4; i++) {
                                                double newValue = attrs[i] + adjustment;
                                                if (newValue >= mins[i] && newValue <= maxs[i]) {
                                                        attrs[i] = newValue;
                                                        break;
                                                }
                                        }
                                }

                                return attrs;
                        } catch (NumberFormatException e) {
                                // Fall back to default random generation if parsing fails (0-20 range)
                                double min = 0.0;
                                double r1 = Math.random() * (total - 4 * min);
                                double r2 = Math.random() * (total - 4 * min - r1);
                                double r3 = Math.random() * (total - 4 * min - r1 - r2);
                                double r4 = total - 4 * min - r1 - r2 - r3;
                                return new double[] {min + r1, min + r2, min + r3, min + r4};
                        }
                }

                private boolean hasWinner() {
                        java.util.List<Robot> livingRobots = new java.util.ArrayList<>();
                        for (Robot robot : cellSpace.getRobots()) {
                                if (robot.isAlive()) {
                                        livingRobots.add(robot);
                                }
                        }

                        // Check if only one robot or one team remains
                        if (livingRobots.size() <= 1) {
                                return true;
                        }

                        // Check if only one team remains
                        java.util.Set<Integer> teams = new java.util.HashSet<>();
                        for (Robot robot : livingRobots) {
                                teams.add(robot.getTeam());
                        }

                        return teams.size() == 1;
                }

                private void recordChampion() {
                        // Find all winning team members and record their wins
                        java.util.List<Robot> winners = new java.util.ArrayList<>();

                        for (Robot robot : cellSpace.getRobots()) {
                                if (robot.isAlive()) {
                                        winners.add(robot);

                                        // Increment win count for all team members
                                        robot.incrementWinCount();

                                        // Update leaderboard for all team members
                                        String robotId = String.format("T%d-A%.1f-W%.1f-M%.1f-AB%.1f",
                                                robot.getTeam(), robot.getOriginalArmor(),
                                                robot.getOriginalWeaponPower(), robot.getOriginalManeuver(),
                                                robot.getOriginalAfterburner());

                                        if (leaderboard.containsKey(robotId)) {
                                                leaderboard.get(robotId).wins = robot.getWinCount();
                                        } else {
                                                leaderboard.put(robotId, new RobotStats(
                                                        robot.getTeam(), robot.getOriginalArmor(),
                                                        robot.getOriginalWeaponPower(), robot.getOriginalManeuver(),
                                                        robot.getOriginalAfterburner(), robot.getWinCount()));
                                        }
                                }
                        }

                        // Pick a random winner from the team to be the champion for the next round
                        if (!winners.isEmpty()) {
                                int randomIndex = (int)(Math.random() * winners.size());
                                championRobot = winners.get(randomIndex);

                                // Adaptive learning: narrow attribute ranges based on wins
                                updateAttributeRangesBasedOnWins(championRobot);
                        }
                }

                /**
                 * Adaptive learning algorithm: narrows attribute ranges based on champion's wins
                 * - 3+ wins: ±4 range
                 * - 5+ wins: ±3 range
                 * - 7+ wins: ±2 range
                 * - 10+ wins: ±1 range
                 * - 15+ wins: ±0.5 range
                 * - 20+ wins: Fixed (no range variation)
                 * NOTE: This modifies internal adaptiveRanges, NOT the UI fields
                 */
                private void updateAttributeRangesBasedOnWins(Robot champion) {
                        int winCount = champion.getWinCount();
                        int team = champion.getTeam();

                        // Determine range delta based on win count
                        double rangeDelta = 0;
                        if (winCount >= 20) {
                                rangeDelta = 0.0; // Fixed values - no variation
                        } else if (winCount >= 15) {
                                rangeDelta = 0.5;
                        } else if (winCount >= 10) {
                                rangeDelta = 1.0;
                        } else if (winCount >= 7) {
                                rangeDelta = 2.0;
                        } else if (winCount >= 5) {
                                rangeDelta = 3.0;
                        } else if (winCount >= 3) {
                                rangeDelta = 4.0;
                        } else {
                                return; // Don't narrow ranges for less than 3 wins
                        }

                        // Get champion's original attributes (the winning configuration)
                        double armor = champion.getOriginalArmor();
                        double weaponPower = champion.getOriginalWeaponPower();
                        double maneuver = champion.getOriginalManeuver();
                        double afterburner = champion.getOriginalAfterburner();

                        // Update INTERNAL adaptive ranges (NOT UI fields)
                        // Armor
                        adaptiveRanges[team][0] = Math.max(0, armor - rangeDelta);
                        adaptiveRanges[team][1] = Math.min(20, armor + rangeDelta);

                        // Weapon Power
                        adaptiveRanges[team][2] = Math.max(0, weaponPower - rangeDelta);
                        adaptiveRanges[team][3] = Math.min(20, weaponPower + rangeDelta);

                        // Maneuver
                        adaptiveRanges[team][4] = Math.max(0, maneuver - rangeDelta);
                        adaptiveRanges[team][5] = Math.min(20, maneuver + rangeDelta);

                        // Afterburner
                        adaptiveRanges[team][6] = Math.max(0, afterburner - rangeDelta);
                        adaptiveRanges[team][7] = Math.min(20, afterburner + rangeDelta);
                }

                @Override
                public void paint(Graphics g) {
                        super.paint(g);

                        g.setFont(new Font("Monospaced", Font.PLAIN, 8));

                        // Draw leaderboard below team range selection (20px spacing)
                        if (!leaderboard.isEmpty()) {
                                // Get top 5 robots by wins
                                java.util.List<RobotStats> topRobots = new java.util.ArrayList<>(leaderboard.values());
                                topRobots.sort(new java.util.Comparator<RobotStats>() {
                                        public int compare(RobotStats a, RobotStats b) {
                                                return Integer.compare(b.wins, a.wins); // Descending order
                                        }
                                });

                                // Position leaderboard below team ranges section
                                // Estimate: controls start at top, team ranges section ends around 280-300px
                                int leaderboardY = 345; // Below team range section with spacing (moved down 45px total)
                                g.setColor(Color.black);
                                g.drawLine(5, leaderboardY, getWidth() - 5, leaderboardY);

                                // Draw title
                                g.setFont(new Font("Monospaced", Font.BOLD, 9));
                                g.drawString("TOP 5 (ALL TIME)", 10, leaderboardY + 15);

                                // Draw top 5
                                g.setFont(new Font("Monospaced", Font.PLAIN, 8));
                                int leaderboardPos = leaderboardY + 30;
                                int rank = 1;
                                for (int i = 0; i < Math.min(5, topRobots.size()); i++) {
                                        RobotStats stats = topRobots.get(i);

                                        // Draw rank and jet icon
                                        Image leaderboardJetImage = null;
                                        if (stats.team == 0) {
                                                leaderboardJetImage = orangeJetImage;
                                        } else if (stats.team == 1) {
                                                leaderboardJetImage = blueJetImage;
                                        } else if (stats.team == 2) {
                                                leaderboardJetImage = greenJetImage;
                                        }

                                        g.setColor(Color.black);
                                        g.drawString("#" + rank, 10, leaderboardPos + 8);

                                        // Draw jet icon
                                        if (leaderboardJetImage != null) {
                                                g.drawImage(leaderboardJetImage, 30, leaderboardPos - 5, 15, 15, null);
                                        } else {
                                                // Fallback to colored box
                                                Color teamColor;
                                                if (stats.team == 0) {
                                                        teamColor = Color.orange;
                                                } else if (stats.team == 1) {
                                                        teamColor = Color.blue;
                                                } else if (stats.team == 2) {
                                                        teamColor = Color.green;
                                                } else {
                                                        teamColor = Color.white;
                                                }
                                                g.setColor(teamColor);
                                                g.fillRect(30, leaderboardPos - 5, 15, 15);
                                        }

                                        // Draw stats
                                        g.setColor(Color.black);
                                        String statInfo = String.format("Armor:%.1f Wpn:%.1f Mnvr:%.1f AfBr:%.0f Win:%d",
                                                stats.strength, stats.weaponPower, stats.speed, stats.afterburner, stats.wins);
                                        g.drawString(statInfo, 50, leaderboardPos + 8);

                                        leaderboardPos += 20;
                                        rank++;
                                }
                        }
                }
        }

        // Inner class for the current simulation panel (far right panel)
        private class CurrentSimulationPanel extends JPanel {
                private java.util.List<Robot> robots = new java.util.ArrayList<>();

                // Static jet images for display in simulation panel
                private static Image orangeJetImage = null;
                private static Image blueJetImage = null;
                private static Image greenJetImage = null;
                private static Image deadJetImage = null;

                // Load team-specific jet images once
                static {
                        try {
                                orangeJetImage = new ImageIcon("yellow jet.png").getImage(); // Using yellow jet for orange team
                                blueJetImage = new ImageIcon("blue jet.png").getImage();
                                greenJetImage = new ImageIcon("green jet.png").getImage();
                                deadJetImage = new ImageIcon("dead jet.png").getImage();
                        } catch (Exception e) {
                                System.err.println("Warning: Could not load jet images for simulation panel");
                        }
                }

                public CurrentSimulationPanel() {
                        setPreferredSize(new Dimension(220, 600)); // Right-most pane
                        setBackground(Color.white);
                }

                public void updateRobots(java.util.List<Robot> newRobots) {
                        this.robots = newRobots;
                }

                @Override
                public void paint(Graphics g) {
                        super.paint(g);

                        g.setFont(new Font("Monospaced", Font.PLAIN, 8));

                        // Draw header
                        g.setColor(Color.black);
                        g.setFont(new Font("Monospaced", Font.BOLD, 9));
                        g.drawString("CURRENT SIMULATION", 10, 20);

                        // Draw separator line
                        g.drawLine(5, 25, getWidth() - 5, 25);

                        // Reset font for robot stats
                        g.setFont(new Font("Monospaced", Font.PLAIN, 8));

                        int yPos = 40; // Start below header
                        for (Robot robot : robots) {
                                // Draw robot indicator (jet icon)
                                Image selectedJetImage = null;
                                if (!robot.isAlive()) {
                                        selectedJetImage = deadJetImage;
                                } else if (robot.getTeam() == 0) {
                                        selectedJetImage = orangeJetImage;
                                } else if (robot.getTeam() == 1) {
                                        selectedJetImage = blueJetImage;
                                } else if (robot.getTeam() == 2) {
                                        selectedJetImage = greenJetImage;
                                }

                                // Draw the jet icon
                                if (selectedJetImage != null) {
                                        g.drawImage(selectedJetImage, 5, yPos, 20, 20, null);
                                } else {
                                        // Fallback to colored box if image not available
                                        Color robotColor;
                                        if (!robot.isAlive()) {
                                                robotColor = Color.black;
                                        } else if (robot.getTeam() == 0) {
                                                robotColor = Color.orange;
                                        } else if (robot.getTeam() == 1) {
                                                robotColor = Color.blue;
                                        } else if (robot.getTeam() == 2) {
                                                robotColor = Color.green;
                                        } else {
                                                robotColor = Color.white;
                                        }
                                        g.setColor(robotColor);
                                        g.fillRect(5, yPos, 20, 20);
                                }

                                // Add red border for winners
                                if (robot.isWinner() || robot.isLastStanding()) {
                                        g.setColor(Color.red);
                                        g.drawRect(5, yPos, 20, 20);
                                        g.drawRect(4, yPos - 1, 22, 22); // Thicker border
                                }

                                // Draw robot stats
                                g.setColor(Color.black);
                                LinearRegressionModel model = new LinearRegressionModel();
                                double lr = model.calculateScore(robot.getArmor(), robot.getWeaponPower(),
                                                                 robot.getManeuver(), robot.getDamage());

                                // Draw original values
                                String originalInfo = String.format("Orig: H:%.0f A:%.1f W:%.1f M:%.1f AB:%.0f",
                                        robot.getOriginalHealth(), robot.getOriginalArmor(), robot.getOriginalWeaponPower(),
                                        robot.getOriginalManeuver(), robot.getOriginalAfterburner());
                                g.drawString(originalInfo, 30, yPos + 10);

                                // Draw current values (without Wins)
                                String currentInfo = String.format("Curr: H:%.0f A:%.1f W:%.1f M:%.1f AB:%.0f",
                                        robot.getHealth(), robot.getArmor(), robot.getWeaponPower(),
                                        robot.getManeuver(), robot.getAfterburner());
                                g.drawString(currentInfo, 30, yPos + 20);

                                yPos += 35;

                                // Reset if too far down
                                if (yPos > getHeight() - 25) {
                                        break;
                                }
                        }
                }
        }
}
