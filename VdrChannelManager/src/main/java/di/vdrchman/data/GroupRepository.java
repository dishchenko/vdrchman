package di.vdrchman.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import di.vdrchman.model.Group;

@Stateless
@Named
public class GroupRepository {

	@Inject
	private EntityManager em;

	@Inject
	private SessionUser sessionUser;

	/**
	 * Builds a list of groups belonging to the current application user. Adds
	 * groups in ascending description order.
	 * 
	 * @return the list of all groups for the current application user
	 */
	public List<Group> findAll() {
		CriteriaBuilder cb;
		CriteriaQuery<Group> criteria;
		Root<Group> groupRoot;

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(Group.class);
		groupRoot = criteria.from(Group.class);

		criteria.select(groupRoot);
		criteria.where(cb.equal(groupRoot.get("userId"), sessionUser.getId()));
		criteria.orderBy(cb.asc(groupRoot.get("description")));

		return em.createQuery(criteria).getResultList();
	}

	/**
	 * Finds a group by the name given among groups belonging to the current
	 * application user.
	 * 
	 * @param name
	 *            the name of the group to find
	 * @return the group found or null if no group found
	 */
	public Group findByName(String name) {
		Group result;
		CriteriaBuilder cb;
		CriteriaQuery<Group> criteria;
		Root<Group> groupRoot;

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(Group.class);
		groupRoot = criteria.from(Group.class);

		criteria.select(groupRoot);
		criteria.where(cb.and(
				cb.equal(groupRoot.get("userId"), sessionUser.getId()),
				cb.equal(groupRoot.get("name"), name)));

		try {
			result = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException ex) {
			result = null;
		}

		return result;
	}

	/**
	 * Finds a group by the starting channel number value given among groups
	 * belonging to the current application user.
	 * 
	 * @param startChannelNo
	 *            the starting channel number value of the group to find
	 * @return the group found or null if no group found
	 */
	public Group findByStartChannelNo(int startChannelNo) {
		Group result;
		CriteriaBuilder cb;
		CriteriaQuery<Group> criteria;
		Root<Group> groupRoot;

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(Group.class);
		groupRoot = criteria.from(Group.class);

		criteria.select(groupRoot);
		criteria.where(cb.and(
				cb.equal(groupRoot.get("userId"), sessionUser.getId()),
				cb.equal(groupRoot.get("startChannelNo"), startChannelNo)));

		try {
			result = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException ex) {
			result = null;
		}

		return result;
	}

	/**
	 * Finds a group by ID.
	 * 
	 * @param id
	 *            the ID of the group to find
	 * @return the group found or null if no group found
	 */
	public Group findById(long id) {

		return em.find(Group.class, id);
	}

	/**
	 * Adds the group to the persisted list of groups (stores it in the
	 * database).
	 * 
	 * @param group
	 *            the group to add
	 */
	public void add(Group group) {
		Group mergedGroup;

		group.setUserId(sessionUser.getId());
		mergedGroup = em.merge(group);
		em.flush();
		group.setId(mergedGroup.getId());
	}

	/**
	 * Updates the group in the persisted list of groups (updates it in the
	 * database).
	 * 
	 * @param group
	 *            the group to update
	 */
	public void update(Group group) {
		em.merge(group);
	}

	/**
	 * Deletes the group from the persisted list of groups (deletes it from the
	 * database).
	 * 
	 * @param group
	 *            the group to delete
	 */
	public void delete(Group group) {
		em.remove(em.merge(group));
	}

}
