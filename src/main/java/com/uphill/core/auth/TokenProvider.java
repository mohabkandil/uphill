package com.uphill.core.auth;

import com.uphill.core.domain.Admin;

public interface TokenProvider {
    
    String generateToken(Admin admin);
    
    boolean validateToken(String token);
    
    Long getUserIdFromToken(String token);
}
