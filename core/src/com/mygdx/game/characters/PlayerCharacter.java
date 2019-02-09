package com.mygdx.game.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.game.GameWorld;

/**
 * Created by neiderm on 5/17/2018.
 */

public class PlayerCharacter extends Stage /* extends stageWithController ?? */  {

    private static final int KEY_CODE_POV_UP = Input.Keys.DPAD_UP;
    private static final int KEY_CODE_POV_DOWN = Input.Keys.DPAD_DOWN;
    private static final int KEY_CODE_POV_LEFT = Input.Keys.DPAD_LEFT;
    private static final int KEY_CODE_POV_RIGHT = Input.Keys.DPAD_RIGHT;

    private ImageButton picButton;
    private ImageButton xButton;
    private Touchpad touchpad;
    private Skin touchpadSkin;
    private BitmapFont font;
    private Label fpsLabel;
    private Table onscreenMenuTbl = new Table();
    private Skin uiSkin = new Skin();
    private Texture gsTexture;
    private Texture btnTexture;
    private final int gsBTNwidth =  Gdx.graphics.getHeight() * 3 / 8;
    private final int gsBTNheight =  Gdx.graphics.getHeight() * 3 / 8;
    private final int gsBTNx = Gdx.graphics.getWidth() / 2 - gsBTNwidth /2;
    private final int gsBTNy = Gdx.graphics.getHeight() / 2;
    private Vector2 v2 = new Vector2();
    private float[] axes = new float[4];
    private InputStruct mapper;


    // caller passes references to input listeners to be mapped to appropriate "buttons" - some will be UI functions
    // handled in here, or subsystem controls e.g. dpad controls go to tank steering, function buttons go to
    // guided missile susbsystem, cannon button inputs go to cannon etc.
    // maybe do a controller abstraction?
    // https://gist.github.com/nhydock/dc0501f34f89686ddf34
    // http://kennycason.com/posts/2015-12-27-libgdx-controller.html

    /*
     TODO: Array<InputListener> buttonListeners should be something like "Array<InputMapping> buttonListeners"
      ... where "InputMapping"  should be array of Buttons-Inputs needed for the screen
      {
        CONTROL_ID   //   POV_UP, POV_DOWN, BTN_START, BTN_X, BTN_DELTA,
        InputListener listener
        Button button
      }
      if listener==null then we have already a default base listener
     */
    public PlayerCharacter(final InputStruct mapper /* , Array<InputListener> buttonListeners */) {
//this.getViewport().getCamera().update(); // GN: hmmm I can get the camera
        this.mapper = mapper;

        if (GameWorld.getInstance().getIsTouchScreen()) {

            addChangeListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                    Touchpad t = (Touchpad) actor;
                    axes[0] = t.getKnobPercentX();
                    axes[1] = t.getKnobPercentY() * ( -1 );     // negated
                    mapper.setAxis(-1, axes);
                }});
        }
        setupPlayerUI(mapper);
    }

