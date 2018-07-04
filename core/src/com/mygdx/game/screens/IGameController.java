package com.mygdx.game.screens;

import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Created by utf1247 on 6/22/2018.
 */

public interface IGameController {

    /* for now, we're not flexible and stuck on this set of input listeners */
    public void create(ChangeListener touchPadChangeListener,
            InputListener buttonAListener, InputListener buttonBListener, InputListener buttonGSListener);
}
