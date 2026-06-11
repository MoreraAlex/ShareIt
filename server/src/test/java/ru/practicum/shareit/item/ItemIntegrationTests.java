package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreateRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemResponseFull;
import ru.practicum.shareit.item.dto.ItemUpdateRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemIntegrationTests {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    @Test
    void createItem_test() {

        UserResponse owner = userService.createUser(getTestUserData());
        ItemCreateRequest itemData = getTestItemData();
        ItemResponse item = itemService.createItem(itemData, owner.getId());

        assertThat(item, notNullValue());
        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemData.getName()));
        assertThat(item.getDescription(), equalTo(itemData.getDescription()));
        assertThat(item.getAvailable(), is(itemData.getAvailable()));

        var ownerItems = itemService.getItemsByOwner(owner.getId());
        assertThat(ownerItems, hasSize(1));

    }

    @Test
    void createItem_withValidRequestId_shouldSaveItemWithRequest() {
        UserResponse owner = userService.createUser(getTestUserData());

        ItemCreateRequest itemDataWithRequest = getTestItemData();
        itemDataWithRequest.setRequestId(999L);

        assertThrows(NotFoundException.class, () -> {
            itemService.createItem(itemDataWithRequest, owner.getId());
        });
    }

    @Test
    void updateItem_test() {
        UserResponse owner = userService.createUser(getTestUserData());
        ItemCreateRequest itemData = getTestItemData();
        ItemResponse item = itemService.createItem(itemData, owner.getId());

        String newName = itemData.getName() + " updated";
        ItemUpdateRequest itemDataUpdate = new ItemUpdateRequest();
        itemDataUpdate.setName(newName);
        ItemResponse updatedItem = itemService.updateItem(owner.getId(), item.getId(), itemDataUpdate);

        assertThat(updatedItem, notNullValue());
        assertThat(updatedItem.getId(), equalTo(item.getId()));
        assertThat(updatedItem.getName(), equalTo(newName));
        assertThat(updatedItem.getDescription(), equalTo(itemData.getDescription()));
    }

    @Test
    void updateItem_whenOtherFieldsPresented_shouldUpdateDescriptionAndAvailable() {
        UserResponse owner = userService.createUser(getTestUserData());
        ItemResponse item = itemService.createItem(getTestItemData(), owner.getId());

        ItemUpdateRequest itemDataUpdate = new ItemUpdateRequest();
        itemDataUpdate.setDescription("new description");
        itemDataUpdate.setAvailable(false);

        ItemResponse updatedItem = itemService.updateItem(owner.getId(), item.getId(), itemDataUpdate);

        assertThat(updatedItem, notNullValue());
        assertThat(updatedItem.getName(), equalTo(item.getName()));
        assertThat(updatedItem.getDescription(), equalTo("new description"));
        assertThat(updatedItem.getAvailable(), is(false));
    }

    @Test
    void updateItem_byNotOwner_shouldThrowException() {
        UserResponse owner = userService.createUser(getTestUserData());
        ItemResponse item = itemService.createItem(getTestItemData(), owner.getId());

        CreateUserRequest userData = new CreateUserRequest();
        userData.setName("user");
        userData.setEmail("user@test.com");
        UserResponse user = userService.createUser(userData);

        ItemUpdateRequest itemDataUpdate = new ItemUpdateRequest();
        itemDataUpdate.setName("new name");

        assertThrows(NotFoundException.class, () -> {
            itemService.updateItem(user.getId(), item.getId(), itemDataUpdate);
        });
    }

    @Test
    void getItemById_test() {

        UserResponse owner = userService.createUser(getTestUserData());
        ItemResponse item = itemService.createItem(getTestItemData(), owner.getId());

        ItemResponseFull itemReturned = itemService.getItemById(owner.getId(), item.getId());

        assertThat(itemReturned, notNullValue());
        assertThat(itemReturned.getId(), equalTo(item.getId()));
        assertThat(itemReturned.getName(), equalTo(item.getName()));
        assertThat(itemReturned.getDescription(), equalTo(item.getDescription()));
        assertThat(itemReturned.getAvailable(), equalTo(item.getAvailable()));
        assertThat(itemReturned.getLastBooking(), nullValue());
        assertThat(itemReturned.getNextBooking(), nullValue());

    }

    @Test
    void getItemById_withLastAndNextBookings_shouldCalculateBookingsCorrectly() {
        UserResponse owner = userService.createUser(getTestUserData());
        CreateUserRequest bookerData = new CreateUserRequest();
        bookerData.setName("booker");
        bookerData.setEmail("booker@test.com");
        UserResponse booker = userService.createUser(bookerData);

        ItemResponse item = itemService.createItem(getTestItemData(), owner.getId());

        BookingCreateRequest pastBookingDto = new BookingCreateRequest();
        pastBookingDto.setItemId(item.getId());
        pastBookingDto.setStart(LocalDateTime.now().minusDays(5));
        pastBookingDto.setEnd(LocalDateTime.now().minusDays(3));
        BookingResponse pastBooking = bookingService.createBooking(booker.getId(), pastBookingDto);
        bookingService.approveRejectBooking(owner.getId(), pastBooking.getId(), true);

        BookingCreateRequest futureBookingDto = new BookingCreateRequest();
        futureBookingDto.setItemId(item.getId());
        futureBookingDto.setStart(LocalDateTime.now().plusDays(3));
        futureBookingDto.setEnd(LocalDateTime.now().plusDays(5));
        BookingResponse futureBooking = bookingService.createBooking(booker.getId(), futureBookingDto);
        bookingService.approveRejectBooking(owner.getId(), futureBooking.getId(), true);

        ItemResponseFull itemReturned = itemService.getItemById(owner.getId(), item.getId());

        assertThat(itemReturned.getLastBooking(), notNullValue());
        assertThat(itemReturned.getNextBooking(), notNullValue());
        assertThat(itemReturned.getLastBooking(), equalTo(pastBooking.getStart()));
        assertThat(itemReturned.getNextBooking(), equalTo(futureBooking.getStart()));

        Collection<ItemResponseFull> ownerItems = itemService.getItemsByOwner(owner.getId());
        assertThat(ownerItems, hasSize(1));
        ItemResponseFull itemFromList = ownerItems.iterator().next();
        assertThat(itemFromList.getLastBooking(), notNullValue());
        assertThat(itemFromList.getNextBooking(), notNullValue());
    }

    @Test
    void getItemsByOwner_test() {

        UserResponse owner = userService.createUser(getTestUserData());
        ItemCreateRequest itemData = getTestItemData();
        itemService.createItem(itemData, owner.getId());
        Collection<ItemResponseFull> items = itemService.getItemsByOwner(owner.getId());

        assertThat(items, hasSize(1));
        ItemResponseFull item = items.iterator().next();
        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemData.getName()));
        assertThat(item.getDescription(), equalTo(itemData.getDescription()));
        assertThat(item.getAvailable(), is(itemData.getAvailable()));

    }

    @Test
    void searchForItems_test() {

        UserResponse owner = userService.createUser(getTestUserData());

        ItemCreateRequest itemDataSearchByName = getTestItemData();
        itemDataSearchByName.setName(itemDataSearchByName.getName() + " search");
        itemService.createItem(itemDataSearchByName, owner.getId());

        ItemCreateRequest itemDataSearchByDescription = getTestItemData();
        itemDataSearchByDescription.setDescription(itemDataSearchByDescription.getDescription() + " search by description");
        ItemResponse itemSearchByDescription = itemService.createItem(itemDataSearchByDescription, owner.getId());

        ItemCreateRequest itemDataNoSearch = getTestItemData();
        itemService.createItem(itemDataNoSearch, owner.getId());

        Collection<ItemResponse> foundItems = itemService.searchForItems("SEARCH");
        assertThat(foundItems, hasSize(2));

        Collection<ItemResponse> foundByDescription = itemService.searchForItems("search by description");
        assertThat(foundByDescription, hasSize(1));
        assertThat(foundByDescription.iterator().next().getId(), equalTo(itemSearchByDescription.getId()));
    }

    @Test
    void searchForItems_whenTextIsEmptyOrBlank_shouldReturnEmptyList() {
        UserResponse owner = userService.createUser(getTestUserData());
        itemService.createItem(getTestItemData(), owner.getId());

        Collection<ItemResponse> foundItemsEmpty = itemService.searchForItems("");
        assertThat(foundItemsEmpty, hasSize(0));

        Collection<ItemResponse> foundItemsBlank = itemService.searchForItems("   ");
        assertThat(foundItemsBlank, hasSize(0));
    }

    @Test
    void createComment_test() {

        CreateUserRequest ownerData = new CreateUserRequest();
        ownerData.setName("owner");
        ownerData.setEmail("owner@test.com");
        UserResponse owner = userService.createUser(ownerData);

        CreateUserRequest bookerData = new CreateUserRequest();
        bookerData.setName("booker");
        bookerData.setEmail("booker@test.com");
        UserResponse booker = userService.createUser(bookerData);

        ItemResponse item = itemService.createItem(getTestItemData(), owner.getId());

        BookingCreateRequest bookingDto = new BookingCreateRequest();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusDays(2));
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));

        BookingResponse booking = bookingService.createBooking(booker.getId(), bookingDto);
        bookingService.approveRejectBooking(owner.getId(), booking.getId(), true);

        CommentCreateRequest commentData = new CommentCreateRequest();
        commentData.setText("comment text");

        CommentResponse response = itemService.createComment(booker.getId(), item.getId(), commentData);

        assertThat(response, notNullValue());
        assertThat(response.getId(), notNullValue());
        assertThat(response.getText(), equalTo(commentData.getText()));
        assertThat(response.getAuthorName(), equalTo(booker.getName()));
        assertThat(response.getCreated(), notNullValue());
    }

    @Test
    void createComment_whenUserHasNoBooking_shouldThrowException() {
        UserResponse owner = userService.createUser(getTestUserData());
        ItemResponse item = itemService.createItem(getTestItemData(), owner.getId());

        CreateUserRequest bookerData = new CreateUserRequest();
        bookerData.setName("booker");
        bookerData.setEmail("booker@test.com");
        UserResponse booker = userService.createUser(bookerData);

        CommentCreateRequest commentData = new CommentCreateRequest();
        commentData.setText("comment text");

        assertThrows(ru.practicum.shareit.exception.ValidationException.class, () -> {
            itemService.createComment(booker.getId(), item.getId(), commentData);
        });
    }

    @Test
    void itemService_uncoveredExceptions_shouldCoverThrowBlocks() {
        UserResponse owner = userService.createUser(getTestUserData());
        ItemResponse item = itemService.createItem(getTestItemData(), owner.getId());

        assertThrows(NotFoundException.class, () -> {
            itemService.createItem(getTestItemData(), 999L);
        });

        CommentCreateRequest commentData = new CommentCreateRequest();
        commentData.setText("text");
        assertThrows(NotFoundException.class, () -> {
            itemService.createComment(999L, item.getId(), commentData);
        });

        assertThrows(NotFoundException.class, () -> {
            itemService.createComment(owner.getId(), 999L, commentData);
        });
    }

    CreateUserRequest getTestUserData() {
        CreateUserRequest userData = new CreateUserRequest();
        userData.setName("test user");
        userData.setEmail("testuser@test.com");
        return userData;
    }

    ItemCreateRequest getTestItemData() {
        ItemCreateRequest itemData = new ItemCreateRequest();
        itemData.setName("test item");
        itemData.setDescription("test item description");
        itemData.setAvailable(true);
        return itemData;
    }
}
