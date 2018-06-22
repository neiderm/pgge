package com.mygdx.game.actors;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.CharacterController;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.CameraOperator;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.GfxUtil;

import java.util.Random;


/**
 * Created by utf1247 on 5/17/2018.
 *
 * Not in the Scene2D sense
 * Actors are anything that implements an "InputReceiver" interface that control commands can be
 * directed at (not necessarily a physical contrllr, e.g. AIs).
 */

public class PlayerActor {

    private CameraOperator cameraOperator ;
    private CharacterController ctrlr;
    private btRigidBody body;

    private Signal<GameEvent> gameEventSignal; // signal queue of pickRaySystem
    private Vector2 inpVect; // control input vector

    public boolean died = false;


    private GameEvent event = new GameEvent(null, GameEvent.EventType.THAT, null) {

        private Vector3 tmpV = new Vector3();
        private Vector3 posV = new Vector3();
        private Matrix4 tmpM = new Matrix4();

        @Override
        public void callback(Entity picked) {
            // we have an object in sight so kil it, bump the score, whatever
            body.getWorldTransform(tmpM);
            tmpM.getTranslation(posV);

            RenderSystem.otherThings.add(
                    GfxUtil.lineTo(tmpM.getTranslation(posV),
                            picked.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmpV),
                            Color.LIME));
        }
    };



    public PlayerActor(CameraOperator cameraOperator, CharacterController ctrlr, btRigidBody body,
                       Vector2 vector, // tmp
                       Signal<GameEvent> gameEventSignal) {

        this.cameraOperator = cameraOperator;
        this.ctrlr = ctrlr;
        this.body = body;
        this.gameEventSignal = gameEventSignal;

//        this.inpVect = ctrlr.inpVect;
this.inpVect = vector;
    }

// needs to implement an "InputReceiver" interface

    public final ChangeListener touchPadChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                /*          -1.0
                       -1.0   +   +1.0
                            + 1.0        */

            Touchpad t = (Touchpad) actor;
            inpVect.x = t.getKnobPercentX();
            inpVect.y = -t.getKnobPercentY();
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

            body.applyImpulse(forceVect.set(0, rnd.nextFloat() * 10.f + 40.0f, 0), tmpV);

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
    private Vector3 tmpV = new Vector3();

    public void update(float delta) {

// for dynamic object you should get world trans directly from rigid body!
        // assert null != bc
        // assert null != bc.body
        body.getWorldTransform(tmpM);
        tmpM.getTranslation(tmpV);

        if (tmpV.y < -19) {
            died = true;
// should also switch cam back to 3rd person
        }

        ctrlr.update(delta);

// if (debug){
        this.event.set(null, GameEvent.EventType.THAT, tmpM);
        gameEventSignal.dispatch(this.event);
//    }
    }
}
