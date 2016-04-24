package aefs.encryption;

import java.io.IOException;
import java.io.OutputStream;

public class UnclosedOutputStream extends OutputStream{
	private OutputStream stream;
	
	public UnclosedOutputStream(OutputStream stream){
		this.stream = stream;
	}
	
	public void write(int v) throws IOException{
		stream.write(v);
	}
	
	public void write(byte[] bytes) throws IOException{
		stream.write(bytes);
	}
	
	public void close(){
		return;
	}
}
