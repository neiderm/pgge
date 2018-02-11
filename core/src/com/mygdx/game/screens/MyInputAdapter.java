package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.InputReceiverSystem;


/**
 * Created by utf1247 on 2/6/2018.
 */

    /*
     * my multiplexed input adaptor
     * TODO:
     *   inputSystem.update(virtualPadX, virtualPadY, virtualButtonStates);
     *
     *   inputSystem:update() would handle (what would presumably) be one certain entity that should
     *   respond to inputs (note: input response not necessarily limited to the player, as maybe we
     *   would want to also drive inputs to e.g. guided missile ;)
     */
class MyInputAdapter extends InputAdapter {

    public static final int TOUCH_BOX_W = Gdx.graphics.getWidth() / 4;
    public static final int TOUCH_BOX_H = TOUCH_BOX_W; // Gdx.graphics.getHeight() / 4;

    // TODO: multiple input receiver systems
    private InputReceiverSystem registeredSystem;

    private int touchDownCt = 0;
    private int touchUpCt = 0;
    private boolean isTouchInPad = false;

    // create a location rectangle for touchbox (in terms of screen coordinates!)
    private Rectangle touchBoxRect = new Rectangle(
            Gdx.graphics.getWidth() / 2 - TOUCH_BOX_W / 2,
            Gdx.graphics.getHeight() - TOUCH_BOX_H,
            TOUCH_BOX_W, TOUCH_BOX_H);

    private Circle touchBoxCircle =
            new Circle(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() - TOUCH_BOX_H /2, 10);


    private Vector2 ctr = new Vector2();


    public void registerSystem(InputReceiverSystem system){
        registeredSystem = system;
    }


    Vector2 tmpV2 = new Vector2();

    private Vector2 setVector(int screenX, int screenY) {

        float normalize = (TOUCH_BOX_H / 2);
        touchBoxRect.getCenter(ctr);

        tmpV2.x = (screenX - ctr.x) / normalize;
        tmpV2.y = (screenY - ctr.y) / normalize;
        return tmpV2;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (touchBoxRect.contains(screenX, screenY)) {


            if (touchBoxCircle.contains(screenX, screenY)) {
                registeredSystem.onButton();
            }


            Gdx.app.log(this.getClass().getName(),
                    String.format("touchDown%d x = %d y = %d", touchDownCt++, screenX, screenY));

            isTouchInPad = true;
            registeredSystem.onTouchDown(setVector(screenX, screenY));

            return true;
        }
        else {
//            cameraSystem.isActive = false;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {

        if (touchBoxRect.contains(screenX, screenY)) {

//                Gdx.app.log(this.g0etClass().getName(), String.format("x = %d y = %d", screenX, screenY));
            isTouchInPad = true;

            registeredSystem.onTouchDragged(setVector(screenX, screenY));

            return true;

        } else if (isTouchInPad) {
            // still touching, but out of bounds, so escape it
//                isTouchInPad = false; // keep handling the touch, but no movement, and no transition to camera movement until touch is released
//                playerComp.vvv = new Vector3(0,0,0); // let motion continue while touch down?
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        Gdx.app.log(this.getClass().getName(),
                String.format("touch up %d x = %d y = %d", touchUpCt++, screenX, screenY));

        if (isTouchInPad) {
            isTouchInPad = false;
            registeredSystem.onTouchUp(new Vector2(0, 0));
            return true;
        }
        return false;
    }
}
