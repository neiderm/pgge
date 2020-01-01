/*
 * Copyright (c) 2019 Glenn Neidermeier
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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.ModelGroup;
import com.mygdx.game.sceneLoader.SceneData;
import com.mygdx.game.sceneLoader.SceneLoader;


/*
 * crudely knock together the revolving rig selector platform thingy (intend short load time!)
 * There is no scene graph here, and Bullet physics is not used either. so it's just raw math to
 * revolve and push things around in the  3D world. Intend to man up and use a real math and
 * transform for object positions. (Right now it's just manipulating X/Z "2 1/2 D" by sin/cos).
 * Like to  have a catchy "revolve the whole thing into place" animation using true 3D.
 */
class SelectScreen  extends BaseScreenWithAssetsEngine {

    private static final int N_SELECTIONS = 3;

    private Texture gsTexture;

    private BitmapFont font;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private InGameMenu stage;

    private Entity platform;
    private ImmutableArray<Entity> characters;
    private final Vector3 originCoordinate = new Vector3(0, 0, 0);
    private int idxCurSel;
    private final float platformHt = 0.2f; // tmp
    private int touchPadDx; // globalized for "debouncing" swipe event
    private int dPadYaxis;

    // position them into equilateral triangle (sin/cos)
    private Vector3[] positions = new Vector3[]{
            new Vector3(),
            new Vector3(),
            new Vector3()
    };

    private static Array<String> stageNamesList = new Array<String>();
    private final String SCREENS_DIR = "screens/";
    private boolean isPaused;


    @Override
    public void show() {

        super.init();

        characters = engine.getEntitiesFor(Family.all(CharacterComponent.class).get());

        GameFeature f = GameWorld.getInstance().getFeature(SceneData.LOCAL_PLAYER_FNAME);

        if (null != f) {
            platform = f.getEntity();
        }

        stage = new InGameMenu();
        Gdx.input.setInputProcessor(stage);

        // setup a screen/file selection menu (development only)
//        FileHandle[] files = Gdx.files.local(SCREENS_DIR).list();
        FileHandle[] files = Gdx.files.internal(SCREENS_DIR).list();

        for (FileHandle file: files) {
            // stick the base-name (no path, no extension) into the menu
            String fname = file.name();
            if (fname.matches("(.*).json($)")) {
                String basename = fname.replaceAll(".json$", "");
                stage.addButton(basename, "toggle");
// specify the path+name for now until all screeen jsons are migrated to Screens_Dir
                stageNamesList.add(SCREENS_DIR + fname);
            }
        }
        stage.addNextButton();
        stage.onscreenMenuTbl.setVisible( false );


        final int gsBTNwidth = Gdx.graphics.getWidth();
        final int gsBTNheight = Gdx.graphics.getHeight() / 4;
        final int gsBTNx = 0;
        final int gsBTNy = 0;

        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap pixmap = new Pixmap(gsBTNwidth, gsBTNheight, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, .3f);
        pixmap.drawRectangle(0, 0, gsBTNwidth, gsBTNheight);
        gsTexture = new Texture(pixmap);
        stage.addImageButton(gsTexture, gsBTNx, gsBTNy, InputMapper.InputState.INP_NONE);
        pixmap.dispose();


        // Font files from ashley-superjumper
        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(1.0f);

        // ok so you can add a label to the stage
        stage.addActor(
                new Label("Pick Your Rig ... ", new Label.LabelStyle(font, Color.WHITE)) );

        Gdx.input.setInputProcessor(stage);

        degreesSetp = 90 - idxCurSel * PLATFRM_INC_DEGREES;
    }

    /*
     * dPad X axis + touch-swipe (left/right)
     */
    private int getStep() {

        int axis = stage.mapper.getDpad(null).getX();

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
        }  else {
            touchPadDx  = 0; // make sure clear any swipe event in progress
        }

