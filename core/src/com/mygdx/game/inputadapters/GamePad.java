package com.mygdx.game.inputadapters;

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
import com.mygdx.game.inputadapters.GameController;

/**
 * Created by utf1247 on 2/15/2018.
 *
 * Use the venerable Game Pad idiom as our user-input abstraction
 * Based on "http://www.bigerstaff.com/libgdx-touchpad-example"
 */

public class GamePad extends Stage implements GameController {

    // copy ....
    //private static final int GAME_BOX_W = Gdx.graphics.getWidth();
    //private static final int GAME_BOX_H = Gdx.graphics.getHeight();

    public void create(ChangeListener touchPadChangeListener, InputListener buttonAListener,
                               InputListener buttonBListener, InputListener buttonGSListener) {

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
        Touchpad touchpad = new Touchpad(10, touchpadStyle);
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

        ImageButton buttonA = new ImageButton(myTexRegionDrawable);
        buttonA.setPosition(3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 9f);
        buttonA.addListener(buttonAListener);

        ImageButton buttonB = new ImageButton(myTexRegionDrawable);
        buttonB.setPosition((2 * Gdx.graphics.getWidth() / 4f) , (Gdx.graphics.getHeight() / 9f));
        buttonB.addListener(buttonBListener);


        button = new Pixmap(150, 150, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(75, 75, 75);

        //       this.buttonGS = new ImageButton(touchBackground);
        myTexture = new Texture(button);
        myTextureRegion = new TextureRegion(myTexture);
        myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);

        ImageButton buttonGS = new ImageButton(myTexRegionDrawable);
        /* bottom left */
        buttonGS.setPosition((Gdx.graphics.getWidth() / 2f) - 75, (Gdx.graphics.getHeight() / 2f) + 0);
        buttonGS.addListener(buttonGSListener);

        this.clear();
        this.addActor(touchpad);
        this.addActor(buttonA); //Add the button to the stage to perform rendering and take input.
        this.addActor(buttonB);
        this.addActor(buttonGS);
    }
}
