package di.vdrchman;

import java.io.BufferedReader;
import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

public class TransponderRepository {

	private EntityManager em;

	public TransponderRepository(EntityManager em) {
		this.em = em;
	}

	// Load Transponders data for the Source with given ID belonging to
	// the User with given ID by reading the data from configuration reader
	public void load(Long userId, Long sourceId, BufferedReader sourceFreq)
			throws NumberFormatException, IOException {
		Query query;
		Integer queryResult;
		int seqno;
		String line;
		boolean ignored;
		String[] splitLine;
		Transponder transponder;
		TranspSeqno transpSeqno;

		query = em
				.createQuery("select max(ts.seqno) from TranspSeqno ts where ts.userId = :userId");
		query.setParameter("userId", userId);
		queryResult = (Integer) query.getSingleResult();

		if (queryResult != null) {
			seqno = queryResult + 1;
		} else {
			seqno = 1;
		}

		while ((line = sourceFreq.readLine()) != null) {

			if (line.length() == 0) {
				continue;
			}
			if (line.charAt(0) == '#') {
				continue;
			}
			ignored = false;
			if (line.charAt(0) == '!') {
				ignored = true;
				line = line.substring(2);
			}
			splitLine = line.split("\\s+");

			transponder = new Transponder();
			transponder.setSourceId(sourceId);
			transponder.setDvbsGen(Integer.parseInt(splitLine[0].substring(1)));
			transponder.setFrequency(Integer.parseInt(splitLine[1]) / 1000);
			transponder.setPolarity(splitLine[2]);
			transponder.setSymbolRate(Integer.parseInt(splitLine[3]) / 1000);
			transponder.setIgnored(ignored);
			em.persist(transponder);
			em.flush();

			transpSeqno = new TranspSeqno();
			transpSeqno.setTranspId(transponder.getId());
			transpSeqno.setUserId(userId);
			transpSeqno.setSeqno(seqno);
			em.persist(transpSeqno);
			em.flush();

			++seqno;
		}
	}

	// Load NIDs and TIDs for Transponders by reading the data from
	// configuration reader.
	// If the Source given is null then NIDs/TIDs are loaded
	// for all Transponders belonging to the current User.
	// Otherwise only Transponders of the given Source are processed.
	// In this case configuration reading interrupts as long as all Source
	// Transponders have been loaded
	public void loadNidsTids(Long userId, Source source,
			BufferedReader channelCfg) throws NumberFormatException,
			IOException {
		SourceRepository sr;
		String curSourceName;
		Source curSource;
		String line;
		String[] splitLine;
		int frequency;
		String polarity;
		Transponder transponder;
		int nid;
		int tid;

		sr = new SourceRepository(em);
		curSourceName = null;
		curSource = null;

		while ((line = channelCfg.readLine()) != null) {

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
					frequency = Integer.parseInt(splitLine[1]);
					polarity = splitLine[2].substring(0, 1);
					transponder = findBySourceFrequencyPolarity(
							curSource.getId(), frequency, polarity);
					if (transponder != null) {
						nid = Integer.parseInt(splitLine[4]);
						if (nid != 0) {
							transponder.setNid(nid);
						}
						tid = Integer.parseInt(splitLine[5]);
						if (tid != 0) {
							transponder.setTid(tid);
						}
						em.flush();
					} else {
						Logger.getLogger(this.getClass()).log(
								Level.ERROR,
								"Can't find Transponder for Source '"
										+ curSourceName + "' with frequency '"
										+ frequency + "' and polarity '"
										+ polarity + "'");
					}
				}
			}
		}
	}

	// Find a Transponder with given frequency and polarity which relates to
	// the Source with the ID given.
	// Return null if no Transponder found
	public Transponder findBySourceFrequencyPolarity(long sourceId,
			int frequency, String polarity) {
		Transponder result;
		CriteriaBuilder cb;
		CriteriaQuery<Transponder> criteria;
		Root<Transponder> transponderRoot;
		Predicate p;

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(Transponder.class);
		transponderRoot = criteria.from(Transponder.class);
		criteria.select(transponderRoot);
		p = cb.conjunction();
		p = cb.and(p, cb.equal(transponderRoot.get("sourceId"), sourceId));
		p = cb.and(p, cb.equal(transponderRoot.get("frequency"), frequency));
		p = cb.and(p, cb.equal(transponderRoot.get("polarity"), polarity));
		criteria.where(p);

		try {
			result = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException ex) {
			result = null;
		}

		return result;
	}

}
