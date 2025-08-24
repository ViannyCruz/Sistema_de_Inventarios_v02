package com.sistema_de_inventarios_v02.audit;

import com.sistema_de_inventarios_v02.audit.CustomRevisionEntity;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity revision = (CustomRevisionEntity) revisionEntity;

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() &&
                    !authentication.getName().equals("anonymousUser")) {

                String username = authentication.getName();

                // Handle OAuth2 authentication
                if (authentication.getPrincipal() instanceof OAuth2User) {
                    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                    String preferredUsername = oauth2User.getAttribute("preferred_username");
                    if (preferredUsername != null) {
                        username = preferredUsername;
                    } else {
                        String name = oauth2User.getAttribute("name");
                        if (name != null) {
                            username = name;
                        }
                    }

                    // Try to extract user ID if available
                    Object userIdAttr = oauth2User.getAttribute("sub");
                    if (userIdAttr != null) {
                        try {
                            revision.setUserId(Long.parseLong(userIdAttr.toString()));
                        } catch (NumberFormatException e) {
                            // User ID might not be a number, that's okay
                        }
                    }
                }

                revision.setUsername(username);
            } else {
                revision.setUsername("system");
            }
        } catch (Exception e) {
            // Fallback in case of any security context issues
            revision.setUsername("unknown");
        }
    }
}