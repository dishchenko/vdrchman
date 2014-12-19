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
@Table(name = "tscanned_channel", uniqueConstraints = {
		@UniqueConstraint(columnNames = "id"),
		@UniqueConstraint(columnNames = { "source_name", "frequency",
				"polarization", "stream_id", "sid", "apid" }) })
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
	private String polarization;

	@NotNull(message = "Transponder symbol rate must be defined")
	@Digits(fraction = 0, integer = 5)
	@Column(name = "symbol_rate")
	private Integer symbolRate;

	@Digits(fraction = 0, integer = 5)
	@Column(name = "stream_id")
	private Integer streamId;

	@Digits(fraction = 0, integer = 5)
	private Integer nid;

	@Digits(fraction = 0, integer = 5)
	private Integer tid;

	@NotNull(message = "Service ID must be defined")
	@Digits(fraction = 0, integer = 5)
	private Integer sid;

	@Digits(fraction = 0, integer = 5)
	private Integer vpid;

	@Digits(fraction = 0, integer = 3)
	private Integer venc;

	private Integer pcr;

	@NotNull(message = "Audio PID must be defined")
	@Digits(fraction = 0, integer = 5)
	private Integer apid;

	@Digits(fraction = 0, integer = 3)
	private Integer aenc;

	@Digits(fraction = 0, integer = 5)
	private Integer tpid;

	@Size(max = 20)
	private String caid;

	private Integer rid;

	@Size(max = 50)
	@Column(name = "scanned_name")
	private String scannedName;

	@Size(max = 50)
	@Column(name = "provider_name")
	private String providerName;

	@NotNull
	@Column(columnDefinition = "BIT", length = 1)
	private Boolean refreshed;

	public ScannedChannel() {
		// do nothing
	}

	public ScannedChannel(ScannedChannel scannedChannel) {
		this.id = scannedChannel.id;
		this.userId = scannedChannel.userId;
		this.sourceName = scannedChannel.sourceName;
		this.dvbsGen = scannedChannel.dvbsGen;
		this.frequency = scannedChannel.frequency;
		this.polarization = scannedChannel.polarization;
		this.symbolRate = scannedChannel.symbolRate;
		this.streamId = scannedChannel.streamId;
		this.nid = scannedChannel.nid;
		this.tid = scannedChannel.tid;
		this.sid = scannedChannel.sid;
		this.vpid = scannedChannel.vpid;
		this.pcr = scannedChannel.pcr;
		this.apid = scannedChannel.apid;
		this.tpid = scannedChannel.tpid;
		this.caid = scannedChannel.caid;
		this.rid = scannedChannel.rid;
		this.scannedName = scannedChannel.scannedName;
		this.providerName = scannedChannel.providerName;
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

	public String getPolarization() {

		return polarization;
	}

	public void setPolarization(String polarization) {
		this.polarization = polarization;
	}

	public Integer getSymbolRate() {

		return symbolRate;
	}

	public void setSymbolRate(Integer symbolRate) {
		this.symbolRate = symbolRate;
	}

	public Integer getStreamId() {

		return streamId;
	}

	public void setStreamId(Integer streamId) {
		this.streamId = streamId;
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

	public String getCaid() {

		return caid;
	}

	public void setCaid(String caid) {
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

	public Boolean getRefreshed() {

		return refreshed;
	}

	public void setRefreshed(Boolean refreshed) {
		this.refreshed = refreshed;
	}

}
