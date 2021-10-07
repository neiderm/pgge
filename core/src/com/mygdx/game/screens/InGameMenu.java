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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
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

    private final Table onscreenMenuTbl = new Table();
    private final Array<Texture> savedTextureRefs = new Array<>();
    private final Array<String> buttonNames = new Array<>();

    private ButtonGroup<TextButton> buttonGroup;
    private Image overlayImage; // disposable
    private int previousIncrement;
    private int actorCount;
    // disposables
    private Texture overlayTexture;
    private BitmapFont menuFont;

    Skin uiSkin;
    InputMapper mapper;

    /**
     * constructor for a menu that has a header label with the menu name
     * https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java
     */
    InGameMenu() {
        super();
        savedTextureRefs.clear();
        mapper = new InputMapper();
        // if mapper gets changed to extend ControllerListenerAdapter then addListener ends up here
        // Controllers.addListener(mapper);

        final String DEFAULT_FONT = "default-font";
        final String DEFAULT_UISKIN_JSON = "skin/uiskin.json";
        Skin skin = new Skin(Gdx.files.internal(DEFAULT_UISKIN_JSON));
        menuFont = skin.getFont(DEFAULT_FONT);
        menuFont.getData().setScale(GameWorld.FONT_X_SCALE, GameWorld.FONT_Y_SCALE);

        uiSkin = skin;

        // transparent overlay layer
        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap pixmap =
                new Pixmap(GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT, Pixmap.Format.RGBA8888);
//        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(1, 1, 1, 1); // default alpha 0 but set all color bits 1
        pixmap.fillRectangle(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);

        overlayTexture = new Texture(pixmap);
        overlayImage = new Image(overlayTexture);
        overlayImage.setPosition(0, 0);
        pixmap.dispose();

        Table overlayTbl = new Table();
        overlayTbl.setFillParent(true);
        overlayTbl.add(overlayImage);
        overlayTbl.setVisible(true);
        overlayTbl.setTouchable(Touchable.disabled);
        addActor(overlayTbl);
        setOverlayColor(0, 0, 0, 0);
//        overlayTbl.setDebug(true);

        buttonGroup = new ButtonGroup<>();
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.setMinCheckCount(1);
    }

    /**
     * Create a selection menu
     * @param menuName String name of menu, no name if empty string or null
     * @param buttonNames String list of button names in order to be added
     */
    void createMenu(String menuName, String... buttonNames) {
        // add the menu table last so that it will always be over the overlay layer
        onscreenMenuTbl.setFillParent(true);
        addActor(onscreenMenuTbl);
//        onscreenMenuTbl.setDebug(true);
        if ((null != menuName) && (menuName.length() > 0)) {
            onscreenMenuTbl.add(new Label(menuName, uiSkin)).fillX().uniformX();
        }
        for (String name : buttonNames) {
            addButton(new TextButton(name, uiSkin, "toggle"));
        }
        addNextButton();
        setMenuVisibility(false);
    }

    void createMenu(String menuName, boolean visible, String... buttonNames) {
        createMenu(menuName, buttonNames);
        setMenuVisibility(visible);
    }

    private void addButton(TextButton button) {
        buttonNames.add(button.getText().toString());
        buttonGroup.add(button);
        actorCount += 1;
        onscreenMenuTbl.row();
        onscreenMenuTbl.add(button).fillX().uniformX();
    }

    void setMenuVisibility(boolean visible) {
        onscreenMenuTbl.setVisible(visible);
    }

    boolean getMenuVisibility() {
        return onscreenMenuTbl.isVisible();
    }

    void setLabelColor(Label label, Color c) {
        label.setStyle(new Label.LabelStyle(menuFont, c));
    }

    void setOverlayColor(float r, float g, float b, float a) {
        if (null != overlayImage) {
            overlayImage.setColor(r, g, b, a);
        }
    }

    /**
     * add a label in the default font and style
     */
    void addLabel(String labelText) {
        addActor(new Label(labelText, uiSkin));
    }

    /**
     * Add a "Next" button to an in-game menu. Button touch handler sets the virtual Control
     * Button in the Input Mapper.
     */
    private void addNextButton() {
        Button nextButton = new TextButton("Next", uiSkin, "default");
        nextButton.setColor(new Color(1, 0f, 0, 1.0f));
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
    }
}
