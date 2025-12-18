package net.lab1024.sa.access.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.BiometricTemplateDao;
import net.lab1024.sa.access.dao.BiometricConfigDao;
import net.lab1024.sa.access.dao.BiometricAuthRecordDao;
import net.lab1024.sa.access.manager.BiometricTemplateManager;
import net.lab1024.sa.common.organization.dao.DeviceDao;

/**
 * Manager閰嶇疆绫? * <p>
 * 鐢ㄤ簬灏嗛棬绂佹ā鍧楃壒鏈夌殑Manager瀹炵幇绫绘敞鍐屼负Spring Bean
 * </p>
 * <p>
 * 娉ㄦ剰锛氬叕鍏盡anager锛圢otificationManager銆乄orkflowApprovalManager绛夛級
 * 宸茬敱CommonBeanAutoConfiguration缁熶竴瑁呴厤锛屾棤闇€鍦ㄦ閲嶅瀹氫箟
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-12-14 绉婚櫎閲嶅鐨勫叕鍏盉ean瀹氫箟锛屾敼鐢ㄧ粺涓€鑷姩瑁呴厤
 * @updated 2025-12-17 娣诲姞BiometricTemplateManager Bean娉ㄥ唽锛屼慨澶峂anager娉ㄨВ杩濊
 */
@Slf4j
@Configuration("accessManagerConfiguration")
public class ManagerConfiguration {

    @Resource
    private BiometricTemplateDao biometricTemplateDao;

    @Resource
    private BiometricConfigDao biometricConfigDao;

    @Resource
    private BiometricAuthRecordDao biometricAuthRecordDao;

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 娉ㄥ唽BiometricTemplateManager涓篠pring Bean
     * <p>
     * 鐢熺墿璇嗗埆妯℃澘绠＄悊鍣紝璐熻矗澶嶆潅鐨勭敓鐗╄瘑鍒ā鏉夸笟鍔℃祦绋嬬紪鎺?     * 鍖呮嫭锛氭ā鏉挎敞鍐屻€佺壒寰佸尮閰嶃€佹椿浣撴娴嬨€?:N璇嗗埆绛?     * </p>
     *
     * @return BiometricTemplateManager瀹炰緥
     */
    @Bean
    public BiometricTemplateManager biometricTemplateManager() {
        log.info("[BiometricTemplateManager] 鍒濆鍖栫敓鐗╄瘑鍒ā鏉跨鐞嗗櫒");
        return new BiometricTemplateManager(
                biometricTemplateDao,
                biometricConfigDao,
                biometricAuthRecordDao,
                deviceDao,
                redisTemplate
        );
    }

    // 鍏叡Bean锛圢otificationManager銆乄orkflowApprovalManager锛夊凡鐢盋ommonBeanAutoConfiguration缁熶竴瑁呴厤
    // 姝ゅ浠呬繚鐣欓棬绂佹ā鍧楃壒鏈夌殑Manager瀹氫箟

}