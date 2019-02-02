package com.mygdx.game.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.characters.CameraMan;
import com.mygdx.game.characters.Chaser;
import com.mygdx.game.characters.InputStruct;
import com.mygdx.game.characters.PlayerCharacter;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.controllers.SimpleVehicleModel;
import com.mygdx.game.controllers.SteeringBulletEntity;
import com.mygdx.game.controllers.SteeringTankController;
import com.mygdx.game.controllers.TankController;
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
import java.util.Random;

import static com.mygdx.game.util.GameEvent.EventType.RAY_DETECT;
import static com.mygdx.game.util.GameEvent.EventType.RAY_PICK;

/**
 * Created by neiderm on 12/18/17.
 */
class GameScreen implements Screen {

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
    private static final int GAME_BOX_W = Gdx.graphics.getWidth();
    private static final int GAME_BOX_H = Gdx.graphics.getHeight();
    private final Color hudOverlayColor = new Color(1, 0, 0, 0.2f);
    private PlayerCharacter playerUI;
    private InputMultiplexer multiplexer = new InputMultiplexer();
    private StringBuilder stringBuilder = new StringBuilder();
    private Label fpsLabel;
    private Signal<GameEvent> pickRayEventSignal = new Signal<GameEvent>();
    private boolean roundOver = false;
    private final Vector3 camDefPosition = new Vector3(1.0f, 13.5f, 02f); // hack: position of fixed camera at 'home" location
    private final Vector3 camDefLookAt = new Vector3(1.0f, 10.5f, -5.0f);
    private Entity pickedPlayer;
    private Entity platformEntity;
    private float colorAlpha = 0.9f;
    private Color platformColor = new Color(255, 0, 0, colorAlpha);

    private final int gsBTNwidth =  Gdx.graphics.getHeight() * 3 / 8;
    private final int gsBTNheight =  Gdx.graphics.getHeight() * 3 / 8;


