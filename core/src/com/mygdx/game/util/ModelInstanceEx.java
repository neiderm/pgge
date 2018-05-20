package com.mygdx.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

public class ModelInstanceEx extends ModelInstance {

    public ModelInstanceEx() {
        super(new Model());
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



/*    private static void setObjectMatlTex(ModelInstance inst, Texture tex){

        Material mat = inst.materials.get(0);
        if (null == mat)
            return; // throw new GdxRuntimeException("not found");

mat.remove(ColorAttribute.Diffuse);
mat.remove(BlendingAttribute.Type);
        mat.set(TextureAttribute.createDiffuse(tex));
    }*/
}
