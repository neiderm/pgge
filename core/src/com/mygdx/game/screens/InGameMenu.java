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

    private Table onscreenMenuTbl;
    private final Array<Texture> savedTextureRefs = new Array<>();

    ButtonGroup<TextButton> buttonGroup;
    private int previousIncrement;

    // disposables
    private Image overlayImage;
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
        Pixmap pixmap =
                new Pixmap(GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
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
    }

    /**
     * Create a selection menu
     *
     * @param menuName String name of menu, no name if empty string or null
     * @param strNames String list of button names in order to be added
     */
    Table createMenu(String menuName, String... strNames) {

        Table table = createMenu(menuName, 1, strNames);
        buttonGroup.setMaxCheckCount(1);
        return table;
    }

    private int menuMinCheckCount;

    /**
     * @param menuName      string
     * @param minCheckCount int probably 0 or 1
     * @param strNames      strings
     * @return table
     */
    Table createMenu(String menuName, int minCheckCount, String... strNames) {

        menuMinCheckCount = minCheckCount;

        if (null != onscreenMenuTbl) {
            onscreenMenuTbl.remove();
        }
        buttonGroup = new ButtonGroup<>();
        buttonGroup.setMaxCheckCount(strNames.length);
        buttonGroup.setMinCheckCount(minCheckCount);

        Table aTable = new Table();
        aTable.setFillParent(true);
//        aTable.setVisible(true); // localized control of visibility
        addActor(aTable);
//        aTable.setDebug(true);
        if ((null != menuName) && (menuName.length() > 0)) {
            aTable.add(new Label(menuName, uiSkin)).fillX().uniformX();
        }
        for (String name : strNames) {
            // adds the button to the Button Group
            addButton(aTable, new TextButton(name, uiSkin, "toggle"));
        }
        TextButton tb = makeTextButton(
                "Next", new Color(0, 1f, 0, 1.0f), ButtonEventHandler.EVENT_A);
        aTable.row();
        aTable.add(tb).fillX().uniformX();
        onscreenMenuTbl = aTable;
        return aTable;
    }

    private void addButton(Table table, TextButton button) {

        buttonGroup.add(button);
        table.row();
        table.add(button).fillX().uniformX();
    }

    void setMenuVisibility(boolean visible) {
        if (null != onscreenMenuTbl) {
            onscreenMenuTbl.setVisible(visible);
        }
    }

    boolean getMenuVisibility() {
        if (null != onscreenMenuTbl) {
            return onscreenMenuTbl.isVisible();
        }
        return false;
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
     * Add a Text Button to the Stage (not all that useful, prefer Buttons to be contained in a Table)
     *
     * @param btnText      String
     * @param theColor     Color
     * @param inputBinding ButtonEventHandler
     * @return TextButton
     */
    private TextButton makeTextButton(String btnText, Color theColor, final ButtonEventHandler inputBinding) {

        TextButton button = new TextButton(btnText, uiSkin, "default");
        button.setSize(GameWorld.VIRTUAL_WIDTH / 4.0f, GameWorld.VIRTUAL_HEIGHT / 4.0f);
        button.setPosition((GameWorld.VIRTUAL_WIDTH / 2.0f) - (GameWorld.VIRTUAL_WIDTH / 8.0f), 0);
        button.setColor(theColor);

        button.addListener(new InputListener() {
            final ButtonEventHandler binding = inputBinding;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (ButtonEventHandler.EVENT_A == binding) {
                    mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, true);
                }
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                if (ButtonEventHandler.EVENT_A == binding) {
                    mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, false);
                }
            }
        });
        return button;
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

    ImageButton addImageButton(Texture tex, float posX, float posY) {
        TextureRegionDrawable myTexRegionDrawable = new TextureRegionDrawable(new TextureRegion(tex));
        ImageButton newButton = new ImageButton(myTexRegionDrawable);
        addActor(newButton);
        newButton.setPosition(posX, posY);
        savedTextureRefs.add(tex);

        return newButton;
    }

    /**
     * name: updateMenuSelection
     *
     * @return checked selection
     */
    int updateMenuSelection() {
        return updateMenuSelection(checkedUpDown());
    }

    private int updateMenuSelection(int checked) {
        if (null != buttonGroup) {
            Label label = buttonGroup.getButtons().get(checked).getLabel();
            buttonGroup.setChecked(label.getText().toString());
        }
        return checked;
    }

    int getCheckedIndex() {
        return buttonGroup.getCheckedIndex();
    }

    /**
     * radio button style (mutually exclusive)
     *
     * @return selected
     */
    private int checkedUpDown() {

        int step = mapper.getAxisI(InputMapper.VIRTUAL_WS_AXIS);
        int selectedIndex = 999999999;

        if (null != buttonGroup) {
            int actorCount = buttonGroup.getButtons().size;
            selectedIndex = buttonGroup.getCheckedIndex();

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
        if (Input.Keys.BACKSPACE == keycode) { // in a UI context, triangle button is often Back
            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_Y, true);
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
/// are these needed ... yes!
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
//////////
        return false;
    }

    /*
     * dispose textures of Actors that were added to the Stage by the client
     */
    private void clearTextureRefs() {
        for (Texture tex : savedTextureRefs) {
            tex.dispose();
        }
    }

    @Override
    public void act(float delta) {

        super.act(delta);

        /* if 1 min check count, handle as "legacy" radio button style button group menu */
        if (1 == menuMinCheckCount) {
            int checkedBox = 0; // button default at top selection
            if (getMenuVisibility()) {
                checkedBox = checkedUpDown();
            }
            updateMenuSelection(checkedBox);
        }
    }

    @Override
    public void dispose() {

        clearTextureRefs();

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
