package com.babyrhythm.auth;

import javax.servlet.http.HttpServletRequest;

public class AuthService {

    public static enum LoginState {
        OK, FAILED, UNCONFIRMED
    };

    public static final String USER = "user";
    private static final String FB_LOGIN = "facebookLogin";
    UserService us;

    public AuthService(UserService us) {
        this.us = us;
    }

    public LoginState login(HttpServletRequest request, String id) {
        BabyrhythmUser user = us.getUser(id);
        if (null == user) {
            return LoginState.FAILED;
        }

        request.getSession().setAttribute(USER, user);
        return LoginState.OK;
    }

    public void login(HttpServletRequest request, BabyrhythmUser user) {
        request.getSession().setAttribute(USER, user);
    }
    
    public LoginState facebookLogin(HttpServletRequest request, String id) {
        LoginState state = this.login(request, id);
        if (LoginState.OK == state)
            request.getSession().setAttribute(FB_LOGIN, "pysch");
        return state;
    }

    public static BabyrhythmUser loggedUser(HttpServletRequest request) {
        return (BabyrhythmUser) request.getSession().getAttribute(USER);
    }

    public static boolean isLogged(HttpServletRequest request) {
        return null != request.getSession().getAttribute(USER);
    }

    public static boolean isLoggedViaFacebook(HttpServletRequest request) {
        return null != request.getSession().getAttribute(FB_LOGIN);
    }

    public void logout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER);
        request.getSession().removeAttribute(FB_LOGIN);
        request.getSession().invalidate();
    }

}
