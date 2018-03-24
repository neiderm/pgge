package com.mygdx.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.collision.btShapeHull;
import com.badlogic.gdx.utils.BufferUtils;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Created by mango on 3/24/18.
 */

public class EntityBuilder {

    private EntityBuilder(){}

    public static Entity loadStaticEntity(Model model, String node) {

        Entity e = new Entity();

        if (null != node) {
            ModelInstance instance;
            instance = getModelInstance(model, node);
            e.add(new ModelComponent(instance, null));
        } else {
            e.add(new ModelComponent(model, new Matrix4()));
        }

        return e;
    }

    public static Entity loadKinematicEntity(
            Model model, String nodeID, btCollisionShape shape, Vector3 trans, Vector3 size) {

        Entity entity = loadDynamicEntity(model, shape, nodeID, 0, trans, size);

        // special sauce here for static entity
        // called loadDynamicEntity w/ mass==0, so it's BC will NOT have a motionState (which is what we
        // want for this object) so we do need to update the bc.body with the location vector we got from the model
        Vector3 tmp = new Vector3();
        entity.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmp);
        BulletComponent bc = entity.getComponent(BulletComponent.class);

        if (null == trans)
        {
            bc.body.translate(tmp); // if translation param not given, need to sync body /w mesh instance
        }

        bc.body.setCollisionFlags(
                bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
        bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);

            /* need ground & landscape objects to be kinematic: once the player ball stopped and was
            deactivated by the bullet dynamics, it would no longer move around under the applied force.
            ONe small complication is the renderer has to know which are the active dynamic objects
            that it has to "refresh" the scaling in the transform (because goofy bullet messes with
            the scaling!). So here we set a flag to tell renderer that it doesn't have to re-scale
            the kinematic object (need to do a "kinematic" component to deal w/ this).
             */
        bc.sFlag = true;

        return entity;
    }


    public static Entity loadDynamicEntity(
            Model model, btCollisionShape shape, String nodeID, float mass, Vector3 translation, Vector3 size) {

        Entity e = loadStaticEntity(model, nodeID);
        ModelInstance instance = e.getComponent(ModelComponent.class).modelInst;

//        if (null != size){
//            instance.transform.scl(size); // if mesh must be scaled, do it before creating the hull shape
//        }

        if (null == shape) {
            if (null != nodeID) {
                shape = createConvexHullShape(instance.getNode(nodeID).parts.get(0).meshPart);
            }else{
                final Mesh mesh = model.meshes.get(0);
                shape = new btConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize());
//                shape = createConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize(), true);
            }
        }

        e.add(new BulletComponent(shape, instance.transform, mass));

        // set to translation here if you don't want what the primitivesModel gives you
        if (null != translation) {
            instance.transform.trn(translation);
            e.getComponent(BulletComponent.class).body.setWorldTransform(instance.transform);
        }

        if (null != size){
            instance.transform.scl(size); // if mesh must be scaled, do it before^H^H^H^H^H^H  ?????
        }

        return e;
    }


    /*
         * IN:
     *   Matrix4 transform: transform must be linked to Bullet Rigid Body
     * RETURN:
     *   ModelInstance ... which would be passed in to ModelComponent()
     */
    private static ModelInstance getModelInstance(Model model, String node) {

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
    private static btConvexHullShape createConvexHullShape(MeshPart meshPart) {

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
