package com.mygdx.game.actors;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.PlayerComponent;

import java.util.Random;

/**
 * Created by utf1247 on 5/17/2018.
 *
 * Actors are anything that implements an "InputReceiver" interface
 */

public class PlayerActor {

    private PlayerComponent playerComp;
    private BulletComponent bc;

    // working variables
    private static Vector3 tmpV = new Vector3();
    private static Random rnd = new Random();
    private static final Vector3 forceVect = new Vector3(); // allowed this to be seen for debug info

    public PlayerActor(BulletComponent bc, PlayerComponent pc){
        this.bc = bc;
        this.playerComp = pc;
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
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            //Gdx.app.log("my app", "Pressed"); //** Usually used to start Game, etc. **//

            // random flip left or right
            if (rnd.nextFloat() > 0.5f)
                tmpV.set(0.1f, 0, 0);
            else
                tmpV.set(-0.1f, 0, 0);

            bc.body.applyImpulse(forceVect.set(0, rnd.nextFloat() * 10.f + 40.0f, 0), tmpV);

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

}
