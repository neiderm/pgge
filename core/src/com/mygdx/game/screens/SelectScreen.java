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
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.ModelGroup;
import com.mygdx.game.sceneLoader.ModelInfo;
import com.mygdx.game.sceneLoader.SceneData;
import com.mygdx.game.sceneLoader.SceneLoader;

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
private static final String STAGE_1 = "vr_zone";

    private final Vector3 logoPositionVec = new Vector3(); // tmp vector for reuse
    private final Vector3 cubePositionVec = new Vector3(); // tmp vector for reuse
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private ArrayList<String> stageNamesList; // needs re-instantiated at each screen invocation
    private InGameMenu stage; // don't instantiate me here ... skips select platform
    private int dPadYaxis;
    private String stagename;
    private Entity logoEntity;
    private Entity cubeEntity;
    private Entity platform;
    private RigSelect rigSelect;
    private MenuType menuType = MenuType.INVALID;
    private Table configMenuTable;
    private Table armorSelectTable;
    private Table logoSelectTable;

    private static final String STATIC_OBJECTS = "InstancedModelMeshes";

    private static final float LOGO_START_PT_X = 0;
    private static final float LOGO_START_PT_Y = 20.0f;

    private static final float LOGO_END_PT_X0 = LOGO_START_PT_X;
    private static final float LOGO_END_PT_Y0 = 0.8f; // see z-dimension of LogoText in cubetest.blend

    private static final float LOGO_END_PT_X1 = 10.0f; // exit, stage right!
    private static final float LOGO_END_PT_Y1 = LOGO_END_PT_Y0;

    private static final float CUBE_START_PT_X = -20.0f;
    private static final float CUBE_START_PT_Y = 0;

    private static final float CUBE_END_PT_X0 = 0;
    private static final float CUBE_END_PT_Y0 = CUBE_START_PT_Y;

    private static final float CUBE_END_PT_X1 = 20.0f; // exit, stage right!
    private static final float CUBE_END_PT_Y1 = CUBE_END_PT_Y0;

    private float logoEndPtX;
    private float logoEndPtY;
    private float cubeEndPtX;
    private float cubeEndPtY;

    private enum MenuType {
        LOGO,
        CONFIG, // select levels/controller/sound
        LEVELS,
        CONTROLLER,
        SOUND,
        ARMOR,
        INVALID
    }

    private enum LogoMenuItems {
        P1START,
        CSETUP
    }
    private enum ConfigMenuItems {
        LEVEL1,
        PASSWORD
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
        stage.setMenuVisibility(false);// should visible==false be default? see InGameMenu()
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

        // starts with Track 13
        music.play();
    }

    private ArrayList<String> createScreensMenu() {
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
        stage.createMenu("Select a mission", namesArray.toArray(new String[0]));
        return namesArray;
    }

    private void createControllerMenu() {
        // unfortunately Input Mapper is hard coded for this order of system configurations
        String[] configNames = new String[]{
                "PC (BT, 2.4 gHz, USB)", // PC
                "foo", // tbd IOS?
                "Android (BT)", // Android BlueTooth
                "bar" // unused
        };
        stage.createMenu("System?", configNames);
    }

    private void createLogoMenu() {

        menuType = MenuType.LOGO;
        /*
         * position cube to start point
         */
        ModelComponent modelCompCube = cubeEntity.getComponent(ModelComponent.class);
        modelCompCube.modelInst.transform.getTranslation(cubePositionVec);
        cubePositionVec.x = CUBE_START_PT_X;
        modelCompCube.modelInst.transform.setToTranslation(cubePositionVec);
        cubeEndPtX = CUBE_END_PT_X0;
        cubeEndPtY = CUBE_END_PT_Y0;

        /*
         * position logo block to start point
         */
        ModelComponent modelComponent = logoEntity.getComponent(ModelComponent.class);
        modelComponent.modelInst.transform.setToTranslation(0, LOGO_START_PT_Y, 0);
        // hold the logo in place by setting target coordinates equal to start location
        logoEndPtX = LOGO_START_PT_X;
        logoEndPtY = LOGO_START_PT_Y; // 0.8f; // see z-dimension of LogoText in cubetest.blend

        logoSelectTable = stage.createMenu(null, "1p Start", "Options");

        // start logo animation upon completing time delay Action
        final Action animLogo = new Action() {

            public boolean act(float delta) {
                // set the endpoint of logo block to initiate animation
                logoEndPtX = LOGO_END_PT_X0;
                logoEndPtY = LOGO_END_PT_Y0;
                return true;
            }
        };
        logoSelectTable.addAction(Actions.sequence(
                Actions.delay(1.0f), // wait for block in position
                // block in position, enable menu and start logo block animation
                Actions.show(),
                animLogo
        ));
    }

    private void createConfigMenu() {
        menuType = MenuType.CONFIG;
        configMenuTable = stage.createMenu(null, "Stage 1", "Password");

        // load next audio track
        music.dispose();
        final String AUDIO_TRACK = "Audio_Track_1";
        // grab a handle to selected entities
        SceneData sd = GameWorld.getInstance().getSceneData();
        if (null != sd) {
            ModelInfo mi = sd.modelInfo.get(AUDIO_TRACK);
            if (null != mi) {
                String audioTrack = mi.fileName;
                if (null != audioTrack) {
                    music = SceneLoader.getAssets().get(audioTrack, Music.class);

                    if (null != music) {
                        music.play();
                    }
                }
            }
        }
    }

    private void createArmorMenu() {

        menuType = MenuType.ARMOR;
        armorSelectTable = new Table();
        armorSelectTable.setVisible(false);
        armorSelectTable.setFillParent(true);
        stage.addActor(armorSelectTable);

        Label armorLabel = new Label("Select armor unit", stage.uiSkin);
        armorSelectTable.add(armorLabel);
        armorSelectTable.row().expand();

        final int ARROW_EXT = 64; // extent of arrow tile (height/width)
        final int ARROW_MID = ARROW_EXT / 2;

        Color theColor = new Color(0, 0f, 0, 0f);
        theColor.set(1, 0, 0, 0.5f);
        Pixmap pixmap;

        pixmap = new Pixmap(ARROW_EXT, ARROW_EXT, Pixmap.Format.RGBA8888);
        pixmap.setColor(theColor);
        pixmap.fillTriangle(0, ARROW_MID, ARROW_EXT, ARROW_EXT, ARROW_EXT, 0);
        ImageButton leftButton = stage.addImageButton(
                new Texture(pixmap), // disposed by stored ref
                0, GameWorld.VIRTUAL_HEIGHT / 2.0f, InGameMenu.ButtonEventHandler.EVENT_LEFT);
        pixmap.dispose();
        armorSelectTable.add(leftButton);

        pixmap = new Pixmap(ARROW_EXT, ARROW_EXT, Pixmap.Format.RGBA8888);
        pixmap.setColor(theColor);
        pixmap.fillTriangle(0, 0, 0, ARROW_EXT, ARROW_EXT, ARROW_MID);
        ImageButton rightButton = stage.addImageButton(
                new Texture(pixmap), // disposed by stored ref
                GameWorld.VIRTUAL_WIDTH - (float) ARROW_EXT, GameWorld.VIRTUAL_HEIGHT / 2.0f,
                InGameMenu.ButtonEventHandler.EVENT_RIGHT);
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

    private int saveSelIndex = -1; // hackish ... get Action to work

    /**
     * handle UI
     */
    private void handleUI() {
        /*
        update animations (only moves if startpoint!=endpoint)
         */
        // update Logo
        ModelComponent modelCompLogo = logoEntity.getComponent(ModelComponent.class);
        modelCompLogo.modelInst.transform.getTranslation(logoPositionVec);
        // update Logo Y (decelerates)
        final float kPlogo = 0.10f;
        float errorL = logoPositionVec.y - logoEndPtY;
        if ((logoPositionVec.y > logoEndPtY) && (errorL > kPlogo)) {
            logoPositionVec.y = logoPositionVec.y - (errorL * kPlogo);
        } else {
            logoPositionVec.y = logoEndPtY; // snap to end point
        }
        // update Logo X (accelerates)
        if (logoPositionVec.x < logoEndPtX) {
            // since x could start at zero, an additional summed amount ensures non-zero multiplicand
            logoPositionVec.x = (logoPositionVec.x + 0.01f) * 1.10f;
            modelCompLogo.modelInst.transform.setToTranslation(logoPositionVec);
            // if menu not visible then show it ...
        } else {
            logoPositionVec.x = logoEndPtX; // snap to end point
        }
        modelCompLogo.modelInst.transform.setToTranslation(logoPositionVec);

        // update Cube
        ModelComponent modelCompCube = cubeEntity.getComponent(ModelComponent.class);
        modelCompCube.modelInst.transform.getTranslation(cubePositionVec);

        switch (menuType) {
            default:
            case INVALID:
                createLogoMenu();
                break;

            case LOGO:
                // Cube X (decelerates)
                final float kPcube = 0.10f;
                float errorC = cubeEndPtX - cubePositionVec.x;
                if ((cubePositionVec.x < cubeEndPtX) && (errorC > kPcube)) {
                    cubePositionVec.x = cubePositionVec.x + (errorC * kPcube);
                } else {
                    cubePositionVec.x = cubeEndPtX; // snap title block to end point
                }
                modelCompCube.modelInst.transform.setToTranslation(cubePositionVec);

                if (logoSelectTable.isVisible()) {
                    if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)) {
                        stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, false); // unlatch
                        // stop music
                        if (null != music) {
                            music.stop();
                        }
                        // grab index
                        saveSelIndex = stage.updateMenuSelection();

                        // set the endpoint of block to initiate animation
                        logoEndPtX = LOGO_END_PT_X1;
                        logoEndPtY = LOGO_END_PT_Y1;
                        // setup Action to handle menu transition
                        logoSelectTable.clearActions();
                        logoSelectTable.addAction(
                                Actions.sequence(
                                        Actions.hide(),
                                        Actions.delay(1.0f), // wait for block in position
                                        // block moved off screen in position, enable menu and start logo block animation
                                        new Action() {
                                            public boolean act(float delta) {
                                                switch (saveSelIndex) {
                                                    default:
                                                    case 0: // P1START
                                                        menuType = MenuType.CONFIG;
                                                        createConfigMenu();
                                                        break;
                                                    case 1: // CSETUP
                                                        menuType = MenuType.CONTROLLER;
                                                        createControllerMenu();
                                                        break;
                                                }
                                                return true;
                                            }
                                        }
                                ));
                    }
                }
                break;

            case CONFIG:
                if (stage.getMenuVisibility()) {
                    if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)) {
                        stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, false); // unlatch
                        // set the endpoint of block to initiate animation
                        cubeEndPtX = CUBE_END_PT_X1;
                        cubeEndPtY = CUBE_END_PT_Y1;
                        // grab index
                        saveSelIndex = stage.updateMenuSelection();
                        // setup Action to handle menu transition
                        configMenuTable.clearActions();
                        configMenuTable.addAction(
                                Actions.sequence(
                                        Actions.hide(),
                                        Actions.delay(1.0f), // wait for block in position
                                        // block in position .. proceed with menu transition
                                        new Action() {
                                            public boolean act(float delta) {
                                                switch (saveSelIndex) {
                                                    default:
                                                    case 0: // LEVEL 1
                                                        stage.setMenuVisibility(false);
                                                        stagename = STAGE_1;
                                                        createArmorMenu();
                                                        break;
                                                    case 1: // PASSWORD
                                                        menuType = MenuType.LEVELS;
                                                        stageNamesList = createScreensMenu();
                                                        break;
                                                }
                                                return true;
                                            }
                                        }
                                ));
                    } else if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_Y)) {
                        stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_Y, false); // unlatch
                        // set the endpoint of block to initiate animation
                        cubeEndPtX = CUBE_END_PT_X1;
                        cubeEndPtY = CUBE_END_PT_Y1;
                        // grab index
                        saveSelIndex = stage.updateMenuSelection();
                        // setup Action to handle menu transition
                        configMenuTable.clearActions();
                        configMenuTable.addAction(
                                Actions.sequence(
                                        Actions.hide(),
                                        Actions.delay(1.0f), // wait for block in position
                                        // block in position .. proceed with menu transition
                                        new Action() {
                                            public boolean act(float delta) {
                                                createLogoMenu();
                                                return true;
                                            }
                                        }
                                ));
                    }
                }
                // update cube animation
                if (cubePositionVec.x < cubeEndPtX) {
                    // since x could start at zero, an additional summed amount ensure non-zero multiplicand
                    cubePositionVec.x = (cubePositionVec.x + 0.01f) * 1.10f;
                    modelCompCube.modelInst.transform.setToTranslation(cubePositionVec);
                }
                break;

            case LEVELS:
                int levelIndex = stage.updateMenuSelection();

                if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)
                        && stage.getMenuVisibility()) {
                    stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, false); // unlatch
                    stage.setMenuVisibility(false);
                    stagename = stageNamesList.get(levelIndex);
                    createArmorMenu();
                }
                break;

            case CONTROLLER:
                int ctrsIndex = stage.updateMenuSelection();
                if (stage.getMenuVisibility()) {
                    if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)) {
                        stage.setMenuVisibility(false); // doesn't matter - new Screen
                        GameWorld.getInstance().setControllerMode(ctrsIndex);
                        GameWorld.getInstance().setSceneData(GameWorld.DEFAULT_SCREEN);
                        GameWorld.getInstance().showScreen( /* ScreenEnum screenEnum, Object... params */
                                new LoadingScreen(LoadingScreen.ScreenTypes.SETUP));
                    }
                }
                break;

            case SOUND:
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

                    } else if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_Y)) {

                        stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_Y, false); // unlatch
                        armorSelectTable.setVisible(false);
//                        menuType = MenuType.CONFIG;
                        // reset/remove platform
                    }
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
