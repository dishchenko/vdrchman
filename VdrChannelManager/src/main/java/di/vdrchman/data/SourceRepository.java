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

	public Source findById(long id) {
		return em.find(Source.class, id);
	}

	public Source findPrevious(long id) {
		Source result;
		Source previous;
		List<Source> sources;

		result = null;
		previous = null;

		sources = findAll();

		for (Source source : sources)
		{
			if (source.getId() == id)
			{
				result = previous;
				break;
			}
			previous = source;
		}

		return result;
	}

	public void add(Source source) {
		source.setUserId(user.getId());
		em.persist(source);
		em.flush();
	}

	public void update(Source source) {
		em.merge(source);
	}

	public void delete(Source source) {
		em.remove(em.merge(source));
	}

}
