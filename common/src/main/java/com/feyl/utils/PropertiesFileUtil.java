package com.feyl.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * properties 配置文件工具类
 *
 * @author Feyl
 */
@Slf4j
public class PropertiesFileUtil {

    private PropertiesFileUtil() {}

    /**
     * 通过类路径下的 properties文件名获取文件对应的 Properties对象
     * @param fileName
     * @return
     */
    public static Properties read(String fileName) {
        // 得到当前ClassPath的绝对URI路径
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if (url != null) {
            rpcConfigPath = url.getPath() + fileName;
        }
        Properties properties = null;
        try (InputStreamReader inputStreamReader = new InputStreamReader(
                new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
            properties = new Properties();
            properties.load(inputStreamReader);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("Occur exception when read properties file [{}]", fileName);
        }
        return properties;
    }

}
