package di.vdrchman;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "tchannel_group", uniqueConstraints = {
		@UniqueConstraint(columnNames = "id"),
		@UniqueConstraint(columnNames = { "channel_id", "group_id" }),
		@UniqueConstraint(columnNames = { "group_id", "seqno" }) })
public class ChannelGroup implements Serializable {

	private static final long serialVersionUID = 462881540844939359L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "channel_id")
	private Long channelId;

	@NotNull
	@Column(name = "group_id")
	private Long groupId;

	@NotNull
	private Integer seqno;

	public Long getId() {

		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getChannelId() {

		return channelId;
	}

	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}

	public Long getGroupId() {

		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public Integer getSeqno() {

		return seqno;
	}

	public void setSeqno(Integer seqno) {
		this.seqno = seqno;
	}

}
