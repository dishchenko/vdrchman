package di.vdrchman.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
	 * @param comparisonFilter
	 *            the variant of result list filtering based on comparison with
	 *            main and ignored channel lists 0 - no filtering 1 - new
	 *            channels (not found in main and ignore channel lists) 2 -
	 *            changed channels (compared to the channels from main list)
	 * @return the list of scanned channels found for the current application
	 *         user and given source and transponder IDs
	 */
	public List<ScannedChannel> findAll(long sourceId, long transpId,
			int comparisonFilter) {
		TypedQuery<ScannedChannel> query;

		switch (comparisonFilter) {
		case 0:
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
		case 1:
			if (transpId < 0) {
				if (sourceId < 0) {
					query = em
							.createQuery(
									"select sc from ScannedChannel sc where sc.userId = :userId and not exists (select c from Source s, Transponder t, Channel c where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and c.sid = sc.sid and c.apid = sc.apid and t.id = c.transpId) and not exists (select ic from Source s, Transponder t, IgnoredChannel ic where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and ic.sid = sc.sid and ic.apid = sc.apid and t.id = ic.transpId) order by sc.id",
									ScannedChannel.class);
					query.setParameter("userId", user.getId());
				} else {
					query = em
							.createQuery(
									"select sc from ScannedChannel sc, Source os where sc.sourceName = os.name and os.id = :sourceId and sc.userId = :userId and not exists (select c from Source s, Transponder t, Channel c where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and c.sid = sc.sid and c.apid = sc.apid and t.id = c.transpId) and not exists (select ic from Source s, Transponder t, IgnoredChannel ic where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and ic.sid = sc.sid and ic.apid = sc.apid and t.id = ic.transpId) order by sc.id",
									ScannedChannel.class);
					query.setParameter("sourceId", sourceId);
					query.setParameter("userId", user.getId());
				}
			} else {
				query = em
						.createQuery(
								"select sc from ScannedChannel sc, Transponder ot, Source os where sc.sourceName = os.name and sc.frequency = ot.frequency and sc.polarity = ot.polarity and ot.sourceId = os.id and ot.id = :transpId and sc.userId = :userId and not exists (select c from Source s, Transponder t, Channel c where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and c.sid = sc.sid and c.apid = sc.apid and t.id = c.transpId) and not exists (select ic from Source s, Transponder t, IgnoredChannel ic where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and ic.sid = sc.sid and ic.apid = sc.apid and t.id = ic.transpId) order by sc.id",
								ScannedChannel.class);
				query.setParameter("transpId", transpId);
				query.setParameter("userId", user.getId());
			}
			break;
		case 2:
			if (transpId < 0) {
				if (sourceId < 0) {
					query = em
							.createQuery(
									"select sc from ScannedChannel sc where sc.userId = :userId and exists (select c from Source s, Transponder t, Channel c where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and c.sid = sc.sid and c.apid = sc.apid and t.id = c.transpId and (t.dvbsGen <> sc.dvbsGen or t.symbolRate <> sc.symbolRate or coalesce(t.nid, 0) <> coalesce(sc.nid, 0) or coalesce(t.tid, 0) <> coalesce(sc.tid, 0) or c.vpid <> coalesce(sc.vpid, 0) or coalesce(c.venc, 0) <> sc.venc or coalesce(c.pcr, 0) <> coalesce(sc.pcr, 0) or coalesce(c.aenc, 0) <> sc.aenc or coalesce(c.tpid, 0) <> coalesce(sc.tpid, 0) or coalesce(c.caid, ' ') <> coalesce(sc.caid, ' ') or coalesce(c.rid, 0) <> coalesce(sc.rid, 0) or coalesce(c.scannedName, ' ') <> coalesce(sc.scannedName, ' ') or coalesce(c.providerName, ' ') <> coalesce(sc.providerName, ' '))) order by sc.id",
									ScannedChannel.class);
					query.setParameter("userId", user.getId());
				} else {
					query = em
							.createQuery(
									"select sc from ScannedChannel sc, Source os where sc.sourceName = os.name and os.id = :sourceId and sc.userId = :userId  and exists (select c from Source s, Transponder t, Channel c where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and c.sid = sc.sid and c.apid = sc.apid and t.id = c.transpId and (t.dvbsGen <> sc.dvbsGen or t.symbolRate <> sc.symbolRate or coalesce(t.nid, 0) <> coalesce(sc.nid, 0) or coalesce(t.tid, 0) <> coalesce(sc.tid, 0) or c.vpid <> coalesce(sc.vpid, 0) or coalesce(c.venc, 0) <> sc.venc or coalesce(c.pcr, 0) <> coalesce(sc.pcr, 0) or coalesce(c.aenc, 0) <> sc.aenc or coalesce(c.tpid, 0) <> coalesce(sc.tpid, 0) or coalesce(c.caid, ' ') <> coalesce(sc.caid, ' ') or coalesce(c.rid, 0) <> coalesce(sc.rid, 0) or coalesce(c.scannedName, ' ') <> coalesce(sc.scannedName, ' ') or coalesce(c.providerName, ' ') <> coalesce(sc.providerName, ' '))) order by sc.id",
									ScannedChannel.class);
					query.setParameter("sourceId", sourceId);
					query.setParameter("userId", user.getId());
				}
			} else {
				query = em
						.createQuery(
								"select sc from ScannedChannel sc, Transponder ot, Source os where sc.sourceName = os.name and sc.frequency = ot.frequency and sc.polarity = ot.polarity and ot.sourceId = os.id and ot.id = :transpId and sc.userId = :userId  and exists (select c from Source s, Transponder t, Channel c where s.name = sc.sourceName and t.frequency = sc.frequency and t.polarity = sc.polarity and s.id = t.sourceId and c.sid = sc.sid and c.apid = sc.apid and t.id = c.transpId and (t.dvbsGen <> sc.dvbsGen or t.symbolRate <> sc.symbolRate or coalesce(t.nid, 0) <> coalesce(sc.nid, 0) or coalesce(t.tid, 0) <> coalesce(sc.tid, 0) or c.vpid <> coalesce(sc.vpid, 0) or coalesce(c.venc, 0) <> sc.venc or coalesce(c.pcr, 0) <> coalesce(sc.pcr, 0) or coalesce(c.aenc, 0) <> sc.aenc or coalesce(c.tpid, 0) <> coalesce(sc.tpid, 0) or coalesce(c.caid, ' ') <> coalesce(sc.caid, ' ') or coalesce(c.rid, 0) <> coalesce(sc.rid, 0) or coalesce(c.scannedName, ' ') <> coalesce(sc.scannedName, ' ') or coalesce(c.providerName, ' ') <> coalesce(sc.providerName, ' '))) order by sc.id",
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
	 * polarity, channel SID and APID given among the channels belonging to the
	 * current application user.
	 * 
	 * @param sourceName
	 *            the source name to find a channel within
	 * @param frequency
	 *            the transponder frequency to find a channel on
	 * @param polarity
	 *            the transponder polarity to find a channel on
	 * @param sid
	 *            the SID of channel to find
	 * @param apid
	 *            the APID of channel to find
	 * @return the channel found or null if no channel found
	 */
	public ScannedChannel findBySourceFrequencyPolaritySidApid(
			String sourceName, Integer frequency, String polarity, Integer sid,
			Integer apid) {
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

}
