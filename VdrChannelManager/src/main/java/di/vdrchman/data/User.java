package di.vdrchman.data;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import java.io.Serializable;

@SessionScoped
@Named
public class User implements Serializable {

	private static final long serialVersionUID = 5646515466286495465L;

	private Integer id;
	private String name;

	public Integer getId() {

		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
