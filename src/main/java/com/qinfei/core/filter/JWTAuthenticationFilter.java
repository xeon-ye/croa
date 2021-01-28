//package com.qinfei.core.filter;
//
//import com.qinfei.core.entity.JwtUser;
//import com.qinfei.core.utils.JwtTokenUtils;
//import com.qinfei.qferp.entity.User;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.ArrayList;
//
//@Slf4j
//public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
//
//    private AuthenticationManager authenticationManager;
//
//    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
//        this.authenticationManager = authenticationManager;
//        super.setFilterProcessesUrl("/auth/login");
////        this.authenticationManager = authenticationManager;
//    }
//
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request,
//                                                HttpServletResponse response) throws AuthenticationException {
//        // 从输入流中获取到登录的信息
//        try {
//            User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
//            return authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword(), new ArrayList<>())
//            );
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    // 成功验证后调用的方法
//    // 如果验证成功，就生成token并返回
//    @Override
//    protected void successfulAuthentication(HttpServletRequest request,
//                                            HttpServletResponse response,
//                                            FilterChain chain,
//                                            Authentication authResult) {
//
//        // 查看源代码会发现调用getPrincipal()方法会返回一个实现了`UserDetails`接口的对象
//        // 所以就是JwtUser啦
//        JwtUser jwtUser = (JwtUser) authResult.getPrincipal();
//        log.debug("jwtUser:" + jwtUser.toString());
//        String token = JwtTokenUtils.createToken(jwtUser.getUsername(), false);
//        // 返回创建成功的token
//        // 但是这里创建的token只是单纯的token
//        // 按照jwt的规定，最后请求的格式应该是 `Bearer token`
//        response.setHeader("token", JwtTokenUtils.TOKEN_PREFIX + token);
//    }
//
//    // 这是验证失败时候调用的方法
//    @Override
//    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
//        response.getWriter().write("authentication failed, reason: " + failed.getMessage());
//    }
//}