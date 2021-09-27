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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.ModelGroup;
import com.mygdx.game.sceneLoader.ModelInfo;
import com.mygdx.game.sceneLoader.SceneData;

/*
 * Player selects the Rig from a revolving platform.
 * There is no scene graph here, and Bullet physics is not used either. so it's just raw math to
 * revolve and push things around in the  3D world. Intend to man up and use a real math and
 * transform for object positions. (Right now it's just manipulating X/Z "2 1/2 D" by sin/cos).
 */
class SelectScreen extends BaseScreenWithAssetsEngine {

    private static final String CLASS_STRING = "SelectScreen";
    private static final Array<String> stageNamesList = new Array<>();

    private final InGameMenu stage = new InGameMenu(); // disposable

    private final Vector3 originCoordinate = new Vector3(0, 0, 0);
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    // position them into equilateral triangle (sin/cos)
    private final Vector3[] positions = new Vector3[]{
            new Vector3(),
            new Vector3(),
            new Vector3()
    };
    private ImmutableArray<Entity> characters;
    private Entity platform;
    private int actorCount = 0;
    private int idxRigSelection;
    private int touchPadDx; // globalized for "debouncing" swipe event
    private int dPadYaxis;
    private boolean isPaused;

    @Override
    public void show() {

        super.init();

        characters = engine.getEntitiesFor(Family.all(CharacterComponent.class).get());
        actorCount = characters.size(); // should be 3!@!!!!

        GameFeature f = GameWorld.getInstance().getFeature(GameWorld.LOCAL_PLAYER_FNAME);

        if (null != f) {
            platform = f.getEntity();
        }

        // setup a screen/file selection menu (development only)
//        FileHandle[] files = Gdx.files.local(SCREENS_DIR).list();
        final String SCREENS_DIR = "screens/";
        FileHandle[] files = Gdx.files.internal(SCREENS_DIR).list();

        for (FileHandle file : files) {
            // stick the base-name (no path, no extension) into the menu
            String fname = file.name();
            if (fname.matches("(.*).json($)")) {
                String basename = fname.replaceAll(".json$", "");
                stage.addToggleButton(basename);
// specify the path+name for now until all screeen jsons are migrated to Screens_Dir
                stageNamesList.add(SCREENS_DIR + fname);
            }
        }
        stage.addNextButton();
        stage.onscreenMenuTbl.setVisible(false);
        Gdx.input.setInputProcessor(stage);

        // disposables
        Pixmap pixmap;
        Texture texture; // AddImageButton() keeps the reference for disposal
        Color theColor = new Color(0, 1.0f, 0, 0.5f);

        int nextbtnW = GameWorld.VIRTUAL_WIDTH / 4;
        int nextbtnH = GameWorld.VIRTUAL_HEIGHT / 4;

        pixmap = new Pixmap(nextbtnW, nextbtnH, Pixmap.Format.RGBA8888);
        pixmap.setColor(theColor);
        pixmap.fillRectangle(0, 0, nextbtnW, nextbtnH);
        texture = new Texture(pixmap);
        stage.addImageButton(texture,
                (GameWorld.VIRTUAL_WIDTH / 2.0f) - (GameWorld.VIRTUAL_WIDTH / 8.0f), 0,
                InGameMenu.ButtonEventHandler.EVENT_A);
        pixmap.dispose();

        Button button2 = new TextButton("Next", stage.uiSkin, "default");
        button2.setSize(nextbtnW, nextbtnH);
        button2.setPosition((GameWorld.VIRTUAL_WIDTH / 2.0f) - (GameWorld.VIRTUAL_WIDTH / 8.0f), 0);
        button2.setColor(theColor);
        button2.addListener(new InputListener() {

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
        stage.addActor(button2);


        final int ARROW_EXT = 64; // extent of arrow tile (height/width)
        final int ARROW_MID = ARROW_EXT / 2;

        pixmap = new Pixmap(ARROW_EXT, ARROW_EXT, Pixmap.Format.RGBA8888);
        pixmap.setColor(theColor);
        pixmap.fillTriangle(0, ARROW_MID, ARROW_EXT, ARROW_EXT, ARROW_EXT, 0);
        texture = new Texture(pixmap);
        stage.addImageButton(texture,
                0, GameWorld.VIRTUAL_HEIGHT / 2.0f, InGameMenu.ButtonEventHandler.EVENT_LEFT);
        //pixmap.dispose();

        pixmap = new Pixmap(ARROW_EXT, ARROW_EXT, Pixmap.Format.RGBA8888);
        pixmap.setColor(theColor);
        pixmap.fillTriangle(0, 0, 0, ARROW_EXT, ARROW_EXT, ARROW_MID);
        texture = new Texture(pixmap);
        stage.addImageButton(texture,
                GameWorld.VIRTUAL_WIDTH - (float) ARROW_EXT,
                GameWorld.VIRTUAL_HEIGHT / 2.0f, InGameMenu.ButtonEventHandler.EVENT_RIGHT);

        pixmap.dispose();

        stage.addLabel("Choose your Rig ... ", Color.WHITE);

        degreesSetp = 90 - idxRigSelection * platformIncDegrees();
    }

    /*
     * dPad X axis + touch-swipe (left/right)
     */
    private int getStep() {

        int axis = stage.mapper.getAxisI(InputMapper.VIRTUAL_AD_AXIS);

        if (Gdx.input.isTouched()) {
            // make sure not in a swipe event already
            if (0 == touchPadDx) {
                touchPadDx = Gdx.input.getDeltaX();

                if (touchPadDx < -1) {
                    axis = -1;
                } else if (touchPadDx > 1) {
                    axis = 1;
                }
            }
        } else {
            touchPadDx = 0; // clear any swipe event in progress
        }

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

    private final Vector3 down = new Vector3();
    private float degreesInst; // instantaneous commanded rotation of platform
    private float degreesSetp; // demanded rotation of platform
    private float degreesStep; // magnitude of control output (can be ramp rate limited by control algo)

    /*
     * steps to the current orientation by applying proportional control with ramp-up
     */
    private void updateRotation() {

        float kP = 0.2f;
        float step = (degreesSetp - degreesInst) * kP; // step = error * kP

        if (Math.abs(step) > 0.01) { // deadband around control point, Q-A-D lock to the setpoint and suppress ringing, normally done by integral term
            if (Math.abs(degreesStep) < 2) { // output is ramped up from 0 to this value, after which 100% of step is accepted
                int sign = (degreesStep < 0) ? -1 : 1;
                degreesStep += 0.1f * sign;
            }
            degreesInst += step;
        } else {
            degreesInst = degreesSetp;
            degreesStep = 0;
        }
    }

    // fixed amount to get the model pointing toward the viewer when selected
    private static final int TANK_MODEL_ORIENTATION = 90;

    // Rigs are positioned in terms of offset from Platform origin
    // Platform height is buried in selectscreen.json unfortunately ("PlayerIsPlatform")
    private static final float PLATFORM_HEIGHT = 0.2f;

    private static final float SELECTED_RIG_OFFS_Y = 0.3f;
    private static final float UNSELECTED_RIG_OFFS_Y = 0.05f;
    // scalar applies to x/y (cos/sin) terms to "push" the Rigs slightly out from the origin
    private static final float RIG_PLACEMENT_RADIUS_SCALAR = 1.1f;

    private float platformIncDegrees() {
        return (360.0f / actorCount);
    }

    /*
     * platformDegrees: currently commanded (absolute) orientation of platform
     */
    private void updatePlatform(float platformDegrees) {

        updateRotation();

        for (int n = 0; n < actorCount; n++) {

            // angular offset of unit to position it relative to platform
            float positionDegrees = platformIncDegrees() * n;

            // final rotation of unit is Platform Degrees plus angular rotation to orient unit relative to platform
            float orientionDegrees = positionDegrees - platformDegrees - TANK_MODEL_ORIENTATION;

            Vector3 position = positions[n]; // not actually using the position[] values right now

            // add Platform Degrees to the unit angular position on platform
            double rads = Math.toRadians(positionDegrees + platformDegrees); // distribute number of vehicles around a circle

            position.x = (float) Math.cos(rads) * RIG_PLACEMENT_RADIUS_SCALAR;

            position.y = (PLATFORM_HEIGHT / 2) + UNSELECTED_RIG_OFFS_Y; // raise slightly above platform

            position.z = (float) Math.sin(rads) * RIG_PLACEMENT_RADIUS_SCALAR;

            Entity e = characters.get(n);

            Matrix4 transform = e.getComponent(ModelComponent.class).modelInst.transform;
            transform.setToTranslation(0, 0, 0);
            transform.setToRotation(down.set(0, 1, 0), positionDegrees + orientionDegrees);
            transform.trn(position);
            transform.trn(originCoordinate);

            if (idxRigSelection == n) { // raise selected for arbitrary effect ;)
                transform.trn(down.set(0, SELECTED_RIG_OFFS_Y, 0));
            }
        }
        Matrix4 transform = platform.getComponent(ModelComponent.class).modelInst.transform;
        transform.setToRotation(down.set(0, 1, 0), 360 - platformDegrees);
        transform.setTranslation(new Vector3(originCoordinate));
        transform.trn(0, -0.1f, 0); // arbitrary additional trn() of platform for no real reason
    }

    // based on InGameMenu. checkedUpDown()
    private int checkedUpDown(int step, int checkedIndex) {

        int selectedIndex = checkedIndex;
        selectedIndex += step;

        if (selectedIndex >= actorCount) {
            selectedIndex = 0;
        } else if (selectedIndex < 0) {
            selectedIndex = actorCount - 1;
        }
        return selectedIndex;
    }

    private Screen newLoadingScreen(String path) {

        SceneData sd = GameWorld.getInstance().getSceneData();

        ModelGroup mg = sd.modelGroups.get("Characters");
        // first 3 Characters are on the platform - use currently selected index to retrieve
        GameObject go = mg.getElement(idxRigSelection);
        String playerObjectName = go.objectName;

        // When loading from Select Screen, need to distinguish the name of the selected player
        // object by an arbitrary character string to make sure locally added player model info
        // doesn't bump into the user-designated model info sections in the screen json files
        final String PLAYER_OBJECT_TAG = "P0_";
        String playerFeatureName = PLAYER_OBJECT_TAG + playerObjectName;

        ModelInfo selectedModelInfo = null;

        if (null != playerObjectName) {
            // get the player model info from previous scene (which should still be valid)
            selectedModelInfo = sd.modelInfo.get(playerObjectName);
        }

        GameWorld.getInstance().setSceneData(path, playerFeatureName, selectedModelInfo);

        return new LoadingScreen();
    }

    // do not convert me to a local variable ... jdoc tag?
    private int idxMenuSelection;

    /*
     * https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
     * "Note that using a StringBuilder is highly recommended against string concatenation in your
     * render method. The StringBuilder will create less garbage, causing almost no hick-ups due to garbage collection."
     */
    @Override
    public void render(float delta) {
        // plots debug graphics
        super.render(delta);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        updatePlatform(degreesInst);

        if (isPaused) {
            idxMenuSelection = stage.setCheckedBox();

            if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)) {
                if (stageNamesList.size > 0) {
                    String stagename = stageNamesList.get(idxMenuSelection);
                    GameWorld.getInstance().showScreen(newLoadingScreen(stagename));
                }
            } else if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_START, false, 30)) {
                stage.onscreenMenuTbl.setVisible(false);
                isPaused = false;
            }
        } else {
            int step = getStep();
            idxRigSelection = checkedUpDown(step, idxRigSelection);
            // Necessary to increment the degrees because we are controlling to it like a setpoint
            // rotating past 360 must not wrap around to o0, it must go to e.g. 480, 600 etc. maybe this is wonky)
            degreesSetp -= platformIncDegrees() * step;   // negated (matches to left/right of object nearest to front of view)

            if ((stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_START, false, 60))) {
                if (stageNamesList.size > 0) {
                    stage.onscreenMenuTbl.setVisible(true);
                    isPaused = true;
                } else {
                    Gdx.app.log(CLASS_STRING, "No screen files found!");
                }
            } else if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)) {
                GameWorld.getInstance().showScreen(newLoadingScreen("vr_zone.json")); // LevelOne.json
            }
        }
    }

    @Override
    public void resize(int width, int height) {
    /*
    https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
    We need to update the stage's viewport in the resize method. The last Boolean argument set the origin to the lower left coordinate, causing the label to be drawn at that location.
     */
// ??? // stage.getViewport().update(width, height, true);
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
