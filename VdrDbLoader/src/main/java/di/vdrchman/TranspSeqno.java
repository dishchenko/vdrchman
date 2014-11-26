package di.vdrchman;

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
		@UniqueConstraint(columnNames = "transp_id") })
public class TranspSeqno implements Serializable {

	private static final long serialVersionUID = 6136014657774985023L;

	@Id
	@Column(name = "transp_id")
	private Integer transpId;

	@NotNull
	private Integer seqno;

	public Integer getTranspId() {

		return transpId;
	}

	public void setTranspId(Integer transpId) {
		this.transpId = transpId;
	}

	public Integer getSeqno() {

		return seqno;
	}

	public void setSeqno(Integer seqno) {
		this.seqno = seqno;
	}

}
