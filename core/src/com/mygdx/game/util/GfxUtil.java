/*
 * Copyright (c) 2019 Glenn Neidermeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.IntBuffer;


/**
 *  Creating Models on the fly seems to be something that happens.
 *  Possibly because I am dumb about this ... e.g.  creating new Mesh and Model for all my debug graphic lines.
 *    .... can get on with just one model ... if each new Mesh added as a Node and unify w/ the "Primtivess" model.
 *  For now, provide some static structures to track and manage lifecycle (implement dispose() on them!).
 *
 *  FOr now stubbornly refuses to be extendsion of Model
 */
public class GfxUtil  /* extends Model ??? */  /*extends ModelInstance*/ {


    private static Array<Model> savedModelRefs;


    private static final String LINE_MESH_PART_ID = "linemeshpartid";

    // default id name is "node" + .size .. nothing fancy  see "ModelBuilder::node()"
    private static final String DEFAULT_MODEL_NODE_ID = "node1";

    private final Vector3 to = new Vector3();

    private final Model lineModel;
    private final ModelInstance instance;

    public GfxUtil() {

        if (null == savedModelRefs){
            savedModelRefs = new Array<Model>();
        }

        lineModel = makeModelMesh(2, LINE_MESH_PART_ID); // simple mesh part ID,
        instance = new ModelInstance(lineModel);

        savedModelRefs.add(lineModel);


//        Node modelNode = instance.getNode(DEFAULT_MODEL_NODE_ID);
        // use default, don't need to set it    // modelNode.id = LINE_MODEL_NODE_ID;

// interesting
        int mts = getMaxTextureSize();
        Gdx.app.log("GfxUtil", "GL_MAX_TEXTURE_SIZE = " + mts);
    }

    public static void clearRefs(){
        int n = 0;
        for (Model mmm : savedModelRefs){
            n += 1;
/*
            mmm.dispose();
 */
        }
        // savedModelRefs = null ??? ?
        Gdx.app.log("GfxUtil:clearRefs()", "Models removed = " + n);
    }


    /**
     *
     * Ref:
     *   https://stackoverflow.com/questions/28057588/setting-the-indices-for-a-3d-mesh-in-libgdx
     *
     *   Needs to be static because the Model needs to be tracked for dispose()
     */
    private static Model makeModelMesh(int nVertices, String meshPartID) {

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
            indices[n] = n;
        }
        mesh.setIndices(indices);

        modelBuilder.part(meshPartID, mesh, GL20.GL_LINES, new Material());
        Model model = modelBuilder.end();

        return model;
    }


    // TODO:
//    @Override
    public void dispose() {

        lineModel.dispose();
        // ihni ... identity If true, == comparison will be used. If false, .equals() comparison will be used
        savedModelRefs.removeValue(this.lineModel, true);
    }

    /*
    https://stackoverflow.com/questions/38928229/how-to-draw-a-line-between-two-points-in-libgdx-in-3d

Below mostly is dogpile of what started as debuggin helps for drawing lines.
Using lineTo and line to manipulate the "this.instance" modelInstance of this Model ... yep wtf alright
Sets the vertices directly in the float buffers and then the instance returned to caller to pump thru the render batch.


    Builder.createXYZCoordinates() ??
     */
    public ModelInstance line(Vector3 from, Vector3 b, Color c) {

        // to := from + b and preserve "from" // to.set(from.add(b));
        to.set(from.x + b.x, from.y + b.y, from.z + b.z);

        return lineTo(from, to, c);
    }

    /*
    MeshPartBuilder.line()!!!!!!!!!
     */
    public ModelInstance lineTo(
            //Vector3[] lineVertsBuffer, int nVertices,
                                Vector3 from, Vector3 to, Color c) {

//         Node modelNode = instance.getNode("asdf");
        Node modelNode = Node.getNode(
                instance.nodes, DEFAULT_MODEL_NODE_ID, true, true);

// if (null != modelNode) {
        MeshPart meshPart = modelNode.parts.get(0).meshPart;

        float[] nVerts = MeshHelper.getVertices(meshPart);
        setVertex(nVerts, 0, 7, to, c );
        setVertex(nVerts, 1, 7, from, c );

        meshPart.mesh.setVertices(nVerts);
// }
        return instance;
    }


    private static void getVertex(float[] array, int index, int stride, Vector3 point, Color c){

        int offset = index * stride;
        point.x = array[offset];
        point.y = array[offset + 1];
        point.z = array[offset + 2];
        c.set(array[offset + 3], array[offset + 4], array[offset + 5], array[offset + 6]);
    }

    private static void setVertex(float[] array, int index, int stride, Vector3 point, Color c){

        int offset = index * stride;
        array[offset    ] = point.x;
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
