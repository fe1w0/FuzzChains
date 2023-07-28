package xyz.xzaslxr.utils.setting;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;



/**
 * 读取静态分析后得到的配置文件，该配置文件格式为 json 格式，
 * 从而获取Source、Sink、Edges等信息。
 * <p></p>
 * @author fe1w0
 * @version 1.0
*/
public class ReadConfiguration {

    /**
     * 读取静态分析后得到的配置文件，该配置文件格式为 json 格式，
     * 从而获取Source、Sink、Edges等信息。
     * <p></p>
     * @param configurationFilePath the path to the configuration file
     */
    public ConfigurationJson readConfiguration(String configurationFilePath) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        File file = new File(configurationFilePath);

        return objectMapper.readValue(file, ConfigurationJson.class);
    }
}
