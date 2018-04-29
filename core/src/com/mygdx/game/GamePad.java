package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
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

    // copy ....
    private static final int GAME_BOX_W = Gdx.graphics.getWidth();
    private static final int GAME_BOX_H = Gdx.graphics.getHeight();

    private ImageButton buttonA;
    private ImageButton buttonB;
    private ImageButton buttonX;
    private ImageButton buttonY;
    private ImageButton buttonL;
    private ImageButton buttonR;
    private ImageButton buttonGS;
    Touchpad touchpad;

    public GamePad (
            ChangeListener touchPadChangeListener,
            InputListener buttonAListener,
            InputListener buttonBListener
    ,            InputListener buttonGSListener
    ) {

        createGamePad(touchPadChangeListener,
                buttonAListener,
                buttonBListener,
         buttonGSListener
        );
    }

    private void createGamePad(
            ChangeListener touchPadChangeListener,
            InputListener buttonAListener,
            InputListener buttonBListener,
            InputListener buttonGSListener
    ) {

        Touchpad.TouchpadStyle touchpadStyle;
        Skin touchpadSkin;
        Drawable touchBackground;
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

// https://stackoverflow.com/questions/27757944/libgdx-drawing-semi-transparent-circle-on-pixmap
        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap background = new Pixmap(200, 200, Pixmap.Format.RGBA8888);
        background.setColor(1, 1, 1, .2f);
        background.fillCircle(100, 100, 100);

        //Apply the Drawables to the TouchPad Style
//        touchpadStyle.background = touchBackground;
        touchpadStyle.background = new TextureRegionDrawable(new TextureRegion(new Texture(background)));

        touchpadStyle.knob = touchpadSkin.getDrawable("touchKnob");

        //Create new TouchPad with the created style
        this.touchpad = new Touchpad(10, touchpadStyle);
        //setBounds(x,y,width,height)
        touchpad.setBounds(15, 15, 200, 200);

        // touchpad.addListener ... https://gamedev.stackexchange.com/questions/127733/libgdx-how-to-handle-touchpad-input/127937#127937
        touchpad.addListener(touchPadChangeListener);



        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(25, 25, 25);


//        myTexture = new Texture(Gdx.files.internal("data/myTexture.png"));
        myTexture = new Texture(button);
        myTextureRegion = new TextureRegion(myTexture);
        myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);

        this.buttonA = new ImageButton(myTexRegionDrawable);
        buttonA.setPosition(3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 9f);
        buttonA.addListener(buttonAListener);

        this.buttonB = new ImageButton(myTexRegionDrawable);
        buttonB.setPosition((2 * Gdx.graphics.getWidth() / 4f) , (Gdx.graphics.getHeight() / 9f));
        buttonB.addListener(buttonBListener);

///*
//       this.buttonGS = new ImageButton(touchBackground);
       this.buttonGS = new ImageButton(myTexRegionDrawable);
        buttonGS.setPosition((Gdx.graphics.getWidth() / 2f) , (Gdx.graphics.getHeight() / 2f));
        buttonGS.addListener(buttonGSListener);
        //*/

        this.clear();
        this.addActor(touchpad);
        this.addActor(buttonA); //Add the button to the stage to perform rendering and take input.
        this.addActor(buttonB);
        this.addActor(buttonGS);
    }
}
