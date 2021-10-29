/*
 * Copyright (c) 2021 Glenn Neidermeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mygdx.game.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.ModelGroup;
import com.mygdx.game.sceneLoader.ModelInfo;
import com.mygdx.game.sceneLoader.SceneData;

import java.util.ArrayList;

/*
 * Player selects the Rig from a revolving platform.
 * There is no scene graph here, and Bullet physics is not used either. so it's just raw math to
 * revolve and push things around in the  3D world. Intend to man up and use a real math and
 * transform for object positions. (Right now it's just manipulating X/Z "2 1/2 D" by sin/cos).
 */
class SelectScreen extends BaseScreenWithAssetsEngine {

    private static final String CLASS_STRING = "SelectScreen";
    private static final String SCREENS_DIR = "screens/";
    private static final String DOT_JSON = ".json";

    private final Vector3 logoPositionVec = new Vector3(); // tmp vector for reuse
    private final Vector3 cubePositionVec = new Vector3(); // tmp vector for reuse
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private ArrayList<String> stageNamesList; // needs re-instantiated at each screen invocation
    private InGameMenu stage; // don't instantiate me here ... skips select platform
    private int dPadYaxis;
    private Label theLabel;
    private String stagename;
    private Entity logoEntity;
    private Entity cubeEntity;
    private Entity platform;
    private TextButton nextButton;
    private RigSelect rigSelect;
    private ScreenType screenType = ScreenType.INVALID;
    private Table armorSelectTable;
    private Texture leftBtexture;
    private Texture rightBtexture;

    private static final String STATIC_OBJECTS = "InstancedModelMeshes";
    private static final float LOGO_START_PT_Y = 10.0f;//tbd
    private static final float LOGO_END_PT_Y = 0.8f; // see z-dimension of LogoText in cubetest.blend

    private enum ScreenType {
        TITLE,
        LEVEL,
        ARMOR,
        INVALID
    }

    /**
     * bah .. determined more or less by arbitrary order in model info STATIC_OBJECTS of json file
     */
    private enum ModelNodes {
        LOGO,
        CUBE
    }

    @Override
    public void show() {
        super.init();
        stage = new InGameMenu();
        stage.addListener(
                new ActorGestureListener() {
                    @Override
                    public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
//                        if (Math.abs(deltaX) > Math.abs(deltaY))
                        {
                            final int THRSH = 5;
                            // r2l2active = true
                            if (deltaX < (-THRSH)) {
                                stage.mapper.setAxis(InputMapper.VIRTUAL_AD_AXIS, -1);
                            } else if (deltaX > (+THRSH)) {
                                stage.mapper.setAxis(InputMapper.VIRTUAL_AD_AXIS, +1);
                            }
                        }
                    }

                    @Override
                    public void panStop(InputEvent event, float x, float y, int pointer, int button) {
                        // if r2l2active
                        stage.mapper.setAxis(InputMapper.VIRTUAL_AD_AXIS, 0);
                    }
                }
        );
        Gdx.input.setInputProcessor(stage);

        ImmutableArray<Entity> characters = engine.getEntitiesFor(Family.all(CharacterComponent.class).get());

        // hide the Armor Units (move out of view on the Y axis)
        for (Entity e : characters) {
            Matrix4 transform = e.getComponent(ModelComponent.class).modelInst.transform;
            transform.setToTranslation(0, 10, 0);
        }

        GameFeature f = GameWorld.getInstance().getFeature(GameWorld.LOCAL_PLAYER_FNAME);
        if (null != f) {
            platform = f.getEntity();
        }

        rigSelect = new RigSelect(platform, characters);

        stageNamesList = createScreenslist();

        Color theColor = new Color(0, 0f, 0, 0f);
        theColor.set(0, 1.0f, 0, 0.5f);
        nextButton = stage.addTextButton("Next", theColor, InGameMenu.ButtonEventHandler.EVENT_A);
        nextButton.setVisible(false);

        createArmorSelectTable();

        theLabel = new Label("I need aligned!", stage.uiSkin);
        stage.addActor(theLabel);
        theLabel.setVisible(false);

        // grab a handle to selected entities
        SceneData sd = GameWorld.getInstance().getSceneData();

