/*******************************************************************************
 * Title:        BattleRobots
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 ******************************************************************************/


// Assignment2.15: Now we need to add an import statement so we can get away with
// typing "Graphics" instead of the full class name of "java.awt.Graphics".
import java.awt.*;

public class Robot extends Object
{

  // Assignment2.9b: Now we've got a problem.  Our Robot class is using g as a
  // reference to the Graphics object. That object reference resides in the
  // BattleRobots class, and we don't have access to it...yet. To resolve this
  // add an access method to BattleRobots (see BattleRobots...)
  Graphics g;

  // Assignment2.13: Alas, we still have a problem... Having the right methods to call does you
  // no good if you don't have a reference to the object whose methods you want to call!
  // Here I'm referring to the BattleRobots object.  A Robot object currently has no reference
  // to the BattleRobots object.  We'd like to preclude the possibility that the Robot object might
  // be created without necessarily having a reference to the BattleRobots object that created it.
  // Therefore, let's define a Robot class constructor such that it requires as a parameter a
  // reference to the BattleRobots object.  Because we are defining a constructor, the compiler
  // will no longer automatically generate the default constructor.  Within the Robot constructor
  // be sure to save the reference value to an instance variable named 'referee'.  Be sure to
  // define the instance variable thus
  // 'BattleRobots referee'
  private BattleRobots referee;

  // Battlebot attributes for linear regression decision-making
  private double strength;
  private double weaponPower;
  private double speed;
  private double damage;
  private double health;

  // Position tracking
  private int gridX;
  private int gridY;

  // Team identification (0 = team A, 1 = team B)
  private int team;

  // Decision state
  private String currentDecision; // "ATTACK" or "RETREAT"

  // Linear regression model
  private static LinearRegressionModel decisionModel = new LinearRegressionModel();

  // Assignment2.13: Constructor that requires a reference to the BattleRobots object
  public Robot(BattleRobots referee)
  {
    // Assignment2.14: Within the Robot constructor, also be sure to use the referee instance variable
    // to invoke the 'getTheG()' access method on the BattleRobots object, and then assign the returned
    // value to the g instance variable of the Robot class.
    this.referee = referee;
    g = referee.getTheG();

    // Initialize robot attributes with random values
    initializeAttributes();

    // Randomly assign team (50/50 split)
    this.team = (Math.random() < 0.5) ? 0 : 1;

    this.currentDecision = "IDLE";
  }

  /**
   * Initialize robot attributes with random values
   */
  private void initializeAttributes()
  {
    // Randomize attributes between 1.0 and 10.0
    this.strength = 1.0 + Math.random() * 9.0;
    this.weaponPower = 1.0 + Math.random() * 9.0;
    this.speed = 1.0 + Math.random() * 9.0;
    this.damage = 0.0; // Initially no damage
    this.health = 100.0; // Full health
  }

  /**
   * Set the grid position of this robot
   */
  public void setPosition(int x, int y)
  {
    this.gridX = x;
    this.gridY = y;
  }

  /**
   * Get grid X position
   */
  public int getGridX()
  {
    return gridX;
  }

  /**
   * Get grid Y position
   */
  public int getGridY()
  {
    return gridY;
  }

  /**
   * Get team identifier
   */
  public int getTeam()
  {
    return team;
  }

  /**
   * Get robot attributes for regression calculation
   */
  public double getStrength()
  {
    return strength;
  }

  public double getWeaponPower()
  {
    return weaponPower;
  }

  public double getSpeed()
  {
    return speed;
  }

  public double getDamage()
  {
    return damage;
  }

  public double getHealth()
  {
    return health;
  }

  /**
   * Calculate the distance to another robot
   */
  public double distanceTo(Robot other)
  {
    int dx = this.gridX - other.gridX;
    int dy = this.gridY - other.gridY;
    return Math.sqrt(dx * dx + dy * dy);
  }

  /**
   * Find the closest opposing robot
   */
  public Robot findClosestOpponent(Robot[][] cells, int cellCols, int cellRows)
  {
    Robot closest = null;
    double minDistance = Double.MAX_VALUE;

    for (int x = 0; x < cellCols; x++)
    {
      for (int y = 0; y < cellRows; y++)
      {
        Robot other = cells[x][y];
        // Check if there's a robot and it's on the opposing team
        if (other != null && other != this && other.getTeam() != this.team)
        {
          double distance = this.distanceTo(other);
          if (distance < minDistance)
          {
            minDistance = distance;
            closest = other;
          }
        }
      }
    }

    return closest;
  }

  /**
   * Make a decision whether to attack or retreat based on linear regression model
   * Returns true if should attack, false if should retreat
   */
  public boolean makeDecision(Robot opponent)
  {
    if (opponent == null)
    {
      this.currentDecision = "IDLE";
      return false;
    }

    // Calculate regression scores for both robots
    double myScore = decisionModel.calculateScore(
      this.strength, this.weaponPower, this.speed, this.damage
    );

    double opponentScore = decisionModel.calculateScore(
      opponent.getStrength(), opponent.getWeaponPower(),
      opponent.getSpeed(), opponent.getDamage()
    );

    // Attack if our score is higher, retreat if opponent's score is higher
    boolean shouldAttack = myScore > opponentScore;
    this.currentDecision = shouldAttack ? "ATTACK" : "RETREAT";

    return shouldAttack;
  }

  /**
   * Get current decision state
   */
  public String getCurrentDecision()
  {
    return currentDecision;
  }

  /**
   * Execute the AI decision-making process for this robot
   */
  public void executeBehavior(Robot[][] cells, int cellCols, int cellRows)
  {
    // Find closest opponent
    Robot opponent = findClosestOpponent(cells, cellCols, cellRows);

    // Make decision based on linear regression model
    boolean shouldAttack = makeDecision(opponent);

    // The decision is stored in currentDecision for rendering/debugging
    // Actual attack/retreat behavior could be implemented here
    if (shouldAttack && opponent != null)
    {
      // Attack logic - could inflict damage, move closer, etc.
      // For now, we just record the decision
    }
    else if (opponent != null)
    {
      // Retreat logic - could move away, defend, etc.
      // For now, we just record the decision
    }
  }

  /**
   * Apply damage to this robot
   */
  public void takeDamage(double damageAmount)
  {
    this.damage += damageAmount;
    this.health = Math.max(0, this.health - damageAmount);
  }

  /**
   * Check if robot is alive
   */
  public boolean isAlive()
  {
    return health > 0;
  }

  // Assignment2.8: Therefore, in the Robot class, add a render() method that
  // accepts 4 int parameters an invoke g.fillRect(...) with these parameters.
  public void render(int inpX, int inpY, int inpWidth, int inpHeight)
  {
    // Assignment2.9a: Now we've got a problem.  Our Robot class is using g as a
    // reference to the Graphics object. That object reference resides in the
    // BattleRobots class, and we don't have access to it...yet. To resolve this
    // add an access method to BattleRobots (see BattleRobots...)

    // Color based on team
    if (team == 0)
    {
      g.setColor(Color.yellow);
    }
    else
    {
      g.setColor(Color.cyan);
    }

    g.fillRect(inpX, inpY, inpWidth, inpHeight);

    // Add visual indicator for decision state
    if ("ATTACK".equals(currentDecision))
    {
      g.setColor(Color.red);
      g.fillRect(inpX, inpY, inpWidth / 3, inpHeight / 3);
    }
    else if ("RETREAT".equals(currentDecision))
    {
      g.setColor(Color.green);
      g.fillRect(inpX, inpY, inpWidth / 3, inpHeight / 3);
    }
  }

}