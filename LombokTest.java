import lombok.Data;
@Data
public class LombokTest {
    private static final Logger log = LoggerFactory.getLogger(LombokTest.class);
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