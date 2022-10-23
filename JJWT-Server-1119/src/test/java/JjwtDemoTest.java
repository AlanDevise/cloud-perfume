import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.Base64Codec;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Filename: JjwtDemoTest.java
 * @Package: PACKAGE_NAME
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月23日 12:32
 */

@SpringBootTest
public class JjwtDemoTest {

    // 创建token
    @Test
    public void testCreateToken() {
        // 创建jwtBuilder对象
        JwtBuilder jwtBuilder = Jwts.builder()
                // 声明的标识
                .setId("8888")
                // 主体，用户{"sub":"Rose"}
                .setSubject("Rose")
                // 创建日期
                .setIssuedAt(new Date())
                // 加密方式，盐
                .signWith(SignatureAlgorithm.HS256, "xxxx");
        // 获取jwt的token
        String token = jwtBuilder.compact();
        System.out.println(token);
        System.out.println("--------------------");
        String[] split = token.split("\\.");
        System.out.println(Base64Codec.BASE64.decodeToString(split[0]));
        System.out.println(Base64Codec.BASE64.decodeToString(split[1]));
        System.out.println(Base64Codec.BASE64.decodeToString(split[2]));
    }

    // 解析token
    @Test
    public void testParseToken() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODg4Iiwic3ViIjoiUm9zZSIsImlhdCI6MTY2NjQ5OTk2M30." +
                "AiR4C57jZIptDRxJRC6LGcLxznBt_S9BJLVXj0eT1aM";
        // 解析token获取负载中声明的对象
        Claims claims = Jwts.parser()
                .setSigningKey("xxxx")
                .parseClaimsJws(token)
                .getBody();
        System.out.println("id: " + claims.getId());
        System.out.println("subject: " + claims.getSubject());
        System.out.println("issuedAt: " + claims.getIssuedAt());
    }

    // 创建token带过期时间
    @Test
    public void testCreateTokenHasExp() {
        // 当前系统时间
        long now = System.currentTimeMillis();
        // 过期时间
        long exp = now + 60 * 1000;
        // 创建jwtBuilder对象
        JwtBuilder jwtBuilder = Jwts.builder()
                // 声明的标识
                .setId("8888")
                // 主体，用户{"sub":"Rose"}
                .setSubject("Rose")
                // 创建日期
                .setIssuedAt(new Date())
                // 加密方式，盐
                .signWith(SignatureAlgorithm.HS256, "xxxx")
                // 设置过期时间
                .setExpiration(new Date(exp));
        // 获取jwt的token
        String token = jwtBuilder.compact();
        System.out.println(token);
        System.out.println("--------------------");
        String[] split = token.split("\\.");
        System.out.println(Base64Codec.BASE64.decodeToString(split[0]));
        System.out.println(Base64Codec.BASE64.decodeToString(split[1]));
        System.out.println(Base64Codec.BASE64.decodeToString(split[2]));
    }

    // 解析token带过期时间
    @Test
    public void testParseTokenHasExp() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODg4Iiwic3ViIjoiUm9zZSIsImlhdCI6MTY2NjUwMDc5OSwiZXhwIjoxNjY2NTAwODU5fQ.lLEN5xiT4UUmaO3KN15nyUaPuzfYuFKbn1hL9i3ghd0";
        // 解析token获取负载中声明的对象
        Claims claims = Jwts.parser()
                .setSigningKey("xxxx")
                .parseClaimsJws(token)
                .getBody();
        System.out.println("id: " + claims.getId());
        System.out.println("subject: " + claims.getSubject());
        System.out.println("issuedAt: " + claims.getIssuedAt());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("签发时间：" + simpleDateFormat.format(claims.getIssuedAt()));
        System.out.println("过期时间：" + simpleDateFormat.format(claims.getExpiration()));
        System.out.println("当前时间：" + simpleDateFormat.format(new Date()));
    }

    // 创建token（自定义声明）
    @Test
    public void testCreateTokenByClaims() {
        // 当前系统时间
        long now = System.currentTimeMillis();
        // 过期时间
        long exp = now + 60 * 1000;
        // 创建jwtBuilder对象
        JwtBuilder jwtBuilder = Jwts.builder()
                // 声明的标识
                .setId("8888")
                // 主体，用户{"sub":"Rose"}
                .setSubject("Rose")
                // 创建日期
                .setIssuedAt(new Date())
                // 加密方式，盐
                .signWith(SignatureAlgorithm.HS256, "xxxx")
                // 设置过期时间
                .setExpiration(new Date(exp))
                .claim("roles","admin")
                .claim("logo","xxx.jpg");
                // 直接传入map
                // .addClaims()
        // 获取jwt的token
        String token = jwtBuilder.compact();
        System.out.println(token);
        System.out.println("--------------------");
        String[] split = token.split("\\.");
        System.out.println(Base64Codec.BASE64.decodeToString(split[0]));
        System.out.println(Base64Codec.BASE64.decodeToString(split[1]));
        System.out.println(Base64Codec.BASE64.decodeToString(split[2]));
    }

    // 解析token(自定义声明)
    @Test
    public void testParseTokenByClaims() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODg4Iiwic3ViIjoiUm9zZSIsImlhdCI6MTY2NjUwMTI1NiwiZXhwIjoxNjY2NTAxMzE1LCJyb2xlcyI6ImFkbWluIiwibG9nbyI6Inh4eC5qcGcifQ.vIjR2v1RQfsAgC5l0hTHTsj9UMUV89upSLdagujl56Q";
        // 解析token获取负载中声明的对象
        Claims claims = Jwts.parser()
                .setSigningKey("xxxx")
                .parseClaimsJws(token)
                .getBody();
        System.out.println("id: " + claims.getId());
        System.out.println("subject: " + claims.getSubject());
        System.out.println("issuedAt: " + claims.getIssuedAt());
        System.out.println("roles: "+claims.get("roles"));
        System.out.println("logo: "+claims.get("logo"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("签发时间：" + simpleDateFormat.format(claims.getIssuedAt()));
        System.out.println("过期时间：" + simpleDateFormat.format(claims.getExpiration()));
        System.out.println("当前时间：" + simpleDateFormat.format(new Date()));

    }
}

