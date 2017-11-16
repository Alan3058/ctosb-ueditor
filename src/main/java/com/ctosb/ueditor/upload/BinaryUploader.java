
package com.ctosb.ueditor.upload;

import com.ctosb.ueditor.ConfigManager;
import com.ctosb.ueditor.PathFormat;
import com.ctosb.ueditor.define.AppInfo;
import com.ctosb.ueditor.define.BaseState;
import com.ctosb.ueditor.define.FileType;
import com.ctosb.ueditor.define.State;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BinaryUploader {

	public static final State save(HttpServletRequest request, Map<String, Object> conf) {
		if (!ServletFileUpload.isMultipartContent(request)) {
			return new BaseState(false, AppInfo.NOT_MULTIPART_CONTENT);
		}
		boolean isAjaxUpload = request.getHeader("X_Requested_With") != null;
		if (request instanceof MultipartHttpServletRequest) {
			if (isAjaxUpload) {
				// 不知道是什么逻辑，只是为了保持springmvc请求从官方代码处理逻辑一致
				try {
					request.setCharacterEncoding("UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			// 针对springmvc请求做处理，否则造旧处理
			MultipartFile upFile = ((MultipartHttpServletRequest) request).getFile((String) conf.get("fieldName"));
			try {
				return save(request, upFile.getOriginalFilename(), upFile.getInputStream(), conf);
			} catch (IOException e) {
				return new BaseState(false, AppInfo.IO_ERROR);
			}
		}
		// 原先处理逻辑不变，造旧处理
		FileItemStream fileStream = null;
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		if (isAjaxUpload) {
			upload.setHeaderEncoding("UTF-8");
		}
		try {
			FileItemIterator iterator = upload.getItemIterator(request);
			while (iterator.hasNext()) {
				fileStream = iterator.next();
				if (!fileStream.isFormField())
					break;
				fileStream = null;
			}
			if (fileStream == null) {
				return new BaseState(false, AppInfo.NOTFOUND_UPLOAD_DATA);
			}
			return save(request, fileStream.getName(), fileStream.openStream(), conf);
		} catch (FileUploadException e) {
			return new BaseState(false, AppInfo.PARSE_REQUEST_ERROR);
		} catch (IOException e) {
			return new BaseState(false, AppInfo.IO_ERROR);
		}
	}

	private static State save(HttpServletRequest request, String originFileName, InputStream inputStream,
			Map<String, Object> conf) throws IOException {
		String savePath = (String) conf.get("savePath");
		String suffix = FileType.getSuffixByFilename(originFileName);
		originFileName = originFileName.substring(0, originFileName.length() - suffix.length());
		savePath = savePath + suffix;
		long maxSize = ((Long) conf.get("maxSize")).longValue();
		if (!validType(suffix, (String[]) conf.get("allowFiles"))) {
			return new BaseState(false, AppInfo.NOT_ALLOW_FILE_TYPE);
		}
		savePath = PathFormat.parse(savePath, originFileName);
		// 获取保存文件的根目录
		String rootPath = ConfigManager.getRootPath(request, conf);
		// 保存文件的绝对路径
		String physicalPath = rootPath + savePath;
		State storageState = StorageManager.saveFileByInputStream(inputStream, physicalPath, maxSize);
		inputStream.close();
		if (storageState.isSuccess()) {
			storageState.putInfo("url", PathFormat.format(savePath));
			storageState.putInfo("type", suffix);
			storageState.putInfo("original", originFileName + suffix);
		}
		return storageState;
	}

	private static boolean validType(String type, String[] allowTypes) {
		List<String> list = Arrays.asList(allowTypes);
		return list.contains(type);
	}
}