        for (Entity e : engine.getEntitiesFor(Family.all(ModelComponent.class).get())) {
            ModelComponent modelComponent = e.getComponent(ModelComponent.class);
            Node nnn = modelComponent.modelInst.nodes.get(0);

            GameObject gameObject;

            gameObject = sd.modelGroups.get(STATIC_OBJECTS).getElement(ModelNodes.LOGO.ordinal());
            if (nnn.id.equals(gameObject.objectName)) {
                logoEntity = e;
            }
            gameObject = sd.modelGroups.get(STATIC_OBJECTS).getElement(ModelNodes.CUBE.ordinal());
            if (nnn.id.equals(gameObject.objectName)) {
                cubeEntity = e;
            }
        }
        // position title text out of view
        ModelComponent modelComponent = logoEntity.getComponent(ModelComponent.class);
        modelComponent.modelInst.transform.setToTranslation(0, LOGO_START_PT_Y, 0);
    }

    private ArrayList<String> createScreenslist() {
        ArrayList<String> namesArray = new ArrayList<>();
        FileHandle[] files = Gdx.files.internal(SCREENS_DIR).list();

        for (FileHandle file : files) {
            // stick the base-name (no path, no extension) into the menu
            String fname = file.name();
            if (fname.matches("(.*).json($)")) {
                String basename = fname.replaceAll(".json$", "");
                namesArray.add(basename);
            }
        }
        stage.createMenu(null, namesArray.toArray(new String[0]));
        return namesArray;
    }

    private ImageButton mkImageButton(Texture tex, float posX, float posY) {
        TextureRegionDrawable myTexRegionDrawable = new TextureRegionDrawable(new TextureRegion(tex));
        ImageButton newButton = new ImageButton(myTexRegionDrawable);
        newButton.setPosition(posX, posY);
        return newButton;
    }

    private void createArmorSelectTable() {

        armorSelectTable = new Table();
        armorSelectTable.setFillParent(true);

        Label armorLabel = new Label("Select armor unit", stage.uiSkin);
        armorSelectTable.add(armorLabel);
        armorSelectTable.row().expand();

//        addActor(xxxx);
//        xxxx.setDebug(true);
        final int ARROW_EXT = 64; // extent of arrow tile (height/width)
        final int ARROW_MID = ARROW_EXT / 2;

        Color theColor = new Color(0, 0f, 0, 0f);
        theColor.set(1, 0, 0, 0.5f);
        Pixmap pixmap;

        pixmap = new Pixmap(ARROW_EXT, ARROW_EXT, Pixmap.Format.RGBA8888);
        pixmap.setColor(theColor);
        pixmap.fillTriangle(0, ARROW_MID, ARROW_EXT, ARROW_EXT, ARROW_EXT, 0);
        leftBtexture = new Texture(pixmap); // disposed by stored ref
        ImageButton leftButton = mkImageButton(leftBtexture, 0, GameWorld.VIRTUAL_HEIGHT / 2.0f);
        leftButton.addListener(
                new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_LEFT, true);
                        return true; // for touch up?
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_LEFT, false);
                    }
                });
        pixmap.dispose();
        armorSelectTable.add(leftButton);

        pixmap = new Pixmap(ARROW_EXT, ARROW_EXT, Pixmap.Format.RGBA8888);
        pixmap.setColor(theColor);
        pixmap.fillTriangle(0, 0, 0, ARROW_EXT, ARROW_EXT, ARROW_MID);
        rightBtexture = new Texture(pixmap);
        ImageButton rightButton = mkImageButton(rightBtexture, GameWorld.VIRTUAL_WIDTH - (float) ARROW_EXT, GameWorld.VIRTUAL_HEIGHT / 2.0f);
        rightButton.addListener(
                new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_RIGHT, true);
                        return true; // for touch up?
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_RIGHT, false);
                    }
                });
        pixmap.dispose();
        armorSelectTable.add(rightButton);

        armorSelectTable.row().expand().bottom();

        TextButton button = new TextButton("Next", stage.uiSkin, "default");
        button.setSize(GameWorld.VIRTUAL_WIDTH / 4.0f, GameWorld.VIRTUAL_HEIGHT / 4.0f);
        button.setPosition((GameWorld.VIRTUAL_WIDTH / 2.0f) - (GameWorld.VIRTUAL_WIDTH / 8.0f), 0);
        button.setColor(new Color(0, 1f, 0, 1.0f));
        button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, false);
            }
        });
        armorSelectTable.add(button);
        armorSelectTable.row().bottom();

        armorSelectTable.setVisible(false);
        stage.addActor(armorSelectTable);
    }

    /*
     * dPad X axis + touch-swipe (left/right)
     */
    private int getStep() {

        int axis = stage.mapper.getAxisI(InputMapper.VIRTUAL_AD_AXIS);
        if (0 == axis) { // if input is inactive
            /* key is released ... not necessary but for debugging */
            dPadYaxis = 0; // de-latch previous input state
        } else { /* if input is active */
            if (0 == dPadYaxis) { // if input is "justPressed"
                dPadYaxis = axis;  // latch the new state
            } else { // if input is held
                axis = 0;
            }
        }
        return axis;
    }

    private Screen newLoadingScreen(String path) {

        SceneData sd = GameWorld.getInstance().getSceneData();
        ModelGroup mg = sd.modelGroups.get("Characters");
        // first 3 Characters are on the platform - use currently selected index to retrieve
        String playerObjectName = mg.getElement(rigSelect.getSelectedIndex()).objectName;

        // When loading from Select Screen, need to distinguish the name of the selected player
        // object by an arbitrary character string to make sure locally added player model info
        // doesn't bump into the user-designated model info sections in the screen json files
        final String PLAYER_OBJECT_TAG = "P0_";
        String playerFeatureName = PLAYER_OBJECT_TAG;

        if (null != playerObjectName) {
            // get the player model info from previous scene (which should still be valid)
            ModelInfo selectedModelInfo = sd.modelInfo.get(playerObjectName);
            playerFeatureName += playerObjectName;
            GameWorld.getInstance().setSceneData(path, playerFeatureName, selectedModelInfo);
        } else {
            Gdx.app.log(CLASS_STRING, "Error: Player Objet Name may not be null!");
        }
        return new LoadingScreen();
    }

    private void handleUI() {

        ModelComponent modelCompLogo = logoEntity.getComponent(ModelComponent.class);
        modelCompLogo.modelInst.transform.getTranslation(logoPositionVec);

        ModelComponent modelCompCube = cubeEntity.getComponent(ModelComponent.class);
        modelCompCube.modelInst.transform.getTranslation(cubePositionVec);

        switch (screenType) {
            default:
            case TITLE:
                // swipe-in the logo text block ...
                final float kPlogo = 0.10f;
                float error = logoPositionVec.y - LOGO_END_PT_Y;
                if ((logoPositionVec.y > LOGO_END_PT_Y) && (error > kPlogo)) {
                    logoPositionVec.y = logoPositionVec.y - (error * kPlogo);
                } else {
                    logoPositionVec.y = LOGO_END_PT_Y; // snap title block to end point
                    //enable Next button
                    nextButton.setVisible(true);
                }
                modelCompLogo.modelInst.transform.setToTranslation(logoPositionVec);

                if (nextButton.isVisible() &&
                        stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)) {
                    stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, false); // hmmm debounce me
                    screenType = ScreenType.LEVEL;
                    stagename = "vr_zone"; // set the default
                    nextButton.setVisible(false);
                }
                break;

            case LEVEL:
                int idxMenuSelection = stage.setCheckedBox(); // stage.updateMenuSelection()
                // move title text off screen
                if (logoPositionVec.x < 10) {
                    // since x could start at zero, an additional summed amount ensures non-zero multiplicand
                    logoPositionVec.x = (logoPositionVec.x + 0.01f) * 1.10f;
                    modelCompLogo.modelInst.transform.setToTranslation(logoPositionVec);
                } else {
                    // once title text is out of the way ...
//                        if (!stageNamesList.isEmpty()) {
                    stage.setMenuVisibility(true);
                    theLabel.setText("Select a mission");
                    theLabel.setVisible(true);
                    // enable next button ...
                    nextButton.setVisible(true);
                }
                if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)
                        && nextButton.isVisible()) {
                    stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, false); // hmmm debounce me
                    nextButton.setVisible(false);
                    if (!stageNamesList.isEmpty()) {
                        stagename = stageNamesList.get(idxMenuSelection);
                        stage.setMenuVisibility(false);
                        screenType = ScreenType.ARMOR;
                        theLabel.setVisible(false);
                    }
                }
                break;

            case ARMOR:
                // if in position, then enable UI
                if (!rigSelect.updatePlatformPosition()) {
                    // enable UI and update platform
                    armorSelectTable.setVisible(true);
                    rigSelect.updatePlatformRotation(getStep());

                    if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)) {
                        GameWorld.getInstance().showScreen(
                                newLoadingScreen(SCREENS_DIR + stagename + DOT_JSON));
                    }
                }
                // update hide the cube
                if (cubePositionVec.x < 20) {
                    // since x could start at zero, an additional summed amount ensure non-zero multiplicand
                    cubePositionVec.x = (cubePositionVec.x + 0.01f) * 1.10f;
                    modelCompCube.modelInst.transform.setToTranslation(cubePositionVec);
                }
                break;
        }
    }

    @Override
    public void render(float delta) {
        // plots debug graphics
        super.render(delta);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        handleUI();
    }

    @Override
    public void resize(int width, int height) { // MT
    }

    @Override
    public void dispose() {
        engine.removeAllEntities(); // allow listeners to be called (for disposal)
        shapeRenderer.dispose();
        stage.dispose();
        leftBtexture.dispose();
        rightBtexture.dispose();
        // screens that load assets must calls assetLoader.dispose() !
        super.dispose();
    }

    /*
     * android "back" button sends ApplicationListener.pause(), but then sends ApplicationListener.dispose() !!
     */
    @Override
    public void pause() { // MT
    }

    @Override
    public void resume() { // MT
    }

    @Override
    public void hide() {
        dispose();
    }
}
