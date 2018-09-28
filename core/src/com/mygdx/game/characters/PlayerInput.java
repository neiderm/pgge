package com.mygdx.game.characters;

import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.util.ModelInstanceEx;

import static com.mygdx.game.characters.InputStruct.ButtonsEnum;


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
    private btRigidBody body; // decide if this belongs here ... ;)

    /**
     * Creates a {@code PlayerInput} behavior for the specified owner and target.
     *
     * @param owner the owner of this behavior
     * @param io    ?.
     */
    PlayerInput(Steerable<T> owner, InputStruct io, btRigidBody body) {
        super(owner);
        this.io = io;
        this.body = body;
    }


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

        boolean jump = false;

        switch (io.buttonPress) {
            case BUTTON_A:
                break;
            case BUTTON_B:
                break;
            case BUTTON_C:
                jump = true;
                io.buttonPress = ButtonsEnum.BUTTON_NONE; // why need this now ... is it fixzed?  handle on touchUp()  doesn't fix it :(    have to "debounce" it!
                break;
            default:
                break;
        }

        steering.angular = io.getAngularDirection();

        Vector3 steeringLinear = (Vector3) steering.linear;

        // Output steering acceleration
        steeringLinear.set(0, 0, io.getLinearDirection());

        if (null != body)
            ModelInstanceEx.rotateRad(steeringLinear, body.getOrientation());
//        else
//            body = null; // wtf  8/22 still gettin these :(

        if (jump)
            steeringLinear.y = 100f; // idfk

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
