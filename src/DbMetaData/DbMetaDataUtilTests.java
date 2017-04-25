package DbMetaData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import util.CaseUtil;
import util.PackageBaseUtil;
import util.PropertiesUtil;

public class DbMetaDataUtilTests {
	
	
//全部表
  public static void main(String[] args) throws SQLException, ClassNotFoundException {
	Properties prop = PropertiesUtil.loadProperties("/config.properties"); 
	String user = prop.getProperty("user").trim();
	String password = prop.getProperty("password").trim();
	String jdbcDriver = prop.getProperty("jdbcDriver").trim();
	String jdbcUrl = prop.getProperty("jdbcUrl").trim();
	DbMetaDataUtilTest.baseWebsiteUrl = prop.getProperty("baseWebsiteUrl").trim();
	DbMetaDataUtilTest.baseBusinessUrl = prop.getProperty("baseBusinessUrl").trim();
	DbMetaDataUtilTest.htmlUrl = prop.getProperty("htmlUrl").trim();
	DbMetaDataUtilTest.businessUrl = prop.getProperty("businessUrl").trim();
	DbMetaDataUtilTest.websiteUrl = prop.getProperty("websiteUrl").trim();
    Connection conn = null;
    Class.forName(jdbcDriver);
    conn = DriverManager.getConnection(jdbcUrl, user, password);
    //获取表名
    ResultSet rs =  conn.getMetaData().getTables("","","",null);

 
    List<Map<String,Object>> listMap=new ArrayList<Map<String,Object>>();
    List<Map<String,Object>> listX=null;
    while(rs.next()) {
     //创建对象map
    Map<String,Object> beanMap=new HashMap<String, Object>();
    
    String tableName=rs.getString("TABLE_NAME");//表名
    //主键  
    ResultSet ids = conn.getMetaData().getPrimaryKeys("", "",tableName);
    String columnName = "";
    while(ids != null && ids.next()){     
    	columnName = ids.getString("COLUMN_NAME");//主键名
        beanMap.put("pkName", ids.getString("PK_NAME"));//主键key
    }  
    String baseUrl = PackageBaseUtil.packageSplit(prop.getProperty("packageBase").trim(), tableName);
    if(baseUrl.equals("delete")){
    	continue;
    }
    beanMap.put("packageUrl", baseUrl);  
    System.out.print(beanMap.get("packageUrl"));
    beanMap.put("className",CaseUtil.upperCaseA(tableName)); 
    beanMap.put("tableName", CaseUtil.upperCase(tableName));
    beanMap.put("name", tableName);
    String baseColumn = "";
    String baseValueColumn = "";
    Integer baseSize = 1;
    listX=new ArrayList<Map<String,Object>>();
	    PreparedStatement pst = null;
	    try {
	      pst = conn.prepareStatement("select * from "+tableName+" where 1=2");
	      ResultSetMetaData rsd = pst.executeQuery().getMetaData();
	      for(int i = 0; i < rsd.getColumnCount(); i++) {	 
	    	Map<String,Object> map=new HashMap<String, Object>();
//	        System.out.print("  java类型："+rsd.getColumnClassName(i + 1));//*
//	        System.out.print("  数据库类型:"+rsd.getColumnTypeName(i + 1));//*
//	        System.out.print("  字段名称:"+rsd.getColumnName(i + 1));//*
//	        System.out.print("  字段长度:"+rsd.getColumnDisplaySize(i + 1));//*
	        ResultSet bean = conn.getMetaData().getColumns("","",tableName,rsd.getColumnName(i + 1) );  
	        String[] remark = null;
    	    while(bean != null && bean.next()){  
    	           remark = bean.getString("REMARKS").split("_");
    	           map.put("remark",remark[0]);//注釋 
    	           if(remark.length ==2 ){
    	        	map.put("remarkType", remark[1]); 
    	           }else{
    	        	map.put("remarkType", "");
    	           }
    	    }  
	        
    	    map.put("proType", rsd.getColumnClassName(i + 1));//java类型
	        map.put("jdbcType", rsd.getColumnTypeName(i+1));//数据库类型
	        map.put("proName", CaseUtil.upperCase(rsd.getColumnName(i + 1)));//字段名称
	        map.put("jdbcName", rsd.getColumnName(i + 1));//数据库原始字段名

	        listX.add(map);
	        if(columnName.equals(rsd.getColumnName(i + 1))){
	        	beanMap.put("pkJavaType",rsd.getColumnClassName(i + 1));
	        	beanMap.put("pkJdbcType",rsd.getColumnTypeName(i+1));
	        	beanMap.put("columnName", CaseUtil.upperCase(columnName));
	        	beanMap.put("proName", rsd.getColumnName(i + 1));//数据库原始字段名
	        }
	        
	        if(baseSize == 6){
	        	   baseColumn = baseColumn + rsd.getColumnName(i + 1) + ",;";
	        	   baseValueColumn = baseValueColumn + " #{" +(String) map.get("proName") + ",jdbcType=" + map.get("jdbcType") + "},;";
	        	   baseSize = 1;
	           }else{
	        	   baseColumn = baseColumn + rsd.getColumnName(i + 1) +",";  
	        	   baseValueColumn = baseValueColumn + " #{" +(String) map.get("proName") + ",jdbcType=" + map.get("jdbcType") + "},";
	        	   baseSize ++;
	           }
	        	        
	      }
	    } catch(SQLException e) {
	      throw new RuntimeException(e);
	    } finally {
		      try {
		        pst.close();
		        pst = null;
		      } catch(SQLException e) {
		        throw new RuntimeException(e);
		      }
	    }
	    beanMap.put("le", "$");
	    beanMap.put("lg", "#");
	    beanMap.put("baseColumn", baseColumn.substring(0, baseColumn.length()-1));
	    beanMap.put("baseValueColumn", baseValueColumn.substring(0, baseValueColumn.length()-1));

	    
	    beanMap.put("properties", listX);
	    listMap.add(beanMap);
	    
    } 
    
    DbMetaDataUtilTest.forList(listMap);
    DbMetaDataUtilTest.forJavaService(listMap);
    DbMetaDataUtilTest.forJavaController(listMap);
    DbMetaDataUtilTest.forBean(listMap);
    DbMetaDataUtilTest.forJavaMapper(listMap);

  }


}