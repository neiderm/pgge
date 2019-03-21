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

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.SceneLoader;
import com.mygdx.game.characters.CameraMan;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.controllers.SimpleVehicleModel;
import com.mygdx.game.controllers.SteeringBulletEntity;
import com.mygdx.game.controllers.SteeringEntity;
import com.mygdx.game.controllers.SteeringTankController;
import com.mygdx.game.controllers.TankController;
import com.mygdx.game.controllers.TrackerSB;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.CharacterSystem;
import com.mygdx.game.systems.PickRaySystem;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.systems.StatusSystem;
import com.mygdx.game.util.BulletEntityStatusUpdate;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

import java.util.Locale;

import static com.mygdx.game.util.GameEvent.EventType.RAY_DETECT;
import static com.mygdx.game.util.GameEvent.EventType.RAY_PICK;

/**
 * Created by neiderm on 12/18/17.
 */
class GameScreen extends ScreenAvecAssets {

    private Engine engine;
    private BulletSystem bulletSystem; //for invoking removeSystem (dispose)
    private RenderSystem renderSystem; //for invoking removeSystem (dispose)
    private CameraMan cameraMan;
    private PerspectiveCamera cam;
    private CameraInputController camController; // FirstPersonCameraController camController;
    private BitmapFont font;
    private OrthographicCamera guiCam;
    private SpriteBatch batch = new SpriteBatch();
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Color hudOverlayColor = new Color(1, 0, 0, 0.2f);
    private GameUI playerUI;
    private InputMultiplexer multiplexer;
    private StringBuilder stringBuilder = new StringBuilder();
    private Signal<GameEvent> gameEventSignal = new Signal<GameEvent>();
    private final Vector3 camDefPosition = new Vector3(1.0f, 13.5f, 02f); // hack: position of fixed camera at 'home" location
    private final Vector3 camDefLookAt = new Vector3(1.0f, 10.5f, -5.0f);
    private Entity pickedPlayer;
    private Entity platformEntity;
    private final float colorAlpha = 0.9f;
    private Color platformColor;

    private static final int ALL_HIT_COUNT = 3;
    private static final int ONE_SECOND = 1;
    private static final int ROUND_COMPLETE_FADEOUT_TIME = 3;
    private static final int ROUND_CONTINUE_WAIT_TIME = 10;
    private static final int INITIAL_GAME_TIME = 30 + ROUND_CONTINUE_WAIT_TIME;

    private int gameOverCountDown;
    private int hitCount;
    private boolean textShow = true;
    private String gameOverMessageString;


    GameScreen(SceneLoader assetLoader){

        super(assetLoader);
    }

    private void screenInit(){

        platformColor = new Color(255, 0, 0, colorAlpha);

        multiplexer = new InputMultiplexer(); // make sure get a new one since there will be a new Stage instance ;)

        font = new BitmapFont(Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);

//        font.setColor(1, 1, 1, 0.5f);

        float fontGetDensity = Gdx.graphics.getDensity();

        if (fontGetDensity > 1)
            font.getData().setScale(fontGetDensity);

        hitCount = 0;
        gameOverCountDown =  INITIAL_GAME_TIME;

        GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_ACTIVE);

        GameWorld.getInstance().setIsPaused(false);

