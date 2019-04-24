package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

/**
 * Created by neiderm on 4/1/18.
 */

public abstract class BaseEntityBuilder /* extends EntityBuilder */ {

    public Entity create(float mass, Vector3 trans, Vector3 size) {
        return null;
    }

    public btCollisionShape getShape(Vector3 size) {
        return null;
    }
}
