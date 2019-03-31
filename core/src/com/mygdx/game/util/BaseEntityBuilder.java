package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

/**
 * Created by neiderm on 4/1/18.
 */

public abstract class BaseEntityBuilder /* extends EntityBuilder */ {

//    @Override
    public Entity create(float mass, Vector3 trans, Vector3 size) {
        return null;
    }

    public btCollisionShape create(ModelInstance instance, float mass, Vector3 trans, Vector3 size) {
        return null;
    }
}
