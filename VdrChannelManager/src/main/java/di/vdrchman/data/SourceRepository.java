package di.vdrchman.data;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

import di.vdrchman.model.Source;
import di.vdrchman.model.Transponder;

@Stateless
@Named
public class SourceRepository {

	@Inject
	private EntityManager em;

	@Inject
	private SessionUser sessionUser;

	@Inject
	private TransponderRepository transponderRepository;

	/**
	 * Builds a list of sources belonging to the current application user. First
	 * adds sources with no rotor value defined in ascending name order. Then
	 * adds sources in ascending rotor value order.
	 * 
	 * @return the list of all sources for the current application user
	 */
	public List<Source> findAll() {
		List<Source> result;
		CriteriaBuilder cb;
		CriteriaQuery<Source> criteria;
		Root<Source> sourceRoot;

		result = new ArrayList<Source>();

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(Source.class);
		sourceRoot = criteria.from(Source.class);

		criteria.select(sourceRoot);
		criteria.where(cb.and(
				cb.equal(sourceRoot.get("userId"), sessionUser.getId()),
				cb.isNull(sourceRoot.get("rotor"))));
		criteria.orderBy(cb.asc(sourceRoot.get("name")));
		result.addAll(em.createQuery(criteria).getResultList());

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(Source.class);
		sourceRoot = criteria.from(Source.class);
		criteria.select(sourceRoot);
		criteria.where(cb.and(
				cb.equal(sourceRoot.get("userId"), sessionUser.getId()),
				cb.isNotNull(sourceRoot.get("rotor"))));
		criteria.orderBy(cb.asc(sourceRoot.get("rotor")));
		result.addAll(em.createQuery(criteria).getResultList());

		return result;
	}

	/**
	 * Finds a source by the name given among sources belonging to the current
	 * application user.
	 * 
	 * @param name
	 *            the name of the source to find
	 * @return the source found or null if no source found
	 */
	public Source findByName(String name) {
		Source result;
		CriteriaBuilder cb;
		CriteriaQuery<Source> criteria;
		Root<Source> sourceRoot;

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(Source.class);
		sourceRoot = criteria.from(Source.class);

		criteria.select(sourceRoot);
		criteria.where(cb.and(
				cb.equal(sourceRoot.get("userId"), sessionUser.getId()),
				cb.equal(sourceRoot.get("name"), name)));

		try {
			result = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException ex) {
			result = null;
		}

		return result;
	}

	/**
	 * Finds a source by the rotor value given among sources belonging to the
	 * current application user.
	 * 
	 * @param rotor
	 *            the rotor value of the source to find
	 * @return the source found or null if no sources found
	 */
	public Source findByRotor(int rotor) {
		Source result;
		CriteriaBuilder cb;
		CriteriaQuery<Source> criteria;
		Root<Source> sourceRoot;

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(Source.class);
		sourceRoot = criteria.from(Source.class);

		criteria.select(sourceRoot);
		criteria.where(cb.and(
				cb.equal(sourceRoot.get("userId"), sessionUser.getId()),
				cb.equal(sourceRoot.get("rotor"), rotor)));

		try {
			result = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException ex) {
			result = null;
		}

		return result;
	}

	/**
	 * Finds a source by ID.
	 * 
	 * @param id
	 *            the ID of the source to find
	 * @return the source found or null if no source found
	 */
	public Source findById(long id) {

		return em.find(Source.class, id);
	}

	/**
	 * Finds a source which resides in the list built by findAll method right
	 * before the source with ID given.
	 * 
	 * @param id
	 *            the ID of the source for which the previous source has to be
	 *            found
	 * @return the previous source or null if there's no previous source
	 */
	public Source findPrevious(long id) {
		Source result;
		Source previous;
		List<Source> sources;

		result = null;
		previous = null;

		sources = findAll();

		for (Source source : sources) {
			if (source.getId() == id) {
				result = previous;
				break;
			}
			previous = source;
		}

		return result;
	}

	/**
	 * Adds the source to the persisted list of sources (stores it in the
	 * database).
	 * 
	 * @param source
	 *            the source to add
	 */
	public void add(Source source) {
		Source mergedSource;

		source.setUserId(sessionUser.getId());
		mergedSource = em.merge(source);
		em.flush();
		source.setId(mergedSource.getId());
	}

	/**
	 * Updates the source in the persisted list of sources (updates it in the
	 * database).
	 * 
	 * @param source
	 *            the source to update
	 */
	public void update(Source source) {
		em.merge(source);
	}

	/**
	 * Deletes the source from the persisted list of sources (deletes it from
	 * the database). Also deletes all transponders related to the source.
	 * 
	 * @param source
	 *            the source to delete
	 */
	public void delete(Source source) {
		List<Transponder> transponders;

		transponders = transponderRepository.findAll(source.getId());

		for (Transponder transponder : transponders) {
			transponderRepository.delete(transponder);
		}

		em.remove(em.merge(source));
	}

}
