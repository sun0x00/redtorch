package xyz.redtorch.common.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CommonFileUtils {

    private static final Logger logger = LoggerFactory.getLogger(CommonFileUtils.class);

    public static List<String> getFileAbsolutePathList(String path, boolean includeSubFolders) {
        List<String> fileAbsolutePathList = new ArrayList<>();

        File file = new File(path);

        File[] tmpFileList = file.listFiles();

        if (tmpFileList != null) {
            for (File tmpFile : tmpFileList) {
                if (tmpFile.isDirectory() && includeSubFolders) {
                    fileAbsolutePathList.addAll(getFileAbsolutePathList(tmpFile.getAbsolutePath(), true));
                } else {
                    fileAbsolutePathList.add(tmpFile.getAbsolutePath());
                }
            }
        } else {
            logger.warn("文件列表为空，路径{}", path);
        }
        return fileAbsolutePathList;
    }

    public static List<String> getFileAbsolutePathList(String path, boolean includeSubFolders, String... suffixes) {

        Set<String> suffixSet = new HashSet<>();
        Collections.addAll(suffixSet, suffixes);

        List<String> fileAbsolutePathList = new ArrayList<>();

        File file = new File(path);

        File[] tmpFileList = file.listFiles();

        if (tmpFileList != null) {
            for (File tmpFile : tmpFileList) {
                if (tmpFile.isDirectory() && includeSubFolders) {
                    fileAbsolutePathList.addAll(getFileAbsolutePathList(tmpFile.getAbsolutePath(), includeSubFolders, suffixes));
                } else {
                    String tmpAbsolutePath = tmpFile.getAbsolutePath();
                    for (String suffix : suffixSet) {
                        if (tmpAbsolutePath.endsWith(suffix)) {
                            fileAbsolutePathList.add(tmpAbsolutePath);
                            break;
                        }
                    }
                }
            }
        } else {
            logger.warn("文件列表为空，路径{}", path);
        }
        return fileAbsolutePathList;
    }

    /**
     * 读取文件到字符串
     */
    public static String readFileToString(String filePath, String encoding) {
        File tmpFile = new File(filePath);
        if (tmpFile.isDirectory() || !tmpFile.exists()) {
            logger.error("读取发生异常,文件不存在{}", filePath);
            return null;
        }
        try {
            return FileUtils.readFileToString(tmpFile, encoding);
        } catch (Exception e) {
            logger.error("读取发生异常", e);
        }

        return null;
    }

    public static String readFileToStringUTF8(String filePath) {
        return readFileToString(filePath, "UTF-8");
    }

    /**
     * 写入字符串到文件
     */
    public static boolean writeStringToFile(String filePath, String content, String encoding) {

        try {
            File file = new File(filePath);
            FileUtils.writeStringToFile(file, content, encoding);
            return true;
        } catch (IOException e) {
            logger.error("写入文件错误", e);
            return false;
        }
    }

    public static boolean writeStringToFileUTF8(String filePath, String content) {
        return writeStringToFile(filePath, content, "UTF-8");
    }

}
