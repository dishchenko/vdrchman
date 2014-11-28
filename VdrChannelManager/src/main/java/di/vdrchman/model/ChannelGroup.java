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
@Table(name = "tchannel_group", uniqueConstraints = {
		@UniqueConstraint(columnNames = "id"),
		@UniqueConstraint(columnNames = { "channel_id", "group_id" }) })
public class ChannelGroup implements Serializable {

	private static final long serialVersionUID = 462881540844939359L;

	@Id
	@Column(name = "id")
	private Long id;

	@NotNull
	@Column(name = "channel_id")
	private Long channelId;

	@NotNull
	@Column(name = "group_id")
	private Long groupId;

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

}
