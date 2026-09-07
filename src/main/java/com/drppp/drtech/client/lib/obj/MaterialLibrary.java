package com.drppp.drtech.Client.lib.obj;


import com.google.common.base.Charsets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import javax.vecmath.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

class MaterialLibrary extends Dictionary<String, Material> {
    static final Set<String> unknownCommands = new HashSet();
    private final Dictionary<String, Material> materialLibrary = new Hashtable();
    private Material currentMaterial;

    public MaterialLibrary() {
    }

    public int size() {
        return this.materialLibrary.size();
    }

    public boolean isEmpty() {
        return this.materialLibrary.isEmpty();
    }

    public Enumeration<String> keys() {
        return this.materialLibrary.keys();
    }

    public Enumeration<Material> elements() {
        return this.materialLibrary.elements();
    }

    public Material get(Object key) {
        return (Material)this.materialLibrary.get(key);
    }

    public Material put(String key, Material value) {
        return (Material)this.materialLibrary.put(key, value);
    }

    public Material remove(Object key) {
        return (Material)this.materialLibrary.remove(key);
    }

    public void loadFromStream(ResourceLocation loc) throws IOException {
        IResource res = Minecraft.getMinecraft().getResourceManager().getResource(loc);
        InputStreamReader lineStream = new InputStreamReader(res.getInputStream(), Charsets.UTF_8);
        BufferedReader lineReader = new BufferedReader(lineStream);

        while(true) {
            String currentLine = lineReader.readLine();
            if (currentLine == null) {
                return;
            }

            if (currentLine.length() != 0 && !currentLine.startsWith("#")) {
                String[] fields = currentLine.split(" ", 2);
                String keyword = fields[0];
                String data = fields[1];
                if (keyword.equalsIgnoreCase("newmtl")) {
                    this.pushMaterial(data);
                } else if (keyword.equalsIgnoreCase("Ka")) {
                    this.currentMaterial.AmbientColor = this.parseVector3f(data);
                } else if (keyword.equalsIgnoreCase("Kd")) {
                    this.currentMaterial.DiffuseColor = this.parseVector3f(data);
                } else if (keyword.equalsIgnoreCase("Ks")) {
                    this.currentMaterial.SpecularColor = this.parseVector3f(data);
                } else if (keyword.equalsIgnoreCase("Ns")) {
                    this.currentMaterial.SpecularCoefficient = this.parseFloat(data);
                } else if (keyword.equalsIgnoreCase("Tr")) {
                    this.currentMaterial.Transparency = this.parseFloat(data);
                } else if (keyword.equalsIgnoreCase("illum")) {
                    this.currentMaterial.IlluminationModel = this.parseInt(data);
                } else if (keyword.equalsIgnoreCase("map_Ka")) {
                    this.currentMaterial.AmbientTextureMap = data;
                    new ResourceLocation(data);
                } else if (keyword.equalsIgnoreCase("map_Kd")) {
                    this.currentMaterial.DiffuseTextureMap = data;
                    new ResourceLocation(data);
                } else if (keyword.equalsIgnoreCase("map_Ks")) {
                    this.currentMaterial.SpecularTextureMap = data;
                } else if (keyword.equalsIgnoreCase("map_Ns")) {
                    this.currentMaterial.SpecularHighlightTextureMap = data;
                } else if (keyword.equalsIgnoreCase("map_d")) {
                    this.currentMaterial.AlphaTextureMap = data;
                } else if (keyword.equalsIgnoreCase("map_bump")) {
                    this.currentMaterial.BumpMap = data;
                } else if (keyword.equalsIgnoreCase("bump")) {
                    this.currentMaterial.BumpMap = data;
                } else if (keyword.equalsIgnoreCase("disp")) {
                    this.currentMaterial.DisplacementMap = data;
                } else if (keyword.equalsIgnoreCase("decal")) {
                    this.currentMaterial.StencilDecalMap = data;
                } else if (!unknownCommands.contains(keyword)) {
                    unknownCommands.add(keyword);
                }
            }
        }
    }

    private float parseFloat(String data) {
        return Float.parseFloat(data);
    }

    private int parseInt(String data) {
        return Integer.parseInt(data);
    }

    private Vector3f parseVector3f(String data) {
        String[] parts = data.split(" ");
        return new Vector3f(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));
    }

    private void pushMaterial(String materialName) {
        this.currentMaterial = new Material(materialName);
        this.materialLibrary.put(this.currentMaterial.Name, this.currentMaterial);
    }
    // 新添加的方法
    public String getTexture(String materialName, String textureType) {
        Material material = this.get(materialName);
        if (material != null) {
            switch (textureType.toLowerCase()) {
                case "ambient":
                    return material.AmbientTextureMap;
                case "diffuse":
                    return material.DiffuseTextureMap;
                case "specular":
                    return material.SpecularTextureMap;
                case "specularhighlight":
                    return material.SpecularHighlightTextureMap;
                case "alpha":
                    return material.AlphaTextureMap;
                case "bump":
                    return material.BumpMap;
                case "displacement":
                    return material.DisplacementMap;
                case "decal":
                    return material.StencilDecalMap;
                default:
                    return null;
            }
        }
        return null;
    }
}
