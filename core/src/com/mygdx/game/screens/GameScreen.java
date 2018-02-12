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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.CharacterComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PlayerComponent;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.physObj;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.CameraSystem;
import com.mygdx.game.systems.CharacterSystem;
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

    private BulletComponent bulletComp; // tmp, debugging info

    private static final int GAME_BOX_W = Gdx.graphics.getWidth();
    private static final int GAME_BOX_H = Gdx.graphics.getHeight();

    private final Color hudOverlayColor = new Color(1, 0, 0, 0.2f);
    private Stage stage;
    private Touchpad touchpad;


    InputMultiplexer multiplexer;



    public GameScreen(MyGdxGame game) {

        this.game = game;

        environment = new Environment();
        environment.set(
                new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(
                new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));


        cam = new PerspectiveCamera(67, GAME_BOX_W, GAME_BOX_H);
        cam.position.set(3f, 7f, 10f);
        cam.lookAt(0, 4, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();


        // make sure add system first before other entity creation crap, so that the system can get entityAdded!
        addSystems();
        addEntities();

        cameraSystem.setCameraLocation(new Vector3(3, 7, 10), new Vector3(0, 4, 0));

        cameraSystem.setChaser(
                playerChaser.getComponent(CharacterComponent.class).transform,
                player.getComponent(ModelComponent.class).modelInst.transform);

        stage = addTouchPad();


        camController = new CameraInputController(cam);
//        camController = new FirstPersonCameraController(cam);

        multiplexer = new InputMultiplexer();
/*
        MyInputAdapter inputAdapter = new MyInputAdapter();
        inputAdapter.registerSystem(playerSystem);
        multiplexer.addProcessor(inputAdapter);
*/
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(camController);
        Gdx.input.setInputProcessor(multiplexer);



        // Font files from ashley-superjumper
        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(0.5f);

        // "guiCam" etc. lifted from 'Learning_LibGDX_Game_Development_2nd_Edition' Ch. 14 example
        guiCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        guiCam.position.set(guiCam.viewportWidth / 2f, guiCam.viewportHeight / 2f, 0);
        guiCam.update();
        batch = new SpriteBatch();
        //      box = new Sprite(new Texture("cube.png"));
        //      box = new Sprite();
        //      box.setPosition(0, 0);
        shapeRenderer = new ShapeRenderer();
    }


    /*
     * from "http://www.bigerstaff.com/libgdx-touchpad-example"
     */
    private Stage addTouchPad() {

        Touchpad.TouchpadStyle touchpadStyle;
        Skin touchpadSkin;
        Drawable touchBackground;
        Drawable touchKnob;
        Texture myTexture;
        TextureRegion myTextureRegion;
        TextureRegionDrawable myTexRegionDrawable;

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
        // touchpad.addListener ... https://gamedev.stackexchange.com/questions/127733/libgdx-how-to-handle-touchpad-input/127937#127937
        touchpad.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                /*          -1.0
                       -1.0   +   +1.0
                            + 1.0        */
                playerSystem.updateV(touchpad.getKnobPercentX(), -touchpad.getKnobPercentY());
            }
        });


        //https://gamedev.stackexchange.com/questions/121115/libgdx-simple-button-with-image
        myTexture = new Texture(Gdx.files.internal("data/myTexture.png"));
        myTextureRegion = new TextureRegion(myTexture);
        myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);
        ImageButton buttonA = new ImageButton(myTexRegionDrawable); //Set the buttonA up
        buttonA.setPosition(3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 6f);

        /*
         * https://gamedev.stackexchange.com/questions/81781/how-can-i-create-a-button-with-an-image-in-libgdx
         */
        buttonA.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                //Gdx.app.log("my app", "Pressed"); //** Usually used to start Game, etc. **//
                playerSystem.onJumpButton();
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

            }
        });

        ImageButton buttonB = new ImageButton(myTexRegionDrawable); //Set the buttonA up
        buttonB.setPosition((3 * Gdx.graphics.getWidth() / 4f) - 100f, (Gdx.graphics.getHeight() / 6f) - 100f);

        /*
         * https://gamedev.stackexchange.com/questions/81781/how-can-i-create-a-button-with-an-image-in-libgdx
         */
        buttonB.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                //Gdx.app.log("my app", "Pressed"); //** Usually used to start Game, etc. **//

                Matrix4 tmpM = new Matrix4();
                Vector3 tmpV = new Vector3();
                player.getComponent(BulletComponent.class).body.getWorldTransform(tmpM);
                PlayerSystem.updateChaseNode(tmpV, tmpM, 5.0f, 4.0f);
                Vector3 lookAt = new Vector3();
                tmpM.getTranslation(lookAt);
                boolean isController = cameraSystem.nextOpMode(tmpV, lookAt);

                if (isController)
                    multiplexer.addProcessor(camController);
                else
                    multiplexer.removeProcessor(camController);

                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

            }
        });

        //Create a Stage and add TouchPad
//		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true, batch);
        stage = new Stage();
        stage.clear();
        stage.addActor(touchpad);
        stage.addActor(buttonA); //Add the button to the stage to perform rendering and take input.
        stage.addActor(buttonB);
//        Gdx.input.setInputProcessor(stage);

        return stage;
    }


    private Entity player;
    private Entity playerChaser;

    void addEntities() {

        physObj.createEntities(engine);

        player = physObj.createPlayer(engine);
        PlayerComponent comp = player.getComponent(PlayerComponent.class);
        playerChaser = physObj.createPlayerChaser(engine, comp.chaseNode);

// tmp
        bulletComp = player.getComponent(BulletComponent.class);
    }

    private void addSystems() {

        Bullet.init(); // must be done before any bullet object can be created

        engine = new Engine();

        engine.addSystem(renderSystem = new RenderSystem(engine, environment, cam));
        engine.addSystem(bulletSystem = new BulletSystem(engine, cam));
        engine.addSystem(playerSystem = new PlayerSystem(this.game));
        cameraSystem = new CameraSystem(cam);
        engine.addSystem(cameraSystem);
        engine.addSystem(new CharacterSystem());
    }


    @Override
    public void show() {
    }


    @Override
    public void render(float delta) {

        camController.update();

        // game box viewport
        Gdx.gl.glViewport(0, 0, GAME_BOX_W, GAME_BOX_H);
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
            Vector3 forceVect = PlayerSystem.forceVect; // sonar warning "change this instance=reference to a static reference??
            String s;
            s = String.format("%+2.1f %+2.1f %+2.1f",
                    forceVect.x, forceVect.y, forceVect.z);
            font.draw(batch, s, 100, Gdx.graphics.getHeight());

            s = String.format("%+2.1f %+2.1f", 0f, 0f);
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
        shapeRenderer.rect(0, 0, GAME_BOX_W, GAME_BOX_H / 4.0f);
        shapeRenderer.end();
        shapeRenderer.end();


        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
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

        stage.dispose();
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
