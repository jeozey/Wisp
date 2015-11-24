package com.rj.connection;

/**
 * 加载SSL数字证书
 */
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
public class SSLServer {
	
	/**
	 * SSLContext 实例
	 */
    private static SSLContext sslContext ;
    
    /**
     * 获取SSLContext实例
     * @return
     */
    public static SSLContext getSSLContextInstance() throws IllegalAccessException {
        if (sslContext == null) {
            throw new IllegalAccessException("sslContext is null");
        }
        return sslContext;

    }
    
	/**
	 * 初始化SSL
	 * @param kclient_is
	 * @param tclient_is
	 * @throws Exception 
	 */
	public static void initSSL(InputStream kclient_is,InputStream tclient_is, 
			String keyPassword, String trustPassword) throws Exception {
        try {
			//取得SSL的SSLContext实例  
        	sslContext = SSLContext.getInstance("TLS");
        	
        	//取得KeyManagerFactory和TrustManagerFactory的X509密钥管理器实例
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            
            //取得BKS密库实例  
            KeyStore ks = KeyStore.getInstance("BKS");
            KeyStore tks = KeyStore.getInstance("BKS");
            
            //加客户端载证书和私钥,通过读取资源文件的方式读取密钥和信任证书  
            ks.load(kclient_is, keyPassword.toCharArray());
            tks.load(tclient_is, trustPassword.toCharArray());
            
            //初始化密钥管理器 
            kmf.init(ks, keyPassword.toCharArray());
            tmf.init(tks);
            
            //初始化SSLContext  
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            
        } catch (Exception e) {
        	throw e;
        }		
	}
}
