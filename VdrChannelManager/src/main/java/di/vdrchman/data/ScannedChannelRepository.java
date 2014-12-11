package di.vdrchman.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import di.vdrchman.model.ScannedChannel;

@Stateless
@Named
public class ScannedChannelRepository {

	@Inject
	private EntityManager em;

	@Inject
	private User user;

	/**
	 * Builds a full or partial list of scanned channels belonging to the
	 * current application user depending on values of sourceId and transpId
	 * parameters. Channels are added to the list in ascending ID order.
	 * 
	 * @param sourceId
	 *            the ID of the source which scanned channels are added to the
	 *            list, if both sourceId and transpId are negative then all
	 *            current user's channels are added
	 * @param transpId
	 *            the ID of the transponder which scanned channels are added to
	 *            the list, if transpId is negative then sourceId value is taken
	 *            into account
	 * @return the list of scanned channels found for the current application
	 *         user and given source and transponder IDs
	 */
	public List<ScannedChannel> findAll(long sourceId, long transpId) {
		TypedQuery<ScannedChannel> query;

		if (transpId < 0) {
			if (sourceId < 0) {
				query = em
						.createQuery(
								"select sc from ScannedChannel sc where sc.userId = :userId order by sc.id",
								ScannedChannel.class);
				query.setParameter("userId", user.getId());
			} else {
				query = em
						.createQuery(
								"select sc from ScannedChannel sc, Source s where sc.sourceName = s.name and s.id = :sourceId and sc.userId = :userId order by sc.id",
								ScannedChannel.class);
				query.setParameter("sourceId", sourceId);
				query.setParameter("userId", user.getId());
			}
		} else {
			query = em
					.createQuery(
							"select sc from ScannedChannel sc, Transponder t, Source s where sc.sourceName = s.name and sc.frequency = t.frequency and sc.polarity = t.polarity and t.sourceId = s.id and t.id = :transpId and sc.userId = :userId order by sc.id",
							ScannedChannel.class);
			query.setParameter("transpId", transpId);
			query.setParameter("userId", user.getId());
		}

		return query.getResultList();
	}

	/**
	 * Finds a scanned channel by ID.
	 * 
	 * @param id
	 *            the ID of the scanned channel to find
	 * @return the scanned channel found or null if no channel found
	 */
	public ScannedChannel findById(long id) {

		return em.find(ScannedChannel.class, id);
	}

}
