package com.mygdx.game.actors;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PlayerComponent;
import com.mygdx.game.TankController;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;

import java.util.Random;


/**
 * Created by utf1247 on 5/17/2018.
 *
 * Not in the Scene2D sense
 * Actors are anything that implements an "InputReceiver" interface that control commands can be
 * directed at (not necessarily a physical contrllr, e.g. AIs).
 * We sort of pass the player entity in here and decompose it into components, which might seem
 * counter to an ECS. whatever, we are putting stuff in here that was junking up the player system.
 */

public class PlayerActor {

    //    private Engine engine;
    private BulletComponent bulletComp;
    private ModelComponent modelComp;
    private PlayerComponent playerComp;
    private BulletWorld world;

    private Signal<GameEvent> gameEventSignal; // signal queue of pickRaySystem


    private GameEvent event = new GameEvent(null, GameEvent.EventType.THAT, null) {

        private Vector3 tmpV = new Vector3();
        private Vector3 posV = new Vector3();
        private Matrix4 tmpM = new Matrix4();

        @Override
        public void callback(Entity picked) {
            // we have an object in sight so kil it, bump the score, whatever
            bulletComp.body.getWorldTransform(tmpM);
            tmpM.getTranslation(posV);

            RenderSystem.otherThings.add(
                    GfxUtil.lineTo(tmpM.getTranslation(posV),
                            picked.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmpV),
                            Color.LIME));
        }
    };


    public Matrix4 getModelTransform(){
        return modelComp.modelInst.transform;
    }


    public PlayerActor(Entity e, BulletWorld world,  Signal<GameEvent> gameEventSignal) {

        modelComp = e.getComponent(ModelComponent.class);
        bulletComp = e.getComponent(BulletComponent.class);
        playerComp = e.getComponent(PlayerComponent.class);

        this.world = world;
        this.gameEventSignal = gameEventSignal;
    }

// needs to implement an "InputReceiver" interface

    public final ChangeListener touchPadChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                /*          -1.0
                       -1.0   +   +1.0
                            + 1.0        */

            Touchpad t = (Touchpad) actor;
            playerComp.inpVect.x = t.getKnobPercentX();
            playerComp.inpVect.y = -t.getKnobPercentY();
        }
    };

    public final InputListener actionButtonListener = new InputListener() {

        private final Vector3 forceVect = new Vector3(); // allowed this to be seen for debug info
        private Vector3 tmpV = new Vector3();
        private Random rnd = new Random();

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            //Gdx.app.log("my app", "Pressed"); //** Usually used to start Game, etc. **//

            // random flip left or right
            if (rnd.nextFloat() > 0.5f)
                tmpV.set(0.1f, 0, 0);
            else
                tmpV.set(-0.1f, 0, 0);

            bulletComp.body.applyImpulse(forceVect.set(0, rnd.nextFloat() * 10.f + 40.0f, 0), tmpV);

            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            // empty
        }
    };

    public final InputListener buttonGSListener = new InputListener() {
        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            // empty
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            return false;
        }
    };


    private static Matrix4 tmpM = new Matrix4();
    private static Vector3 down = new Vector3();
    private static Quaternion rotation = new Quaternion();
    private Vector3 tmpV = new Vector3();

    public void update(float delta) {

// for dynamic object you should get world trans directly from rigid body!
        // assert null != bc
        // assert null != bc.body
        bulletComp.body.getWorldTransform(tmpM);
        tmpM.getTranslation(tmpV);

        if (tmpV.y < -19) {
            playerComp.died = true;
// should also switch cam back to 3rd person
        }

        ModelInstanceEx.rotateRad(down.set(0, -1, 0), bulletComp.body.getOrientation());
//            down.set(0, 0, -1).rotateRad(axis, bc.body.getOrientation().getAxisAngleRad(axis));

        // check for contact w/ surface, only apply force if in contact, not falling
        // 1 meters max from the origin seems to work pretty good
        if (world.rayTest(tmpV, down, 1.0f)) {
            TankController.update(bulletComp.body, bulletComp.mass, delta, playerComp.inpVect);
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

// if (debug){
        this.event.set(null, GameEvent.EventType.THAT, tmpM);
        gameEventSignal.dispatch(this.event);
//    }



        ModelInstance lineInstance = GfxUtil.line(modelComp.modelInst.transform.getTranslation(tmpV),
                ModelInstanceEx.rotateRad(down.set(0, -1, 0), modelComp.modelInst.transform.getRotation(rotation)),
                Color.RED);

        RenderSystem.otherThings.add(lineInstance);
    }

}
