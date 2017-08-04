package DbMetaData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class DbMetaDataUtilTest {
	
	public static String baseBusinessUrl;//项目路径
	public static String baseWebsiteUrl;//项目路径		
	public static String htmlUrl;//路径
	public static String businessUrl;
	public static String websiteUrl;
	
	
  //单表
  public static void main(String[] args) throws SQLException, ClassNotFoundException {
	Properties prop = PropertiesUtil.loadProperties("/config.properties"); 
    String user = prop.getProperty("user").trim();
    String password = prop.getProperty("password").trim();
    String jdbcDriver = prop.getProperty("jdbcDriver").trim();
    String jdbcUrl = prop.getProperty("jdbcUrl").trim();
    baseWebsiteUrl = prop.getProperty("baseWebsiteUrl").trim();
    baseBusinessUrl = prop.getProperty("baseBusinessUrl").trim();
    htmlUrl = prop.getProperty("htmlUrl").trim();
    businessUrl = prop.getProperty("businessUrl").trim();
    websiteUrl = prop.getProperty("websiteUrl").trim();
    Connection conn = null;
    Class.forName(jdbcDriver);
    conn = DriverManager.getConnection(jdbcUrl, user, password);

 
    List<Map<String,Object>> listMap=new ArrayList<Map<String,Object>>();
    List<Map<String,Object>> listX=null;
    
     //创建对象map
    Map<String,Object> beanMap=new HashMap<String, Object>();
   
    String tableName = prop.getProperty("tableName").trim();//需要的表名
  //主键  
    ResultSet ids = conn.getMetaData().getPrimaryKeys("", "",tableName);
    String columnName = "";
    while(ids != null && ids.next()){   
        columnName = ids.getString("COLUMN_NAME");//主键名
        beanMap.put("pkName", ids.getString("PK_NAME"));//主键key
    }  
    beanMap.put("packageUrl", PackageBaseUtil.packageSplit(prop.getProperty("packageBase").trim(), tableName));       

    beanMap.put("className",CaseUtil.upperCaseA(tableName)); 
    beanMap.put("tableName", CaseUtil.upperCase(tableName));
    beanMap.put("name",tableName);
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
	        System.out.print("  java类型："+rsd.getColumnClassName(i + 1));
	        System.out.print("  数据库类型:"+rsd.getColumnTypeName(i + 1));
//	        System.out.print("  字段名称:"+rsd.getColumnName(i + 1));
//	        System.out.print("  字段长度:"+rsd.getColumnDisplaySize(i + 1));
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
	        //主键字段赋值
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
	    
   
    
    forList(listMap);
    forJavaService(listMap);
    forJavaController(listMap);
    forBean(listMap);
    forJavaMapper(listMap);
  }

	//html
	public static void forList(List<Map<String,Object>> listMap){
	  Configuration cfg = new  Configuration();  
	  try  {  
	      cfg.setClassForTemplateLoading(DbMetaDataUtilTest.class ,  "../html" ); //指定模板所在的classpath目录   
	      cfg.setSharedVariable("upperFC" ,  new  UpperFirstCharacter()); //添加一个"宏"共享变量用来将属性名首字母大写   
	      Template t1 = cfg.getTemplate("list.html"); //指定模板List
	      Template t2 = cfg.getTemplate("edit.html"); //制定模板Edit
	      Template t3 = cfg.getTemplate("add.html"); //制定模板Add
	      FileOutputStream fos1 =null;
	      FileOutputStream fos2 =null;
	      FileOutputStream fos3 =null;
	      
	      for(int i=0;i<listMap.size();i++){
	    	  Map<String,Object> beanMap=listMap.get(i);
	          File file = new File(baseWebsiteUrl+"src/main/webapp/WEB-INF/"+htmlUrl+beanMap.get("packageUrl"));
		  		// 判断是否有词路径.没有新建
		  		if (!file.exists()) {
		  			try {
		  				file.mkdirs();
		  			} catch (Exception e) {
		  				e.printStackTrace();
		  			}
		  		}	  		     
		 
			    fos1=new  FileOutputStream( new  File( baseWebsiteUrl+"src/main/webapp/WEB-INF/"+htmlUrl+beanMap.get("packageUrl")+"/"+CaseUtil.upperCase((String)beanMap.get("tableName"))+"List.html")); //java文件的生成目录
			    fos2=new  FileOutputStream( new  File( baseWebsiteUrl+"src/main/webapp/WEB-INF/"+htmlUrl+beanMap.get("packageUrl")+"/"+CaseUtil.upperCase((String)beanMap.get("tableName"))+"Edit.html")); //java文件的生成目录
			    fos3=new  FileOutputStream( new  File( baseWebsiteUrl+"src/main/webapp/WEB-INF/"+htmlUrl+beanMap.get("packageUrl")+"/"+CaseUtil.upperCase((String)beanMap.get("tableName"))+"Add.html")); //java文件的生成目录   
	
		        t1.process(beanMap, new  OutputStreamWriter(fos1, "utf-8" )); //list
		        t2.process(beanMap, new  OutputStreamWriter(fos2, "utf-8" )); //edit
		        t3.process(beanMap, new  OutputStreamWriter(fos3, "utf-8" )); //edit
		        fos1.flush();  
		        fos1.close();
		        fos2.flush();
		        fos2.close();
		        fos3.flush();
		        fos3.close();
	      }
	  } catch  (IOException e) {  
	      e.printStackTrace();  
	  } catch  (TemplateException e) {  
	      e.printStackTrace();  
	  } 
	   
	 }
	
	//entity
	public static void forBean(List<Map<String,Object>> listMap){
	  Configuration cfg = new  Configuration();  
	  try  {  
	      cfg.setClassForTemplateLoading(DbMetaDataUtilTest.class ,  "../entity" ); //指定模板所在的classpath目录   
	      cfg.setSharedVariable("upperFC" ,  new  UpperFirstCharacter()); //添加一个"宏"共享变量用来将属性名首字母大写   
	      Template t1 = cfg.getTemplate("javaBeanSpringMvc.html"); //指定模板
	      Template t2 = cfg.getTemplate("javaBeanDTOSpringMvc.html"); //制定模板
	      FileOutputStream fos1 =null;
	      FileOutputStream fos2 =null;
	      
	      for(int i=0;i<listMap.size();i++){
	    	  Map<String,Object> beanMap=listMap.get(i);
	          File file =  new File(baseBusinessUrl+"src/main/java/"+businessUrl+beanMap.get("packageUrl")+"/entity");
	          File file1 = new File(baseBusinessUrl+"src/main/java/"+businessUrl+beanMap.get("packageUrl")+"/dto");
		  		// 判断是否有词路径.没有新建
		  		if (!file.exists()) {
		  			try {
		  				file.mkdirs();
		  				file1.mkdirs();
		  			} catch (Exception e) {
		  				e.printStackTrace();
		  			}
		  		}	  		     
		        beanMap.put("package", businessUrl.replace("/", ".")+beanMap.get("packageUrl"));
		   	        
			    fos1=new  FileOutputStream( new  File( baseBusinessUrl+"src/main/java/"+businessUrl+beanMap.get("packageUrl")+"/entity/"+CaseUtil.upperCaseA((String)beanMap.get("tableName"))+".java")); //java文件的生成目录
			    fos2=new  FileOutputStream( new  File( baseBusinessUrl+"src/main/java/"+businessUrl+beanMap.get("packageUrl")+"/dto/"+CaseUtil.upperCaseA((String)beanMap.get("tableName"))+"DTO.java")); //java文件的生成目录   
	
		        t1.process(beanMap, new  OutputStreamWriter(fos1, "utf-8" )); //list
		        t2.process(beanMap, new  OutputStreamWriter(fos2, "utf-8" )); //edit
		        fos1.flush();  
		        fos1.close();
		        fos2.flush();
		        fos2.close();
	      }
	  } catch  (IOException e) {  
	      e.printStackTrace();  
	  } catch  (TemplateException e) {  
	      e.printStackTrace();  
	  } 
	   
	 }
	
	
	//controller
	public static void forJavaController(List<Map<String,Object>> listMap){
		Configuration cfg = new  Configuration();  
		try  {  
		    cfg.setClassForTemplateLoading(DbMetaDataUtilTests.class , "../controller" ); //指定模板所在的classpath目录   
		    cfg.setSharedVariable("upperFC" ,  new  UpperFirstCharacter()); //添加一个"宏"共享变量用来将属性名首字母大写   
		    Template t = cfg.getTemplate("javaControllerSpringMvc.html" ); //指定模板   
		    FileOutputStream fos =null;
		    for(int i=0;i<listMap.size();i++){
			  	Map<String,Object> beanMap=listMap.get(i);
			  	
			    File file = new File(baseWebsiteUrl+"src/main/java/"+websiteUrl+beanMap.get("packageUrl")+"/controller");
			 		// 判断是否有词路径.没有新建
			 		if (!file.exists()) {
			 			try {
			 				file.mkdirs();
			 			} catch (Exception e) {
			 				e.printStackTrace();
			 			}
			 		}
			 		
			 		beanMap.put("package" , websiteUrl.replace("/", ".")+beanMap.get("packageUrl")); //controller包名   
			 		beanMap.put("packageBusiness",businessUrl.replace("/", ".")+beanMap.get("packageUrl"));//service包名
			              		     
			      fos=new  FileOutputStream( new  File( baseWebsiteUrl+"src/main/java/"+websiteUrl+beanMap.get("packageUrl")+"/controller/"+CaseUtil.upperCaseA((String)beanMap.get("tableName"))+"Controller.java")); //java文件的生成目录 
			      t.process(beanMap, new  OutputStreamWriter(fos, "utf-8" )); //  
			      fos.flush();  
			      fos.close(); 
			 }
		} catch  (IOException e) {  
		    e.printStackTrace();  
		} catch  (TemplateException e) {  
		    e.printStackTrace();  
		} 
	
	} 
	  
	//service创建
	public static void forJavaService(List<Map<String,Object>> listMap){
		Configuration cfg = new  Configuration();  
		try  {  
		    cfg.setClassForTemplateLoading(DbMetaDataUtilTests.class , "../service" ); //指定模板所在的classpath目录   
		    cfg.setSharedVariable("upperFC" ,  new  UpperFirstCharacter()); //添加一个"宏"共享变量用来将属性名首字母大写   
		    Template t = cfg.getTemplate("javaServiceSpringMvc.html" ); //指定模板   
		    Template t1 = cfg.getTemplate("javaServiceSpringImplMvc.html" ); //指定模板
		    FileOutputStream fos =null;
		    FileOutputStream fos1 =null; 
		    for(int i=0;i<listMap.size();i++){
			  	Map<String,Object> beanMap=listMap.get(i);
			  	
			    File file = new File(baseBusinessUrl+"src/main/java/"+businessUrl+beanMap.get("packageUrl")+"/service/impl");
			 		// 判断是否有词路径.没有新建
			 		if (!file.exists()) {
			 			try {
			 				file.mkdirs();
			 			} catch (Exception e) {
			 				e.printStackTrace();
			 			}
			 		} 	
			      	    		         
			 		beanMap.put("package" , businessUrl.replace("/", ".")+beanMap.get("packageUrl")); //service包名        
			      
			      fos=new  FileOutputStream( new  File( baseBusinessUrl+"src/main/java/"+businessUrl+beanMap.get("packageUrl")+"/service/"+CaseUtil.upperCaseA((String)beanMap.get("tableName"))+"Service.java")); //java文件的生成目录 
			      fos1=new FileOutputStream( new  File( baseBusinessUrl+"src/main/java/"+businessUrl+beanMap.get("packageUrl")+"/service/impl/"+CaseUtil.upperCaseA((String)beanMap.get("tableName"))+"ServiceImpl.java")); //java文件的生成目录    
			      t.process(beanMap, new  OutputStreamWriter(fos, "utf-8" )); //  
			      t1.process(beanMap, new  OutputStreamWriter(fos1, "utf-8" )); // 
			      fos.flush();  
			      fos.close(); 
			      fos1.flush();  
			      fos1.close(); 
		    }
		} catch  (IOException e) {  
		    e.printStackTrace();  
		} catch  (TemplateException e) {  
		    e.printStackTrace();  
		} 
	
	}


	//mapper创建
	public static void forJavaMapper(List<Map<String,Object>> listMap){
		Configuration cfg = new  Configuration();  
		try  {  
		    cfg.setClassForTemplateLoading(DbMetaDataUtilTests.class , "../repository" ); //指定模板所在的classpath目录   
		    cfg.setSharedVariable("upperFC" ,  new  UpperFirstCharacter()); //添加一个"宏"共享变量用来将属性名首字母大写   
		    Template t = cfg.getTemplate("javaMapperSpringMvc.html" ); //指定模板   
		    Template t1 = cfg.getTemplate("javaMapperSpringImplMvc.html" ); //指定模板
		    FileOutputStream fos =null;
		    FileOutputStream fos1 =null; 
		    for(int i=0;i<listMap.size();i++){
			  	Map<String,Object> beanMap=listMap.get(i);
			  	
			    File file = new File(baseBusinessUrl+"src/main/java/"+businessUrl+beanMap.get("packageUrl")+"/repository");
			    File file1 = new File(baseBusinessUrl+"src/main/resources/"+businessUrl+beanMap.get("packageUrl")+"/repository");
			 		// 判断是否有词路径.没有新建
			 		if (!file.exists()) {
			 			try {
			 				file.mkdirs();
			 				
			 			} catch (Exception e) {
			 				e.printStackTrace();
			 			}
			 		}
			 		// 判断是否有词路径.没有新建
			 		if (!file1.exists()) {
			 			try {
			 				file1.mkdirs();
			 				
			 			} catch (Exception e) {
			 				e.printStackTrace();
			 			}
			 		}
		 	
		 	      
		 	      beanMap.put("package", businessUrl.replace("/", ".")+beanMap.get("packageUrl"));
		        
			      fos=new  FileOutputStream( new  File( baseBusinessUrl+"src/main/java/"+businessUrl+beanMap.get("packageUrl")+"/repository/"+CaseUtil.upperCaseA((String)beanMap.get("tableName"))+"Mapper.java")); //java文件的生成目录 
			      fos1=new FileOutputStream( new  File( baseBusinessUrl+"src/main/resources/"+businessUrl+beanMap.get("packageUrl")+"/repository/"+CaseUtil.upperCaseA((String)beanMap.get("tableName"))+"Mapper.xml")); //java文件的生成目录    
			      t.process(beanMap, new  OutputStreamWriter(fos, "utf-8" )); //  
			      t1.process(beanMap, new  OutputStreamWriter(fos1, "utf-8" )); // 
			      fos.flush();  
			      fos.close(); 
			      fos1.flush();  
			      fos1.close(); 
		    }
		} catch  (IOException e) {  
		    e.printStackTrace();  
		} catch  (TemplateException e) {  
		    e.printStackTrace();  
		} 
	
	}
  
}