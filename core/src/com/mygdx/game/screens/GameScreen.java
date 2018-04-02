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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PlayerComponent;
import com.mygdx.game.GamePad;
import com.mygdx.game.GameWorld;
import com.mygdx.game.SceneLoader;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.CameraSystem;
import com.mygdx.game.systems.CharacterSystem;
import com.mygdx.game.systems.PlayerSystem;
import com.mygdx.game.systems.RenderSystem;

import static com.mygdx.game.EntityBuilder.loadDynamicEntity;
import static com.mygdx.game.EntityBuilder.loadKinematicEntity;
import static com.mygdx.game.EntityBuilder.loadStaticEntity;

/**
 * Created by mango on 12/18/17.
 */

public class GameScreen implements Screen {

    private SceneLoader sceneLoader = SceneLoader.instance;
    private Engine engine;
    private BulletSystem bulletSystem; //for invoking removeSystem (dispose)
    private RenderSystem renderSystem; //for invoking removeSystem (dispose)
    private PlayerSystem playerSystem; //for reference to player entity
    private CameraSystem cameraSystem;

    private PerspectiveCamera cam;

    private CameraInputController camController;
    //    public FirstPersonCameraController camController;
    private Environment environment;

    private BitmapFont font;
    private OrthographicCamera guiCam;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private BulletComponent bulletComp; // tmp, debugging info
    private PlayerComponent playerComp; // tmp, debugging info

    private static final int GAME_BOX_W = Gdx.graphics.getWidth();
    private static final int GAME_BOX_H = Gdx.graphics.getHeight();

    private final Color hudOverlayColor = new Color(1, 0, 0, 0.2f);
    private GamePad stage;

    private InputMultiplexer multiplexer;

    private StringBuilder stringBuilder = new StringBuilder();
    private Label label;


