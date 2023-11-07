package com.tecdo.adm.auth;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * 临时认证
 *
 * Created by Zeki on 2023/3/15
 */
@WebFilter(urlPatterns = {"/adm/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader("Token");
        String path = req.getServletPath();
        if ("asdfiouw4uw3h6jjklse".equals(token)
                || "/adm/ae/rta/daily/report".equals(path)
                || path.contains("/dict/")) {
            chain.doFilter(request, response);
        } else {
            response.getWriter().write("auth fail!");
            response.getWriter().flush();
        }
    }
}
