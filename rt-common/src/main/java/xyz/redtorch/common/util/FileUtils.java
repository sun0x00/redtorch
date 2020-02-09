package xyz.redtorch.common.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

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

}
