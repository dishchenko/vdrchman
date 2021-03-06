package di.vdrchman.login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

import org.jboss.security.auth.spi.DatabaseServerLoginModule;

import di.vdrchman.data.SessionUser;

// Customized login module which saves ID of the logged in user
// in the SessionUser bean
public class UserInjectingLoginModule extends DatabaseServerLoginModule {

	@Override
	public boolean login() throws LoginException {
		boolean result;
		Connection conn;
		PreparedStatement ps;
		ResultSet rs;
		InitialContext ictx;
		DataSource ds;
		String username;
		BeanManager bm;
		Bean<SessionUser> bean;
		CreationalContext<SessionUser> cctx;
		SessionUser sessionUser;

		result = super.login();

		conn = null;
		ps = null;
		rs = null;

		if (result) {
			try {
				ictx = new InitialContext();
				ds = (DataSource) ictx.lookup(dsJndiName);
				username = getUsername();
				bm = (BeanManager) ictx.lookup("java:comp/BeanManager");
				bean = getSessionUserBean(bm);
				cctx = bm.createCreationalContext(bean);
				sessionUser = (SessionUser) bm.getReference(bean,
						SessionUser.class, cctx);
				conn = ds.getConnection();
				ps = conn
						.prepareStatement("select id from tuser where name=?");
				ps.setString(1, username);
				rs = ps.executeQuery();
				rs.next();
				sessionUser.setId(rs.getLong(1));
				sessionUser.setName(username);
			}

			catch (NamingException ex) {
				throw new LoginException(ex.toString(true));
			}

			catch (SQLException ex) {
				log.error("Query failed", ex);
				throw new LoginException(ex.toString());
			}

			finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException ex) {
					}
				}
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException ex) {
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException ex) {
					}
				}
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private Bean<SessionUser> getSessionUserBean(BeanManager bm) {

		return (Bean<SessionUser>) bm.getBeans(SessionUser.class).iterator()
				.next();
	}
}
