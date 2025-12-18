package net.lab1024.sa.common.access.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 闂ㄧ璁板綍瀹炰綋绫? * <p>
 * 瀵瑰簲鏁版嵁搴撹〃 t_access_record
 * 鐢ㄤ簬璁板綍闂ㄧ閫氳浜嬩欢锛屽寘鎷垚鍔熷拰澶辫触鐨勯€氳璁板綍
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 缁ф壙BaseEntity鑾峰彇瀹¤瀛楁
 * - 浣跨敤@TableName鎸囧畾鏁版嵁搴撹〃鍚? * - 瀛楁鏁版帶鍒跺湪30涓互鍐? * - 琛屾暟鎺у埗鍦?00琛屼互鍐? * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_record")
public class AccessRecordEntity extends BaseEntity {

    /**
     * 璁板綍ID锛堜富閿級
     */
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    /**
     * 鐢ㄦ埛ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 璁惧ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 鍖哄煙ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 閫氳缁撴灉
     * <p>
     * 1-鎴愬姛
     * 2-澶辫触
     * </p>
     */
    @TableField("access_result")
    private Integer accessResult;

    /**
     * 閫氳鏃堕棿
     */
    @TableField("access_time")
    private LocalDateTime accessTime;

    /**
     * 閫氳绫诲瀷
     * <p>
     * IN-杩涘叆
     * OUT-绂诲紑
     * </p>
     */
    @TableField("access_type")
    private String accessType;

    /**
     * 楠岃瘉鏂瑰紡
     * <p>
     * FACE-浜鸿劯
     * CARD-鍒峰崱
     * FINGERPRINT-鎸囩汗
     * </p>
     */
    @TableField("verify_method")
    private String verifyMethod;

    /**
     * 鐓х墖璺緞
     */
    @TableField("photo_path")
    private String photoPath;
}
