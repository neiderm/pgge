package com.mygdx.game.characters;

import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.controllers.InputStruct;

import static com.mygdx.game.controllers.InputStruct.ButtonsEnum.BUTTON_A;

/** {@code Seek} behavior moves the owner towards the target position. Given a target, this behavior calculates the linear steering
 * acceleration which will direct the agent towards the target as fast as possible.
 *
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 *
 * @author neiderm  (based on "com.badlogic.gdx.ai.steer.behaviors.Seek")
 */
public class PlayerInput<T extends Vector<T>> extends SteeringBehavior<T> {

    /** The target to seek */
//    protected Location<T> target;
//    private InputStruct io;

    /** Creates a {@code Seek} behavior for the specified owner.
     * @param owner the owner of this behavior. */
    public PlayerInput (Steerable<T> owner) {
        this(owner, null);
    }

    /** Creates a {@code Seek} behavior for the specified owner and target.
     * @param owner the owner of this behavior
     * @param io  ?. */
    PlayerInput (Steerable<T> owner, InputStruct io) {
        super(owner);
//        this.io = io;
    }

    protected Vector2 inpVect = new Vector2(0, 0); // control input vector

    @Override
    protected SteeringAcceleration<T> calculateRealSteering (SteeringAcceleration<T> steering) {

if (false) { // this is just a pass-thru for now ;)

//tmp helpme        inpVect.set(io.inpVector);
    InputStruct.ButtonsEnum buttonPress = BUTTON_A; // help tmp!

    final float DZ = 0.25f; // actual number is irrelevant if < deadzoneRadius of TouchPad

    // rotate by a constant rate according to stick left or stick right.
    float angularDirection = 0f;
    float linearDirection = 0f;

    if (inpVect.x < -DZ) {
        angularDirection = 1f;
    } else if (inpVect.x > DZ) {
        angularDirection = -1f;
    }

    if (inpVect.y > DZ) {
        // reverse thrust & "steer" opposite direction !
        linearDirection = 1f;
        angularDirection *= -1f;
    } else if (inpVect.y < -DZ) {
        linearDirection = -1f;
    }
    // else ... inside deadzone

//        calcSteeringOutputPlayer(degrees, linearF);

    switch (buttonPress) {
        case BUTTON_A:
            break;
        case BUTTON_B:
            break;
        case BUTTON_C:
//                applyJump();
            break;
        default:
            break;
    }

    final float ANGULAR_GAIN = 5.0f; // tmp

    //             steeringBehavior.calculateSteering(steeringOutput);
    Vector3 linear = (Vector3) steering.linear;
//        steering.linear.set(0f, 0f, linearDirection); ... wtf?
    linear.set(0f, 0f, linearDirection);
    steering.angular = angularDirection * ANGULAR_GAIN;

} // end false

        // Output steering acceleration
        return steering;
    }


    /** Returns the target to seek. */
//    public Location<T> getTarget () {
//        return target;
//    }

    /** Sets the target to seek.
     * @return this behavior for chaining. */
    public PlayerInput<T> setTarget (Location<T> target) {
//        this.target = target;
        return this;
    }

    //
    // Setters overridden in order to fix the correct return type for chaining
    //

    @Override
    public PlayerInput<T> setOwner (Steerable<T> owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public PlayerInput<T> setEnabled (boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /** Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear acceleration.
     * @return this behavior for chaining. */
    @Override
    public PlayerInput<T> setLimiter (Limiter limiter) {
        this.limiter = limiter;
        return this;
    }

}
