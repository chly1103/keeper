package wang.crick.keeper.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.*;

@Controller
public class OauthController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${oauth2.server.url:https://gitlab.com}")
    private String gitlabServerUrl;

    @Value("${oauth2.client.id:5e6ee9a97d02281ae979af4b714eabf246b256c631114cc4478e67d3d77bbbb0}")
    private String clientId;
    @Value("${oauth2.client.secret:737898bcc0cdead654a0b94cdc1d4084a6309d7ec708da25bfda2734df72e5b9}")
    private String clientSecret;
    @Value("${oauth2.client.callback.url:http://localhost:9000/callback}")
    private String callbackUrl;

    private static final String CURRENT_USER = "CurrentUser";
    private static final String AUTHORIZATION_KEY = "Authorization";
    private Map<String, User> userStore = new HashMap<>();
    private RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/main")
    @ResponseBody
    public String main(HttpServletRequest req, HttpServletResponse response) {
        User user = (User) RequestContextHolder.getRequestAttributes().getAttribute(CURRENT_USER, RequestAttributes.SCOPE_SESSION);
        return "<html><body>hi:" + user.username + "</body></html>";
    }


    @GetMapping("/callback")
    public String callback(@RequestParam(value = "code", required = false) String code,
                           RedirectAttributes redirectAttributes,
                           HttpServletRequest request) {
        String referer = request.getParameter("referer");
        String accessToken = getAccessToken(code, buildCallbackUrl(referer));
        User user = getUser(accessToken);

        String uuid = UUID.randomUUID().toString();
        userStore.put(uuid, user);
        redirectAttributes.addAttribute(AUTHORIZATION_KEY, uuid);
        return "redirect:" + referer;
    }

    private User getUser(String accessToken) {
        return restTemplate.getForObject(gitlabServerUrl + "/api/v4/user?access_token=" + accessToken, User.class);
    }

    private String buildCallbackUrl(String referer) {
        return callbackUrl + "?referer=" + referer;
    }

    private String getAccessToken(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<JSONAccessTokenResponse> response =
                restTemplate.exchange(gitlabServerUrl + "/oauth/token",
                        HttpMethod.POST,
                        entity,
                        JSONAccessTokenResponse.class);
        return Objects.requireNonNull(response.getBody()).access_token;
    }

    @Configuration
    class WebConfig implements WebMvcConfigurer {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(
                    new HandlerInterceptorAdapter() {
                        @Override
                        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                            String authorization = Optional.ofNullable(request.getParameter(AUTHORIZATION_KEY)).orElse(request.getHeader(AUTHORIZATION_KEY));
                            if (authorization == null) {
                                String referer = request.getRequestURL().toString();
                                String redirectUri = URLEncoder.encode(buildCallbackUrl(referer), "utf-8");
                                String gitlabAuthUrl = gitlabServerUrl + "/oauth/authorize?response_type=code&redirect_uri=" + redirectUri + "&client_id=" + clientId;
                                logger.info("gitlabAuthUrl:{}", gitlabAuthUrl);
                                response.sendRedirect(gitlabAuthUrl);
                                return false;
                            }
                            response.addHeader("Authorization", authorization);
                            RequestContextHolder.getRequestAttributes().setAttribute(CURRENT_USER, userStore.get(authorization), RequestAttributes.SCOPE_SESSION);
                            return super.preHandle(request, response, handler);
                        }
                    })
                    .addPathPatterns("/main", "/test");
        }
    }

    static class JSONAccessTokenResponse implements Serializable {
        public String access_token;
    }

    static class User implements Serializable {
        public String name;
        public String username;
    }
}