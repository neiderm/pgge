package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

/**
 * Created by mango on 4/1/18.
 */

/*
 * extended objects ... a bullet object, optionally a "kinematic" body.
 * primitive object templates have an appropriately sized basic bullet shape bound to them automatically
 * by the overloaded create() method.
 * You would want to be able set a custom material color/texture on them.
 * if same shape instance to be used for multiple entities, then shape would need to be instanced only
 * once in the constructor so you would need to do it a different way/ ???? investigate?.
 * Bounding box only works on mesh and doesn't work if we are scaling the mesh :(
 */
public abstract class SizeableEntityBuilder extends BulletEntityBuilder {

    // use unit (i.e. 1.0f) for all dimensions - primitive objects will have scale applied by load()
    public static final float primUnit = 1.0f;
    public static final float primHE = 1f / 2f; // primitives half extent constant
    public static final float primCapsuleHt = 1.0f + 0.5f + 0.5f; // define capsule height ala bullet (HeightTotal = H + 1/2R + 1/2R)

    // needed for method override (make this class from an interface, derived GameObject?)
    // can't override declare static method in anonymous inner class
    public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
        return null;
    }

    public static Entity load(
            Model model, String nodeID, btCollisionShape shape, Vector3 size, float mass, Vector3 translation) {

        Entity e;

        if (0 != mass)
            e = load(model, nodeID, size, mass, translation, shape);
        else
            e = load(model, nodeID, shape, translation, size);

        return e;
    }
}
