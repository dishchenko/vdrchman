package di.vdrchman.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.richfaces.model.UploadedFile;

@SessionScoped
@Named
public class FilesManager implements Serializable {

	private static final long serialVersionUID = 8655706926659652902L;

	// The list of scanned channels files
	private List<UploadedFile> scanFiles = new ArrayList<UploadedFile>();

	public List<UploadedFile> getScanFiles() {

		return scanFiles;
	}

}
