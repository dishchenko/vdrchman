package di.vdrchman;

import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class SourceRepository {

	private EntityManager em;

	public SourceRepository(EntityManager em) {
		this.em = em;
	}

	public void clean(Integer userId) {
		Query query;

		query = em.createQuery("delete from Source s where s.userId = :userId");
		query.setParameter("userId", userId);
		query.executeUpdate();
	}

	public void load(Integer userId, Scanner sourcesConf, Scanner diseqcConf,
			Scanner rotorConf) {
		String line;
		String[] splitLine;
		Source source;
		StringBuffer sb;

		sb = new StringBuffer();

		while (sourcesConf.hasNextLine()) {
			line = sourcesConf.nextLine().trim();
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
			em.persist(source);
			em.flush();
		}

		while (diseqcConf.hasNextLine()) {
			line = diseqcConf.nextLine().trim();
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
			em.flush();
		}

		while (rotorConf.hasNextLine()) {
			line = rotorConf.nextLine().trim();
			if (line.length() == 0) {
				continue;
			}
			if (line.charAt(0) == '#') {
				continue;
			}
			splitLine = line.split("\\s+");
			source = findByName(userId, splitLine[1]);
			source.setRotor(Integer.valueOf(splitLine[0]));
			em.flush();
		}
	}

	public Source findByName(Integer userId, String name) {
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
