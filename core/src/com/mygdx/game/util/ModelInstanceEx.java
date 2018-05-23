package com.mygdx.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class ModelInstanceEx extends ModelInstance {

    public ModelInstanceEx() {
        super(new Model());
    }

    private static Vector3 axis = new Vector3();
    /*
    doesn't belong here but it's handy to keep the temp vector axis somewhere
     */
    public static Vector3 rotateRad(Vector3 v, Quaternion rotation){

        return v.rotateRad(axis, rotation.getAxisAngleRad(axis));
    }


    public void setColorAttribute(Color c, float alpha) {

        setColorAttribute(this, c, alpha);
    }

    public static void setColorAttribute(ModelInstance inst, Color c, float alpha) {

        /*        ColorAttribute ca = (ColorAttribute) mat.get(ColorAttribute.Diffuse);         */

        Material mat = inst.materials.get(0);

        if (null == mat)
            return; // throw new GdxRuntimeException("not found");

        mat.set(ColorAttribute.createDiffuse(c));

        BlendingAttribute blendingAttribute =
                new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, alpha);
        mat.set(blendingAttribute);
    }


    private static int nextColor = 0;

    public static void setMaterialColor(ModelInstance modelInst, Color c) {

        Array<Color> colors = new Array<Color>();
        colors.add(Color.WHITE);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.YELLOW);
        colors.add(Color.PURPLE);

        // hmmm, w/ alt. pick test, now getting null somtimes?
//        if (null == modelInst) {
//            return; //  throw new GdxRuntimeException("e == null ");
//        }

        nextColor += 1;
        if (nextColor >= colors.size) {
            nextColor = 0;
        }

/*        ColorAttribute ca = (ColorAttribute) mat.get(ColorAttribute.Diffuse);

        for (Color color : colors) {
            if (ca.color != color) {
                mat.set(ColorAttribute.createDiffuse(color));
                break;
            }
        }*/
        setColorAttribute(modelInst, colors.get(nextColor), 0.5f);
    }


/*    private static void setObjectMatlTex(ModelInstance inst, Texture tex){

        Material mat = inst.materials.get(0);
        if (null == mat)
            return; // throw new GdxRuntimeException("not found");

mat.remove(ColorAttribute.Diffuse);
mat.remove(BlendingAttribute.Type);
        mat.set(TextureAttribute.createDiffuse(tex));
    }*/
}
