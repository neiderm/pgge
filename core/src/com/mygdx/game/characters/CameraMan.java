package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.controllers.SteeringEntity;
import com.mygdx.game.screens.IUserInterface;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.ModelInstanceEx;

import static com.mygdx.game.util.GameEvent.EventType.RAY_DETECT;
import static com.mygdx.game.util.GameEvent.EventType.RAY_PICK;


/**
 * Created by mango on 2/4/18.
 * <p>
 * - work on avoiding jitter experienced when subject is basically still:
 * need the actor/subject to provide its velocity and don't move camera until
 * minimum amount of velocity exceeded
 * <p>
 * Can camera have a kinematic body to keep it from going thru dynamic bodies, but yet that
 * wouldn't exert any forces on other bodies?
 * <p>
 * <p>
 * ultimately, i should have a camera system that can be a listener on any entity
 * (having a transform) to lookat and track.
 * for now .... lookat player and track a position relative to it
 * <p>
 * LATEST IDEA:
 * multiple camera system instances and types ...
 */

public class CameraMan extends SteeringEntity {

    private Signal<GameEvent> gameEventSignal; // signal queue of pickRaySystem
    private GameEvent gameEvent; // stored in Character comp but does it need to be?
    private Ray pickRay;
    private PerspectiveCamera cam;
    private CameraOpMode cameraOpMode = CameraOpMode.FIXED_PERSPECTIVE;
    private int nodeIndex = 0;

    // tmp variables
    private Vector3 tmpPosition = new Vector3();
    private Vector3 tmpLookAt = new Vector3();


    // https://stackoverflow.com/questions/17664445/is-there-an-increment-operator-for-java-enum/17664546
    public enum CameraOpMode {
        FIXED_PERSPECTIVE,
        CHASE
// TODO: fixed+lookAT (only implementes the facing part of seek behavior i.e. doesn' update position
                // FP_PERSPECTIVE,
                // FOLLOW
                // ABOVE
                {
                    @Override
                    public CameraOpMode getNext() {
                        return values()[0]; // rollover to the first
                    }
                };

        public CameraOpMode getNext() {
            return this.ordinal() < CameraOpMode.values().length - 1
                    ? CameraOpMode.values()[this.ordinal() + 1]
                    : CameraOpMode.values()[0];
        }
    }

    private static final int FIXED = 1; // idfk

    private static class CameraNode {

        private Matrix4 positionRef;
        private Matrix4 lookAtRef;
        private int flags; // e.g. FIXED_PERSPECTIVE

        CameraNode(int flags, Matrix4 positionRef, Matrix4 lookAtRef) {
            this.flags = flags;
            this.positionRef = positionRef;
            this.lookAtRef = lookAtRef;
        }

        Matrix4 getPositionRef() {
            return positionRef;
        }

        Matrix4 getLookAtRef() {
            return lookAtRef;
        }
    }

    private ArrayMap<String, CameraNode> cameraNodes =
            new ArrayMap<String, CameraNode>(String.class, CameraNode.class);


    private void setCameraNode(String key, Matrix4 posM, Matrix4 lookAtM, int flags) {

        CameraNode cameraNode = new CameraNode(flags, posM, lookAtM);

        int index = cameraNodes.indexOfKey(key);
        if (-1 != index) {
            cameraNodes.setValue(index, cameraNode);
        } else
            cameraNodes.put(key, cameraNode);
    }

    private boolean setOpModeByKey(String key) {
        int index = cameraNodes.indexOfKey(key);
        if (index > -1) {
            return setOpModeByIndex(index);
        }
        return false;
    }

    public boolean nextOpMode() {
        int index = nodeIndex;
        if (++index >= cameraNodes.size) {
            index = 0;
        }
        return setOpModeByIndex(index);
    }

    private boolean setOpModeByIndex(int index) {

        CameraNode prevNode = cameraNodes.getValueAt(nodeIndex);
        nodeIndex = index;

        boolean isController = false;
        CameraNode node = cameraNodes.getValueAt(index);
        cameraOpMode = CameraOpMode.CHASE;
        Vector3 tmp = cam.position.cpy();

        if (node.flags == FIXED) {

            cameraOpMode = CameraOpMode.FIXED_PERSPECTIVE;
// offset the cam position on y axis. LookAt doesn't change so grab it from the previous node.
            tmp.y += 1;
            setCameraLocation(tmp, prevNode.getLookAtRef().getTranslation(tmpLookAt));

            isController = true;

        } else {
            // set the target node to previous camera position, allowing it to zoom in from wherever it was fixed to
            // this would be nicer if we could "un-stiffen" the control gain during this zoom!
            node.getPositionRef().setToTranslation(tmp);
        }
        return isController;
    }

    private void setCameraLocation(Vector3 position, Vector3 lookAt) {

        cam.position.set(position);
        cam.lookAt(lookAt);
        cam.up.set(0, 1, 0); // googling ... Beginning Java Game Development with LibGDX ... lookAt may have undesired result of tilting camera left or right
        cam.update();
    }

