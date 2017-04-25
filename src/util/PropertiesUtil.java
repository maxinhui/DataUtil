package util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;



public class PropertiesUtil {
	public static Properties loadProperties(String filePath) {
		Properties prop = new Properties(); 
		InputStream in = Object.class.getResourceAsStream(filePath); 
		try {
			
			prop.load(in);  
		} catch (FileNotFoundException e) {
			System.out.print("配置文件《"+filePath+"》没找到！");
		} catch (IOException e) {
			System.out.print("读取配置文件《"+filePath+"》");
		}
		return prop;
	}
	

}
