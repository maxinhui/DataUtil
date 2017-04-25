package util;

import java.util.Properties;

public class PackageBaseUtil {

	
    public static void main(String[] args) {
    	Properties prop = PropertiesUtil.loadProperties("/config.properties"); 
    	String packageBase = prop.getProperty("packageBase").trim();
       
        packageSplit(packageBase,"user");
	}
    
    public static String packageSplit(String packageBase,String tableName){
    	String result = "delete";
    	if(null!=packageBase&&!"".equals(packageBase)){
    		String[] str0 = packageBase.split("/");
    		KO:
    		for(int i=0;i<str0.length;i++){
    			String[] str1 = str0[i].split(":");//str1[0]为统一文件路径   str1[1]为表名
    			String[] str2 = str1[1].split(",");//表名
    			for(int l=0;l<str2.length;l++){
    				if(tableName.equals(str2[l])){
    					result = str1[0];
    					break KO;
    				}
    			}
    		}
    		return result;
    	}else{    		
    		System.out.print("文件统一存放路径为空，无法生成");
    		return result;
    	}
    	
    	
    }
}
