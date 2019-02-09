package com.mygdx.game.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GameWorld;
import com.mygdx.game.characters.InputStruct;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;


/*
 * crudely knock together the revolving rig selector platform thingy (intend short load time!)
 * There is no scene graph here, and Bullet physics is not used either. so it's just raw math to
 * revolve and push things around in the  3D world. Intend to man up and use a real math and
 * transform for object positions. (Right now it's just manipulating X/Z "2 1/2 D" by sin/cos).
 * Like to  have a catchy "revolve the whole thing into place" animation using true 3D.
 */
class SelectScreen implements Screen {

    private static final int N_SELECTIONS = 3;

    private InputStruct mapper = new InputStruct();
    private Engine engine = new Engine();
    private RenderSystem renderSystem; //for invoking removeSystem (dispose)

    private Environment environment;
    private DirectionalShadowLight shadowLight;
    private Vector3 lightDirection = new Vector3(0.5f, -1f, 0f);

    private BitmapFont font;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    private static final int GAME_BOX_W = Gdx.graphics.getWidth();
    private static final int GAME_BOX_H = Gdx.graphics.getHeight();

    private Stage stage;
    private Entity platform;

    private Array<Entity> characters = new Array<Entity>();

    private final Vector3 originCoordinate = new Vector3(0, 0, 0);

    private int idxCurSel;

    private final float yCoordOnPlatform = 0.1f;

    // position them into equilateral triangle (sin/cos)
    private Vector3[] positions = new Vector3[]{
            new Vector3(),
            new Vector3(),
            new Vector3()
    };

    private Vector2 v2 = new Vector2();

    SelectScreen() {

        environment = new Environment();
        environment.set(
                new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));

        PerspectiveCamera cam = new PerspectiveCamera(67, GAME_BOX_W, GAME_BOX_H);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        environment.remove(shadowLight);

        shadowLight = new DirectionalShadowLight(1024, 1024, 120, 120, 1f, 300);
        shadowLight.set(0.8f, 0.8f, 0.8f, lightDirection);
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;

        engine.addSystem(renderSystem = new RenderSystem(shadowLight, environment, cam));

        // point the camera to platform
        final Vector3 camPosition = new Vector3(0, 1.2f, 3f); // ook
        final Vector3 camLookAt = new Vector3(0, 0, 0);

        cam.position.set(camPosition);
        cam.lookAt(camLookAt);
        cam.up.set(0, 1, 0);
        cam.update();

// build the platform moanually (not from data file) for simplicity of retrieving entity
//        platform = PrimitivesBuilder.getCylinderBuilder().create(0, new Vector3(0, 10, -5), new Vector3(4, 1, 4));
        platform = PrimitivesBuilder.getBoxBuilder().create(
                0, null, new Vector3(4, yCoordOnPlatform * 2, 4));
        engine.addEntity(platform);
        ModelInstanceEx.setColorAttribute(platform.getComponent(ModelComponent.class).modelInst, Color.GOLD, 0.1f);

        GameWorld.sceneLoader.buildCharacters(
                characters, engine, "tanks", true, false);

        GameWorld.sceneLoader.buildArena(engine);

        stage = new Stage();
        stage.addListener(new InputListener() {
                              @Override
                              public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                  Gdx.app.log("SelectScreen", "(TouchDown) x= " + x + " y= " + y);
// work around troubles with dectecting touch Down vs touch Down+swipe
                                  if (y < 100)
                                      mapper.setInputState(InputStruct.InputState.INP_SELECT);

                                  return true; // must return true in order for touch up, dragged to work!
                              }
                              @Override
                              public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                  touchPadDx = 0;
                              }
                          });

        // Font files from ashley-superjumper
        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(1.0f);

        // ok so you can add a label to the stage
        Label label = new Label("Pick Your Rig ... ", new Label.LabelStyle(font, Color.WHITE));
        stage.addActor(label);

        Gdx.input.setInputProcessor(stage);


        idxCurSel = 2;

        // make sure to initialize in case user does not rotate the selector platform
        GameWorld.getInstance().setPlayerObjectName(
                characters.get(idxCurSel).getComponent(PickRayComponent.class).objectName); // whatever

