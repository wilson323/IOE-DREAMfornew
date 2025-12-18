package net.lab1024.sa.access;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IOE-DREAM 闂ㄧ绠＄悊鏈嶅姟鍚姩绫? * <p>
 * 绔彛: 8090
 * 鑱岃矗: 鎻愪緵闂ㄧ鎺у埗銆侀€氳璁板綍銆佹潈闄愮鐞嗐€佸尯鍩熺鐞嗙瓑涓氬姟API
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖:
 * - 浣跨敤@SpringBootApplication娉ㄨВ
 * - 鍚敤Nacos鏈嶅姟鍙戠幇(@EnableDiscoveryClient)
 * - 绮剧‘閰嶇疆鎵弿璺緞(鍙壂鎻忛渶瑕佺殑鍏叡鍖呭拰access鍖?
 * - 姝ｇ‘閰嶇疆MapperScan璺緞
 * </p>
 * <p>
 * 鏍稿績鍔熻兘妯″潡:
 * - 闂ㄧ鎺у埗:闂ㄧ璁惧绠＄悊銆侀€氳鎺у埗銆佸妯℃€侀獙璇? * - 閫氳璁板綍:閫氳璁板綍鏌ヨ銆佺粺璁″垎鏋愩€佸紓甯稿憡璀? * - 鏉冮檺绠＄悊:鍖哄煙鏉冮檺銆佹椂闂存潈闄愩€佷汉鍛樻潈闄? * - 鍖哄煙绠＄悊:鍖哄煙閰嶇疆銆佸尯鍩熷叧鑱斻€佹潈闄愮户鎵? * </p>
 * <p>
 * <b>鍐呭瓨浼樺寲璇存槑</b>:
 * scanBasePackages绮剧‘閰嶇疆,鍙壂鎻忛棬绂佹湇鍔￠渶瑕佺殑鍏叡鍖?
 * 鍑忓皯涓嶅繀瑕佺殑绫诲姞杞?浼樺寲鍐呭瓨浣跨敤銆? * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SpringBootApplication(
    scanBasePackages = {
        // 闂ㄧ鏈嶅姟鑷韩鍖?        "net.lab1024.sa.access",
        // 鏍稿績閰嶇疆(蹇呴渶)
        "net.lab1024.sa.common.config",
        // 鍝嶅簲鍜屽紓甯稿鐞?        "net.lab1024.sa.common.response",
        "net.lab1024.sa.common.exception",
        // 宸ュ叿绫?        "net.lab1024.sa.common.util",
        // 瀹夊叏璁よ瘉
        "net.lab1024.sa.common.security",
        // 闂ㄧ鐩稿叧鍏叡妯″潡
        "net.lab1024.sa.common.access",
        // 缁勭粐鏈烘瀯
        "net.lab1024.sa.common.organization",
        // RBAC鏉冮檺
        "net.lab1024.sa.common.rbac"
    },
    exclude = {
        HibernateJpaAutoConfiguration.class
    }
)
@EnableDiscoveryClient
@EnableScheduling
@MapperScan(basePackages = {
    // Common妯″潡DAO
    "net.lab1024.sa.common.auth.dao",
    "net.lab1024.sa.common.rbac.dao",
    "net.lab1024.sa.common.system.employee.dao",
    "net.lab1024.sa.common.system.dao",
    "net.lab1024.sa.common.access.dao",
    "net.lab1024.sa.common.organization.dao",
    "net.lab1024.sa.common.audit.dao",
    "net.lab1024.sa.common.dict.dao",
    "net.lab1024.sa.common.menu.dao",
    // Workflow妯″潡DAO(鏀寔瀹℃壒娴佺▼)
    "net.lab1024.sa.common.workflow.dao",
    // Access妯″潡DAO
    "net.lab1024.sa.access.dao"
})
public class AccessServiceApplication {

    /**
     * 涓诲惎鍔ㄦ柟娉?     * <p>
     * 鍚姩IOE-DREAM闂ㄧ绠＄悊鏈嶅姟,绔彛8090
     * </p>
     *
     * @param args 鍚姩鍙傛暟
     */
    public static void main(String[] args) {
        SpringApplication.run(AccessServiceApplication.class, args);
    }
}