    public GameScreen(GameWorld world) {

        this.engine = world.engine;

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
        addEntities(); // this takes a long time!

        stage = new GamePad(
                playerSystem.touchPadChangeListener,
                playerSystem.actionButtonListener,
                buttonBListener);

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

        label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        stage.addActor(label);


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


    public final InputListener buttonBListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

            boolean isController = cameraSystem.nextOpMode();

            if (isController)
                multiplexer.addProcessor(camController);
            else
                multiplexer.removeProcessor(camController);

            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

        }
    };


    public static void loadDynamicEntiesByName(
            Engine engine, Model model, String node, float mass, btCollisionShape shape ) {

        for (int i = 0; i < model.nodes.size; i++) {
            String id = model.nodes.get(i).id;
            if (id.startsWith(node)) {
                engine.addEntity(loadDynamicEntity(model, shape, id, mass, null, null));
            }
        }
    }

    private void createTestObjects(){

        Vector3 size;
        float yTrans = -10.0f;
        Vector3 trans = new Vector3(0, -4 + yTrans, 0);
        Entity e;

        size = new Vector3(40, 2, 40); // TODO: how to get size from modelinstance
        btCollisionShape shape =  new btBoxShape(size.cpy().scl(0.5f)); // convex hull NOT working with scale right now
        e = loadKinematicEntity(sceneLoader.boxTemplateModel, null, shape, trans, size);
        engine.addEntity(e);

        float radius = 16;
        trans = new Vector3(10, 5 + yTrans, 0);
        shape = new btSphereShape(radius * 0.5f);
        size.set(radius, radius, radius);
        e = loadKinematicEntity(sceneLoader.sphereTemplateModel, null, shape, trans, size);
        engine.addEntity(e);

        if (false) { // this slows down bullet debug drawer considerably!

            e = loadKinematicEntity(
                    sceneLoader.landscapeModel, null,
                    new btBvhTriangleMeshShape(sceneLoader.landscapeModel.meshParts), null, null);

            // put the landscape at an angle so stuff falls of it...
            ModelInstance inst = e.getComponent(ModelComponent.class).modelInst;
            inst.transform.idt().rotate(new Vector3(1, 0, 0), 20f).trn(0, 0 + yTrans, 0);

            e.getComponent(BulletComponent.class).body.setWorldTransform(inst.transform);
            engine.addEntity(e);
        }

        // TODO: how to get size from modelinstance
        size = new Vector3(2f, 1f, 1.5f); // TODO: how to get size from modelinstance
        shape = null; // new btBoxShape(size.cpy().scl(0.5f));
        loadDynamicEntiesByName(engine, sceneLoader.testCubeModel, "Crate", 0.1f, shape);



        size = new Vector3(40, 2, 40); // TODO: how to get size from modelinstance
        shape = null; // new btBoxShape(size.cpy().scl(0.5f))
//        sceneLoader.loadKinematicEntity(engine, sceneLoader.sceneModel, "Platform", new btBoxShape(size.cpy().scl(0.5f)));
        engine.addEntity(loadKinematicEntity(sceneLoader.testCubeModel, "Platform001", shape, null, null));

        engine.addEntity(loadStaticEntity(sceneLoader.testCubeModel, "Cube"));
    }


    private void addEntities() {

        sceneLoader.createEntities(engine);

if (true)                createTestObjects();
else        sceneLoader.createTestObjects(engine);


        btCollisionShape boxshape = null; // new btBoxShape(new Vector3(0.5f, 0.35f, 0.75f)); // test ;)
//        Entity player = loadDynamicEntity(sceneLoader.sceneModel, boxshape, "ship", 5.1f, null, null);
        Entity player = loadDynamicEntity(
                sceneLoader.shipModel, boxshape, null, 5.1f, new Vector3(0, 15f, -5f), null);
        player.add(new PlayerComponent());
        engine.addEntity(player);

        Entity skybox = loadStaticEntity(sceneLoader.sceneModel, "space");
        skybox.getComponent(ModelComponent.class).isShadowed = false; // disable shadowing of skybox
        engine.addEntity(skybox);


        Entity playerChaser;
/*
        playerChaser = SceneLoader.createChaser2(engine, comp.chaseNode);

        cameraSystem.setCameraNode("chaser2",
                new CameraSystem.CameraNode(
                        playerChaser.getComponent(CharacterComponent.class).transform,
                        player.getComponent(ModelComponent.class).modelInst.transform
                ));
*/
///*
        Matrix4 plyrTransform = player.getComponent(ModelComponent.class).modelInst.transform;

        playerChaser = sceneLoader.createChaser1(engine, plyrTransform);

        cameraSystem.setCameraNode("chaser1",
                playerChaser.getComponent(ModelComponent.class).modelInst.transform,
                player.getComponent(ModelComponent.class).modelInst.transform);
        //*/

// tmp
        bulletComp = player.getComponent(BulletComponent.class);
        playerComp = player.getComponent(PlayerComponent.class);
// playerComp.died = false;
    }

    private void addSystems() {

        Bullet.init(); // must be done before any bullet object can be created

        engine.addSystem(renderSystem = new RenderSystem(engine, environment, cam));
        engine.addSystem(bulletSystem = new BulletSystem(engine, cam));
        engine.addSystem(playerSystem = new PlayerSystem());
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
                 Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        engine.update(delta);

        // GUI viewport (full screen)
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();

        //if (null != playerBody)
        {
            Vector3 forceVect = PlayerSystem.forceVect; // sonar warning "change this instance=reference to a static reference??
            String s;
            s = String.format("%+2.1f %+2.1f %+2.1f",
                    forceVect.x, forceVect.y, forceVect.z);
            font.draw(batch, s, 100, Gdx.graphics.getHeight());

            s = String.format("%+2.1f %+2.1f %+2.1f",
                    playerComp.down.x, playerComp.down.y, playerComp.down.z);
            font.draw(batch, s, 250, Gdx.graphics.getHeight());

            Matrix4 mmm = bulletComp.motionstate.transform;
            Quaternion r = new Quaternion();
            mmm.getRotation(r);
            r = bulletComp.body.getOrientation(); /// same as getRotation?
            s = String.format("%+2.1f %+2.1f %+2.1f", r.getPitch(), r.getYaw(), r.getRoll());
            font.draw(batch, s, 400, Gdx.graphics.getHeight());
        }

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


        stringBuilder.setLength(0);
        stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
        stringBuilder.append(" Visible: ").append(renderSystem.visibleCount);
        stringBuilder.append(" / ").append(renderSystem.renderableCount);

        label.setText(stringBuilder);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void dispose() {

        trash();

        // HACKME HACK HACK
        if (!isPaused) {
    sceneLoader.dispose(); // static dispose models
}
    }

    void trash(){
        engine.removeSystem(bulletSystem); // make the system dispose its stuff
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
    private boolean isPaused = false;

    @Override
    public void pause() {
// android "home", "back", or "left" button all send ApplicationListener.pause() notifcation (called by Game.pause()
        isPaused = true;
    }

    @Override
    public void resume() {
        isPaused  = false; // android clicked app icon or from "left button"
    }

    @Override
    public void hide() {
        // Game.dispose calls screen.hide()
        trash();
    }
}
