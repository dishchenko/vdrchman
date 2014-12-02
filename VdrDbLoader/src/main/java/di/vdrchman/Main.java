package di.vdrchman;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

public class Main {

	private ArgsParser ap;
	private EntityManager em;

	public static void main(String[] args) {
		ArgsParser ap;

		ap = new ArgsParser();
		ap.parse(args);

		if (ap.getCommand() == null) {
			System.err.println("Usage: java -jar <JAR> cleanDb <userId>");
			System.err.println("                       loadSources <userId>");
			System.err
					.println("                       loadTransponders <userId> <sourceName>");
			System.err
					.println("                       loadNidsTids <userId> <sourceName|ALL_SOURCES>");
			System.err
					.println("                       loadChannels <userId> <sourceName|ALL_SOURCES>");
			System.err.println("                       loadGroups <userId>");
			System.err
					.println("                       loadChannelGroups <userId> <sourceName|ALL_SOURCES>");
			System.exit(0);
		}

		(new Main(ap)).doMain();
	}

	public Main(ArgsParser ap) {
		this.ap = ap;
	}

	// Setup an environment and process the command parsed
	private void doMain() {
		Properties p;

		try {
			Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);

			p = new Properties(System.getProperties());
			p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
			p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL",
					"SEVERE");
			System.setProperties(p);

			em = PersistenceManager.INSTANCE.getEntityManager();

			em.getTransaction().begin();

			if (ap.getCommand() == Command.CLEAN_DB) {
				cleanDb();
			}
			if (ap.getCommand() == Command.LOAD_SOURCES) {
				loadSources();
			}
			if (ap.getCommand() == Command.LOAD_TRANSPONDERS) {
				loadTransponders();
			}
			if (ap.getCommand() == Command.LOAD_NIDS_TIDS) {
				loadNidsTids();
			}
			if (ap.getCommand() == Command.LOAD_CHANNELS) {
				loadChannels();
			}
			if (ap.getCommand() == Command.LOAD_GROUPS) {
				loadGroups();
			}
			if (ap.getCommand() == Command.LOAD_CHANNEL_GROUPS) {
				loadChannelGroups();
			}