    GameScreen() {

        GameWorld.getInstance().setIsPaused(false);

        // been using same light setup as ever
        //  https://xoppa.github.io/blog/loading-a-scene-with-libgdx/
        // shadow lighting lifted from 'Learning_LibGDX_Game_Development_2nd_Edition' Ch. 14 example
        Environment environment = new Environment();
        environment.set(
                new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
//        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, lightDirection));

        cam = new PerspectiveCamera(67, GAME_BOX_W, GAME_BOX_H);
//        cam.position.set(3f, 7f, 10f);
//        cam.lookAt(0, 4, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        camController = new CameraInputController(cam);
//        camController = new FirstPersonCameraController(cam);

        // must be done before any bullet object can be created
        BulletWorld.getInstance().initialize(cam);

        // "guiCam" etc. lifted from 'Learning_LibGDX_Game_Development_2nd_Edition' Ch. 14 example
        guiCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        guiCam.position.set(guiCam.viewportWidth / 2f, guiCam.viewportHeight / 2f, 0);
        guiCam.update();

        DirectionalShadowLight shadowLight = new DirectionalShadowLight(1024, 1024, 120, 120, 1f, 300);
        shadowLight.set(0.8f, 0.8f, 0.8f, new Vector3(0.5f, -1f, 0f));
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;


        engine = new Engine();
        engine.addSystem(renderSystem = new RenderSystem(shadowLight, environment, cam));
        engine.addSystem(bulletSystem = new BulletSystem(BulletWorld.getInstance()));
        engine.addSystem(new PickRaySystem(pickRayEventSignal));
        engine.addSystem(new StatusSystem());
        engine.addSystem(new CharacterSystem());

        Vector3 scale = new Vector3(4, 1, 4);
        Vector3 trans = new Vector3(0, 10, -5);
        platformEntity =
                PrimitivesBuilder.getBoxBuilder("box").create(0.0f, trans, scale);
        ModelInstanceEx.setColorAttribute(
                platformEntity.getComponent(ModelComponent.class).modelInst, platformColor);
        engine.addEntity(platformEntity);

        GameWorld.sceneLoader.buildArena(engine);

        onPlayerPicked();
    }


    private void onPlayerPicked() {

        GameWorld.sceneLoader.onPlayerPicked(engine); // creates test objects

// load the rigs and search for matching name (name of rig as read from model is stashed in PickRayComp as a hack ;)
        Array<Entity> characters = new Array<Entity>();
        GameWorld.sceneLoader.buildCharacters(characters, engine, "tanks", true); // hack object name embedded into pick component

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

        GameWorld.sceneLoader.buildCharacters(characters, engine, "characters", false);

        for (Entity e : characters) {

            TankController tc = new TankController(e.getComponent(BulletComponent.class).body,
                    e.getComponent(BulletComponent.class).mass);/* should be a property of the tank? */
            e.add(new CharacterComponent(new SteeringTankController(tc, e,
                    new SteeringBulletEntity(pickedPlayer.getComponent(BulletComponent.class).body))));
            engine.addEntity(e);
        }

        /*
         player character should be able to attach camera operator to arbitrary entity (e.g. guided missile control)
          */
        Chaser chaser = new Chaser();
        engine.addEntity(chaser.create(
                pickedPlayer.getComponent(ModelComponent.class).modelInst.transform));


        cameraMan = new CameraMan(cam, camDefPosition, camDefLookAt,
                pickedPlayer.getComponent(ModelComponent.class).modelInst.transform);

        CharacterComponent comp = new CharacterComponent(cameraMan);
        Entity cameraEntity = new Entity();
        cameraEntity.add(comp);
        engine.addEntity(cameraEntity);

// plug in the picked player
        final StatusComponent sc = new StatusComponent();
        pickedPlayer.add(sc);
        sc.transform = pickedPlayer.getComponent(ModelComponent.class).modelInst.transform;

        sc.statusUpdater = new BulletEntityStatusUpdate() {
            Vector3 v = new Vector3();
            @Override
            public void update() {
                v = sc.transform.getTranslation(v);
                if (v.dst2(sc.origin) > sc.boundsDst2) {
                    roundOver = true; // respawn() ... can't do it in this context??
                }
            }};

        setupVehicle(pickedPlayer);

        multiplexer.addProcessor(playerUI);
        Gdx.input.setInputProcessor(multiplexer);
    }

/*
 game event object for signalling to pickray system
 */
    private final GameEvent gameEvent = new GameEvent() {
        @Override
        public void callback(Entity picked, EventType eventType) {
            if (RAY_PICK == eventType && null != picked) {
                ModelInstanceEx.setMaterialColor(
                        picked.getComponent(ModelComponent.class).modelInst, Color.RED);
            }
        }};

    private void cameraSwitch(){
        if (cameraMan.nextOpMode())
            multiplexer.addProcessor(camController);
        else
            multiplexer.removeProcessor(camController);
    }


    private final int gsBTNx = Gdx.graphics.getWidth() / 2 - gsBTNwidth /2;
    private final int gsBTNy = Gdx.graphics.getHeight() / 2;

    private Ray setPickRay(float x, float y) {
        // Note: Y coordinate must be flipped around before passing to cam
        return cam.getPickRay(gsBTNx + x,
                Gdx.graphics.getHeight() - (gsBTNy + y));
    }


    private void setupVehicle(final Entity pickedPlayer){

// setup the vehicle model so it can be referenced in the mapper
        final SimpleVehicleModel vehicleModel = new TankController(
                pickedPlayer.getComponent(BulletComponent.class).body,
                pickedPlayer.getComponent(BulletComponent.class).mass /* should be a property of the tank? */);

        InputStruct mapper = new InputStruct() {

            btRigidBody body = pickedPlayer.getComponent(BulletComponent.class).body;
            Vector3 tmpV = new Vector3();
            Random rnd = new Random();
            final Vector3 impulseForceV = new Vector3();
            InputState preInputState;

            @Override
            public void update(float deltaT) {
                InputState nowInputState = getInputState(false);
                Vector2 pointer = getPointer();
                // have to read the button to be sure it's state is delatched and not activate in a pause!
                if ( ! GameWorld.getInstance().getIsPaused()) {

//                    Vector2 axes = getAxes();

                    vehicleModel.updateControls(getAxisY(0), getAxisX(0),
                            (InputState.INP_B2 != preInputState && InputState.INP_B2 == nowInputState), // so dumb
                            0); // need to use Vector2

                    if (InputState.INP_ESC == nowInputState && InputState.INP_ESC != preInputState) {

                        GameWorld.getInstance().setIsPaused(true);
                        onscreenMenuTbl.setVisible(true);
                        cameraSwitch();
                        //                    gameEventSignal.dispatch(gameEvent.set(IS_PAUSED, null, 0));
                    }
                    if (InputState.INP_SELECT != preInputState && InputState.INP_SELECT == nowInputState) {

                        if (pointer.x < 0 && pointer.y < 0) {
                            // default to center of button
                            pickRayEventSignal.dispatch(gameEvent.set(
                                    RAY_PICK, setPickRay(gsBTNwidth / 2f, gsBTNheight / 2f), 0));
                        } else {
                            pickRayEventSignal.dispatch(gameEvent.set(RAY_PICK, setPickRay(pointer.x, pointer.y), 0));
                        }
                    }
                    if (InputState.INP_B2 != preInputState && InputState.INP_B2 == nowInputState) {
                    }
                } else {

                    if (InputState.INP_ESC == nowInputState && InputState.INP_ESC != preInputState) {

                        roundOver = true;
                    }
                    if (InputState.INP_SELECT != preInputState && InputState.INP_SELECT == nowInputState) {

                        GameWorld.getInstance().setIsPaused(false);
                        onscreenMenuTbl.setVisible(false);
                        cameraSwitch();
                    }
                }
                preInputState = nowInputState;
            }
        };

        // select the Steering Bullet Entity here and pass it to the character
/*
        SteeringEntity sbe = new SteeringEntity();
        final PlayerInput<Vector3> playerInpSB = new PlayerInput<Vector3>(mapper);
        sbe.setSteeringBehavior(playerInpSB);
        pickedPlayer.add(new CharacterComponent(sbe));
*/

        playerUI = new PlayerCharacter(mapper, null);
///*
        setupPlayerUI(mapper);
//*/
    }


    private void setupPlayerUI(final InputStruct mapper){

        InputListener gsListener = new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                mapper.setInputState(InputStruct.InputState.INP_SELECT, x, y);
  /*
                if (GameWorld.getInstance().getIsPaused()) {  // would like to allow engine to be actdive if ! paused but on-screen menu is up

                    GameWorld.getInstance().setIsPaused(false);
                    onscreenMenuTbl.setVisible(false);
                    cameraSwitch();
                } else {
                    pickRayEventSignal.dispatch(gameEvent.set(RAY_PICK, setPickRay(x, y), 0));
                }
*/
                return false;
            }};

