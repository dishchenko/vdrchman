package di.vdrchman;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

public class Main {

	public static void main(String[] args) {
		ArgsParser ap;
		Properties p;
		EntityManager em;

		ap = new ArgsParser();
		ap.parse(args);

		if (ap.getCommand() == null) {
			System.err.println("Usage: java -jar <JAR> cleanDb <userId>");
			System.err.println("                       loadSources <userId>");
			System.err
					.println("                       loadTransponders <userId> <sourceName>");
			System.exit(0);
		}

		em = null;

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
				cleanDb(em, ap.getUserId());
			}
			if (ap.getCommand() == Command.LOAD_SOURCES) {
				loadSources(em, ap.getUserId());
			}
			if (ap.getCommand() == Command.LOAD_TRANSPONDERS) {
				loadTransponders(em, ap.getUserId(), ap.getSourceName());
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

	private static void cleanDb(EntityManager em, Integer userId) {
		SourceRepository sr;

		sr = new SourceRepository(em);
		sr.clean(userId);
	}

	private static void loadSources(EntityManager em, Integer userId)
			throws FileNotFoundException, IOException {
		Scanner sourcesConf;
		Scanner diseqcConf;
		Scanner rotorConf;
		SourceRepository sr;

		sourcesConf = null;
		diseqcConf = null;
		rotorConf = null;

		try {
			sourcesConf = new Scanner(new File("sources.conf"), "UTF-8");
			diseqcConf = new Scanner(new File("diseqc.conf"), "UTF-8");
			rotorConf = new Scanner(new File("rotor.conf"), "UTF-8");

			sr = new SourceRepository(em);
			sr.load(userId, sourcesConf, diseqcConf, rotorConf);
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

	private static void loadTransponders(EntityManager em, Integer userId,
			String sourceName) throws FileNotFoundException, IOException {
		SourceRepository sr;
		Source source;
		Scanner sourceFreq;
		TransponderRepository tr;

		sr = new SourceRepository(em);
		source = sr.findByName(userId, sourceName);

		if (source != null) {
			sourceFreq = null;

			try {
				sourceFreq = new Scanner(new File(sourceName + ".freq"),
						"UTF-8");

				tr = new TransponderRepository(em);
				tr.load(userId, source.getId(), sourceFreq);
			}

			finally {
				if (sourceFreq != null) {
					sourceFreq.close();
				}
			}
		} else {
			System.out.println("Can't find source with name '" + sourceName
					+ "'");
		}
	}

}
