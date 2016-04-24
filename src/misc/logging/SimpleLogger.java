package misc.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleLogger {

	private static String source = "SimpleLogger";
	
	private static void log(String signifier, String message){
		String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		System.out.println(ts+" ["+source+":"+signifier+"] "+message);
	}
	
	public static void error(String message){
		log("ERROR", message);
	}
	
	public static void info(String message){
		log("INFO", message);
	}
	
	public static void setSource(String source){
		SimpleLogger.source = source;
	}
	
}
