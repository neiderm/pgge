package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Gdx;
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
import com.mygdx.game.components.ControllerComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.controllers.ICharacterControlAuto;
import com.mygdx.game.controllers.PIDcontrol;
import com.mygdx.game.screens.IUserInterface;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.ModelInstanceEx;

import static com.mygdx.game.util.GameEvent.EventType.RAY_DETECT;


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
 * <p>
 * <p>
 * Perspective type, most basic
 * <p>
 * Chase type would be constructed with a reference to the chasee
 */

public class CameraMan implements IGameCharacter {

    private Signal<GameEvent> gameEventSignal; // signal queue of pickRaySystem
    private GameEvent gameEvent; // stored in Character comp but does it need to be?
    private Ray pickRay;
    private PerspectiveCamera cam;

    // https://stackoverflow.com/questions/17664445/is-there-an-increment-operator-for-java-enum/17664546
    public enum CameraOpMode {
        FIXED_PERSPECTIVE,
        CHASE
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

/*        public CameraNode(Matrix4 positionRef, Matrix4 lookAtRef){
            this(FIXED, positionRef, lookAtRef);
        }*/

        CameraNode(int flags, Matrix4 positionRef, Matrix4 lookAtRef) {
            this.flags = flags;
            this.positionRef = positionRef;
///*
            this.lookAtRef = lookAtRef;
//*/
        }
    }

    private ArrayMap<String, CameraNode> cameraNodes =
            new ArrayMap<String, CameraNode>(String.class, CameraNode.class);
    private Matrix4 camPositionMatrix = new Matrix4();


    //TODO: ---> ControllerComponent!!!!
    private ICharacterControlAuto pidControl;


    /*
     * all this could be in constructor??????
     *  IN: key
     *  IN: lookAtM - typically a reference to the players transformation matrix (but could it be just the position vector? )
     */

    private void setCameraNode(String key, Matrix4 posM, Matrix4 lookAtM, int flags) {

        CameraNode cameraNode = new CameraNode(flags, posM, lookAtM);

        int index = cameraNodes.indexOfKey(key);
        if (-1 != index) {
            cameraNodes.setValue(index, cameraNode);
        } else
            cameraNodes.put(key, cameraNode);
    }

    // remove camera node


    private CameraOpMode cameraOpMode = CameraOpMode.FIXED_PERSPECTIVE;

    // these reference whatever the camera is supposed to be chasing
    private Matrix4 positionMatrixRef;
    private Matrix4 lookAtMatrixRef;

    private int nodeIndex = 0;
    private boolean isController;


    /*
     */
    public boolean setOpModeByKey(String key) {
        int index = cameraNodes.indexOfKey(key);

        if (index > -1) {
            nodeIndex = index;
            return setOpModeByIndex(index);
        }
        return false;
    }


    public boolean nextOpMode() {

        if (++nodeIndex >= cameraNodes.size) {
            nodeIndex = 0;
        }
        return setOpModeByIndex(nodeIndex);
    }

    private boolean setOpModeByIndex(int index) {

        isController = false;
        CameraNode node = cameraNodes.getValueAt(index);
        cameraOpMode = CameraOpMode.CHASE;
        Vector3 tmp = cam.position.cpy();

        if (node.flags == FIXED) {

            cameraOpMode = CameraOpMode.FIXED_PERSPECTIVE;

            tmp.y += 1;
            setCameraLocation(tmp, currentLookAtV);

            isController = true;

            // they way it's setup right now, these don't matter for fixed camera
            positionMatrixRef = null;
            lookAtMatrixRef = null;

        } else {

            // set working refs to the selected node
            positionMatrixRef = node.positionRef;
            lookAtMatrixRef = node.lookAtRef;

            // set the target node to previous camera position, allowing it to zoom in from wherever it was fixed to
            // this would be nicer if we could "un-stiffen" the control gain during this zoom!
            positionMatrixRef.setToTranslation(tmp);
        }
        return isController;
    }


    private void setCameraLocation(Vector3 position, Vector3 lookAt) {

        cam.position.set(position);
        cam.lookAt(lookAt);
        cam.up.set(0, 1, 0); // googling ... Beginning Java Game Development with LibGDX ... lookAt may have undesired result of tilting camera left or right
        cam.update();

        currentPositionV.set(position);
        currentLookAtV.set(lookAt);
    }


