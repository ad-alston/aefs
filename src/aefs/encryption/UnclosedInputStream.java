package aefs.encryption;

import java.io.IOException;
import java.io.InputStream;

public class UnclosedInputStream extends InputStream{
	private InputStream stream;
	
	public UnclosedInputStream(InputStream stream){
		this.stream = stream;
	}
	
	public int read() throws IOException{
		return stream.read();
	}
	
	public int read(byte[] bytes) throws IOException{
		return stream.read(bytes);
	}
	
	public void close(){
		return;
	}
}
