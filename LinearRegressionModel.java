/*******************************************************************************
 * Title:        BattleRobots - Linear Regression Model
 * Description:  Linear regression model for battlebot decision-making
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 ******************************************************************************/

/**
 * LinearRegressionModel implements a simple linear regression model
 * to calculate a combat effectiveness score based on robot attributes.
 *
 * The model uses the formula:
 * Score = w1*armor + w2*weaponPower + w3*maneuver + w4*damage + intercept
 *
 * where w1, w2, w3, w4 are weight coefficients learned or configured
 * for each attribute.
 */
public class LinearRegressionModel
{
  // Regression coefficients (weights) for each attribute
  private double armorWeight;
  private double weaponPowerWeight;
  private double maneuverWeight;
  private double damageWeight;
  private double intercept;

  /**
   * Constructor initializes the linear regression model with default weights.
   * These weights determine the importance of each attribute in the decision.
   *
   * Default weights are chosen to balance all attributes:
   * - Armor: 2.5 (high importance for defensive capability)
   * - Weapon Power: 3.0 (highest importance for offensive capability)
   * - Maneuver: 1.5 (medium importance for maneuverability)
   * - Damage: -2.0 (negative because more damage means weaker robot)
   * - Intercept: 10.0 (baseline score)
   */
  public LinearRegressionModel()
  {
    // Default regression weights - can be tuned based on training data
    this.armorWeight = 2.5;
    this.weaponPowerWeight = 3.0;
    this.maneuverWeight = 1.5;
    this.damageWeight = -2.0;  // Negative because damage is a penalty
    this.intercept = 10.0;
  }

  /**
   * Constructor with custom weights for advanced users
   *
   * @param armorWeight Weight for armor attribute
   * @param weaponPowerWeight Weight for weapon power attribute
   * @param maneuverWeight Weight for maneuver attribute
   * @param damageWeight Weight for damage attribute
   * @param intercept Intercept term (baseline score)
   */
  public LinearRegressionModel(double armorWeight, double weaponPowerWeight,
                                double maneuverWeight, double damageWeight,
                                double intercept)
  {
    this.armorWeight = armorWeight;
    this.weaponPowerWeight = weaponPowerWeight;
    this.maneuverWeight = maneuverWeight;
    this.damageWeight = damageWeight;
    this.intercept = intercept;
  }

  /**
   * Calculate the combat effectiveness score using linear regression
   *
   * @param armor Robot's armor attribute
   * @param weaponPower Robot's weapon power attribute
   * @param maneuver Robot's maneuver attribute
   * @param damage Robot's accumulated damage (penalty)
   * @return Calculated combat effectiveness score
   */
  public double calculateScore(double armor, double weaponPower,
                                double maneuver, double damage)
  {
    // Linear regression formula: y = w1*x1 + w2*x2 + w3*x3 + w4*x4 + intercept
    double score = intercept +
                   (armorWeight * armor) +
                   (weaponPowerWeight * weaponPower) +
                   (maneuverWeight * maneuver) +
                   (damageWeight * damage);

    return score;
  }

  /**
   * Predict whether a robot should attack based on comparing two scores
   *
   * @param myScore This robot's calculated score
   * @param opponentScore Opponent robot's calculated score
   * @return true if should attack (myScore > opponentScore), false otherwise
   */
  public boolean predictAttack(double myScore, double opponentScore)
  {
    return myScore > opponentScore;
  }

  /**
   * Get the current weights and intercept (for debugging/analysis)
   */
  public String getModelParameters()
  {
    return String.format(
      "Linear Regression Model Parameters:\n" +
      "  Armor Weight: %.2f\n" +
      "  Weapon Power Weight: %.2f\n" +
      "  Maneuver Weight: %.2f\n" +
      "  Damage Weight: %.2f\n" +
      "  Intercept: %.2f\n",
      armorWeight, weaponPowerWeight, maneuverWeight, damageWeight, intercept
    );
  }

  /**
   * Update model weights (allows for learning/tuning)
   */
  public void setWeights(double armorWeight, double weaponPowerWeight,
                         double maneuverWeight, double damageWeight, double intercept)
  {
    this.armorWeight = armorWeight;
    this.weaponPowerWeight = weaponPowerWeight;
    this.maneuverWeight = maneuverWeight;
    this.damageWeight = damageWeight;
    this.intercept = intercept;
  }

  // Getters for individual weights
  public double getArmorWeight()
  {
    return armorWeight;
  }

  public double getWeaponPowerWeight()
  {
    return weaponPowerWeight;
  }

  public double getManeuverWeight()
  {
    return maneuverWeight;
  }

  public double getDamageWeight()
  {
    return damageWeight;
  }

  public double getIntercept()
  {
    return intercept;
  }
}
