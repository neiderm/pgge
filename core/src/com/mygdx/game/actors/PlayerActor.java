package com.mygdx.game.actors;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PlayerComponent;
import com.mygdx.game.util.GfxUtil;

import java.util.Random;

import static com.mygdx.game.util.ModelInstanceEx.rotateRad;

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

    private PlayerComponent playerComp;
    private BulletComponent bulletComp;
    private ModelComponent modelComp;

    // working variables
    private static Vector3 tmpV = new Vector3();
    private static Random rnd = new Random();
    private static final Vector3 forceVect = new Vector3(); // allowed this to be seen for debug info


    public PlayerActor(Entity e) {

        modelComp = e.getComponent(ModelComponent.class);
        bulletComp = e.getComponent(BulletComponent.class);
        playerComp = e.getComponent(PlayerComponent.class);
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


    Vector3 position = new Vector3();
    Vector3 down = new Vector3();
    Quaternion rotation = new Quaternion();

    public void update(float delta) {

/*
        ModelInstance lineInstance = GfxUtil.line(modelComp.modelInst.transform.getTranslation(position),
                rotateRad(down.set(0, -1, 0), modelComp.modelInst.transform.getRotation(rotation)),
                Color.RED);
*/
    }

}
