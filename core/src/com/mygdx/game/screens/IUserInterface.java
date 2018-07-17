package com.mygdx.game.screens;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Created by utf1247 on 6/22/2018.
 */

public abstract class IUserInterface extends Stage {

    public void addTouchPad(ChangeListener touchPadChangeListener) {
    }
    public void addButton(InputListener buttonAListener, Pixmap button, float x, float y){
    }
}