        if (0 == axis) { // if input is inactive
            /* && 0 != dPadYaxis */ /* key is released ... not necessary but for debugging */
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

    // tmp for the pick marker
    private Vector3 down = new Vector3();
    private float degreesInst; // instantaneous commanded rotation of platform
    private float degreesSetp; // demanded rotation of platform
    private float degreesStep; // magnitude of control output (can be ramp rate limited by control algo)
    private static final float PLATFRM_INC_DEGREES = 360.0f / N_SELECTIONS;


    /*
     * steps to the current orientation by applying proportional control with ramp-up
     */
    private void updateRotation() {

        float step = (degreesSetp - degreesInst) * 0.2f; // error * kP

        // I think we will have a discrete flag to 1) only show the marker line when locked to position and 2) only allow enter/select when in position
        if (Math.abs(step) > 0.01) { // deadband around control point, Q-A-D lock to the setpoint and suppress ringing, normally done by integral term
            if (Math.abs(degreesStep) < 2) { // output is ramped up from 0 to this value, after which 100% of step is accepted
                int sign = degreesStep < 0 ? -1 : 1;
                degreesStep += 0.1f * sign;
            }
            degreesInst += step;
        } else {
            degreesInst = degreesSetp;
            degreesStep = 0;
        }
    }


    private static final int TANK_MODEL_ORIENTATION = 90; // fixed amount to get the model pointing toward the viewer when selected

    /*
     * platformDegrees: currently commanded (absolute) orientation of platform
     */
    private void updateTanks(float platformDegrees) {

        for (int n = 0; n < N_SELECTIONS; n++) {

            // angular offset of unit to position it relative to platform
            float positionDegrees = PLATFRM_INC_DEGREES * n;

            // final rotation of unit is Platform Degrees plus angular rotation to orient unit relative to platform
            float orientionDegrees = positionDegrees - platformDegrees - TANK_MODEL_ORIENTATION;

            Vector3 position = positions[n]; // not actually using the position[] values right now

            // add Platform Degrees to the unit angular position on platform
            double rads = Math.toRadians(positionDegrees + platformDegrees); // distribute number of vehicles around a circle

            position.x = (float) Math.cos(rads);
            position.y = 0.5f // tmp
                    + platformHt / 2; // arbitrary amount above platform
            position.z = (float) Math.sin(rads);

            Entity e = characters.get(n);

            Matrix4 transform = e.getComponent(ModelComponent.class).modelInst.transform;
            transform.setToTranslation(0, 0, 0);
            transform.setToRotation(down.set(0, 1, 0), positionDegrees + orientionDegrees);
            transform.trn(position);
            transform.trn(originCoordinate);

            if (idxCurSel == n) { // raise selected for arbitrary effect ;)
                transform.trn(down.set(0, 0.2f, 0));
            }
        }
    }

    private void updatePlatform(float platformDegrees) {

        Matrix4 transform = platform.getComponent(ModelComponent.class).modelInst.transform;
        transform.setToRotation(down.set(0, 1, 0), 360 - platformDegrees);
        transform.setTranslation(new Vector3(originCoordinate));
        transform.trn(0, -0.1f, 0); // arbitrary additional trn() of platform for no real reason
    }

    private int checkedUpDown(int step, int checkedIndex) {

        int selectedIndex = checkedIndex;

//        if (0 == previousIncrement)   // ... alternative to debouncing?) ... can't hurt ;)
        selectedIndex += step;

//        previousIncrement = step;

        if (selectedIndex >= N_SELECTIONS) {
            selectedIndex = 0;
        }
        else if (selectedIndex < 0) {
            selectedIndex = N_SELECTIONS - 1;
        }

        return selectedIndex;
    }

    private Screen newLoadingScreen(String path) {
        // show loading bar on this screen? omit LoadingScreen? allowing the
        // next (gameScreen) to be instantiated, and thus it's data store available to set parameters etc.
        // Can have a "generic" pass-off ... each screen as closed sets parameters in next screens data.
// Next screen i.e. Loading screen, knows it needs to pass certain data (againi, i.e. player name)
// So the screen may actually own and instance the scene data, not the sceene loader.
        // screen pass sceneData to scene loader as parameter.

        SceneData sd = GameWorld.getInstance().getSceneData();
        ModelGroup mg = sd.modelGroups.get("Characters");        // Magik string
        GameObject go = mg.getElement(idxCurSel); // first 3 Characters are on the platform - use currently selected index to retrieve

        GameWorld.getInstance().setSceneData(path, go.objectName);

        return new LoadingScreen();
    }

    /*
     * https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
     * "Note that using a StringBuilder is highly recommended against string concatenation in your
     * render method. The StringBuilder will create less garbage, causing almost no hick-ups due to garbage collection."
     */
    @Override
    public void render(float delta) {

        updateRotation();
        updateTanks(degreesInst);
        updatePlatform(degreesInst);

        // plots debug graphics
        super.render(delta);


        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (isPaused){
            int idxCurSel = stage.checkedUpDown(stage.mapper.getDpad(null).getY());
            stage.setCheckedBox(idxCurSel);

            InputMapper.InputState inp = stage.mapper.getInputState();
            if (InputMapper.InputState.INP_A == inp) {

                if (stageNamesList.size > 0){
                    String stagename = stageNamesList.get(idxCurSel);
                    GameWorld.getInstance().showScreen(newLoadingScreen(stagename));
                }
            }
            else if (InputMapper.InputState.INP_START == inp) {

                stage.onscreenMenuTbl.setVisible( false );
                isPaused = false;
            }
        } else {
            int step = getStep();
            idxCurSel = checkedUpDown(step, idxCurSel);
            // Necessary to increment the degrees because we are controlling to it like a setpoint (IOW
            // rotating past 360 must not wrap around to o0, it must go to e.g. 480, 600 etc. maybe this is wonky)
            degreesSetp -= PLATFRM_INC_DEGREES * step;   // negated (matches to left/right of car nearest to front of view)

            // lower previous (raised current selection in updateTanks() )
            characters.get(idxCurSel).getComponent(ModelComponent.class).modelInst.transform.trn(
                    down.set(0, -0.5f, 0));

            InputMapper.InputState inputState = stage.mapper.getInputState();

            if (InputMapper.InputState.INP_START == inputState) {
//                GameWorld.getInstance().showScreen(newLoadingScreen("gbr.json")); // LevelOne.json
// setup a screen/file selection menu (development only)
                if (stageNamesList.size > 0) {
                    stage.onscreenMenuTbl.setVisible(true);
                    isPaused = true;
                }
            } else if (InputMapper.InputState.INP_A == inputState) {

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

        font.dispose();
        shapeRenderer.dispose();
        stage.dispose();

        if (null != gsTexture)
            gsTexture.dispose();

        // screens that load assets must calls assetLoader.dispose() !
        super.dispose();
    }

    /*
     * android "back" button sends ApplicationListener.pause(), but then sends ApplicationListener.dispose() !!
     */

    @Override
    public void pause() {
        // Android "Recent apps" (square on-screen button), Android "Home" (middle o.s. btn ... Game.pause()->Screen.pause()
        Gdx.app.log("SelectScreen", "pause");
    }

    @Override
    public void resume() {
        // Android resume from "minimized" (Recent Apps button selected)
    }

    @Override
    public void hide() {
        Gdx.app.log("SelectScreen", "hide");
        dispose();
    }
}