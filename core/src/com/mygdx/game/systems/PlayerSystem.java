package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.PlayerComponent;
import com.mygdx.game.SliderForceControl;

/**
 * Created by mango on 1/23/18.
 */


/* idea for player camera placment (chaseer
"We have a main character, who has a main node (the actor), a sight node (the point the character is
supposed to be looking at), and a chase camera node (where we think the best chasing camera should
be placed). "
 */


public class PlayerSystem extends EntitySystem implements EntityListener {

    // magnitude of force applied (property of "vehicle" type?)
    private static /* final */ float forceMag = 12.0f;

    /* kinetic friction? ... ground/landscape is not dynamic and doesn't provide friction!
     * ultimately, somehow MU needs to be a property of the "surface" player is contact with and
     * passed as parameter to the friction computation .
     * Somehow, this seems to work well - the vehicle accelerates only to a point at which the
     * velocity seems to be limited and constant ... go look up the math eventually */
    private static final float MU = 0.5f;

    //    private Engine engine;
    private PlayerComponent playerComp;
    private BulletComponent bc;

    // working variables
    private static Matrix4 tmpM = new Matrix4();
    private static Vector3 tmpV = new Vector3();

    public /* private */ static final Vector3 forceVect = new Vector3(); // allowed this to be seen for debug info

    public PlayerSystem() {
      // empty
    }

    @Override
    public void addedToEngine(Engine engine) {

//        this.engine = engine;

        engine.addEntityListener(Family.all(PlayerComponent.class).get(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {

        engine.removeEntityListener(this); // Ashley bug (doesn't remove listener when system removed?
    }

    @Override
    public void update(float delta) {

        final float DZ = 0.25f; // actual number is irrelevant if < deadzoneRadius of TouchPad

        // rotate by a constant rate according to stick left or stick right.
        float degrees = 0;
        if (playerComp.inpVect.x < -DZ) {
            degrees = 1;
        } else if (playerComp.inpVect.x > DZ) {
            degrees = -1;
        }

        Quaternion r = bc.body.getOrientation();

        forceVect.set(0, 0, -1);
        float rad = r.getAxisAngleRad(axis);
        forceVect.rotateRad(axis, rad);


        if (playerComp.inpVect.y > DZ) {
            // reverse thrust & "steer" opposite direction !
            forceVect.scl(-1);
            degrees *= -1;
        } else if (!(playerComp.inpVect.y < -DZ)) {
            forceVect.set(0, 0, 0);
        }

// for dynamic object you should get world trans directly from rigid body!
        bc.body.getWorldTransform(tmpM);
        tmpM.getTranslation(tmpV);

        if (tmpV.y < -19) {
            playerComp.died = true;
        }

        // check for contact w/ surface, only apply force if in contact, not falling
        if (surfaceContact(
                bc.collisionWorld, playerComp,
                tmpV, bc.body.getOrientation())) {

            // we should maybe be using torque for this to be consistent in dealing with our rigid body player!
            tmpM.rotate(0, 1, 0, degrees); // does not touch translation ;)

            SliderForceControl.comp(delta, // eventually we should take time into account not assume 16mS?
                    bc.body, forceVect, forceMag, MU, bc.mass);
        }

/*
do same kind of raycst for tank ray-gun and optionally draw the ray to anything we "hit", of course we'll want to
notify the thing that was hit so it can chg. color etc.
But the BulletSystem.rayTest is particular to bullet bodies, whereas this will be purely "visual" check for any
entity objects that are enabled in the "ray-detection" system.
1) caster shines ray (insert my ray into the raySystem queue)
2) raySystem updates and processes the queue of castedRays (for each ray do ; for each registeredObject, etc. ...
3) ... invokes "callback" (interface) by which the ray caster can be notified
4) The caster uses other means to enact consequences of the rayhit (allowing rays to do different things, e.g. see vs. distroy!

not need to be asynchronous ...
 we need a raySystem (subscribed to appropriate entities) but it doesn't have to be an updated system.?
 */

        bc.body.setWorldTransform(tmpM);
    }


    /*
    make sure player is "upright" - this is so we don't apply motive force if e.g.
    rolled over falling etc. or otherwise not in contact with some kind of
    "tractionable" surface (would it belong in it's own system?)
     */
    private Ray ray = new Ray();
    private Vector3 axis = new Vector3();
//    private Vector3 down = new Vector3();

    private boolean surfaceContact(btCollisionWorld myCollisionWorld,
                                   PlayerComponent pc, Vector3 bodyTranslation, Quaternion bodyOrientation) {

        btCollisionObject rayPickObject;

        // get quat from world transfrom ... or not? seems equivalent to body.getOrientation()
//        bodyWorldTransform.getRotation(bodyOrientation);
// bodyOrientation = plyrPhysBody.getOrientation()

        Vector3 down = pc.down;
        down.set(0, -1, 0);
        float rad = bodyOrientation.getAxisAngleRad(axis);
        down.rotateRad(axis, rad);

        ray.set(bodyTranslation, down);
        // 1 meters max from the origin seems to work pretty good
        rayPickObject = BulletSystem.rayTest(myCollisionWorld, ray, 1f);

        return (null != rayPickObject);
    }


    @Override
    public void entityAdded(Entity entity) {

        // TODO: only allow one player ... assertion that these
        // state variables are not initialized (null)

//        if (null != entity.getComponent(PlayerComponent.class))
        {
            playerComp = entity.getComponent(PlayerComponent.class);
            bc = entity.getComponent(BulletComponent.class);
        }
    }

    @Override
    public void entityRemoved(Entity entity) {
      // emtpy
    }
}
