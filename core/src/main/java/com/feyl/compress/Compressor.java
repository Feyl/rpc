package com.feyl.compress;

import com.feyl.extension.SPI;

/**
 * 字节序列压缩器接口
 *
 * @author Feyl
 */
@SPI
public interface Compressor {

    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);
}
