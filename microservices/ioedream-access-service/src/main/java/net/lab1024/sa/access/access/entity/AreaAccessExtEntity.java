package net.lab1024.sa.access.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 鍖哄煙闂ㄧ鎵╁睍瀹炰綋绫? * <p>
 * 鐢ㄤ簬绠＄悊鍖哄煙鐨勯棬绂佹墿灞曚俊鎭紝鍖呮嫭闂ㄧ绾у埆銆佹ā寮忋€侀獙璇佹柟寮忕瓑
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 缁ф壙BaseEntity鑾峰彇瀹¤瀛楁
 * - 浣跨敤@TableName鎸囧畾鏁版嵁搴撹〃鍚? * - 瀛楁鏁版帶鍒跺湪30涓互鍐? * - 琛屾暟鎺у埗鍦?00琛屼互鍐? * </p>
 * <p>
 * 涓氬姟鍦烘櫙锛? * - 闂ㄧ鍖哄煙鎵╁睍閰嶇疆
 * - 闂ㄧ绾у埆绠＄悊
 * - 楠岃瘉鏂瑰紡閰嶇疆
 * </p>
 * <p>
 * 鏁版嵁搴撹〃锛歵_access_area_ext锛堟牴鎹瓺AO涓殑SQL鎺ㄦ柇锛? * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_area_ext")
@SuppressWarnings("PMD.ShortVariable")
public class AreaAccessExtEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 鎵╁睍ID锛堜富閿級
     * <p>
     * 缁熶竴浣跨敤id浣滀负涓婚敭瀛楁鍚嶏紝閫氳繃@TableId鐨剉alue灞炴€ф槧灏勫埌鏁版嵁搴撳垪ext_id
     * 绗﹀悎瀹炰綋绫讳富閿懡鍚嶈鑼冿細缁熶竴浣跨敤id锛岄伩鍏嶆柟娉曞紩鐢ㄩ敊璇?     * </p>
     */
    @TableId(value = "ext_id", type = IdType.AUTO)
    private Long id;

    /**
     * 鍖哄煙ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 闂ㄧ绾у埆
     */
    @TableField("access_level")
    private Integer accessLevel;

    /**
     * 闂ㄧ妯″紡
     */
    @TableField("access_mode")
    private String accessMode;

    /**
     * 楠岃瘉鏂瑰紡
     */
    @TableField("verification_mode")
    private String verificationMode;

    /**
     * 璁惧鏁伴噺
     */
    @TableField("device_count")
    private Integer deviceCount;

    /**
     * 鎵╁睍閰嶇疆锛圝SON鏍煎紡锛?     */
    @TableField("ext_config")
    private String extConfig;
}


