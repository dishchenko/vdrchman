package di.vdrchman.data;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import di.vdrchman.model.Channel;

@Stateless
@Named
public class ChannelRepository {

	@Inject
	private EntityManager em;

	@Inject
	private User user;

	public List<Channel> findAll(long sourceId, long transpId) {
		List<Channel> result;
		Query query;
		List<?> queryResult;

		if (transpId < 0) {
			if (sourceId < 0) {
				query = em
						.createQuery("select c, cs.seqno from Channel c, ChannelSeqno cs where c.id = cs.channelId and cs.userId = :userId order by cs.seqno");
				query.setParameter("userId", user.getId());
			} else {
				query = em
						.createQuery("select c, cs.seqno from Channel c, ChannelSeqno cs, Transponder t where c.id = cs.channelId and c.transpId = t.id and cs.userId = :userId and t.sourceId = :sourceId order by cs.seqno");
				query.setParameter("userId", user.getId());
				query.setParameter("sourceId", sourceId);
			}
		} else {
			query = em
					.createQuery("select c, cs.seqno from Channel c, ChannelSeqno cs where c.id = cs.channelId and cs.userId = :userId and c.transpId = :transpId order by cs.seqno");
			query.setParameter("userId", user.getId());
			query.setParameter("transpId", transpId);
		}

		queryResult = query.getResultList();

		result = new ArrayList<Channel>();

		for (Object row : queryResult) {
			result.add((Channel) ((Object[]) row)[0]);
		}

		return result;
	}

}