        Pixmap pixmap;

        // Font files from ashley-superjumper
        font = new BitmapFont( Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(1.0f);

        fpsLabel = new Label("Whatever ... ", new Label.LabelStyle(font, Color.WHITE));
        playerUI.addActor(fpsLabel);

        Label onScreenMenuLabel = new Label("Paused", new Label.LabelStyle(font, Color.WHITE));
        onscreenMenuTbl.setFillParent(true);
        onscreenMenuTbl.setDebug(true);
        onscreenMenuTbl.add(onScreenMenuLabel).fillX().uniformX();

        //create a Labels showing the score and some credits
        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        uiSkin.add("white", new Texture(pixmap)); //https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java
        pixmap.dispose();

//        textStyle.font = font;
        uiSkin.add("default", new Label.LabelStyle(font, Color.WHITE));
        // Store the default libgdx font under the name "default".
        uiSkin.add("default", font);

        // Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = uiSkin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.down = uiSkin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.checked = uiSkin.newDrawable("white", Color.BLUE);
        textButtonStyle.over = uiSkin.newDrawable("white", Color.LIGHT_GRAY);
        textButtonStyle.font = uiSkin.getFont("default");
        uiSkin.add("default", textButtonStyle);

        // Create a button with the "default" TextButtonStyle. A 3rd parameter can be used to specify a name other than "default".
        TextButton textButton = new TextButton("Restart", uiSkin);
        onscreenMenuTbl.row().pad(10, 0, 10, 0);
        onscreenMenuTbl.add(textButton).fillX().uniformX();

        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
//                Gdx.app.exit();
//                GameWorld.getInstance().showScreen(new SplashScreen());
            }
        });

        onscreenMenuTbl.setVisible(false);
        playerUI.addActor(onscreenMenuTbl);

        TextureRegion myTextureRegion;
        TextureRegionDrawable myTexRegionDrawable;
        ImageButton button;
        Pixmap.setBlending(Pixmap.Blending.None);

        pixmap = new Pixmap(gsBTNwidth, gsBTNheight, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, .3f);
        pixmap.drawRectangle(0, 0, gsBTNwidth, gsBTNheight);
        gsTexture = new Texture(pixmap);
        myTextureRegion = new TextureRegion(gsTexture);
        myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);
        button = new ImageButton(myTexRegionDrawable);
        button.setPosition(gsBTNx, gsBTNy);
        button.addListener(gsListener);
        playerUI.addActor(button);
        pixmap.dispose();

        pixmap = new Pixmap(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 4, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, .3f);
        pixmap.drawRectangle(0, 0, Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 4);

        btnTexture = new Texture(pixmap);
        myTextureRegion = new TextureRegion(btnTexture);
        myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);
        button = new ImageButton(myTexRegionDrawable);
        button.setPosition(3f * Gdx.graphics.getWidth() / 4, 0);
        button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                mapper.setInputState(InputStruct.InputState.INP_B2);
                return false;
            }});
        playerUI.addActor(button);
        pixmap.dispose();
    }

    private Skin uiSkin = new Skin();
    private Table onscreenMenuTbl  = new Table();
    private Texture gsTexture;
    private Texture btnTexture;

    /*
     * this is kind of a hack to test some ray casting
     */
    private GameEvent nearestObjectToPlayerEvent = new GameEvent() {

        private Vector3 tmpV = new Vector3();
        private Vector3 posV = new Vector3();
        private GfxUtil lineInstance = new GfxUtil();
        /*
        we have no way to invoke a callback to the picked component.
        Pickable component required to implment some kind of interface to provide a
        callback method e.g.
          pickedComp = picked.getComponent(PickRayComponent.class).pickInterface.picked( blah foo bar)
          if (null != pickedComp.pickedInterface)
             pickInterface.picked( myEntityReference );
         */
        @Override
        public void callback(Entity picked, GameEvent.EventType eventType) {

            final btRigidBody btRigidBodyPlayer = pickedPlayer.getComponent(BulletComponent.class).body;

            if (RAY_DETECT == eventType && null != picked) {
                        RenderSystem.debugGraphics.add(lineInstance.lineTo(
                                        btRigidBodyPlayer.getWorldTransform().getTranslation(posV),
                                        picked.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmpV),
                                        Color.LIME));
            }}
    };

    private Vector3 position = new Vector3();
    private Quaternion rotation = new Quaternion();
    private Vector3 direction = new Vector3(0, 0, -1); // vehicle forward
    private Ray lookRay = new Ray();
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
        Gdx.gl.glViewport(0, 0, GAME_BOX_W, GAME_BOX_H);
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        camController.update(); // this can probaly be pause as well
        engine.update(delta);
