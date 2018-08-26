package com.mygdx.game.characters;

import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.controllers.InputStruct;

import static com.mygdx.game.controllers.InputStruct.ButtonsEnum;


/**
 * {@code PlayerInput} behavior .... allows player input to be treated as Steerable
 *
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * @author neiderm  (based on "com.badlogic.gdx.ai.steer.behaviors.Seek")
 */
public class PlayerInput<T extends Vector<T>> extends SteeringBehavior<T> {

    /**
     * the input we're tracking
     */
    private InputStruct io;

    /**
     * Creates a {@code PlayerInput} behavior for the specified owner and target.
     *
     * @param owner the owner of this behavior
     * @param io    ?.
     */
    PlayerInput(Steerable<T> owner, InputStruct io) {
        super(owner);
        this.io = io;
    }


    private Vector3 tmpV = new Vector3();

    /*
     * steering output is a 2d vector applied to the controller ...
     *     ctrlr.inputSet(touchPadCoords.set(t.getKnobPercentX(), -t.getKnobPercentY()), buttonStateFlags)
     *
     *     ... controller applies as a force vector aligned parallel w/ body Z axis,
     *     and then simple rotates the body 1deg/16mS about the Y axis if there is any left/right component to the touchpad.
     *
     *     Simply throw away Y component of steering output?
     */
    @Override
    protected SteeringAcceleration<T> calculateRealSteering(SteeringAcceleration<T> steering) {

        int jump = 0;

        switch (io.buttonPress) {
            case BUTTON_A:
                break;
            case BUTTON_B:
                break;
            case BUTTON_C:
//                applyJump();
                jump = 1;
                io.buttonPress = ButtonsEnum.BUTTON_NONE; // why need this now ... is it fixzed?  handle on touchUp()  doesn't fix it :(    have to "debounce" it!
                break;
            default:
                break;
        }

        tmpV.set(0f, jump, io.getLinearDirection());
        steering.linear.set((T) tmpV); // how to fix "Unchecked cast: 'com.badlogic.gdx.math.Vector3' to 'T'
        steering.angular = io.getAngularDirection();
        // Output steering acceleration
        return steering;
    }


    //
    // Setters overridden in order to fix the correct return type for chaining
    //

    @Override
    public PlayerInput<T> setOwner(Steerable<T> owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public PlayerInput<T> setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear acceleration.
     *
     * @return this behavior for chaining.
     */
    @Override
    public PlayerInput<T> setLimiter(Limiter limiter) {
        this.limiter = limiter;
        return this;
    }
}
