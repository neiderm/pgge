package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.PlayerComponent;
import com.mygdx.game.TankController;
import com.mygdx.game.util.EventQueue;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.ModelInstanceEx;


/**
 * Created by mango on 1/23/18.
 */


/* idea for player camera placment (chaseer
"We have a main character, who has a main node (the actor), a sight node (the point the character is
supposed to be looking at), and a chase camera node (where we think the best chasing camera should
be placed). "
 */


public class PlayerSystem extends EntitySystem /* IteratingSystem */ implements EntityListener {

    //    private Engine engine;
    private PlayerComponent playerComp;
    private BulletComponent bc;
    private BulletWorld world;
    private EventQueue eventQueue;

    // working variables
    private static Matrix4 tmpM = new Matrix4();
    private static Vector3 posV = new Vector3();


    public PlayerSystem(BulletWorld world,  Signal<GameEvent> gameEventSignal ) {

//        super(Family.all(PlayerComponent.class).get());

        eventQueue = new EventQueue();
        gameEventSignal.add(eventQueue);

        this.world = world;
    }

    @Override
    public void addedToEngine(Engine engine) {

//        this.engine = engine;

        engine.addEntityListener(Family.all(PlayerComponent.class).get(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {

//super.removedFromEngine(engine);
        engine.removeEntityListener(this); // Ashley bug (doesn't remove listener when system removed?
    }

    private Vector3 down = new Vector3();


//    protected void processEntity (Entity entity, float deltaTime){};

    @Override
    public void update(float delta) {

        // super();

// for dynamic object you should get world trans directly from rigid body!
        // assert null != bc
        // assert null != bc.body
        bc.body.getWorldTransform(tmpM);
        tmpM.getTranslation(posV);

        if (posV.y < -19) {
            playerComp.died = true;
// should also switch cam back to 3rd person
        }

        ModelInstanceEx.rotateRad(down.set(0, -1, 0), bc.body.getOrientation());
//            down.set(0, 0, -1).rotateRad(axis, bc.body.getOrientation().getAxisAngleRad(axis));

        // check for contact w/ surface, only apply force if in contact, not falling
        // 1 meters max from the origin seems to work pretty good
        if (world.rayTest(posV, down, 1.0f)) {
            TankController.update(bc.body, bc.mass, delta, playerComp.inpVect);
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
        for (GameEvent event : eventQueue.getEvents()) {
            switch (event) {

                case THAT:
                    break;
                case THIS:
                    break;
                default:

            }
        }

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

        bc = null; // kind of a hack so we know shits not initialized or been de-initialized
    }
}