//        mapper.update(delta);
/*
        if (GameWorld.getInstance().getIsPaused())  // ooh yuck have to force the update() because the system that updates it is paused!
            pickedPlayer.getComponent(CharacterComponent.class).steerable.update(delta); // hmmmmm ....we have no hook to do regular player update stuff? There used to be a player system ...
*/
        Matrix4 transform = pickedPlayer.getComponent(ModelComponent.class).modelInst.transform;
        transform.getTranslation(position);
        transform.getRotation(rotation);
        lookRay.set(position, ModelInstanceEx.rotateRad(direction.set(0, 0, -1), rotation));
        pickRayEventSignal.dispatch(nearestObjectToPlayerEvent.set(RAY_DETECT, lookRay, 0));


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
        if (false) {
            // calls new Formatter() which we dont want!
            s = String.format(Locale.ENGLISH, "%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 100, Gdx.graphics.getHeight());
            s = String.format(Locale.ENGLISH, "%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 250, Gdx.graphics.getHeight());
            s = String.format(Locale.ENGLISH, "%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 400, Gdx.graphics.getHeight());
        }
        batch.end();

            float visibleCount = renderSystem.visibleCount;
            float renderableCount = renderSystem.renderableCount;
            //s = String.format("fps=%d vis.cnt=%d rndrbl.cnt=%d", Gdx.graphics.getFramesPerSecond(), renderSystem.visibleCount, renderSystem.renderableCount);
            stringBuilder.setLength(0);
            stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
            stringBuilder.append(" Visible: ").append(visibleCount);
            stringBuilder.append(" / ").append(renderableCount);
///*
            fpsLabel.setText(stringBuilder);
//*/

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

if (GameWorld.getInstance().getIsPaused()) {
    // example of using ShapeRender to draw directly to screen
    //        shapeRenderer.setProjectionMatrix ????
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(hudOverlayColor);
    shapeRenderer.rect(0, 0, GAME_BOX_W, GAME_BOX_H);
    shapeRenderer.end();
}

        playerUI.act(Gdx.graphics.getDeltaTime());
        playerUI.draw();

        /* "Falling off platform" ... let a "sensor" or some suitable means to detect "fallen off platform" at which point, set gameOver.
          This way user can't pause during falling sequence. Once fallen past certain point, then allow screen switch.
         */
        if (roundOver) {
            roundOver = false;
            GameWorld.getInstance().showScreen(new MainMenuScreen());
        }
    }

    @Override
    public void show() {        // empty
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

    @Override
    public void dispose() {

        engine.removeSystem(bulletSystem); // make the system dispose its stuff
        engine.removeSystem(renderSystem); // make the system dispose its stuff
        engine.removeAllEntities(); // allow listeners to be called (for disposal)

        uiSkin.dispose();
///*
        btnTexture.dispose();
        gsTexture.dispose();
        font.dispose();
//*/
        batch.dispose();
        shapeRenderer.dispose();
        playerUI.dispose();
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
