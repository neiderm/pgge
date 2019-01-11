package com.mygdx.game.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.characters.ControllerListenerAdapter;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;


class SelectScreen implements Screen {

    private static final int MAXTANKS3_3Z = 3;

    private Engine engine;
    private Controller connectedCtrl;

    private RenderSystem renderSystem; //for invoking removeSystem (dispose)

    private PerspectiveCamera cam;

    private Environment environment;
    private DirectionalShadowLight shadowLight;
    private Vector3 lightDirection = new Vector3(0.5f, -1f, 0f);

    private BitmapFont font;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private static final int GAME_BOX_W = Gdx.graphics.getWidth();
    private static final int GAME_BOX_H = Gdx.graphics.getHeight();

    private Stage stage;

    private Entity platform;

    private Array<Entity> characters = new Array<Entity>();

    private final Vector3 origin = new Vector3(0, 0, 0);

    private Matrix4 pickedTransform;
    private int selectedIndex;

    // position them into equilateral triangle (sin/cos)
    private Vector3[] positions = new Vector3[]{
/*
            new Vector3(0, 0, 1),
            new Vector3(-0.866f, 0, -0.5f),
            new Vector3(0.866f, 0, -0.5f)
            */
            new Vector3(),
            new Vector3(),
            new Vector3()
    };
    private float[] orientationOffsets =
            new float[]{0 * PLATFRM_INC_DEGREES, 1 * PLATFRM_INC_DEGREES, 2 * PLATFRM_INC_DEGREES};

    SelectScreen() {

        engine = new Engine();

        environment = new Environment();
        environment.set(
                new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));

        cam = new PerspectiveCamera(67, GAME_BOX_W, GAME_BOX_H);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        // Font files from ashley-superjumper
        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(1.0f);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();


        if (null != shadowLight)  // if this is a new round but not new gamescreen
            environment.remove(shadowLight);
        shadowLight = new DirectionalShadowLight(1024, 1024, 120, 120, 1f, 300);
        shadowLight.set(0.8f, 0.8f, 0.8f, lightDirection);
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;

        engine.addSystem(renderSystem = new RenderSystem(shadowLight, environment, cam));


// build the platform moanually (not from data file) for simplicity of retrieving entity
//        platform = PrimitivesBuilder.getCylinderBuilder().create(0, new Vector3(0, 10, -5), new Vector3(4, 1, 4));
        platform = PrimitivesBuilder.getBoxBuilder().create(0, null, new Vector3(4, 0.5f, 4));
        engine.addEntity(platform);
        ModelInstanceEx.setColorAttribute(platform.getComponent(ModelComponent.class).modelInst, Color.GOLD, 0.1f);
        platform.getComponent(ModelComponent.class).modelInst.transform.setTranslation(origin);
///*
        Entity asdf = PrimitivesBuilder.getBoxBuilder().create(0, null, new Vector3(.1f, .1f, .1f));
        engine.addEntity(asdf);
        ModelInstanceEx.setColorAttribute(asdf.getComponent(ModelComponent.class).modelInst, Color.PURPLE, 1f);
        asdf.getComponent(ModelComponent.class).modelInst.transform.setTranslation(new Vector3(0, 0.25f, 0));
        //*/

        GameWorld.sceneLoader.buildCharacters(
                characters, engine, "tanks", true, false);

        GameWorld.sceneLoader.buildArena(engine);

        Controllers.addListener(controllerListener);

        // If a controller is connected, find it and grab a link to it
        int i = 0;
        for (Controller c : Controllers.getControllers()) {
            Gdx.app.log("SelectScreen", "#" + i++ + ": " + c.getName());
            connectedCtrl = c;
            // save index i for later ref?
        }