        // been using same light setup as ever
        //  https://xoppa.github.io/blog/loading-a-scene-with-libgdx/
        // shadow lighting lifted from 'Learning_LibGDX_Game_Development_2nd_Edition' Ch. 14 example
        Environment environment = new Environment();
        environment.set(
                new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
//        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, lightDirection));

        cam = new PerspectiveCamera(67, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
//        cam.position.set(3f, 7f, 10f);
//        cam.lookAt(0, 4, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        camController = new CameraInputController(cam);
//        camController = new FirstPersonCameraController(cam);

        // must be done before any bullet object can be created .. I don't remember why the BulletWorld is only instanced once
        BulletWorld.getInstance().initialize(cam);              // TODO: screen inheritcs from e.g. "ScreenWithBulletWorld"

        // "guiCam" etc. lifted from 'Learning_LibGDX_Game_Development_2nd_Edition' Ch. 14 example
        guiCam = new OrthographicCamera(GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        guiCam.position.set(guiCam.viewportWidth / 2f, guiCam.viewportHeight / 2f, 0);
        guiCam.update();

        DirectionalShadowLight shadowLight = new DirectionalShadowLight(1024, 1024, 120, 120, 1f, 300);
        shadowLight.set(0.8f, 0.8f, 0.8f, new Vector3(0.5f, -1f, 0f));
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;


        engine = new Engine();
        renderSystem = new RenderSystem(shadowLight, environment, cam);
        bulletSystem = new BulletSystem();
        engine.addSystem(renderSystem);
        engine.addSystem(bulletSystem);
        engine.addSystem(new PickRaySystem(gameEventSignal));
        engine.addSystem(new StatusSystem());
        engine.addSystem(new CharacterSystem());

        Vector3 scale = new Vector3(4, 1, 4);
        Vector3 trans = new Vector3(0, 10, -5);
        platformEntity =
                PrimitivesBuilder.getBoxBuilder("box").create(0.0f, trans, scale);
        ModelInstanceEx.setColorAttribute(
                platformEntity.getComponent(ModelComponent.class).modelInst, platformColor);
        engine.addEntity(platformEntity);


        onPlayerPicked();
    }

    // this is kind of an arbitrary
    private void onPlayerPicked() {

        screenData.buildArena(engine);

// load the rigs and search for matching name (name of rig as read from model is stashed in PickRayComp as a hack ;)
        Array<Entity> characters = new Array<Entity>();
        screenData.buildCharacters(characters, engine, "tanks"); // hack object name embedded into pick component

        String objectName = GameWorld.getInstance().getPlayerObjectName();

        for (Entity e : characters) {
            if (e.getComponent(PickRayComponent.class).objectName.equals(objectName)) {
                pickedPlayer = e;
                pickedPlayer.remove(PickRayComponent.class); // component no longer needed, remove  it
            }
//            else
//                engine.removeEntity(e); // let'em become zombies ;)
        }

        characters = new Array<Entity>();

        screenData.buildCharacters(characters, engine, "characters");

        for (Entity e : characters) {

            btRigidBody chbody = e.getComponent(BulletComponent.class).body;
            TankController tc = new TankController(chbody, e.getComponent(BulletComponent.class).mass);/* should be a property of the tank? */
            e.add(new CharacterComponent(
                    new SteeringTankController(
                            tc, chbody, new SteeringBulletEntity(pickedPlayer.getComponent(BulletComponent.class).body))));
            engine.addEntity(e);
        }

        Matrix4 playerTrnsfm = pickedPlayer.getComponent(ModelComponent.class).modelInst.transform;
        /*
         player character should be able to attach camera operator to arbitrary entity (e.g. guided missile control)
          */
        chaserSteerable.setSteeringBehavior(
                new TrackerSB<Vector3>(chaserSteerable, playerTrnsfm, chaserTransform, new Vector3(0, 2, 0)));

        cameraMan = new CameraMan(cam, camDefPosition, camDefLookAt, playerTrnsfm);

        Entity cameraEntity = new Entity();
        cameraEntity.add(new CharacterComponent(cameraMan));
        engine.addEntity(cameraEntity);

// plug in the picked player
        final StatusComponent sc = new StatusComponent();
        pickedPlayer.add(sc);

        /*
         * this goofball thing exists because of dependency between game model and UI i.e. player
         * dead or whatever in the world model must signal back to UI to pause/restart whatever
         */
        /*

        THIS CXAN BE REPLADED BY TIMER!!!!!
         */
        sc.statusUpdater = new BulletEntityStatusUpdate() {

            Vector3 origin = new Vector3(0, 0, 0); // the reference point for determining an object has exitted the level
            Vector3 bounds = new Vector3(20, 20, 20);

            // the reference point for determining an object has exitted the level
            float boundsDst2 = bounds.dst2(origin);
            Vector3 v = new Vector3();

            private void onScreenTransition(){

                textShow = false;
                sillyFontFx(new Color(0, 0, 1, 1f));
                Timer.schedule(gameOverTask, ROUND_CONTINUE_WAIT_TIME);
                // start fadeout 2 seconds prior to Continue Wait Time up
                Timer.schedule(fadeoutTask, ROUND_CONTINUE_WAIT_TIME - 2.0f, 0.1f);
                gameOverCountDown = ROUND_CONTINUE_WAIT_TIME; // for displaying the timer countdound ...
                GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_CONTINUE);
            }

            @Override
            public void update(Entity e) {

                if (GameWorld.GAME_STATE_T.ROUND_ACTIVE == GameWorld.getInstance().getRoundActiveState()
                        && !GameWorld.getInstance().getIsPaused()) {

                    v = e.getComponent(ModelComponent.class).modelInst.transform.getTranslation(v);

                    if (v.dst2(origin) > boundsDst2) {
                        gameOverMessageString ="Elvis is Dead! Continue?";
                        onScreenTransition();
                    }
                    else
                    if (gameOverCountDown <= ROUND_CONTINUE_WAIT_TIME) {
                        gameOverMessageString = "Time's Up! Continue?";
                        onScreenTransition();
                    }
                }
            }};

        setupplayerUI(pickedPlayer);
        Timer.schedule(oneSecondTask, 0, 1);

        multiplexer.addProcessor(playerUI);
        Gdx.input.setInputProcessor(multiplexer);
    }

    private Timer.Task fadeoutTask = new Timer.Task(){
        @Override
        public void run (){

            if (hudOverlayColor.r > .1 )
                hudOverlayColor.r -= .1;
            if (hudOverlayColor.g > .1 )
                hudOverlayColor.g -= .1;
            if (hudOverlayColor.b > .1 )
                hudOverlayColor.b -= .1;
            if (hudOverlayColor.a < 1.0 )
                hudOverlayColor.a += .1;
        }
    };

    private Timer.Task gameOverTask = new Timer.Task(){
        @Override
        public void run (){
            GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_QUIT);
        }
    };

