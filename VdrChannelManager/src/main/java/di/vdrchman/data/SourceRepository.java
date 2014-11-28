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
import di.vdrchman.data.User;

@Stateless
@Named
public class SourceRepository {

	@Inject
	private EntityManager em;

	@Inject
	private User user;

	/**
	 * Builds a list of Sources belonging to the current application user.
	 * First adds Sources with no rotor value defined
	 * in ascending name order.
	 * Then adds Sources in ascending rotor value order.
	 * 
	 * @return the list of all Sources for the current application user
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
		criteria.where(cb.and(cb.equal(sourceRoot.get("userId"), user.getId()),
				cb.isNull(sourceRoot.get("rotor"))));
		criteria.orderBy(cb.asc(sourceRoot.get("name")));
		result.addAll(em.createQuery(criteria).getResultList());

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(Source.class);
		sourceRoot = criteria.from(Source.class);
		criteria.select(sourceRoot);
		criteria.where(cb.and(cb.equal(sourceRoot.get("userId"), user.getId()),
				cb.isNotNull(sourceRoot.get("rotor"))));
		criteria.orderBy(cb.asc(sourceRoot.get("rotor")));
		result.addAll(em.createQuery(criteria).getResultList());

		return result;
	}

	/**
	 * Finds a Source by the name given among the Sources belonging to
	 * the current application user.
	 * 
	 * @param name the name of the Source to find
	 * @return the Source found or null if no Sources found
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
		criteria.where(cb.and(cb.equal(sourceRoot.get("userId"), user.getId()),
				cb.equal(sourceRoot.get("name"), name)));

		try {
			result = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException ex) {
			result = null;
		}

		return result;
	}

	/**
	 * Finds a Source by the rotor value given among the Sources belonging to
	 * the current application user.
	 * 
	 * @param rotor the rotor value of the Source to find
	 * @return the Source found or null if no Sources found
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
		criteria.where(cb.and(cb.equal(sourceRoot.get("userId"), user.getId()),
				cb.equal(sourceRoot.get("rotor"), rotor)));

		try {
			result = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException ex) {
			result = null;
		}

		return result;
	}

	/**
	 * Finds a Source by ID.
	 * 
	 * @param id the ID of the Source to find
	 * @return the Source found or null if no Sources found
	 */
	public Source findById(long id) {

		return em.find(Source.class, id);
	}

	/**
	 * Finds a Source which resides in the list built by findAll method
	 * right before the Source with ID given.
	 * 
	 * @param id the ID of the Source for which the previous Source has to be found
	 * @return the previous Source or null if there's no previous Source
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
	 * Adds the Source to the persisted list of Sources
	 * (stores it in the database).
	 * 
	 * @param source the Source to add
	 */
	public void add(Source source) {
		source.setUserId(user.getId());
		em.persist(source);
		em.flush();
	}

	/**
	 * Updates the Source in the persisted list of Sources
	 * (updates it in the database).
	 * 
	 * @param source the Source to update
	 */
	public void update(Source source) {
		em.merge(source);
	}

	/**
	 * Deletes the Source from the persisted list of Sources
	 * (deletes it from the database).
	 * 
	 * @param source the Source to delete
	 */
	public void delete(Source source) {
		em.remove(em.merge(source));
	}

}