// initialize platform rotation setpoint (further updates will be relative to this i.e. plus/minus the platform increment degrees)
        degreesSetp = 90 - idxCurSel * PLATFRM_INC_DEGREES;
// optional: advance immediately to the setpoint
        if (false) {
            degreesInst = degreesSetp;
            updateTanks(idxCurSel * PLATFRM_INC_DEGREES);
        }
    }


    @Override
    public void show() {
        // empty
    }


    private int touchPadDx;
    private int dPadYaxis;

    /*
     * dPad X axis + touch-swipe (left/right)
     */
    private int getStep() {

        int axis = mapper.getDpad(null).getX();

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
    private GfxUtil gfxLine = new GfxUtil();
    private Vector3 tmpV = new Vector3();
    private Matrix4 tmpM = new Matrix4();
    private Quaternion rotation = new Quaternion();
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
            position.y = yCoordOnPlatform; // arbitrary amount above platform
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

        Matrix4 transform;

        transform = platform.getComponent(ModelComponent.class).modelInst.transform;
        transform.setToRotation(down.set(0, 1, 0), 360 - platformDegrees);
        transform.setTranslation(new Vector3(originCoordinate));
        transform.trn(0, -0.1f, 0); // arbitrary additional trn() of platform for no real reason

        // debug graphic
        transform = characters.get(idxCurSel).getComponent(ModelComponent.class).modelInst.transform;
        RenderSystem.debugGraphics.add(gfxLine.line(transform.getTranslation(tmpV),
                ModelInstanceEx.rotateRad(down.set(0, 1, 0), tmpM.getRotation(rotation)),
                Color.RED));
    }


    private int previousIncrement;

    private int checkedUpDown(int step, int checkedIndex){

        int selectedIndex = checkedIndex;

//        if (0 == previousIncrement)   // ... alternative to debouncing?) ... can't hurt ;)
        selectedIndex += step;

        previousIncrement = step;

        if (selectedIndex >= N_SELECTIONS)
            selectedIndex = 0;
        else if (selectedIndex < 0)
            selectedIndex = N_SELECTIONS - 1;


        return selectedIndex;
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

        // game box viewport
        Gdx.gl.glViewport(0, 0, GAME_BOX_W, GAME_BOX_H);
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        engine.update(delta);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();


        int step = getStep();
        idxCurSel = checkedUpDown(step, idxCurSel);
        // Necessary to increment the degrees because we are controlling to it like a setpoint (IOW
        // rotating past 360 must not wrap around to o0, it must go to e.g. 480, 600 etc. maybe this is wonky)
        degreesSetp -= PLATFRM_INC_DEGREES * step;   // negated (matches to left/right of car nearest to front of view)

        // lower previous (raised current selection in updateTanks() )
        characters.get(idxCurSel).getComponent(ModelComponent.class).modelInst.transform.trn(
                    down.set(0, -0.5f, 0));

        InputStruct.InputState inputState = mapper.getInputState();

        if (InputStruct.InputState.INP_ESC == inputState) {

            GameWorld.getInstance().showScreen(new MainMenuScreen());     // presently I'm not sure what should go here

        } else if (InputStruct.InputState.INP_SELECT == inputState) {

//             v2 = mapper.getPointer();

            GameWorld.getInstance().setPlayerObjectName(characters.get(idxCurSel).getComponent(PickRayComponent.class).objectName); // whatever
            GameWorld.getInstance().showScreen(new LoadingScreen("GameData.json"));
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

        engine.removeSystem(renderSystem); // make the system dispose its stuff
        engine.removeAllEntities(); // allow listeners to be called (for disposal)

        font.dispose();
        shapeRenderer.dispose();
        stage.dispose();
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
