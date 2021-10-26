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
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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

    private final Vector3 originCoordinate = new Vector3(0, 0, 0);
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    // position them into equilateral triangle (sin/cos)
    private final Vector3[] positions = new Vector3[]{
            new Vector3(),
            new Vector3(),
            new Vector3()
    };
    private ArrayList<String> stageNamesList; // needs re-instantiated at each screen invocation
    private ImmutableArray<Entity> characters;
    private InGameMenu stage; // don't instantiate me here ... skips select platform
    private Entity platform;
    private int actorCount;
    private int idxRigSelection;
    private int touchPadDx; // globalized for "debouncing" swipe event
    private int dPadYaxis;
    private Label theLabel;
    private ImageButton leftButton;
    private ImageButton rightButton;
    private _ScreenType screenType;
    private String stagename;
    private Entity logoEntity = null;
    private Entity cubeEntity = null;

    private static final String STATIC_OBJECTS = "InstancedModelMeshes";
    private static final float LOGO_START_PT_Y = 10.0f;//tbd
    private static final float LOGO_END_PT_Y = 0.8f; // see z-dimension of LogoText in cubetest.blend

    private enum _ScreenType {
        TITLE,
        LEVEL,
        ARMOR
    }

    /**
     * bah .. determined more or less by arbitrary order in model info STATIC_OBJECTS of json file
     */
    private enum _ModelNodes {
        LOGO,
        CUBE
    }

    @Override
    public void show() {

        super.init();
        stage = new InGameMenu();
        Gdx.input.setInputProcessor(stage);

        characters = engine.getEntitiesFor(Family.all(CharacterComponent.class).get());
        actorCount = characters.size(); // should be 3!@!!!!
        screenType = _ScreenType.TITLE;

        // hide the armor units (translate several units on the Y axis)
        for (Entity e : characters) {
            Matrix4 transform = e.getComponent(ModelComponent.class).modelInst.transform;
            transform.setToTranslation(0, 10, 0);
        }

        GameFeature f = GameWorld.getInstance().getFeature(GameWorld.LOCAL_PLAYER_FNAME);
        if (null != f) {
            platform = f.getEntity();
        }

        // setup a screen/file selection menu (development only)
        FileHandle[] files = Gdx.files.internal(SCREENS_DIR).list();
        stageNamesList = new ArrayList<>(); //  must be re-instantiated at each screen invocation

        for (FileHandle file : files) {
            // stick the base-name (no path, no extension) into the menu
            String fname = file.name();
            if (fname.matches("(.*).json($)")) {
                String basename = fname.replaceAll(".json$", "");
                stageNamesList.add(basename);
            }
        }
        stage.createMenu(null, stageNamesList.toArray(new String[0]));

        // disposables
        Pixmap pixmap;
        Texture texture; // AddImageButton() keeps the reference for disposal
        Color theColor = new Color(0, 1.0f, 0, 0.5f);

        stage.addTextButton("Next", theColor, InGameMenu.ButtonEventHandler.EVENT_A);

        final int ARROW_EXT = 64; // extent of arrow tile (height/width)
        final int ARROW_MID = ARROW_EXT / 2;

        pixmap = new Pixmap(ARROW_EXT, ARROW_EXT, Pixmap.Format.RGBA8888);
        pixmap.setColor(theColor);
        pixmap.fillTriangle(0, ARROW_MID, ARROW_EXT, ARROW_EXT, ARROW_EXT, 0);
        texture = new Texture(pixmap);

        leftButton = stage.addImageButton(texture,
                0, GameWorld.VIRTUAL_HEIGHT / 2.0f, InGameMenu.ButtonEventHandler.EVENT_LEFT);
        pixmap.dispose();

        pixmap = new Pixmap(ARROW_EXT, ARROW_EXT, Pixmap.Format.RGBA8888);
        pixmap.setColor(theColor);
        pixmap.fillTriangle(0, 0, 0, ARROW_EXT, ARROW_EXT, ARROW_MID);
        texture = new Texture(pixmap);

        rightButton = stage.addImageButton(texture,
                GameWorld.VIRTUAL_WIDTH - (float) ARROW_EXT,
                GameWorld.VIRTUAL_HEIGHT / 2.0f, InGameMenu.ButtonEventHandler.EVENT_RIGHT);

        pixmap.dispose();

        leftButton.setVisible(false);
        rightButton.setVisible(false);

        theLabel = new Label("I need aligned!", stage.uiSkin);
        stage.addActor(theLabel);
        theLabel.setVisible(false);

        // grab a handle to selected entities
        SceneData sd = GameWorld.getInstance().getSceneData();

        for (Entity e : engine.getEntitiesFor(Family.all(ModelComponent.class).get())) {
            ModelComponent modelComponent = e.getComponent(ModelComponent.class);
            Node nnn = modelComponent.modelInst.nodes.get(0);

            GameObject gameObject;

            gameObject = sd.modelGroups.get(STATIC_OBJECTS).getElement(_ModelNodes.LOGO.ordinal());
            if (nnn.id.equals(gameObject.objectName)) {
                logoEntity = e;
            }
            gameObject = sd.modelGroups.get(STATIC_OBJECTS).getElement(_ModelNodes.CUBE.ordinal());
            if (nnn.id.equals(gameObject.objectName)) {
                cubeEntity = e;
            }
        }
        // position title text out of view
        ModelComponent modelComponent = logoEntity.getComponent(ModelComponent.class);
        modelComponent.modelInst.transform.setToTranslation(0, LOGO_START_PT_Y, 0);

        degreesSetp = 90 - idxRigSelection * platformIncDegrees();
        degreesInst = degreesSetp; // no movement at screen start
        degreesInst = 0; // does 90 degree rotation
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

        if (Math.abs(step) > 0.01f) { // deadband around control point, Q-A-D lock to the setpoint and suppress ringing, normally done by integral term
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

    private float platformIncDegrees() {
        return (360.0f / actorCount);
    }

    /*
     * platformDegrees: currently commanded (absolute) orientation of platform
     */
    private void updatePlatform(float platformDegrees) {

        // fixed amount to get the model pointing toward the viewer when selected
        final int ROTATE_FORWARD_ANGLE = 90;
        // Rigs are positioned in terms of offset from Platform origin
        // Platform height is buried in Selectscreen unfortunately ("PlayerIsPlatform")
        final float PLATFORM_HEIGHT = 0.2f;
        final float SELECTED_RIG_OFFS_Y = 0.3f;
        final float UNSELECTED_RIG_OFFS_Y = 0.05f;
        // scalar applies to x/y (cos/sin) terms to "push" the Rigs slightly out from the origin
        final float RIG_PLACEMENT_RADIUS_SCALAR = 1.1f;

        updateRotation();

        for (int n = 0; n < actorCount; n++) {

            // angular offset of unit to position it relative to platform
            float positionDegrees = platformIncDegrees() * n;

            // final rotation of unit is Platform Degrees plus angular rotation to orient unit relative to platform
            float orientionDegrees = positionDegrees - platformDegrees - ROTATE_FORWARD_ANGLE;

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
    private Vector3 logoPositionVec = new Vector3(); // tmp vector for reuse
    private Vector3 cubePositionVec = new Vector3(); // tmp vector for reuse

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

        ModelComponent modelCompLogo = logoEntity.getComponent(ModelComponent.class);
        modelCompLogo.modelInst.transform.getTranslation(logoPositionVec);

        ModelComponent modelCompCube = cubeEntity.getComponent(ModelComponent.class);
        modelCompCube.modelInst.transform.getTranslation(cubePositionVec);

        switch (screenType) {
            default:
            case TITLE:
                // swipe-in the logo text block ...
                float error = logoPositionVec.y - LOGO_END_PT_Y;
                final float kP = 0.10f;
                final float THR = 0.001f * (LOGO_START_PT_Y - LOGO_END_PT_Y);
                // /* positionVector.y > LOGO_END_PT_Y */  .. simpler?
                if (error > THR) {
                    logoPositionVec.y = logoPositionVec.y - (error * kP);
                } else {
                    // once title text in place ...
                    logoPositionVec.y = LOGO_END_PT_Y;
                    //enable Next button

                }
                modelCompLogo.modelInst.transform.setToTranslation(logoPositionVec);

                if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)) {
                    stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, false); // hmmm debounce me
                    screenType = _ScreenType.LEVEL;
                    stagename = "vr_zone"; // set the default
                }
                break;

            case LEVEL:
                idxMenuSelection = stage.setCheckedBox();
                // hide title text
                if (logoPositionVec.x < 10) {
                    // since x could start at zero, an additional summed amount ensure non-zero multiplicand
                    logoPositionVec.x = (logoPositionVec.x + 0.01f) * 1.10f;
                    modelCompLogo.modelInst.transform.setToTranslation(logoPositionVec);
                } else {
                    // once title text in place ...
//                        if (!stageNamesList.isEmpty()) {
                    stage.setMenuVisibility(true);
                    theLabel.setText("Select a mission");
                    theLabel.setVisible(true);
                    // enable next button ...

                }
                if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)) {
                    stage.mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, false); // hmmm debounce me

                    if (!stageNamesList.isEmpty()) {
                        stagename = stageNamesList.get(idxMenuSelection);
                        stage.setMenuVisibility(false);
                        screenType = _ScreenType.ARMOR;
                        leftButton.setVisible(true);
                        rightButton.setVisible(true);
                        theLabel.setText("Select Armor Unit");
                        theLabel.setVisible(true);
                    } else {
                        Gdx.app.log(CLASS_STRING, "No screen files found!");
                    }
                } else if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_START)) {
                    stage.setMenuVisibility(false);
                }
                break;

            case ARMOR:
                // hide cube
                if (cubePositionVec.x < 20) {
                    // since x could start at zero, an additional summed amount ensure non-zero multiplicand
                    cubePositionVec.x = (+1) *((cubePositionVec.x + 0.001f) * 1.10f);
                    modelCompCube.modelInst.transform.setToTranslation(cubePositionVec);
                } else {
                    // enable next button ...

                }
                updatePlatform(degreesInst);
                int step = getStep();
                idxRigSelection = checkedUpDown(step, idxRigSelection);
                // Necessary to increment the degrees because we are controlling to it like a setpoint
                // rotating past 360 must not wrap around to o0, it must go to e.g. 480, 600 etc. maybe this is wonky)
                degreesSetp -= platformIncDegrees() * step;   // negated (matches to left/right of object nearest to front of view)

                if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)) {
                    GameWorld.getInstance().showScreen(newLoadingScreen(SCREENS_DIR + stagename + DOT_JSON));
                }
                break;
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
