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

    static final int BTN_KCODE_FIRE1 = 0;
    static final int BTN_KCODE_FIRE2 = 1;

    private final Array<Texture> savedTextureRefs = new Array<Texture>();
    private final Array<String> buttonNames = new Array<String>();
    private final ButtonGroup<TextButton> bg;

    final InputMapper mapper = new InputMapper();
    final Table onscreenMenuTbl = new Table();
    final Table playerInfoTbl = new Table();

    private int previousIncrement;
    private int actorCount;

    // @dispsables
    private final Texture overlayTexture;
    private final Image overlayImage;
    private final Skin uiSkin;
    private final BitmapFont font;
    private Texture buttonTexture;

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
        savedTextureRefs.clear();

        if (null != skinName) {
            Skin skin = new Skin(Gdx.files.internal(skinName));

            final String DEFAULT_FONT = "default-font";
            font = skin.getFont(DEFAULT_FONT);

            // to use this skin on game screen, create a Labels style for up/down text button on pause menu
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();

            skin.add("white", new Texture(pixmap));
            pixmap.dispose();

            skin.add("default", new Label.LabelStyle(font, Color.WHITE));
            // Store the default libgdx font under the name "default".
            skin.add("default", font);

            // Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
            TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
            textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
            textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
            textButtonStyle.checked = skin.newDrawable("white", Color.BLUE);
            textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
            textButtonStyle.font = skin.getFont("default");
            skin.add("default", textButtonStyle);

            uiSkin = skin;
        } else {
            // screens that are not loading UI from a skin must load the font directly
            font = new BitmapFont(Gdx.files.internal(GameWorld.DEFAULT_FONT_FNT),
                    Gdx.files.internal(GameWorld.DEFAULT_FONT_PNG), false);
            uiSkin = makeSkin();
        }

        float scale = Gdx.graphics.getDensity();
        if (scale > 1) {
            font.getData().setScale(scale);
        }
        /*
         * GameUI sets a label on the menu which may eventually be useful for other purposes
         */
        if (null != menuName) {
            Label onScreenMenuLabel = new Label(menuName, new Label.LabelStyle(font, Color.WHITE));
            onscreenMenuTbl.add(onScreenMenuLabel).fillX().uniformX();
        }

        onscreenMenuTbl.setFillParent(true);
        onscreenMenuTbl.setDebug(true);
        onscreenMenuTbl.setVisible(true);
        addActor(onscreenMenuTbl);

        setupPlayerInfo();

        // transparent overlay layer
//        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap pixmap =
                new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
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

        bg = new ButtonGroup<TextButton>();
        bg.setMaxCheckCount(1);
        bg.setMinCheckCount(1);

        // hack ...state for "non-game" screen should be "paused" since we use it as a visibility flag!
        GameWorld.getInstance().setIsPaused(true);
    }

    void setLabelColor(Label label, Color c) {
        label.setStyle(new Label.LabelStyle(font, c));
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
        scoreLabel = new Label("0000", new Label.LabelStyle(font, Color.WHITE));
        playerInfoTbl.add(scoreLabel);

        itemsLabel = new Label("0/3", new Label.LabelStyle(font, Color.WHITE));
        playerInfoTbl.add(itemsLabel);

        timerLabel = new Label("0:15", new Label.LabelStyle(font, Color.WHITE));
        playerInfoTbl.add(timerLabel).padRight(1);

        playerInfoTbl.row().expand();

        mesgLabel = new Label("Continue? 9 ... ", new Label.LabelStyle(font, Color.WHITE));
        playerInfoTbl.add(mesgLabel).colspan(3);
        mesgLabel.setVisible(false); // only see this in "Continue ..." sceeen

        playerInfoTbl.row().bottom().left();
        playerInfoTbl.setFillParent(true);
//        playerInfoTbl.setDebug(true);
        playerInfoTbl.setVisible(false);
        addActor(playerInfoTbl);
    }

    private Skin makeSkin() {
        Skin skin = new Skin();

        //create a Labels showing the score and some credits
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        skin.add("white", new Texture(pixmap)); //https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java
        pixmap.dispose();

        skin.add("default", new Label.LabelStyle(font, Color.WHITE));
        // Store the default libgdx font under the name "default".
        skin.add("default", font);

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

    /*
     * add a label in the default font and style
     */
    void addLabel(String labelText, Color labelColor) {
        addActor(new Label(labelText, new Label.LabelStyle(font, labelColor)));
    }

    /**
     * Add a "Next" button to an in-game menu. Button touch handler sets the virtual Control
     * Button in the Input Mapper.
     */
    void addNextButton() {
        if (GameWorld.getInstance().getIsTouchScreen()) {
            Pixmap button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
            button.setBlending(Pixmap.Blending.None);
//            Pixmap.setBlending(Pixmap.Blending.None);
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
                    // TS, in-game menu, tap Next button ... stuck on FIRE1, can't ESC
                    // mapper.setControlButton(BTN_KCODE_FIRE1, true);
                    mapper.setInputState(InputMapper.InputState.INP_FIRE1);
                    return false;
                }
            });
            onscreenMenuTbl.row();
            onscreenMenuTbl.add(nextButton).fillX().uniformX();
        }
    }


    /*
     * saves the texture ref for disposal ;)
     */
    ImageButton addImageButton(
            Texture tex, float posX, float posY, final InputMapper.InputState inputBinding) {

        savedTextureRefs.add(tex);

        final ImageButton newButton = addImageButton(tex, posX, posY);

        newButton.addListener(
                new InputListener() {
                    final InputMapper.InputState binding = inputBinding;

                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                        if (InputMapper.InputState.INP_FIRE1 == binding) {
                            mapper.setControlButton(BTN_KCODE_FIRE1, true);

                        } else if (InputMapper.InputState.INP_FIRE2 == binding) {
                            mapper.setControlButton(BTN_KCODE_FIRE2, true);

                        } else if (InputMapper.InputState.INP_NONE == binding) {
                            // I don't know why Select Screen event is INP NONE
                            mapper.setControlButton(BTN_KCODE_FIRE1, true);
                        }
                        return true; // to also handle touchUp
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        if (InputMapper.InputState.INP_FIRE1 == binding) {
                            mapper.setControlButton(BTN_KCODE_FIRE1, false);
                        } else if (InputMapper.InputState.INP_FIRE2 == binding) {
                            mapper.setControlButton(BTN_KCODE_FIRE2, false);
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
        bg.add(button);
        actorCount += 1;
        onscreenMenuTbl.row();
        onscreenMenuTbl.add(button).fillX().uniformX();
    }

    void setCheckedBox(int checked) {
        if (buttonNames.size > 0) {
            String name = buttonNames.get(checked);
            bg.setChecked(name);
        }
    }

    int getCheckedIndex() {
        return bg.getCheckedIndex();
    }

    int checkedUpDown(int step) {

        int selectedIndex = bg.getCheckedIndex();

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
        if (null != font) {
            font.dispose();
        }
        if (null != uiSkin) {
            uiSkin.dispose();
        }
        if (null != buttonTexture) {
            buttonTexture.dispose();
        }
    }
}
