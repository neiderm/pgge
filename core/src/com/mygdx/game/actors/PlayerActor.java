package com.mygdx.game.actors;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.controllers.TankController;
import com.mygdx.game.controllers.CharacterControlManual;
import com.mygdx.game.inputadapters.GameController;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.CameraOperator;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;

import java.util.Random;

import static com.mygdx.game.util.GameEvent.EventType.RAY_DETECT;
import static com.mygdx.game.util.GameEvent.EventType.RAY_PICK;


/**
 * Created by utf1247 on 5/17/2018.
 *
 * Not in the Scene2D sense
 * Actor implements "glue" between Game Characters, Game Screen, and Game Controller (which is not
 * necessarily a physical contrllr, e.g. AIs).  Furthermore, Actor is also attached to a Game Event Signal.
 * There will likely be multiple enemy actors integrated to an Enemy System and Enemy Component.
 * Player Actor is not presently tied to the Entity Engine and we do not have "Player Component".
 * If we supported multi-player we would perhaps need to have a Player System.
 */

public class PlayerActor implements GameCharacter {

    private CameraOperator cameraOperator ;
    private CharacterControlManual ctrlr;
    private btRigidBody body;
    private Signal<GameEvent> gameEventSignal; // signal queue of pickRaySystem
    public boolean died = false;


    private GameEvent createGameEvent(GameEvent.EventType t) {

        return new GameEvent(t) {

            private Vector3 tmpV = new Vector3();
            private Vector3 posV = new Vector3();
            private Matrix4 tmpM = new Matrix4();

            @Override
            public void callback(Entity picked, EventType eventType) {

                body.getWorldTransform(tmpM);
                tmpM.getTranslation(posV);
                //assert (null != picked)
                switch (eventType) {
                    case RAY_DETECT:
                        // we have an object in sight so kil it, bump the score, whatever
                        RenderSystem.otherThings.add(
                                GfxUtil.lineTo(tmpM.getTranslation(posV),
                                        picked.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmpV),
                                        Color.LIME));
                        break;
                    case RAY_PICK:
                        ModelInstanceEx.setMaterialColor(picked.getComponent(ModelComponent.class).modelInst, Color.RED);
                        break;
                    default:
                        ;
                }

            }
        };
    }


    public PlayerActor(GameController stage,
            CameraOperator cameraOperator, btRigidBody body, Signal<GameEvent> gameEventSignal) {

        // eventually, pass in a type enum for this?
        TankController tank = new TankController(BulletWorld.getInstance(), body, /* bulletComp.mass */ 5.1f /* should be a property of the tank? */ );
        this.cameraOperator = cameraOperator;
        this.ctrlr = tank;
        this.body = body;
        this.gameEventSignal = gameEventSignal;

        stage.create(touchPadChangeListener, actionButtonListener, buttonBListener, buttonGSListener);
    }


    public final ChangeListener touchPadChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                /*          -1.0
                       -1.0   +   +1.0
                            + 1.0        */

            Touchpad t = (Touchpad) actor;
            ctrlr.inputSet(t.getKnobPercentX(), -t.getKnobPercentY());
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

            /*
            TODO: what the "action" button does exactly should be implemnented in the Character!
             */
            body.applyImpulse(forceVect.set(0, rnd.nextFloat() * 10.f + 40.0f, 0), tmpV);

            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            // empty
        }
    };

    /*
     "gun sight" will be draggable on the screen surface, then click to pick and/or shoot that direction
      */
    public final InputListener buttonGSListener = new InputListener() {

        private GameEvent gameEvent = createGameEvent(RAY_PICK);

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            // empty
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            // only do this if FPV mode (i.e. cam controller is not handling game window input)
            if (!cameraOperator.getIsController()) {
// tmp hack: offset button x,y to screen x,y (button origin on bottom left)
                float nX = (Gdx.graphics.getWidth() / 2f) + (x - 75);
                float nY = (Gdx.graphics.getHeight() / 2f) - (y - 75) - 75;

// we will be grabbing a pick ray from the cameera and then passing it to whatever gun-sight function is active, if any
                gameEvent.set(RAY_PICK, tmpM, id++);
                gameEventSignal.dispatch(gameEvent);
                //Gdx.app.log(this.getClass().getName(), String.format("GS touchDown x = %f y = %f, id = %d", x, y, id));
                }
            return true;
        }
    };

    public final InputListener buttonBListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

            // assert null != cameraOperator
            cameraOperator.nextOpMode();

            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            // empty
        }
    };


    private static Matrix4 tmpM = new Matrix4();
    private Vector3 tmpV = new Vector3();


    private int id = 0; // tmp : test that I can create another gameEvent.set from this module
    private GameEvent gameEvent = createGameEvent(RAY_DETECT);

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
        this.gameEvent.set(RAY_DETECT, tmpM, id++);
        gameEventSignal.dispatch(this.gameEvent);
//Gdx.app.log(this.getClass().getName(), String.format("update() ... id == %d", id));
        //    }
  }
}
