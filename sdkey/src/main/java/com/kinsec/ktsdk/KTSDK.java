package com.kinsec.ktsdk;

// 文件属性
class KTFILEATTRIBUTE {
    int nFileSize;                // 文件大小
    int nReadRights;            // 读文件是否需要用户权限	0－不需要；1－需要
    int nWriteRights;            // 写文件是否需要用户权限	0－不需要；1－需要

    public KTFILEATTRIBUTE() {
        nFileSize = 0;
        nReadRights = 0;
        nWriteRights = 0;
    }
}

// KTCORE签名信息
class SIGN_INFO {
    String strSignFileName;
    String strSignTime;
    byte[] byCert;
    int nCertStatus;

    public SIGN_INFO() {
        strSignFileName = "";
        strSignTime = "";
        byCert = null;
        nCertStatus = 0;
    }
}

public class KTSDK {
    /////////////////////////////////////////////////////////////////////////////
    // 初始化

    // 设置日志文件路径
    // 若不需要日志，可不调用该函数
    public static native int KTSDK_SetLogFilePath(String strLogFilePath);

    // PKISDK调用初始化
    public static native int KTSDK_Initialize();

    // 结束PKISDK调用
    public static native int KTSDK_Finalize();

    // 获取错误码
    // 0	调用成功
    public static native int KTSDK_GetErrorCode();

    // 获取错误信息
    public static native String KTSDK_GetErrorString();

    /////////////////////////////////////////////////////////////////////////////
    // 设置

    // 设置时间戳服务器地址及端口，如：192.168.5.177:80
    // 若不使用时间戳，设置其为空
    public static native int KTSDK_SetTSSURL(String strURL);

    // 清空证书链
    public static native int KTSDK_ClearCACerts();

    // 设置证书链
    // 若要验证时间戳，必须设置证书链
    public static native int KTSDK_AddCACert(byte[] byCert);

    /////////////////////////////////////////////////////////////////////////////
    // 密码算法

    // BASE64编码
    public static native String KTSDK_Base64Encode(byte[] byIn);

    // BASE64解码
    public static native byte[] KTSDK_Base64Decode(String strIn);

    // SHA1摘要
    public static native byte[] KTSDK_HashData(byte[] byIn);

    // 验证签名
    public static native int KTSDK_VerifySignedData(byte[] byData, byte[] bySign, byte[] byCert);

    // 用证书做非对称加密
    public static native byte[] KTSDK_EncryptWithCert(byte[] byIn, byte[] byCert);

    /////////////////////////////////////////////////////////////////////////////
    // 证书解析

    // 解析证书。调用证书解析后续函数时，必须先调用该函数
    public static native int KTSDK_ParseCert(byte[] byCert);

    // 是否SM2证书
    public static native boolean KTSDK_IsSM2Cert();

    // 获取证书序列号
    public static native String KTSDK_GetCertSN();

    // 获取证书颁发者DN
    public static native String KTSDK_GetCertIssuerDN();

    // 获取证书DN
    public static native String KTSDK_GetCertDN();

    // 获取证书CN
    public static native String KTSDK_GetCertCN();

    // 获取证书有效期开始时间
    public static native String KTSDK_GetCertNotBefore();

    // 获取证书有效期结束时间
    public static native String KTSDK_GetCertNotAfter();

    // 获取证书密钥用法
    public static native int KTSDK_GetCertKeyUsage();

    /////////////////////////////////////////////////////////////////////////////
    // 设备（SD卡）操作

    // 设备调用初始化
    public static native int KTSDK_Device_Initialize(String strLibDir, String strAndroidPackageName);

    // 结束设备调用
    public static native int KTSDK_Device_Finalize();

    // 格式化设备（格式化会将设备内所有信息删除，慎用！！！）
    public static native int KTSDK_Device_InitDevice(String strAdminPin, String strUserPin);

    // 格式化设备，飞天专用（格式化会将设备内所有信息删除，慎用！！！）
    public static native int KTSDK_Device_InitDevice_FT(String strAdminPin, String strUserPin);

    // 打开设备
    public static native int KTSDK_Device_Open();

