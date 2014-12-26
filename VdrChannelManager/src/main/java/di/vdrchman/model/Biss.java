package di.vdrchman.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "tbiss", uniqueConstraints = @UniqueConstraint(columnNames = "channel_id"))
public class Biss implements Serializable {

	private static final long serialVersionUID = 4858713886494249192L;

	@Id
	@Column(name = "channel_id")
	private Long channelId;

	@NotNull(message = "Video key must be defined")
	@Size(max = 16)
	private String vkey;

	@NotNull(message = "Audio key must be defined")
	@Size(max = 16)
	private String akey;

	public Biss() {
		// do nothing
	}

	public Biss(Biss biss) {
		this.channelId = biss.channelId;
		this.vkey = biss.vkey;
		this.akey = biss.akey;
	}

	public Long getChannelId() {

		return channelId;
	}

	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}

	public String getVkey() {

		return vkey;
	}

	public void setVkey(String vkey) {
		this.vkey = vkey;
	}

	public String getAkey() {

		return akey;
	}

	public void setAkey(String akey) {
		this.akey = akey;
	}

}
