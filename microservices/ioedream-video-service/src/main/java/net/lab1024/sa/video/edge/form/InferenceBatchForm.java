package net.lab1024.sa.video.edge.form;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/**
 * 批量推理表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class InferenceBatchForm {

    @Valid
    @NotEmpty(message = "推理请求列表不能为空")
    private List<InferenceForm> requests;

    public List<InferenceForm> getRequests() {
        return requests;
    }

    public void setRequests(List<InferenceForm> requests) {
        this.requests = requests;
    }
}


