package net.lab1024.sa.access.domain.form;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 闂ㄧ璁板綍鏌ヨ琛ㄥ崟
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤Form鍚庣紑鍛藉悕
 * - 浣跨敤Jakarta楠岃瘉娉ㄨВ
 * - 缁ф壙鍒嗛〉鍙傛暟瑙勮寖
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessRecordQueryForm {

    /**
     * 椤电爜锛堜粠1寮€濮嬶級
     */
    @NotNull(message = "椤电爜涓嶈兘涓虹┖")
    @Min(value = 1, message = "椤电爜蹇呴』澶т簬0")
    private Integer pageNum = 1;

    /**
     * 姣忛〉澶у皬
     */
    @NotNull(message = "姣忛〉澶у皬涓嶈兘涓虹┖")
    @Min(value = 1, message = "姣忛〉澶у皬蹇呴』澶т簬0")
    private Integer pageSize = 20;

    /**
     * 鐢ㄦ埛ID
     */
    private Long userId;

    /**
     * 璁惧ID
     */
    private Long deviceId;

    /**
     * 鍖哄煙ID
     */
    private String areaId;

    /**
     * 寮€濮嬫棩鏈?     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /**
     * 缁撴潫鏃ユ湡
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /**
     * 閫氳缁撴灉
     * <p>
     * 鏋氫妇鍊硷細
     * - 1-鎴愬姛
     * - 2-澶辫触
     * - 3-寮傚父
     * </p>
     */
    private Integer accessResult;
}


