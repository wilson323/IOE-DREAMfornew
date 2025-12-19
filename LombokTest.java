import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class LombokTest {
    private Long id;
    private String name;

    public static void main(String[] args) {
        LombokTest test = new LombokTest();
        test.setId(1L);
        test.setName("test");

        log.info("Created test: {}", test);
        System.out.println("Getter test: " + test.getName());
    }
}