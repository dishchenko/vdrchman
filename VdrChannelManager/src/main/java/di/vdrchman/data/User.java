package di.vdrchman.data;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import java.io.Serializable;

@SessionScoped
@Named
public class User implements Serializable {

	private static final long serialVersionUID = 5646515466286495465L;

	private Long id;
	private String name;

	public Long getId() {

		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
