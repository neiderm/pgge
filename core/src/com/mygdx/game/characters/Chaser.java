package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.controllers.SteeringEntity;
import com.mygdx.game.controllers.TrackerSB;
import com.mygdx.game.util.PrimitivesBuilder;


/*
    A character object that tracks the given "node" ... a position (transform) plus offset for the
    PID control to track. So if it is handed the the world transform of the camera, it makes for a
    crude camera controller. "Plant" output is simply an offset displacement added to present position
    The sphere is just eye candy.

    This is really nothing more than a trivial example. Once there is a proper "non-bullet" Steerable available
    then this can be the Character for the cameraman.
*/

public class Chaser {

    public Entity create(Matrix4 tgtTransform) {

        float r = 0.5f;
        Entity e = PrimitivesBuilder.loadSphere(r, new Vector3(0, 15f, -5f));

        ModelInstance instance = e.getComponent(ModelComponent.class).modelInst;

        SteeringEntity character = new SteeringEntity();

        character.setSteeringBehavior(new TrackerSB<Vector3>(character, tgtTransform, instance.transform, /*spOffs*/new Vector3(0, 1, 2)));

        e.add(new CharacterComponent(character));

        return e;
    }
}
