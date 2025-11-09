# BattleBots Linear Regression Decision-Making Capability

## Overview

This document describes the newly implemented linear regression-based decision-making capability for BattleBots. The system enables robots to intelligently decide whether to **attack** or **retreat** based on analyzing the attributes of the closest opposing battlebot.

## Architecture

### Components

1. **Robot.java** - Enhanced battlebot class with:
   - Combat attributes (strength, weapon power, speed, damage, health)
   - Team identification system
   - Position tracking
   - Decision-making logic
   - Opponent detection

2. **LinearRegressionModel.java** - Machine learning model that:
   - Calculates combat effectiveness scores
   - Uses weighted linear regression formula
   - Enables tunable decision-making weights

3. **BattleRobots.java** - Updated game engine with:
   - AI behavior execution in game loop
   - Robot position tracking
   - Team-based visual rendering

## How It Works

### 1. Robot Attributes

Each battlebot has the following attributes:

| Attribute | Description | Range | Impact |
|-----------|-------------|-------|--------|
| **Strength** | Defensive capability | 1.0 - 10.0 | Higher is better |
| **Weapon Power** | Offensive capability | 1.0 - 10.0 | Higher is better |
| **Speed** | Maneuverability | 1.0 - 10.0 | Higher is better |
| **Damage** | Accumulated damage | 0.0+ | Lower is better |
| **Health** | Remaining health | 0.0 - 100.0 | Higher is better |

Attributes are randomly initialized when a robot is created.

### 2. Team System

- Robots are randomly assigned to **Team 0 (Yellow)** or **Team 1 (Cyan)**
- Visual differentiation in the game UI
- Robots only engage opponents from the opposing team

### 3. Linear Regression Model

The decision-making uses a linear regression formula:

```
Score = (w1 × strength) + (w2 × weaponPower) + (w3 × speed) + (w4 × damage) + intercept
```

**Default Weights:**
- `w1` (Strength Weight): **2.5** - Defensive importance
- `w2` (Weapon Power Weight): **3.0** - Offensive importance (highest)
- `w3` (Speed Weight): **1.5** - Maneuverability importance
- `w4` (Damage Weight): **-2.0** - Damage penalty (negative)
- `intercept`: **10.0** - Baseline score

### 4. Decision-Making Process

Each game tick, every robot:

1. **Scans the battlefield** to find the closest opposing robot
2. **Calculates its own score** using the linear regression model
3. **Calculates the opponent's score** using the same model
4. **Compares scores**:
   - If `myScore > opponentScore` → **ATTACK** (red indicator)
   - If `myScore < opponentScore` → **RETREAT** (green indicator)
   - If no opponent found → **IDLE** (no indicator)

### 5. Visual Indicators

Each robot displays a small colored square in the top-left corner:
- **Red square** = ATTACK decision
- **Green square** = RETREAT decision
- **No square** = IDLE (no opponents detected)

## Code Examples

### Finding the Closest Opponent

```java
Robot opponent = robot.findClosestOpponent(cells, cellCols, cellRows);
```

This method:
- Iterates through all grid cells
- Identifies robots on the opposing team
- Calculates Euclidean distance
- Returns the closest enemy robot

### Making a Decision

```java
boolean shouldAttack = robot.makeDecision(opponent);
```

This method:
- Calculates both robots' combat scores
- Compares the scores
- Returns `true` for attack, `false` for retreat
- Updates the robot's visual state

### Calculating Combat Score

```java
double score = model.calculateScore(strength, weaponPower, speed, damage);
```

The linear regression model weighs each attribute and returns a combat effectiveness score.

## Integration with Game Loop

The game loop (`BattleRobots.java:346-359`) executes AI behavior for all robots each tick:

```java
public synchronized void next() {
    time++;

    // Execute AI behavior for all robots
    for (int x = 0; x < cellCols; x++) {
        for (int y = 0; y < cellRows; y++) {
            Robot robot = cells[x][y];
            if (robot != null && robot.isAlive()) {
                robot.executeBehavior(cells, cellCols, cellRows);
            }
        }
    }
}
```

## Customization

### Adjusting Model Weights

You can create a custom linear regression model with different weights:

```java
LinearRegressionModel customModel = new LinearRegressionModel(
    3.0,   // strengthWeight
    4.0,   // weaponPowerWeight
    2.0,   // speedWeight
    -1.5,  // damageWeight
    15.0   // intercept
);
```

### Extending the Model

The architecture supports future enhancements:
- **Machine learning training** - Learn optimal weights from battle data
- **Dynamic weight adjustment** - Adapt weights based on performance
- **Additional attributes** - Add armor, range, energy, etc.
- **Complex decision trees** - Multi-factor decision-making
- **Movement AI** - Implement actual attack/retreat movements

## Usage

1. **Compile the code:**
   ```bash
   javac *.java
   ```

2. **Run the applet** (in a browser or applet viewer)

3. **Place robots** by clicking on the grid
   - Yellow robots = Team 0
   - Cyan robots = Team 1

4. **Start the simulation** by clicking "Start"

5. **Observe decisions**:
   - Red corner = Robot is attacking
   - Green corner = Robot is retreating

## Performance Characteristics

- **Time Complexity**: O(n²) per tick, where n = number of robots
  - Each robot scans all cells to find closest opponent
  - For large numbers of robots, consider spatial indexing

- **Space Complexity**: O(1) per robot (constant attribute storage)

## Future Enhancements

Potential improvements to the system:

1. **Actual Combat** - Implement damage dealing and health reduction
2. **Movement System** - Robots move toward/away based on decisions
3. **Learning Algorithm** - Train weights using battle outcomes
4. **Formation Tactics** - Group coordination and team strategies
5. **Ranged Combat** - Distance-based attack effectiveness
6. **Resource Management** - Energy/ammunition systems
7. **Advanced ML Models** - Neural networks, decision trees, reinforcement learning

## Technical Notes

- **Thread Safety**: The `next()` method is synchronized to prevent race conditions
- **Null Safety**: All opponent checks include null validation
- **Floating Point**: Uses `double` precision for attribute calculations
- **Randomization**: Attributes are randomly initialized using `Math.random()`

## Files Modified/Created

| File | Status | Description |
|------|--------|-------------|
| `Robot.java` | Modified | Added attributes, AI logic, decision-making |
| `LinearRegressionModel.java` | Created | Linear regression implementation |
| `BattleRobots.java` | Modified | Integrated AI into game loop |
| `LINEAR_REGRESSION_CAPABILITY.md` | Created | This documentation file |

## References

- Original concept from README.md (lost assignment recreation)
- Linear regression: y = w₁x₁ + w₂x₂ + w₃x₃ + w₄x₄ + b
- Combat effectiveness scoring for game AI

---

**Implementation Date**: November 9, 2025
**Version**: 1.0
**Status**: Fully functional and tested
