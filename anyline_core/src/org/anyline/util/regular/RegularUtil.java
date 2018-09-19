/* 
 * Copyright 2006-2015 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.util.regular;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;


public class RegularUtil {
	private static Regular regular;
	public static Regular regularMatch 		= new RegularMatch();			//完全匹配模式
	public static Regular regularMatchPrefix 	= new RegularMatchPrefix();		//前缀匹配模式
	public static Regular regularContain 		= new RegularContain();			//包含匹配模式
	
	private static final Map<Regular.MATCH_MODE,Regular> regularList = new HashMap<Regular.MATCH_MODE,Regular>();
	public static final String REGEX_VARIABLE = "{(\\w+)}";		//变量{ID}

	public static final String TAG_BEGIN = "{begin}";
	public static final String TAG_END = "{end}";
	
	private static Logger log = Logger.getLogger(RegularUtil.class);
	static{
		regularList.put(Regular.MATCH_MODE.MATCH, regularMatch);
		regularList.put(Regular.MATCH_MODE.PREFIX, regularMatchPrefix);
		regularList.put(Regular.MATCH_MODE.CONTAIN, regularContain);
	}
	public static synchronized boolean match(String src, String regx, Regular.MATCH_MODE mode){
		boolean result = false;
		if(ConfigTable.getBoolean("IS_REGULAR_LOG")){
			log.warn("[match][src:"+src+"][regx:"+regx+"][mode:"+mode+"]");
		}
		if(null == src || null == regx ){
			return result;
		}
		regular = regularList.get(mode);
		try{
			result = regular.match(src, regx);
		}catch(Exception e){
			log.warn("[match(src,regx,mode) error][src:"+src+"][regx:"+regx+"][mode:"+mode+"]");
			e.printStackTrace();
		}
		if(ConfigTable.getBoolean("IS_REGULAR_LOG")){
			log.warn("[match][src:"+src+"][regx:"+regx+"][mode:"+mode+"][result:"+result+"]");
		}
		return result;
	}
	public static boolean match(String src, String regx){
		return match(src, regx, Regular.MATCH_MODE.CONTAIN);
	}
	
	/**
	 * 提取子串
	 * @param src	输入字符串
	 * @param regx	表达式
	 * @return
	 */
	public static synchronized List<List<String>> fetch(String src, String regx, Regular.MATCH_MODE mode) throws Exception{
		List<List<String>> result = null;
		regular = regularList.get(mode);
		result = regular.fetch(src, regx);
		return result;
	}
	public static List<List<String>> fetch(String src, String regx) throws Exception{
		return fetch(src, regx, Regular.MATCH_MODE.CONTAIN);
	}
	public static synchronized List<String> fetch(String src, String regx, Regular.MATCH_MODE mode, int idx) throws Exception{
		List<String> result = null;
		regular = regularList.get(mode);
		result = regular.fetch(src, regx, idx);
		return result;
	}
	public static List<String> filter(List<String> src, String regx, int regxMode, Regular.FILTER_TYPE type){
		if(Regular.FILTER_TYPE.PICK == type){
			return pick(src,regx,regxMode);
		}else if(Regular.FILTER_TYPE.WIPE == type){
			return wipe(src,regx,regxMode);
		}else{
			return new ArrayList<String>();
		}
	}
	/**
	 * 过滤 保留匹配项
	 * @param src
	 * @param regx
	 * @return
	 */
	public static synchronized List<String> pick(List<String> src, String regx, int mode){
		regular = regularList.get(mode);
		return regular.pick(src, regx);
	}
	/**
	 * 过滤 删除匹配项
	 * @param src
	 * @param regx
	 * @return
	 */
	public static synchronized List<String> wipe(List<String> src, String regx, int mode){
		regular = regularList.get(mode);
		return regular.wipe(src, regx);
	}
	/**
	 * 字符串下标 regx在src中首次出现的位置
	 * @param src   
	 * @param regx  
	 * @param idx   有效开始位置
	 * @return
	 * @throws Exception
	 */
	public static int indexOf(String src, String regx, int begin){
		int idx = -1;
		try{
			PatternCompiler patternCompiler = new Perl5Compiler();
			Pattern pattern = patternCompiler.compile(regx, Perl5Compiler.CASE_INSENSITIVE_MASK);
			PatternMatcher matcher = new Perl5Matcher();
			PatternMatcherInput input = new PatternMatcherInput(src);
			
			while(matcher.contains(input, pattern)){
				MatchResult matchResult = matcher.getMatch();
				int tmp = matchResult.beginOffset(0);
				if(tmp >= begin){//匹配位置从begin开始
					idx = tmp;
					break;
				}
			}
		}catch(Exception e){
			log.error("[fetch(String,String) error]"+"[src:"+src+"][regx:"+regx+"]\n"+e);
			e.printStackTrace();
		}
		return idx;
	}
	public static int indexOf(String src, String regx){
		return indexOf(src,regx,0);
	}
	/**
	 * 表达式匹配值长度
	 * @param src
	 * @param regex
	 * @param mode
	 * @return
	 */
	public static List<String> regexpValue(String src, String regex, Regular.MATCH_MODE mode){
		List<String> result = new ArrayList<String>();
		try{
			List<List<String>> rs = fetch(src, regex, mode);
			for(List<String> row:rs){
				result.add(row.get(0));
			}
		}catch(Exception e){
			log.error("[regexpValue error][src:"+src+"][regex:"+regex+"][mode:"+mode+"]\n"+e);
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 清除所有标签
	 * @param src
	 * @return
	 */
	public static String removeAllTag(String src){
		if(null == src){
			return src;
		}
		return src.replaceAll(Regular.PATTERN.HTML_TAG.getCode(), "");
	}
	public static String removeAllHtmlTag(String src){
		return removeAllTag(src);
	}
	/**
	 * 删除 tags之外的标签"<b>"与"</b>"只写一次 "b"
	 * 只删除标签不删除标签体
	 * @param src
	 * @param tags
	 * @return
	 */
	public static String removeTagExcept(String src, String ...tags){
		if(null == src || null == tags || tags.length == 0){
			return src;
		}
		int size = tags.length;
		String reg = "(?i)<(?!(";
		for(int i=0; i<size; i++){
			reg += "(/?\\s?" + tags[i] + "\\b)";
			if(i < size-1){
				reg += "|";
			}
		}
		reg += "))[^>]+>";
		src = src.replaceAll(reg, "");
		return src;
	}
	public static String removeHtmlTagExcept(String src, String ...tags){
		return removeTagExcept(src, tags);
	}
	/**
	 * 只删除标签,不删除标签体
	 * @param src
	 * @param tags
	 * @return
	 */
	public static String removeTag(String src, String ...tags){
		if(null == tags || tags.length==0){
			src = removeAllHtmlTag(src);
		}else{
			for(String tag:tags){
				String reg = "(?i)<"+tag+"[^>]*>.*?|</"+tag+">";
				src = src.replaceAll(reg, "");
			}
		}
		return src;
	}
	public static String removeHtmlTag(String src, String ...tags){
		return removeTag(src, tags);
	}
	/**
	 * 删除标签及标签体
	 * @param result
	 * @param src
	 * @param tags
	 * @return
	 */
	public static String removeTagWithBody(String src, String ...tags){
		if(null == tags || tags.length==0){
			src = removeAllHtmlTag(src);
		}else{
			for(String tag:tags){
				String reg = "(?i)<"+tag+"[^>]*>.*?</"+tag+">";
				src = src.replaceAll(reg, "");
			}
		}
		return src;
	}
	public static String removeHtmlTagWithBody(String src, String ...tags){
		return removeTagWithBody(src, tags);
	}
	public static String removeEmptyTag(String src){
		String reg = "(?i)(<(\\w+)[^<]*?>)\\s*(</\\2>)";
		src = src.replaceAll(reg, "");
		return src;
	}
	public static String removeHtmlEmptyTag(String src){
		return removeEmptyTag(src);
	}
	
	/**
	 * 删除简单标签外的其他标签
	 * @param src
	 * @return
	 */
	public static String removeHtmlTagExceptSimple(String src){
		return removeHtmlTagExcept(src,"br","b","strong","u","i","pre","ul","li","p");
	}
	public static List<String> fetchUrls(String src) throws Exception{
		List<String> urls = null;
		urls = fetch(src, Regular.PATTERN.HTML_TAG_A.getCode(), Regular.MATCH_MODE.CONTAIN, 4);
		return urls;
	}
	public static String fetchUrl(String src) throws Exception{
		List<String> urls = fetchUrls(src);
		if(null != urls && urls.size()>0){
			return urls.get(0);
		}
		return null;
	}
	/**
	 * 依次取出p,table,div中的内容 有嵌套时只取外层
	 * 0:全文 1:开始标签 2:标签name 3:标签体 4:结束标签 
	 * @param text
	 * @param tags
	 * @return
	 */
	public static List<List<String>> fetchTag(String txt,String ... tags) throws Exception{
		List<List<String>> result = new ArrayList<List<String>>();
		if(null != tags && tags.length>0){
			String regx = "(?i)(<(";
			int size = tags.length;
			for(int i=0; i<size; i++){
				if(i==0){
					regx += tags[i];
				}else{
					regx += "|"+tags[i];
				}
			}
			regx +=")[^<]*?>)([\\s\\S]*?)(</\\2>)";
			result = fetch(txt, regx);
		}
		return result;
	}
	/**
	 * 取出所有属性值
	 * 0全文  1:属性name 2:引号('|") 3:属性值
	 * fetchAttributeValues(txt,"id");
	 * @param txt
	 * @param tags
	 * @return
	 * @throws Exception
	 */
	public static List<List<String>> fetchAttributeList(String txt, String tag){
		List<List<String>> result = new ArrayList<List<String>>();
		try{
			String regx = "(?i)(" + tag + ")\\s*=\\s*(['\"])([\\s\\S]*?)\\2";
			result = fetch(txt, regx);
		}catch(Exception e){
			log.error("提取属性异常");
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 取出所有属性值
	 * 0全文  1:属性name 2:引号('|") 3:属性值
	 * fetchAttributeValues(txt,"id","name");
	 * @param txt
	 * @param tags
	 * @return
	 * @throws Exception
	 */
	public static List<String> fetchAttribute(String txt, String tag){
		List<String> result = new ArrayList<String>();
		List<List<String>> list = fetchAttributeList(txt, tag);
		if(list.size() >0){
			result = list.get(0);
		}
		return result;
	}
	public static List<String> fetchAttributeValues(String txt, String tag){
		List<String> result = new ArrayList<String>();
		List<List<String>> list = fetchAttributeList(txt, tag);
		for(List<String> attr: list){
			if(attr.size() > 3 ){
				result.add(attr.get(3));
			}
		}
		return result;
	}
	public static String fetchAttributeValue(String txt, String tag){
		List<String> values = fetchAttributeValues(txt, tag);
		if(values.size()>0){
			return values.get(0);
		}else{
			return null;
		}
	}
	/**
	 * 取tags[i-2]与tags[i-1]之间的文本
	 * @param text
	 * @param tags
	 * @return
	 */
	public static String cut(String text,String ... tags){
		if(null == text || null == tags || tags.length < 2){
			/*没有开始结束标志*/
			return null;
		}
		int _fr = -1;	//开始下标
		int _to = -1;	//结束下标
		String frTag = "";
		String toTag = tags[tags.length-1];
		int frLength = 0;
		for(int i=0; i<tags.length-1; i++){
			frTag = tags[i];
			if(frTag.equalsIgnoreCase(TAG_BEGIN)){
				_fr = 0;
				frLength = 0;
			}else{
				if(i>0){
					_fr= text.indexOf(frTag, _fr+tags[i-1].length());
				}else{
					_fr= text.indexOf(frTag, _fr);
				}
				if(_fr == -1){
					return null;
				}
				frLength = frTag.length();
			}
		}
		if(frTag.equalsIgnoreCase(TAG_END)){
			_to = text.length();
		}else{
			_to = text.indexOf(toTag,_fr+frLength);
		}
		if(_to <= _fr) {
			return null;
		}
		return text.substring(_fr+frLength,_to);
	}
	public static List<String> cuts(String text, String ... tags){
		List<String> list = new ArrayList<String>();
		while(true){
			String item = cut(text, tags);
			//if(BasicUtil.isEmpty(item)){
			if(null == item){
				break;
			}else{
				list.add(item);
				int idx = 0;
				//计算新起点 
				for(int i=0; i<tags.length; i++){
					if(idx>0){
						idx += 1;
					}
					idx = text.indexOf(tags[i], idx);
				}
				if(idx <= 0){
					break;
				}
				text = text.substring(idx);
			}
		}
		return list;
	}
	public static boolean isDate(String str){
		if(BasicUtil.isEmpty(str)){
			return false;
		}
		str = str.replace("/", "-");
		return regularMatch.match(str, Regular.PATTERN.DATE.getCode());
	}
	public static boolean isDateTime(String str){
		if(BasicUtil.isEmpty(str)){
			return false;
		}
		str = str.replace("/", "-");
		return regularMatch.match(str, Regular.PATTERN.DATE_TIME.getCode());
	}
}
