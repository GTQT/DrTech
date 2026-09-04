package com.drppp.drtech.drone.program.codec;

import com.drppp.drtech.drone.program.compile.DroneCompileResult;
import com.drppp.drtech.drone.program.compile.DroneProgramCompiler;
import com.drppp.drtech.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.drone.program.registry.DroneNodeRegistry;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTSizeTracker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

/** Bounded text transfer format for clipboard import/export. */
public final class DroneProgramTransferCodec {
    public static final String PREFIX = "DRTECH-PROGRAM-1:";
    public static final int MAX_TEXT_LENGTH = 262_144;
    public static final int MAX_COMPRESSED_BYTES = 196_608;
    public static final long MAX_DECOMPRESSED_BYTES = 2L * 1024L * 1024L;

    private DroneProgramTransferCodec() {}

    public static String encode(DroneProgramGraph graph) throws DroneProgramFormatException {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(DroneProgramNbtCodec.write(graph), output);
            byte[] compressed = output.toByteArray();
            if (compressed.length > MAX_COMPRESSED_BYTES) {
                throw new DroneProgramFormatException("Export exceeds compressed size limit");
            }
            String result = PREFIX + Base64.getEncoder().encodeToString(compressed);
            if (result.length() > MAX_TEXT_LENGTH) throw new DroneProgramFormatException("Export text is too large");
            return result;
        } catch (IOException exception) {
            throw new DroneProgramFormatException("Unable to compress program");
        }
    }

    public static DroneProgramGraph decodeAndValidate(String text, DroneNodeRegistry registry)
            throws DroneProgramFormatException {
        if (text == null || text.length() > MAX_TEXT_LENGTH || !text.startsWith(PREFIX)) {
            throw new DroneProgramFormatException("Invalid program transfer header or size");
        }
        byte[] compressed;
        try {
            compressed = Base64.getDecoder().decode(text.substring(PREFIX.length()));
        } catch (IllegalArgumentException exception) {
            throw new DroneProgramFormatException("Program transfer is not valid Base64");
        }
        if (compressed.length == 0 || compressed.length > MAX_COMPRESSED_BYTES) {
            throw new DroneProgramFormatException("Compressed program exceeds size limit");
        }
        try {
            ByteArrayOutputStream expanded = new ByteArrayOutputStream();
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = gzip.read(buffer)) >= 0) {
                    if (expanded.size() + read > MAX_DECOMPRESSED_BYTES) {
                        throw new DroneProgramFormatException("Decompressed program exceeds size limit");
                    }
                    expanded.write(buffer, 0, read);
                }
            }
            NBTTagCompound root = CompressedStreamTools.read(
                    new DataInputStream(new ByteArrayInputStream(expanded.toByteArray())),
                    new NBTSizeTracker(MAX_DECOMPRESSED_BYTES));
            DroneProgramGraph graph = DroneProgramNbtCodec.read(root);
            DroneCompileResult result = new DroneProgramCompiler(registry).compile(graph);
            if (result.hasErrors()) {
                String code = result.getDiagnostics().isEmpty() ? "invalid_graph"
                        : result.getDiagnostics().get(0).getCode().name().toLowerCase(java.util.Locale.ROOT);
                throw new DroneProgramFormatException("Imported program failed validation: " + code);
            }
            return graph;
        } catch (IOException | RuntimeException exception) {
            throw new DroneProgramFormatException("Unable to decode compressed program");
        }
    }
}
