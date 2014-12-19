package di.vdrchman.data;

import static di.vdrchman.util.Tools.*;

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
import di.vdrchman.model.ChannelGroup;
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
	 * application user depending on values of sourceId, transpId and
	 * comparisonFilter parameters. Channels are added to the list in ascending
	 * sequence number order.
	 * 
	 * @param sourceId
	 *            the ID of the source which channels are added to the list, if
	 *            both sourceId and transpId are negative then all current
	 *            user's channels are added
	 * @param transpId
	 *            the ID of the transponder which channels are added to the
	 *            list, if transpId is negative then sourceId value is taken
	 *            into account
	 * @param comparisonFilter
	 *            the variant of result list filtering based on comparison with
	 *            scanned channel list (COMPARISON_NONE - no filtering,
	 *            COMPARISON_CHANGED_MAIN - changed channels (compared to the
	 *            channels from scanned channel list), COMPARISON_NOT_SCANNED -
	 *            channels not found in the scanned channel list)
	 * @return the list of channels found for the current application user and
	 *         given source and transponder IDs
	 */
	public List<Channel> findAll(long sourceId, long transpId,
			int comparisonFilter) {
		List<Channel> result;
		TypedQuery<Object[]> query;
		List<Object[]> queryResult;

		switch (comparisonFilter) {
		case COMPARISON_NONE:
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
			break;
		case COMPARISON_CHANGED_MAIN:
			if (transpId < 0) {
				if (sourceId < 0) {
					query = em
							.createQuery(
									"select c, cs.seqno from Channel c, ChannelSeqno cs where c.id = cs.channelId and cs.userId = :userId and exists (select sc from Source s, Transponder t, ScannedChannel sc where c.transpId = t.id and t.sourceId = s.id and sc.sourceName = s.name and sc.frequency = t.frequency and sc.polarization = t.polarization and coalesce(sc.streamId, 0) = coalesce(t.streamId, 0) and sc.sid = c.sid and sc.apid = c.apid and (t.dvbsGen <> sc.dvbsGen or t.symbolRate <> sc.symbolRate or coalesce(t.nid, 0) <> coalesce(sc.nid, 0) or coalesce(t.tid, 0) <> coalesce(sc.tid, 0) or c.vpid <> coalesce(sc.vpid, 0) or coalesce(c.venc, 0) <> sc.venc or coalesce(c.pcr, 0) <> coalesce(sc.pcr, 0) or coalesce(c.aenc, 0) <> sc.aenc or coalesce(c.tpid, 0) <> coalesce(sc.tpid, 0) or coalesce(c.caid, ' ') <> coalesce(sc.caid, ' ') or coalesce(c.rid, 0) <> coalesce(sc.rid, 0) or coalesce(c.scannedName, ' ') <> coalesce(sc.scannedName, ' ') or coalesce(c.providerName, ' ') <> coalesce(sc.providerName, ' ')) and sc.userId = :userId) order by cs.seqno",
									Object[].class);
					query.setParameter("userId", user.getId());
				} else {
					query = em
							.createQuery(
									"select c, cs.seqno from Channel c, ChannelSeqno cs, Transponder o_t where c.id = cs.channelId and c.transpId = o_t.id and cs.userId = :userId and o_t.sourceId = :sourceId and exists (select sc from Source s, Transponder t, ScannedChannel sc where c.transpId = t.id and t.sourceId = s.id and sc.sourceName = s.name and sc.frequency = t.frequency and sc.polarization = t.polarization and coalesce(sc.streamId, 0) = coalesce(t.streamId, 0) and sc.sid = c.sid and sc.apid = c.apid and (t.dvbsGen <> sc.dvbsGen or t.symbolRate <> sc.symbolRate or coalesce(t.nid, 0) <> coalesce(sc.nid, 0) or coalesce(t.tid, 0) <> coalesce(sc.tid, 0) or c.vpid <> coalesce(sc.vpid, 0) or coalesce(c.venc, 0) <> sc.venc or coalesce(c.pcr, 0) <> coalesce(sc.pcr, 0) or coalesce(c.aenc, 0) <> sc.aenc or coalesce(c.tpid, 0) <> coalesce(sc.tpid, 0) or coalesce(c.caid, ' ') <> coalesce(sc.caid, ' ') or coalesce(c.rid, 0) <> coalesce(sc.rid, 0) or coalesce(c.scannedName, ' ') <> coalesce(sc.scannedName, ' ') or coalesce(c.providerName, ' ') <> coalesce(sc.providerName, ' ')) and sc.userId = :userId) order by cs.seqno",
									Object[].class);
					query.setParameter("userId", user.getId());
					query.setParameter("sourceId", sourceId);
				}
			} else {
				query = em
						.createQuery(
								"select c, cs.seqno from Channel c, ChannelSeqno cs where c.id = cs.channelId and cs.userId = :userId and c.transpId = :transpId and exists (select sc from Source s, Transponder t, ScannedChannel sc where c.transpId = t.id and t.sourceId = s.id and sc.sourceName = s.name and sc.frequency = t.frequency and sc.polarization = t.polarization and coalesce(sc.streamId, 0) = coalesce(t.streamId, 0) and sc.sid = c.sid and sc.apid = c.apid and (t.dvbsGen <> sc.dvbsGen or t.symbolRate <> sc.symbolRate or coalesce(t.nid, 0) <> coalesce(sc.nid, 0) or coalesce(t.tid, 0) <> coalesce(sc.tid, 0) or c.vpid <> coalesce(sc.vpid, 0) or coalesce(c.venc, 0) <> sc.venc or coalesce(c.pcr, 0) <> coalesce(sc.pcr, 0) or coalesce(c.aenc, 0) <> sc.aenc or coalesce(c.tpid, 0) <> coalesce(sc.tpid, 0) or coalesce(c.caid, ' ') <> coalesce(sc.caid, ' ') or coalesce(c.rid, 0) <> coalesce(sc.rid, 0) or coalesce(c.scannedName, ' ') <> coalesce(sc.scannedName, ' ') or coalesce(c.providerName, ' ') <> coalesce(sc.providerName, ' ')) and sc.userId = :userId) order by cs.seqno",
								Object[].class);
				query.setParameter("userId", user.getId());
				query.setParameter("transpId", transpId);
			}
			break;
		case COMPARISON_NOT_SCANNED:
			if (transpId < 0) {
				if (sourceId < 0) {
					query = em
							.createQuery(
									"select c, cs.seqno from Channel c, ChannelSeqno cs where c.id = cs.channelId and cs.userId = :userId and not exists (select sc from Source s, Transponder t, ScannedChannel sc where c.transpId = t.id and t.sourceId = s.id and sc.sourceName = s.name and sc.frequency = t.frequency and sc.polarization = t.polarization and coalesce(sc.streamId, 0) = coalesce(t.streamId, 0) and sc.sid = c.sid and sc.apid = c.apid and sc.userId = :userId) order by cs.seqno",
									Object[].class);
					query.setParameter("userId", user.getId());
				} else {
					query = em
							.createQuery(
									"select c, cs.seqno from Channel c, ChannelSeqno cs, Transponder o_t where c.id = cs.channelId and c.transpId = o_t.id and cs.userId = :userId and o_t.sourceId = :sourceId and not exists (select sc from Source s, Transponder t, ScannedChannel sc where c.transpId = t.id and t.sourceId = s.id and sc.sourceName = s.name and sc.frequency = t.frequency and sc.polarization = t.polarization and coalesce(sc.streamId, 0) = coalesce(t.streamId, 0) and sc.sid = c.sid and sc.apid = c.apid and sc.userId = :userId) order by cs.seqno",
									Object[].class);
					query.setParameter("userId", user.getId());
					query.setParameter("sourceId", sourceId);
				}
			} else {
				query = em
						.createQuery(
								"select c, cs.seqno from Channel c, ChannelSeqno cs where c.id = cs.channelId and cs.userId = :userId and c.transpId = :transpId and not exists (select sc from Source s, Transponder t, ScannedChannel sc where c.transpId = t.id and t.sourceId = s.id and sc.sourceName = s.name and sc.frequency = t.frequency and sc.polarization = t.polarization and coalesce(sc.streamId, 0) = coalesce(t.streamId, 0) and sc.sid = c.sid and sc.apid = c.apid and sc.userId = :userId) order by cs.seqno",
								Object[].class);
				query.setParameter("userId", user.getId());
				query.setParameter("transpId", transpId);
			}
			break;
		default:
			throw new IllegalArgumentException(
					"Wrong 'comparisonFilter' value: " + comparisonFilter);
		}

		queryResult = query.getResultList();

		result = new ArrayList<Channel>();

		for (Object[] row : queryResult) {
			result.add((Channel) row[0]);
		}

		return result;
	}

	/**
	 * Finds a channel by the combination of transponder ID, SID and APID given
	 * among the channels belonging to the current application user.
	 * 
	 * @param transpId
	 *            the transponder ID to find a channel within
	 * @param sid
	 *            the SID of channel to find
	 * @param apid
	 *            the APID of channel to find
	 * @return the channel found or null if no channel found
	 */
	public Channel findByTransponderSidApid(long transpId, int sid, int apid) {
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
	public Integer findSeqno(Channel channel) {
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
	 * Returns a sequence number of the channel in the group given.
	 * 
	 * @param channelId
	 *            the ID of the channel to get the sequence number for
	 * @param groupId
	 *            the group to get the sequence number of the channel in
	 * @return the sequence number or null if no channel found
	 */
	public Integer findSeqno(long channelId, long groupId) {
		TypedQuery<Integer> query;

		query = em
				.createQuery(
						"select cg.seqno from ChannelGroup cg where cg.channelId = :channelId and cg.groupId = :groupId",
						Integer.class);
		query.setParameter("channelId", channelId);
		query.setParameter("groupId", groupId);

		return query.getSingleResult();
	}

	/**
	 * Builds a list of channels in the group with given ID belonging to the
	 * current application user. Channels are added to the list in ascending
	 * sequence number order.
	 * 
	 * @param groupId
	 *            the ID of the group which channels are added to the list
	 * @return the list of channels found in the group for the current
	 *         application user
	 */
	public List<Channel> findAllInGroup(long groupId) {
		List<Channel> result;
		TypedQuery<Object[]> query;
		List<Object[]> queryResult;

		query = em
				.createQuery(
						"select c, cg.seqno from Channel c, ChannelGroup cg where c.id = cg.channelId and cg.groupId = :groupId order by cg.seqno",
						Object[].class);
		query.setParameter("groupId", groupId);

		queryResult = query.getResultList();

		result = new ArrayList<Channel>();

		for (Object[] row : queryResult) {
			result.add((Channel) row[0]);
		}

		return result;
	}

	/**
	 * Builds a list of groups which the channel with given ID is a member of.
	 * 
	 * @param channelId
	 *            the ID of the channel to build the list of groups for
	 * @return the list of groups which the channel is a member of
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
	 * Finds maximum sequence number of channel in the group with ID given.
	 * 
	 * @param groupId
	 *            the group ID to find a channel's maximum sequence number
	 * @return the maximum channel sequence number or null if no channels found
	 */
	public Integer findMaxGroupSeqno(long groupId) {
		TypedQuery<Integer> query;

		query = em
				.createQuery(
						"select max(cg.seqno) from ChannelGroup cg where cg.groupId = :groupId",
						Integer.class);
		query.setParameter("groupId", groupId);

		return query.getSingleResult();
	}

	/**
	 * Adds the channel to the persisted list of channels (stores it in the
	 * database).
	 * 
	 * @param channel
	 *            the channel to add
	 * @param seqno
	 *            the sequence number of the added channel
	 */
	public void add(Channel channel, int seqno) {

		em.persist(channel);
		em.flush();

		move(channel, seqno);
	}

	/**
	 * Updates the channel in the persisted list of channels (updates it in the
	 * database).
	 * 
	 * @param channel
	 *            the channel to update
	 */
	public void update(Channel channel) {
		em.merge(channel);
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

	/**
	 * Deletes the channel from the persisted list of channels (deletes it from
	 * the database).
	 * 
	 * @param channel
	 *            the channel to delete
	 */
	public void delete(Channel channel) {
		ChannelSeqno channelSeqno;
		int seqno;
		Query query;

		channelSeqno = em.find(ChannelSeqno.class, channel.getId());

		if (channelSeqno != null) {
			seqno = channelSeqno.getSeqno();

			em.remove(channelSeqno);

			query = em
					.createQuery("update ChannelSeqno cs set cs.seqno = -cs.seqno where cs.userId = :userId and cs.seqno > :seqno");
			query.setParameter("userId", user.getId());
			query.setParameter("seqno", seqno);
			query.executeUpdate();

			query = em
					.createQuery("update ChannelSeqno cs set cs.seqno = -cs.seqno - 1 where cs.userId = :userId and cs.seqno < -:seqno");
			query.setParameter("userId", user.getId());
			query.setParameter("seqno", seqno);
			query.executeUpdate();
		}

		em.remove(em.merge(channel));
	}

	/**
	 * Adds the channel to group relation to the persisted list of relations
	 * (stores it in the database).
	 * 
	 * @param channelId
	 *            the channel ID to add relation for
	 * @param groupId
	 *            the group ID to add relation for
	 * @param seqno
	 *            the sequence number of the added relation
	 */
	public void add(long channelId, long groupId, int seqno) {
		Query query;
		ChannelGroup channelGroup;

		query = em
				.createQuery("update ChannelGroup cg set cg.seqno = -cg.seqno where cg.groupId = :groupId and cg.seqno >= :seqno");
		query.setParameter("groupId", groupId);
		query.setParameter("seqno", seqno);
		query.executeUpdate();

		query = em
				.createQuery("update ChannelGroup cg set cg.seqno = -cg.seqno + 1 where cg.groupId = :groupId and cg.seqno <= -:seqno");
		query.setParameter("groupId", groupId);
		query.setParameter("seqno", seqno);
		query.executeUpdate();

		channelGroup = new ChannelGroup();

		channelGroup.setChannelId(channelId);
		channelGroup.setGroupId(groupId);
		channelGroup.setSeqno(seqno);

		em.persist(channelGroup);
	}

	/**
	 * Moves the channel to group relation to the new sequence number in the
	 * persisted list of relations.
	 * 
	 * @param channelId
	 *            the channel ID of the relation to move
	 * @param groupId
	 *            the group ID of the relation to move
	 * @param seqno
	 *            the new sequence number
	 */
	public void move(long channelId, long groupId, int seqno) {
		TypedQuery<ChannelGroup> query;
		ChannelGroup channelGroup;

		query = em
				.createQuery(
						"select cg from ChannelGroup cg where cg.channelId = :channelId and cg.groupId = :groupId",
						ChannelGroup.class);
		query.setParameter("channelId", channelId);
		query.setParameter("groupId", groupId);

		channelGroup = query.getSingleResult();

		moveMerged(channelGroup, seqno);
	}

	/**
	 * Moves the channel to group relation to the new sequence number in the
	 * persisted list of relations.
	 * 
	 * @param channelGroup
	 *            the channel to group relation to move
	 * @param seqno
	 *            the new sequence number
	 */
	public void move(ChannelGroup channelGroup, int seqno) {
		moveMerged(em.merge(channelGroup), seqno);

		channelGroup.setSeqno(seqno);
	}

	/**
	 * Deletes the channel to group relation from the persisted list of
	 * relations (deletes it from the database).
	 * 
	 * @param channelId
	 *            the channel ID of the relation to delete
	 * @param groupId
	 *            the group ID of the relation to delete
	 */
	public void delete(long channelId, long groupId) {
		TypedQuery<ChannelGroup> query;
		ChannelGroup channelGroup;

		query = em
				.createQuery(
						"select cg from ChannelGroup cg where cg.channelId = :channelId and cg.groupId = :groupId",
						ChannelGroup.class);
		query.setParameter("channelId", channelId);
		query.setParameter("groupId", groupId);

		channelGroup = query.getSingleResult();

		deleteMerged(channelGroup);
	}

	/**
	 * Deletes the channel to group relation from the persisted list of
	 * relations (deletes it from the database).
	 * 
	 * @param channelGroup
	 *            the channel to group relation to delete
	 */
	public void delete(ChannelGroup channelGroup) {
		deleteMerged(em.merge(channelGroup));
	}

	/**
	 * Updates groups which the channel with given ID is a member of.
	 * 
	 * @param channelId
	 *            the ID of the channel to update groups for
	 */
	public void updateGroups(long channelId, List<Group> groups) {
		TypedQuery<ChannelGroup> query;
		List<ChannelGroup> curChannelGroups;
		boolean isMember;
		long curGroupId;
		long groupId;
		Integer maxGroupSeqno;
		int seqno;

		query = em
				.createQuery(
						"select cg from ChannelGroup cg where cg.channelId = :channelId",
						ChannelGroup.class);
		query.setParameter("channelId", channelId);

		curChannelGroups = query.getResultList();

		for (ChannelGroup curChannelGroup : curChannelGroups) {
			curGroupId = curChannelGroup.getGroupId();

			isMember = false;
			for (Group group : groups) {
				if (group.getId() == curGroupId) {
					isMember = true;
					break;
				}
			}

			if (!isMember) {
				deleteMerged(curChannelGroup);
			}
		}

		for (Group group : groups) {
			groupId = group.getId();

			isMember = false;
			for (ChannelGroup curChannelGroup : curChannelGroups) {
				if (curChannelGroup.getGroupId() == groupId) {
					isMember = true;
					break;
				}
			}

			if (!isMember) {
				maxGroupSeqno = findMaxGroupSeqno(group.getId());

				if (maxGroupSeqno != null) {
					seqno = maxGroupSeqno + 1;
				} else {
					seqno = 1;
				}

				add(channelId, groupId, seqno);
			}
		}
	}

	// Move the (merged into EM) channel to group relation to the new sequence
	// number
	private void moveMerged(ChannelGroup mergedChannelGroup, int seqno) {
		long groupId;
		int curSeqno;
		Query query;

		groupId = mergedChannelGroup.getGroupId();
		curSeqno = mergedChannelGroup.getSeqno();

		mergedChannelGroup.setSeqno(0);

		query = em
				.createQuery("update ChannelGroup cg set cg.seqno = -cg.seqno where cg.groupId = :groupId and cg.seqno > :curSeqno");
		query.setParameter("groupId", groupId);
		query.setParameter("curSeqno", curSeqno);
		query.executeUpdate();

		query = em
				.createQuery("update ChannelGroup cg set cg.seqno = -cg.seqno - 1 where cg.groupId = :groupId and cg.seqno < -:curSeqno");
		query.setParameter("groupId", groupId);
		query.setParameter("curSeqno", curSeqno);
		query.executeUpdate();

		query = em
				.createQuery("update ChannelGroup cg set cg.seqno = -cg.seqno where cg.groupId = :groupId and cg.seqno >= :seqno");
		query.setParameter("groupId", groupId);
		query.setParameter("seqno", seqno);
		query.executeUpdate();

		query = em
				.createQuery("update ChannelGroup cg set cg.seqno = -cg.seqno + 1 where cg.groupId = :groupId and cg.seqno <= -:seqno");
		query.setParameter("groupId", groupId);
		query.setParameter("seqno", seqno);
		query.executeUpdate();

		mergedChannelGroup.setSeqno(seqno);
	}

	// Delete the (merged into EM) channel to group relation
	private void deleteMerged(ChannelGroup mergedChannelGroup) {
		long groupId;
		int seqno;
		Query query;

		groupId = mergedChannelGroup.getGroupId();
		seqno = mergedChannelGroup.getSeqno();

		em.remove(mergedChannelGroup);

		query = em
				.createQuery("update ChannelGroup cg set cg.seqno = -cg.seqno where cg.groupId = :groupId and cg.seqno > :seqno");
		query.setParameter("groupId", groupId);
		query.setParameter("seqno", seqno);
		query.executeUpdate();

		query = em
				.createQuery("update ChannelGroup cg set cg.seqno = -cg.seqno - 1 where cg.groupId = :groupId and cg.seqno < -:seqno");
		query.setParameter("groupId", groupId);
		query.setParameter("seqno", seqno);
		query.executeUpdate();
	}

}
