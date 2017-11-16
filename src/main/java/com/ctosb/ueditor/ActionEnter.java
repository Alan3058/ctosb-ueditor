
package com.ctosb.ueditor;

import com.ctosb.ueditor.define.ActionMap;
import com.ctosb.ueditor.define.AppInfo;
import com.ctosb.ueditor.define.BaseState;
import com.ctosb.ueditor.define.State;
import com.ctosb.ueditor.hunter.FileManager;
import com.ctosb.ueditor.hunter.ImageHunter;
import com.ctosb.ueditor.upload.Uploader;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 入口类
 * @date 2017/11/15 13:24
 * @author liliangang-1163
 * @since 1.0.0
 */
public class ActionEnter {

	private HttpServletRequest request = null;
	private String actionType = null;
	private ConfigManager configManager = null;

	/**
	 * 构造函数
	 * @date 2017/11/15 13:40
	 * @author liliangang-1163
	 * @since 1.0.0
	 * @param request
	 * @param saveFileRootPath （默认为空字符）存储文件的根目录路径，设置为绝对路径，可以保存到非项目路径下，使得上传文件路径和项目脱离
	 * @param configFilePath 配置文件路径+配置文件名称，相对项目的路径
	 */
	public ActionEnter(HttpServletRequest request, String saveFileRootPath, String configFilePath) {
		this.request = request;
		this.actionType = request.getParameter("action");
		saveFileRootPath = saveFileRootPath == null ? "" : saveFileRootPath;
		this.configManager = ConfigManager.getInstance(saveFileRootPath, configFilePath);
	}

	public String exec() {
		String callbackName = this.request.getParameter("callback");
		if (callbackName != null) {
			if (!validCallbackName(callbackName)) {
				return new BaseState(false, AppInfo.ILLEGAL).toJSONString();
			}
			return callbackName + "(" + this.invoke() + ");";
		} else {
			return this.invoke();
		}
	}

	public String invoke() {
		if (actionType == null || !ActionMap.mapping.containsKey(actionType)) {
			return new BaseState(false, AppInfo.INVALID_ACTION).toJSONString();
		}
		if (this.configManager == null || !this.configManager.valid()) {
			return new BaseState(false, AppInfo.CONFIG_ERROR).toJSONString();
		}
		State state = null;
		int actionCode = ActionMap.getType(this.actionType);
		Map<String, Object> conf = null;
		switch (actionCode) {
			case ActionMap.CONFIG:
				return this.configManager.getAllConfig().toString();
			case ActionMap.UPLOAD_IMAGE:
			case ActionMap.UPLOAD_SCRAWL:
			case ActionMap.UPLOAD_VIDEO:
			case ActionMap.UPLOAD_FILE:
				conf = this.configManager.getConfig(actionCode);
				state = new Uploader(request, conf).doExec();
				break;
			case ActionMap.CATCH_IMAGE:
				conf = configManager.getConfig(actionCode);
				String[] list = this.request.getParameterValues((String) conf.get("fieldName"));
				state = new ImageHunter(conf).capture(list);
				break;
			case ActionMap.LIST_IMAGE:
			case ActionMap.LIST_FILE:
				conf = configManager.getConfig(actionCode);
				int start = this.getStartIndex();
				state = new FileManager(conf).listFile(start);
				break;
		}
		return state.toJSONString();
	}

	public int getStartIndex() {
		String start = this.request.getParameter("start");
		try {
			return Integer.parseInt(start);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * callback参数验证
	 */
	public boolean validCallbackName(String name) {
		if (name.matches("^[a-zA-Z_]+[\\w0-9_]*$")) {
			return true;
		}
		return false;
	}
}
