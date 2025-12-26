using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace FixControllers
{
    class Program
    {
        static void Main(string[] args)
        {
            string controllerPath = @"D:\IOE-DREAM\microservices\ioedream-consume-service\src\main\java\net\lab1024\sa\consume\controller";

            var controllers = new List<string>
            {
                "AccountController.java",
                "ConsumeAccountController.java",
                "ConsumeController.java",
                "ConsumeMobileController.java",
                "ConsumeProductController.java",
                "ConsumeRefundController.java",
                "DeviceConsumeController.java",
                "MealOrderController.java",
                "MealOrderMobileController.java",
                "MobileConsumeController.java",
                "PaymentController.java",
                "ReconciliationController.java",
                "RefundApplicationController.java",
                "ReimbursementApplicationController.java",
                "ReportController.java"
            };

            Console.WriteLine("开始修复 Consume Service Controller 文件...");

            foreach (string controllerFile in controllers)
            {
                string filePath = Path.Combine(controllerPath, controllerFile);
                string className = Path.GetFileNameWithoutExtension(controllerFile);

                Console.WriteLine($"修复 {controllerFile}...");

                try
                {
                    if (File.Exists(filePath))
                    {
                        // 读取文件内容
                        byte[] bytes = File.ReadAllBytes(filePath);
                        string content;

                        // 检查并移除BOM
                        if (bytes.Length >= 3 && bytes[0] == 0xEF && bytes[1] == 0xBB && bytes[2] == 0xBF)
                        {
                            content = Encoding.UTF8.GetString(bytes, 3, bytes.Length - 3);
                        }
                        else
                        {
                            content = Encoding.UTF8.GetString(bytes);
                        }

                        // 修复类声明
                        content = System.Text.RegularExpressions.Regex.Replace(
                            content,
                            @"(@PermissionCheck\([^)]+\))\s*\{\s*$",
                            "$1\npublic class " + className + " {",
                            System.Text.RegularExpressions.RegexOptions.Multiline);

                        content = System.Text.RegularExpressions.Regex.Replace(
                            content,
                            @"(@Tag\([^)]+\))\s*\{\s*$",
                            "$1\npublic class " + className + " {",
                            System.Text.RegularExpressions.RegexOptions.Multiline);

                        content = System.Text.RegularExpressions.Regex.Replace(
                            content,
                            @"(@RequestMapping\([^)]+\))\s*\{\s*$",
                            "$1\npublic class " + className + " {",
                            System.Text.RegularExpressions.RegexOptions.Multiline);

                        // 移除 @Slf4j
                        content = content.Replace("@Slf4j", "");

                        // 写回文件（不带BOM）
                        File.WriteAllText(filePath, content, new UTF8Encoding(false));

                        Console.WriteLine($"✓ {controllerFile} 修复完成");
                    }
                    else
                    {
                        Console.WriteLine($"✗ 文件不存在: {filePath}");
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"✗ 修复失败: {controllerFile} - {ex.Message}");
                }
            }

            Console.WriteLine("Consume Service Controller 修复完成!");
        }
    }
}