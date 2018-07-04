package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.controllers.ICharacterControlManual;
import com.mygdx.game.controllers.InputStruct;
import com.mygdx.game.screens.IGameController;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.CameraOperator;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;

import static com.mygdx.game.util.GameEvent.EventType.RAY_DETECT;
import static com.mygdx.game.util.GameEvent.EventType.RAY_PICK;


/**
 * Created by utf1247 on 5/17/2018.
 *
 * Character implements the intelligence "glue" between Game Character, a virtual input device (player
 * control input, or possibly from an AI) and a "Character Controller" (a poor term for the essence of
 * what is in and interacting with the game model).  Character can be attached to a Game Event Signal.
 * There will likely be multiple enemy actors integrated to an Enemy System and Enemy Component.
 */

public class PlayerCharacter implements IGameCharacter {

    private CameraOperator cameraOperator;
    private ICharacterControlManual ctrlr;
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
                        break;
                }

            }
        };
    }


    public PlayerCharacter(ICharacterControlManual ctrl, IGameController stage,
                           CameraOperator cameraOperator, btRigidBody body, Signal<GameEvent> gameEventSignal) {

        // eventually, pass in a type enum for this?
        this.cameraOperator = cameraOperator;
        this.ctrlr = ctrl;
        this.body = body;
        this.gameEventSignal = gameEventSignal;

        /*
         GS listener needs some geometry info to process correctly.
         Extend InputListener?  to include some fields to describe button geometry, those fields
         would be set  by stage.create()
         */
        stage.create(touchPadChangeListener, actionButtonListener, buttonBListener, buttonGSListener);
    }

    /* need this persistent since we pass it every time but only update on change */
    private Vector2 touchPadCoords = new Vector2();
    private InputStruct io = new InputStruct();


    private final ChangeListener touchPadChangeListener = new ChangeListener() {

        @Override
        public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                /*          -1.0
                       -1.0   +   +1.0
                            + 1.0        */

            Touchpad t = (Touchpad) actor;

            ctrlr.inputSet(
                    io.set(touchPadCoords.set(t.getKnobPercentX(), -t.getKnobPercentY()),
                            InputStruct.ButtonsEnum.BUTTON_NONE));
        }
    };

    private final InputListener actionButtonListener = new InputListener() {

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            //Gdx.app.log("my app", "Pressed"); //** Usually used to start Game, etc. **//
            ctrlr.inputSet(io.set(touchPadCoords, InputStruct.ButtonsEnum.BUTTON_C));

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
    private final InputListener buttonGSListener = new InputListener() {

        private GameEvent gameEvent = createGameEvent(RAY_PICK);

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            // empty
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            // only do this if FPV mode (i.e. cam controller is not handling game window input)
            if (!cameraOperator.getIsController()) {

                // offset button x,y to screen x,y (button origin on bottom left) (should not have screen/UI geometry crap in here!)
                float nX = (Gdx.graphics.getWidth() / 2f) + (x - 75);
                float nY = (Gdx.graphics.getHeight() / 2f) - (y - 75) - 75;

                gameEvent.set(RAY_PICK, cameraOperator.cam.getPickRay(nX, nY), id++);
                gameEventSignal.dispatch(gameEvent);
                //Gdx.app.log(this.getClass().getName(), String.format("GS touchDown x = %f y = %f, id = %d", x, y, id));
            }
            return true;
        }
    };

    private final InputListener buttonBListener = new InputListener() {
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


    private Matrix4 tmpM = new Matrix4();
    private Vector3 tmpV = new Vector3();
    private int id = 0; // tmp : test that I can create another gameEvent.set from this module
    private GameEvent gameEvent = createGameEvent(RAY_DETECT);
    private Ray ray = new Ray();
    private Vector3 position = new Vector3();
    private Vector3 direction = new Vector3(0, 0, -1); // vehicle forward
    private Quaternion rotation = new Quaternion();

    public void update(float delta) {

        cameraOperator.update(delta);

// for dynamic object you should get world trans directly from rigid body!
        // assert null != bc
        // assert null != bc.body
        body.getWorldTransform(tmpM);
        tmpM.getTranslation(tmpV);

        if (tmpV.y < -19) {
            died = true;
// should also switch cam back to 3rd person
        }
// if (debug){
        ray.set(tmpM.getTranslation(position),
                ModelInstanceEx.rotateRad(direction.set(0, 0, -1), tmpM.getRotation(rotation))        );

        this.gameEvent.set(RAY_DETECT, ray, id++);
        gameEventSignal.dispatch(this.gameEvent);
//    }
    }
}
