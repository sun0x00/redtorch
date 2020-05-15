package xyz.redtorch.common.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonFileUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(CommonFileUtils.class);

	public static List<String> getFileAbsolutePathList(String path, boolean includeSubfolders) {
		List<String> fileAbsolutePathList = new ArrayList<>();

		File file = new File(path);

		File[] tmpFileList = file.listFiles();

		if (tmpFileList != null) {
			for (File tmpFile : tmpFileList) {
				if (tmpFile.isDirectory() && includeSubfolders) {
					fileAbsolutePathList.addAll(getFileAbsolutePathList(tmpFile.getAbsolutePath(), includeSubfolders));
				} else {
					fileAbsolutePathList.add(tmpFile.getAbsolutePath());
				}
			}
		} else {
			logger.warn("文件列表为空，路径{}",path);
		}
		return fileAbsolutePathList;
	}

	public static List<String> getFileAbsolutePathList(String path, boolean includeSubfolders, String... suffixes) {

		Set<String> suffixSet = new HashSet<>();
		for (String suffix : suffixes) {
			suffixSet.add(suffix);
		}

		List<String> fileAbsolutePathList = new ArrayList<>();

		File file = new File(path);

		File[] tmpFileList = file.listFiles();

		if (tmpFileList != null) {
			for (File tmpFile : tmpFileList) {
				if (tmpFile.isDirectory() && includeSubfolders) {
					fileAbsolutePathList.addAll(getFileAbsolutePathList(tmpFile.getAbsolutePath(), includeSubfolders, suffixes));
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
			logger.warn("文件列表为空，路径{}",path);
		}
		return fileAbsolutePathList;
	}
	
	/**
	 * 读取文件到字符串
	 * 
	 * @param file
	 * @return
	 */
	public static String readFileToString(String filePath, String encoding) {
		File tmpFile = new File(filePath);
		if (tmpFile.isDirectory() || !tmpFile.exists()) {
			logger.error("读取发生异常,文件不存在{}", filePath);
			return null;
		}
		try (FileReader fileReader = new FileReader(filePath); Scanner scanner = new Scanner(fileReader);) {
			// Read the entire contents of sample.txt
			String content = FileUtils.readFileToString(tmpFile, encoding);
			return content;
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
	 * 
	 * @param filePath
	 * @param content
	 * @return
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
