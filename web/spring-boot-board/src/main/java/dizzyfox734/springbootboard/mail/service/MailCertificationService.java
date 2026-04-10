package dizzyfox734.springbootboard.mail.service;

import dizzyfox734.springbootboard.mail.domain.CertificationCodeGenerator;
import dizzyfox734.springbootboard.mail.exception.ExpiredMailCertificationCodeException;
import dizzyfox734.springbootboard.mail.exception.InvalidMailCertificationCodeException;
import dizzyfox734.springbootboard.mail.repository.MailCertificationRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class MailCertificationService {

    private static final String SIGNUP_MAIL_SUBJECT = "회원가입 인증코드입니다.";

    private final MailCertificationRepository mailCertificationRepository;
    private final CertificationCodeGenerator certificationCodeGenerator;
    private final MailContentBuilder mailContentBuilder;
    private final MailSenderService mailSenderService;

    public MailCertificationService(MailCertificationRepository mailCertificationRepository,
                                    CertificationCodeGenerator certificationCodeGenerator,
                                    MailContentBuilder mailContentBuilder,
                                    MailSenderService mailSenderService) {
        this.mailCertificationRepository = mailCertificationRepository;
        this.certificationCodeGenerator = certificationCodeGenerator;
        this.mailContentBuilder = mailContentBuilder;
        this.mailSenderService = mailSenderService;
    }

    public void sendSignupVerificationCode(String email) {
        String previousCode = mailCertificationRepository.get(email);
        Duration previousExpiration = previousCode == null
                ? null
                : mailCertificationRepository.getExpiration(email);
        String certificationCode = certificationCodeGenerator.generate();
        String content = mailContentBuilder.buildSignUpVerificationContent(certificationCode);

        mailCertificationRepository.save(email, certificationCode);
        try {
            mailSenderService.send(email, SIGNUP_MAIL_SUBJECT, content);
        } catch (RuntimeException e) {
            restorePreviousCertificationCode(email, previousCode, previousExpiration);
            throw e;
        }
    }

    private void restorePreviousCertificationCode(String email, String previousCode, Duration previousExpiration) {
        if (previousCode == null) {
            mailCertificationRepository.remove(email);
            return;
        }

        if (previousExpiration == null) {
            mailCertificationRepository.save(email, previousCode);
            return;
        }

        mailCertificationRepository.save(email, previousCode, previousExpiration);
    }

    public void verifyEmailCertificationCode(String email, String checkCode) {
        String savedCode = mailCertificationRepository.get(email);

        if (savedCode == null) {
            throw new ExpiredMailCertificationCodeException();
        }

        if (!savedCode.equals(checkCode)) {
            throw new InvalidMailCertificationCodeException();
        }

        mailCertificationRepository.remove(email);
    }
}
