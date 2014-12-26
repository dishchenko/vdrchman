package di.vdrchman;

import java.io.BufferedReader;
import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

public class SourceRepository {

	private EntityManager em;

	public SourceRepository(EntityManager em) {
		this.em = em;
	}

	// Delete all Sources for User with given userId
	public void clean(Long userId) {
		Query query;

		query = em.createQuery("delete from Source s where s.userId = :userId");
		query.setParameter("userId", userId);
		query.executeUpdate();
	}

	// Load Sources data for the User with given ID by reading the data
	// from configuration readers
	public void load(Long userId, BufferedReader sourcesConf,
			BufferedReader diseqcConf, BufferedReader rotorConf)
			throws IOException {
		StringBuffer sb;
		int lineNo;
		String line;
		String[] splitLine;
		Source source;

		sb = new StringBuffer();

		lineNo = 0;

		try {
			while ((line = sourcesConf.readLine()) != null) {

				if (line.length() == 0) {
					continue;
				}
				if (line.charAt(0) == '#') {
					continue;
				}
				splitLine = line.split("\\s+");

				source = new Source();
				source.setUserId(userId);
				source.setName(splitLine[0]);
				sb.setLength(0);
				for (int i = 1; i < splitLine.length; ++i) {
					sb.append(splitLine[i]).append(' ');
				}
				sb.setLength(sb.length() - 1);
				source.setDescription(sb.toString());
				em.merge(source);
			}
		} catch (Throwable ex) {
			Logger.getLogger(this.getClass()).log(
					Level.ERROR,
					"Exception while processing sources.conf line " + lineNo
							+ "\n\n");

			throw ex;
		}

		lineNo = 0;

		try {
			while ((line = diseqcConf.readLine()) != null) {

				if (line.length() == 0) {
					continue;
				}
				if (line.charAt(0) == '#') {
					continue;
				}
				splitLine = line.split("\\s+");

				source = findByName(userId, splitLine[0]);
				sb.setLength(0);
				for (int i = 1; i < splitLine.length; ++i) {
					sb.append(splitLine[i]).append(' ');
				}
				sb.setLength(sb.length() - 1);
				if (splitLine[1].equals("11700")) {
					if (splitLine[2].equals("V")) {
						source.setLoV(sb.toString());
					} else {
						source.setLoH(sb.toString());
					}
				} else {
					if (splitLine[2].equals("V")) {
						source.setHiV(sb.toString());
					} else {
						source.setHiH(sb.toString());
					}
				}
			}
		} catch (Throwable ex) {
			Logger.getLogger(this.getClass()).log(
					Level.ERROR,
					"Exception while processing diseqc.conf line " + lineNo
							+ "\n\n");

			throw ex;
		}

		lineNo = 0;

		try {
			while ((line = rotorConf.readLine()) != null) {

				if (line.length() == 0) {
					continue;
				}
				if (line.charAt(0) == '#') {
					continue;
				}
				splitLine = line.split("\\s+");

				source = findByName(userId, splitLine[1]);
				source.setRotor(Integer.valueOf(splitLine[0]));
			}
		} catch (Throwable ex) {
			Logger.getLogger(this.getClass()).log(
					Level.ERROR,
					"Exception while processing rotor.conf line " + lineNo
							+ "\n\n");

			throw ex;
		}
	}

	// Find a Source with given name belonging to the User identified by ID.
	// Return null if no Source found
	public Source findByName(Long userId, String name) {
		Source result;
		CriteriaBuilder cb;
		CriteriaQuery<Source> criteria;
		Root<Source> sourceRoot;

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(Source.class);
		sourceRoot = criteria.from(Source.class);

		criteria.select(sourceRoot);
		criteria.where(cb.and(cb.equal(sourceRoot.get("userId"), userId),
				cb.equal(sourceRoot.get("name"), name)));

		try {
			result = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException ex) {
			result = null;
		}

		return result;
	}

}
