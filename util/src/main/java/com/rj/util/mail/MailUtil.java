package com.rj.util.mail;


public class MailUtil {
    private static final String emailServer = "smtp.qq.com";
    private static final String emailPort = "25";
    private static final String user = "3049409052@qq.com";
    private static final String pass = "qwer1234!";
    private static final String to = "3049409052@qq.com";
//	private static final String[] receivers = {"2936703656@qq.com"};


    public static void sendEmail(String subject, String content) {
//		Log.e("mail", "发送邮件:"+content);
        // 这个类主要是设置邮件
        MailSenderInfo mailInfo = new MailSenderInfo();
        mailInfo.setMailServerHost(emailServer);
        mailInfo.setMailServerPort(emailPort);
        mailInfo.setValidate(true);
        mailInfo.setUserName(user);
        mailInfo.setPassword(pass);// 您的邮箱密码
        mailInfo.setFromAddress(user);
        mailInfo.setToAddress(to);
//		mailInfo.setReceivers(receivers);
        mailInfo.setSubject(subject);
        mailInfo.setContent(content);
        // 这个类主要来发送邮件
        SimpleMailSender sms = new SimpleMailSender();
        sms.sendTextMail(mailInfo);// 发送文体格式
//		sms.sendHtmlMail(mailInfo);// 发送html格式
    }

    public static void sendHtmlMail(String subject, String content) {
//		Log.e("mail", "发送邮件:"+content);
        // 这个类主要是设置邮件
        MailSenderInfo mailInfo = new MailSenderInfo();
        mailInfo.setMailServerHost(emailServer);
        mailInfo.setMailServerPort(emailPort);
        mailInfo.setValidate(true);
        mailInfo.setUserName(user);
        mailInfo.setPassword(pass);// 您的邮箱密码
        mailInfo.setFromAddress(user);
        mailInfo.setToAddress(to);
//		mailInfo.setReceivers(receivers);
        mailInfo.setSubject(subject);
        mailInfo.setContent(content);
        // 这个类主要来发送邮件
        SimpleMailSender sms = new SimpleMailSender();
        SimpleMailSender.sendHtmlMail(mailInfo);// 发送html格式
    }
}
