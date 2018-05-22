package com.mygdx.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class GfxUtil {

    private static Vector3 to = new Vector3();
    private static ModelBuilder modelBuilder = new ModelBuilder();

    /*
    https://stackoverflow.com/questions/38928229/how-to-draw-a-line-between-two-points-in-libgdx-in-3d
     */
    public static ModelInstance line(Vector3 from, Vector3 b, Color c) {

//        tmpRay.set(from, b);
//        tmpRay.getEndPoint(to, 0.2f);
        to.set(from.x + b.x, from.y + b.y, from.z + b.z);
        return lineTo(from, to, c);
    }

    public static ModelInstance lineTo(Vector3 from, Vector3 to, Color c) {

        modelBuilder.begin();
        MeshPartBuilder lineBuilder = modelBuilder.part("line", 1, 3, new Material());
        lineBuilder.setColor(c);
        lineBuilder.line(from, to);
        Model lineModel = modelBuilder.end();
        return  new ModelInstance(lineModel);
    }
}
