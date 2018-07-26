package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.controllers.ICharacterControlManual;
import com.mygdx.game.controllers.InputStruct;
import com.mygdx.game.screens.IUserInterface;


/**
 * Created by utf1247 on 5/17/2018.
 * <p>
 * Character implements the intelligence "glue" between Game Character, a virtual input device (player
 * control input, or possibly from an AI) and a "Character Controller" (a poor term for the essence of
 * what is in and interacting with the game model).  Character can be attached to a Game Event Signal.
 * There will likely be multiple enemy actors integrated to an Enemy System and Enemy Component.
 */

public class PlayerCharacter implements IGameCharacter {

    private ICharacterControlManual ctrlr;


    public PlayerCharacter(ICharacterControlManual ctrl, IUserInterface stage) {

        this.ctrlr = ctrl;

// UI pixmaps etc. should eventually come from a user-selectable skin
        stage.addTouchPad(touchPadChangeListener);

        Pixmap button;

        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(25, 25, 25);
        stage.addButton(actionButtonListener, button,
                3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 9f);
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


    @Override
    public void update(Entity entity, float deltaTime, Object whatever) {
// nothing to see here
    }
}

