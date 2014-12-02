package di.vdrchman;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

public class IgnoredChannelRepository {

	private EntityManager em;

	public IgnoredChannelRepository(EntityManager em) {
		this.em = em;
	}

	// Load Ignored Channels by reading the data from configuration reader.
	// If the Source given is null then Ignored Channels are loaded
	// for all Sources belonging to the current User.
	// Otherwise only Channels of the given Source are taken into account.
	public void load(Long userId, Source source, BufferedReader channelsIgnored)
			throws IOException {
		SourceRepository sr;
		TransponderRepository tr;
		String line;
		String[] splitLine;
		Source curSource;
		String curSourceName;
		Transponder transponder;
		int frequency;
		String polarity;
		int sid;
		String[] snInfo;
		String scannedName;
		String providerName;
		String[] aInfo;
		String[] aInfo2;
		int apid;
		IgnoredChannel ignoredChannel;

		sr = new SourceRepository(em);
		tr = new TransponderRepository(em);

		while ((line = channelsIgnored.readLine()) != null) {

			if (line.length() == 0) {
				continue;
			}
			if (line.charAt(0) == '#') {
				continue;
			}
			splitLine = line.split(":");

			curSource = null;

			curSourceName = splitLine[0];
			if (source != null) {
				if (curSourceName.equals(source.getName())) {
					curSource = source;
				}
			} else {
				curSource = sr.findByName(userId, curSourceName);
				if (curSource == null) {
					Logger.getLogger(this.getClass()).log(Level.ERROR,
							"Can't find Source named '" + curSourceName + "'");
				}
			}

			transponder = null;

			if (curSource != null) {
				frequency = Integer.parseInt(splitLine[1]);
				polarity = splitLine[2].substring(0, 1);
				transponder = tr.findBySourceFrequencyPolarity(
						curSource.getId(), frequency, polarity);
				if (transponder == null) {
					Logger.getLogger(this.getClass()).log(
							Level.ERROR,
							"Can't find Transponder for Source '"
									+ curSourceName + "' with frequency '"
									+ frequency + "' and polarity '" + polarity
									+ "'");
				}
			}

			if (transponder != null) {
				sid = Integer.parseInt(splitLine[3]);
				snInfo = Charset
						.forName("ISO-8859-5")
						.decode(ByteBuffer.wrap(splitLine[5]
								.getBytes("ISO-8859-1"))).toString().split(";");
				scannedName = snInfo[0];
				if (snInfo.length == 2) {
					providerName = snInfo[1];
				} else {
					providerName = null;
				}
				aInfo = splitLine[4].split(",");
				for (int i = 0; i < aInfo.length; ++i) {
					aInfo2 = aInfo[i].split(";");
					for (int j = 0; j < aInfo2.length; ++j) {
						apid = Integer.parseInt(aInfo2[j].split("=")[0]);

						ignoredChannel = findByTransponderSidApid(transponder.getId(), sid, apid);
						if (ignoredChannel == null) {
							ignoredChannel = new IgnoredChannel();
							ignoredChannel.setTranspId(transponder.getId());
							ignoredChannel.setSid(sid);
							ignoredChannel.setApid(apid);
							ignoredChannel.setScannedName(scannedName);
							ignoredChannel.setProviderName(providerName);
							em.persist(ignoredChannel);
							em.flush();
						}
					}
				}
			}
		}
	}

	// Find an Ignored Channel with given SID and APID which relates to
	// the Transponder with the ID given.
	// Return null if no Ignored Channel found
	public IgnoredChannel findByTransponderSidApid(long transpId, int sid, int apid) {
		IgnoredChannel result;
		CriteriaBuilder cb;
		CriteriaQuery<IgnoredChannel> criteria;
		Root<IgnoredChannel> ignoredChannelRoot;
		Predicate p;

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(IgnoredChannel.class);
		ignoredChannelRoot = criteria.from(IgnoredChannel.class);
		criteria.select(ignoredChannelRoot);
		p = cb.conjunction();
		p = cb.and(p, cb.equal(ignoredChannelRoot.get("transpId"), transpId));
		p = cb.and(p, cb.equal(ignoredChannelRoot.get("sid"), sid));
		p = cb.and(p, cb.equal(ignoredChannelRoot.get("apid"), apid));
		criteria.where(p);

		try {
			result = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException ex) {
			result = null;
		}

		return result;
	}

}
