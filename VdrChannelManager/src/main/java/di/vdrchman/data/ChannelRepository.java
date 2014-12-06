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

import di.vdrchman.model.Channel;
import di.vdrchman.model.ChannelSeqno;
import di.vdrchman.model.Group;

@Stateless
@Named
public class ChannelRepository {

	@Inject
	private EntityManager em;

	@Inject
	private User user;

	/**
	 * Builds a full or partial list of channels belonging to the current
	 * application user depending on values of sourceId and transpId parameters.
	 * Channels are added to the list in ascending sequence number order.
	 * 
	 * @param sourceId
	 *            the ID of the source which channels are added to the list, if
	 *            both sourceId and transpId are negative then all current
	 *            user's channels are added
	 * @param transpId
	 *            the ID of the transponder which channels are added to the
	 *            list, if transpId is negative then sourceId value is taken
	 *            into account
	 * @return the list of channels found for the current application user and
	 *         given source and transponder IDs
	 */
	public List<Channel> findAll(long sourceId, long transpId) {
		List<Channel> result;
		TypedQuery<Object[]> query;
		List<Object[]> queryResult;

		if (transpId < 0) {
			if (sourceId < 0) {
				query = em
						.createQuery(
								"select c, cs.seqno from Channel c, ChannelSeqno cs where c.id = cs.channelId and cs.userId = :userId order by cs.seqno",
								Object[].class);
				query.setParameter("userId", user.getId());
			} else {
				query = em
						.createQuery(
								"select c, cs.seqno from Channel c, ChannelSeqno cs, Transponder t where c.id = cs.channelId and c.transpId = t.id and cs.userId = :userId and t.sourceId = :sourceId order by cs.seqno",
								Object[].class);
				query.setParameter("userId", user.getId());
				query.setParameter("sourceId", sourceId);
			}
		} else {
			query = em
					.createQuery(
							"select c, cs.seqno from Channel c, ChannelSeqno cs where c.id = cs.channelId and cs.userId = :userId and c.transpId = :transpId order by cs.seqno",
							Object[].class);
			query.setParameter("userId", user.getId());
			query.setParameter("transpId", transpId);
		}

		queryResult = query.getResultList();

		result = new ArrayList<Channel>();

		for (Object[] row : queryResult) {
			result.add((Channel) row[0]);
		}

		return result;
	}

	/**
	 * Finds a channel by the combination of transponder ID, SID and
	 * APID given among the channels belonging to the current
	 * application user.
	 * 
	 * @param transpId
	 *            the transponder ID to find a channel within
	 * @param sid
	 *            the SID of channel to find
	 * @param apid
	 *            the APID of channel to find
	 * @return the channel found or null if no channel found
	 */
	public Channel findByTransponderSidApid(long transpId,
			int sid, int apid) {
		Channel result;
		CriteriaBuilder cb;
		CriteriaQuery<Channel> criteria;
		Root<Channel> channelRoot;
		Predicate p;

		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(Channel.class);
		channelRoot = criteria.from(Channel.class);
		criteria.select(channelRoot);
		p = cb.conjunction();
		p = cb.and(p, cb.equal(channelRoot.get("transpId"), transpId));
		p = cb.and(p, cb.equal(channelRoot.get("sid"), sid));
		p = cb.and(p, cb.equal(channelRoot.get("apid"), apid));
		criteria.where(p);

		try {
			result = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException ex) {
			result = null;
		}

		return result;
	}

	/**
	 * Finds a channel by ID.
	 * 
	 * @param id
	 *            the ID of the channel to find
	 * @return the channel found or null if no channel found
	 */
	public Channel findById(long id) {

		return em.find(Channel.class, id);
	}

	/**
	 * Finds maximum sequence number of channel related to the transponder with
	 * ID given.
	 * 
	 * @param transpId
	 *            the transponder ID to find a channel's maximum sequence number
	 * @return the maximum channel sequence number or null if no channels found
	 */
	public Integer findMaxSeqno(long transpId) {
		TypedQuery<Integer> query;

		if (transpId < 0) {
			query = em
					.createQuery(
							"select max(cs.seqno) from ChannelSeqno cs, Channel c where c.id = cs.channelId and cs.userId = :userId",
							Integer.class);
			query.setParameter("userId", user.getId());
		} else {
			query = em
					.createQuery(
							"select max(cs.seqno) from ChannelSeqno cs, Channel c where c.id = cs.channelId and cs.userId = :userId and c.transpId = :transpId",
							Integer.class);
			query.setParameter("userId", user.getId());
			query.setParameter("transpId", transpId);
		}

		return query.getSingleResult();
	}

	/**
	 * Returns a sequence number of the channel given.
	 * 
	 * @param channel
	 *            the channel to get the sequence number for
	 * @return the sequence number or null if no channel found
	 */
	public Integer getSeqno(Channel channel) {
		Integer result;
		ChannelSeqno channelSeqno;

		result = null;

		channelSeqno = em.find(ChannelSeqno.class, channel.getId());
		if (channelSeqno != null) {
			result = channelSeqno.getSeqno();
		}

		return result;
	}

	/**
	 * Builds a list of groups in which the channel with given ID is present.
	 * 
	 * @param channelId
	 *            the ID of the channel to build the list of groups for
	 * @return the list of groups in which the channel is present
	 */
	public List<Group> findGroups(long channelId) {
		TypedQuery<Group> query;

		query = em
				.createQuery(
						"select g from Group g where g.id in (select cg.groupId from ChannelGroup cg where cg.channelId = :channelId) order by g.description",
						Group.class);
		query.setParameter("channelId", channelId);

		return query.getResultList();
	}

	/**
	 * Moves the channel to the new sequence number in the list of channels of
	 * the current application user.
	 * 
	 * @param channel
	 *            the channel to move
	 * @param seqno
	 *            the new sequence number
	 */
	public void move(Channel channel, int seqno) {
		ChannelSeqno channelSeqno;
		int curSeqno;
		Query query;

		channelSeqno = em.find(ChannelSeqno.class, channel.getId());

		if (channelSeqno != null) {
			curSeqno = channelSeqno.getSeqno();
			channelSeqno.setSeqno(0);

			query = em
					.createQuery("update ChannelSeqno cs set cs.seqno = -cs.seqno where cs.userId = :userId and cs.seqno > :curSeqno");
			query.setParameter("userId", user.getId());
			query.setParameter("curSeqno", curSeqno);
			query.executeUpdate();

			query = em
					.createQuery("update ChannelSeqno cs set cs.seqno = -cs.seqno - 1 where cs.userId = :userId and cs.seqno < -:curSeqno");
			query.setParameter("userId", user.getId());
			query.setParameter("curSeqno", curSeqno);
			query.executeUpdate();
		}

		query = em
				.createQuery("update ChannelSeqno cs set cs.seqno = -cs.seqno where cs.userId = :userId and cs.seqno >= :seqno");
		query.setParameter("userId", user.getId());
		query.setParameter("seqno", seqno);
		query.executeUpdate();

		query = em
				.createQuery("update ChannelSeqno cs set cs.seqno = -cs.seqno + 1 where cs.userId = :userId and cs.seqno <= -:seqno");
		query.setParameter("userId", user.getId());
		query.setParameter("seqno", seqno);
		query.executeUpdate();

		if (channelSeqno != null) {
			channelSeqno.setSeqno(seqno);
		} else {
			channelSeqno = new ChannelSeqno();
			channelSeqno.setChannelId(channel.getId());
			channelSeqno.setUserId(user.getId());
			channelSeqno.setSeqno(seqno);
			em.persist(channelSeqno);
		}
	}

}
