package com.mygdx.game.characters;

import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.controllers.TankController;
import com.mygdx.game.util.ModelInstanceEx;


/**
 * {@code PlayerInput} behavior .... allows player input to be treated as Steerable
 *
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * @author neiderm  (based on "com.badlogic.gdx.ai.steer.behaviors.Seek")
 */
public class PlayerInput<T extends Vector<T>> extends SteeringBehavior<T> {

    private InputStruct io; // the input we're tracking
    private Matrix4 transform; // link to model instance transform
    private Quaternion q = new Quaternion();
    TankController tc;

    /**
     * Creates a {@code PlayerInput} behavior for the specified owner and target.
     *
     * @param owner the owner of this behavior
     * @param io    ?.
     */
    PlayerInput(Steerable<T> owner, InputStruct io, Matrix4 transform,
                TankController tc) {
//GN: idfk        super(owner);
        super(null);
        this.io = io;
        this.transform = transform;
this.tc = tc;
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

    private Vector3 steeringLinear = new Vector3();
    private float steeringAngular;
    SteeringAcceleration<Vector3> mySteering = new SteeringAcceleration<Vector3>(steeringLinear, steeringAngular);

    @Override
    protected SteeringAcceleration<T> calculateRealSteering(SteeringAcceleration<T> steering) {

        final int BUTTON_CODE_1 = 1; // to be assigned by UI configuration ;)

        boolean jump = false;

        switch (io.buttonPress) {
            case BUTTON_CODE_1:
                jump = true;
                io.buttonPress = 0; // why need this now ... is it fixzed?  handle on touchUp()  doesn't fix it :(    have to "debounce" it!
                break;
            default:
                break;
        }

        mySteering.angular = io.getAngularDirection();

// tmp!
        mySteering.linear = (Vector3)steering.linear;

        // Output steering acceleration
        // Determine resultant pushing force by rotating the linear direction vector (0, 0, 1 or 0, 0, -1) to
        // the vehicle orientation, Vechicle steering uses resultant X & Y components of steeringLinear to apply
        // a pushing force to the vehicle along tt's Z axes. This gets a desired effect of i.e. magnitude
        // of applied force reduces proportionately the more that the vehicle is on an incline
        mySteering.linear.set(0, 0, io.getLinearDirection());

        ModelInstanceEx.rotateRad(mySteering.linear, transform.getRotation( q ));

        if (jump)
            mySteering.linear.y = 100f; // idfk


//        tc.applySteering(mySteering, 0);

        // tmp
//        steering.angular = 0;
//        ((Vector3) steering.linear).set(0, 0, 0);

//        steering.linear.set(mySteering.linear);
        steering.angular = mySteering.angular;

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
