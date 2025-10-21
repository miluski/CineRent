package pl.kielce.tu.backend.service.reminder.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.DvdReminder;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.DvdReminderRepository;
import pl.kielce.tu.backend.service.email.EmailStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class ReminderSchedulerServiceTest {

    @Mock
    private EmailStrategy emailStrategy;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private DvdReminderRepository dvdReminderRepository;

    @Mock
    private UserContextLogger userContextLogger;

    @InjectMocks
    private ReminderSchedulerService reminderSchedulerService;

    private static final String BASE_URL = "https://localhost:10443";
    private static final String REMINDER_SUBJECT = "DVD Available: {dvdTitle}";
    private static final String TEMPLATE_PATH = "classpath:templates/dvd-availability-notification.html";
    private static final String TEMPLATE_CONTENT = "<h1>{dvdTitle}</h1><p>{dvdDescription}</p><p>Copies: {copiesAvailable}</p><p>Price: {rentalPricePerDay}</p><a href=\"{rentUrl}\">Rent now</a>";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reminderSchedulerService, "baseUrl", BASE_URL);
        ReflectionTestUtils.setField(reminderSchedulerService, "reminderSubject", REMINDER_SUBJECT);
        ReflectionTestUtils.setField(reminderSchedulerService, "reminderTemplatePath", TEMPLATE_PATH);
    }

    @Test
    void processAvailableDvdReminders_whenNoReminders_logsAndReturns() {
        when(dvdReminderRepository.findRemindersForAvailableDvds()).thenReturn(Collections.emptyList());

        reminderSchedulerService.processAvailableDvdReminders();

        verify(userContextLogger, times(1)).logUserOperation("SCHEDULER_START",
                "Processing DVD availability reminders");
        verify(userContextLogger, times(1)).logUserOperation("SCHEDULER_COMPLETE",
                "No reminders found for available DVDs");
        verify(emailStrategy, never()).sendEmail(anyString(), anyString(), anyString());
        verify(dvdReminderRepository, never()).delete(any(DvdReminder.class));
    }

    @Test
    void processAvailableDvdReminders_whenRemindersExist_sendsEmailsAndDeletesReminders() throws IOException {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .build();

        Dvd dvd = Dvd.builder()
                .id(42L)
                .title("The Matrix")
                .description("A great movie")
                .copiesAvalaible(5)
                .rentalPricePerDay(2.99f)
                .build();

        DvdReminder reminder = DvdReminder.builder()
                .id(1L)
                .user(user)
                .dvd(dvd)
                .build();

        List<DvdReminder> reminders = List.of(reminder);

        when(dvdReminderRepository.findRemindersForAvailableDvds()).thenReturn(reminders);

        Resource mockResource = Mockito.mock(Resource.class);
        when(resourceLoader.getResource(TEMPLATE_PATH)).thenReturn(mockResource);
        when(mockResource.getInputStream())
                .thenReturn(new ByteArrayInputStream(TEMPLATE_CONTENT.getBytes(StandardCharsets.UTF_8)));

        reminderSchedulerService.processAvailableDvdReminders();

        verify(userContextLogger, times(1)).logUserOperation("SCHEDULER_START",
                "Processing DVD availability reminders");
        verify(userContextLogger, times(1)).logUserOperation("SCHEDULER_PROCESSING", "Found 1 reminders to process");
        verify(emailStrategy, times(1)).sendEmail(eq("user@example.com"), eq(REMINDER_SUBJECT), anyString());
        verify(dvdReminderRepository, times(1)).delete(reminder);
        verify(userContextLogger, times(1)).logUserOperation("EMAIL_SENT",
                "Availability notification to user@example.com for DVD 'The Matrix'");
        verify(userContextLogger, times(1)).logUserOperation("REMINDER_DELETED",
                "Reminder ID: 1 deleted after processing");
        verify(userContextLogger, times(1)).logUserOperation("REMINDER_PROCESSED", "User ID: 1, DVD ID: 42");
        verify(userContextLogger, times(1)).logUserOperation("SCHEDULER_COMPLETE", "Processed: 1 successful, 0 failed");
    }

    @Test
    void processAvailableDvdReminders_whenEmailFails_logsFailureAndContinues() throws IOException {
        User user1 = User.builder().id(1L).email("user1@example.com").build();
        User user2 = User.builder().id(2L).email("user2@example.com").build();

        Dvd dvd1 = Dvd.builder().id(1L).title("DVD 1").copiesAvalaible(1).rentalPricePerDay(1.99f).build();
        Dvd dvd2 = Dvd.builder().id(2L).title("DVD 2").copiesAvalaible(2).rentalPricePerDay(2.99f).build();

        DvdReminder reminder1 = DvdReminder.builder().id(1L).user(user1).dvd(dvd1).build();
        DvdReminder reminder2 = DvdReminder.builder().id(2L).user(user2).dvd(dvd2).build();

        when(dvdReminderRepository.findRemindersForAvailableDvds()).thenReturn(List.of(reminder1, reminder2));

        Resource mockResource = Mockito.mock(Resource.class);
        when(resourceLoader.getResource(TEMPLATE_PATH)).thenReturn(mockResource);
        when(mockResource.getInputStream())
                .thenReturn(new ByteArrayInputStream(TEMPLATE_CONTENT.getBytes(StandardCharsets.UTF_8)));

        doThrow(new RuntimeException("Email sending failed")).when(emailStrategy).sendEmail(eq("user1@example.com"),
                anyString(), anyString());

        reminderSchedulerService.processAvailableDvdReminders();

        verify(emailStrategy, times(2)).sendEmail(anyString(), anyString(), anyString());
        verify(dvdReminderRepository, times(1)).delete(reminder2);
        verify(dvdReminderRepository, never()).delete(reminder1);
        verify(userContextLogger, times(1)).logUserOperation("REMINDER_PROCESSING_FAILED",
                "Reminder ID: 1, User ID: 1, DVD ID: 1, Error: Email sending failed");
        verify(userContextLogger, times(1)).logUserOperation("REMINDER_PROCESSED", "User ID: 2, DVD ID: 2");
        verify(userContextLogger, times(1)).logUserOperation("SCHEDULER_COMPLETE", "Processed: 1 successful, 1 failed");
    }

    @Test
    void processAvailableDvdReminders_whenMultipleReminders_processesAll() throws IOException {
        User user1 = User.builder().id(1L).email("user1@example.com").build();
        User user2 = User.builder().id(2L).email("user2@example.com").build();
        User user3 = User.builder().id(3L).email("user3@example.com").build();

        Dvd dvd1 = Dvd.builder().id(1L).title("DVD 1").copiesAvalaible(1).rentalPricePerDay(1.99f).build();
        Dvd dvd2 = Dvd.builder().id(2L).title("DVD 2").copiesAvalaible(2).rentalPricePerDay(2.99f).build();
        Dvd dvd3 = Dvd.builder().id(3L).title("DVD 3").copiesAvalaible(3).rentalPricePerDay(3.99f).build();

        DvdReminder reminder1 = DvdReminder.builder().id(1L).user(user1).dvd(dvd1).build();
        DvdReminder reminder2 = DvdReminder.builder().id(2L).user(user2).dvd(dvd2).build();
        DvdReminder reminder3 = DvdReminder.builder().id(3L).user(user3).dvd(dvd3).build();

        when(dvdReminderRepository.findRemindersForAvailableDvds())
                .thenReturn(List.of(reminder1, reminder2, reminder3));

        Resource mockResource = Mockito.mock(Resource.class);
        when(resourceLoader.getResource(TEMPLATE_PATH)).thenReturn(mockResource);
        when(mockResource.getInputStream())
                .thenReturn(new ByteArrayInputStream(TEMPLATE_CONTENT.getBytes(StandardCharsets.UTF_8)));

        reminderSchedulerService.processAvailableDvdReminders();

        verify(emailStrategy, times(3)).sendEmail(anyString(), anyString(), anyString());
        verify(dvdReminderRepository, times(3)).delete(any(DvdReminder.class));
        verify(userContextLogger, times(1)).logUserOperation("SCHEDULER_PROCESSING", "Found 3 reminders to process");
        verify(userContextLogger, times(1)).logUserOperation("SCHEDULER_COMPLETE", "Processed: 3 successful, 0 failed");
    }

    @Test
    void processAvailableDvdReminders_whenTemplateLoadFails_logsFailure() {
        User user = User.builder().id(1L).email("user@example.com").build();
        Dvd dvd = Dvd.builder().id(1L).title("DVD 1").copiesAvalaible(1).rentalPricePerDay(1.99f).build();
        DvdReminder reminder = DvdReminder.builder().id(1L).user(user).dvd(dvd).build();

        when(dvdReminderRepository.findRemindersForAvailableDvds()).thenReturn(List.of(reminder));
        when(resourceLoader.getResource(TEMPLATE_PATH)).thenThrow(new RuntimeException("Template not found"));

        reminderSchedulerService.processAvailableDvdReminders();

        verify(emailStrategy, never()).sendEmail(anyString(), anyString(), anyString());
        verify(dvdReminderRepository, never()).delete(any(DvdReminder.class));
        verify(userContextLogger, times(1)).logUserOperation("REMINDER_PROCESSING_FAILED",
                "Reminder ID: 1, User ID: 1, DVD ID: 1, Error: Template not found");
        verify(userContextLogger, times(1)).logUserOperation("SCHEDULER_COMPLETE", "Processed: 0 successful, 1 failed");
    }

}
