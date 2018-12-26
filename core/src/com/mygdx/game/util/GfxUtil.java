package com.mygdx.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

// https://stackoverflow.com/questions/28057588/setting-the-indices-for-a-3d-mesh-in-libgdx

public class GfxUtil  /*extends ModelInstance*/ {

    private Vector3 to = new Vector3();
    private Model lineModel;
    private ModelInstance instance;

    public GfxUtil() {

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        Mesh mesh = new Mesh(true, 2, 2,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, "a_color"));
        mesh.setVertices(new float[(4 + 3) * 2]);
        mesh.setIndices(new short[]{0, 1});
        modelBuilder.part("line", mesh, GL20.GL_LINES, new Material());
        lineModel = modelBuilder.end();

        instance = new ModelInstance(lineModel);
        Node modelNode = instance.getNode("node1");
        modelNode.id = "asdf";
        /*
        modelBuilder.begin();

        MeshPartBuilder lineBuilder = modelBuilder.part("line", GL20.GL_LINES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, new Material());
        lineBuilder.setColor(null);
        lineBuilder.line(new Vector3(), new Vector3()); // 2 vertices
        lineModel = modelBuilder.end();
        instance = new ModelInstance(lineModel);\
        */
    }


    // TODO:
//    @Override
    public void dispose() {
        lineModel.dispose();
    }

    /*
    https://stackoverflow.com/questions/38928229/how-to-draw-a-line-between-two-points-in-libgdx-in-3d
     */
    public ModelInstance line(Vector3 from, Vector3 b, Color c) {

        to.set(from.x + b.x, from.y + b.y, from.z + b.z);
        return lineTo(from, to, c);
    }

    /*
    this is probablly a screwy way to do this
     */
    public ModelInstance lineTo(Vector3 from, Vector3 to, Color c) {

        Node modelNode = instance.getNode("asdf");
        MeshPart meshPart = modelNode.parts.get(0).meshPart;

        float[] nVerts = MeshHelper.getVertices(meshPart);
        nVerts[0] = from.x;
        nVerts[1] = from.y;
        nVerts[2] = from.z;
        nVerts[3] = c.r;
        nVerts[4] = c.g;
        nVerts[5] = c.b;
        nVerts[6] = c.a;
        nVerts[7] = to.x;
        nVerts[8] = to.y;
        nVerts[9] = to.z;
        nVerts[10] = c.r;
        nVerts[11] = c.g;
        nVerts[12] = c.b;
        nVerts[13] = c.a;
        meshPart.mesh.setVertices(nVerts);

        return instance;
    }
}
