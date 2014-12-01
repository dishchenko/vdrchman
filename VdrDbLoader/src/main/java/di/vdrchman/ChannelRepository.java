package di.vdrchman;

import java.util.Scanner;

import javax.persistence.EntityManager;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

public class ChannelRepository {

	private EntityManager em;

	public ChannelRepository(EntityManager em) {
		this.em = em;
	}

	// Load Channels for Sources by reading the data from configuration scanner.
	// If the Source given is null then Channels are loaded
	// for all Sources belonging to the current User.
	// Otherwise only Channels of the given Source are taken into account.
	// In this case configuration scanning interrupts since all Source
	// Channels have been loaded
	public void load(Long userId, Source source, Scanner channelCfg) {
		SourceRepository sr;
		TransponderRepository tr;
		String curSourceName;
		Source curSource;
		String line;
		String[] splitLine;
		int frequency;
		String polarity;
		Transponder transponder;

		sr = new SourceRepository(em);
		tr = new TransponderRepository(em);
		curSourceName = null;
		curSource = null;
		transponder = null;

		while (channelCfg.hasNextLine()) {
			line = channelCfg.nextLine().trim();
			if (line.length() == 0) {
				continue;
			}
			if (line.charAt(0) == '#') {
				continue;
			}
			splitLine = line.split(":");

			if ("S".equals(splitLine[0])) {
				curSourceName = splitLine[1];
				if (source != null) {
					if (curSourceName.equals(source.getName())) {
						curSource = source;
					} else {
						if (curSource != null) {
							break;
						}
					}
				} else {
					curSource = sr.findByName(userId, curSourceName);
					if (curSource == null) {
						Logger.getLogger(this.getClass()).log(
								Level.ERROR,
								"Can't find Source named '" + curSourceName
										+ "'");
					}
				}
			}

			if ("T".equals(splitLine[0])) {
				if (curSource != null) {
					if ((source == null) || (source == curSource)) {
						frequency = Integer.parseInt(splitLine[1]);
						polarity = splitLine[2].substring(0, 1);
						transponder = tr.findBySourceFrequencyPolarity(
								curSource.getId(), frequency, polarity);
						if (transponder == null) {
							Logger.getLogger(this.getClass()).log(
									Level.ERROR,
									"Can't find Transponder for Source '"
											+ curSourceName
											+ "' with frequency '" + frequency
											+ "' and polarity '" + polarity
											+ "'");
						}
					}
				}
			}

			if ("C".equals(splitLine[0])) {
				if (transponder != null) {
					// TODO
				}
			}
		}
	}

}
