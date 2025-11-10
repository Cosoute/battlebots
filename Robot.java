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
import javax.swing.ImageIcon;

public class Robot extends Object
{

  // Static jet images for each team
  private static Image yellowJetImage = null;
  private static Image blueJetImage = null;
  private static Image greenJetImage = null;
  private static Image deadJetImage = null;

  // Load team-specific jet images once
  static {
    try {
      yellowJetImage = new ImageIcon("yellow jet.png").getImage();
      blueJetImage = new ImageIcon("blue jet.png").getImage();
      greenJetImage = new ImageIcon("green jet.png").getImage();
      deadJetImage = new ImageIcon("dead jet.png").getImage();
    } catch (Exception e) {
      System.err.println("Warning: Could not load jet images");
      e.printStackTrace();
    }
  }

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
  private double armor;
  private double weaponPower;
  private double maneuver;
  private double damage;
  private double health;
  private double afterburner; // Afterburner attribute - allows 5x speed for 3 grid squares

  // Original starting attributes (for display purposes)
  private double originalArmor;
  private double originalWeaponPower;
  private double originalManeuver;
  private double originalAfterburner;
  private double originalHealth = 100.0;

  // Position tracking
  private int gridX;
  private int gridY;

  // Team identification (0 = team A, 1 = team B)
  private int team;

  // Decision state
  private String currentDecision; // "ATTACK" or "RETREAT"

  // Winner flag
  private boolean isWinner;

  // Last robot standing flag (for flashing effect)
  private boolean isLastStanding;

  // Flash state counter for flashing effect
  private int flashCounter;

  // Bounce direction after wall collision
  private int bounceDirectionX = 0;
  private int bounceDirectionY = 0;
  private int bounceCountdown = 0; // Number of turns to follow bounce direction

  // Current direction for image rotation (updated when robot moves)
  private int currentDirectionX = 1; // Default facing right
  private int currentDirectionY = 0;

  // Win counter for simulations
  private int winCount = 0;

  // Afterburner state tracking
  private boolean afterburnerActive = false;
  private int afterburnerMovesRemaining = 0;
  private String afterburnerStrategy = "Aggressive"; // Aggressive, Moderate, or Conservative

  // Conservative strategy tracking
  private int consecutiveMovesWithin1Cell = 0; // Track how many consecutive moves opponent is within 1 cell

  // Maneuverability tracking
  private int movesSinceDirectionChange = 0; // Track moves since last direction change

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

  // Constructor with specific team and custom stats
  public Robot(BattleRobots referee, int team, double armor, double weaponPower, double maneuver, double afterburner)
  {
    this.referee = referee;
    g = referee.getTheG();

    // Set specific attributes
    this.team = team;
    this.armor = armor;
    this.weaponPower = weaponPower;
    this.maneuver = maneuver;
    this.afterburner = afterburner;
    this.damage = 0.0;
    this.health = 100.0;

    // Store original values
    this.originalArmor = armor;
    this.originalWeaponPower = weaponPower;
    this.originalManeuver = maneuver;
    this.originalAfterburner = afterburner;
    this.originalHealth = 100.0;

    // Set random initial direction
    setRandomDirection();

    this.currentDecision = "IDLE";
  }

