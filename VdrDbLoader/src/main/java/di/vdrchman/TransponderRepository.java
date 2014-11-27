package di.vdrchman;

import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class TransponderRepository {

	private EntityManager em;

	public TransponderRepository(EntityManager em) {
		this.em = em;
	}

	public void load(Long userId, Long sourceId, Scanner sourceFreq) {
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

		while (sourceFreq.hasNextLine()) {
			line = sourceFreq.nextLine().trim();
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

}
