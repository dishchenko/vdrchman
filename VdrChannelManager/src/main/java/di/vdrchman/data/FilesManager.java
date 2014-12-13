package di.vdrchman.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@SessionScoped
@Named
public class FilesManager implements Serializable {

	private static final long serialVersionUID = 8655706926659652902L;

	// The list of scans (scanned channel file datas)
	private List<Scan> scans = new ArrayList<Scan>();

	public void addScan(String fileName, byte[] data) {
		Scan scan;

		scan = new Scan();

		scan.setFileName(fileName);
		scan.setData(new byte[data.length]);
		System.arraycopy(data, 0, scan.getData(), 0, data.length);

		scans.add(scan);
	}

	// Clear the list of scans
	public void clearScans() {
		scans.clear();
	}

	public List<Scan> getScans() {

		return scans;
	}

}
