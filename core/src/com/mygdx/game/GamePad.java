package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Created by utf1247 on 2/15/2018.
 *
 * Use the venerable Game Pad idiom as our user-input abstraction
 * Based on "http://www.bigerstaff.com/libgdx-touchpad-example"
 */

public class GamePad extends Stage {

    private ImageButton buttonA;
    private ImageButton buttonB;
    private ImageButton buttonX;
    private ImageButton buttonY;
    private ImageButton buttonL;
    private ImageButton buttonR;
    Touchpad touchpad;

    public GamePad (
            ChangeListener touchPadChangeListener,
            InputListener buttonAListener,
            InputListener buttonBListener) {

        createGamePad(touchPadChangeListener,
                buttonAListener,
                buttonBListener);
    }

    private void createGamePad(
            ChangeListener touchPadChangeListener,
            InputListener buttonAListener,
            InputListener buttonBListener) {

        Touchpad.TouchpadStyle touchpadStyle;
        Skin touchpadSkin;
        Drawable touchBackground;
        Drawable touchKnob;
        Texture myTexture;
        TextureRegion myTextureRegion;
        TextureRegionDrawable myTexRegionDrawable;

        //Create a touchpad skin
        touchpadSkin = new Skin();
        //Set background image
        touchpadSkin.add("touchBackground", new Texture("data/touchBackground.png"));
        //Set knob image
        touchpadSkin.add("touchKnob", new Texture("data/touchKnob.png"));
        //Create TouchPad Style
        touchpadStyle = new Touchpad.TouchpadStyle();
        //Create Drawable's from TouchPad skin
        touchBackground = touchpadSkin.getDrawable("touchBackground");
        touchKnob = touchpadSkin.getDrawable("touchKnob");
        //Apply the Drawables to the TouchPad Style
        touchpadStyle.background = touchBackground;
        touchpadStyle.knob = touchKnob;

        //Create new TouchPad with the created style
        this.touchpad = new Touchpad(10, touchpadStyle);
        //setBounds(x,y,width,height)
        touchpad.setBounds(15, 15, 200, 200);
        // touchpad.addListener ... https://gamedev.stackexchange.com/questions/127733/libgdx-how-to-handle-touchpad-input/127937#127937
        touchpad.addListener(touchPadChangeListener);


        myTexture = new Texture(Gdx.files.internal("data/myTexture.png"));
        myTextureRegion = new TextureRegion(myTexture);
        myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);

        this.buttonA = new ImageButton(myTexRegionDrawable); //Set the buttonA up
        buttonA.setPosition(3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 6f);
        buttonA.addListener(buttonAListener);

        this.buttonB = new ImageButton(myTexRegionDrawable); //Set the buttonA up
        buttonB.setPosition((3 * Gdx.graphics.getWidth() / 4f) - 100f, (Gdx.graphics.getHeight() / 6f) - 100f);
        buttonB.addListener(buttonBListener);

        this.clear();
        this.addActor(touchpad);
        this.addActor(buttonA); //Add the button to the stage to perform rendering and take input.
        this.addActor(buttonB);
    }
}
