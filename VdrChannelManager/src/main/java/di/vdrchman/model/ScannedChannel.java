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
@Table(name = "tscanned_channel", uniqueConstraints = { @UniqueConstraint(columnNames = "id") })
public class ScannedChannel implements Serializable {

	private static final long serialVersionUID = -5089338070282762299L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "user_id")
	private Long userId;

	@NotNull(message = "Source name must be defined")
	@Size(max = 20)
	@Column(name = "source_name")
	private String sourceName;

	@NotNull
	@Column(name = "dvbs_gen")
	private Integer dvbsGen;

	@NotNull(message = "Transponder frequency must be defined")
	@Digits(fraction = 0, integer = 5)
	private Integer frequency;

	@NotNull
	@Size(max = 1)
	private String polarity;

	@NotNull(message = "Transponder symbol rate must be defined")
	@Digits(fraction = 0, integer = 5)
	@Column(name = "symbol_rate")
	private Integer symbolRate;

	@Digits(fraction = 0, integer = 5)
	private Integer nid;

	@Digits(fraction = 0, integer = 5)
	private Integer tid;

	@NotNull(message = "Service ID must be defined")
	@Digits(fraction = 0, integer = 5)
	private Integer sid;

	@Digits(fraction = 0, integer = 5)
	private Integer vpid;

	// TODO: add pcr

	@NotNull(message = "Audio PID must be defined")
	@Digits(fraction = 0, integer = 5)
	private Integer apid;

	@Digits(fraction = 0, integer = 5)
	private Integer tpid;

	@Digits(fraction = 0, integer = 5)
	private Integer caid;

	private Integer rid;

	@Size(max = 50)
	@Column(name = "scanned_name")
	private String scannedName;

	@Size(max = 50)
	@Column(name = "provider_name")
	private String providerName;

	public ScannedChannel() {
		// do nothing
	}

	public ScannedChannel(ScannedChannel scannedChannel) {
		this.id = scannedChannel.id != null ? new Long(scannedChannel.id)
				: null;
		this.userId = scannedChannel.userId != null ? new Long(
				scannedChannel.userId) : null;
		this.sourceName = scannedChannel.sourceName != null ? new String(
				scannedChannel.sourceName) : null;
		this.dvbsGen = scannedChannel.dvbsGen != null ? new Integer(
				scannedChannel.dvbsGen) : null;
		this.frequency = scannedChannel.frequency != null ? new Integer(
				scannedChannel.frequency) : null;
		this.polarity = scannedChannel.polarity != null ? new String(
				scannedChannel.polarity) : null;
		this.symbolRate = scannedChannel.symbolRate != null ? new Integer(
				scannedChannel.symbolRate) : null;
		this.nid = scannedChannel.nid != null ? new Integer(scannedChannel.nid) : null;
		this.tid = scannedChannel.tid != null ? new Integer(scannedChannel.tid) : null;
		this.sid = scannedChannel.sid != null ? new Integer(scannedChannel.sid) : null;
		this.vpid = scannedChannel.vpid != null ? new Integer(scannedChannel.vpid) : null;
		this.apid = scannedChannel.apid != null ? new Integer(scannedChannel.apid) : null;
		this.tpid = scannedChannel.tpid != null ? new Integer(scannedChannel.tpid) : null;
		this.caid = scannedChannel.caid != null ? new Integer(scannedChannel.caid) : null;
		this.rid = scannedChannel.rid != null ? new Integer(scannedChannel.rid) : null;
		this.scannedName = scannedChannel.scannedName != null ? new String(
				scannedChannel.scannedName) : null;
		this.providerName = scannedChannel.providerName != null ? new String(
				scannedChannel.providerName) : null;
	}

	public Long getId() {

		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {

		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getSourceName() {

		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public Integer getDvbsGen() {

		return dvbsGen;
	}

	public void setDvbsGen(Integer dvbsGen) {
		this.dvbsGen = dvbsGen;
	}

	public Integer getFrequency() {

		return frequency;
	}

	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

	public String getPolarity() {

		return polarity;
	}

	public void setPolarity(String polarity) {
		this.polarity = polarity;
	}

	public Integer getSymbolRate() {

		return symbolRate;
	}

	public void setSymbolRate(Integer symbolRate) {
		this.symbolRate = symbolRate;
	}

	public Integer getNid() {

		return nid;
	}

	public void setNid(Integer nid) {
		this.nid = nid;
	}

	public Integer getTid() {

		return tid;
	}

	public void setTid(Integer tid) {
		this.tid = tid;
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

	public Integer getApid() {

		return apid;
	}

	public void setApid(Integer apid) {
		this.apid = apid;
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

}
