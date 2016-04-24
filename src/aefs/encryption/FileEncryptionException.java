package aefs.encryption;

public class FileEncryptionException extends Exception {
	public FileEncryptionException(Exception e){ super(e); }
	public FileEncryptionException(String msg){ super(msg); }
}
