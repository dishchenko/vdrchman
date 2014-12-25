package di.vdrchman.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import di.vdrchman.model.User;

@Stateless
@Named
public class UserRepository {

	@Inject
	private EntityManager em;

	@Inject
	private SessionUser sessionUser;

	public boolean checkPassword(String password)
			throws NoSuchAlgorithmException {
		MessageDigest md;

		md = MessageDigest.getInstance("SHA-256");
		md.update(password.getBytes());

		return Base64.getEncoder().encodeToString(md.digest())
				.equals(em.find(User.class, sessionUser.getId()).getPassword());
	}

	public void changePassword(String newPassword)
			throws NoSuchAlgorithmException {
		MessageDigest md;

		md = MessageDigest.getInstance("SHA-256");
		md.update(newPassword.getBytes());

		em.find(User.class, sessionUser.getId()).setPassword(
				Base64.getEncoder().encodeToString(md.digest()));
	}

}
