import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.user.utils.JwtTokenUtils;
import org.junit.Test;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description: Jwt生成工具 已废弃
 */

public class GenPasswd {

    @Test
    public void genPasswd() {
        String passwd = "chen";
        String md5Passwd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        System.out.println(md5Passwd);
    }

    @Test
    public void genJson() throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("userId",122432141);
        map.put("username","chen");
        ObjectMapper objectMapper = new ObjectMapper();
        String s= null;
        try {
            s = objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println(s);
    }

    @Test
    public void testJwtUtils() {
        String msg = "1224313";
        JwtTokenUtils build = JwtTokenUtils.builder().msg(msg).build();
        String token = build.creatJwtToken();
        System.out.println(token);

        token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJjaWdnYXIiLCJleHAiOjE2MjE4NTcxNTMsInVzZXIiOiJGREUwMzQ3MDFBMkRGNTk3RkZFQkE5ODgzNTY5RTZBNiJ9.XMANCgAS3OXLhoDZroDIymB6daP0ZNY_jZZYNbA6Ai0";

        JwtTokenUtils tokenUtils = JwtTokenUtils.builder().token(token).build();
        String freeJwt = tokenUtils.freeJwt();
        System.out.println(freeJwt);
    }

    @Test
    public void genJackson(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 不转化null数据
        Map<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("uid",123414);
        userInfoMap.put("username",null);
        userInfoMap.put("file",null);
        try {
            String jwtMsg = objectMapper.writeValueAsString(userInfoMap);
            System.out.println(jwtMsg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
