package di.vdrchman;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

public class ChannelRepository {

	private EntityManager em;

	public ChannelRepository(EntityManager em) {
		this.em = em;
	}

	// Load Channels for Sources by reading the data from configuration reader.
	// If the Source given is null then Channels are loaded
	// for all Sources belonging to the current User.
	// Otherwise only Channels of the given Source are taken into account.
	// In this case configuration reading interrupts since all Source
	// Channels have been loaded
	public void load(Long userId, Source source, BufferedReader channelCfg)
			throws IOException {
		Query query;
		Integer queryResult;
		int seqno;
		SourceRepository sr;
		TransponderRepository tr;
		String curSourceName;
		Source curSource;
		String line;
		String[] splitLine;
		int frequency;
		String polarity;
		Transponder transponder;
		Channel channel;
		String[] vInfo;
		int vpid;
		int venc;
		int pcr;
		String[] aInfo;
		int apid;
		int aenc;
		int tpid;
		int caid;
		int rid;
		String[] snInfo;
		String scannedName;
		String providerName;
		String[] nlInfo;
		String name;
		String lang;
		ChannelSeqno channelSeqno;

		query = em
				.createQuery("select max(cs.seqno) from ChannelSeqno cs where cs.userId = :userId");
		query.setParameter("userId", userId);
		queryResult = (Integer) query.getSingleResult();

		if (queryResult != null) {
			seqno = queryResult + 1;
		} else {
			seqno = 1;
		}

		sr = new SourceRepository(em);
		tr = new TransponderRepository(em);
		curSourceName = null;
		curSource = null;
		transponder = null;

		while ((line = channelCfg.readLine()) != null) {

			if (line.length() == 0) {
				continue;
			}
			if (line.charAt(0) == '#') {
				continue;
			}
			splitLine = line.split(":");

			if ("S".equals(splitLine[0])) {
				curSourceName = splitLine[1];
				if (source != null) {
					if (curSourceName.equals(source.getName())) {
						curSource = source;
					} else {
						if (curSource != null) {
							break;
						}
					}
				} else {
					curSource = sr.findByName(userId, curSourceName);
					if (curSource == null) {
						Logger.getLogger(this.getClass()).log(
								Level.ERROR,
								"Can't find Source named '" + curSourceName
										+ "'");
					}
				}
			}

			if ("T".equals(splitLine[0])) {
				if (curSource != null) {
					frequency = Integer.parseInt(splitLine[1]);
					polarity = splitLine[2].substring(0, 1);
					transponder = tr.findBySourceFrequencyPolarity(
							curSource.getId(), frequency, polarity);
					if (transponder == null) {
						Logger.getLogger(this.getClass()).log(
								Level.ERROR,
								"Can't find Transponder for Source '"
										+ curSourceName + "' with frequency '"
										+ frequency + "' and polarity '"
										+ polarity + "'");
					}
				}
			}

			if ("C".equals(splitLine[0])) {
				if (transponder != null) {
					vInfo = splitLine[2].split("=");
					if (vInfo.length == 2) {
						venc = Integer.parseInt(vInfo[1]);
						if (venc == 27) {
							venc = 2;
						}
					} else {
						venc = 0;
					}
					vInfo = vInfo[0].split("\\+");
					vpid = Integer.parseInt(vInfo[0]);
					if (vInfo.length == 2) {
						pcr = Integer.parseInt(vInfo[1]);
					} else {
						pcr = 0;
					}
					aInfo = splitLine[3].split("=@");
					apid = Integer.parseInt(aInfo[0]);
					if (aInfo.length == 2) {
						aenc = Integer.parseInt(aInfo[1]);
						if (aenc == 129) {
							aenc = 2;
						}
					} else {
						aenc = 0;
					}
					tpid = Integer.parseInt(splitLine[4]);
					caid = Integer.parseInt(splitLine[5]);
					rid = Integer.parseInt(splitLine[6]);
					snInfo = Charset
							.forName("ISO-8859-5")
							.decode(ByteBuffer.wrap(splitLine[7]
									.getBytes("ISO-8859-1"))).toString()
							.split(";");
					scannedName = snInfo[0];
					if (snInfo.length == 2) {
						providerName = snInfo[1];
					} else {
						providerName = null;
					}
					nlInfo = Charset
							.forName("UTF-8")
							.decode(ByteBuffer.wrap(splitLine[9]
									.getBytes("ISO-8859-1"))).toString()
							.split("\\) ");
					if (nlInfo.length == 2) {
						lang = nlInfo[0];
						name = nlInfo[1];
					} else {
						name = nlInfo[0];
						lang = null;
					}

					channel = new Channel();
					channel.setTranspId(transponder.getId());
					channel.setSid(Integer.parseInt(splitLine[1]));
					if (vpid != 0) {
						channel.setVpid(vpid);
					}
					if (venc != 0) {
						channel.setVenc(venc);
					}
					if (pcr != 0) {
						channel.setPcr(pcr);
					}
					channel.setApid(apid);
					if (aenc != 0) {
						channel.setAenc(aenc);
					}
					if (tpid != 0) {
						channel.setTpid(tpid);
					}
					if (caid != 0) {
						channel.setCaid(caid);
					}
					if (rid != 0) {
						channel.setRid(rid);
					}
					channel.setScannedName(scannedName);
					channel.setProviderName(providerName);
					channel.setName(name);
					channel.setLang(lang);
					channel.setLocked("L".equals(splitLine[11]));
					em.persist(channel);
					em.flush();

					channelSeqno = new ChannelSeqno();
					channelSeqno.setChannelId(channel.getId());
					channelSeqno.setUserId(userId);
					channelSeqno.setSeqno(seqno);
					em.persist(channelSeqno);
					em.flush();

					++seqno;
				}
			}
		}
	}

}
