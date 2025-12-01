#!/bin/bash
echo "批量修复缺失的getter/setter方法..."

# 修复 EmailRequest 类
echo "修复 EmailRequest.java..."
email_file="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo/EmailRequest.java"
if [ -f "$email_file" ]; then
    # 检查是否已有getter/setter，如果没有则添加
    if ! grep -q "public String getTo(" "$email_file"; then
        cat >> "$email_file" << 'EMAIL_GETTERS'

    // Getter和Setter方法
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
    
    public boolean isHtml() { return isHtml; }
    public void setHtml(boolean html) { isHtml = html; }
    
    public EmailPriority getPriority() { return priority; }
    public void setPriority(EmailPriority priority) { this.priority = priority; }
EMAIL_GETTERS
    fi
fi

# 修复 SmsRequest 类
echo "修复 SmsRequest.java..."
sms_file="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo/SmsRequest.java"
if [ -f "$sms_file" ]; then
    if ! grep -q "public String getPhoneNumber(" "$sms_file"; then
        cat >> "$sms_file" << 'SMS_GETTERS'

    // Getter和Setter方法
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    
    public Map<String, Object> getTemplateParams() { return templateParams; }
    public void setTemplateParams(Map<String, Object> templateParams) { this.templateParams = templateParams; }
SMS_GETTERS
    fi
fi

# 修复 SmsResult 类
echo "修复 SmsResult.java..."
sms_result_file="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo/SmsResult.java"
if [ -f "$sms_result_file" ]; then
    if ! grep -q "public boolean isSuccess(" "$sms_result_file"; then
        cat >> "$sms_result_file" << 'SMS_RESULT_GETTERS'

    // Getter和Setter方法
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
SMS_RESULT_GETTERS
    fi
fi

echo "批量getter/setter修复完成"
