package di.vdrchman.controller;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

import di.vdrchman.data.FilesManager;

@Model
public class FilesBacking {

	@Inject
	private FilesManager filesManager;

	// Add uploaded file to the list of scanned channel files
	public void scanFileUploadListener(FileUploadEvent event) {
		UploadedFile file;

		file = event.getUploadedFile();
		filesManager.addScan(file.getName(), file.getData());
	}

}
