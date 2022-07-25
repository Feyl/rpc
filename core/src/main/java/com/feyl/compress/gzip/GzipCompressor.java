package com.feyl.compress.gzip;

import com.feyl.compress.Compressor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Gzip压缩
 *
 * @author Feyl
 */
public class GzipCompressor  implements Compressor {

    private static final int BUFFER_SIZE = 1024 * 4; //4K

    @Override
    public byte[] compress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes is null");
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gos = new GZIPOutputStream(baos)) {
            gos.write(bytes);
            gos.flush();
            gos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Gzip compress error", e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes is null");
        }
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = gis.read(buf)) > -1) {
                baos.write(buf, 0, n);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Gzip decompress error", e);
        }
    }
}
