package di.vdrchman;

import javax.persistence.EntityManager;

public class UserRepository {

	private EntityManager em;

	public UserRepository(EntityManager em) {
		this.em = em;
	}

	// Add new User
	public Long add(String userName, String passwordHash) {
		User user;
		User mergedUser;
		UserRole userRole;

		user = new User();
		user.setName(userName);
		user.setPassword(passwordHash);

		mergedUser = em.merge(user);
		em.flush();
		user.setId(mergedUser.getId());

		userRole = new UserRole();
		userRole.setUserId(user.getId());
		userRole.setRole("AuthUser");

		em.merge(userRole);

		return user.getId();
	}

	// Store new password hash for the user with given userId
	public boolean setPassword(Long userId, String passwordHash) {
		User user;

		user = em.find(User.class, userId);

		if (user != null) {
			user.setPassword(passwordHash);
		}

		return (user != null);
	}

	// Remove the User with given userId
	public boolean remove(Long userId) {
		User user;

		user = em.find(User.class, userId);

		if (user != null) {
			em.remove(user);
		}

		return (user != null);
	}

}
