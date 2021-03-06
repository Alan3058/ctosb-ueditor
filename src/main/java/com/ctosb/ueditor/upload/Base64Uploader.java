
package com.ctosb.ueditor.upload;

import com.ctosb.ueditor.ConfigManager;
import com.ctosb.ueditor.PathFormat;
import com.ctosb.ueditor.define.AppInfo;
import com.ctosb.ueditor.define.BaseState;
import com.ctosb.ueditor.define.FileType;
import com.ctosb.ueditor.define.State;
import org.apache.commons.codec.binary.Base64;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public final class Base64Uploader {

	public static State save(HttpServletRequest request, Map<String, Object> conf) {
		String filedName = (String) conf.get("fieldName");
		String fileName = request.getParameter(filedName);
		byte[] data = decode(fileName);
		long maxSize = ((Long) conf.get("maxSize")).longValue();
		if (!validSize(data, maxSize)) {
			return new BaseState(false, AppInfo.MAX_SIZE);
		}
		String suffix = FileType.getSuffix("JPG");
		String savePath = PathFormat.parse((String) conf.get("savePath"), (String) conf.get("filename"));
		savePath = savePath + suffix;
		// 获取保存文件的根目录
		String rootPath = ConfigManager.getRootPath(request, conf);
		// 保存文件的绝对路径
		String physicalPath = rootPath + savePath;
		// 保存文件
		State storageState = StorageManager.saveBinaryFile(data, physicalPath);
		if (storageState.isSuccess()) {
			storageState.putInfo("url", PathFormat.format(savePath));
			storageState.putInfo("type", suffix);
			storageState.putInfo("original", "");
		}
		return storageState;
	}

	private static byte[] decode(String content) {
		return Base64.decodeBase64(content);
	}

	private static boolean validSize(byte[] data, long length) {
		return data.length <= length;
	}
}
