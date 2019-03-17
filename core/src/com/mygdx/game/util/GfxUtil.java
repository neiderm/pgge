package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.IntBuffer;

// https://stackoverflow.com/questions/28057588/setting-the-indices-for-a-3d-mesh-in-libgdx

public class GfxUtil  /*extends ModelInstance*/ {

    private Vector3 to = new Vector3();
    private Model lineModel;
    private ModelInstance instance;

    public GfxUtil() {

        lineModel = makeModelMesh(2, "line");
        instance = new ModelInstance(lineModel);
        Node modelNode = instance.getNode("node1");
        modelNode.id = "asdf";

        int mts = getMaxTextureSize();
        Gdx.app.log("GfxUtil", "GL_MAX_TEXTURE_SIZE = " + mts);
    }

    private static Model makeModelMesh(int nVertices, String meshPartID) {

        Model model;
        int maxVertices = nVertices;
        int maxIndices = nVertices;

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        Mesh mesh = new Mesh(true, maxVertices, maxIndices,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, "a_color"));
        mesh.setVertices(new float[(4 + 3) * nVertices]);

        short[] indices = new short[nVertices];
        for (short n = 0; n < nVertices; n++){
            indices[n] = n; // idfk
        }
        mesh.setIndices(indices);

        modelBuilder.part(meshPartID, mesh, GL20.GL_LINES, new Material());
        model = modelBuilder.end();

        return model;
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
    MeshPartBuilder.line()!!!!!!!!!
     */
    public ModelInstance lineTo(
            //Vector3[] lineVertsBuffer, int nVertices,
                                Vector3 from, Vector3 to, Color c) {

        Node modelNode = instance.getNode("asdf");
        MeshPart meshPart = modelNode.parts.get(0).meshPart;

        float[] nVerts = MeshHelper.getVertices(meshPart);
        setVertex(nVerts, 0, 7, to, c );
        setVertex(nVerts, 1, 7, from, c );

        meshPart.mesh.setVertices(nVerts);

        return instance;
    }


    public static void getVertex(float[] array, int index, int stride, Vector3 point, Color c){

        int offset = index * stride;
        point.x = array[offset + 0];
        point.y = array[offset + 1];
        point.z = array[offset + 2];
        c.set(array[offset + 3], array[offset + 4], array[offset + 5], array[offset + 6]);
    }

    private static void setVertex(float[] array, int index, int stride, Vector3 point, Color c){

        int offset = index * stride;
        array[offset + 0] = point.x;
        array[offset + 1] = point.y;
        array[offset + 2] = point.z;
        array[offset + 3] = c.r;
        array[offset + 4] = c.g;
        array[offset + 5] = c.b;
        array[offset + 6] = c.a;
    }

    /*
    https://stackoverflow.com/questions/35627720/libgdx-what-texture-size
     */
    private static int getMaxTextureSize() {
        IntBuffer buffer = BufferUtils.newIntBuffer(16);
        Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buffer);
        return buffer.get(0);
    }
}
