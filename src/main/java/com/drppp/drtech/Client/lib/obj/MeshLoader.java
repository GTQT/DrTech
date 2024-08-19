package com.drppp.drtech.Client.lib.obj;


import com.google.common.base.Charsets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import scala.NotImplementedError;

public class MeshLoader {
    static final Set<String> unknownCommands = new HashSet();
    private MeshModel currentModel;
    private MeshPart currentPart;
    private MaterialLibrary currentMatLib;
    int firstIndex;
    int lastIndex;
    private String filePath;
    private String lastObjectName;

    public MeshLoader() {
    }

    private void addTexCoord(String line) {
        String[] args = line.split(" ");
        float x = Float.parseFloat(args[0]);
        float y = Float.parseFloat(args[1]);
        this.currentModel.addTexCoords(x, y);
    }

    private void addNormal(String line) {
        String[] args = line.split(" ");
        float x = Float.parseFloat(args[0]);
        float y = Float.parseFloat(args[1]);
        float z = args[2].equals("\\\\") ? (float)Math.sqrt((double)(1.0F - x * x - y * y)) : Float.parseFloat(args[2]);
        this.currentModel.addNormal(x, y, z);
    }

    private void addPosition(String line) {
        String[] args = line.split(" ");
        float x = Float.parseFloat(args[0]);
        float y = Float.parseFloat(args[1]);
        float z = Float.parseFloat(args[2]);
        this.currentModel.addPosition(x, y, z);
    }

    private void addFace(String line) {
        String[] args = line.split(" ");
        if (args.length >= 3 && args.length <= 4) {
            String[] p1 = args[0].split("/");
            String[] p2 = args[1].split("/");
            String[] p3 = args[2].split("/");
            int[] v1 = this.parseIndices(p1);
            int[] v2 = this.parseIndices(p2);
            int[] v3 = this.parseIndices(p3);
            if (args.length == 3) {
                this.currentPart.addTriangleFace(v1, v2, v3);
            } else if (args.length == 4) {
                String[] p4 = args[3].split("/");
                int[] v4 = this.parseIndices(p4);
                this.currentPart.addQuadFace(v1, v2, v3, v4);
            }

        } else {
            throw new NotImplementedError();
        }
    }

    private int[] parseIndices(String[] p1) {
        int[] indices = new int[p1.length];

        for(int i = 0; i < p1.length; ++i) {
            indices[i] = Integer.parseInt(p1[i]) - 1;
        }

        return indices;
    }

    private void useMaterial(String matName) {
        Material mat = this.currentMatLib.get(matName);
        this.currentPart = new MeshPart();
        this.currentPart.name = this.lastObjectName;
        this.currentPart.material = mat;
        this.currentModel.addPart(this.currentPart);
    }

    private void newObject(String line) {
        this.lastObjectName = line;
    }

    private void newGroup(String line) {
        this.lastObjectName = line;
    }

    private void loadMaterialLibrary(ResourceLocation locOfParent, String path) throws IOException {
        String prefix = locOfParent.getPath();
        int pp = prefix.lastIndexOf(47);
        prefix = pp >= 0 ? prefix.substring(0, pp + 1) : "";
        ResourceLocation loc = new ResourceLocation(locOfParent.getNamespace(), prefix + path);
        this.currentMatLib.loadFromStream(loc);
    }

    public MeshModel loadFromResource(ResourceLocation loc) throws IOException {
        IResource res = Minecraft.getMinecraft().getResourceManager().getResource(loc);
        InputStreamReader lineStream = new InputStreamReader(res.getInputStream(), Charsets.UTF_8);
        BufferedReader lineReader = new BufferedReader(lineStream);
        this.currentModel = new MeshModel();
        this.currentMatLib = new MaterialLibrary();

        while(true) {
            String currentLine = lineReader.readLine();
            if (currentLine == null) {
                return this.currentModel;
            }

            if (currentLine.length() != 0 && !currentLine.startsWith("#")) {
                if (currentLine.startsWith("v  ")) {
                    currentLine = currentLine.replaceFirst("v  ", "v ");
                }

                String[] fields = currentLine.split(" ", 2);
                String keyword = fields[0];
                String data = fields[1];
                if (keyword.equalsIgnoreCase("o")) {
                    this.newObject(data);
                } else if (keyword.equalsIgnoreCase("g")) {
                    this.newGroup(data);
                } else if (keyword.equalsIgnoreCase("mtllib")) {
                    this.loadMaterialLibrary(loc, data);
                } else if (keyword.equalsIgnoreCase("usemtl")) {
                    this.useMaterial(data);
                } else if (keyword.equalsIgnoreCase("v")) {
                    this.addPosition(data);
                } else if (keyword.equalsIgnoreCase("vn")) {
                    this.addNormal(data);
                } else if (keyword.equalsIgnoreCase("vt")) {
                    this.addTexCoord(data);
                } else if (keyword.equalsIgnoreCase("f")) {
                    this.addFace(data);
                } else if (!unknownCommands.contains(keyword)) {
                    unknownCommands.add(keyword);
                }
            }
        }
    }
}
