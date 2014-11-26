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
@Table(name = "ttransp_seqno", uniqueConstraints = {
		@UniqueConstraint(columnNames = "transp_id"),
		@UniqueConstraint(columnNames = { "user_id", "seqno" }) })
public class TranspSeqno implements Serializable {

	private static final long serialVersionUID = 7151718387249632343L;

	@Id
	@Column(name = "transp_id")
	private Integer transpId;

	@NotNull
	@Column(name = "user_id")
	private Integer userId;

	@NotNull
	private Integer seqno;

	public Integer getTranspId() {

		return transpId;
	}

	public void setTranspId(Integer transpId) {
		this.transpId = transpId;
	}

	public Integer getUserId() {

		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getSeqno() {

		return seqno;
	}

	public void setSeqno(Integer seqno) {
		this.seqno = seqno;
	}

}
