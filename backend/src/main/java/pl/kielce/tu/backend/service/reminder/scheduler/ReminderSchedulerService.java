package pl.kielce.tu.backend.service.reminder.scheduler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.DvdReminder;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.DvdReminderRepository;
import pl.kielce.tu.backend.service.email.EmailStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Service
@RequiredArgsConstructor
public class ReminderSchedulerService {

    @Value("${server.base-url}")
    private String baseUrl;

    @Value("${email.reminder.subject}")
    private String reminderSubject;

    @Value("${email.reminder.template}")
    private String reminderTemplatePath;

    private final EmailStrategy emailStrategy;
    private final ResourceLoader resourceLoader;
    private final DvdReminderRepository dvdReminderRepository;
    private final UserContextLogger userContextLogger;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void processAvailableDvdReminders() {
        userContextLogger.logUserOperation("SCHEDULER_START", "Processing DVD availability reminders");
        List<DvdReminder> reminders = findRemindersForAvailableDvds();
        if (reminders.isEmpty()) {
            userContextLogger.logUserOperation("SCHEDULER_COMPLETE", "No reminders found for available DVDs");
            return;
        }
        userContextLogger.logUserOperation("SCHEDULER_PROCESSING",
                String.format("Found %d reminders to process", reminders.size()));
        processReminders(reminders);
    }

    private void processReminders(List<DvdReminder> reminders) {
        int successCount = 0;
        int failureCount = 0;
        for (DvdReminder reminder : reminders) {
            if (processReminder(reminder)) {
                successCount++;
            } else {
                failureCount++;
            }
        }
        logProcessingResults(successCount, failureCount);
    }

    private void logProcessingResults(int successCount, int failureCount) {
        userContextLogger.logUserOperation("SCHEDULER_COMPLETE",
                String.format("Processed: %d successful, %d failed", successCount, failureCount));
    }

    private boolean processReminder(DvdReminder reminder) {
        try {
            sendAvailabilityNotification(reminder);
            deleteReminder(reminder);
            logSuccessfulProcessing(reminder);
            return true;
        } catch (Exception e) {
            logFailedProcessing(reminder, e);
            return false;
        }
    }

    private void logSuccessfulProcessing(DvdReminder reminder) {
        userContextLogger.logUserOperation("REMINDER_PROCESSED",
                String.format("User ID: %d, DVD ID: %d", reminder.getUser().getId(), reminder.getDvd().getId()));
    }

    private void logFailedProcessing(DvdReminder reminder, Exception e) {
        userContextLogger.logUserOperation("REMINDER_PROCESSING_FAILED",
                String.format("Reminder ID: %d, User ID: %d, DVD ID: %d, Error: %s",
                        reminder.getId(), reminder.getUser().getId(), reminder.getDvd().getId(), e.getMessage()));
    }

    private List<DvdReminder> findRemindersForAvailableDvds() {
        return dvdReminderRepository.findRemindersForAvailableDvds();
    }

    private void sendAvailabilityNotification(DvdReminder reminder) {
        User user = reminder.getUser();
        Dvd dvd = reminder.getDvd();
        String emailBody = buildAvailabilityEmailBody(dvd);
        emailStrategy.sendEmail(user.getEmail(), reminderSubject, emailBody);
        userContextLogger.logUserOperation("EMAIL_SENT",
                String.format("Availability notification to %s for DVD '%s'", user.getEmail(), dvd.getTitle()));
    }

    private String buildAvailabilityEmailBody(Dvd dvd) {
        try {
            String template = loadTemplate(reminderTemplatePath);
            return populateTemplate(template, dvd);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email template", e);
        }
    }

    private String populateTemplate(String template, Dvd dvd) {
        String rentUrl = baseUrl + "/dashboard/rent/" + dvd.getId();
        return template
                .replace("{dvdTitle}", dvd.getTitle())
                .replace("{dvdDescription}", dvd.getDescription() != null ? dvd.getDescription() : "")
                .replace("{copiesAvailable}", String.valueOf(dvd.getCopiesAvalaible()))
                .replace("{rentalPricePerDay}", String.format("%.2f", dvd.getRentalPricePerDay()))
                .replace("{rentUrl}", rentUrl);
    }

    private String loadTemplate(String templatePath) throws IOException {
        Resource resource = resourceLoader.getResource(templatePath);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    private void deleteReminder(DvdReminder reminder) {
        dvdReminderRepository.delete(reminder);
        userContextLogger.logUserOperation("REMINDER_DELETED",
                String.format("Reminder ID: %d deleted after processing", reminder.getId()));
    }

}
