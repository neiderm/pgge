package com.mygdx.game.util;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.collision.btShapeHull;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Created by mango on 3/24/18.
 */

public class MeshHelper /* extends btConvexHullShape */ {

    private MeshHelper() {
    }

    /*
      http://badlogicgames.com/forum/viewtopic.php?t=24875&p=99976
     */
    public static btConvexHullShape createConvexHullShape(Node node) {

        MeshPart meshPart = node.parts.get(0).meshPart;

//        int numVertices  = meshPart.mesh.getNumVertices();    // no only works where our subject is the only node in the mesh!
        int numVertices = meshPart.size;
        int vertexSize = meshPart.mesh.getVertexSize();

        float[] nVerts = getVertices(meshPart);
        int size = numVertices * vertexSize; // nbr of floats

        FloatBuffer buffer = ByteBuffer.allocateDirect(size * 4).asFloatBuffer();
        BufferUtils.copy(nVerts, 0, buffer, size);

        return createConvexHullShape(buffer, numVertices, vertexSize, true);
    }

    /*
     * going off script ... found no other way to properly get the vertices from an "indexed" object
     */
    static float[] getVertices(MeshPart meshPart) {

        int numMeshVertices = meshPart.mesh.getNumVertices();
        int numPartIndices = meshPart.size;
        short[] meshPartIndices = new short[numPartIndices];
        meshPart.mesh.getIndices(meshPart.offset, numPartIndices, meshPartIndices, 0);

        final int stride = meshPart.mesh.getVertexSize() / 4;
        float[] allVerts = new float[numMeshVertices * stride];
        meshPart.mesh.getVertices(0, allVerts.length, allVerts);

        float[] iVerts = new float[numPartIndices * stride];

        for (short n = 0; n < numPartIndices; n++) {
            System.arraycopy(allVerts, meshPartIndices[n] * stride, iVerts, n * stride, stride);
        }
        return iVerts;
    }

    /*
      https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/bullet/ConvexHullTest.java
     */
    private static btConvexHullShape createConvexHullShape(
            FloatBuffer points, int numPoints, int stride, boolean optimize) {

        final btConvexHullShape shape = new btConvexHullShape(points, numPoints, stride);

        if (!optimize) return shape;
        // now optimize the shape
        final btShapeHull hull = new btShapeHull(shape);
        hull.buildHull(shape.getMargin());
        final btConvexHullShape result = new btConvexHullShape(hull);
        // delete the temporary shape
        shape.dispose();
        hull.dispose();
        return result;
    }

    public static btConvexHullShape createConvexHullShape(Model model, boolean optimize) {

        final Mesh mesh = model.meshes.get(0);

        return MeshHelper.createConvexHullShape(
                mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize(), optimize);
    }
}
