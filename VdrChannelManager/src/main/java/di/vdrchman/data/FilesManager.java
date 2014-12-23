package di.vdrchman.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

@SessionScoped
@Named
public class FilesManager implements Serializable {

	private static final long serialVersionUID = 8655706926659652902L;

	// The list of scans (scanned channel file datas)
	private List<Scan> scans = new ArrayList<Scan>();

	// Add uploaded scan to the list of scans
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

	// Download the data from string 'content' to the browser as text file with
	// name 'fileName'
	public void download(String fileName, String content) throws IOException {
		FacesContext facesContext;
		HttpServletResponse response;
		BufferedInputStream bis;
		BufferedOutputStream bos;
		byte[] buffer;

		facesContext = FacesContext.getCurrentInstance();
		response = (HttpServletResponse) facesContext.getExternalContext()
				.getResponse();

		response.reset();
		response.setContentType("text/plain");
		response.setHeader("Content-disposition", "attachment; filename=\""
				+ fileName + "\"");

		bis = null;
		bos = null;

		try {
			bis = new BufferedInputStream(new ByteArrayInputStream(
					content.getBytes("UTF-8")));
			bos = new BufferedOutputStream(response.getOutputStream());

			buffer = new byte[2048];
			for (int length; (length = bis.read(buffer)) > 0;) {
				bos.write(buffer, 0, length);
			}
		} finally {
			if (bos != null) {
				bos.close();
			}
			if (bis != null) {
				bis.close();
			}

			facesContext.responseComplete();
		}
	}

	public List<Scan> getScans() {

		return scans;
	}

}
