package di.vdrchman.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "tchannel_seqno", uniqueConstraints = {
		@UniqueConstraint(columnNames = "channel_id"),
		@UniqueConstraint(columnNames = { "user_id", "seqno" }) })
public class ChannelSeqno implements Serializable {

	private static final long serialVersionUID = -8225234734823944927L;

	@Id
	@Column(name = "channel_id")
	private Long channelId;

	@NotNull
	@Column(name = "user_id")
	private Long userId;

	@NotNull
	private Integer seqno;

	public Long getChannelId() {

		return channelId;
	}

	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}

	public Long getUserId() {

		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getSeqno() {

		return seqno;
	}

	public void setSeqno(Integer seqno) {
		this.seqno = seqno;
	}

}
