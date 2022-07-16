package com.feyl.compress;

import com.feyl.extension.SPI;

/**
 * @author Feyl
 */
@SPI
public interface Compressor {

    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);
}
