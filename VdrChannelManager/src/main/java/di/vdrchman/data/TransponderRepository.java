package di.vdrchman.data;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import di.vdrchman.model.TranspSeqno;
import di.vdrchman.model.Transponder;
import di.vdrchman.data.User;

@Stateless
@Named
public class TransponderRepository {

	@Inject
	private EntityManager em;

	@Inject
	private User user;

	/**
	 * Builds a full or partial list of Transponders belonging to
	 * the current application user depending on the value of
	 * the sourceId parameter.
	 * Transponders are added to the list in ascending sequence number order.
	 * 
	 * @param sourceId the ID of the Source which Transponders are added to
	 *                 the list, if SourceId is negative then all current user's
	 *                 Transponders are added
	 * @return the list of Transponders found for the current application user
	 *         and given Source ID
	 */
	public List<Transponder> findAll(long sourceId) {
		List<Transponder> result;
		Query query;
		List<?> queryResult;

		if (sourceId < 0) {
			query = em
					.createQuery("select t, ts.seqno from Transponder t, TranspSeqno ts where t.id = ts.transpId and ts.userId = :userId order by ts.seqno");
			query.setParameter("userId", user.getId());
		} else {
			query = em
					.createQuery("select t, ts.seqno from Transponder t, TranspSeqno ts where t.id = ts.transpId and ts.userId = :userId and t.sourceId = :sourceId order by ts.seqno");
			query.setParameter("userId", user.getId());
			query.setParameter("sourceId", sourceId);
		}

		queryResult = query.getResultList();

		result = new ArrayList<Transponder>();

		for (Object row : queryResult) {
			result.add((Transponder) ((Object[]) row)[0]);
		}

		return result;
	}

	/**
	 * Finds a Transponder by the combination of Source ID, frequency and polarity given
	 * among the Transponders belonging to the current application user.
	 * 
	 * @param sourceId  the Source ID to find a Transponder on
	 * @param frequency the frequency of Transponder to find
	 * @param polarity  the polarity of Transponder to find
	 * @return the Transponder found or null if no Transponders found
	 */
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

	/**
	 * Finds a Transponder by ID.
	 * 
	 * @param id the ID of the Transponder to find
	 * @return the Transponder found or null if no Transponders found
	 */
	public Transponder findById(long id) {

		return em.find(Transponder.class, id);
	}

	/**
	 * Finds maximum sequence number of Transponder belonging to
	 * the Source with ID given.
	 * 
	 * @param sourceId the Source ID to find a Transponder's maximum sequence
	 *                 number
	 * @return the maximum Transponder sequence number or
	 *         null if no Transponders found
	 */
	public Integer findMaxSeqno(long sourceId) {
		Query query;

		if (sourceId < 0) {
			query = em
					.createQuery("select max(ts.seqno) from TranspSeqno ts, Transponder t where t.id = ts.transpId and ts.userId = :userId");
			query.setParameter("userId", user.getId());
		} else {
			query = em
					.createQuery("select max(ts.seqno) from TranspSeqno ts, Transponder t where t.id = ts.transpId and ts.userId = :userId and t.sourceId = :sourceId");
			query.setParameter("userId", user.getId());
			query.setParameter("sourceId", sourceId);
		}

		return (Integer) query.getSingleResult();
	}

	/**
	 * Returns a sequence number of the Transponder given.
	 * 
	 * @param transponder the Transponder to get the sequence number for
	 * @return the sequence number or null if Transponder not found
	 */
	public Integer getSeqno(Transponder transponder) {
		Integer result;
		TranspSeqno transpSeqno;

		result = null;

		transpSeqno = em.find(TranspSeqno.class, transponder.getId());
		if (transpSeqno != null) {
			result = transpSeqno.getSeqno();
		}

		return result;
	}

	/**
	 * Adds the Transponder to the persisted list of Transponders
	 * (stores it in the database).
	 * 
	 * @param transponder the Transponder to add
	 * @param seqno       the sequence number of the added Transponder
	 */
	public void add(Transponder transponder, int seqno) {

		em.persist(transponder);
		em.flush();

		move(transponder, seqno);
	}

	/**
	 * Updates the Transponder in the persisted list of Transponders
	 * (updates it in the database).
	 * 
	 * @param transponder the Transponder to update
	 */
	public void update(Transponder transponder) {
		em.merge(transponder);
	}

	/**
	 * Moves the Transponder to the new sequence number in the list of
	 * Transponders of the current application user.
	 * 
	 * @param transponder the Transponder to move
	 * @param seqno the new sequence number
	 */
	public void move(Transponder transponder, int seqno) {
		TranspSeqno transpSeqno;
		int curSeqno;
		Query query;

		transpSeqno = em.find(TranspSeqno.class, transponder.getId());

		if (transpSeqno != null) {
			curSeqno = transpSeqno.getSeqno();
			transpSeqno.setSeqno(0);

			query = em.createQuery("update TranspSeqno ts set ts.seqno = -ts.seqno where ts.userId = :userId and ts.seqno > :curSeqno");
			query.setParameter("userId", user.getId());
			query.setParameter("curSeqno", curSeqno);
			query.executeUpdate();

			query = em.createQuery("update TranspSeqno ts set ts.seqno = -ts.seqno - 1 where ts.userId = :userId and ts.seqno < -:curSeqno");
			query.setParameter("userId", user.getId());
			query.setParameter("curSeqno", curSeqno);
			query.executeUpdate();
		}

		query = em.createQuery("update TranspSeqno ts set ts.seqno = -ts.seqno where ts.userId = :userId and ts.seqno >= :seqno");
		query.setParameter("userId", user.getId());
		query.setParameter("seqno", seqno);
		query.executeUpdate();

		query = em.createQuery("update TranspSeqno ts set ts.seqno = -ts.seqno + 1 where ts.userId = :userId and ts.seqno <= -:seqno");
		query.setParameter("userId", user.getId());
		query.setParameter("seqno", seqno);
		query.executeUpdate();

		if (transpSeqno != null) {
			transpSeqno.setSeqno(seqno);
		}
		else
		{
			transpSeqno = new TranspSeqno();
			transpSeqno.setTranspId(transponder.getId());
			transpSeqno.setUserId(user.getId());
			transpSeqno.setSeqno(seqno);
			em.persist(transpSeqno);
		}
	}

	/**
	 * Deletes the Transponder from the persisted list of Transponders
	 * (deletes it from the database).
	 * 
	 * @param transponder the Transponder to delete
	 */
	public void delete(Transponder transponder) {
		long transpId;
		TranspSeqno transpSeqno;
		int seqno;
		Query query;

		transpId = transponder.getId();

		transpSeqno = em.find(TranspSeqno.class, transpId);

		if (transpSeqno != null) {
			seqno = transpSeqno.getSeqno();

			em.remove(transpSeqno);

			query = em.createQuery("update TranspSeqno ts set ts.seqno = -ts.seqno where ts.userId = :userId and ts.seqno > :seqno");
			query.setParameter("userId", user.getId());
			query.setParameter("seqno", seqno);
			query.executeUpdate();

			query = em.createQuery("update TranspSeqno ts set ts.seqno = -ts.seqno - 1 where ts.userId = :userId and ts.seqno < -:seqno");
			query.setParameter("userId", user.getId());
			query.setParameter("seqno", seqno);
			query.executeUpdate();
		}

		em.remove(em.merge(transponder));
	}

}
