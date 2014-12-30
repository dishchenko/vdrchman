package di.vdrchman.data;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import di.vdrchman.model.TranspSeqno;
import di.vdrchman.model.Transponder;

@Stateless
@Named
public class TransponderRepository {

	@Inject
	private EntityManager em;

	@Inject
	private SessionUser sessionUser;

	/**
	 * Builds a full or partial list of transponders belonging to the current
	 * application user depending on the value of sourceId parameter.
	 * Transponders are added to the list in ascending sequence number order.
	 * 
	 * @param sourceId
	 *            the ID of the source which transponders are added to the list,
	 *            if sourceId is negative then all current user's transponders
	 *            are added
	 * @return the list of transponders found for the current application user
	 *         and given source ID
	 */
	public List<Transponder> findAll(long sourceId) {
		List<Transponder> result;
		TypedQuery<Object[]> query;
		List<Object[]> queryResult;

		if (sourceId < 0) {
			query = em
					.createQuery(
							"select t, ts.seqno from Transponder t, TranspSeqno ts where t.id = ts.transpId and ts.userId = :userId order by ts.seqno",
							Object[].class);
			query.setParameter("userId", sessionUser.getId());
		} else {
			query = em
					.createQuery(
							"select t, ts.seqno from Transponder t, TranspSeqno ts where t.id = ts.transpId and ts.userId = :userId and t.sourceId = :sourceId order by ts.seqno",
							Object[].class);
			query.setParameter("userId", sessionUser.getId());
			query.setParameter("sourceId", sourceId);
		}

		queryResult = query.getResultList();

		result = new ArrayList<Transponder>();

		for (Object[] row : queryResult) {
			result.add((Transponder) row[0]);
		}

		return result;
	}

	/**
	 * Finds a transponder by the combination of source ID, frequency,
	 * polarization and stream ID given among the transponders belonging to the
	 * current application user.
	 * 
	 * @param sourceId
	 *            the source ID to find a transponder within
	 * @param frequency
	 *            the frequency of transponder to find
	 * @param polarization
	 *            the polarization of transponder to find
	 * @param streamId
	 *            the stream ID within a multistream transponder to find or
	 *            null/zero if not multistream transponder
	 * @return the transponder found or null if no transponder found
	 */
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

	/**
	 * Finds a transponder by ID.
	 * 
	 * @param id
	 *            the ID of the transponder to find
	 * @return the transponder found or null if no transponder found
	 */
	public Transponder findById(long id) {

		return em.find(Transponder.class, id);
	}

	/**
	 * Finds maximum sequence number of transponder related to the source with
	 * ID given.
	 * 
	 * @param sourceId
	 *            the source ID to find a transponder's maximum sequence number
	 * @return the maximum transponder sequence number or null if no
	 *         transponders found
	 */
	public Integer findMaxSeqno(long sourceId) {
		TypedQuery<Integer> query;

		if (sourceId < 0) {
			query = em
					.createQuery(
							"select max(ts.seqno) from TranspSeqno ts, Transponder t where t.id = ts.transpId and ts.userId = :userId",
							Integer.class);
			query.setParameter("userId", sessionUser.getId());
		} else {
			query = em
					.createQuery(
							"select max(ts.seqno) from TranspSeqno ts, Transponder t where t.id = ts.transpId and ts.userId = :userId and t.sourceId = :sourceId",
							Integer.class);
			query.setParameter("userId", sessionUser.getId());
			query.setParameter("sourceId", sourceId);
		}

		return query.getSingleResult();
	}

