package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.collision.btShapeHull;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Created by mango on 3/24/18.
 */

public class EntityBuilder {

    private EntityBuilder() {
    }

    /*
     * IN:
     *   Matrix4 transform: transform must be linked to Bullet Rigid Body
     * RETURN:
     *   ModelInstance ... which would be passed in to ModelComponent()
     */
    public static ModelInstance getModelInstance(Model model, String node) {

        Matrix4 transform = new Matrix4();
        ModelInstance instance = new ModelInstance(model, transform, node);
        Node modelNode = instance.getNode(node);

// https://xoppa.github.io/blog/loading-a-scene-with-libgdx/
        instance.transform.set(modelNode.globalTransform);
        modelNode.translation.set(0, 0, 0);
        modelNode.scale.set(1, 1, 1);
        modelNode.rotation.idt();
        instance.calculateTransforms();

        return instance;
    }


    /*
      http://badlogicgames.com/forum/viewtopic.php?t=24875&p=99976
     */
    public static btConvexHullShape createConvexHullShape(MeshPart meshPart) {

//        int numVertices  = meshPart.mesh.getNumVertices();    // no only works where our subject is the only node in the mesh!
        int numVertices = meshPart.size;
        int vertexSize = meshPart.mesh.getVertexSize();

        float[] nVerts = getVertices(meshPart);
        int size = numVertices * vertexSize; // nbr of floats

        FloatBuffer buffer = ByteBuffer.allocateDirect(size * 4).asFloatBuffer();
        BufferUtils.copy(nVerts, 0, buffer, size);

        btConvexHullShape shape = createConvexHullShape(buffer, numVertices, vertexSize, true);

        return shape;
    }

    /*
     * going off script ... found no other way to properly get the vertices from an "indexed" object
     */
    private static float[] getVertices(MeshPart meshPart) {

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
}
