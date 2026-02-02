package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.usb.serial.SerialPort;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.LED;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp
public class REV_STARTER_BOT extends LinearOpMode {

  private DcMotor flywheel;
  private DcMotor coreHex;
  private DcMotor leftDrive;
  private CRServo servo;
  private DcMotor rightDrive;
  private DigitalChannel redLED;
   private ElapsedTime autoLaunchTimer = new ElapsedTime();
   private boolean sequenceRunning = false;
private boolean reversing = false;
  //private DigitalChannel greenLED;

    // Setting our velocity targets. These values are in ticks per second!
  private static final int bankVelocity = 1300;
  private static final int farVelocity = 1920;
  private static final int maxVelocity = 2200;
  private boolean b= true;
  

  @Override
  public void runOpMode() {
    flywheel = hardwareMap.get(DcMotor.class, "flywheel");
    coreHex = hardwareMap.get(DcMotor.class, "coreHex");
    leftDrive = hardwareMap.get(DcMotor.class, "leftDrive");
    servo = hardwareMap.get(CRServo.class, "servo");
    rightDrive = hardwareMap.get(DcMotor.class, "rightDrive");
    redLED = hardwareMap.get(DigitalChannel.class, "red");
    //greenLED = hardwareMap.get(DigitalChannel.class, "green");

    // Establishing the direction and mode for the motors
    flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    flywheel.setDirection(DcMotor.Direction.REVERSE);
    coreHex.setDirection(DcMotor.Direction.REVERSE);
    leftDrive.setDirection(DcMotor.Direction.REVERSE);
    redLED.setMode(DigitalChannel.Mode.OUTPUT);
    //greenLED.setMode(DigitalChannel.Mode.OUTPUT);
  
    //Ensures the servo is active and ready
    servo.setPower(0);
    
    //Sets LED Incdicators off
    redLED.setState(false);
    //greenLED.setState(false);
   
    waitForStart();
    if (opModeIsActive()) {
      while (opModeIsActive()) {
        // Calling our methods while the OpMode is running
        splitStickArcadeDrive();
        setFlywheelVelocity();
        manualCoreHexAndServoControl();
        telemetry.addData("Flywheel Velocity", ((DcMotorEx) flywheel).getVelocity());
        telemetry.addData("Flywheel Power", flywheel.getPower());
        telemetry.update();
        LEDindicatorRed();
        //LEDindicatorGreen();
      }
    }
    
    
  }

  /**
   * Controls for the drivetrain. The robot uses a split stick stlye arcade drive. 
   * Forward and back is on the left stick. Turning is on the right stick.
   */
  private void splitStickArcadeDrive() {
    float x;
    float y;

    x = gamepad1.right_stick_x;
    y = -gamepad1.left_stick_y;
    leftDrive.setPower(y - x);
    rightDrive.setPower(y + x);
  }
  
  /**
   * Manual control for the Core Hex powered feeder and the agitator servo in the hopper
   */
  private void manualCoreHexAndServoControl() {
    // Manual control for the Core Hex intake
    if (gamepad2.cross) {
      coreHex.setPower(1);
    } else if (gamepad2.triangle||gamepad1.triangle) {
      coreHex.setPower(-1);
    }
    // Manual control for the hopper's servo
    if (gamepad2.dpad_left) {
      servo.setPower(1);
    } else if (gamepad2.dpad_right) {
      servo.setPower(-1);
    }
 
  }
  
  
  /**
   * This if/else statement contains the controls for the flywheel, both manual and auto.
   * Circle and Square will spin up ONLY the flywheel to the target velocity set.
   * The bumpers will activate the flywheel, Core Hex feeder, and servo to cycle a series of balls.
   */
  private void setFlywheelVelocity() {
    if (gamepad2.options) {
      flywheel.setPower(-1);
    } else if (gamepad2.left_bumper) {
      farPowerAuto();
    } else if (gamepad2.right_bumper) {
      bankShotAuto();
    } else if (gamepad2.circle) {
      ((DcMotorEx) flywheel).setVelocity(bankVelocity);
    } else if (gamepad2.square) {
      ((DcMotorEx) flywheel).setVelocity(maxVelocity);
    } else {
      ((DcMotorEx) flywheel).setVelocity(0);
      coreHex.setPower(0);
      // The check below is in place to prevent stuttering with the servo. It checks if the servo is under manual control!
      if (!gamepad2.dpad_right && !gamepad2.dpad_left) {
        servo.setPower(0);
        
     
      }
    }
  }

  /**
   * The bank shot or near velocity is intended for launching balls touching or a few inches from the goal.
   * When running this function, the flywheel will spin up and the Core Hex will wait before balls can be fed.
   * The servo will spin until the bumper is released.
   */
private void bankShotAuto() {
  ((DcMotorEx) flywheel).setVelocity(bankVelocity);
   coreHex.setPower(-1);
    // START sequence on initial press
   /* if (gamepad2.right_bumper && !sequenceRunning && !reversing) {
        autoLaunchTimer.reset();
        sequenceRunning = true;
    }

    // MAIN SEQUENCE while bumper held
    if (sequenceRunning && gamepad2.right_bumper) {
        double t = autoLaunchTimer.milliseconds();

        if (t < 2000) {
            ((DcMotorEx) flywheel).setVelocity(bankVelocity);
        } else if (t < 3000) {
            ((DcMotorEx) flywheel).setVelocity(bankVelocity);
            servo.setPower(-.7);
        } else {
            ((DcMotorEx) flywheel).setVelocity(bankVelocity);
            coreHex.setPower(-.7);
        }
    }

    // BUTTON RELEASE → start reverse
    if (!gamepad2.right_bumper && sequenceRunning) {
        autoLaunchTimer.reset();
        sequenceRunning = false;
        reversing = true;
    }

    // REVERSE servo for 1s
    if (reversing) {
        if (autoLaunchTimer.milliseconds() < 1000) {
            servo.setPower(1);
        } else {
            servo.setPower(0);
            reversing = false;
        }

        coreHex.setPower(0);
        ((DcMotorEx) flywheel).setVelocity(0);
    }*/
}

  
  //max velocity light indicator
  
  private void LEDindicatorRed(){
    if (((DcMotorEx) flywheel).getVelocity() >= (bankVelocity - 200)){
      
    redLED.setState(false);
    //LED Turns on
      
    }else{
     redLED.setState(true);
    //LED Turns on
    }
  }
  
  /**private void LEDindicatorGreen(){
    if (((DcMotorEx) flywheel).getVelocity() >= (bankVelocity - 50)){
      
    greenLED.setState(false);
    //LED Turns on
      
    }else{
     greenLED.setState(true);
    //LED Turns on
    }
  }**/

  /**
   * The far power velocity is intended for launching balls a few feet from the goal. It may require adjusting the deflector.
   * When running this function, the flywheel will spin up and the Core Hex will wait before balls can be fed.
   * The servo will spin until the bumper is released.
   */
  private void farPowerAuto() {
    ((DcMotorEx) flywheel).setVelocity(farVelocity);
    //servo.setPower(-1);
    if (((DcMotorEx) flywheel).getVelocity() >= farVelocity - 100) {
      coreHex.setPower(-.7);
    } else {
      coreHex.setPower(0);
    }
  }

}
