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
    public Robot(BattleRobots referee)
  {
    // Assignment2.14: Within the Robot constructor, also be sure to use the referee instance variable
    // to invoke the 'getTheG()' access method on the BattleRobots object, and then assign the returned
    // value to the g instance variable of the Robot class.
    //
    referee.getTheG();
  }

  // Assignment2.8: Therefore, in the Robot class, add a render() method that
  // accepts 4 int parameters an invoke g.fillRect(...) with these parameters.
  public void render(int inpX, int inpY, int inpWidth, int inpHeight)
  {
    // Assignment2.9a: Now we've got a problem.  Our Robot class is using g as a
    // reference to the Graphics object. That object reference resides in the
    // BattleRobots class, and we don't have access to it...yet. To resolve this
    // add an access method to BattleRobots (see BattleRobots...)
    g.fillRect(inpX, inpY, inpWidth, inpHeight);
  }

}