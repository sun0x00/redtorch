package xyz.redtorch.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 邮件发送工具（通过mail.properties配置）
 * @author huangwl
 *
 */
public class MailSender {
	
	private static Logger log = LoggerFactory.getLogger(MailSender.class);
	
	private static String from;
	
	private static String to;
	
	private static String authCode;
	
	private static Properties props;
	
	static {
		props = new Properties();
        
        InputStream io = ClassLoader.getSystemResourceAsStream("mail.properties");
        try {
			props.load(io);
			from = props.getProperty("mail.username");
			to = props.getProperty("mail.to");
			authCode = props.getProperty("mail.authCode");
		} catch (IOException e) {
			log.error("需要在resources目录增加mail.properties文件");
			log.warn("以下为配置样例：");
			log.warn("mail.to=（目标邮箱名）@126.com\r\n" + 
					"mail.username= （邮箱名）@qq.com（）\r\n" + 
					"mail.authCode=（邮箱授权码）\r\n" + 
					"mail.transport.protocol=smtp\r\n" + 
					"mail.smtp.auth=true\r\n" + 
					"mail.smtp.host=smtp.qq.com");
		}
	}
	

	public static void sendMail(SimpleMessage msg) throws Exception {
        // 创建Session实例对象
        Session session = Session.getDefaultInstance(props);
        // 创建MimeMessage实例对象
        MimeMessage message = new MimeMessage(session);
        // 设置发件人
        message.setFrom(new InternetAddress(from));
        // 设置收件人
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        // 设置发送日期
        message.setSentDate(new Date());
        // 设置邮件主题
        message.setSubject(msg.getSubject());
        // 设置纯文本内容的邮件正文
        message.setText(msg.getContent());
        // 保存并生成最终的邮件内容
        message.saveChanges();
        // 设置为debug模式, 可以查看详细的发送 log
        session.setDebug(false);
        // 获取Transport对象
        Transport transport = session.getTransport("smtp");
        // 第2个参数需要填写的是QQ邮箱的SMTP的授权码，什么是授权码，它又是如何设置？
        transport.connect(from, authCode);
        // 发送，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
	}
}
