
package edu.niet.portal;

public record LoginRequest(
        String username,
        String password,
        String role) {
}