    private CameraMan(Entity cameraMan, Signal<GameEvent> gameEventSignal, PerspectiveCamera cam,
                      Vector3 positionV, Vector3 lookAtV, GameEvent event) {

        this.pickRay =  new Ray();
        CharacterComponent comp = new CharacterComponent(this, this.pickRay );
        cameraMan.add(comp);

        this.gameEvent = event;
        this.gameEventSignal = gameEventSignal;
//        this.pickRay = comp.lookRay;
        this.cam = cam;

        setCameraLocation(positionV, lookAtV);
    }

    public CameraMan(Entity cameraMan, IUserInterface stage, Signal<GameEvent> gameEventSignal,
                     PerspectiveCamera cam, Vector3 positionV, Vector3 lookAtV, Matrix4 tgtTransfrm) {

        final GameEvent event = new GameEvent() {
            /*
    create a game event object for signalling to pickray system.     modelinstance reference doesn't belong in here but we could
        simply have the "client" of this class pass a gameEvent along witht the gameEventSignal into the constructor.
         */
            @Override
            public void callback(Entity picked, EventType eventType) {
                switch (eventType) {
                    case RAY_PICK:
                        if (null != picked)
                            ModelInstanceEx.setMaterialColor(
                                    picked.getComponent(ModelComponent.class).modelInst, Color.RED);
                        break;
                    default:
                        break;
                }
            }
        };

        Matrix4 camTransform = new Matrix4();

        this.cam = cam;
        setCameraNode("fixed", null, null, FIXED); // don't need transform matrix for fixed camera
        setCameraNode("chaser1", camTransform, tgtTransfrm, 0);
        setOpModeByKey("chaser1");

        setSteeringBehavior(new TrackerSB<Vector3>(this, tgtTransfrm, camTransform, /*spOffs*/new Vector3(0, 2, 3)));

        this.pickRay = new Ray();
        CharacterComponent comp = new CharacterComponent(this, this.pickRay);
        cameraMan.add(comp);

        this.gameEvent = event;
        this.gameEventSignal = gameEventSignal;
//        this.pickRay = comp.lookRay;

        setCameraLocation(positionV, lookAtV);

        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap button = new Pixmap(150, 150, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(75, 75, 75);   /// I don't know how you would actually do a circular touchpad area like this
        stage.addInputListener(buttonGSListener, button, (Gdx.graphics.getWidth() / 2f) - 75, (Gdx.graphics.getHeight() / 2f) + 0);
        button.dispose();
    }

    public CameraMan(Entity cameraMan, IUserInterface stage, Signal<GameEvent> gameEventSignal,
                     PerspectiveCamera cam, Vector3 positionV, Vector3 lookAtV, GameEvent event) {

        this(cameraMan, gameEventSignal, cam, positionV, lookAtV, event);

//        setOpModeByKey("fixed"); // this returns FALSE here so it is apparently useless!

        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap button = new Pixmap(150, 150, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillRectangle(0, 0, 150, 150);
        stage.addInputListener(buttonGSListener, button,
                (Gdx.graphics.getWidth() / 2f) - 75, (Gdx.graphics.getHeight() / 2f) + 0);
        button.dispose();
    }

    @Override
    protected void applySteering(SteeringAcceleration<Vector3> steering, float deltaTime) {

        CameraNode node;

        if (CameraOpMode.FIXED_PERSPECTIVE == cameraOpMode) {
            // nothing
        } else if (CameraOpMode.CHASE == cameraOpMode) {

            node = cameraNodes.getValueAt(nodeIndex);

            setCameraLocation(
                    node.getPositionRef().getTranslation(tmpPosition),
                    node.getLookAtRef().getTranslation(tmpLookAt));
        }
    }

    /*
 "gun sight" will be draggable on the screen surface, then click to pick and/or shoot that direction
  */
    private final InputListener buttonGSListener = new InputListener() {

        private Ray setPickRay(float x, float y) {
            // offset button x,y to screen x,y (button origin on bottom left) (should not have screen/UI geometry crap in here!)
            float nX = (Gdx.graphics.getWidth() / 2f) + (x - 75);
            float nY = (Gdx.graphics.getHeight() / 2f) - (y - 75) - 75;
            Ray rayTmp = cam.getPickRay(nX, nY);
            return pickRay.set(rayTmp.origin, rayTmp.direction);
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) { /*empty*/ }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            // only do this if FPV mode (i.e. cam controller is not handling game window input)
//            if (!isController) // TODO: remove the GS from the gameUI if !isController (in GameScreen)
            {
                gameEventSignal.dispatch(gameEvent.set(RAY_PICK, setPickRay(x, y), 0));
                //Gdx.app.log(this.getClass().getName(), String.format("GS touchDown x = %f y = %f, id = %d", x, y, id));
            }
            return true;
        }
    };
}