/*    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {

        Gdx.app.log("", "screenX = " + screenX + " screenY = " + screenY);
        return true;
    }*/

    @Override
    public boolean keyDown(int keycode) {

        int axisIndex = -1; // idfk

        if (KEY_CODE_POV_LEFT == keycode) {
            axes[0] = -1;
        }
        if (KEY_CODE_POV_RIGHT == keycode) {
            axes[0] = +1;
        }
        if (KEY_CODE_POV_UP == keycode) {
            axes[1] = -1;
        }
        if (KEY_CODE_POV_DOWN == keycode) {
            axes[1] = +1;
        }
        mapper.setAxis(255, axes);

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {

        int axisIndex = -1; // idfk

        if (KEY_CODE_POV_LEFT == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_RIGHT) ||
                KEY_CODE_POV_RIGHT == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_LEFT)) {
            axes[0] = 0;
            axisIndex = 0;
        }
        if (KEY_CODE_POV_UP == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_DOWN) ||
                KEY_CODE_POV_DOWN == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_UP)) {
            axes[1] = 0;
            axisIndex = 1;
        }

        mapper.setAxis(axisIndex, axes);

        return false;
    }


    /**
     * Based on "http://www.bigerstaff.com/libgdx-touchpad-example"
     */
    private void addChangeListener(ChangeListener touchPadChangeListener) {

        Touchpad.TouchpadStyle touchpadStyle;

        //Create a touchpad skin
        touchpadSkin = new Skin();
        //Set background image
//        touchpadSkin.add("touchBackground", new Texture("data/touchBackground.png"));
        //Set knob image
        touchpadSkin.add("touchKnob", new Texture("data/touchKnob.png"));
        //Create TouchPad Style
        touchpadStyle = new Touchpad.TouchpadStyle();
        //Create Drawable's from TouchPad skin
//        Drawable touchBackground = touchpadSkin.getDrawable("touchBackground");

// https://stackoverflow.com/questions/27757944/libgdx-drawing-semi-transparent-circle-on-pixmap
        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap background = new Pixmap(200, 200, Pixmap.Format.RGBA8888);
        background.setColor(1, 1, 1, .2f);
        background.fillCircle(100, 100, 100);

        //Apply the Drawables to the TouchPad Style
//        touchpadStyle.background = touchBackground;
        touchpadStyle.background = new TextureRegionDrawable(new TextureRegion(new Texture(background)));
        touchpadStyle.knob = touchpadSkin.getDrawable("touchKnob");
        // touchpadStyle.knob = = new TextureRegionDrawable ....
                //Create new TouchPad with the created style
        touchpad = new Touchpad(10, touchpadStyle);
        //setBounds(x,y,width,height)
        touchpad.setBounds(15, 15, 200, 200);

        // touchpad.addListener ... https://gamedev.stackexchange.com/questions/127733/libgdx-how-to-handle-touchpad-input/127937#127937
        touchpad.addListener(touchPadChangeListener);
        this.addActor(touchpad);

        background.dispose();
    }


    private void setupPlayerUI(final InputStruct mapper){

        Pixmap pixmap;

        // Font files from ashley-superjumper
        font = new BitmapFont( Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(1.0f);

        fpsLabel = new Label("Whatever ... ", new Label.LabelStyle(font, Color.WHITE));
        addActor(fpsLabel);

        // On Screen menu: up/down control over buttons does not wrap
        Label onScreenMenuLabel = new Label("Paused", new Label.LabelStyle(font, Color.WHITE));
        onscreenMenuTbl.setFillParent(true);
        onscreenMenuTbl.setDebug(true);
        onscreenMenuTbl.add(onScreenMenuLabel).fillX().uniformX();

        //create a Labels showing the score and some credits
        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        uiSkin.add("white", new Texture(pixmap)); //https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java
        pixmap.dispose();

//        textStyle.font = font;
        uiSkin.add("default", new Label.LabelStyle(font, Color.WHITE));
        // Store the default libgdx font under the name "default".
        uiSkin.add("default", font);

        // Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = uiSkin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.down = uiSkin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.checked = uiSkin.newDrawable("white", Color.BLUE);
        textButtonStyle.over = uiSkin.newDrawable("white", Color.LIGHT_GRAY);
        textButtonStyle.font = uiSkin.getFont("default");
        uiSkin.add("default", textButtonStyle);

        // Create a button with the "default" TextButtonStyle. A 3rd parameter can be used to specify a name other than "default".
        TextButton textButton = new TextButton("Camera", uiSkin);
        onscreenMenuTbl.row().pad(10, 0, 10, 0);
        onscreenMenuTbl.add(textButton).fillX().uniformX();

        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mapper.setInputState(InputStruct.InputState.INP_CAMCTRL);
//  ?? GameWorld.getInstance().setIsPaused(false); // any of the on-screen menu button should un-pause if clicked
//                onscreenMenuTbl.setVisible(false);
            }
        });

        //resume
        //restart
        //quit

        onscreenMenuTbl.setVisible(false);
        addActor(onscreenMenuTbl);

        TextureRegion myTextureRegion;
        TextureRegionDrawable myTexRegionDrawable;
        Pixmap.setBlending(Pixmap.Blending.None);

        pixmap = new Pixmap(gsBTNwidth, gsBTNheight, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, .3f);
        pixmap.drawRectangle(0, 0, gsBTNwidth, gsBTNheight);
        gsTexture = new Texture(pixmap);
        myTextureRegion = new TextureRegion(gsTexture);
        myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);
        picButton = new ImageButton(myTexRegionDrawable);
        picButton.setPosition(gsBTNx, gsBTNy);
        picButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // alternatively ?  e.g. toScrnCoord.x = Gdx.input.getX() etc.
                Vector2 toScrnCoord = picButton.localToParentCoordinates(v2.set(x, y));
                mapper.setInputState(InputStruct.InputState.INP_SELECT, toScrnCoord.x, toScrnCoord.y);
                return false;
            }});
        addActor(picButton);
        pixmap.dispose();

        pixmap = new Pixmap(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 4, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, .3f);
        pixmap.drawRectangle(0, 0, Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 4);

        btnTexture = new Texture(pixmap);
        myTextureRegion = new TextureRegion(btnTexture);
        myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);
        xButton = new ImageButton(myTexRegionDrawable);
        xButton.setPosition(3f * Gdx.graphics.getWidth() / 4, 0);
        xButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                mapper.setInputState(InputStruct.InputState.INP_B2);
                return false;
            }});
        addActor(xButton);
        pixmap.dispose();
    }

    public boolean addImageButton (EventListener listener /* .... */) {

        return addListener(listener);
    }


    private void update(){

        fpsLabel.setVisible(true);

//        boolean paused = GameWorld.getInstance().getIsPaused();
        if (GameWorld.getInstance().getIsPaused()) {

            // update menu selection state by up/down and d-pad

            fpsLabel.setVisible(false);
            onscreenMenuTbl.setVisible(true);
            xButton.setVisible(false);
//            picButton.setVisible(false); // only way for touchscreen system to un-pause right now!

            if (null != touchpad) {
                touchpad.setVisible(false);
            }
        }
        else {
            onscreenMenuTbl.setVisible(false);
            xButton.setVisible(true);
//            picButton.setVisible(true); // only way for touchscreen system to un-pause right now!

            if (null != touchpad)
                touchpad.setVisible(true);
        }
    }

    @Override
    public void act (float delta) {

        super.act(delta);
        mapper.update(delta);

        update();
    }

    @Override
    public void dispose () {

        super.dispose();

        if (null != touchpadSkin)
            touchpadSkin.dispose();

        uiSkin.dispose();
        btnTexture.dispose();
        gsTexture.dispose();
        font.dispose();
    }
}
