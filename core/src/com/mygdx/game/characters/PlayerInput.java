package com.mygdx.game.characters;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector;
import com.mygdx.game.controllers.TankController;


/**
 * {@code PlayerInput} behavior .... allows player input to be treated as Steerable
 *
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * @author neiderm  (based on "com.badlogic.gdx.ai.steer.behaviors.Seek")
 */
public class PlayerInput<T extends Vector<T>> extends SteeringBehavior<T> {

    private InputStruct io; // the input we're tracking
    private TankController tc;

    /**
     * Creates a {@code PlayerInput} behavior for the specified owner and target.
     *
     * @param owner the owner of this behavior
     * @param io    ?.
     */
    PlayerInput(
//            Steerable<T> owner,
            InputStruct io, TankController tc) {
//GN: idfk        super(owner);
        super(null);
        this.io = io;
        this.tc = tc;
    }


    @Override
    protected SteeringAcceleration<T> calculateRealSteering(SteeringAcceleration<T> steering) {

        final int BUTTON_CODE_1 = 1; // to be assigned by UI configuration ;)

        boolean jump = false;

        switch (io.buttonPress) {
            case BUTTON_CODE_1:
                jump = true;
                io.buttonPress = 0; // have to "debounce" it!
                break;
            default:
                break;
        }

        tc.updateControls(jump, io.getLinearDirection(), io.getAngularDirection(), 0);

        return null;
    }
}
