package di.vdrchman.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import di.vdrchman.model.IgnoredChannel;

@Stateless
@Named
public class IgnoredChannelRepository {

	@Inject
	private EntityManager em;

	@Inject
	private User user;

	/**
	 * Builds a full or partial list of ignored channels belonging to the
	 * current application user depending on values of sourceId and transpId
	 * parameters. Channels are added to the list in ascending ID order.
	 * 
	 * @param sourceId
	 *            the ID of the source which ignored channels are added to the
	 *            list, if both sourceId and transpId are negative then all
	 *            current user's channels are added
	 * @param transpId
	 *            the ID of the transponder which ignored channels are added to
	 *            the list, if transpId is negative then sourceId value is taken
	 *            into account
	 * @return the list of ignored channels found for the current application
	 *         user and given source and transponder IDs
	 */
	public List<IgnoredChannel> findAll(long sourceId, long transpId) {
		TypedQuery<IgnoredChannel> query;

		if (transpId < 0) {
			if (sourceId < 0) {
				query = em
						.createQuery(
								"select ic from IgnoredChannel ic, Transponder t, Source s where ic.transpId = t.id and t.sourceId = s.id and s.userId = :userId order by ic.id",
								IgnoredChannel.class);
				query.setParameter("userId", user.getId());
			} else {
				query = em
						.createQuery(
								"select ic from IgnoredChannel ic, Transponder t where ic.transpId = t.id and t.sourceId = :sourceId order by ic.id",
								IgnoredChannel.class);
				query.setParameter("sourceId", sourceId);
			}
		} else {
			query = em
					.createQuery(
							"select ic from IgnoredChannel ic where ic.transpId = :transpId order by ic.id",
							IgnoredChannel.class);
			query.setParameter("transpId", transpId);
		}

		return query.getResultList();
	}

	/**
	 * Finds an channel by the combination of transponder ID, SID and APID given
	 * among the ignored channels belonging to the current application user.
	 * 
	 * @param transpId
	 *            the transponder ID to find an ignored channel within
	 * @param sid
	 *            the SID of ignored channel to find
	 * @param apid
	 *            the APID of ignored channel to find
	 * @return the ignored channel found or null if no channel found
	 */
	public IgnoredChannel findByTransponderSidApid(long transpId, int sid,
			int apid) {
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

	/**
	 * Finds an ignored channel by ID.
	 * 
	 * @param id
	 *            the ID of the ignored channel to find
	 * @return the ignored channel found or null if no channel found
	 */
	public IgnoredChannel findById(long id) {

		return em.find(IgnoredChannel.class, id);
	}

	/**
	 * Deletes the channel from the persisted list of ignored channels (deletes
	 * it from the database).
	 * 
	 * @param channel
	 *            the ignored channel to delete
	 */
	public void delete(IgnoredChannel channel) {
		em.remove(em.merge(channel));
	}

}
