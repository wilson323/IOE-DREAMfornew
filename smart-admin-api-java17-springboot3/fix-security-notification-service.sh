#!/bin/bash
echo "修复 SecurityNotificationServiceImpl.java 中的 builder 和 getter/setter 问题..."

file="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/impl/SecurityNotificationServiceImpl.java"

# 备份原文件
cp "$file" "$file.backup"

# 修复 EmailRequest.builder() 问题
sed -i 's/EmailRequest.builder()/new EmailRequest()/g' "$file"

# 修复链式调用问题 - 将 .xxx() 改为 setter 调用
sed -i 's/\.to(/\.setTo(/g' "$file"
sed -i 's/\.subject(/\.setSubject(/g' "$file"
sed -i 's/\.content(/\.setContent(/g' "$file"
sed -i 's/\.attachments(/\.setAttachments(/g' "$file"
sed -i 's/\.isHtml(/\.setHtml(/g' "$file"
sed -i 's/\.priority(/\.setPriority(/g' "$file"

# 移除最后的 .build() 并添加正确的分号
sed -i 's/\.build();/;/g' "$file"

echo "已修复 EmailRequest builder 问题"

# 修复 SmsRequest.builder() 问题
sed -i 's/SmsRequest.builder()/new SmsRequest()/g' "$file"
sed -i 's/\.phoneNumber(/\.setPhoneNumber(/g' "$file"
sed -i 's/\.message(/\.setMessage(/g' "$file"
sed -i 's/\.templateId(/\.setTemplateId(/g' "$file"
sed -i 's/\.templateParams(/\.setTemplateParams(/g' "$file"

echo "已修复 SmsRequest builder 问题"

# 修复 PushNotificationRequest.builder() 问题
sed -i 's/PushNotificationRequest.builder()/new PushNotificationRequest()/g' "$file"
sed -i 's/\.title(/\.setTitle(/g' "$file"
sed -i 's/\.body(/\.setBody(/g' "$file"
sed -i 's/\.deviceToken(/\.setDeviceToken(/g' "$file"

echo "已修复 PushNotificationRequest builder 问题"

# 修复 WechatNotificationRequest.builder() 问题
sed -i 's/WechatNotificationRequest.builder()/new WechatNotificationRequest()/g' "$file"
sed -i 's/\.openId(/\.setOpenId(/g' "$file"
sed -i 's/\.templateId(/\.setTemplateId(/g' "$file"
sed -i 's/\.data(/\.setData(/g' "$file"

echo "已修复 WechatNotificationRequest builder 问题"

# 修复 BatchNotificationResult setter 问题
sed -i 's/result\.setTotalCount(/result.setTotalCount(/g' "$file"
sed -i 's/result\.setSuccessCount(/result.setSuccessCount(/g' "$file"
sed -i 's/result\.setFailureCount(/result.setFailureCount(/g' "$file"
sed -i 's/result\.setSuccessResults(/result.setSuccessResults(/g' "$file"
sed -i 's/result\.setFailureResults(/result.setFailureResults(/g' "$file"

# 修复 getter 问题
sed -i 's/\.getTotalCount()/getTotalCount()/g' "$file"
sed -i 's/\.getSuccessCount()/getSuccessCount()/g' "$file"
sed -i 's/\.getFailureCount()/getFailureCount()/g' "$file"

echo "已修复 BatchNotificationResult getter/setter 问题"

echo "SecurityNotificationServiceImpl.java 修复完成"
