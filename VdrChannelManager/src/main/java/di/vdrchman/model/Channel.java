package di.vdrchman.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "tchannel", uniqueConstraints = {
		@UniqueConstraint(columnNames = "id"),
		@UniqueConstraint(columnNames = { "transp_id", "sid", "apid" }) })
public class Channel implements Serializable {

	private static final long serialVersionUID = -4320692161692842240L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "transp_id")
	private Long transpId;

	@NotNull(message = "Service ID must be defined")
	@Digits(fraction = 0, integer = 5)
	private Integer sid;

	@Digits(fraction = 0, integer = 5)
	private Integer vpid;

	private Integer venc;

	private Integer pcr;

	@NotNull(message = "Audio PID must be defined")
	@Digits(fraction = 0, integer = 5)
	private Integer apid;

	private Integer aenc;

	@Digits(fraction = 0, integer = 5)
	private Integer tpid;

	@Digits(fraction = 0, integer = 5)
	private Integer caid;

	@Digits(fraction = 0, integer = 5)
	private Integer rid;

	@Size(max = 50)
	@Column(name = "scanned_name")
	private String scannedName;

	@Size(max = 50)
	@Column(name = "provider_name")
	private String providerName;

	@NotNull(message = "Channel Name must be defined")
	@Size(max = 50)
	private String name;

	@Size(max = 5)
	private String lang;

	@NotNull
	@Column(columnDefinition = "BIT", length = 1)
	private Boolean locked;

	public Channel() {
		// do nothing
	}

	public Channel(Channel channel) {
		this.id = channel.id != null ? new Long(channel.id) : null;
		this.transpId = channel.transpId != null ? new Long(channel.transpId)
				: null;
		this.sid = channel.sid != null ? new Integer(channel.sid) : null;
		this.vpid = channel.vpid != null ? new Integer(channel.vpid) : null;
		this.venc = channel.venc != null ? new Integer(channel.venc) : null;
		this.pcr = channel.pcr != null ? new Integer(channel.pcr) : null;
		this.apid = channel.apid != null ? new Integer(channel.apid) : null;
		this.aenc = channel.aenc != null ? new Integer(channel.aenc) : null;
		this.tpid = channel.tpid != null ? new Integer(channel.tpid) : null;
		this.caid = channel.caid != null ? new Integer(channel.caid) : null;
		this.rid = channel.rid != null ? new Integer(channel.rid) : null;
		this.scannedName = channel.scannedName != null ? new String(
				channel.scannedName) : null;
		this.providerName = channel.providerName != null ? new String(
				channel.providerName) : null;
		this.name = channel.name != null ? new String(channel.name) : null;
		this.lang = channel.lang != null ? new String(channel.lang) : null;
		this.locked = channel.locked != null ? new Boolean(channel.locked)
				: null;
	}

	public Long getId() {

		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTranspId() {

		return transpId;
	}

	public void setTranspId(Long transpId) {
		this.transpId = transpId;
	}

	public Integer getSid() {

		return sid;
	}

	public void setSid(Integer sid) {
		this.sid = sid;
	}

	public Integer getVpid() {

		return vpid;
	}

	public void setVpid(Integer vpid) {
		this.vpid = vpid;
	}

	public Integer getVenc() {

		return venc;
	}

	public void setVenc(Integer venc) {
		this.venc = venc;
	}

	public Integer getPcr() {

		return pcr;
	}

	public void setPcr(Integer pcr) {
		this.pcr = pcr;
	}

	public Integer getApid() {

		return apid;
	}

	public void setApid(Integer apid) {
		this.apid = apid;
	}

	public Integer getAenc() {

		return aenc;
	}

	public void setAenc(Integer aenc) {
		this.aenc = aenc;
	}

	public Integer getTpid() {

		return tpid;
	}

	public void setTpid(Integer tpid) {
		this.tpid = tpid;
	}

	public Integer getCaid() {

		return caid;
	}

	public void setCaid(Integer caid) {
		this.caid = caid;
	}

	public Integer getRid() {

		return rid;
	}

	public void setRid(Integer rid) {
		this.rid = rid;
	}

	public String getScannedName() {

		return scannedName;
	}

	public void setScannedName(String scannedName) {
		this.scannedName = scannedName;
	}

	public String getProviderName() {

		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLang() {

		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public Boolean getLocked() {

		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

}
