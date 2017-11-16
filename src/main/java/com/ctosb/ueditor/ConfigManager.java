
package com.ctosb.ueditor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ctosb.ueditor.define.ActionMap;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置管理器
 * @date 2017/11/15 13:22
 * @author liliangang-1163
 * @since 1.0.0
 */
public final class ConfigManager {

	private final String rootPath;
	private JSONObject jsonConfig;
	// 涂鸦上传filename定义
	private final static String SCRAWL_FILE_NAME = "scrawl";
	// 远程图片抓取filename定义
	private final static String REMOTE_FILE_NAME = "remote";

	/**
	 * 通过一个给定的路径构建一个配置管理器
	 * @date 2017/11/15 13:23
	 * @author liliangang-1163
	 * @since 1.0.0
	 * @param saveFileRootPath 存储文件的根目录路径，设置为绝对路径，可以保存到非项目路径下，使得上传文件路径和项目脱离
	 * @param configFilePath 配置文件路径+配置文件名称，相对项目的路径
	 * @throws IOException
	 */
	private ConfigManager(String saveFileRootPath, String configFilePath) throws IOException {
		this.rootPath = saveFileRootPath.replace("\\", "/");
		// 读取配置文件内容
		String configContent = this.readFile(this.getConfigFileInputStream(configFilePath));
		try {
			// 解析配置文件成json对象
			this.jsonConfig = (JSONObject) JSONObject.parse(configContent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 配置管理器构造工厂
	 * @date 2017/11/15 13:15
	 * @author liliangang-1163
	 * @since 1.0.0
	 * @param saveFileRootPath 存储文件的根目录路径，设置为绝对路径，可以保存到非项目路径下，使得上传文件路径和项目脱离
	 * @param configFilePath 配置文件路径+配置文件名称，相对项目的路径
	 * @return 配置管理器实例或者null
	 */
	public static ConfigManager getInstance(String saveFileRootPath, String configFilePath) {
		try {
			return new ConfigManager(saveFileRootPath, configFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 验证配置文件加载是否正确
	 * @date 2017/11/15 11:48
	 * @author liliangang-1163
	 * @since 1.0.0
	 */
	public boolean valid() {
		return this.jsonConfig != null;
	}

	public JSONObject getAllConfig() {
		return this.jsonConfig;
	}

	/**
	 * 获取对应类型的配置文件信息
	 * @date 2017/11/15 13:50
	 * @author liliangang-1163
	 * @since 1.0.0
	 * @param type
	 * @return
	 */
	public Map<String, Object> getConfig(int type) {
		Map<String, Object> conf = new HashMap<String, Object>();
		String savePath = null;
		switch (type) {
			case ActionMap.UPLOAD_FILE:
				conf.put("isBase64", "false");
				conf.put("maxSize", this.jsonConfig.getLong("fileMaxSize"));
				conf.put("allowFiles", this.getArray("fileAllowFiles"));
				conf.put("fieldName", this.jsonConfig.getString("fileFieldName"));
				savePath = this.jsonConfig.getString("filePathFormat");
				break;
			case ActionMap.UPLOAD_IMAGE:
				conf.put("isBase64", "false");
				conf.put("maxSize", this.jsonConfig.getLong("imageMaxSize"));
				conf.put("allowFiles", this.getArray("imageAllowFiles"));
				conf.put("fieldName", this.jsonConfig.getString("imageFieldName"));
				savePath = this.jsonConfig.getString("imagePathFormat");
				break;
			case ActionMap.UPLOAD_VIDEO:
				conf.put("maxSize", this.jsonConfig.getLong("videoMaxSize"));
				conf.put("allowFiles", this.getArray("videoAllowFiles"));
				conf.put("fieldName", this.jsonConfig.getString("videoFieldName"));
				savePath = this.jsonConfig.getString("videoPathFormat");
				break;
			case ActionMap.UPLOAD_SCRAWL:
				conf.put("filename", ConfigManager.SCRAWL_FILE_NAME);
				conf.put("maxSize", this.jsonConfig.getLong("scrawlMaxSize"));
				conf.put("fieldName", this.jsonConfig.getString("scrawlFieldName"));
				conf.put("isBase64", "true");
				savePath = this.jsonConfig.getString("scrawlPathFormat");
				break;
			case ActionMap.CATCH_IMAGE:
				conf.put("filename", ConfigManager.REMOTE_FILE_NAME);
				conf.put("filter", this.getArray("catcherLocalDomain"));
				conf.put("maxSize", this.jsonConfig.getLong("catcherMaxSize"));
				conf.put("allowFiles", this.getArray("catcherAllowFiles"));
				conf.put("fieldName", this.jsonConfig.getString("catcherFieldName") + "[]");
				savePath = this.jsonConfig.getString("catcherPathFormat");
				break;
			case ActionMap.LIST_IMAGE:
				conf.put("allowFiles", this.getArray("imageManagerAllowFiles"));
				conf.put("dir", this.jsonConfig.getString("imageManagerListPath"));
				conf.put("count", this.jsonConfig.getIntValue("imageManagerListSize"));
				break;
			case ActionMap.LIST_FILE:
				conf.put("allowFiles", this.getArray("fileManagerAllowFiles"));
				conf.put("dir", this.jsonConfig.getString("fileManagerListPath"));
				conf.put("count", this.jsonConfig.getIntValue("fileManagerListSize"));
				break;
		}
		conf.put("savePath", savePath);
		conf.put("rootPath", this.rootPath);
		return conf;
	}

	/**
	 * Get rootPath from request,if not,find it from conf map.
	 * @param request
	 * @param conf
	 * @return
	 * @author Ternence
	 * @create 2015年1月31日
	 */
	public static String getRootPath(HttpServletRequest request, Map<String, Object> conf) {
		Object rootPath = request.getAttribute("rootPath");
		if (rootPath != null) {
			return rootPath + "" + File.separatorChar;
		} else {
			return conf.get("rootPath") + "";
		}
	}

	/**
	 * 获取配置文件输入流，不使用获取文件名称方式。以便springboot项目时，读取jar包中的配置
	 * @date 2017/11/15 13:41
	 * @author liliangang-1163
	 * @since 1.0.0
	 * @param configFilePath
	 * @return
	 */
	private InputStream getConfigFileInputStream(String configFilePath) {
		return getClass().getClassLoader().getResourceAsStream(configFilePath);
	}

	private String[] getArray(String key) {
		JSONArray jsonArray = this.jsonConfig.getJSONArray(key);
		String[] result = new String[jsonArray.size()];
		for (int i = 0, len = jsonArray.size(); i < len; i++) {
			result[i] = jsonArray.getString(i);
		}
		return result;
	}

	private String readFile(InputStream inputStream) throws IOException {
		StringBuilder builder = new StringBuilder();
		try {
			InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
			BufferedReader bfReader = new BufferedReader(reader);
			String tmpContent = null;
			while ((tmpContent = bfReader.readLine()) != null) {
				builder.append(tmpContent);
			}
			bfReader.close();
		} catch (UnsupportedEncodingException e) {
			// 忽略
			e.printStackTrace();
		}
		return this.filter(builder.toString());
	}

	/**
	 * 过滤输入字符串, 剔除多行注释以及替换掉反斜杠
	 * @date 2017/11/15 13:49
	 * @author liliangang-1163
	 * @since 1.0.0
	 * @param input
	 * @return
	 */
	private String filter(String input) {
		return input.replaceAll("/\\*[\\s\\S]*?\\*/", "");
	}
}