	/**
	 * Returns a sequence number of the transponder given.
	 * 
	 * @param transponder
	 *            the transponder to get the sequence number for
	 * @return the sequence number or null if no transponder found
	 */
	public Integer findSeqno(Transponder transponder) {
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
	 * Finds a transponder by its sequence number.
	 * 
	 * @param seqno
	 *            the sequence number of the transponder to be found
	 * @return the transponder found or null if no transponder found
	 */
	public Transponder findBySeqno(int seqno) {
		Transponder result;
		TypedQuery<Integer> query;
		Integer queryResult;

		result = null;

		query = em
				.createQuery(
						"select ts.transpId from TranspSeqno ts where ts.userId = :userId and ts.seqno = :seqno",
						Integer.class);
		query.setParameter("userId", sessionUser.getId());
		query.setParameter("seqno", seqno);

		try {
			queryResult = query.getSingleResult();
		} catch (NoResultException ex) {
			queryResult = null;
		}

		if (queryResult != null) {
			result = findById(queryResult);
		}

		return result;
	}

	/**
	 * Finds a transponder which has one less sequence number compared to the
	 * sequence number of the transponder given.
	 * 
	 * @param id
	 *            the transponder for which the previous transponder has to be
	 *            found
	 * @return the previous transponder or null if there's no previous
	 *         transponder
	 */
	public Transponder findPrevious(Transponder transponder) {

		return findBySeqno(findSeqno(transponder) - 1);
	}

	/**
	 * Finds a transponder which has one less sequence number compared to the
	 * sequence number of the transponder with ID given.
	 * 
	 * @param id
	 *            the ID of the transponder for which the previous transponder
	 *            has to be found
	 * @return the previous transponder or null if there's no previous
	 *         transponder
	 */
	public Transponder findPrevious(long id) {

		return findBySeqno(findSeqno(findById(id)) - 1);
	}

	/**
	 * Adds the transponder to the persisted list of transponders (stores it in
	 * the database).
	 * 
	 * @param transponder
	 *            the transponder to add
	 * @param seqno
	 *            the sequence number of the added transponder
	 */
	public void add(Transponder transponder, int seqno) {
		Transponder mergedTransponder;

		mergedTransponder = em.merge(transponder);
		em.flush();
		transponder.setId(mergedTransponder.getId());

		move(transponder, seqno);
	}

	/**
	 * Updates the transponder in the persisted list of transponders (updates it
	 * in the database).
	 * 
	 * @param transponder
	 *            the transponder to update
	 */
	public void update(Transponder transponder) {
		em.merge(transponder);
	}

	/**
	 * Moves the transponder to the new sequence number in the list of
	 * transponders of the current application user.
	 * 
	 * @param transponder
	 *            the transponder to move
	 * @param seqno
	 *            the new sequence number
	 */
	public void move(Transponder transponder, int seqno) {
		TranspSeqno transpSeqno;
		int curSeqno;
		Query query;

		transpSeqno = em.find(TranspSeqno.class, transponder.getId());

		if (transpSeqno != null) {
			curSeqno = transpSeqno.getSeqno();
			transpSeqno.setSeqno(0);

			query = em
					.createQuery("update TranspSeqno ts set ts.seqno = -ts.seqno where ts.userId = :userId and ts.seqno > :curSeqno");
			query.setParameter("userId", sessionUser.getId());
			query.setParameter("curSeqno", curSeqno);
			query.executeUpdate();

			query = em
					.createQuery("update TranspSeqno ts set ts.seqno = -ts.seqno - 1 where ts.userId = :userId and ts.seqno < -:curSeqno");
			query.setParameter("userId", sessionUser.getId());
			query.setParameter("curSeqno", curSeqno);
			query.executeUpdate();
		}

		query = em
				.createQuery("update TranspSeqno ts set ts.seqno = -ts.seqno where ts.userId = :userId and ts.seqno >= :seqno");
		query.setParameter("userId", sessionUser.getId());
		query.setParameter("seqno", seqno);
		query.executeUpdate();

		query = em
				.createQuery("update TranspSeqno ts set ts.seqno = -ts.seqno + 1 where ts.userId = :userId and ts.seqno <= -:seqno");
		query.setParameter("userId", sessionUser.getId());
		query.setParameter("seqno", seqno);
		query.executeUpdate();

		if (transpSeqno != null) {
			transpSeqno.setSeqno(seqno);
		} else {
			transpSeqno = new TranspSeqno();
			transpSeqno.setTranspId(transponder.getId());
			transpSeqno.setUserId(sessionUser.getId());
			transpSeqno.setSeqno(seqno);
			em.merge(transpSeqno);
		}
	}

	/**
	 * Deletes the transponder from the persisted list of transponders (deletes
	 * it from the database).
	 * 
	 * @param transponder
	 *            the transponder to delete
	 */
	public void delete(Transponder transponder) {
		TranspSeqno transpSeqno;
		int seqno;
		Query query;

		transpSeqno = em.find(TranspSeqno.class, transponder.getId());

		if (transpSeqno != null) {
			seqno = transpSeqno.getSeqno();

			em.remove(transpSeqno);

			query = em
					.createQuery("update TranspSeqno ts set ts.seqno = -ts.seqno where ts.userId = :userId and ts.seqno > :seqno");
			query.setParameter("userId", sessionUser.getId());
			query.setParameter("seqno", seqno);
			query.executeUpdate();

			query = em
					.createQuery("update TranspSeqno ts set ts.seqno = -ts.seqno - 1 where ts.userId = :userId and ts.seqno < -:seqno");
			query.setParameter("userId", sessionUser.getId());
			query.setParameter("seqno", seqno);
			query.executeUpdate();
		}

		em.remove(em.merge(transponder));
	}

	/**
	 * Renumbers (makes exactly sequentially ordered) transponders' sequence
	 * numbers
	 */
	public void renumberSeqnos() {
		TypedQuery<TranspSeqno> query;
		List<TranspSeqno> transpSeqnos;
		int orderedSeqno;

		query = em
				.createQuery(
						"select ts from TranspSeqno ts where ts.userId = :userId order by ts.seqno",
						TranspSeqno.class);
		query.setParameter("userId", sessionUser.getId());

		transpSeqnos = query.getResultList();

		orderedSeqno = 1;

		for (TranspSeqno transpSeqno : transpSeqnos) {
			if (transpSeqno.getSeqno() != orderedSeqno) {
				transpSeqno.setSeqno(orderedSeqno);
			}
			++orderedSeqno;
		}
	}

}
