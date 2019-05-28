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
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.ashley.utils.ImmutableArray;
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
import com.badlogic.gdx.utils.Timer;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.characters.CameraMan;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.DeleteMeComponent;
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
class GameScreen extends TimedGameScreen {

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

    private static final int ALL_HIT_COUNT = 3;
    private static final int TIME_LIMIT_WARN_SECS = 10;

//    private void setHitCount(int ct){
//        pickedPlayer.getComponent(StatusComponent.class).hitCount = 0;
//    }

    private int incHitCount(int ct) {
        pickedPlayer.getComponent(StatusComponent.class).hitCount += ct;
        return pickedPlayer.getComponent(StatusComponent.class).hitCount;
    }

    private void screenInit(){

        screenTimer = DEFAULT_SCREEN_TIME;

        font = new BitmapFont(Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);

//        font.setColor(1, 1, 1, 0.5f);

        float fontGetDensity = Gdx.graphics.getDensity();

        if (fontGetDensity > 1)
            font.getData().setScale(fontGetDensity);

//        pickedPlayer.getComponent(StatusComponent.class).hitCount = 0;

        GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_ACTIVE);

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

        onPlayerPicked();
    }

    // this is kind of an arbitrary
    private void onPlayerPicked() {

        sceneLoader.buildScene(engine);
        ImmutableArray<Entity> characters = engine.getEntitiesFor(Family.all(CharacterComponent.class).get());

        // name of picked player rig as read from model is stashed in PickRayComp as a hack ;)
        String objectName = GameWorld.getInstance().getPlayerObjectName();
        pickedPlayer = null; // bad w3e have to depend on this crap for now

        for (Entity e : characters) {

            PickRayComponent pc = e.getComponent(PickRayComponent.class);
            if (null != pc && null != pc.objectName && pc.objectName.equals(objectName)) {
//            if (e.getComponent(PickRayComponent.class).objectName.equals(objectName)) {
                pickedPlayer = e;
                pickedPlayer.remove(PickRayComponent.class); // tmp ... stop picking yourself ...
                pickedPlayer.remove(CharacterComponent.class); // only needed it for selecting the steerables
            }
        }

        for (Entity e : characters) {
//            if (e != pickedPlayer)  /// removed comp, so the immuatble array no longer contain picked player entity ...

            btRigidBody chbody = e.getComponent(BulletComponent.class).body;
            TankController tc = new TankController(chbody, e.getComponent(BulletComponent.class).mass); /* should be a property of the tank? */

            CharacterComponent cc = e.getComponent(CharacterComponent.class);

            cc.setSteerable(
                    new SteeringTankController(
                            tc, chbody, new SteeringBulletEntity(pickedPlayer.getComponent(BulletComponent.class).body)));
        }

        Matrix4 playerTrnsfm = pickedPlayer.getComponent(ModelComponent.class).modelInst.transform;
        /*
         player character should be able to attach camera operator to arbitrary entity (e.g. guided missile control)
 (a "chaser-camera" entity - no visual but has transform and possibly AI and/or physics chaacterisitics - can be active at most time
 and camera may or may not actually be on it)
          */
        chaserSteerable.setSteeringBehavior(
                new TrackerSB<Vector3>(chaserSteerable, playerTrnsfm, chaserTransform, new Vector3(0, 2, 0)));

        cameraMan = new CameraMan(cam, camDefPosition, camDefLookAt, playerTrnsfm);

        Entity cameraEntity = new Entity();
        cameraEntity.add(new CharacterComponent(cameraMan));
        /*
        cameraEntity.add(new CharacterComponent());
        CharacterComponent cc = cameraEntity.getComponent(CharacterComponent.class);
        cc.setSteerable(cameraMan);
*/
        engine.addEntity(cameraEntity);

// plug in the picked player
        final StatusComponent sc = new StatusComponent(15, 10);
        pickedPlayer.add(sc);

        /*
         * this goofball thing exists because of dependency between game model and UI i.e. player
         * dead or whatever in the world model must signal back to UI to pause/restart whatever
         */
        sc.statusUpdater = new BulletEntityStatusUpdate() {

            Vector3 origin = new Vector3(0, 0, 0); // the reference point for determining an object has exitted the level
            Vector3 bounds = new Vector3(20, 20, 20);

            // the reference point for determining an object has exitted the level
            float boundsDst2 = bounds.dst2(origin);
            Vector3 v = new Vector3();

            @Override
            public void update(Entity e) {

                if (GameWorld.GAME_STATE_T.ROUND_ACTIVE == GameWorld.getInstance().getRoundActiveState()
                        && !GameWorld.getInstance().getIsPaused()) {

                    v = e.getComponent(ModelComponent.class).modelInst.transform.getTranslation(v);

                    if (v.dst2(origin) > boundsDst2) {
                       e.getComponent(StatusComponent.class).lifeClock = 0;
                    }
                }
            }
        };

        setupplayerUI(pickedPlayer);

        multiplexer = new InputMultiplexer(playerUI); // make sure get a new one since there will be a new Stage instance ;)
        Gdx.input.setInputProcessor(multiplexer);
    }

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
// short delay before change Screen
            GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_COMPLETE_WAIT);
            screenTimer = 3 * 60;
        }

        // placeholder for "picked" action (fadeout, explode, etc.)
        void onPicked(Entity e){

            // entity.lifeTimeRemain = 0;   .... to make it fade!

            ModelInstanceEx.setMaterialColor(
                    e.getComponent(ModelComponent.class).modelInst, Color.RED);
        }

        @Override
        public void callback(Entity picked, EventType eventType) {

            if (RAY_PICK == eventType && null != picked) {

                onPicked(picked);

//pickedPlayer.getComponent(StatusComponent.class).hitCount += 1;
                int ct = incHitCount(1);

                if (ALL_HIT_COUNT == ct /* pickedPlayer.getComponent(StatusComponent.class).hitCount */) {
                    onScreenTransition();
                } else
                    if (ct /* pickedPlayer.getComponent(StatusComponent.class).hitCountj */ < ALL_HIT_COUNT) {
//                      gameOverCountDown += ROUND_CONTINUE_WAIT_TIME; // each successfull hit buys time back on the clock!
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

                boolean paused = GameWorld.getInstance().getIsPaused();
                int checkedBox = 0; // button default at top selection
                mapper.latchInputState();

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

                        if (GameWorld.GAME_STATE_T.ROUND_OVER_MORTE == GameWorld.getInstance().getRoundActiveState()) {
                            GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_RESTART);
                        }
                    }
                    if (mapper.isInputState(InputMapper.InputState.INP_ESC)) {

                        if (GameWorld.GAME_STATE_T.ROUND_OVER_MORTE != GameWorld.getInstance().getRoundActiveState()){
                            paused = true;
                        }
                    }
                }
                else { // paused
                    if (mapper.isInputState(InputMapper.InputState.INP_ESC)) {

                        GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_QUIT);
                    }
                    if (mapper.isInputState(InputMapper.InputState.INP_SELECT)) {

                        paused = false;

                        switch (getCheckedIndex()) {
                            default:
                            case 0: // resume
                                break;
                            case 1: // restart
                                GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_RESTART);
                                break;
                            case 2: // quit
                                GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_QUIT);
                                break;
                            case 3: // camera
                                cameraSwitch();
                                break;
                            case 4: // dbg drwr
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

                updateUI();
                super.act(delta);
            }

            private void updateUI(){

                stringBuilder.setLength(0);
                stringBuilder.append("0001");
                scoreLabel.setText(stringBuilder);

                stringBuilder.setLength(0);
                stringBuilder.append(screenTimer / 60); // FPS
                timerLabel.setText(stringBuilder);

                stringBuilder.setLength(0);
                stringBuilder.append(incHitCount(0) ).append(" / 3"); // FPS
                itemsLabel.setText(stringBuilder);

                stringBuilder.setLength(0);
                stringBuilder.append("Continue? ").append(screenTimer / 60); // FPS
                mesgLabel.setText(stringBuilder);
            }
        };
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

        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();
