/*
 * Copyright (c) 2021 Glenn Neidermeier
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GameWorld;

/**
 * Created by neiderm on 12/18/17.
 * <p>
 * Reference:
 * on-screen menus:
 * https://www.gamedevelopment.blog/full-libgdx-game-tutorial-menu-control/
 * UI skin defined programmatically:
 * https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java
 */
class InGameMenu extends Stage {

    private final Array<Texture> savedTextureRefs = new Array<>();
    private final Array<String> buttonNames = new Array<>();
    private final ButtonGroup<TextButton> buttonGroup;
    // disposables
    private final Texture overlayTexture;
    private final Image overlayImage;
    private final BitmapFont menuFont;
    // disposable
    private Texture buttonTexture;

    private int previousIncrement;
    private int actorCount;

    final Table onscreenMenuTbl = new Table();
    final Table playerInfoTbl = new Table();
    final Skin uiSkin;

    InputMapper mapper;

    static final String DEFAULT_UISKIN_JSON = "skin/uiskin.json";

    InGameMenu() {
        this(DEFAULT_UISKIN_JSON);
    }

    /**
     * make constructor public to specify the skin name
     *
     * @param skinName String skin name
     */
    private InGameMenu(String skinName) {
        this(skinName, null);
    }

    InGameMenu(String skinName, String menuName) {

        super();

        mapper = new InputMapper();
        // if mapper gets changed to extend ControllerListenerAdapter then addListener ends up here
        // Controllers.addListener(mapper);

        savedTextureRefs.clear();

        if (null != skinName) {
            final String DEFAULT_FONT = "default-font";
            Skin skin = new Skin(Gdx.files.internal(skinName));
            menuFont = skin.getFont(DEFAULT_FONT);
            uiSkin = makeSkin(new Skin(Gdx.files.internal(skinName)));

        } else {
            // screens that are not loading UI from a skin must load the font directly
            menuFont = new BitmapFont(Gdx.files.internal(GameWorld.DEFAULT_FONT_FNT),
                    Gdx.files.internal(GameWorld.DEFAULT_FONT_PNG), false);
            uiSkin = makeSkin();
        }
//todo: // uiSkin = makeSkin(skin);

        float scale = Gdx.graphics.getDensity();
        if (scale > 1) {
            menuFont.getData().setScale(scale);
        }
        /*
         * GameUI sets a label on the menu which may eventually be useful for other purposes
         */
        if (null != menuName) {
            Label onScreenMenuLabel = new Label(menuName, new Label.LabelStyle(menuFont, Color.WHITE));
            onscreenMenuTbl.add(onScreenMenuLabel).fillX().uniformX();
        }

        onscreenMenuTbl.setFillParent(true);
        onscreenMenuTbl.setDebug(true);
        onscreenMenuTbl.setVisible(true);
        addActor(onscreenMenuTbl);

        setupPlayerInfo();

        // transparent overlay layer
        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap pixmap =
                new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
//        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(1, 1, 1, 1); // default alpha 0 but set all color bits 1
        pixmap.fillRectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        overlayTexture = new Texture(pixmap);
        overlayImage = new Image(overlayTexture);
        overlayImage.setPosition(0, 0);
        pixmap.dispose();

        Table overlayTbl = new Table();
        overlayTbl.setFillParent(true);
        overlayTbl.add(overlayImage);
//        overlayTbl.setDebug(true);
        overlayTbl.setVisible(true);
        overlayTbl.setTouchable(Touchable.disabled);
        addActor(overlayTbl);
        setOverlayColor(0, 0, 0, 0);

        buttonGroup = new ButtonGroup<>();
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.setMinCheckCount(1);

        // hack ...state for "non-game" screen should be "paused" since we use it as a visibility flag!
        GameWorld.getInstance().setIsPaused(true);
    }

    /*
     * Reference:
     *  https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java
     */
    private Skin makeSkin() {
        return makeSkin(new Skin());
    }

    private Skin makeSkin(Skin skin) {
        //create a Labels showing the score and some credits
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        skin.add("default", new Label.LabelStyle(menuFont, Color.WHITE));
        // Store the default libgdx font under the name "default".
        skin.add("default", menuFont);

        // Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.checked = skin.newDrawable("white", Color.BLUE);
        textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);

