/*
 * Copyright (c) 2019 Glenn Neidermeier
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
 */

/*
 * Reference:
 *  on-screen menus:
 *   https://www.gamedevelopment.blog/full-libgdx-game-tutorial-menu-control/
 *  UI skin defined programmatically:
 *   https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java
 */

class InGameMenu extends Stage {

    InputMapper mapper = new InputMapper();
    private Table onscreenMenuTbl = new Table();
    private Table playerInfoTbl = new Table();
    private Table overlayTbl = new Table();

    private int previousIncrement;
    private Array<String> buttonNames = new Array<String>();
    private ButtonGroup<TextButton> bg;
    private int count;
// @dispsables
    private Texture buttonTexture;
    private Texture overlayTexture;
    private Image overlayImage;

    private  Skin uiSkin;
    private BitmapFont font;


//    InGameMenu(String skinName){} // TODO

    InGameMenu(String skinName, String menuName) {

        super();

        // make suure to have a font to work with!
        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"), Gdx.files.internal("data/font.png"), false);

        if (null != skinName) {

            uiSkin = new Skin(Gdx.files.internal(skinName/*"skin/uiskin.json"*/));
            BitmapFont bf = uiSkin.getFont("commodore-64");

            float scale = Gdx.graphics.getDensity();

            if (scale > 1) {
                    bf.getData().setScale(scale);
            }
        }
        else{
            uiSkin = setSkin();
        }

        if (null != menuName){
            Label onScreenMenuLabel = new Label(menuName, new Label.LabelStyle(font, Color.WHITE));
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
                new Pixmap(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1); // default alpha 0 but set all color bits 1
        pixmap.fillRectangle(0, 0, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

        overlayTexture = new Texture(pixmap);
        overlayImage = new Image(overlayTexture);
        overlayImage.setPosition(0,0);
        pixmap.dispose();

        overlayTbl.setFillParent(true);
        overlayTbl.add(overlayImage);
//        overlayTbl.setDebug(true);
        overlayTbl.setVisible(true);
        overlayTbl.setTouchable(Touchable.disabled);
        addActor(overlayTbl);
        setOverlayColor(0, 0, 0, 0);
/*
        Stack stack = new Stack();
        addActor(stack);
        stack.setSize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        stack.add(onscreenMenuTbl);
        stack.add(playerInfoTbl);
        stack.add(overlayTbl);
*/
        bg = new ButtonGroup<TextButton>();
        bg.setMaxCheckCount(1);
        bg.setMinCheckCount(1);

        // hack ...state for "non-game" screen should be "paused" since we use it as a visibility flag!
        GameWorld.getInstance().setIsPaused(true);
    }

    void setLabelColor(Label label, Color c){
        label.setStyle(new Label.LabelStyle(font, c));
    }

    void setOverlayColor(float r, float g, float b, float a){
        if (null != overlayImage) {
            overlayImage.setColor(r, g, b, a);
        }
    }

    Label scoreLabel;
    Label itemsLabel;
    Label timerLabel;
    Label mesgLabel;

    void setVisibleUI(boolean state){
        mesgLabel.setVisible(state);
        itemsLabel.setVisible(state);
        timerLabel.setVisible(state);
        scoreLabel.setVisible(state);
    }

    private void setupPlayerInfo(){

        scoreLabel = new Label("0000", new Label.LabelStyle(font, Color.WHITE));
        playerInfoTbl.add(scoreLabel);

        itemsLabel = new Label("0/3", new Label.LabelStyle(font, Color.WHITE));
        playerInfoTbl.add(itemsLabel);

        timerLabel = new Label("0:15", new Label.LabelStyle(font, Color.WHITE));
        playerInfoTbl.add(timerLabel).padRight(1);

        playerInfoTbl.row().expand();

        mesgLabel = new Label("Continue? 9 ... ", new Label.LabelStyle(font, Color.WHITE));
        playerInfoTbl.add(mesgLabel).colspan(3);

        playerInfoTbl.setFillParent(true);
//        playerInfoTbl.setDebug(true);
        playerInfoTbl.setVisible(true);
        addActor(playerInfoTbl);
    }

    void addNextButton(){

        if (GameWorld.getInstance().getIsTouchScreen()) {
            Pixmap.setBlending(Pixmap.Blending.None);
            Pixmap button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
            button.setColor(1, 0, 0, 1);
            button.fillCircle(25, 25, 25);

            buttonTexture = new Texture(button);
            button.dispose();
            TextureRegion myTextureRegion = new TextureRegion(buttonTexture);
            TextureRegionDrawable myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);

            ImageButton nextButton = new ImageButton(myTexRegionDrawable);
            nextButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    mapper.setInputState(InputMapper.InputState.INP_SELECT);
                    return false;
                }
            });
            onscreenMenuTbl.row();
            onscreenMenuTbl.add(nextButton).fillX().uniformX();
        }
    }

    private Skin setSkin(){
/*
        font = new BitmapFont(Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
*/
        //create a Labels showing the score and some credits
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        Skin skin = new Skin();
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

        float scale = Gdx.graphics.getDensity();
        BitmapFont bf = skin.getFont("default");

        if (scale > 1)
            bf.getData().setScale(scale);

        return skin;
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
        count += 1;

        onscreenMenuTbl.row();
        onscreenMenuTbl.add(button).fillX().uniformX();
    }

    /*
     * returns true ^H^H^H^H
     */
    void setCheckedBox(int checked) {

        String name = buttonNames.get(checked);

        bg.setChecked(name);
    }

    int getCheckedIndex(){
        return bg.getCheckedIndex();
    }

/*
    int getCurrentSelection(){
        return currentSelection;
    }
*/
    int checkedUpDown(int step) {

        int checkedIndex = bg.getCheckedIndex();

        final int N_SELECTIONS = count;

        int selectedIndex = checkedIndex;

        if (0 == previousIncrement)
            selectedIndex += step;

        previousIncrement = step;

        if (selectedIndex >= N_SELECTIONS) {
//            selectedIndex = 0;
            selectedIndex = (N_SELECTIONS - 1);
        }
        if (selectedIndex < 0) {
//            selectedIndex = N_SELECTIONS - 1;
            selectedIndex = 0;
        }

//        setCheckedBox(selectedIndex);

        return selectedIndex;
    }

    @Override
    public void act(float delta){

// one or the other
        if (GameWorld.getInstance().getIsPaused()){
            playerInfoTbl.setVisible(false);
            onscreenMenuTbl.setVisible(true);
        } else {
            onscreenMenuTbl.setVisible(false);
            playerInfoTbl.setVisible(true);
        }

        super.act(delta);
    }

    @Override
    public void dispose(){

        if (null != overlayTexture)
            overlayTexture.dispose();

        if (null != font)
            font.dispose();

        if (null != uiSkin)
            uiSkin.dispose();

        if (null != buttonTexture)
            buttonTexture.dispose();
    }
}
