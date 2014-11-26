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
	private Integer id;

	@NotNull
	@Column(name = "user_id")
	private Integer userId;

	@NotNull(message = "Source Name must be defined")
	@Size(max = 20)
	private String name;

	@NotNull(message = "Source Description cannot be empty")
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
		this.id = source.id != null ? new Integer(source.id) : null;
		this.userId = source.userId != null ? new Integer(source.userId) : null;
		this.name = source.name != null ? new String(source.name) : null;
		this.description = source.description != null ? new String(
				source.description) : null;
		this.loV = source.loV != null ? new String(source.loV) : null;
		this.hiV = source.hiV != null ? new String(source.hiV) : null;
		this.loH = source.loH != null ? new String(source.loH) : null;
		this.hiH = source.hiH != null ? new String(source.hiH) : null;
		this.rotor = source.rotor != null ? new Integer(source.rotor) : null;
	}

	public Integer getId() {

		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {

		return userId;
	}

	public void setUserId(Integer userId) {
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
