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
@Table(name = "ttransponder", uniqueConstraints = {
		@UniqueConstraint(columnNames = "id"),
		@UniqueConstraint(columnNames = { "source_id", "frequency", "polarity" }) })
public class Transponder implements Serializable {

	private static final long serialVersionUID = 8792144048918228648L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "source_id")
	private Long sourceId;

	// 1 - S, 2 - S2
	@NotNull
	@Column(name = "dvbs_gen")
	private Integer dvbsGen;

	@NotNull(message = "Transponder Frequency must be defined")
	@Digits(fraction = 0, integer = 5)
	private Integer frequency;

	@NotNull
	@Size(max = 1)
	private String polarity;

	@NotNull(message = "Transponder Symbol Rate must be defined")
	@Digits(fraction = 0, integer = 5)
	@Column(name = "symbol_rate")
	private Integer symbolRate;

	@Digits(fraction = 0, integer = 5)
	private Integer nid;

	@Digits(fraction = 0, integer = 5)
	private Integer tid;

	@NotNull
	@Column(columnDefinition = "BIT", length = 1)
	private Boolean ignored;

	public Transponder() {
		// do nothing
	}

	public Transponder(Transponder transponder) {
		this.id = transponder.id != null ? new Long(transponder.id) : null;
		this.sourceId = transponder.sourceId != null ? new Long(
				transponder.sourceId) : null;
		this.dvbsGen = transponder.dvbsGen != null ? new Integer(
				transponder.dvbsGen) : null;
		this.frequency = transponder.frequency != null ? new Integer(
				transponder.frequency) : null;
		this.polarity = transponder.polarity != null ? new String(
				transponder.polarity) : null;
		this.symbolRate = transponder.symbolRate != null ? new Integer(
				transponder.symbolRate) : null;
		this.nid = transponder.nid != null ? new Integer(transponder.nid) : null;
		this.tid = transponder.tid != null ? new Integer(transponder.tid) : null;
		this.ignored = transponder.ignored != null ? new Boolean(
				transponder.ignored) : null;
	}

	public Long getId() {

		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSourceId() {

		return sourceId;
	}

	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
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

	public Boolean getIgnored() {

		return ignored;
	}

	public void setIgnored(Boolean ignored) {
		this.ignored = ignored;
	}

}
