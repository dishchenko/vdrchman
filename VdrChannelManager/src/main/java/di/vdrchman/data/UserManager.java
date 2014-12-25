package di.vdrchman.data;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@SessionScoped
@Named
public class UserManager implements Serializable {

	private static final long serialVersionUID = 3291804911472576864L;

	private String currentPassword;

	private String newPassword;

	private String newPasswordAgain;

	private String passwordChangeResult;

	public void clearPasswordChangeResult() {
		passwordChangeResult = null;
	}

	public String getCurrentPassword() {

		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {

		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getNewPasswordAgain() {

		return newPasswordAgain;
	}

	public void setNewPasswordAgain(String newPasswordAgain) {
		this.newPasswordAgain = newPasswordAgain;
	}

	public String getPasswordChangeResult() {

		return passwordChangeResult;
	}

	public void setPasswordChangeResult(String passwordChangeResult) {
		this.passwordChangeResult = passwordChangeResult;
	}

}