        stage = new Stage();
        stage.addListener(new InputListener() {
                              @Override
                              public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                  Gdx.app.log("SelectScreen", "(TouchDown) x= " + x + " y= " + y);
// work around troubles with dectecting touch Down vs touch Down+swipe
                                  if (y < 100)
                                      setKeyDown(KEY_ANY);

                                  return true; // must return true in order for touch up, dragged to work!
                              }
                              @Override
                              public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                  touchPadDx = 0;
                              }
                          }
        );

        // ok so you can add a label to the stage
        Label label = new Label("Pick Your Rig ... ", new Label.LabelStyle(font, Color.WHITE));
        stage.addActor(label);

        // point the camera to platform
        final Vector3 camPosition = new Vector3(0, 2, 1.5f); // final Vector3 camPosition = new Vector3(0f, 1, 3);
        final Vector3 camLookAt = new Vector3(0, 0, 0);

        cam.position.set(camPosition);
        cam.lookAt(camLookAt);
        cam.up.set(0, 1, 0); // googling ... Beginning Java Game Development with LibGDX ... lookAt may have undesired result of tilting camera left or right
        cam.update();

        Gdx.input.setInputProcessor(stage);


        // TODO: rotate to arbitrary initial orientation but positions[] is not update
        selectedIndex = 0;

        pickedTransform = characters.get(selectedIndex).getComponent(ModelComponent.class).modelInst.transform;

        // make sure to initialize in case user does not rotate the selector platform
        GameWorld.getInstance().setPlayerObjectName(characters.get(selectedIndex).getComponent(PickRayComponent.class).objectName); // whatever

        updateTanks(orientationOffsets[selectedIndex]);
    }


    /*
     * keep the reference so the listener can be removed at dispose()
     */
    private final ControllerListenerAdapter controllerListener = new ControllerListenerAdapter() {
        @Override
        public boolean buttonDown(Controller controller, int buttonIndex) {
            setKeyDown(KEY_ANY);
            return false;
        }
    };


    @Override
    public void show() {
        // empty
    }


    private int touchPadDx;

    /*
     * "virtual dPad" provider (only cares about left/right)
     */
    private int getDpad() {

        int dPadXaxis = 0;
        PovDirection povDir;

        if (null != connectedCtrl) {
            povDir = connectedCtrl.getPov(0); // povCode ...

            if (PovDirection.east == povDir)
                dPadXaxis = 1;
            else if (PovDirection.west == povDir)
                dPadXaxis = -1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dPadXaxis = -1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dPadXaxis = 1;
        }

        if (Gdx.input.isTouched()) {
            // make sure not in a swipe event already
            if (0 == touchPadDx) {

                touchPadDx = Gdx.input.getDeltaX();

                if (touchPadDx < -1) {
                    dPadXaxis = -1;
                } else if (touchPadDx > 1) {
                    dPadXaxis = 1;
                }
            }
        }

        return dPadXaxis;
    }

    private int getKeyDown() {

        int rv = keyDown;

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.RIGHT) ||
                Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            rv = 0; // "d-pad" ... no-op
        } else if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            rv = KEY_BACK;
        } else if (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)) {
            rv = KEY_ANY;
        }
        keyDown = 0; // unlatch the input state
        return rv;
    }

    private void setKeyDown(int keyDown) {

        this.keyDown = keyDown;
    }

    private int keyDown;
    private static final int KEY_ANY = 1;
    private static final int KEY_BACK = -1;

    // tmp for the pick marker
    private GfxUtil gfxLine = new GfxUtil();
    private Vector3 tmpV = new Vector3();
    private Matrix4 tmpM = new Matrix4();
    private Quaternion rotation = new Quaternion();
    private Vector3 down = new Vector3();
    private float degreesInst;
    private float degreesSetp;
    private float degreesStep; // ramp rate limiting
    private float platformAngularDirection;
    private static final float PLATFRM_INC_DEGREES = 120f;


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


    private void updateTanks(float rotationDegrees) {

        for (int n = 0; n < MAXTANKS3_3Z; n++) {

            Vector3 position = positions[n]; // not actually using the position values out of here right now

            double rads = Math.toRadians(rotationDegrees + orientationOffsets[n]);

            position.x = (float) Math.cos(rads);
            position.y = positions[n].y;
            position.z = (float) Math.sin(rads);

            Entity e = characters.get(n);

            e.getComponent(ModelComponent.class).modelInst.transform.setToTranslation(0, 0, 0);
            e.getComponent(ModelComponent.class).modelInst.transform.setToRotation(down.set(0, 1, 0), 360 - rotationDegrees);
            e.getComponent(ModelComponent.class).modelInst.transform.trn(position);
            e.getComponent(ModelComponent.class).modelInst.transform.trn(origin);
        }
    }

    private void updatePlatform(float rotationDegrees) {

        platform.getComponent(ModelComponent.class).modelInst.transform.setToRotation(down.set(0, 1, 0), 360 - rotationDegrees);
        platform.getComponent(ModelComponent.class).modelInst.transform.trn(origin);
    }

    /*
     * https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
     * "Note that using a StringBuilder is highly recommended against string concatenation in your
     * render method. The StringBuilder will create less garbage, causing almost no hick-ups due to garbage collection."
     */
    @Override
    public void render(float delta) {
///*
        updateRotation();
        updateTanks(degreesInst);
        updatePlatform(degreesInst);
//*/
        if (null != pickedTransform) {
            RenderSystem.debugGraphics.add(gfxLine.line(pickedTransform.getTranslation(tmpV),
                    ModelInstanceEx.rotateRad(down.set(0, 1, 0), tmpM.getRotation(rotation)),
                    Color.RED));
        }

        // game box viewport
        Gdx.gl.glViewport(0, 0, GAME_BOX_W, GAME_BOX_H);
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        engine.update(delta);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();


        float tmp = getDpad();
        // active on "key down" not key up
        if (0 != tmp) {
            // "key down
            if (0 == platformAngularDirection) {
                // not set so increment the degrees
                platformAngularDirection = -tmp;
                degreesSetp += PLATFRM_INC_DEGREES * platformAngularDirection;

                // cycle thru position 0 1 2 etc.
                int saveSelectIndex = selectedIndex;
                selectedIndex += tmp;
                if (selectedIndex >= MAXTANKS3_3Z)
                    selectedIndex = 0;
                else if (selectedIndex < 0)
                    selectedIndex = MAXTANKS3_3Z - 1;

                // lower previous and raise current selection
//                pickedTransform = characters.get(saveSelectIndex).getComponent(ModelComponent.class).modelInst.transform;
//                pickedTransform.trn(down.set(0, -0.5f, 0));
                pickedTransform = characters.get(selectedIndex).getComponent(ModelComponent.class).modelInst.transform;
//                pickedTransform.trn(down.set(0, 0.5f, 0));
            }
        } else {
            // "key up" ... release the latch-out
            platformAngularDirection = 0;
        }

        int keyState = getKeyDown();
        if (KEY_BACK == keyState) {
            GameWorld.getInstance().showScreen(new MainMenuScreen());
        } else if (KEY_ANY == keyState) { // mapper.buttonGet(InputStruct.ButtonsEnum.BUTTON_1);
            if (
                    null != pickedTransform                  // tmp, hack, using this as a stupid flag to indicate wether or not a tank has been picked
                    ) {
                GameWorld.getInstance().setPlayerObjectName(characters.get(selectedIndex).getComponent(PickRayComponent.class).objectName); // whatever
                GameWorld.getInstance().showScreen(new LoadingScreen("GameData.json"));
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

        Controllers.removeListener(controllerListener);
        engine.removeSystem(renderSystem); // make the system dispose its stuff
        engine.removeAllEntities(); // allow listeners to be called (for disposal)

        font.dispose();
        batch.dispose();
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
