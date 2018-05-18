package org.anyline.config.db;

import java.util.List;

import org.anyline.config.db.impl.SQLVariableImpl;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class SQLHelper {

	public static void main(String args[]){
		try {
			String reg  =  SQL.SQL_PARAM_VAIRABLE_REGEX;
			String text = "SELECT * FROM TAB WHERE ID=:ID "
					+ "AND TYPE IN(:TYPE) "
					+ "AND SORT = ::SORT "
					+ "AND NM LIKE ':NM%' "
					+ "AND CODE LIEK '%' + :CODE + '%' "
					+ "AND CODE LIEK '%' + ::CODE + '%'";
			List<List<String>> keys = RegularUtil.fetch(text, reg, RegularUtil.MATCH_MODE_CONTAIN);
			for(List<String> ks:keys){
				int i = 0;
				for(String k:ks){
					System.out.print(i+++".["+k+"]\t\t");
				}
				System.out.println("");
			}
			System.out.println("======================");
			reg  = SQL.SQL_PARAM_VAIRABLE_REGEX_EL;
			text = "SELECT * FROM TAB WHERE ID=${ID} "
					+ "AND TYPE IN(${TYPE}) "
					+ "AND SORT = '${SORT}' "
					+ "AND NM LIKE '%${NM}%' "
					+ "AND CODE LIKE CONTAT('%', ${CODE},'%')";
			keys = RegularUtil.fetch(text, reg, RegularUtil.MATCH_MODE_CONTAIN);
			for(List<String> ks:keys){
				int i = 0;
				for(String k:ks){
					System.out.print(i+++".["+k+"]\t\t");
				}
				System.out.println("");
			}
			System.out.println("----------------------");
			reg  = SQL.SQL_PARAM_VAIRABLE_REGEX_EL;
			text = "SELECT * FROM TAB WHERE ID={ID} "
					+ "AND TYPE IN ({TYPE}) "
					+ "AND SORT = '{SORT}' "
					+ "AND NM LIKE '%{NM}%' "
					+ "AND NM LIKE '%{NM}' "
					+ "AND CODE LIKE CONTAT('%', {CODE},'%')";
			keys = RegularUtil.fetch(text, reg, RegularUtil.MATCH_MODE_CONTAIN);
			for(List<String> ks:keys){
				int i = 0;
				for(String k:ks){
					System.out.print(i+++".["+k+"]\t\t");
				}
				System.out.println("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	/**
0.[ID=:ID ]				1.[ID=]				2.[:ID]			3.[ ]		
0.[IN(:TYPE)]			1.[IN(]				2.[:TYPE]		3.[)]		
0.[= ::SORT ]			1.[=]				2.[::SORT]		3.[ ]		
0.[':NM%]				1.[']				2.[:NM]			3.[%]		
0.[+ :CODE ]			1.[+]				2.[:CODE]		3.[ ]		
0.[+ ::CODE ]			1.[+]				2.[::CODE]		3.[ ]		
======================
0.[ID=${ID} ]			1.[ID=]				2.[${ID}]		3.[ ]		
0.[IN(${TYPE})]			1.[IN(]				2.[${TYPE}]		3.[)]		
0.['${SORT}']			1.[']				2.[${SORT}]		3.[']		
0.['%${NM}%]			1.['%]				2.[${NM}]		3.[%]		
0.[CONTAT('%',{CODE}]	1.[CONTAT('%',]		2.[{CODE}]		3.[null]		

	   @param type 1:已:区分 2:已{}区分	
	 * @param all
	 * @param prefix
	 * @param fullKey
	 * @param afterChar
	 * @return
	 */
	public static SQLVariable buildVariable(int signType, String all, String prefix, String fullKey, String afterChar){
		int varType = -1;
		int compare = SQL.COMPARE_TYPE_EQUAL;
		if(null == afterChar){
			afterChar = "";
		}
		SQLVariable var = new SQLVariableImpl();
		String key = null;
		if(signType ==1){
			key = fullKey.replace(":", "");
		}else if(signType ==2){
			key = fullKey.replace("$", "").replace("{", "").replace("}", "");
		}
		
		if(fullKey.startsWith("$") || fullKey.startsWith("::")){
			// AND CD = ${CD} 
			// AND CD = ::CD
			varType = SQLVariable.VAR_TYPE_REPLACE;
		}else if("'".equals(afterChar)){
			// AND CD = '{CD}'
			// AND CD = ':CD'
			varType = SQLVariable.VAR_TYPE_KEY_REPLACE;
		}else if(prefix.endsWith("%") || afterChar.startsWith("%")){
			//AND CD LIKE '%{CD}%'
			//AND CD LIKE '%:CD%'
			varType = SQLVariable.VAR_TYPE_KEY;
			if(prefix.endsWith("%") && afterChar.startsWith("%")){
				compare = SQL.COMPARE_TYPE_LIKE;
			}else if(prefix.endsWith("%")){
				compare = SQL.COMPARE_TYPE_LIKE_PREFIX;
			}else if(afterChar.startsWith("%")){
				compare = SQL.COMPARE_TYPE_LIKE_SUBFIX;
			}
		}else{
			varType = SQLVariable.VAR_TYPE_KEY;
			if(prefix.equalsIgnoreCase("IN") || prefix.equalsIgnoreCase("IN(")){
				//AND CD IN({CD})
				compare = SQL.COMPARE_TYPE_IN;
			}
		}
		var.setSignType(signType);
		var.setKey(key);
		var.setType(varType);
		var.setCompare(compare);
		return var;
	}
}