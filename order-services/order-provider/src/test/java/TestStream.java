import org.junit.Test;

import java.util.ArrayList;

/**
 * @author ZhaoJiachen on 2021/5/27
 * <p>
 * Description:
 */
public class TestStream {

    @Test
    public void testSt() {
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(2);
        integers.add(3);
        integers.add(4);
        integers.parallelStream().forEach(integer -> {
            System.out.println(integer);
        });
    }
}
