package com.drppp.drtech.Client.lib.obj;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class MeshModel {
    public List<Vector3f> positions;
    public List<Vector3f> normals;
    public List<Vector2f> texCoords;
    public List<MeshPart> parts = new ArrayList();

    public MeshModel() {
    }

    public MeshModel clone() {
        MeshModel mm = new MeshModel();
        mm.parts = new ArrayList();
        Iterator var2 = this.parts.iterator();

        while(var2.hasNext()) {
            MeshPart mp = (MeshPart)var2.next();
            mm.parts.add(mp);
        }

        Vector3f mp;
        if (this.positions != null) {
            mm.positions = new ArrayList();
            var2 = this.positions.iterator();

            while(var2.hasNext()) {
                mp = (Vector3f)var2.next();
                mm.positions.add((Vector3f)mp.clone());
            }
        }

        if (this.normals != null) {
            mm.normals = new ArrayList();
            var2 = this.normals.iterator();

            while(var2.hasNext()) {
                mp = (Vector3f)var2.next();
                mm.normals.add((Vector3f)mp.clone());
            }
        }

        if (this.texCoords != null) {
            mm.texCoords = new ArrayList();
            var2 = this.texCoords.iterator();

            while(var2.hasNext()) {
                Vector2f mp1 = (Vector2f)var2.next();
                mm.texCoords.add((Vector2f)mp1.clone());
            }
        }

        return mm;
    }

    public void rotate(double d, Vector3 axis, Vector3 offset) {
        Rotation r = new Rotation(d, axis);
        List<Vector3f> p = new ArrayList();
        Iterator var7 = this.positions.iterator();

        while(var7.hasNext()) {
            Vector3f v = (Vector3f)var7.next();
            Vector3 vec = new Vector3((double)v.x, (double)v.y, (double)v.z);
            r.apply(vec);
            vec = vec.add(offset);
            p.add(new Vector3f((float)vec.x, (float)vec.y, (float)vec.z));
        }

        this.positions = p;
    }

    public void addPosition(float x, float y, float z) {
        if (this.positions == null) {
            this.positions = new ArrayList();
        }

        this.positions.add(new Vector3f(x, y, z));
    }

    public void addNormal(float x, float y, float z) {
        if (this.normals == null) {
            this.normals = new ArrayList();
        }

        this.normals.add(new Vector3f(x, y, z));
    }

    public void addTexCoords(float x, float y) {
        if (this.texCoords == null) {
            this.texCoords = new ArrayList();
        }

        this.texCoords.add(new Vector2f(x, y));
    }

    public void addPart(MeshPart part) {
        this.parts.add(part);
    }

    public void addPart(MeshPart part, int ti) {
        this.parts.add(new MeshPart(part, ti));
    }

    private int getColorValue(Vector3f color) {
        int r = (int)color.x;
        int g = (int)color.y;
        int b = (int)color.z;
        return -16777216 | r << 16 | g << 8 | b;
    }

    public List<BakedQuad> bakeModel(ModelManager manager) {
        List<BakedQuad> bakeList = new ArrayList();

        for(int j = 0; j < this.parts.size(); ++j) {
            MeshPart part = (MeshPart)this.parts.get(j);
            TextureAtlasSprite sprite = null;
            int color = -1;
            if (part.material != null) {
                if (part.material.DiffuseTextureMap != null) {
                    sprite = manager.getTextureMap().getAtlasSprite(part.material.DiffuseTextureMap);
                } else if (part.material.AmbientTextureMap != null) {
                    sprite = manager.getTextureMap().getAtlasSprite(part.material.AmbientTextureMap);
                }

                if (part.material.DiffuseColor != null) {
                    color = this.getColorValue(part.material.DiffuseColor);
                }
            }

            for(int i = 0; i < part.indices.size(); i += 4) {
                BakedQuad quad = this.bakeQuad(part, i, sprite, color);
                bakeList.add(quad);
            }
        }

        return bakeList;
    }

    public List<BakedQuad> bakeModel(TextureAtlasSprite sprite) {
        List<BakedQuad> bakeList = new ArrayList();

        for(int j = 0; j < this.parts.size(); ++j) {
            MeshPart part = (MeshPart)this.parts.get(j);
            int color = -1;

            for(int i = 0; i < part.indices.size(); i += 4) {
                BakedQuad quad = this.bakeQuad(part, i, sprite, color);
                bakeList.add(quad);
            }
        }

        return bakeList;
    }

    private BakedQuad bakeQuad(MeshPart part, int startIndex, TextureAtlasSprite sprite, int color) {
        int[] faceData = new int[28];

        for(int i = 0; i < 4; ++i) {
            Vector3f position = new Vector3f(0.0F, 0.0F, 0.0F);
            Vector2f texCoord = new Vector2f(0.0F, 0.0F);
            int p = 0;
            int[] indices = (int[])part.indices.get(startIndex + i);
            if (this.positions != null) {
                position = (Vector3f)this.positions.get(indices[p++]);
            }

            if (this.normals != null) {
                ++p;
            }

            if (this.texCoords != null) {
                texCoord = (Vector2f)this.texCoords.get(indices[p++]);
            }

            storeVertexData(faceData, i, position, texCoord, sprite, color);
        }

        return new BakedQuad(faceData, part.name.contains("focus") ? 1 : part.tintIndex, FaceBakery.getFacingFromVertexData(faceData), sprite, false, DefaultVertexFormats.BLOCK);
    }

    private static void storeVertexData(int[] faceData, int storeIndex, Vector3f position, Vector2f faceUV, TextureAtlasSprite sprite, int shadeColor) {
        int l = storeIndex * 7;
        faceData[l + 0] = Float.floatToRawIntBits(position.x);
        faceData[l + 1] = Float.floatToRawIntBits(position.y);
        faceData[l + 2] = Float.floatToRawIntBits(position.z);
        faceData[l + 3] = shadeColor;
        if (sprite != null) {
            faceData[l + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU((double)(faceUV.x * 16.0F)));
            faceData[l + 5] = Float.floatToRawIntBits(sprite.getInterpolatedV((double)(faceUV.y * 16.0F)));
        } else {
            faceData[l + 4] = Float.floatToRawIntBits(faceUV.x);
            faceData[l + 5] = Float.floatToRawIntBits(faceUV.y);
        }

        faceData[l + 6] = 0;
    }
}
