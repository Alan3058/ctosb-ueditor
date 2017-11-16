# ctosb-ueditor
改造官方ueditor jar包，主要有以下改变。
1. 修正官方jar包在springmvc项目中不能获取到文件信息。由于springmvc在接收文件时已经对请求的文件进行了一次包装解析（详见CommonsMultipartResolver类parseRequest方法中已经使用了ServletFileUpload去解析文件）
2. 配置文件名称可自定义。
3. 升级org.json为阿里的fastjson。
