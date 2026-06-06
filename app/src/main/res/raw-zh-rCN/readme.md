# SRPatch X

[SRPatch X](https://github.com/KhunHtetzNaing/SRPatch-X) 是一款现代的 Android APK 签名验证绕过工具,提供多种 Hook 技术:

-   **包管理器 Hook (PMS Hook)** — 在系统服务层拦截签名校验
-   **文件系统 Hook (IO Hook)** — 通过直接访问文件绕过签名验证
-   **系统调用 Hook (SVC Hook)** — 在内核系统调用层绕过检查

## 功能

- [x]  多种签名绕过方式,支持配置防护强度
- [x] 现代 Material Design 3 界面
- [x] 路径重定向以实现更高级的防护
- [ ] 支持自定义密钥库的 APK 签名

## 说明

**SRPatch X** 是 **SRPatch** (`com.lingxing.srpatch`) 的扩展版本,最初由 **LingXing** 开发。由于 SRPatch 已停止维护,且其官方站点 ([srpatch.cc](https://srpatch.cc/)) 已无法访问,我在提取 `libSRPatch.so` 后,围绕该核心库构建了本应用。

**相比原版 SRPatch 的改进:**

-   新增 APK 签名功能
-   提升稳定性与兼容性

**版权声明:**  
"SRPatch" 名称与 `libSRPatch` 核心库并非本人所有,版权归原作者 LingXing 所有。

**用途说明:**  
最初为个人自用而构建,现面向 Android 安全研究者与开发者开源,希望能为大家带来帮助。

## 免责声明

本工具仅供学习教育用途及合法的安全研究使用。使用者需确保其行为符合所在地法律法规与服务条款。作者不对任何不当使用承担责任。

## 运行要求

-   **设备:** Android 8.0+ (API 26+)
-   **架构:** 仅支持 arm64-v8a
