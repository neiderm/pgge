package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import static com.mygdx.game.util.GfxUtil.makeModelMesh;


public class ModelInstanceEx extends ModelInstance {

    public ModelInstanceEx() {
        super(new Model());
    }


    public static void setColorAttribute(ModelInstance inst, Color color) {

        setColorAttribute(inst, color, color.a);
    }

    public static void setColorAttribute(ModelInstance inst, Color color, float alpha) {

        /*        ColorAttribute ca = (ColorAttribute) mat.get(ColorAttribute.Diffuse);         */

        Material mat = inst.materials.get(0);

//        if (null == mat)
//            return; // throw new GdxRuntimeException("not found");

        mat.set(ColorAttribute.createDiffuse(color));

        BlendingAttribute blendingAttribute =
                new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, alpha);

        mat.set(blendingAttribute);
    }


//    private static int nextColor = 0;
//
//    public static void setMaterialColor(ModelInstance modelInst, Color c) {
//
//        Array<Color> colors = new Array<Color>();
//        colors.add(Color.WHITE);
//        colors.add(Color.RED);
//        colors.add(Color.ORANGE);
//        colors.add(Color.YELLOW);
//        colors.add(Color.GREEN);
//        colors.add(Color.BLUE);
//        colors.add(Color.VIOLET);
//
//        // hmmm, w/ alt. pick test, now getting null somtimes?
////        if (null == modelInst) {
////            return; //  throw new GdxRuntimeException("e == null ");
////        }
//
//        nextColor += 1;
//        if (nextColor >= colors.size) {
//            nextColor = 0;
//        }
//
///*        ColorAttribute ca = (ColorAttribute) mat.get(ColorAttribute.Diffuse);
//
//        for (Color color : colors) {
//            if (ca.color != color) {
//                mat.set(ColorAttribute.createDiffuse(color));
//                break;
//            }
//        }*/
//        setColorAttribute(modelInst, colors.get(nextColor), 0.5f);
//    }
//
//
///*    private static void setObjectMatlTex(ModelInstance inst, Texture tex){
//
//        Material mat = inst.materials.get(0);
//        if (null == mat)
//            return; // throw new GdxRuntimeException("not found");
//
//mat.remove(ColorAttribute.Diffuse);
//mat.remove(BlendingAttribute.Type);
//        mat.set(TextureAttribute.createDiffuse(tex));
//    }*/



    /*
     * IN:
     *
     * RETURN:
     *   ModelInstance ... which would be passed in to ModelComponent()
     */
    public static ModelInstance getModelInstance(Model model, String node) {

        Matrix4 transform = new Matrix4();
        ModelInstance instance = new ModelInstance(model, transform, node);
        Node modelNode = instance.getNode(node);

        if (null == modelNode){
            Gdx.app.log("ModelInstanceEx", "Failed, inst.nodes.size = " + instance.nodes.size);
            instance = null;

        } else {
// https://xoppa.github.io/blog/loading-a-scene-with-libgdx/
            instance.transform.set(modelNode.globalTransform);
            modelNode.translation.set(0, 0, 0);
            modelNode.scale.set(1, 1, 1);
            modelNode.rotation.idt();
            instance.calculateTransforms();
        }
        return instance;
    }

    public static ModelInstance getModelInstance(Model model, String node, Vector3 scale) {

        ModelInstance instance = getModelInstance(model, node);

        if (null == instance){ // TODO: cleaner way to deal with objects like this
            Model mdl = makeModelMesh(2, "line");
            instance = new ModelInstance(mdl);
        }

        // https://stackoverflow.com/questions/21827302/scaling-a-modelinstance-in-libgdx-3d-and-bullet-engine
        if (null != scale) {
            instance.nodes.get(0).scale.set(scale);
            instance.calculateTransforms();
        }
        return instance;
    }
}
