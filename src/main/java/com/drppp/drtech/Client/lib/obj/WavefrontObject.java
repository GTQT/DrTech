package com.drppp.drtech.Client.lib.obj;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WavefrontObject implements IModelCustom {
    private static Pattern vertexPattern = Pattern.compile("(v( (\\-){0,1}\\d+\\.\\d+){3,4} *\\n)|(v( (\\-){0,1}\\d+\\.\\d+){3,4} *$)");
    private static Pattern vertexNormalPattern = Pattern.compile("(vn( (\\-){0,1}\\d+\\.\\d+){3,4} *\\n)|(vn( (\\-){0,1}\\d+\\.\\d+){3,4} *$)");
    private static Pattern textureCoordinatePattern = Pattern.compile("(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *\\n)|(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *$)");
    private static Pattern face_V_VT_VN_Pattern = Pattern.compile("(f( \\d+/\\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+/\\d+){3,4} *$)");
    private static Pattern face_V_VT_Pattern = Pattern.compile("(f( \\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+){3,4} *$)");
    private static Pattern face_V_VN_Pattern = Pattern.compile("(f( \\d+//\\d+){3,4} *\\n)|(f( \\d+//\\d+){3,4} *$)");
    private static Pattern face_V_Pattern = Pattern.compile("(f( \\d+){3,4} *\\n)|(f( \\d+){3,4} *$)");
    private static Pattern groupObjectPattern = Pattern.compile("([go]( [\\w\\d\\.]+) *\\n)|([go]( [\\w\\d\\.]+) *$)");
    private static Matcher vertexMatcher;
    private static Matcher vertexNormalMatcher;
    private static Matcher textureCoordinateMatcher;
    private static Matcher face_V_VT_VN_Matcher;
    private static Matcher face_V_VT_Matcher;
    private static Matcher face_V_VN_Matcher;
    private static Matcher face_V_Matcher;
    private static Matcher groupObjectMatcher;
    public ArrayList<Vertex> vertices = new ArrayList();
    public ArrayList<Vertex> vertexNormals = new ArrayList();
    public ArrayList<TextureCoordinate> textureCoordinates = new ArrayList();
    public ArrayList<GroupObject> groupObjects = new ArrayList();
    private GroupObject currentGroupObject;
    private String fileName;

    public WavefrontObject(ResourceLocation resource) throws ModelFormatException {
        this.fileName = resource.toString();

        try {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(resource);
            this.loadObjModel(res.getInputStream());
        } catch (IOException var3) {
            IOException e = var3;
            throw new WavefrontObject.ModelFormatException("IO Exception reading model format", e);
        }
    }

    public WavefrontObject(String filename, InputStream inputStream) throws ModelFormatException {
        this.fileName = filename;
        this.loadObjModel(inputStream);
    }

    private void loadObjModel(InputStream inputStream) throws ModelFormatException {
        BufferedReader reader = null;
        String currentLine = null;
        int lineCount = 0;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));

            while((currentLine = reader.readLine()) != null) {
                ++lineCount;
                currentLine = currentLine.replaceAll("\\s+", " ").trim();
                if (!currentLine.startsWith("#") && currentLine.length() != 0) {
                    Vertex vertex;
                    if (currentLine.startsWith("v ")) {
                        vertex = this.parseVertex(currentLine, lineCount);
                        if (vertex != null) {
                            this.vertices.add(vertex);
                        }
                    } else if (currentLine.startsWith("vn ")) {
                        vertex = this.parseVertexNormal(currentLine, lineCount);
                        if (vertex != null) {
                            this.vertexNormals.add(vertex);
                        }
                    } else if (currentLine.startsWith("vt ")) {
                        TextureCoordinate textureCoordinate = this.parseTextureCoordinate(currentLine, lineCount);
                        if (textureCoordinate != null) {
                            this.textureCoordinates.add(textureCoordinate);
                        }
                    } else if (currentLine.startsWith("f ")) {
                        if (this.currentGroupObject == null) {
                            this.currentGroupObject = new GroupObject("Default");
                        }

                        Face face = this.parseFace(currentLine, lineCount);
                        if (face != null) {
                            this.currentGroupObject.faces.add(face);
                        }
                    } else if (currentLine.startsWith("g ") | currentLine.startsWith("o ")) {
                        GroupObject group = this.parseGroupObject(currentLine, lineCount);
                        if (group != null && this.currentGroupObject != null) {
                            this.groupObjects.add(this.currentGroupObject);
                        }

                        this.currentGroupObject = group;
                    }
                }
            }

            this.groupObjects.add(this.currentGroupObject);
        } catch (IOException var16) {
            IOException e = var16;
            throw new ModelFormatException("IO Exception reading model format", e);
        } finally {
            try {
                reader.close();
            } catch (IOException var15) {
            }

            try {
                inputStream.close();
            } catch (IOException var14) {
            }

        }
    }

    @SideOnly(Side.CLIENT)
    public void renderAll() {
        Tessellator tessellator = Tessellator.getInstance();
        if (this.currentGroupObject != null) {
            tessellator.getBuffer().begin(this.currentGroupObject.glDrawingMode, DefaultVertexFormats.POSITION_TEX_NORMAL);
        } else {
            tessellator.getBuffer().begin(4, DefaultVertexFormats.POSITION_TEX_NORMAL);
        }

        this.tessellateAll(tessellator);
        tessellator.draw();
    }

    @SideOnly(Side.CLIENT)
    public void tessellateAll(Tessellator tessellator) {
        Iterator var2 = this.groupObjects.iterator();

        while(var2.hasNext()) {
            GroupObject groupObject = (GroupObject)var2.next();
            groupObject.render(tessellator);
        }

    }

    @SideOnly(Side.CLIENT)
    public void renderOnly(String... groupNames) {
        Iterator var2 = this.groupObjects.iterator();

        while(var2.hasNext()) {
            GroupObject groupObject = (GroupObject)var2.next();
            String[] var4 = groupNames;
            int var5 = groupNames.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String groupName = var4[var6];
                if (groupName.equalsIgnoreCase(groupObject.name)) {
                    groupObject.render();
                }
            }
        }

    }

    @SideOnly(Side.CLIENT)
    public void tessellateOnly(Tessellator tessellator, String... groupNames) {
        Iterator var3 = this.groupObjects.iterator();

        while(var3.hasNext()) {
            GroupObject groupObject = (GroupObject)var3.next();
            String[] var5 = groupNames;
            int var6 = groupNames.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String groupName = var5[var7];
                if (groupName.equalsIgnoreCase(groupObject.name)) {
                    groupObject.render(tessellator);
                }
            }
        }

    }

    @SideOnly(Side.CLIENT)
    public String[] getPartNames() {
        ArrayList<String> l = new ArrayList();
        Iterator var2 = this.groupObjects.iterator();

        while(var2.hasNext()) {
            GroupObject groupObject = (GroupObject)var2.next();
            l.add(groupObject.name);
        }

        return (String[])l.toArray(new String[0]);
    }

    @SideOnly(Side.CLIENT)
    public void renderPart(String partName) {
        Iterator var2 = this.groupObjects.iterator();

        while(var2.hasNext()) {
            GroupObject groupObject = (GroupObject)var2.next();
            if (partName.equalsIgnoreCase(groupObject.name)) {
                groupObject.render();
            }
        }

    }

    @SideOnly(Side.CLIENT)
    public void tessellatePart(Tessellator tessellator, String partName) {
        Iterator var3 = this.groupObjects.iterator();

        while(var3.hasNext()) {
            GroupObject groupObject = (GroupObject)var3.next();
            if (partName.equalsIgnoreCase(groupObject.name)) {
                groupObject.render(tessellator);
            }
        }

    }

    @SideOnly(Side.CLIENT)
    public void renderAllExcept(String... excludedGroupNames) {
        Iterator var2 = this.groupObjects.iterator();

        while(var2.hasNext()) {
            GroupObject groupObject = (GroupObject)var2.next();
            boolean skipPart = false;
            String[] var5 = excludedGroupNames;
            int var6 = excludedGroupNames.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String excludedGroupName = var5[var7];
                if (excludedGroupName.equalsIgnoreCase(groupObject.name)) {
                    skipPart = true;
                }
            }

            if (!skipPart) {
                groupObject.render();
            }
        }

    }

    @SideOnly(Side.CLIENT)
    public void tessellateAllExcept(Tessellator tessellator, String... excludedGroupNames) {
        Iterator var4 = this.groupObjects.iterator();

        while(var4.hasNext()) {
            GroupObject groupObject = (GroupObject)var4.next();
            boolean exclude = false;
            String[] var6 = excludedGroupNames;
            int var7 = excludedGroupNames.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                String excludedGroupName = var6[var8];
                if (excludedGroupName.equalsIgnoreCase(groupObject.name)) {
                    exclude = true;
                }
            }

            if (!exclude) {
                groupObject.render(tessellator);
            }
        }

    }

    private Vertex parseVertex(String line, int lineCount) throws ModelFormatException {
        Vertex vertex = null;
        if (isValidVertexLine(line)) {
            line = line.substring(line.indexOf(" ") + 1);
            String[] tokens = line.split(" ");

            try {
                if (tokens.length == 2) {
                    return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]));
                } else {
                    return (Vertex)(tokens.length == 3 ? new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])) : vertex);
                }
            } catch (NumberFormatException var6) {
                NumberFormatException e = var6;
                throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "', line " + lineCount + ") in file '" + this.fileName + "' - Incorrect format");
        }
    }

    private Vertex parseVertexNormal(String line, int lineCount) throws ModelFormatException {
        Vertex vertexNormal = null;
        if (isValidVertexNormalLine(line)) {
            line = line.substring(line.indexOf(" ") + 1);
            String[] tokens = line.split(" ");

            try {
                return (Vertex)(tokens.length == 3 ? new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])) : vertexNormal);
            } catch (NumberFormatException var6) {
                NumberFormatException e = var6;
                throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "', line " + lineCount + ") in file '" + this.fileName + "' - Incorrect format");
        }
    }

    private TextureCoordinate parseTextureCoordinate(String line, int lineCount) throws ModelFormatException {
        TextureCoordinate textureCoordinate = null;
        if (isValidTextureCoordinateLine(line)) {
            line = line.substring(line.indexOf(" ") + 1);
            String[] tokens = line.split(" ");

            try {
                if (tokens.length == 2) {
                    return new TextureCoordinate(Float.parseFloat(tokens[0]), 1.0F - Float.parseFloat(tokens[1]));
                } else {
                    return (TextureCoordinate)(tokens.length == 3 ? new TextureCoordinate(Float.parseFloat(tokens[0]), 1.0F - Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])) : textureCoordinate);
                }
            } catch (NumberFormatException var6) {
                NumberFormatException e = var6;
                throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "', line " + lineCount + ") in file '" + this.fileName + "' - Incorrect format");
        }
    }
    private Face parseFace(String line, int lineCount) throws ModelFormatException {
        Face face = null;
        if (isValidFaceLine(line)) {
            face = new Face();
            String trimmedLine = line.substring(line.indexOf(" ") + 1);
            String[] tokens = trimmedLine.split(" ");
            String[] subTokens = null;

            // 支持三角形面和四边形面
            if (tokens.length == 3 || tokens.length == 4) {
                if (this.currentGroupObject.glDrawingMode == -1) {
                    this.currentGroupObject.glDrawingMode = (tokens.length == 3) ? 4 : 7; // GL_TRIANGLES 或 GL_QUADS
                } else if (this.currentGroupObject.glDrawingMode != 4 && this.currentGroupObject.glDrawingMode != 7) {
                    throw new ModelFormatException("Error parsing entry ('" + line + "', line " + lineCount + ") in file '" + this.fileName + "' - Invalid number of points for face (expected 3 or 4, found " + tokens.length + ")");
                }
            } else {
                // 如果既不是三角形面也不是四边形面，抛出异常
                throw new ModelFormatException("Error parsing entry ('" + line + "', line " + lineCount + ") in file '" + this.fileName + "' - Invalid number of points for face (expected 3 or 4, found " + tokens.length + ")");
            }

            // 解析 V/Vt/Vn
            int i;
            if (isValidFace_V_VT_VN_Line(line)) {
                face.vertices = new Vertex[tokens.length];
                face.textureCoordinates = new TextureCoordinate[tokens.length];
                face.vertexNormals = new Vertex[tokens.length];

                for(i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("/");
                    face.vertices[i] = (Vertex)this.vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.textureCoordinates[i] = (TextureCoordinate)this.textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
                    face.vertexNormals[i] = (Vertex)this.vertexNormals.get(Integer.parseInt(subTokens[2]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            } else if (isValidFace_V_VT_Line(line)) {
                face.vertices = new Vertex[tokens.length];
                face.textureCoordinates = new TextureCoordinate[tokens.length];

                for(i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("/");
                    face.vertices[i] = (Vertex)this.vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.textureCoordinates[i] = (TextureCoordinate)this.textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            } else if (isValidFace_V_VN_Line(line)) {
                face.vertices = new Vertex[tokens.length];
                face.vertexNormals = new Vertex[tokens.length];

                for(i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("//");
                    face.vertices[i] = (Vertex)this.vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.vertexNormals[i] = (Vertex)this.vertexNormals.get(Integer.parseInt(subTokens[1]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            } else {
                if (!isValidFace_V_Line(line)) {
                    throw new ModelFormatException("Error parsing entry ('" + line + "', line " + lineCount + ") in file '" + this.fileName + "' - Incorrect format");
                }

                face.vertices = new Vertex[tokens.length];

                for(i = 0; i < tokens.length; ++i) {
                    face.vertices[i] = (Vertex)this.vertices.get(Integer.parseInt(tokens[i]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            }

            return face;
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "', line " + lineCount + ") in file '" + this.fileName + "' - Incorrect format");
        }
    }

    private GroupObject parseGroupObject(String line, int lineCount) throws ModelFormatException {
        GroupObject group = null;
        if (isValidGroupObjectLine(line)) {
            String trimmedLine = line.substring(line.indexOf(" ") + 1);
            if (trimmedLine.length() > 0) {
                group = new GroupObject(trimmedLine);
            }

            return group;
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "', line " + lineCount + ") in file '" + this.fileName + "' - Incorrect format");
        }
    }

    private static boolean isValidVertexLine(String line) {
        if (vertexMatcher != null) {
            vertexMatcher.reset();
        }

        vertexMatcher = vertexPattern.matcher(line);
        return vertexMatcher.matches();
    }

    private static boolean isValidVertexNormalLine(String line) {
        if (vertexNormalMatcher != null) {
            vertexNormalMatcher.reset();
        }

        vertexNormalMatcher = vertexNormalPattern.matcher(line);
        return vertexNormalMatcher.matches();
    }

    private static boolean isValidTextureCoordinateLine(String line) {
        if (textureCoordinateMatcher != null) {
            textureCoordinateMatcher.reset();
        }

        textureCoordinateMatcher = textureCoordinatePattern.matcher(line);
        return textureCoordinateMatcher.matches();
    }

    private static boolean isValidFace_V_VT_VN_Line(String line) {
        if (face_V_VT_VN_Matcher != null) {
            face_V_VT_VN_Matcher.reset();
        }

        face_V_VT_VN_Matcher = face_V_VT_VN_Pattern.matcher(line);
        return face_V_VT_VN_Matcher.matches();
    }

    private static boolean isValidFace_V_VT_Line(String line) {
        if (face_V_VT_Matcher != null) {
            face_V_VT_Matcher.reset();
        }

        face_V_VT_Matcher = face_V_VT_Pattern.matcher(line);
        return face_V_VT_Matcher.matches();
    }

    private static boolean isValidFace_V_VN_Line(String line) {
        if (face_V_VN_Matcher != null) {
            face_V_VN_Matcher.reset();
        }

        face_V_VN_Matcher = face_V_VN_Pattern.matcher(line);
        return face_V_VN_Matcher.matches();
    }

    private static boolean isValidFace_V_Line(String line) {
        if (face_V_Matcher != null) {
            face_V_Matcher.reset();
        }

        face_V_Matcher = face_V_Pattern.matcher(line);
        return face_V_Matcher.matches();
    }

    private static boolean isValidFaceLine(String line) {
        return isValidFace_V_VT_VN_Line(line) || isValidFace_V_VT_Line(line) || isValidFace_V_VN_Line(line) || isValidFace_V_Line(line);
    }

    private static boolean isValidGroupObjectLine(String line) {
        if (groupObjectMatcher != null) {
            groupObjectMatcher.reset();
        }

        groupObjectMatcher = groupObjectPattern.matcher(line);
        return groupObjectMatcher.matches();
    }

    public String getType() {
        return "obj";
    }

    public class ModelFormatException extends RuntimeException {
        private static final long serialVersionUID = 2023547503969671835L;

        public ModelFormatException() {
        }

        public ModelFormatException(String message, Throwable cause) {
            super(message, cause);
        }

        public ModelFormatException(String message) {
            super(message);
        }

        public ModelFormatException(Throwable cause) {
            super(cause);
        }
    }

    public class GroupObject {
        public String name;
        public ArrayList<Face> faces;
        public int glDrawingMode;

        public GroupObject() {
            this("");
        }

        public GroupObject(String name) {
            this(name, -1);
        }

        public GroupObject(String name, int glDrawingMode) {
            this.faces = new ArrayList();
            this.name = name;
            this.glDrawingMode = glDrawingMode;
        }

        @SideOnly(Side.CLIENT)
        public void render() {
            if (this.faces.size() > 0) {
                Tessellator tessellator = Tessellator.getInstance();
                tessellator.getBuffer().begin(this.glDrawingMode, DefaultVertexFormats.POSITION_TEX_NORMAL);
                this.render(tessellator);
                tessellator.draw();
            }

        }

        @SideOnly(Side.CLIENT)
        public void render(Tessellator tessellator) {
            if (this.faces.size() > 0) {
                Iterator var2 = this.faces.iterator();

                while(var2.hasNext()) {
                    Face face = (Face)var2.next();
                    face.addFaceForRender(tessellator);
                }
            }

        }
    }

    public class Face {
        public Vertex[] vertices;
        public Vertex[] vertexNormals;
        public Vertex faceNormal;
        public TextureCoordinate[] textureCoordinates;

        public Face() {
        }

        @SideOnly(Side.CLIENT)
        public void addFaceForRender(Tessellator tessellator) {
            this.addFaceForRender(tessellator, 5.0E-4F);
        }

        @SideOnly(Side.CLIENT)
        public void addFaceForRender(Tessellator tessellator, float textureOffset) {
            if (this.faceNormal == null) {
                this.faceNormal = this.calculateFaceNormal();
            }

            float averageU = 0.0F;
            float averageV = 0.0F;
            if (this.textureCoordinates != null && this.textureCoordinates.length > 0) {
                for(int ix = 0; ix < this.textureCoordinates.length; ++ix) {
                    averageU += this.textureCoordinates[ix].u;
                    averageV += this.textureCoordinates[ix].v;
                }

                averageU /= (float)this.textureCoordinates.length;
                averageV /= (float)this.textureCoordinates.length;
            }

            for(int i = 0; i < this.vertices.length; ++i) {
                if (this.textureCoordinates != null && this.textureCoordinates.length > 0) {
                    float offsetU = textureOffset;
                    float offsetV = textureOffset;
                    if (this.textureCoordinates[i].u > averageU) {
                        offsetU = -textureOffset;
                    }

                    if (this.textureCoordinates[i].v > averageV) {
                        offsetV = -offsetV;
                    }

                    tessellator.getBuffer().pos((double)this.vertices[i].x, (double)this.vertices[i].y, (double)this.vertices[i].z).tex((double)(this.textureCoordinates[i].u + offsetU), (double)(this.textureCoordinates[i].v + offsetV)).normal(this.faceNormal.x, this.faceNormal.y, this.faceNormal.z).endVertex();
                } else {
                    tessellator.getBuffer().pos((double)this.vertices[i].x, (double)this.vertices[i].y, (double)this.vertices[i].z).normal(this.faceNormal.x, this.faceNormal.y, this.faceNormal.z).endVertex();
                }
            }

        }

        public Vertex calculateFaceNormal() {
            Vec3d v1 = new Vec3d((double)(this.vertices[1].x - this.vertices[0].x), (double)(this.vertices[1].y - this.vertices[0].y), (double)(this.vertices[1].z - this.vertices[0].z));
            Vec3d v2 = new Vec3d((double)(this.vertices[2].x - this.vertices[0].x), (double)(this.vertices[2].y - this.vertices[0].y), (double)(this.vertices[2].z - this.vertices[0].z));
            Vec3d normalVector = null;
            normalVector = v1.crossProduct(v2).normalize();
            return new Vertex((float)normalVector.x, (float)normalVector.y, (float)normalVector.z);
        }
    }

    public class TextureCoordinate {
        public float u;
        public float v;
        public float w;

        public TextureCoordinate(float u, float v) {
            this(u, v, 0.0F);
        }

        public TextureCoordinate(float u, float v, float w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }
    }
}