    private CameraMan(Entity cameraMan, Signal<GameEvent> gameEventSignal, PerspectiveCamera cam) {


//        pidControl = new PIDcontrol(lookAtM, camPositionMatrix, new Vector3(0, 2, 3), 0.1f, 0, 0);


        CharacterComponent comp = new CharacterComponent(this, gameEventSignal,
        /* create us a game event object for signalling to pickray system.     modelinstance reference doesn't belong in here but we could
    simply have the "client" of this class pass a gameEvent along witht the gameEventSignal into the constructor.
     */
                new GameEvent() {
                    @Override
                    public void callback(Entity picked, EventType eventType) {
                        switch (eventType) {
                            case RAY_DETECT:
                                if (null != picked)
                                    ModelInstanceEx.setMaterialColor(
                                            picked.getComponent(ModelComponent.class).modelInst, Color.RED);
                                break;
                            case RAY_PICK:
                            default:
                                break;
                        }
                    }
                });

        cameraMan.add(comp);
        this.gameEvent = comp.gameEvent;
        this.gameEventSignal = gameEventSignal;
        this.pickRay = comp.lookRay;
        this.cam = cam;

//        Vector3 posV = new Vector3();
//        Vector3 lookAtV = new Vector3();
// we don't really use the transform matrix for fixed camera
//        Matrix4 pos = new Matrix4();
//        Matrix4 look = new Matrix4();
//        pos.setToTranslation(posV);
//        look.setToTranslation(lookAtV);
//        setCameraNode("fixed", pos, look, FIXED);

        setCameraNode("fixed", null, null, FIXED);
        setCameraLocation(new Vector3(), new Vector3());
    }

    public CameraMan(Entity cameraMan, IUserInterface stage, Signal<GameEvent> gameEventSignal,
                     PerspectiveCamera cam, Vector3 positionV, Vector3 lookAtV, ControllerComponent comp) {

        this(cameraMan, gameEventSignal, cam);

        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap button = new Pixmap(150, 150, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(75, 75, 75);   /// I don't know how you would actually do a circular touchpad area like this
        stage.addButton(buttonGSListener, button, (Gdx.graphics.getWidth() / 2f) - 75, (Gdx.graphics.getHeight() / 2f) + 0);

        setCameraLocation(positionV, lookAtV);
        setCameraNode("chaser1", camPositionMatrix, comp.transform, 0);
        pidControl = new PIDcontrol(comp.transform, camPositionMatrix, new Vector3(0, 2, 3), 0.1f, 0, 0);
    }

    public CameraMan(Entity cameraMan, IUserInterface stage, Signal<GameEvent> gameEventSignal,
                     PerspectiveCamera cam, Vector3 positionV, Vector3 lookAtV,
                     ControllerComponent cc,
                     GameEvent event) {

        CharacterComponent comp = new CharacterComponent(this, gameEventSignal, event);
        cameraMan.add(comp);
        this.gameEvent = comp.gameEvent;
        this.gameEventSignal = gameEventSignal;
        this.pickRay = comp.lookRay;
        this.cam = cam;
//        setCameraNode("fixed", null, null, FIXED);
//        setCameraLocation(new Vector3(), new Vector3());

        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap button = new Pixmap(150, 150, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillRectangle(0, 0, 150, 150);
        stage.addButton(buttonGSListener, button,
                (Gdx.graphics.getWidth() / 2f) - 75, (Gdx.graphics.getHeight() / 2f) + 0);

        setCameraLocation(positionV, lookAtV);
        cc.transform = new Matrix4(); /* doesn't matter */
        setCameraNode("chaser1", camPositionMatrix, cc.transform, 0);
        pidControl = new PIDcontrol(cc.transform, camPositionMatrix, new Vector3(0, 2, 3), 0.1f, 0, 0);
    }


    private Vector3 currentPositionV = new Vector3();
    private Vector3 currentLookAtV = new Vector3();


    @Override
    public void update(Entity entity, float delta, Object whatever) {

// TODO: add a proper ControllerComponent to this entity!!!!!!!
        if (null != pidControl)
            pidControl.update(delta);

        if (CameraOpMode.FIXED_PERSPECTIVE == cameraOpMode) {
            // nothing
        } else if (CameraOpMode.CHASE == cameraOpMode) {
            positionMatrixRef.getTranslation(currentPositionV);
// if (null != lookAtMatrixRef) // ???
            lookAtMatrixRef.getTranslation(currentLookAtV);
            setCameraLocation(currentPositionV, currentLookAtV);
        }
    }

    /*
 "gun sight" will be draggable on the screen surface, then click to pick and/or shoot that direction
  */
    public final InputListener buttonGSListener = new InputListener() {

        private void setPickRay(float x, float y) {
            // offset button x,y to screen x,y (button origin on bottom left) (should not have screen/UI geometry crap in here!)
            float nX = (Gdx.graphics.getWidth() / 2f) + (x - 75);
            float nY = (Gdx.graphics.getHeight() / 2f) - (y - 75) - 75;
            Ray rayTmp = cam.getPickRay(nX, nY);
            pickRay.set(rayTmp.origin, rayTmp.direction);
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) { /*empty*/ }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            // only do this if FPV mode (i.e. cam controller is not handling game window input)
//            if (!isController) // TODO: remove the GS from the gameUI if !isController (in GameScreen)
            {
                setPickRay(x, y);
                gameEventSignal.dispatch(gameEvent.set(RAY_DETECT, pickRay, 0));
                //Gdx.app.log(this.getClass().getName(), String.format("GS touchDown x = %f y = %f, id = %d", x, y, id));
            }
            return true;
        }
    };
}
