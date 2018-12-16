package com.mygdx.game.characters;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector;


/**
 * {@code PlayerInput} behavior .... allows player input to be treated as Steerable
 *
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * @author neiderm  (based on "com.badlogic.gdx.ai.steer.behaviors.Seek")
 */
public class PlayerInput<T extends Vector<T>> extends SteeringBehavior<T> {

    private CtrlMapperIntrf mapper; // the input we're tracking

    /**
     * Creates a {@code PlayerInput} behavior for the specified owner and target.
     *
     * @param mapper    control mapper interface that updates the vehicle model
     */
    PlayerInput(
//            Steerable<T> owner,
            InputStruct mapper) {
//GN: idfk        super(owner);
        super(null);
        this.mapper = mapper;
    }


    @Override
    protected SteeringAcceleration<T> calculateRealSteering(SteeringAcceleration<T> steering) {

        mapper.update(0);

        return null;
    }
}
