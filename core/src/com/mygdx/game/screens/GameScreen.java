package com.mygdx.game.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.PlayerComponent;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.CameraSystem;
import com.mygdx.game.systems.PlayerSystem;
import com.mygdx.game.systems.RenderSystem;


/**
 * Created by mango on 12/18/17.
 */

public class GameScreen implements Screen {

    private MyGdxGame game;

    private Engine engine;
    private BulletSystem bulletSystem; //for invoking removeSystem (dispose)
    private RenderSystem renderSystem; //for invoking removeSystem (dispose)
    private PlayerSystem playerSystem; //for reference to player entity
    private CameraSystem cameraSystem;

    private PerspectiveCamera cam;

    private CameraInputController camController;
    //    public FirstPersonCameraController camController;
    private Environment environment;

    //    Sprite box;
    private BitmapFont font;
    private OrthographicCamera guiCam;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private PlayerComponent playerComp;
    private BulletComponent bulletComp;
//private ModelComponent modelComp;
    //    private btRigidBody playerBody;

    public static final int touchBoxW = Gdx.graphics.getWidth() / 4;
    public static final int touchBoxH = touchBoxW; // Gdx.graphics.getHeight() / 4;
    public static final int gameBoxW = Gdx.graphics.getWidth();
    //    private static final int gameBoxH = Gdx.graphics.getHeight() - touchBoxH;
    public static final int gameBoxH = Gdx.graphics.getHeight();

    private final Color hudOverlayColor = new Color(1, 0, 0, 0.2f);




    public GameScreen(MyGdxGame game) {

        this.game = game;

        environment = new Environment();
        environment.set(
                new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(
                new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        cam = new PerspectiveCamera(67, gameBoxW, gameBoxH);
        cam.position.set(3f, 7f, 10f);
        cam.lookAt(0, 4, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();


        camController = new CameraInputController(cam);
//        camController = new FirstPersonCameraController(cam);


/*
        MyInputAdapter inputAdapter = new MyInputAdapter();
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(inputAdapter);
        multiplexer.addProcessor(camController);
        Gdx.input.setInputProcessor(multiplexer);
*/
addTouchPad();


        // make sure add system first before other entity creation crap, so that the system can get entityAdded!
        addSystems();
        addEntities();

/*
        inputAdapter.registerSystem(playerSystem);
*/

        // Font files from ashley-superjumper
        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(0.5f);

        guiCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        guiCam.position.set(guiCam.viewportWidth / 2f, guiCam.viewportHeight / 2f, 0);
        guiCam.update();
        batch = new SpriteBatch();
        //      box = new Sprite(new Texture("cube.png"));
        //      box = new Sprite();
        //      box.setPosition(0, 0);
        shapeRenderer = new ShapeRenderer();
    }



    private Stage stage;
    private Touchpad touchpad;
    private Touchpad.TouchpadStyle touchpadStyle;
    private Skin touchpadSkin;
    private Drawable touchBackground;
    private Drawable touchKnob;

    /*
     * from "http://www.bigerstaff.com/libgdx-touchpad-example"
     */
    void addTouchPad()
    {
        //Create a touchpad skin
        touchpadSkin = new Skin();
        //Set background image
        touchpadSkin.add("touchBackground", new Texture("data/touchBackground.png"));
        //Set knob image
        touchpadSkin.add("touchKnob", new Texture("data/touchKnob.png"));
        //Create TouchPad Style
        touchpadStyle = new Touchpad.TouchpadStyle();
        //Create Drawable's from TouchPad skin
        touchBackground = touchpadSkin.getDrawable("touchBackground");
        touchKnob = touchpadSkin.getDrawable("touchKnob");
        //Apply the Drawables to the TouchPad Style
        touchpadStyle.background = touchBackground;
        touchpadStyle.knob = touchKnob;
        //Create new TouchPad with the created style
        touchpad = new Touchpad(10, touchpadStyle);
        //setBounds(x,y,width,height)
        touchpad.setBounds(15, 15, 200, 200);

        //Create a Stage and add TouchPad
//		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true, batch);
        stage = new Stage();
        stage.addActor(touchpad);
        Gdx.input.setInputProcessor(stage);
    }



    void addEntities() {

        physObj.createEntities(engine);

        Entity plyr = physObj.createPlayer(engine);

        cameraSystem.setSubject(plyr);

        playerComp = playerSystem.playerEntity.getComponent(PlayerComponent.class);
        bulletComp = playerSystem.playerEntity.getComponent(BulletComponent.class);
    }

    private void addSystems() {

        Bullet.init(); // must be done before any bullet object can be created

        engine = new Engine();

        engine.addSystem(renderSystem = new RenderSystem(engine, environment, cam));
        engine.addSystem(bulletSystem = new BulletSystem(engine, cam));
        engine.addSystem(playerSystem = new PlayerSystem(this.game));
        cameraSystem = new CameraSystem(cam);
        engine.addSystem(cameraSystem);
    }


    @Override
    public void show() {
    }


    @Override
    public void render(float delta) {

        camController.update();

        // game box viewport
//        Gdx.gl.glViewport(0, touchBoxH, gameBoxW, gameBoxH);
        Gdx.gl.glViewport(0, 0, gameBoxW, gameBoxH);
        //         Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        engine.update(delta);

        // GUI viewport (full screen)
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();

        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, Gdx.graphics.getHeight());

        //if (null != playerBody)
        {
            btRigidBody playerBody = bulletComp.body;
            String s;
            s = String.format("%+2.1f %+2.1f %+2.1f",
//                    playerBody.getLinearVelocity().x, playerBody.getLinearVelocity().y, playerBody.getLinearVelocity().z);
                    playerComp.vVelocity.x, playerComp.vVelocity.y, playerComp.vVelocity.z);
            font.draw(batch, s, 100, Gdx.graphics.getHeight());

            s = String.format("%+2.1f %+2.1f %+2.1f",
                    playerComp.vvv.x, playerComp.vvv.y, playerComp.vvv.z);
            font.draw(batch, s, 250, Gdx.graphics.getHeight());

            Matrix4 mmm = bulletComp.motionstate.transform;
            Quaternion r = new Quaternion();
            mmm.getRotation(r);
            r = bulletComp.body.getOrientation(); /// same as getRotation?
            s = String.format("%+2.1f %+2.1f %+2.1f", r.getPitch(), r.getYaw(), r.getRoll());
            font.draw(batch, s, 400, Gdx.graphics.getHeight());
        }
//        box.draw(batch);

        batch.end();

//        shapeRenderer.setProjectionMatrix ????
        // semi-opaque filled box over touch area
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(hudOverlayColor);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), touchBoxH);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(
                Gdx.graphics.getWidth() / 2.0f - touchBoxW / 2.0f, 0, touchBoxW, touchBoxH);
        shapeRenderer.circle(Gdx.graphics.getWidth() / 2.0f, touchBoxH / 2.0f, 10.0f);
        shapeRenderer.circle(Gdx.graphics.getWidth() / 2.0f, touchBoxH / 2.0f, touchBoxH / 2.0f);
        shapeRenderer.end();


        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

// hack!
        playerSystem.updateV(touchpad.getKnobPercentX(), -touchpad.getKnobPercentY());
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void dispose() {

        engine.removeSystem(bulletSystem); // make the system dispose its stuff
        engine.removeSystem(renderSystem); // make the system dispose its stuff

        physObj.dispose(); // static dispose models

        engine.removeAllEntities(); // allow listeners to be called (for disposal)

        font.dispose();
        batch.dispose();
        shapeRenderer.dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}
