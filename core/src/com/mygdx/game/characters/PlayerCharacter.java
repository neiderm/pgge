package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.controllers.ICharacterControlManual;
import com.mygdx.game.controllers.InputStruct;
import com.mygdx.game.screens.IUserInterface;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;

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

    private CameraMan cameraOperator;
    private ICharacterControlManual ctrlr;
    private Signal<GameEvent> gameEventSignal; // signal queue of pickRaySystem
    private Matrix4 transform;

    private GameEvent createGameEvent(GameEvent.EventType eventType) {

        return new GameEvent(eventType) {

            private Vector3 tmpV = new Vector3();
            private Vector3 posV = new Vector3();

            @Override
            public void callback(Entity picked, EventType eventType) {

                //assert (null != picked)
                switch (eventType) {
                    case RAY_DETECT:
                        // we have an object in sight so kil it, bump the score, whatever
                        RenderSystem.otherThings.add(
                                GfxUtil.lineTo(
                                        transform.getTranslation(posV),
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


    public PlayerCharacter(ICharacterControlManual ctrl, IUserInterface stage,
                           CameraMan cameraOperator, Signal<GameEvent> gameEventSignal, Matrix4 transform) {

        this.cameraOperator = cameraOperator;
        this.ctrlr = ctrl;
        this.transform = transform;
        this.gameEventSignal = gameEventSignal;

// UI pixmaps etc. should eventually come from a user-selectable skin
        stage.addTouchPad(touchPadChangeListener);

        Pixmap button;

        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(25, 25, 25);
        stage.addButton(actionButtonListener, button,
                3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 9f);


        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(150, 150, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(75, 75, 75);   /// I don't know how you would actually do a circular touchpad area like this
        stage.addButton(buttonGSListener, button,
                (Gdx.graphics.getWidth() / 2f) - 75, (Gdx.graphics.getHeight() / 2f) + 0);
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

        private int id = 0; // tmp : test that I can create another gameEvent.set from this module

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


    public void update(float deltaTime) {
// nothing to see here
    }
}