        return skin;
    }

    void setLabelColor(Label label, Color c) {
        label.setStyle(new Label.LabelStyle(menuFont, c));
    }

    void setOverlayColor(float r, float g, float b, float a) {
        if (null != overlayImage) {
            overlayImage.setColor(r, g, b, a);
        }
    }

    Label scoreLabel;
    Label itemsLabel;
    Label timerLabel;
    Label mesgLabel;

    private void setupPlayerInfo() {
        scoreLabel = new Label("0000", new Label.LabelStyle(menuFont, Color.WHITE));
        playerInfoTbl.add(scoreLabel);

        itemsLabel = new Label("0/3", new Label.LabelStyle(menuFont, Color.WHITE));
        playerInfoTbl.add(itemsLabel);

        timerLabel = new Label("0:15", new Label.LabelStyle(menuFont, Color.WHITE));
        playerInfoTbl.add(timerLabel).padRight(1);

        playerInfoTbl.row().expand();

        mesgLabel = new Label("Continue? 9 ... ", new Label.LabelStyle(menuFont, Color.WHITE));
        playerInfoTbl.add(mesgLabel).colspan(3);
        mesgLabel.setVisible(false); // only see this in "Continue ..." sceeen

        playerInfoTbl.row().bottom().left();
        playerInfoTbl.setFillParent(true);
//        playerInfoTbl.setDebug(true);
        playerInfoTbl.setVisible(false);
        addActor(playerInfoTbl);
    }

    /*
     * add a label in the default font and style
     */
    void addLabel(String labelText, Color labelColor) {
        addActor(new Label(labelText, new Label.LabelStyle(menuFont, labelColor)));
    }

    /**
     * Add a "Next" button to an in-game menu. Button touch handler sets the virtual Control
     * Button in the Input Mapper.
     */
    void addNextButton() {
        if (GameWorld.getInstance().getIsTouchScreen()) {
            Pixmap button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
//            button.setBlending(Pixmap.Blending.None);
            Pixmap.setBlending(Pixmap.Blending.None);
            button.setColor(1, 0, 0, 1);
            button.fillCircle(25, 25, 25);

            buttonTexture = new Texture(button);
            button.dispose();
            TextureRegion myTextureRegion = new TextureRegion(buttonTexture);
            TextureRegionDrawable myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);

            ImageButton nextButton = new ImageButton(myTexRegionDrawable);
            // add a touch down listener to the Next button
            nextButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, true);
                    return false;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, false);
                }
            });
            onscreenMenuTbl.row();
            onscreenMenuTbl.add(nextButton).fillX().uniformX();
        }
    }

    /*
     * key/touch event handlers
     */
    public enum ButtonEventHandler {
        EVENT_NONE,
        EVENT_A,
        EVENT_B,
        EVENT_LEFT,
        EVENT_RIGHT
    }

    /**
     * @param tex          texture
     * @param posX         X coord
     * @param posY         Y coord
     * @param inputBinding Button Event Handler
     * @return Image Button
     */
    ImageButton addImageButton(
            Texture tex, float posX, float posY, final ButtonEventHandler inputBinding) {

        savedTextureRefs.add(tex);
        final ImageButton newButton = addImageButton(tex, posX, posY);

        newButton.addListener(
                new InputListener() {
                    final ButtonEventHandler binding = inputBinding;

                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                        if (ButtonEventHandler.EVENT_A == binding) {
                            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, true);

                        } else if (ButtonEventHandler.EVENT_B == binding) {
                            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_B, true);

                        } else if (ButtonEventHandler.EVENT_LEFT == binding) {
                            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_LEFT, true);

                        } else if (ButtonEventHandler.EVENT_RIGHT == binding) {
                            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_RIGHT, true);
                        }
                        // true to also handle touchUp events which seems to be needed in a few cases
                        return true;
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        if (ButtonEventHandler.EVENT_A == binding) {
                            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, false);
                        } else if (ButtonEventHandler.EVENT_B == binding) {
                            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_B, false);
                        } else if (ButtonEventHandler.EVENT_LEFT == binding) {
                            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_LEFT, false);
                        } else if (ButtonEventHandler.EVENT_RIGHT == binding) {
                            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_RIGHT, false);
                        }
                    }
                }
        );
        return newButton;
    }

    private ImageButton addImageButton(Texture tex, float posX, float posY) {
        TextureRegionDrawable myTexRegionDrawable = new TextureRegionDrawable(new TextureRegion(tex));
        ImageButton newButton = new ImageButton(myTexRegionDrawable);
        addActor(newButton);
        newButton.setPosition(posX, posY);
        return newButton;
    }

    void addButton(String name, String styleName) {
        addButton(new TextButton(name, uiSkin, styleName));
    }

    void addButton(String text) {
        addButton(new TextButton(text, uiSkin));
    }

    private void addButton(TextButton button) {
        buttonNames.add(button.getText().toString());
        buttonGroup.add(button);
        actorCount += 1;
        onscreenMenuTbl.row();
        onscreenMenuTbl.add(button).fillX().uniformX();
    }

    int setCheckedBox() {
        return setCheckedBox(checkedUpDown());
    }

    int setCheckedBox(int checked) {
        if (buttonNames.size > 0) {
            String name = buttonNames.get(checked);
            buttonGroup.setChecked(name);
        }
        return checked;
    }

    int getCheckedIndex() {
        return buttonGroup.getCheckedIndex();
    }

    int checkedUpDown() {

        int step = mapper.getAxisI(InputMapper.VIRTUAL_WS_AXIS);
        int selectedIndex = buttonGroup.getCheckedIndex();

        if (0 == previousIncrement) {
            selectedIndex += step;
        }
        previousIncrement = step;

        if (selectedIndex >= actorCount) {
            selectedIndex = (actorCount - 1);
        }
        if (selectedIndex < 0) {
            selectedIndex = 0;
        }
        return selectedIndex;
    }

    private static final int KEY_CODE_POV_UP = Input.Keys.DPAD_UP;
    private static final int KEY_CODE_POV_DOWN = Input.Keys.DPAD_DOWN;
    private static final int KEY_CODE_POV_LEFT = Input.Keys.DPAD_LEFT;
    private static final int KEY_CODE_POV_RIGHT = Input.Keys.DPAD_RIGHT;

    @Override
    public boolean keyDown(int keycode) {

        int axisSetIndexX = InputMapper.VIRTUAL_AD_AXIS;
        int axisSetIndexY = InputMapper.VIRTUAL_WS_AXIS;

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            axisSetIndexX = InputMapper.VIRTUAL_X1_AXIS; // right anlg stick "X" (if used)
            axisSetIndexY = InputMapper.VIRTUAL_Y1_AXIS; // right anlg stick "Y" (if used)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
            axisSetIndexX = InputMapper.VIRTUAL_R2_AXIS; // front-left axis button
            axisSetIndexY = InputMapper.VIRTUAL_L2_AXIS; // front-right axis button
        }
        if (Input.Keys.DPAD_LEFT == keycode) {
            mapper.setAxis(axisSetIndexX, -1);
        }
        if (Input.Keys.DPAD_RIGHT == keycode) {
            mapper.setAxis(axisSetIndexX, +1);
        }
        if (Input.Keys.DPAD_UP == keycode) {
            mapper.setAxis(axisSetIndexY, -1);
        }
        if (Input.Keys.DPAD_DOWN == keycode) {
            mapper.setAxis(axisSetIndexY, +1);
        }
        if (Input.Keys.SPACE == keycode) {
            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, true);
        }
        if (Input.Keys.CONTROL_LEFT == keycode) {
            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_B, true);
        }
        if (Input.Keys.ESCAPE == keycode || Input.Keys.BACK == keycode) {
            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_START, true);
        }
        if (Input.Keys.TAB == keycode) {
            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_SELECT, true);
        }
        if (Input.Keys.SHIFT_RIGHT == keycode) {
            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_L1, true);
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {

        int axisSetIndexX = InputMapper.VIRTUAL_AD_AXIS;
        int axisSetIndexY = InputMapper.VIRTUAL_WS_AXIS;

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            axisSetIndexX = InputMapper.VIRTUAL_X1_AXIS; // right anlg stick "X" (if used)
            axisSetIndexY = InputMapper.VIRTUAL_Y1_AXIS; // right anlg stick "Y" (if used)
        } else if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
            axisSetIndexX = InputMapper.VIRTUAL_R2_AXIS;
            axisSetIndexY = InputMapper.VIRTUAL_L2_AXIS;
        }
        /*
         * keyboard WASD handling: axis is only cleared if both +/- keys are released
         */
        if ((KEY_CODE_POV_LEFT == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_RIGHT)) ||
                (KEY_CODE_POV_RIGHT == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_LEFT))) {
            mapper.setAxis(axisSetIndexX, 0);
        }
        if ((KEY_CODE_POV_UP == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_DOWN)) ||
                (KEY_CODE_POV_DOWN == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_UP))) {
            mapper.setAxis(axisSetIndexY, 0);
        }
        // action buttons
        if (Input.Keys.SPACE == keycode) {
            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, false);
        }
        if (Input.Keys.CONTROL_LEFT == keycode) {
            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_B, false);
        }
        // UI/menu activation buttons
        if (Input.Keys.ESCAPE == keycode || Input.Keys.BACK == keycode) {
            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_START, false);
        }
        if (Input.Keys.TAB == keycode) {
            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_SELECT, false);
        }
        if (Input.Keys.SHIFT_RIGHT == keycode) {
            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_L1, false);
        }
        return false;
    }

    // event handlers to be overridden
    protected void onSelectEvent() { // mt
    }

    protected void onPauseEvent() { // mt
    }

    protected void onEscEvent() { // mt
    }

    /*
     * dispose textures of Actors that were added to the Stage by the client
     */
    private void clearShapeRefs() {
        for (Texture tex : savedTextureRefs) {
            tex.dispose();
        }
    }

    @Override
    public void dispose() {

        clearShapeRefs();

        if (null != overlayTexture) {
            overlayTexture.dispose();
        }
        if (null != menuFont) {
            menuFont.dispose();
        }
        if (null != uiSkin) {
            uiSkin.dispose();
        }
        if (null != buttonTexture) {
            buttonTexture.dispose();
        }
    }
}
