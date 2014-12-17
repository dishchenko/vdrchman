package di.vdrchman.data;

public class Scan {

	private String fileName;
	private byte[] data;

	public String buildSourceName() {
		return fileName.substring(0, fileName.length() - 8);
	}

	public String getFileName() {

		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getData() {

		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