// TODO: the UI to be blitted into a separate bitmap (not inside the UI draw! )
        // String.format calls new Formatter() which we dont want!
        int screenTimerSecs = screenTimer / 60; // FPS

        if (GameWorld.GAME_STATE_T.ROUND_OVER_MORTE == GameWorld.getInstance().getRoundActiveState()) {
// don't care about screen timer seconds anymore (possible that screen timer expires before 10 seconds continue time)
//            s = String.format(Locale.ENGLISH, "%2d", screenTimerSecs);
//            font.draw(batch, s, 10, 0 + font.getLineHeight());

            // center of screen ....
            int dieClock = pickedPlayer.getComponent(StatusComponent.class).dieClock / 60; // FPS
            s = String.format(Locale.ENGLISH, "%s (%d)", "Continue? ", dieClock);
            font.draw(batch, s, 10, GameWorld.VIRTUAL_HEIGHT / 2.0f + font.getLineHeight());

        } else if (GameWorld.GAME_STATE_T.ROUND_COMPLETE_WAIT == GameWorld.getInstance().getRoundActiveState()) {

            s = String.format(Locale.ENGLISH, "%s", "EXIT");
            font.draw(batch, s, 10, GameWorld.VIRTUAL_HEIGHT - font.getLineHeight());

            // still show the screen timere
            s = String.format(Locale.ENGLISH, "%2d", screenTimerSecs);
            font.draw(batch, s, 10, 0 + font.getLineHeight());

        } else if (GameWorld.GAME_STATE_T.ROUND_ACTIVE == GameWorld.getInstance().getRoundActiveState()) {

            s = String.format(Locale.ENGLISH, "%2d", screenTimerSecs);

            if (screenTimerSecs <= TIME_LIMIT_WARN_SECS) {
                s = String.format(Locale.ENGLISH, "<%2d>", screenTimerSecs);
            }
            font.draw(batch, s, 10, 0 + font.getLineHeight());

            s = String.format(Locale.ENGLISH, "(%d)", incHitCount(0) /* pickedPlayer.getComponent(StatusComponent.class).hitCount */);
            font.draw(batch, s, (GameWorld.VIRTUAL_WIDTH / 4.0f) * 3, GameWorld.VIRTUAL_HEIGHT - font.getLineHeight());
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
                || GameWorld.GAME_STATE_T.ROUND_OVER_MORTE == GameWorld.getInstance().getRoundActiveState()
                || GameWorld.GAME_STATE_T.ROUND_OVER_TIMEOUT == GameWorld.getInstance().getRoundActiveState()
        ) {
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

        for (Entity e : engine.getEntitiesFor(Family.all(DeleteMeComponent.class).get())) {
            if (null != e) {
                if (e.getComponent(DeleteMeComponent.class).deleteMe) {
                    engine.removeEntity(e);
                }
            }
        }
    }

    /*
     * collect all the screen transition state management here
     */
    private void checkForScreenTransition() {

        if ( !GameWorld.getInstance().getIsPaused() // have to do this here for now
                && screenTimer > 0) {
            screenTimer -= 1;
        }

        switch (GameWorld.getInstance().getRoundActiveState()) {

            default:
            case ROUND_ACTIVE:
                if (0 == screenTimer){
                    pickedPlayer.getComponent(StatusComponent.class).dieClock = 2 * 60; // FPS // 2 seconds fadout screen transition
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_TIMEOUT);
                }
                else if (0 == pickedPlayer.getComponent(StatusComponent.class).lifeClock){
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_MORTE);
                }
                break;

            case ROUND_OVER_TIMEOUT:
                if (pickedPlayer.getComponent(StatusComponent.class).dieClock > 0) {
                    fadeScreen();
                }
                else {
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_QUIT);
                }
                break;

            case ROUND_COMPLETE_WAIT:
                if (screenTimer <= 0){
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_COMPLETE_NEXT);
                }
                break;

            case ROUND_OVER_MORTE: // Continue to Restart transition is triggered by hit "Select" while in Continue State
                if (pickedPlayer.getComponent(StatusComponent.class).dieClock <= 0) {
                    pickedPlayer.getComponent(StatusComponent.class).dieClock = 2 * 60; // FPS // 2 seconds fadout screen transition
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_TIMEOUT);
                }
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

    private void fadeScreen(){

        if (hudOverlayColor.r > 0.1f )
            hudOverlayColor.r -= .1;
        if (hudOverlayColor.g > 0.1f )
            hudOverlayColor.g -= .1;
        if (hudOverlayColor.b > 0.1f )
            hudOverlayColor.b -= .1;
        if (hudOverlayColor.a < 1 )
            hudOverlayColor.a += 0.1f;
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

        PrimitivesBuilder.clearShapeRefs();
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
