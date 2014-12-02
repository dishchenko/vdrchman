package di.vdrchman;

import java.io.BufferedReader;
import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class GroupRepository {

	private EntityManager em;

	public GroupRepository(EntityManager em) {
		this.em = em;
	}

	// Delete all Groups for User with given userId
	public void clean(Long userId) {
		Query query;

		query = em.createQuery("delete from Group g where g.userId = :userId");
		query.setParameter("userId", userId);
		query.executeUpdate();
	}

	// Load Groups data for the User with given ID by reading the data
	// from configuration reader
	public void load(Long userId, BufferedReader groupsCfg) throws IOException {
		StringBuffer sb;
		String line;
		String[] splitLine;
		String[] ncInfo;
		Group group;

		sb = new StringBuffer();

		while ((line = groupsCfg.readLine()) != null) {

			if (line.length() == 0) {
				continue;
			}
			if (line.charAt(0) == '#') {
				continue;
			}
			splitLine = line.split("\\s+");
			ncInfo = splitLine[0].split(":@");

			group = new Group();
			group.setUserId(userId);
			group.setName(ncInfo[0]);
			group.setStartChannelNo(Integer.parseInt(ncInfo[1]));
			sb.setLength(0);
			for (int i = 1; i < splitLine.length; ++i) {
				sb.append(splitLine[i]).append(' ');
			}
			sb.setLength(sb.length() - 1);
			group.setDescription(sb.toString());
			em.persist(group);
			em.flush();
		}
	}

	// Find a Group with given name belonging to the User identified by ID.
	// Return null if no Group found
	public Group findByName(Long userId, String name) {
		Group result;
		CriteriaBuilder cb;
		CriteriaQuery<Group> criteria;
		Root<Group> groupRoot;

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(Group.class);
		groupRoot = criteria.from(Group.class);

		criteria.select(groupRoot);
		criteria.where(cb.and(cb.equal(groupRoot.get("userId"), userId),
				cb.equal(groupRoot.get("name"), name)));

		try {
			result = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException ex) {
			result = null;
		}

		return result;
	}

}
