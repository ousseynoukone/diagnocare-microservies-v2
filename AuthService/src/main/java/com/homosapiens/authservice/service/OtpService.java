package com.homosapiens.authservice.service;

import com.homosapiens.authservice.core.exception.AppException;
import com.homosapiens.authservice.model.Otp;
import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.repository.OtpRepository;
import com.homosapiens.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.mail.internet.MimeMessage;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${app.otp.expiration-minutes:10}")
    private long expirationMinutes;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.mail.from:${SMTP_USERNAME:no-reply@diagnocare.com}}")
    private String mailFrom;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void sendEmailVerificationOtp(User user, String lang) {
        cleanupExpiredOtps();
        invalidateActiveOtps(user);

        String code = generateOtp();
        Otp otp = new Otp();
        otp.setUser(user);
        otp.setCode(code);
        otp.setExpiresAt(new Date(System.currentTimeMillis() + expirationMinutes * 60_000));
        otpRepository.save(otp);

        String normalizedLang = normalizeLang(lang);
        sendHtmlEmail(
                user.getEmail(),
                buildSubject(normalizedLang),
                buildHtmlBody(normalizedLang, user.getFirstName(), code)
        );
    }

    @Transactional
    public void sendEmailVerificationOtp(String email, String lang) {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "User not found");
        }
        sendEmailVerificationOtp(user, lang);
    }

    @Transactional
    public void validateEmailOtp(String email, String code) {
        cleanupExpiredOtps();
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "User not found");
        }

        Otp otp = otpRepository.findTopByUserAndUsedAtIsNullOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "OTP not found"));

        Date now = new Date();
        if (otp.getExpiresAt().before(now)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "OTP expired");
        }
        if (!otp.getCode().equals(code)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "OTP invalid");
        }

        otp.setUsedAt(now);
        otpRepository.save(otp);

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }
    }

    private void invalidateActiveOtps(User user) {
        List<Otp> activeOtps = otpRepository.findByUserAndUsedAtIsNull(user);
        if (activeOtps.isEmpty()) {
            return;
        }
        Date now = new Date();
        for (Otp otp : activeOtps) {
            otp.setUsedAt(now);
        }
        otpRepository.saveAll(activeOtps);
    }

    private void cleanupExpiredOtps() {
        otpRepository.deleteByExpiresAtBefore(new Date());
    }

    private String normalizeLang(String lang) {
        if (lang == null) {
            return "fr";
        }
        String normalized = lang.trim().toLowerCase();
        if (normalized.startsWith("en")) {
            return "en";
        }
        return "fr";
    }

    private String buildSubject(String lang) {
        if ("en".equals(lang)) {
            return "Verify your email";
        }
        return "Vérification de votre email";
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to send OTP email", ex);
        }
    }

    private String buildHtmlBody(String lang, String firstName, String code) {
        String safeName = firstName == null || firstName.trim().isEmpty() ? "" : firstName.trim();
        String greeting = "en".equals(lang) ? "Hello" : "Bonjour";
        String intro = "en".equals(lang)
                ? "Use the verification code below to confirm your email address."
                : "Utilisez le code de vérification ci-dessous pour confirmer votre adresse email.";
        String expires = "en".equals(lang)
                ? "This code expires in " + expirationMinutes + " minutes."
                : "Ce code expire dans " + expirationMinutes + " minutes.";
        String support = "en".equals(lang)
                ? "If you did not request this, you can safely ignore this email."
                : "Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email.";

        return "<!DOCTYPE html>"
                + "<html><head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"margin:0;padding:0;background:#f5f7fb;font-family:Arial,Helvetica,sans-serif;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#f5f7fb;padding:24px;\">"
                + "<tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 6px 18px rgba(16,24,40,0.08);\">"
                + "<tr><td style=\"padding:24px 28px;background:#0f172a;color:#ffffff;font-size:20px;font-weight:bold;\">DiagnoCare</td></tr>"
                + "<tr><td style=\"padding:28px;\">"
                + "<p style=\"margin:0 0 12px 0;font-size:16px;color:#0f172a;\">" + greeting + (safeName.isEmpty() ? "" : " " + safeName) + ",</p>"
                + "<p style=\"margin:0 0 20px 0;font-size:14px;color:#334155;\">" + intro + "</p>"
                + "<div style=\"text-align:center;margin:24px 0;\">"
                + "<span style=\"display:inline-block;background:#eff6ff;color:#1d4ed8;font-size:28px;font-weight:bold;letter-spacing:6px;padding:12px 20px;border-radius:10px;\">"
                + code
                + "</span>"
                + "</div>"
                + "<p style=\"margin:0 0 12px 0;font-size:13px;color:#64748b;\">" + expires + "</p>"
                + "<p style=\"margin:0;font-size:13px;color:#94a3b8;\">" + support + "</p>"
                + "</td></tr>"
                + "<tr><td style=\"padding:16px 28px;background:#f1f5f9;color:#94a3b8;font-size:12px;\">DiagnoCare · Secure verification</td></tr>"
                + "</table>"
                + "</td></tr>"
                + "</table>"
                + "</body></html>";
    }

    private String generateOtp() {
        int max = (int) Math.pow(10, otpLength);
        int value = secureRandom.nextInt(max);
        return String.format("%0" + otpLength + "d", value);
    }
}
