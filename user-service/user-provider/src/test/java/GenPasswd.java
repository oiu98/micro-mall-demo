import org.junit.Test;
import org.springframework.util.DigestUtils;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description:
 */

public class GenPasswd {

    @Test
    public void genPasswd() {
        String passwd = "chen";
        String md5Passwd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        System.out.println(md5Passwd);
    }
}
