package dizzyfox734.springbootboard.domain.mail;

import dizzyfox734.springbootboard.exception.ExpiredMailCertificationCodeException;
import dizzyfox734.springbootboard.exception.InvalidMailCertificationCodeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void sendSignupVerificationCode(String email) {
        String certificationCode = certificationCodeGenerator.generate();
        String content = mailContentBuilder.buildSignUpVerificationContent(certificationCode);

        mailSenderService.send(email, SIGNUP_MAIL_SUBJECT, content);
        mailCertificationRepository.save(email, certificationCode);
    }

    @Transactional
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