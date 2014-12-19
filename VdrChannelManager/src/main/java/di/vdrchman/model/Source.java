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
@Table(name = "tsource", uniqueConstraints = {
		@UniqueConstraint(columnNames = "id"),
		@UniqueConstraint(columnNames = { "user_id", "name" }) })
public class Source implements Serializable {

	private static final long serialVersionUID = -3725240150153741668L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "user_id")
	private Long userId;

	@NotNull(message = "Source name must be defined")
	@Size(max = 20)
	private String name;

	@NotNull(message = "Source description cannot be empty")
	@Size(max = 50)
	private String description;

	@Size(max = 100)
	@Column(name = "lo_v")
	private String loV;

	@Size(max = 100)
	@Column(name = "hi_v")
	private String hiV;

	@Size(max = 100)
	@Column(name = "lo_h")
	private String loH;

	@Size(max = 100)
	@Column(name = "hi_h")
	private String hiH;

	@Digits(fraction = 0, integer = 3)
	private Integer rotor;

	public Source() {
		// do nothing
	}

	public Source(Source source) {
		this.id = source.id;
		this.userId = source.userId;
		this.name = source.name;
		this.description = source.description;
		this.loV = source.loV;
		this.hiV = source.hiV;
		this.loH = source.loH;
		this.hiH = source.hiH;
		this.rotor = source.rotor;
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

	public String getName() {

		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {

		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLoV() {

		return loV;
	}

	public void setLoV(String loV) {
		this.loV = loV;
	}

	public String getHiV() {

		return hiV;
	}

	public void setHiV(String hiV) {
		this.hiV = hiV;
	}

	public String getLoH() {

		return loH;
	}

	public void setLoH(String loH) {
		this.loH = loH;
	}

	public String getHiH() {

		return hiH;
	}

	public void setHiH(String hiH) {
		this.hiH = hiH;
	}

	public Integer getRotor() {

		return rotor;
	}

	public void setRotor(Integer rotor) {
		this.rotor = rotor;
	}

}
