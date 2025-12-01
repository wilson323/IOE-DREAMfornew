package net.lab1024.sa.base.config;

import cn.dev33.satoken.config.SaTokenConfig;
import jakarta.annotation.Resource;
import net.lab1024.sa.base.module.support.securityprotect.service.Level3ProtectConfigService;
import org.springframework.context.annotation.Configuration;

/**
 *
 * 涓夌骇绛変繚配置[序列化]=序列化]濆鍖栧悗鏈€浣庢椿璺冮鐜囧叏灞€配置
 *
 * @Author 1024[序列化]=序列化][序列化]=序列化][序列化]=序列化][序列化]=序列化][序列化]=序列化][序列化]=序列化][序列化]=序列化][序列化]=序列化][序列化]=序列化]涘人脸吋设计模式
 * @Date 2024/11/24
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href[序列化]=序列化][序列化]=序列化]https://1024lab.net[序列化]=序列化]>1024[序列化]=序列化][序列化]=序列化][序列化]=序列化][序列化]=序列化][序列化]=序列化][序列化]=序列化][序列化]=序列化][序列化]=序列化]/a> 锛孲ince 2012
 */

@Configuration
public class TokenConfig {

    @Resource
    private Level3ProtectConfigService level3ProtectConfigService;

    // 姝ら厤缃細瑕嗙洊 sa-base.yaml 涓殑配置
    @Resource
    public void configSaToken(SaTokenConfig config) {

        config.setActiveTimeout(level3ProtectConfigService.getLoginActiveTimeoutSeconds());
    }


}