			em.getTransaction().commit();
		}

		catch (Throwable ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}

		finally {
			if (em != null) {
				em.close();
			}
			PersistenceManager.INSTANCE.close();
		}
	}

	// Clean VDR Channel Manager database for the current User
	private void cleanDb() {
		SourceRepository sr;
		GroupRepository gr;

		sr = new SourceRepository(em);
		sr.clean(ap.getUserId());

		gr = new GroupRepository(em);
		gr.clean(ap.getUserId());
	}

	// Load Sources data for the current User from sources.conf,
	// diseqc.conf and rotor.conf files.
	// All files must reside in the directory where the loader is launched
	private void loadSources() throws FileNotFoundException, IOException {
		BufferedReader sourcesConf;
		BufferedReader diseqcConf;
		BufferedReader rotorConf;
		SourceRepository sr;

		sourcesConf = null;
		diseqcConf = null;
		rotorConf = null;

		try {
			sourcesConf = new BufferedReader(new InputStreamReader(
					new FileInputStream("sources.conf"), "UTF-8"));
			diseqcConf = new BufferedReader(new InputStreamReader(
					new FileInputStream("diseqc.conf"), "UTF-8"));
			rotorConf = new BufferedReader(new InputStreamReader(
					new FileInputStream("rotor.conf"), "UTF-8"));

			sr = new SourceRepository(em);
			sr.load(ap.getUserId(), sourcesConf, diseqcConf, rotorConf);
		}

		finally {
			if (sourcesConf != null) {
				sourcesConf.close();
			}
			if (diseqcConf != null) {
				diseqcConf.close();
			}
			if (rotorConf != null) {
				rotorConf.close();
			}
		}
	}

	// Load Transponders data for the Source defined and the current User
	// from <sourceName>.freq file.
	// The file must reside in the directory where the loader is launched
	private void loadTransponders() throws FileNotFoundException, IOException {
		SourceRepository sr;
		Source source;
		BufferedReader sourceFreq;
		TransponderRepository tr;

		sr = new SourceRepository(em);
		source = sr.findByName(ap.getUserId(), ap.getSourceName());

		if (source != null) {
			sourceFreq = null;

			try {
				sourceFreq = new BufferedReader(new InputStreamReader(
						new FileInputStream(ap.getSourceName() + ".freq"),
						"UTF-8"));

				tr = new TransponderRepository(em);
				tr.load(ap.getUserId(), source.getId(), sourceFreq);
			}

			finally {
				if (sourceFreq != null) {
					sourceFreq.close();
				}
			}
		} else {
			System.err.println("Can't find Source named '" + ap.getSourceName()
					+ "'");
		}
	}

	// Load NIDs and TIDs for Transponders from channels.cfg file.
	// The file must reside in the directory where the loader is launched.
	// When a special source name 'ALL_SOURCES' is used, NIDs/TIDs are loaded
	// for all Transponders belonging to the current User.
	// Otherwise only Transponders of the Source defined are processed
	private void loadNidsTids() throws FileNotFoundException, IOException {
		BufferedReader channelsCfg;
		TransponderRepository tr;
		SourceRepository sr;
		Source source;

		channelsCfg = null;

		try {
			channelsCfg = new BufferedReader(new InputStreamReader(
					new FileInputStream("channels.cfg"), "ISO-8859-1"));

			tr = new TransponderRepository(em);
			if (ap.getSourceName().equals("ALL_SOURCES")) {
				tr.loadNidsTids(ap.getUserId(), null, channelsCfg);
			} else {
				sr = new SourceRepository(em);
				source = sr.findByName(ap.getUserId(), ap.getSourceName());

				if (source != null) {
					tr.loadNidsTids(ap.getUserId(), source, channelsCfg);
				} else {
					System.err.println("Can't find Source named '"
							+ ap.getSourceName() + "'");
				}
			}
		}

		finally {
			if (channelsCfg != null) {
				channelsCfg.close();
			}
		}
	}

	// Load Channels for Sources from channels.cfg file.
	// The file must reside in the directory where the loader is launched.
	// When a special source name 'ALL_SOURCES' is used, Channels are loaded
	// for all Sources belonging to the current User.
	// Otherwise only Channels for the Source defined are taken into account
	private void loadChannels() throws FileNotFoundException, IOException {
		BufferedReader channelsCfg;
		ChannelRepository cr;
		SourceRepository sr;
		Source source;

		channelsCfg = null;

		try {
			channelsCfg = new BufferedReader(new InputStreamReader(
					new FileInputStream("channels.cfg"), "ISO-8859-1"));

			cr = new ChannelRepository(em);
			if (ap.getSourceName().equals("ALL_SOURCES")) {
				cr.load(ap.getUserId(), null, channelsCfg);
			} else {
				sr = new SourceRepository(em);
				source = sr.findByName(ap.getUserId(), ap.getSourceName());

				if (source != null) {
					cr.load(ap.getUserId(), source, channelsCfg);
				} else {
					System.err.println("Can't find Source named '"
							+ ap.getSourceName() + "'");
				}
			}
		}

		finally {
			if (channelsCfg != null) {
				channelsCfg.close();
			}
		}
	}

	// Load Groups data for the current User from groups.cfg.
	// The file must reside in the directory where the loader is launched
	private void loadGroups() throws FileNotFoundException, IOException {
		BufferedReader groupsCfg;
		GroupRepository gr;

		groupsCfg = null;

		try {
			groupsCfg = new BufferedReader(new InputStreamReader(
					new FileInputStream("groups.cfg"), "UTF-8"));

			gr = new GroupRepository(em);
			gr.load(ap.getUserId(), groupsCfg);
		}

		finally {
			if (groupsCfg != null) {
				groupsCfg.close();
			}
		}
	}

	// Load Channel Groups from channels.cfg file.
	// The file must reside in the directory where the loader is launched.
	// When a special source name 'ALL_SOURCES' is used, Channels Groups
	// are loaded for all Sources belonging to the current User.
	// Otherwise only Channels for the Source defined are processed
	private void loadChannelGroups() throws FileNotFoundException, IOException {
		BufferedReader channelsCfg;
		ChannelRepository cr;
		SourceRepository sr;
		Source source;

		channelsCfg = null;

		try {
			channelsCfg = new BufferedReader(new InputStreamReader(
					new FileInputStream("channels.cfg"), "ISO-8859-1"));

			cr = new ChannelRepository(em);
			if (ap.getSourceName().equals("ALL_SOURCES")) {
				cr.loadGroups(ap.getUserId(), null, channelsCfg);
			} else {
				sr = new SourceRepository(em);
				source = sr.findByName(ap.getUserId(), ap.getSourceName());

				if (source != null) {
					cr.loadGroups(ap.getUserId(), source, channelsCfg);
				} else {
					System.err.println("Can't find Source named '"
							+ ap.getSourceName() + "'");
				}
			}
		}

		finally {
			if (channelsCfg != null) {
				channelsCfg.close();
			}
		}
	}

}
