import com.mall.pay.PayCoreService;
import com.mall.pay.bootstrap.PayProviderApplication;
import com.mall.pay.dto.PaymentRequest;
import com.mall.pay.dto.alipay.AlipaymentResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ZhaoJiachen on 2021/6/1
 * <p>
 * Description:
 */

@SpringBootTest(classes = PayProviderApplication.class)
@RunWith(SpringRunner.class)
public class AlipayTest {

    @Autowired
    private PayCoreService payCoreService;

    @Test
    public void testAlipay() {
        PaymentRequest request = new PaymentRequest();
        request.setUserId(124214L);
        request.setSubject("fdagdsa");
        request.setAddressId(43141L);
        request.setOrderFee(new BigDecimal(2332531));
        request.setPayChannel("dsafdsaf");
        request.setSpbillCreateIp("dsajfioafd");
        request.setTradeNo("adsiofeqiwojklafjewqfewqf");
        AlipaymentResponse alipaymentResponse = payCoreService.aliPay(request);
        String qrCode = alipaymentResponse.getQrCode();
    }

    public static void main(String[] args) {
        String yyyymmdd = new SimpleDateFormat("yyyyMMdd").format(new Date());
        System.out.println(yyyymmdd);
    }
}
