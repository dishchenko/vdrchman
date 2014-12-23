package di.vdrchman.controller;

import java.io.IOException;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

import di.vdrchman.data.FilesManager;
import di.vdrchman.data.SourcesManager;
import di.vdrchman.data.TranspondersManager;
import di.vdrchman.model.Source;

@Model
public class FilesBacking {

	@Inject
	private FilesManager filesManager;

	@Inject
	private SourcesManager sourcesManager;

	@Inject
	private TranspondersManager transpondersManager;

	// Add uploaded file to the list of scanned channel files
	public void scanFileUploadListener(FileUploadEvent event) {
		UploadedFile file;

		file = event.getUploadedFile();
		filesManager.addScan(file.getName(), file.getData());
	}

	// Build sources.conf data and download it as a file to the browser
	public void downloadSourcesConf() throws IOException {
		filesManager
				.download("sources.conf", sourcesManager.buildSourcesConf());
	}

	// Build diseqc.conf data and download it as a file to the browser
	public void downloadDiseqcConf() throws IOException {
		filesManager.download("diseqc.conf", sourcesManager.buildDiseqcConf());
	}

	// Build rotor.conf data and download it as a file to the browser
	public void downloadRotorConf() throws IOException {
		filesManager.download("rotor.conf", sourcesManager.buildRotorConf());
	}

	// Build rotorng.conf data and download it as a file to the browser
	public void downloadRotorngConf() throws IOException {
		filesManager
				.download("rotorng.conf", sourcesManager.buildRotorngConf());
	}

	// Build <sourceName>.freq data and download it as a file to the browser
	public void downloadFreq(Source source) throws IOException {
		filesManager.download(source.getName() + ".freq",
				transpondersManager.buildFreq(source));
	}

}
