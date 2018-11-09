package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.controllers.SteeringEntity;
import com.mygdx.game.screens.IUserInterface;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.GfxUtil;

/**
 * Created by utf1247 on 5/17/2018.
 * <p>
 * Character implements the intelligence "glue" between Game Character, a virtual input device (player
 * control input, or possibly from an AI) and a "Character Controller" (a poor term for the essence of
 * what is in and interacting with the game model).  Character can be attached to a Game Event Signal.
 * There will likely be multiple enemy actors integrated to an Enemy System and Enemy Component.
 */

public class PlayerCharacter extends IUserInterface {

    public PlayerCharacter(final btRigidBody btRigidBodyPlayer, SteeringEntity steerable) {

        final PlayerInput<Vector3> playerInpSB = new PlayerInput<Vector3>(steerable, io, btRigidBodyPlayer);
        steerable.setSteeringBehavior(playerInpSB);

        this.gameEvent = new GameEvent() {

            private Vector3 tmpV = new Vector3();
            private Vector3 posV = new Vector3();
//            private Matrix4 transform = playerTransform;

            /*
            we have no way to invoke a callback to the picked component.
            Pickable component required to implment some kind of interface to provide a
            callback method e.g.
              pickedComp = picked.getComponent(PickRayComponent.class).pickInterface.picked( blah foo bar)
              if (null != pickedComp.pickedInterface)
                 pickInterface.picked( myEntityReference );
             */
            @Override
            public void callback(Entity picked, EventType eventType) {

                switch (eventType) {
                    case RAY_DETECT:
                        if (null != picked) {
                            // we have an object in sight so kil it, bump the score, whatever
                            RenderSystem.otherThings.add(
                                    GfxUtil.lineTo(
                                            btRigidBodyPlayer.getWorldTransform().getTranslation(posV),
//                                            transform.getTranslation(posV),
                                            picked.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmpV),
                                            Color.LIME));
                        }
                        break;
                    case RAY_PICK:
                    default:
                        break;
                }
            }
        };

// UI pixmaps etc. should eventually come from a user-selectable skin
        addChangeListener(touchPadChangeListener);

        Pixmap button;

        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(25, 25, 25);
        addInputListener(actionButtonListener, button,
                3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 9f);
        button.dispose();
    }


    /* need this persistent since we pass it every time but only update on change */
    private Vector3 inpVect = new Vector3(0f, 0f, 0f);
    private InputStruct io = new InputStruct();

    private final ChangeListener touchPadChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                /*          -1.0
                       -1.0   +   +1.0
                            + 1.0        */
            Touchpad t = (Touchpad) actor;

            final float DZ = 0.25f; // actual number is irrelevant if < deadzoneRadius of TouchPad

            // rotate by a constant rate according to stick left or stick right.
            float angularDirection = 0f;
            float linearDirection = 0f;

            float knobX = t.getKnobPercentX();
            float knobY = t.getKnobPercentY();

            if (knobX < -DZ) {
                angularDirection = 1f;
            } else if (knobX > DZ) {
                angularDirection = -1f;
            }

            if (knobY > DZ) {
                // reverse thrust & "steer" opposite direction !
                linearDirection = -1f;
            } else if (knobY < -DZ) {
                linearDirection = +1f;
                angularDirection *= -1f;
            }
            // else ... inside deadzone

            io.set(inpVect.set(angularDirection, 0f, linearDirection), InputStruct.ButtonsEnum.BUTTON_NONE);
        }
    };

    private final InputListener actionButtonListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            io.set(inpVect.set(0f, 0f, 0f), InputStruct.ButtonsEnum.BUTTON_C);
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) { /* empty */ }
    };

    @Override
    public boolean keyDown(int keycode) {
// this needs work ... then we can refactor
        super.keyDown(keycode);

        // rotate by a constant rate according to stick left or stick right.
        float angularDirection = 0f;
        float linearDirection = 0f;

        if (keycode == Input.Keys.A) {
            angularDirection = 1f;
        } else if (keycode == Input.Keys.D) {
            angularDirection = -1f;
        }

        if (keycode == Input.Keys.W) {
            // reverse thrust & "steer" opposite direction !
            linearDirection = -1f;
        } else if (keycode == Input.Keys.S) {
            linearDirection = +1f;
            angularDirection *= -1f;
        }
        // else ... inside deadzone

        io.set(inpVect.set(
                angularDirection, 0f, linearDirection), InputStruct.ButtonsEnum.BUTTON_NONE);

        return false;
    }
}
