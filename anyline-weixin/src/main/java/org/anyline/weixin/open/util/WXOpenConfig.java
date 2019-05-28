package org.anyline.weixin.open.util;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;
import org.anyline.weixin.mp.util.WXMPConfig;
import org.anyline.weixin.util.WXConfig;


public class WXOpenConfig extends WXConfig{
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();
	static{
		init();
		debug();
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}

	public static WXOpenConfig getInstance(){
		return getInstance("default");
	}
	public static WXOpenConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}

		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - WXOpenConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			//重新加载
			loadConfig();
		}
		return (WXOpenConfig)instances.get(key);
	}

	public static WXOpenConfig parse(String key, DataRow row){
		return parse(WXOpenConfig.class, key, row, instances, compatibles);
	}
	public static Hashtable<String,BasicConfig> parse(String column, DataSet set){
		for(DataRow row:set){
			String key = row.getString(column);
			parse(key, row);
		}
		return instances;
	}
	/**
	 * 加载配置文件
	 */
	private synchronized static void loadConfig() {
		loadConfig(instances, WXOpenConfig.class, "anyline-weixin-open.xml",compatibles);
		WXOpenConfig.lastLoadTime = System.currentTimeMillis();
	}
	private static void debug(){
	}
}