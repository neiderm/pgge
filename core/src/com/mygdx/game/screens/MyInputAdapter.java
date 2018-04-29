package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
     *
     *
     *   Each inputAdapter can be a specific input fucntionality ... e.g. this one is to be
     *   "Touch aiming viewport action" .... maybe it could be generalized for touch presses on
     *   game screen in general.
     */
class MyInputAdapter extends InputAdapter {
//class MyInputAdapter extends Stage {

//    public static final int TOUCH_BOX_W = Gdx.graphics.getWidth() / 4;
//    public static final int TOUCH_BOX_H = TOUCH_BOX_W; // Gdx.graphics.getHeight() / 4;
    private static final int GAME_BOX_W = Gdx.graphics.getWidth();
    private static final int GAME_BOX_H = Gdx.graphics.getHeight();

    // TODO: multiple input receiver systems
    private InputReceiverSystem registeredSystem;

    private int touchDownCt = 0;
    private int touchUpCt = 0;
    private boolean isTouchInPad = false;

    // create a location rectangle for touch ara (in terms of screen coordinates!)
    private Rectangle touchBoxRect =
            new Rectangle(0, GAME_BOX_H / 4.0f, GAME_BOX_W, (GAME_BOX_H / 4) * 3);

    private Vector2 xy = new Vector2();


    public void registerSystem(InputReceiverSystem system){
        registeredSystem = system;
    }


    Vector2 tmpV2 = new Vector2();


    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        if (touchBoxRect.contains(screenX, screenY)) {

            Gdx.app.log(this.getClass().getName(),
                    String.format("touchDown x = %d y = %d", screenX, screenY));
//            Ray ray = cam.getPickRay(screenX, screenY);
            //GameObject.applyPickRay(ray); // objects register themselves with Gameobject:objectsArray at creation

            if (null != registeredSystem)
                registeredSystem.onTouchUp(xy.set(screenX, screenY));

            return true;
        }
        return false;
    }
}
