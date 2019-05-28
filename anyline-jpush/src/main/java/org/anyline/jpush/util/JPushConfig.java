package org.anyline.jpush.util;

import java.util.Hashtable;

import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

public class JPushConfig extends BasicConfig{
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();
	public String APP_KEY ="";
	public String MASTER_SECRET ="";
		
	static{
		init();
		debug();
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}

	public static JPushConfig getInstance(){
		return getInstance("default");
	}
	public static JPushConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}

		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - JPushConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			//重新加载
			loadConfig();
		}
		return (JPushConfig)instances.get(key);
	}
	/**
	 * 加载配置文件
	 */
	private synchronized static void loadConfig() {
		loadConfig(instances, JPushConfig.class, "anyline-jpush.xml");
		JPushConfig.lastLoadTime = System.currentTimeMillis();
	}
	private static void debug(){
	}
}