package pl.kielce.tu.backend.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import pl.kielce.tu.backend.model.dto.DvdReminderRequestDto;
import pl.kielce.tu.backend.service.reminder.ReminderService;

@ExtendWith(MockitoExtension.class)
class ReminderControllerTest {

    @Mock
    private ReminderService reminderService;

    @InjectMocks
    private ReminderController reminderController;

    @Test
    void createReminder_delegatesToService_andReturnsResponse() {
        DvdReminderRequestDto reminderRequest = mock(DvdReminderRequestDto.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        ResponseEntity<Void> expected = ResponseEntity.status(HttpStatus.CREATED).build();

        when(reminderService.handleCreateReminder(request, reminderRequest)).thenReturn(expected);

        ResponseEntity<Void> actual = reminderController.createReminder(reminderRequest, request);

        assertSame(expected, actual);
        verify(reminderService, times(1)).handleCreateReminder(request, reminderRequest);
        verifyNoMoreInteractions(reminderService);
    }

    @Test
    void createReminder_whenServiceReturnsForbidden_returnsCorrectResponse() {
        DvdReminderRequestDto reminderRequest = mock(DvdReminderRequestDto.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        ResponseEntity<Void> expected = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        when(reminderService.handleCreateReminder(request, reminderRequest)).thenReturn(expected);

        ResponseEntity<Void> actual = reminderController.createReminder(reminderRequest, request);

        assertSame(expected, actual);
        verify(reminderService, times(1)).handleCreateReminder(request, reminderRequest);
    }

    @Test
    void createReminder_whenServiceReturnsNotFound_returnsCorrectResponse() {
        DvdReminderRequestDto reminderRequest = mock(DvdReminderRequestDto.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        ResponseEntity<Void> expected = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(reminderService.handleCreateReminder(request, reminderRequest)).thenReturn(expected);

        ResponseEntity<Void> actual = reminderController.createReminder(reminderRequest, request);

        assertSame(expected, actual);
        verify(reminderService, times(1)).handleCreateReminder(request, reminderRequest);
    }

    @Test
    void createReminder_whenServiceReturnsServerError_returnsCorrectResponse() {
        DvdReminderRequestDto reminderRequest = mock(DvdReminderRequestDto.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        ResponseEntity<Void> expected = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        when(reminderService.handleCreateReminder(request, reminderRequest)).thenReturn(expected);

        ResponseEntity<Void> actual = reminderController.createReminder(reminderRequest, request);

        assertSame(expected, actual);
        verify(reminderService, times(1)).handleCreateReminder(request, reminderRequest);
    }

}
