package di.vdrchman;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
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
		TypedQuery<Integer> query;
		Integer queryResult;
		int seqno;
		int lineNo;
		String line;
		boolean ignored;
		String[] splitLine;
		Transponder transponder;
		Integer streamId;
		Transponder mergedTransponder;
		TranspSeqno transpSeqno;

		query = em
				.createQuery(
						"select max(ts.seqno) from TranspSeqno ts where ts.userId = :userId",
						Integer.class);
		query.setParameter("userId", userId);
		queryResult = query.getSingleResult();

		if (queryResult != null) {
			seqno = queryResult + 1;
		} else {
			seqno = 1;
		}

		lineNo = 0;

		try {
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
				transponder.setDvbsGen(Integer.parseInt(splitLine[0]
						.substring(1)));
				transponder.setFrequency(Integer.parseInt(splitLine[1]) / 1000);
				transponder.setPolarization(splitLine[2]);
				transponder
						.setSymbolRate(Integer.parseInt(splitLine[3]) / 1000);
				streamId = null;
				if (splitLine.length == 8) {
					streamId = Integer.parseInt(splitLine[7]);
				}
				transponder.setStreamIdNullable(streamId);
				transponder.setIgnored(ignored);
				mergedTransponder = em.merge(transponder);
				em.flush();
				transponder.setId(mergedTransponder.getId());

				transpSeqno = new TranspSeqno();
				transpSeqno.setTranspId(transponder.getId());
				transpSeqno.setUserId(userId);
				transpSeqno.setSeqno(seqno);
				em.merge(transpSeqno);

				++seqno;
			}
		} catch (Throwable ex) {
			Logger.getLogger(this.getClass()).log(Level.ERROR,
					"Exception while processing line " + lineNo + "\n\n");

			throw ex;
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
			BufferedReader channelsCfg) throws NumberFormatException,
			IOException {
		SourceRepository sr;
		String curSourceName;
		Source curSource;
		int lineNo;
		String line;
		String[] splitLine;
		int frequency;
		String polarization;
		Integer streamId;
		int streamIdPos;
		Transponder transponder;
		int nid;
		int tid;

		sr = new SourceRepository(em);
		curSourceName = null;
		curSource = null;

		lineNo = 0;

		try {
			while ((line = channelsCfg.readLine()) != null) {

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
									Level.WARN,
									"Can't find Source named '" + curSourceName
											+ "'");
						}
					}
				}

				if ("T".equals(splitLine[0])) {
					if (curSource != null) {
						frequency = Integer.parseInt(splitLine[1]);
						polarization = splitLine[2].substring(0, 1);
						streamId = null;
						streamIdPos = splitLine[2].indexOf("X");
						if (streamIdPos >= 0) {
							try {
								streamId = NumberFormat
										.getInstance()
										.parse(splitLine[2]
												.substring(streamIdPos + 1))
										.intValue();
							} catch (ParseException ex) {
								// do nothing
							}
						}
						transponder = findBySourceFrequencyPolarizationStream(
								curSource.getId(), frequency, polarization,
								streamId);
						if (transponder != null) {
							nid = Integer.parseInt(splitLine[4]);
							if (nid != 0) {
								transponder.setNid(nid);
							}
							tid = Integer.parseInt(splitLine[5]);
							if (tid != 0) {
								transponder.setTid(tid);
							}
						} else {
							Logger.getLogger(this.getClass()).log(
									Level.WARN,
									"Can't find Transponder for Source '"
											+ curSourceName
											+ "' with frequency '" + frequency
											+ "', polarization '"
											+ polarization
											+ "' and stream ID '" + streamId
											+ "'");
						}
					}
				}
			}
		} catch (Throwable ex) {
			Logger.getLogger(this.getClass()).log(Level.ERROR,
					"Exception while processing line " + lineNo + "\n\n");

			throw ex;
		}
	}

	// Renumber (makes exactly sequentially ordered) transponders' sequence
	// numbers
	public void renumberSeqnos(Long userId) {
		TypedQuery<TranspSeqno> query;
		List<TranspSeqno> transpSeqnos;
		int orderedSeqno;

		query = em
				.createQuery(
						"select ts from TranspSeqno ts where ts.userId = :userId order by ts.seqno",
						TranspSeqno.class);
		query.setParameter("userId", userId);

		transpSeqnos = query.getResultList();

		orderedSeqno = 1;

		for (TranspSeqno transpSeqno : transpSeqnos) {
			if (transpSeqno.getSeqno() != orderedSeqno) {
				transpSeqno.setSeqno(orderedSeqno);
			}
			++orderedSeqno;
		}
	}

	// Find a Transponder with given frequency, polarization and stream ID which
	// relates to the Source with the ID given.
	// Return null if no Transponder found
	public Transponder findBySourceFrequencyPolarizationStream(long sourceId,
			int frequency, String polarization, Integer streamId) {
		Transponder result;
		CriteriaBuilder cb;
		CriteriaQuery<Transponder> criteria;
		Root<Transponder> transponderRoot;
		Predicate p;

		if (streamId == null) {
			streamId = 0;
		}

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(Transponder.class);
		transponderRoot = criteria.from(Transponder.class);
		criteria.select(transponderRoot);
		p = cb.conjunction();
		p = cb.and(p, cb.equal(transponderRoot.get("sourceId"), sourceId));
		p = cb.and(p, cb.equal(transponderRoot.get("frequency"), frequency));
		p = cb.and(p,
				cb.equal(transponderRoot.get("polarization"), polarization));
		p = cb.and(p, cb.equal(transponderRoot.get("streamId"), streamId));
		criteria.where(p);

		try {
			result = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException ex) {
			result = null;
		}

		return result;
	}

}
