package com.mygdx.game.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Created by utf1247 on 7/11/2018.
 */

public class SetupUI extends IUserInterface {

    @Override
    public void addButton(InputListener listener, Pixmap pixmap, float x, float y) {

        Texture myTexture = new Texture(pixmap);
        TextureRegion myTextureRegion = new TextureRegion(myTexture);
        TextureRegionDrawable myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);

        ImageButton button = new ImageButton(myTexRegionDrawable);
        button.setPosition(x, y);
        button.addListener(listener);

        this.addActor(button);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {

            GameWorld.getInstance().showScreen(new MainMenuScreen());
        }
        return false;
    }
}
