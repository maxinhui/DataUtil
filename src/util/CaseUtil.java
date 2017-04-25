package util;

public class CaseUtil {

	  //驼峰首字母小写
	  public static String upperCase(String str){
		  String[] str1=str.split("_");
		  String res="";
		  if(str1.length>0){
			  res=str1[0];
			  for(int i=1;i<str1.length;i++){
				  if(Character.isUpperCase(str1[i].charAt(0)))
			           res+= str1[i];
			        else
			            res+= (new StringBuilder()).append(Character.toUpperCase(str1[i].charAt(0))).append(str1[i].substring(1)).toString();
			  }
		  }
		  return res;
	  }
	//驼峰首字母大写
	  public static String upperCaseA(String str){
		 
		  String[] str1=str.split("_");
		  String res="";
		  if(str1.length>0){
			  res=(new StringBuilder()).append(Character.toUpperCase(str1[0].charAt(0))).append(str1[0].substring(1)).toString();;
			  for(int i=1;i<str1.length;i++){
				  if(Character.isUpperCase(str1[i].charAt(0)))
			           res+= str1[i];
			        else
			            res+= (new StringBuilder()).append(Character.toUpperCase(str1[i].charAt(0))).append(str1[i].substring(1)).toString();
			  }
		  }
		  return res;
	  }
	  
}
