import com.mall.shopping.bootstrap.ShoppingProviderApplication;
import com.mall.shopping.bootstrap.ShoppingProviderApplicationTests;
import com.mall.shopping.dto.CartProductDto;
import com.mall.shopping.services.cache.CartManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * @author ZhaoJiachen on 2021/5/23
 * <p>
 * Description:
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShoppingProviderApplication.class)
public class GenCart {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    CartManager cartManager;

    @Test
    public void genCartItemDto() {

        CartProductDto cartProductDto = new CartProductDto();
        cartProductDto.setProductId(100057601L);
        cartProductDto.setProductImg("https://resource.smartisan.com/resource/d9586f7c5bb4578e3128de77a13e4d85.png");
        cartProductDto.setProductNum(1L);
        cartProductDto.setProductName("Smartisan T恤 皇帝的新装");
        cartProductDto.setChecked("true");
        cartProductDto.setSalePrice(new BigDecimal(149));
        cartProductDto.setLimitNum(100L);

        RMap<String, Object> cart_74 = redissonClient.getMap("cart_75");

        cart_74.put(String.valueOf(cartProductDto.getProductId()),cartProductDto);
    }

    @Test
    public void testCartManager() {
        List<CartProductDto> carts = cartManager.getCarts(String.valueOf(74));
        System.out.println(carts);
    }

    @Test
    public void testRedisson() {
        RMap<Object, Object> cart_74 = redissonClient.getMap("cart_74");
        Collection<Object> values = cart_74.values();
        System.out.println(values);
    }
}
