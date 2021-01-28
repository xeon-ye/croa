//package com.qinfei.core.entity;
//
//import com.qinfei.qferp.entity.User;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.Collection;
//
//public class JwtUser implements UserDetails {
//
//    private Integer id;
//    private String username;
//    private String password;
//    private Collection<? extends GrantedAuthority> authorities;
//
//    public JwtUser() {
//    }
//
//    // 写一个能直接使用user创建jwtUser的构造器
//    public JwtUser(User user) {
//        id = user.getUserId();
//        username = user.getUserName();
//        password = user.getPassword();
////        authorities = Collections.singleton(new SimpleGrantedAuthority(user.getRole()));
//    }
//
//    // 获取权限信息
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return authorities;
//    }
//
//    @Override
//    public String getPassword() {
//        return password;
//    }
//
//    @Override
//    public String getUsername() {
//        return username;
//    }
//
//    // 账号是否未过期，默认是false
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    // 账号是否未锁定，默认是false
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    // 账号凭证是否未过期，默认是false
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//
//    // 我自己重写打印下信息看的
//    @Override
//    public String toString() {
//        return "JwtUser{" +
//                "id=" + id +
//                ", username='" + username + '\'' +
//                ", password='" + password + '\'' +
//                ", authorities=" + authorities +
//                '}';
//    }
//}