    // 打开指定设备
    // 海泰		101
    // 飞天		102	（目前不支持）
    // 华申		201
    // 三所		202
    public static native int KTSDK_Device_Open2(int nDevType);

    // 关闭设备
    public static native int KTSDK_Device_Close();

    // 获取设备类型
    public static native int KTSDK_Device_GetKeyType();

    // 获取设备类型（同KTSDK_Device_GetKeyType）
    public static native int KTSDK_Device_GetDevType();

    // 获取设备序列号
    public static native String KTSDK_Device_GetDevSN();

    // 登录设备
    public static native int KTSDK_Device_Login(String strPin, boolean bUser);

    // 退出设备登录
    public static native int KTSDK_Device_Logout();

    // 判断是否已登录（不支持SKF接口类型的设备）
    public static native boolean KTSDK_Device_IsLogin(boolean bUser);

    // 设置（修改）PIN
    public static native int KTSDK_Device_SetPin(String strPin, String strNewPin, boolean bUser);

    // 解锁用户PIN
    public static native int KTSDK_Device_UnlockUserPin(String strAdminPin, String strUserPin);

    // 获取PIN剩余尝试次数
    public static native int KTSDK_Device_GetRemainRetryCount(boolean bUser);

    // 获取证书个数
    // 返回结果为1表示单证，结果为2表示双证
    public static native int KTSDK_Device_GetCertNo();

    // 设置当前操作的证书
    // 1	签名证书
    // 2	加密证书
    public static native int KTSDK_Device_SetCurrentCert(int nCertNo);

    // 读取证书
    // 请先设置当前操作的证书
    public static native byte[] KTSDK_Device_ReadCert();

    // 写入证书（慎用！！！）
    // 请先设置当前操作的证书
    public static native int KTSDK_Device_WriteCert(byte[] byCert);

    // RSA签名
    // 请先设置当前操作的证书
    public static native byte[] KTSDK_Device_RSASign(byte[] byIn);

    // SM2签名
    // 请先设置当前操作的证书
    public static native byte[] KTSDK_Device_SM2Sign(byte[] byIn, byte[] byCert);

    // RSA解密
    // 请先设置当前操作的证书
    public static native byte[] KTSDK_Device_RSADecrypt(byte[] byIn);

    // SM2解密
    // 请先设置当前操作的证书
    public static native byte[] KTSDK_Device_SM2Decrypt(byte[] byIn);

    // 枚举文件。如有多个文件，文件名以“&&”分隔
    public static native String KTSDK_Device_EnumFiles();

    // 创建文件
    // nReadRights:		读文件是否需要用户权限
    // nWriteRights:	写文件是否需要用户权限
    // 0	不需要
    // 1	需要
    public static native int KTSDK_Device_CreateFile(String strFileName, int nFileSize, int nReadRights, int nWriteRights);

    // 删除文件
    public static native int KTSDK_Device_DeleteFile(String strFileName);

    // 获取文件信息
    public static native int KTSDK_Device_GetFileInfo(String strFileName, KTFILEATTRIBUTE FileAttribute);

    // 读文件
    public static native byte[] KTSDK_Device_ReadFile(String strFileName, int nOffset, int nSize);

    // 写文件
    public static native int KTSDK_Device_WriteFile(String strFileName, int nOffset, byte[] byIn);

    /////////////////////////////////////////////////////////////////////////////
    // KTCORE密码算法

    // KTCORE调用初始化
    public static native int KTCore_Initialize();

    // 结束KTCORE调用
    public static native int KTCore_Finalize();

    // KTCORE签名
    public static native String KTCore_Sign(String strPlainFile, String strSignatureFile, boolean bPlainInSignature);

    // KTCORE验证签名
    public static native String KTCore_VerifySign(String strSignatureFile, String strPlainFile, SIGN_INFO SignInfo);

    // KTCORE数字信封
    public static native String KTCore_Envelop(String strPlainFiles, String strCipherFile, String strEnvelopCerts);

    // 解密KTCORE数字信封
    public static native String KTCore_Develop(String strCipherFile, String strPlainFileOrFolder);
}