  /**
   * Initialize robot attributes with random values
   */
  private void initializeAttributes()
  {
    // Randomize attributes between 0.0 and 20.0
    this.armor = Math.random() * 20.0;
    this.weaponPower = Math.random() * 20.0;
    this.maneuver = Math.random() * 20.0;
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
  public double getArmor()
  {
    return armor;
  }

  public double getWeaponPower()
  {
    return weaponPower;
  }

  public double getManeuver()
  {
    return maneuver;
  }

  public double getDamage()
  {
    return damage;
  }

  public double getHealth()
  {
    return health;
  }

  public double getAfterburner()
  {
    return afterburner;
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
   * Find the closest opposing robot that is still alive
   * Teams always work together - never attack teammates
   */
  public Robot findClosestOpponent(Robot[][] cells, int cellCols, int cellRows)
  {
    Robot closest = null;
    double minDistance = Double.MAX_VALUE;

    // Find closest opponent from a different team
    for (int x = 0; x < cellCols; x++)
    {
      for (int y = 0; y < cellRows; y++)
      {
        Robot other = cells[x][y];
        if (other != null && other != this && other.isAlive())
        {
          // Always only attack other teams - never teammates
          boolean isValidTarget = (other.getTeam() != this.team);

          if (isValidTarget)
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
      this.armor, this.weaponPower, this.maneuver, this.damage
    );

    double opponentScore = decisionModel.calculateScore(
      opponent.getArmor(), opponent.getWeaponPower(),
      opponent.getManeuver(), opponent.getDamage()
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
   * Calculate random direction change: 90 degrees (left or right) or 180 degrees
   * Returns new direction as [dx, dy]
   */
  private int[] getRandomBounceDirection(int currentDx, int currentDy)
  {
    double random = Math.random();

    if (random < 0.33) {
      // 180 degrees - reverse direction
      return new int[] {-currentDx, -currentDy};
    } else if (random < 0.66) {
      // 90 degrees clockwise: (dx, dy) -> (dy, -dx)
      return new int[] {currentDy, -currentDx};
    } else {
      // 90 degrees counter-clockwise: (dx, dy) -> (-dy, dx)
      return new int[] {-currentDy, currentDx};
    }
  }

  /**
   * Check if robot can change direction based on maneuver value
   * Maneuver >= 10: Can change every move
   * Maneuver 5-9: Can change every move
   * Maneuver 3-4: Can change after 2 moves
   * Maneuver 0-2: Can change after 3 moves
   */
  private boolean canChangeDirection()
  {
    if (maneuver >= 5) {
      // High maneuverability - can change direction every move
      return true;
    } else if (maneuver >= 3) {
      // Medium maneuverability - can change after 2 moves
      return movesSinceDirectionChange >= 2;
    } else {
      // Low maneuverability - can change after 3 moves
      return movesSinceDirectionChange >= 3;
    }
  }

  /**
   * Get allowed turn angles based on maneuver value
   * Maneuver >= 10: 90, 180, or 270 degrees
   * Maneuver 5-9: 90 degrees only
   * Maneuver 3-4: 90 degrees only
   * Maneuver 0-2: 90 degrees only
   */
  private int[] calculateDirectionChange(int currentDx, int currentDy)
  {
    double random = Math.random();

    if (maneuver >= 10) {
      // Can turn 90, 180, or 270 degrees
      if (random < 0.25) {
        // 90 degrees clockwise
        return new int[] {currentDy, -currentDx};
      } else if (random < 0.5) {
        // 90 degrees counter-clockwise
        return new int[] {-currentDy, currentDx};
      } else if (random < 0.75) {
        // 180 degrees
        return new int[] {-currentDx, -currentDy};
      } else {
        // 270 degrees (equivalent to 90 counter to the other 90)
        return new int[] {currentDy, -currentDx};
      }
    } else {
      // Can only turn 90 degrees (left or right)
      if (random < 0.5) {
        // 90 degrees clockwise
        return new int[] {currentDy, -currentDx};
      } else {
        // 90 degrees counter-clockwise
        return new int[] {-currentDy, currentDx};
      }
    }
  }

  /**
   * Calculate the next position to move towards (for attack) or away from (for retreat)
   * Returns an array [newX, newY] or null if no valid move
   */
  public int[] calculateNextMove(Robot opponent, Robot[][] cells, int cellCols, int cellRows, boolean shouldAttack)
  {
    if (opponent == null && bounceCountdown == 0) {
      return null;
    }

    int dx = 0;
    int dy = 0;

    // If bouncing from wall, use bounce direction
    if (bounceCountdown > 0) {
      dx = bounceDirectionX;
      dy = bounceDirectionY;
      bounceCountdown--;
      movesSinceDirectionChange = 0; // Reset counter when bouncing
    } else {
      // Calculate desired direction to opponent
      int desiredDx = 0;
      int desiredDy = 0;

      if (opponent.gridX > this.gridX) desiredDx = 1;
      else if (opponent.gridX < this.gridX) desiredDx = -1;

      if (opponent.gridY > this.gridY) desiredDy = 1;
      else if (opponent.gridY < this.gridY) desiredDy = -1;

      // If retreating, reverse direction
      if (!shouldAttack) {
        desiredDx = -desiredDx;
        desiredDy = -desiredDy;
      }

      // Check if direction change is needed
      boolean needsDirectionChange = (desiredDx != currentDirectionX || desiredDy != currentDirectionY);

      if (needsDirectionChange && canChangeDirection()) {
        // Can change direction
        dx = desiredDx;
        dy = desiredDy;
        movesSinceDirectionChange = 0; // Reset counter
      } else if (needsDirectionChange && !canChangeDirection()) {
        // Cannot change direction yet, continue in current direction
        dx = currentDirectionX;
        dy = currentDirectionY;
        movesSinceDirectionChange++;
      } else {
        // No direction change needed
        dx = desiredDx;
        dy = desiredDy;
        movesSinceDirectionChange++;
      }
    }

    // Update current direction for rendering
    if (dx != 0 || dy != 0) {
      currentDirectionX = dx;
      currentDirectionY = dy;
    }

    // Try to move in the calculated direction
    int newX = this.gridX + dx;
    int newY = this.gridY + dy;

    // Check bounds - if hitting wall, take damage and bounce randomly (90 or 180 degrees)
    if (newX < 0 || newX >= cellCols || newY < 0 || newY >= cellRows) {
      // Wall collision: -2 health damage
      takeDamage(2);

      // Random direction change: 90 or 180 degrees
      int[] newDirection = getRandomBounceDirection(dx, dy);
      bounceDirectionX = newDirection[0];
      bounceDirectionY = newDirection[1];
      bounceCountdown = 5; // Follow bounce direction for 5 turns

      return null;
    }

    // Check if target cell is empty (dead robots also block movement to stay visible)
    if (cells[newX][newY] != null) {
      Robot blocker = cells[newX][newY];

      // If hitting a dead robot, bounce randomly (90 or 180 degrees) like hitting a wall
      if (!blocker.isAlive()) {
        takeDamage(2);
        int[] newDirection = getRandomBounceDirection(dx, dy);
        bounceDirectionX = newDirection[0];
        bounceDirectionY = newDirection[1];
        bounceCountdown = 5;
        return null;
      }

      // Cell is blocked by living robot, try alternate moves
      // Try moving only in X direction
      int altX = this.gridX + dx;
      if (dx != 0) {
        if (altX < 0 || altX >= cellCols) {
          // Wall collision on alternate X move - bounce
          takeDamage(2);
          bounceDirectionX = -dx;
          bounceDirectionY = -dy;
          bounceCountdown = 5;
        } else if (cells[altX][this.gridY] == null) {
          return new int[] {altX, this.gridY};
        }
      }
      // Try moving only in Y direction
      int altY = this.gridY + dy;
      if (dy != 0) {
        if (altY < 0 || altY >= cellRows) {
          // Wall collision on alternate Y move - bounce
          takeDamage(2);
          bounceDirectionX = -dx;
          bounceDirectionY = -dy;
          bounceCountdown = 5;
        } else if (cells[this.gridX][altY] == null) {
          return new int[] {this.gridX, altY};
        }
      }
      // No valid moves
      return null;
    }

    return new int[] {newX, newY};
  }

  /**
   * Check if this robot is adjacent to another robot
   */
  public boolean isAdjacentTo(Robot other)
  {
    if (other == null) return false;

    int dx = Math.abs(this.gridX - other.gridX);
    int dy = Math.abs(this.gridY - other.gridY);

    // Adjacent means within 1 cell (horizontally, vertically, or diagonally)
    return dx <= 1 && dy <= 1 && (dx + dy > 0);
  }

  /**
   * Attack another robot, reducing its armor and health based on weapon damage multiplier
   * Weapon damage multipliers:
   * - 10+: 1.5x damage
   * - 6-9: 1.3x damage
   * - 3-5: 1.2x damage
   * - 0-2: 1.0x damage
   */
  public void attackRobot(Robot target)
  {
    if (target != null && target.isAlive())
    {
      // Calculate weapon damage multiplier based on attacker's weapon power
      double weaponMultiplier;
      if (this.weaponPower >= 10) {
        weaponMultiplier = 1.5;
      } else if (this.weaponPower >= 6) {
        weaponMultiplier = 1.3;
      } else if (this.weaponPower >= 3) {
        weaponMultiplier = 1.2;
      } else {
        weaponMultiplier = 1.0;
      }

      // Reduce opponent's armor by 1
      target.armor = Math.max(0, target.armor - 1);

      // Base damage is 5, multiplied by weapon multiplier
      double baseDamage = 5.0 * weaponMultiplier;
      target.takeDamage(baseDamage);

      // If armor reaches 0, deal additional heavy damage to health
      if (target.armor <= 0)
      {
        double additionalDamage = 15.0 * weaponMultiplier;
        target.takeDamage(additionalDamage); // Additional damage when armor is 0
      }
    }
  }

  /**
   * Execute the AI decision-making process for this robot
   * Returns the new position [x, y] if robot should move, or null if staying put
   */
  public int[] executeBehavior(Robot[][] cells, int cellCols, int cellRows)
  {
    // Stop moving if health is too low (< 5)
    if (health < 5) {
      return null; // Too weak to move
    }

    // Find closest opponent
    Robot opponent = findClosestOpponent(cells, cellCols, cellRows);

    // Check if afterburner should trigger based on strategy
    if (opponent != null && afterburner > 0 && !afterburnerActive) {
      double distance = distanceTo(opponent);
      boolean opponentAttacking = opponent.makeDecision(this); // Check if opponent would attack us
      boolean shouldTrigger = false;

      if (opponentAttacking) {
        // Aggressive: trigger if attacker within 2 grid cells
        if (afterburnerStrategy.equals("Aggressive") && distance <= 2.0) {
          shouldTrigger = true;
        }
        // Moderate: trigger if attacker within 1 grid cell
        else if (afterburnerStrategy.equals("Moderate") && distance <= 1.0) {
          shouldTrigger = true;
        }
        // Conservative: trigger if attacker within 1 grid cell for 3+ consecutive moves
        else if (afterburnerStrategy.equals("Conservative")) {
          if (distance <= 1.0) {
            consecutiveMovesWithin1Cell++;
            if (consecutiveMovesWithin1Cell >= 3) {
              shouldTrigger = true;
              consecutiveMovesWithin1Cell = 0; // Reset counter after triggering
            }
          } else {
            consecutiveMovesWithin1Cell = 0; // Reset if opponent not within 1 cell
          }
        }
      } else {
        // Reset conservative counter if opponent not attacking
        consecutiveMovesWithin1Cell = 0;
      }

      if (shouldTrigger) {
        // Trigger afterburner
        afterburnerActive = true;
        afterburnerMovesRemaining = 3; // 3 grid squares
        afterburner -= 1; // Consume 1 afterburner charge
      }
    }

    // Make decision based on linear regression model
    boolean shouldAttack = makeDecision(opponent);

    // If adjacent to opponent and attacking, perform combat
    if (shouldAttack && opponent != null && isAdjacentTo(opponent))
    {
      attackRobot(opponent);
      return null; // Stay in place while attacking
    }

    // Calculate and return next move
    if (opponent != null)
    {
      return calculateNextMove(opponent, cells, cellCols, cellRows, shouldAttack);
    }

    return null; // No move
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

  /**
   * Regenerate health when staying still
   */
  public void regenerateHealth(double amount)
  {
    this.health = Math.min(100, this.health + amount);
  }

  /**
   * Apply movement cost to health
   */
  public void applyMovementCost()
  {
    takeDamage(3);
  }

  /**
   * Mark this robot as a winner
   */
  public void setWinner(boolean winner)
  {
    this.isWinner = winner;
  }

  /**
   * Check if this robot is a winner
   */
  public boolean isWinner()
  {
    return isWinner;
  }

  /**
   * Mark this robot as the last one standing
   */
  public void setLastStanding(boolean lastStanding)
  {
    this.isLastStanding = lastStanding;
  }

  /**
   * Check if this robot is the last one standing
   */
  public boolean isLastStanding()
  {
    return isLastStanding;
  }

  /**
   * Update flash counter for flashing effect
   */
  public void updateFlashCounter()
  {
    flashCounter++;
  }

  // Assignment2.8: Therefore, in the Robot class, add a render() method that
  // accepts 4 int parameters an invoke g.fillRect(...) with these parameters.
  public void render(Graphics g, int inpX, int inpY, int inpWidth, int inpHeight)
  {
    // Assignment2.9a: Now we've got a problem.  Our Robot class is using g as a
    // reference to the Graphics object. That object reference resides in the
    // BattleRobots class, and we don't have access to it...yet. To resolve this
    // add an access method to BattleRobots (see BattleRobots...)

    // Draw white background
    g.setColor(Color.white);
    g.fillRect(inpX, inpY, inpWidth, inpHeight);

    // Determine team color for jet tinting
    Color teamColor;
    if (!isAlive())
    {
      // Dead robots turn black
      teamColor = Color.black;
    }
    else if (isLastStanding)
    {
      // Last robot standing flashes between red and white
      // Flash every 2 frames (5x faster than before) - (flashCounter / 2) % 2 gives alternating 0 and 1
      if ((flashCounter / 2) % 2 == 0) {
        teamColor = Color.red;
      } else {
        teamColor = Color.white;
      }
    }
    else if (isWinner)
    {
      // Winners turn red (when multiple robots from same team win)
      teamColor = Color.red;
    }
    else if (team == 0)
    {
      teamColor = Color.yellow;
    }
    else if (team == 1)
    {
      teamColor = Color.cyan;
    }
    else if (team == 2)
    {
      teamColor = Color.green;
    }
    else
    {
      // Default to white for any other team
      teamColor = Color.white;
    }

    // Select appropriate jet image based on team and alive status
    Image selectedJetImage = null;
    if (!isAlive()) {
      selectedJetImage = deadJetImage;
    } else if (team == 0) {
      selectedJetImage = yellowJetImage;
    } else if (team == 1) {
      selectedJetImage = blueJetImage;
    } else if (team == 2) {
      selectedJetImage = greenJetImage;
    }

    // Draw team-specific jet image with rotation
    if (selectedJetImage != null) {
      Graphics2D g2d = (Graphics2D) g.create();

      // Calculate rotation angle based on current direction
      // Default direction (1, 0) = right = 0 degrees
      double angle = Math.atan2(currentDirectionY, currentDirectionX);

      // Calculate center of the robot cell
      int centerX = inpX + inpWidth / 2;
      int centerY = inpY + inpHeight / 2;

      // Apply rotation around the center
      g2d.rotate(angle, centerX, centerY);

      // Draw the team-specific jet image
      g2d.drawImage(selectedJetImage, inpX, inpY, inpWidth, inpHeight, null);

      g2d.dispose();
    }

    // Add visual indicator for decision state (only for alive robots)
    if (isAlive() && "ATTACK".equals(currentDecision))
    {
      g.setColor(Color.red);
      g.fillRect(inpX, inpY, inpWidth / 3, inpHeight / 3);
    }
    else if (isAlive() && "RETREAT".equals(currentDecision))
    {
      g.setColor(Color.green);
      g.fillRect(inpX, inpY, inpWidth / 3, inpHeight / 3);
    }

  }

  /**
   * Get flash counter for external rendering
   */
  public int getFlashCounter()
  {
    return flashCounter;
  }

  /**
   * Get win count
   */
  public int getWinCount()
  {
    return winCount;
  }

  /**
   * Set win count
   */
  public void setWinCount(int count)
  {
    this.winCount = count;
  }

  /**
   * Increment win count
   */
  public void incrementWinCount()
  {
    this.winCount++;
  }

  /**
   * Set afterburner strategy
   */
  public void setAfterburnerStrategy(String strategy)
  {
    this.afterburnerStrategy = strategy;
  }

  /**
   * Check if afterburner is active
   */
  public boolean isAfterburnerActive()
  {
    return afterburnerActive && afterburnerMovesRemaining > 0;
  }

  /**
   * Decrement afterburner moves and deactivate if done
   */
  public void consumeAfterburnerMove()
  {
    if (afterburnerMovesRemaining > 0) {
      afterburnerMovesRemaining--;
    }
    if (afterburnerMovesRemaining <= 0) {
      afterburnerActive = false;
    }
  }

  /**
   * Set a random initial direction for the robot
   */
  private void setRandomDirection()
  {
    // Randomly choose one of 8 directions (4 cardinal + 4 diagonal)
    int direction = (int)(Math.random() * 8);
    switch (direction) {
      case 0: // Right
        currentDirectionX = 1;
        currentDirectionY = 0;
        break;
      case 1: // Down
        currentDirectionX = 0;
        currentDirectionY = 1;
        break;
      case 2: // Left
        currentDirectionX = -1;
        currentDirectionY = 0;
        break;
      case 3: // Up
        currentDirectionX = 0;
        currentDirectionY = -1;
        break;
      case 4: // Down-Right
        currentDirectionX = 1;
        currentDirectionY = 1;
        break;
      case 5: // Down-Left
        currentDirectionX = -1;
        currentDirectionY = 1;
        break;
      case 6: // Up-Left
        currentDirectionX = -1;
        currentDirectionY = -1;
        break;
      case 7: // Up-Right
        currentDirectionX = 1;
        currentDirectionY = -1;
        break;
    }
  }

  /**
   * Get original starting attributes
   */
  public double getOriginalArmor()
  {
    return originalArmor;
  }

  public double getOriginalWeaponPower()
  {
    return originalWeaponPower;
  }

  public double getOriginalManeuver()
  {
    return originalManeuver;
  }

  public double getOriginalAfterburner()
  {
    return originalAfterburner;
  }

  public double getOriginalHealth()
  {
    return originalHealth;
  }

}