package di.vdrchman.data;

import static di.vdrchman.util.Tools.*;

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
	 * current application user depending on values of sourceId, transpId and
	 * comparisonFilter parameters. Channels are added to the list in ascending
	 * ID order.
	 * 
	 * @param sourceId
	 *            the ID of the source which scanned channels are added to the
	 *            list, if both sourceId and transpId are negative then all
	 *            current user's channels are added
	 * @param transpId
	 *            the ID of the transponder which scanned channels are added to
	 *            the list, if transpId is negative then sourceId value is taken
	 *            into account
	 * @param comparisonFilter
	 *            the variant of result list filtering based on comparison with
	 *            main and ignored channel lists (COMPARISON_NONE - no
	 *            filtering, COMPARISON_NEW - new channels (not found in main
	 *            and ignore channel lists), COMPARISON_CHANGED_MAIN - changed
	 *            channels (compared to the channels from main list),
	 *            COMPARISON_CHANGED_IGNORED - changed channels (compared to the
	 *            channels from ignored list))
	 * @return the list of scanned channels found for the current application
	 *         user and given source and transponder IDs
	 */
	public List<ScannedChannel> findAll(long sourceId, long transpId,
			int comparisonFilter) {
		TypedQuery<ScannedChannel> query;

		switch (comparisonFilter) {
		case COMPARISON_NONE:
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
			break;
		case COMPARISON_NEW:
			if (transpId < 0) {
				if (sourceId < 0) {
					query = em
							.createQuery(
									"select sc from ScannedChannel sc where sc.userId = :userId and not exists (select c from Source s, Transponder t, Channel c where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and c.sid = sc.sid and c.apid = sc.apid and t.id = c.transpId and s.userId = :userId) and not exists (select ic from Source s, Transponder t, IgnoredChannel ic where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and ic.sid = sc.sid and ic.apid = sc.apid and t.id = ic.transpId and s.userId = :userId) order by sc.id",
									ScannedChannel.class);
					query.setParameter("userId", user.getId());
				} else {
					query = em
							.createQuery(
									"select sc from ScannedChannel sc, Source o_s where sc.sourceName = o_s.name and o_s.id = :sourceId and sc.userId = :userId and not exists (select c from Source s, Transponder t, Channel c where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and c.sid = sc.sid and c.apid = sc.apid and t.id = c.transpId and s.userId = :userId) and not exists (select ic from Source s, Transponder t, IgnoredChannel ic where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and ic.sid = sc.sid and ic.apid = sc.apid and t.id = ic.transpId and s.userId = :userId) order by sc.id",
									ScannedChannel.class);
					query.setParameter("sourceId", sourceId);
					query.setParameter("userId", user.getId());
				}
			} else {
				query = em
						.createQuery(
								"select sc from ScannedChannel sc, Transponder o_t, Source o_s where sc.sourceName = o_s.name and sc.frequency = o_t.frequency and sc.polarity = o_t.polarity and o_t.sourceId = o_s.id and o_t.id = :transpId and sc.userId = :userId and not exists (select c from Source s, Transponder t, Channel c where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and c.sid = sc.sid and c.apid = sc.apid and t.id = c.transpId and s.userId = :userId) and not exists (select ic from Source s, Transponder t, IgnoredChannel ic where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and ic.sid = sc.sid and ic.apid = sc.apid and t.id = ic.transpId and s.userId = :userId) order by sc.id",
								ScannedChannel.class);
				query.setParameter("transpId", transpId);
				query.setParameter("userId", user.getId());
			}
			break;
		case COMPARISON_CHANGED_MAIN:
			if (transpId < 0) {
				if (sourceId < 0) {
					query = em
							.createQuery(
									"select sc from ScannedChannel sc where sc.userId = :userId and exists (select c from Source s, Transponder t, Channel c where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and c.sid = sc.sid and c.apid = sc.apid and t.id = c.transpId and (t.dvbsGen <> sc.dvbsGen or t.symbolRate <> sc.symbolRate or coalesce(t.nid, 0) <> coalesce(sc.nid, 0) or coalesce(t.tid, 0) <> coalesce(sc.tid, 0) or c.vpid <> coalesce(sc.vpid, 0) or coalesce(c.venc, 0) <> sc.venc or coalesce(c.pcr, 0) <> coalesce(sc.pcr, 0) or coalesce(c.aenc, 0) <> sc.aenc or coalesce(c.tpid, 0) <> coalesce(sc.tpid, 0) or coalesce(c.caid, ' ') <> coalesce(sc.caid, ' ') or coalesce(c.rid, 0) <> coalesce(sc.rid, 0) or coalesce(c.scannedName, ' ') <> coalesce(sc.scannedName, ' ') or coalesce(c.providerName, ' ') <> coalesce(sc.providerName, ' ')) and s.userId = :userId) order by sc.id",
									ScannedChannel.class);
					query.setParameter("userId", user.getId());
				} else {
					query = em
							.createQuery(
									"select sc from ScannedChannel sc, Source o_s where sc.sourceName = o_s.name and o_s.id = :sourceId and sc.userId = :userId and exists (select c from Source s, Transponder t, Channel c where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and c.sid = sc.sid and c.apid = sc.apid and t.id = c.transpId and (t.dvbsGen <> sc.dvbsGen or t.symbolRate <> sc.symbolRate or coalesce(t.nid, 0) <> coalesce(sc.nid, 0) or coalesce(t.tid, 0) <> coalesce(sc.tid, 0) or c.vpid <> coalesce(sc.vpid, 0) or coalesce(c.venc, 0) <> sc.venc or coalesce(c.pcr, 0) <> coalesce(sc.pcr, 0) or coalesce(c.aenc, 0) <> sc.aenc or coalesce(c.tpid, 0) <> coalesce(sc.tpid, 0) or coalesce(c.caid, ' ') <> coalesce(sc.caid, ' ') or coalesce(c.rid, 0) <> coalesce(sc.rid, 0) or coalesce(c.scannedName, ' ') <> coalesce(sc.scannedName, ' ') or coalesce(c.providerName, ' ') <> coalesce(sc.providerName, ' ')) and s.userId = :userId) order by sc.id",
									ScannedChannel.class);
					query.setParameter("sourceId", sourceId);
					query.setParameter("userId", user.getId());
				}
			} else {
				query = em
						.createQuery(
								"select sc from ScannedChannel sc, Transponder o_t, Source o_s where sc.sourceName = o_s.name and sc.frequency = o_t.frequency and sc.polarity = o_t.polarity and o_t.sourceId = o_s.id and o_t.id = :transpId and sc.userId = :userId and exists (select c from Source s, Transponder t, Channel c where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and c.sid = sc.sid and c.apid = sc.apid and t.id = c.transpId and (t.dvbsGen <> sc.dvbsGen or t.symbolRate <> sc.symbolRate or coalesce(t.nid, 0) <> coalesce(sc.nid, 0) or coalesce(t.tid, 0) <> coalesce(sc.tid, 0) or c.vpid <> coalesce(sc.vpid, 0) or coalesce(c.venc, 0) <> sc.venc or coalesce(c.pcr, 0) <> coalesce(sc.pcr, 0) or coalesce(c.aenc, 0) <> sc.aenc or coalesce(c.tpid, 0) <> coalesce(sc.tpid, 0) or coalesce(c.caid, ' ') <> coalesce(sc.caid, ' ') or coalesce(c.rid, 0) <> coalesce(sc.rid, 0) or coalesce(c.scannedName, ' ') <> coalesce(sc.scannedName, ' ') or coalesce(c.providerName, ' ') <> coalesce(sc.providerName, ' ')) and s.userId = :userId) order by sc.id",
								ScannedChannel.class);
				query.setParameter("transpId", transpId);
				query.setParameter("userId", user.getId());
			}
			break;
		case COMPARISON_CHANGED_IGNORED:
			if (transpId < 0) {
				if (sourceId < 0) {
					query = em
							.createQuery(
									"select sc from ScannedChannel sc where sc.userId = :userId and exists (select ic from Source s, Transponder t, IgnoredChannel ic where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and ic.sid = sc.sid and ic.apid = sc.apid and t.id = ic.transpId and (t.dvbsGen <> sc.dvbsGen or t.symbolRate <> sc.symbolRate or coalesce(t.nid, 0) <> coalesce(sc.nid, 0) or coalesce(t.tid, 0) <> coalesce(sc.tid, 0) or ic.vpid <> coalesce(sc.vpid, 0) or coalesce(ic.caid, ' ') <> coalesce(sc.caid, ' ') or coalesce(ic.scannedName, ' ') <> coalesce(sc.scannedName, ' ') or coalesce(ic.providerName, ' ') <> coalesce(sc.providerName, ' ')) and s.userId = :userId) order by sc.id",
									ScannedChannel.class);
					query.setParameter("userId", user.getId());
				} else {
					query = em
							.createQuery(
									"select sc from ScannedChannel sc, Source o_s where sc.sourceName = o_s.name and o_s.id = :sourceId and sc.userId = :userId and exists (select ic from Source s, Transponder t, IgnoredChannel ic where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and ic.sid = sc.sid and ic.apid = sc.apid and t.id = ic.transpId and (t.dvbsGen <> sc.dvbsGen or t.symbolRate <> sc.symbolRate or coalesce(t.nid, 0) <> coalesce(sc.nid, 0) or coalesce(t.tid, 0) <> coalesce(sc.tid, 0) or ic.vpid <> coalesce(sc.vpid, 0) or coalesce(ic.caid, ' ') <> coalesce(sc.caid, ' ') or coalesce(ic.scannedName, ' ') <> coalesce(sc.scannedName, ' ') or coalesce(ic.providerName, ' ') <> coalesce(sc.providerName, ' ')) and s.userId = :userId) order by sc.id",
									ScannedChannel.class);
					query.setParameter("sourceId", sourceId);
					query.setParameter("userId", user.getId());
				}
			} else {
				query = em
						.createQuery(
								"select sc from ScannedChannel sc, Transponder o_t, Source o_s where sc.sourceName = o_s.name and sc.frequency = o_t.frequency and sc.polarity = o_t.polarity and o_t.sourceId = o_s.id and o_t.id = :transpId and sc.userId = :userId and exists (select ic from Source s, Transponder t, IgnoredChannel ic where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and ic.sid = sc.sid and ic.apid = sc.apid and t.id = ic.transpId and (t.dvbsGen <> sc.dvbsGen or t.symbolRate <> sc.symbolRate or coalesce(t.nid, 0) <> coalesce(sc.nid, 0) or coalesce(t.tid, 0) <> coalesce(sc.tid, 0) or ic.vpid <> coalesce(sc.vpid, 0) or coalesce(ic.caid, ' ') <> coalesce(sc.caid, ' ') or coalesce(ic.scannedName, ' ') <> coalesce(sc.scannedName, ' ') or coalesce(ic.providerName, ' ') <> coalesce(sc.providerName, ' ')) and s.userId = :userId) order by sc.id",
								ScannedChannel.class);
				query.setParameter("transpId", transpId);
				query.setParameter("userId", user.getId());
			}
			break;
		default:
			throw new IllegalArgumentException(
					"Wrong 'comparisonFilter' value: " + comparisonFilter);
		}

		return query.getResultList();
	}

	/**
	 * Finds a channel by the combination of source name, transponder frequency,
	 * polarity, stream ID, channel SID and APID given among the channels
	 * belonging to the current application user.
	 * 
	 * @param sourceName
	 *            the source name to find a channel within
	 * @param frequency
	 *            the transponder frequency to find a channel on
	 * @param polarity
	 *            the transponder polarity to find a channel on
	 * @param streamId
	 *            the stream ID within a multistream transponder to find or null
	 *            if not multistream transponder
	 * @param sid
	 *            the SID of channel to find
	 * @param apid
	 *            the APID of channel to find
	 * @return the channel found or null if no channel found
	 */
	public ScannedChannel findBySourceFrequencyPolarityStreamSidApid(
			String sourceName, Integer frequency, String polarity,
			Integer streamId, Integer sid, Integer apid) {
		ScannedChannel result;
		CriteriaBuilder cb;
		CriteriaQuery<ScannedChannel> criteria;
		Root<ScannedChannel> scannedChannelRoot;
		Predicate p;
		cb = em.getCriteriaBuilder();
		criteria = cb.createQuery(ScannedChannel.class);
		scannedChannelRoot = criteria.from(ScannedChannel.class);
		criteria.select(scannedChannelRoot);
		p = cb.conjunction();
		p = cb.and(p,
				cb.equal(scannedChannelRoot.get("sourceName"), sourceName));
		p = cb.and(p, cb.equal(scannedChannelRoot.get("frequency"), frequency));
		p = cb.and(p, cb.equal(scannedChannelRoot.get("polarity"), polarity));
		if (streamId == null) {
			p = cb.and(p, cb.isNull(scannedChannelRoot.get("streamId")));
		} else {
			p = cb.and(p,
					cb.equal(scannedChannelRoot.get("streamId"), streamId));
		}
		p = cb.and(p, cb.equal(scannedChannelRoot.get("sid"), sid));
		p = cb.and(p, cb.equal(scannedChannelRoot.get("apid"), apid));
		criteria.where(p);
		try {
			result = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException ex) {
			result = null;
		}
		return result;
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

	/**
	 * Adds the channel to the persisted list of scanned channels (stores it in
	 * the database).
	 * 
	 * @param channel
	 *            the scanned channel to add
	 */
	public void add(ScannedChannel channel) {
		channel.setUserId(user.getId());
		em.persist(channel);
		em.flush();
	}

	/**
	 * Updates the channel in the persisted list of scanned channels (updates it
	 * in the database).
	 * 
	 * @param channel
	 *            the scanned channel to update
	 */
	public void update(ScannedChannel channel) {
		em.merge(channel);
	}

	/**
	 * Marks all channels of the source with name given which belong to the
	 * current application user as 'not refreshed' i.e. not inserted/updated
	 * during the last source channels scan processing
	 * 
	 * @param sourceName
	 *            the name of the source which channels are marked
	 */
	public void makeNotRefreshed(String sourceName) {
		Query query;

		query = em
				.createQuery("update ScannedChannel sc set sc.refreshed = false where sc.sourceName = :sourceName and sc.userId = :userId");
		query.setParameter("sourceName", sourceName);
		query.setParameter("userId", user.getId());
		query.executeUpdate();
	}

	/**
	 * Deletes 'not refreshed' (i.e. not inserted/updated during the last source
	 * channels scan processing) channels from the source given
	 * 
	 * @param sourceName
	 *            the name of the source which files are deleted
	 */
	public void deleteNotRefreshed(String sourceName) {
		Query query;

		query = em
				.createQuery("delete from ScannedChannel sc where sc.refreshed = false and sc.sourceName = :sourceName and sc.userId = :userId");
		query.setParameter("sourceName", sourceName);
		query.setParameter("userId", user.getId());
		query.executeUpdate();
	}

}
