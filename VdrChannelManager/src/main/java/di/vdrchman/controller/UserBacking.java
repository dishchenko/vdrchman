package di.vdrchman.controller;

import java.security.NoSuchAlgorithmException;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import di.vdrchman.data.UserManager;
import di.vdrchman.data.UserRepository;

@Model
public class UserBacking {

	@Inject
	private UserManager userManager;

	@Inject
	private UserRepository userRepository;

	public void changePassword() throws NoSuchAlgorithmException {
		if (userRepository.checkPassword(userManager.getCurrentPassword())) {
			if (userManager.getNewPassword().equals(
					userManager.getNewPasswordAgain())) {
				userRepository.changePassword(userManager.getNewPassword());
				userManager.setPasswordChangeResult("Password changed.");
			} else {
				userManager
						.setPasswordChangeResult("Error: Two new passwords do not match, current password not changed.");
			}
		} else {
			userManager
					.setPasswordChangeResult("Error: Wrong current password, password not changed.");
		}
	}

}