    private Timer.Task roundCompleteTask = new Timer.Task(){
        @Override
        public void run (){

            GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_COMPLETE_NEXT);
        }
    };

    private Timer.Task oneSecondTask = new Timer.Task(){
        @Override
        public void run (){
            gameOverCountDown -= ONE_SECOND; // fps for now
                textShow = !textShow;
        }
    };

    private void sillyFontFx(Color cc){
        font.setColor(cc);
        float scaleX = font.getScaleX();
        float scaleY = font.getScaleY();
        font.getData().setScale(scaleY  * 1.5f);

    }

    /*
    game event object for signalling to pickray system
    */
    private final GameEvent gameEvent = new GameEvent() {

        private void onScreenTransition(){

            sillyFontFx(new Color(0, 1, 0, 1f));
            gameOverCountDown = ROUND_COMPLETE_FADEOUT_TIME; // short delay before Next Screen
            Timer.schedule(roundCompleteTask, ROUND_COMPLETE_FADEOUT_TIME);
            GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_COMPLETE_WAIT);
            gameOverMessageString = "Congratulations!";
        }

        @Override
        public void callback(Entity picked, EventType eventType) {
            if (RAY_PICK == eventType && null != picked) {
                ModelInstanceEx.setMaterialColor(
                        picked.getComponent(ModelComponent.class).modelInst, Color.RED);

                hitCount += 1;

                if (ALL_HIT_COUNT == hitCount) {
                    onScreenTransition();
                } else
                    if (hitCount < ALL_HIT_COUNT) {
                    gameOverCountDown += ROUND_CONTINUE_WAIT_TIME; // each successfull hit buys time back on the clock!
                }
            }
        }
    };

    private void cameraSwitch(){
        if (cameraMan.nextOpMode())
            multiplexer.addProcessor(camController);
        else
            multiplexer.removeProcessor(camController);
    }

    /*
     * override stage:act() and handle controls
     */
    private void setupplayerUI(final Entity pickedPlayer){

// setup the vehicle model so it can be referenced in the mapper
        final SimpleVehicleModel vehicleModel = new TankController(
                pickedPlayer.getComponent(BulletComponent.class).body,
                pickedPlayer.getComponent(BulletComponent.class).mass /* should be a property of the tank? */);

        playerUI = new GameUI(new InputMapper()){
            @Override
            public void act (float delta) {

                mapper.latchInputState();

                boolean paused = GameWorld.getInstance().getIsPaused();
                int checkedBox = 0; // button default at top selection

                if (!paused) {
                    /*
                    AIs and Player act thru same interface to rig model (updateControols()) but the AIs are run by
the ECS via the CharacgterSystem, whereas the Player update directly here with controller inputs.
So we have to pause it explicitly as it is not governed by ECS
  */
                    GameWorld.GAME_STATE_T state = GameWorld.getInstance().getRoundActiveState();
// don't update controls during Continue State
                    if ( GameWorld.GAME_STATE_T.ROUND_ACTIVE == state ||
                            GameWorld.GAME_STATE_T.ROUND_COMPLETE_WAIT == GameWorld.getInstance().getRoundActiveState() ) {
                        vehicleModel.updateControls(mapper.getAxisY(0), mapper.getAxisX(0),
                                (mapper.isInputState(InputMapper.InputState.INP_B2)), 0); // need to use Vector2
                    }

                    if (mapper.isInputState(InputMapper.InputState.INP_SELECT)) {

                        gameEventSignal.dispatch(
                                gameEvent.set(RAY_PICK, cam.getPickRay(mapper.getPointerX(), mapper.getPointerY()), 0));

                        if (GameWorld.GAME_STATE_T.ROUND_OVER_CONTINUE == GameWorld.getInstance().getRoundActiveState()) {
                            GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_RESTART);
                        }
                    }
                    if (mapper.isInputState(InputMapper.InputState.INP_ESC)) {
                        paused = true;
                    }
                } else { // paused

                    if (mapper.isInputState(InputMapper.InputState.INP_ESC)) {

                        GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_QUIT);
                    }
                    if (mapper.isInputState(InputMapper.InputState.INP_SELECT)) {

                        switch (getCheckedIndex()) {
                            default:
                            case 0: // resume
                                paused = false;
                                break;
                            case 1: // restart
                                GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_RESTART);
                                break;
                            case 2: // quit
                                GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_QUIT);
                                break;
                            case 3: // camera
                                paused = false;
                                cameraSwitch();
                                break;
                            case 4: // dbg drwr
                                paused = false;
                                BulletWorld.USE_DDBUG_DRAW = !BulletWorld.USE_DDBUG_DRAW;
                                // has to reinitialize bullet world to set the flag
                                GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_RESTART);
                                break;
                        }
                        Gdx.app.log("GameUI", "  getCheckedIndex() == " + getCheckedIndex());
                    }
                    checkedBox = checkedUpDown(mapper.getDpad(null).getY());
                }
                setCheckedBox(checkedBox);
                GameWorld.getInstance().setIsPaused(paused);

                super.act(delta);
            }};
    }

    /*
     * this is kind of a hack to test some ray casting
     */
    private GameEvent nearestObjectToPlayerEvent = new GameEvent() {

        Vector3 tmpV = new Vector3();
        Vector3 posV = new Vector3();
        GfxUtil lineInstance = new GfxUtil();
        /*
        we have no way to invoke a callback to the picked component.
        Pickable component required to implment some kind of interface to provide a
        callback method e.g.
          pickedComp = picked.getComponent(PickRayComponent.class).pickInterface.picked( blah foo bar)
          if (null != pickedComp.pickedInterface)
             pickInterface.picked( myEntityReference );

@picked: simpler type (not Entity) eg. Vector3 .... ?
         */
        @Override
        public void callback(Entity picked, GameEvent.EventType eventType) {

            if (RAY_DETECT == eventType && null != picked) {
                RenderSystem.debugGraphics.add(lineInstance.lineTo(
                        pickedPlayer.getComponent(ModelComponent.class).modelInst.transform.getTranslation(posV),
                        picked.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmpV),
                        Color.LIME));
            }}
    };

    private Vector3 tmpV = new Vector3();
    private Vector3 position = new Vector3();
    private Quaternion rotation = new Quaternion();
    private Vector3 direction = new Vector3(0, 0, -1); // vehicle forward
    private Ray lookRay = new Ray();
    private GfxUtil camDbgLineInstance = new GfxUtil();
    private Matrix4 chaserTransform = new Matrix4();
    private SteeringEntity chaserSteerable = new SteeringEntity();

    //private String s = new String(); // doesn't help ... String.format calls new Formatter()!
    /*
     * https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
     * "Note that using a StringBuilder is highly recommended against string concatenation in your
     * render method. The StringBuilder will create less garbage, causing almost no hick-ups due to garbage collection."
     */
    @Override
    public void render(float delta) {

        String s;
        // game box viewport
        Gdx.gl.glViewport(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // put in any debug graphics to the render pipeline
        chaserSteerable.update(delta);
        RenderSystem.debugGraphics.add(camDbgLineInstance.lineTo(
                pickedPlayer.getComponent(ModelComponent.class).modelInst.transform.getTranslation(position),
                chaserTransform.getTranslation(tmpV), Color.PURPLE));

        camController.update(); // this can probaly be pause as well
        engine.update(delta);
        BulletWorld.getInstance().update(delta);

        Matrix4 transform = pickedPlayer.getComponent(ModelComponent.class).modelInst.transform;
        transform.getTranslation(position);
        transform.getRotation(rotation);
        lookRay.set(position, ModelInstanceEx.rotateRad(direction.set(0, 0, -1), rotation));
        gameEventSignal.dispatch(nearestObjectToPlayerEvent.set(RAY_DETECT, lookRay, 0)); // maybe pass transform and invoke lookRay there


        // crude  hack for platform disappear effect
        if (platformColor.a > 0.1f) {
            platformColor.a -= 0.005f;
            ModelInstanceEx.setColorAttribute(platformEntity.getComponent(ModelComponent.class).modelInst, platformColor);
        } else if (null != platformEntity) {
            engine.removeEntity(platformEntity);
            //platformEntity.remove(BulletComponent.class); // idfk
            platformColor.a = 0;
        }

        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();
            // String.format calls new Formatter() which we dont want!
        if (GameWorld.GAME_STATE_T.ROUND_OVER_CONTINUE == GameWorld.getInstance().getRoundActiveState()) {
///*
            s = String.format(Locale.ENGLISH, "%s (%d)", gameOverMessageString, gameOverCountDown);
            font.draw(batch, s, 10, 0 + font.getLineHeight());
 //*/

        } else {

            if (GameWorld.GAME_STATE_T.ROUND_COMPLETE_WAIT == GameWorld.getInstance().getRoundActiveState()){
                s = String.format(Locale.ENGLISH, "%s (%d)", gameOverMessageString, gameOverCountDown);
                font.draw(batch, s, 10, 0 + font.getLineHeight());
            }
            else
            if (textShow
                    || GameWorld.GAME_STATE_T.ROUND_ACTIVE == GameWorld.getInstance().getRoundActiveState()) {

                s = String.format(Locale.ENGLISH, "%2d", gameOverCountDown - ROUND_CONTINUE_WAIT_TIME);
                font.draw(batch, s, 10, 0 + font.getLineHeight());
            }

            s = String.format(Locale.ENGLISH, "(%d)", hitCount);
            font.draw(batch, s, (GameWorld.VIRTUAL_WIDTH / 4.0f) * 3, 0 + font.getLineHeight());
        }
        batch.end();
/*
        float visibleCount = renderSystem.visibleCount;
        float renderableCount = renderSystem.renderableCount;
        //s = String.format("fps=%d vis.cnt=%d rndrbl.cnt=%d", Gdx.graphics.getFramesPerSecond(), renderSystem.visibleCount, renderSystem.renderableCount);
        stringBuilder.setLength(0);
        stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
        stringBuilder.append(" Visible: ").append(visibleCount);
        stringBuilder.append(" / ").append(renderableCount);
            fpsLabel.setText(stringBuilder);
*/
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (GameWorld.getInstance().getIsPaused()
                || GameWorld.GAME_STATE_T.ROUND_OVER_CONTINUE == GameWorld.getInstance().getRoundActiveState()) {
            // example of using ShapeRender to draw directly to screen
            //        shapeRenderer.setProjectionMatrix ????
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(hudOverlayColor);
            shapeRenderer.rect(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
            shapeRenderer.end();
        }

        playerUI.act(Gdx.graphics.getDeltaTime());
        playerUI.draw();

        /* "Falling off platform" ... let a "sensor" or some suitable means to detect "fallen off platform" at which point, set gameOver.
          This way user can't pause during falling sequence. Once fallen past certain point, then allow screen switch.
         */
        checkForScreenTransition();
    }

    /*
     * collect all the screen transition state management here
     */
    private void checkForScreenTransition() {

        switch (GameWorld.getInstance().getRoundActiveState()) {

            default:
            case ROUND_ACTIVE:
            case ROUND_COMPLETE_WAIT:
            case ROUND_OVER_CONTINUE: // Continue to Restart transition is triggered by hit "Select" while in Continue State
                break;

            case ROUND_OVER_RESTART:
                screenTeardown();
                screenInit();
                break;

            case ROUND_COMPLETE_NEXT:
                GameWorld.getInstance().showScreen(new MainMenuScreen()); // tmp menu screen
                break;

            case ROUND_OVER_QUIT:
                GameWorld.getInstance().showScreen(new SplashScreen());
                break;
        }
    }

    @Override
    public void show() {

        screenInit();
    }

    @Override
    public void resize(int width, int height) {
    /*
    https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
    We need to update the stage's viewport in the resize method. The last Boolean argument set the origin to the lower left coordinate, causing the label to be drawn at that location.
     */
// resizes the stage UI view port but the 3D camera is not!
        //        stage.getViewport().update(width, height, true);
    }

    /*
     * because some stuff but not all stuff done every screen (re)start
     */
    private void screenTeardown(){

        Gdx.app.log("GameScreen", "screenTearDown");

        Timer.instance().clear();

        engine.removeSystem(bulletSystem); // make the system dispose its stuff
        engine.removeSystem(renderSystem); // make the system dispose its stuff
        engine.removeAllEntities(); // allow listeners to be called (for disposal)

        BulletWorld.getInstance().dispose();
        playerUI.dispose();

        font.dispose(); // only instantiated on show()  for some reaseon
    }

    @Override
    public void dispose() {

        screenTeardown();

        batch.dispose();
        shapeRenderer.dispose();

        // screens that load assets must calls assetLoader.dispose() !
        super.dispose();
    }

    /*
     * android "back" button sends ApplicationListener.pause(), but then sends ApplicationListener.dispose() !!
     */

    @Override
    public void pause() {
        // Android "Recent apps" (square on-screen button), Android "Home" (middle o.s. btn ... Game.pause()->Screen.pause()
    }

    @Override
    public void resume() {
        // Android resume from "minimized" (Recent Apps button selected)
    }

    @Override
    public void hide() {
        dispose();
    }
}
