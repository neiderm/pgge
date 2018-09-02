package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.controllers.InputStruct;
import com.mygdx.game.controllers.SteeringBulletEntity;
import com.mygdx.game.screens.IUserInterface;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;

import static com.mygdx.game.util.GameEvent.EventType.RAY_DETECT;

/**
 * Created by utf1247 on 5/17/2018.
 * <p>
 * Character implements the intelligence "glue" between Game Character, a virtual input device (player
 * control input, or possibly from an AI) and a "Character Controller" (a poor term for the essence of
 * what is in and interacting with the game model).  Character can be attached to a Game Event Signal.
 * There will likely be multiple enemy actors integrated to an Enemy System and Enemy Component.
 */

public class PlayerCharacter implements IGameCharacter {

    private SteeringBulletEntity ctrlr;

    private Signal<GameEvent> gameEventSignal; // signal queue of pickRaySystem
    private GameEvent gameEvent;

    public PlayerCharacter(final Entity player, IUserInterface stage,
                           Signal<GameEvent> gameEventSignal, SteeringBulletEntity ctrl) {

        this.ctrlr = ctrl;

        final PlayerInput<Vector3> playerInpSB =
                new PlayerInput<Vector3>(ctrlr, io, player.getComponent(BulletComponent.class).body);
        ctrlr.setSteeringBehavior(playerInpSB);

        this.gameEventSignal = gameEventSignal; // local reference to item stored in component

//        player.add(new ControllerComponent(ctrl));

        this.gameEvent = new GameEvent() {

            private Vector3 tmpV = new Vector3();
            private Vector3 posV = new Vector3();
            private Matrix4 transform = player.getComponent(ModelComponent.class).modelInst.transform;
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
                                            transform.getTranslation(posV),
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

        // game Event stored in character comp but it probably doesn't need to be but lets be rigorous
        // and make sure it's valid anyway
//        comp.gameEvent = this.gameEvent
        CharacterComponent comp = new CharacterComponent(this, gameEvent);
        player.add(comp);


// UI pixmaps etc. should eventually come from a user-selectable skin
        stage.addChangeListener(touchPadChangeListener);

        Pixmap button;

        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(25, 25, 25);
        stage.addInputListener(actionButtonListener, button,
                3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 9f);
        button.dispose();
    }


    /* need this persistent since we pass it every time but only update on change */
    private Vector3 inpVect = new Vector3(0f, 0f, 0f);
    private InputStruct io =
            new InputStruct(inpVect, InputStruct.ButtonsEnum.BUTTON_NONE);

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

            io.set(inpVect.set(
                    angularDirection, 0f, linearDirection), InputStruct.ButtonsEnum.BUTTON_NONE);
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


    private Vector3 position = new Vector3();
    private Quaternion rotation = new Quaternion();
    private Vector3 direction = new Vector3(0, 0, -1); // vehicle forward

    @Override
    public void update(Entity entity, float deltaTime, Object whatever) {

        ctrlr.update(deltaTime);


        // different things have different means of setting their lookray
        Matrix4 transform = entity.getComponent(ModelComponent.class).modelInst.transform;
        transform.getTranslation(position);
        transform.getRotation(rotation);

        Ray lookRay = entity.getComponent(CharacterComponent.class).lookRay;
        lookRay.set(position, ModelInstanceEx.rotateRad(direction.set(0, 0, -1), rotation));
        gameEventSignal.dispatch(gameEvent.set(RAY_DETECT, lookRay, 0));
    }
}
