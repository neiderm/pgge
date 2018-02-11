package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by utf1247 on 2/6/2018.
 */

public interface  InputReceiverSystem {

    void onButton();

    void onTouchDown(Vector2 xy);

    void onTouchUp(Vector2 xy);

    void onTouchDragged(Vector2 xy);
}
