package di.vdrchman;

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
@Table(name = "tgroup", uniqueConstraints = {
		@UniqueConstraint(columnNames = "id"),
		@UniqueConstraint(columnNames = { "user_id", "name" }),
		@UniqueConstraint(columnNames = { "user_id", "start_channel_no" }) })
public class Group implements Serializable {

	private static final long serialVersionUID = -476710351490513765L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "user_id")
	private Long userId;

	@NotNull(message = "Group name must be defined")
	@Size(max = 20)
	private String name;

	@NotNull(message = "Starting channel number must be defined")
	@Digits(fraction = 0, integer = 5)
	@Column(name = "start_channel_no")
	private Integer startChannelNo;

	@NotNull(message = "Group description cannot be empty")
	@Size(max = 50)
	private String description;

	@NotNull
	@Column(columnDefinition = "BIT", length = 1)
	private Boolean ignored;

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

	public Integer getStartChannelNo() {

		return startChannelNo;
	}

	public void setStartChannelNo(Integer startChannelNo) {
		this.startChannelNo = startChannelNo;
	}

	public String getDescription() {

		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIgnored() {

		return ignored;
	}

	public void setIgnored(Boolean ignored) {
		this.ignored = ignored;
	}

}
