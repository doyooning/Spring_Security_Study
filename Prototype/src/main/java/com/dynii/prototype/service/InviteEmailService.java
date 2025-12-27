package com.dynii.prototype.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Log4j2
@Service
public class InviteEmailService {

    private final SendGrid sendGrid;

    public InviteEmailService(@Value("${spring.sendgrid.api-key}") String apiKey) {
        this.sendGrid = new SendGrid(apiKey);
    }

    public void sendInviteMail(String email, String inviteUrl) throws IOException {
        Email from = new Email("dyniiyeyo@naver.com");
        Email to = new Email(email);
        String subject = "[Deskterior] 판매자 동업자 초대 안내";

        Content content = new Content("text/html",
                "<h2>판매자 동업자 초대</h2>" +
                        "<p>아래 버튼을 눌러 회원가입을 완료해주세요.</p>" +
                        "<a href='" + inviteUrl + "'>회원가입 하러 가기</a>"
        );

        Mail mail = new Mail(from, subject, to, content);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        sendGrid.api(request);
    }
}
