package com.babyrhythm.controller;

import static com.babyrhythm.FacebookGraphUtil.getAccessTokenUrl;
import static com.babyrhythm.FacebookGraphUtil.parseEmailFromState;
import static com.babyrhythm.FacebookGraphUtil.parseFacebookResponce;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.log4j.Logger.getLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.babyrhythm.FacebookGraphUtil;
import com.babyrhythm.UIVersion;
import com.babyrhythm.auth.AuthService;
import com.babyrhythm.auth.BabyrhythmUser;
import com.babyrhythm.auth.UserService;
import com.babyrhythm.mvc.Controller;
import com.babyrhythm.mvc.ErrorView;
import com.babyrhythm.mvc.PathParser;
import com.babyrhythm.mvc.Redirect;
import com.babyrhythm.mvc.TemplateView;
import com.babyrhythm.mvc.View;
import com.google.gson.Gson;

public class FacebookLoginController extends Controller {

    private final static Logger log = getLogger(FacebookLoginController.class);

    private final UserService us;
    private final AuthService as;

    FacebookGraphUtil facebook = new FacebookGraphUtil();
    Gson g = new Gson();

    private final String facebookId;
    private final String facebookSecret;
    private final String redirect;

    public FacebookLoginController(UserService us, AuthService as, String facebookId, String facebookSecret, String host) {
        this.us = us;
        this.as = as;
        this.facebookId = facebookId;
        this.facebookSecret = facebookSecret;
        this.redirect = host + "/fb";
    }

    @Override
    public View get(HttpServletRequest request, PathParser pathInfo) throws Exception {
        if ("error".equals(pathInfo.cutNext())) {
            Map<String, String> ctx = new HashMap<String, String>();
            String second = pathInfo.cutNext();
            if ("differentUser".equals(second))
                ctx.put("message", "Different facebook profile was associated with this account.");

            return new TemplateView("facebookConnected.vm");
        }

        String state = request.getParameter("state");
        String code = request.getParameter("code");
        if (isNotBlank(state) && isNotBlank(code)) {

            String out = facebook.get(getAccessTokenUrl(facebookId, facebookSecret, redirect, code));
            Map<String, String> resp = parseFacebookResponce(out);
            if (StringUtils.isBlank(resp.get("access_token"))) {
                log.debug("Error while retrieving 'access_token'");
                return ErrorView.NOT_FOUND_GENERIC;
            }

            Map<String, Object> meMap = readFacebookUser(resp.get("access_token"));
            if (meMap == null || meMap.isEmpty() || meMap.containsKey("error")) {
                log.debug("Error while accessing facebook user data redirecting to /");
                return new Redirect("/");
            }

            return doFacebookPysch(request, resp, meMap);
        }

        String errorReason = request.getParameter("error_reason"); // error_reason=user_denied
        String error = request.getParameter("error"); // error=access_denied
        String errorDescription = request.getParameter("error_description"); // error_description=The+user+denied+your+request.
        if ((isNotBlank(error) || isNotBlank(errorReason) || isNotBlank(errorDescription)) && isNotBlank(state)) {
            String email = parseEmailFromState(state);
            BabyrhythmUser u = us.getUser(email);
            if (u != null) {
                u.facebookToken = null;
                u.facebookTokenExpiry = 0;
                log.info(" rewoking facebook access_token: " + g.toJson(u));
                us.saveUser(u);
            }
        }

        return new Redirect("/");
    }

    private View doFacebookPysch(HttpServletRequest request, Map<String, String> resp, Map<String, Object> meMap) {
        /**
         * Ok, this is weird shit. If we are here, then validations passed and
         * meMap contains parsed user profile from facebook
         */
        String meMapEmail = (String) meMap.get("email");
        String id = (String) meMap.get("id");

        BabyrhythmUser byfacebookId = us.getUserByFacebook(id);

        /**
         * Check if there is a user logged in
         */
        if (AuthService.isLogged(request)) {

            BabyrhythmUser loggedUser = AuthService.loggedUser(request);
            if (byfacebookId == null) {
                /**
                 * Add facebook account id if there was none before
                 */
                loggedUser.facebookId = id;
                us.saveUser(loggedUser);
                log.debug("Connected facebook id (" + id + ") to " + loggedUser.facebookId);
            } else if (!byfacebookId.facebookId.equals(loggedUser.facebookId)) {
                /**
                 * Show error message if given facebook id is connected to
                 * different email
                 */
                Map<String, Object> ctxt = new HashMap<String, Object>();
                ctxt.put("version", UIVersion.getVersionString());
                return new TemplateView("failedConnectFacebook.vm", ctxt);
            }
            /**
             * Perform login to remove the "connect your facebook" link
             */
            doFacebookLogin(request, resp, loggedUser);
        } else {
            /**
             * There is no current user, but given facebook id might match some
             * existing Torchoo profile
             */
            if (byfacebookId == null) {
                /**
                 * Check if there's existing user with same email
                 */
                byfacebookId = us.getUser(meMapEmail);
                if (byfacebookId != null) {
                    /**
                     * Only add facebook, if there is one
                     */
                    byfacebookId.facebookId = id;
                    byfacebookId.confirmed = true;
//                    byfacebookId.password = defaultString(byfacebookId.password, UUID.randomUUID().toString());
//                    log.debug("Connected facebook id (" + id + ") to " + byfacebookId.email);
                    us.saveUser(byfacebookId);
                } else {
                    /**
                     * Create new profile, if none was found
                     */
                    String email = meMapEmail;
                    String name = (String) meMap.get("name");
                    byfacebookId = new BabyrhythmUser(id, UUID.randomUUID().toString(), name);
                    byfacebookId.confirmed = true;
                    us.saveUser(byfacebookId);
                    log.debug("New user created: " + byfacebookId);
                }
            }
            /**
             * Perform login with facebook
             */
            doFacebookLogin(request, resp, byfacebookId);
        }

        return new Redirect("/");
    }

    private void doFacebookLogin(HttpServletRequest request, Map<String, String> resp, BabyrhythmUser byfacebookId) {
        as.facebookLogin(request, byfacebookId.facebookId);
        byfacebookId.facebookToken = resp.get("access_token");
        byfacebookId.facebookTokenExpiry = Integer.parseInt(resp.get("expires"));
    }

    private Map<String, Object> readFacebookUser(String facebookToken) {
        String me = facebook.get("/me?access_token=" + facebookToken);
        @SuppressWarnings("unchecked")
        Map<String, Object> meMap = (Map<String, Object>) g.fromJson(me, Object.class);
        return meMap;
    };
}
