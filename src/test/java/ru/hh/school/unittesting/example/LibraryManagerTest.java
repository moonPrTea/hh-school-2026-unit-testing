package ru.hh.school.unittesting.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hh.school.unittesting.homework.LibraryManager;
import ru.hh.school.unittesting.homework.NotificationService;
import ru.hh.school.unittesting.homework.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {

    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private LibraryManager libraryManager;

    @BeforeEach
    void setUp() {
        libraryManager.addBook("Book", 20);
    }

    @Test
    void shouldAddBook() {
        libraryManager.addBook("firstBook", 13);
        assertEquals(13, libraryManager.getAvailableCopies("firstBook"));
    }

    @Test
    void shouldAddQuantityToExistingBook() {
        libraryManager.addBook("Book", 12);
        assertEquals(32, libraryManager.getAvailableCopies("Book"));
    }

    @Test
    void shouldReturnFalseIfUserIsNotActive() {
        boolean borrowBookResult = libraryManager.borrowBook("Book", "user1");

        assertFalse(borrowBookResult);
        verify(notificationService)
                .notifyUser("user1", "Your account is not active.");
    }

    @Test
    void shouldReturnFalseIfBookDoesNotExist() {
        when(userService.isUserActive("user1"))
                .thenReturn(true);

        boolean borrowBookResult = libraryManager.borrowBook("book4", "user1");
        assertFalse(borrowBookResult);
    }

    @Test
    void shouldChangeQuantity() {
        when(userService.isUserActive("user1"))
                .thenReturn(true);

        libraryManager.borrowBook("Book", "user1");
        assertEquals(19, libraryManager.getAvailableCopies("Book"));
    }

    @Test
    void shouldAddRecordWithBorrowedBook() {
        when(userService.isUserActive("user1"))
                .thenReturn(true);

        libraryManager.borrowBook("Book", "user1");
        assertTrue(libraryManager.returnBook("Book", "user1"));
    }

    @Test
    void shouldBorrowBookAndSendNotification() {
        when(userService.isUserActive("user1"))
                .thenReturn(true);

        boolean succeedBorrowBook = libraryManager.borrowBook("Book", "user1");
        assertTrue(succeedBorrowBook);
        verify(notificationService)
                .notifyUser("user1", "You have borrowed the book: Book");
    }

    @Test
    void shouldReturnFalseWhenBookIsNotBorrowed() {
        assertFalse(libraryManager.returnBook("Book", "user1"));
    }

    @Test
    void shouldReturnFalseWhenBookIsBorrowedByAnotherUser() {
        when(userService.isUserActive("user2"))
                .thenReturn(true);

        libraryManager.borrowBook("Book", "user2");
        assertFalse(libraryManager.returnBook("Book", "user1"));
    }

    @Test
    void shouldReturnTrueIfBookIsBorrowedByUser() {
        when(userService.isUserActive("user1"))
                .thenReturn(true);

        libraryManager.borrowBook("Book", "user1");
        assertTrue(libraryManager.returnBook("Book", "user1"));
    }

    @Test
    void shouldReturnZeroIfBookDoesNotExists() {
        assertEquals(0, libraryManager.getAvailableCopies("bookfkf"));
    }

    @Test
    void testAvailableCopies() {
        when(userService.isUserActive("user1"))
                .thenReturn(true);

        libraryManager.borrowBook("Book", "user1");
        assertEquals(19, libraryManager.getAvailableCopies("Book"));
    }

    @Test
    void calculateDynamicLateFeeShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> libraryManager.calculateDynamicLateFee(-1, false, false));
    }

    @Test
    void shouldAddFeeWhenBookIsBestseller() {
        assertEquals(10.5, libraryManager.calculateDynamicLateFee(14, true, false));
    }

    @Test
    void shouldAddDiscountFeeWhenUserIsPremiumMember() {
        assertEquals(5.6, libraryManager.calculateDynamicLateFee(14, false, true));
    }

    @Test
    void shouldCalculateLateFeeWhenNoBestsellerAndNoPremium() {
        assertEquals(7, libraryManager.calculateDynamicLateFee(14, false, false));
    }
}
