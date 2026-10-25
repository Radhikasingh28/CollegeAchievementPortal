
package edu.niet.portal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PortalUserDetailsService implements UserDetailsService {

    private final JdbcTemplate jdbc;

    public PortalUserDetailsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        var users = jdbc.query(
                """
                        SELECT username, password_hash, role
                        FROM portal_users
                        WHERE username = ?
                        """,
                (rs, rowNum) -> User.withUsername(rs.getString("username"))
                        .password(rs.getString("password_hash"))
                        .roles(rs.getString("role"))
                        .build(),
                username);

        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        return users.get(0);
    }
}
