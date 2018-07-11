package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Created by utf1247 on 7/11/2018.
 */

public class SetupUI extends IUserInterface {

    // copy ....
    //private static final int GAME_BOX_W = Gdx.graphics.getWidth();
    //private static final int GAME_BOX_H = Gdx.graphics.getHeight();

    @Override
    public void create(ChangeListener touchPadChangeListener, InputListener buttonAListener,
                       InputListener buttonBListener, InputListener buttonGSListener) {

        Texture myTexture;
        TextureRegion myTextureRegion;
        TextureRegionDrawable myTexRegionDrawable;

        Pixmap button = new Pixmap(150, 150, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillRectangle(0, 0, 150, 150);

        //       this.buttonGS = new ImageButton(touchBackground);
        myTexture = new Texture(button);
        myTextureRegion = new TextureRegion(myTexture);
        myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);

        ImageButton buttonGS = new ImageButton(myTexRegionDrawable);
        /* bottom left */
        buttonGS.setPosition((Gdx.graphics.getWidth() / 2f) - 75, (Gdx.graphics.getHeight() / 2f) + 0);
        buttonGS.addListener(buttonGSListener);

        this.clear();
//        this.addActor(touchpad);
//        this.addActor(buttonA); //Add the button to the stage to perform rendering and take input.
//        this.addActor(buttonB);
        this.addActor(buttonGS);
    }

    ///*
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {

            GameWorld.getInstance().showScreen(new MainMenuScreen());
        }
        return false;
    }
    //*/
}
