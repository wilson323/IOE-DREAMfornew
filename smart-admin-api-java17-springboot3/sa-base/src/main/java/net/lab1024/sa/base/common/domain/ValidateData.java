package net.lab1024.sa.base.common.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 鏍￠獙数据闈炵┖鐨勫寘瑁呯被
 *
 * @author 1024Lab
 */
@Data
public class ValidateData<T> {

    @NotNull(message = "数据涓嶈兘涓虹┖")
    private T data;
}
