package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestIntegrationTests {

    private final ItemRequestService itemRequestService;
    private final ItemService itemService;
    private final UserService userService;
    private final EntityManager entityManager;

    @Test
    void createItemRequest_test() {

        UserResponse requester = userService.createUser(getTestRequesterData());

        ItemRequestCreateDto itemRequestData = new ItemRequestCreateDto();
        itemRequestData.setDescription("request description");

        LocalDateTime testStartTime = LocalDateTime.now().minusSeconds(1);

        ItemRequestResponseDto itemRequest = itemRequestService.createItemRequest(requester.getId(), itemRequestData);

        assertThat(itemRequest, notNullValue());
        assertThat(itemRequest.getId(), notNullValue());
        assertThat(itemRequest.getDescription(), equalTo(itemRequestData.getDescription()));

        assertThat(itemRequest.getCreated(), notNullValue());
        assertThat(itemRequest.getCreated().isAfter(testStartTime), is(true));
        assertThat(itemRequest.getCreated().isBefore(LocalDateTime.now().plusSeconds(1)), is(true));
    }

    @Test
    void getItemRequestsOfUser_test() throws InterruptedException {

        UserResponse requester = userService.createUser(getTestRequesterData());
        UserResponse itemOwner = userService.createUser(getTestOwnerData());

        ItemRequestCreateDto firstRequestDto = new ItemRequestCreateDto();
        firstRequestDto.setDescription("request 1");
        ItemRequestResponseDto firstRequest = itemRequestService.createItemRequest(requester.getId(), firstRequestDto);

        Thread.sleep(50);

        ItemRequestCreateDto secondRequestDto = new ItemRequestCreateDto();
        secondRequestDto.setDescription("request 2");
        ItemRequestResponseDto secondRequest = itemRequestService.createItemRequest(requester.getId(), secondRequestDto);

        ItemCreateRequest itemDto = getTestItemData();
        itemDto.setRequestId(firstRequest.getId());
        ItemResponse item = itemService.createItem(itemDto, itemOwner.getId());

        entityManager.flush(); // Принудительно выталкиваем все изменения в БД H2
        entityManager.clear(); // Полностью очищаем кэш памяти Hibernate

        Collection<ItemRequestResponseDto> itemRequests = itemRequestService.getItemRequestsOfUser(requester.getId());

        assertThat(itemRequests, notNullValue());
        assertThat(itemRequests, hasSize(2));

        Iterator<ItemRequestResponseDto> iterator = itemRequests.iterator();

        ItemRequestResponseDto firstInResult = iterator.next();
        assertThat(firstInResult.getId(), equalTo(secondRequest.getId()));
        assertThat(firstInResult.getItems(), is(empty()));

        ItemRequestResponseDto secondInResult = iterator.next();
        assertThat(secondInResult.getId(), equalTo(firstRequest.getId()));

        assertThat(secondInResult.getItems(), hasSize(1));
        assertThat(secondInResult.getItems().iterator().next().getId(), equalTo(item.getId()));
    }

    @Test
    void getAllItemRequests_test() throws InterruptedException {

        UserResponse user1 = userService.createUser(getTestUserData("user1", "user1@test.com"));
        UserResponse user2 = userService.createUser(getTestUserData("user2", "user2@test.com"));

        ItemRequestCreateDto firstDto = new ItemRequestCreateDto();
        firstDto.setDescription("request 1");
        ItemRequestResponseDto requestFromUser1 = itemRequestService.createItemRequest(user1.getId(), firstDto);

        Thread.sleep(50);

        ItemRequestCreateDto secondDto = new ItemRequestCreateDto();
        secondDto.setDescription("request 2");
        ItemRequestResponseDto requestFromUser2 = itemRequestService.createItemRequest(user2.getId(), secondDto);

        ItemCreateRequest itemDto = getTestItemData();
        itemDto.setRequestId(requestFromUser1.getId());
        ItemResponse item = itemService.createItem(itemDto, user2.getId());

        entityManager.flush();
        entityManager.clear();

        Collection<ItemRequestResponseDto> itemRequests = itemRequestService.getAllItemRequests(user1.getId());

        assertThat(itemRequests, notNullValue());
        assertThat(itemRequests, hasSize(1));

        Iterator<ItemRequestResponseDto> iterator = itemRequests.iterator();

        ItemRequestResponseDto firstInResult = iterator.next();
        assertThat(firstInResult.getId(), equalTo(requestFromUser2.getId()));
        assertThat(firstInResult.getItems(), is(empty()));
    }

    @Test
    void getItemRequestById_test() {

        UserResponse requester = userService.createUser(getTestRequesterData());
        UserResponse itemOwner = userService.createUser(getTestOwnerData());

        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("item request");
        ItemRequestResponseDto createdRequest = itemRequestService.createItemRequest(requester.getId(), createDto);

        ItemCreateRequest itemDto = getTestItemData();
        itemDto.setRequestId(createdRequest.getId());
        ItemResponse item = itemService.createItem(itemDto, itemOwner.getId());

        entityManager.flush();
        entityManager.clear();

        ItemRequestResponseDto response = itemRequestService.getItemRequestById(requester.getId(), createdRequest.getId());

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(createdRequest.getId()));
        assertThat(response.getCreated(), notNullValue());

        assertThat(response.getItems(), hasSize(1));
        assertThat(response.getItems().iterator().next().getId(), equalTo(item.getId()));
    }

    private ItemCreateRequest getTestItemData() {
        ItemCreateRequest itemData = new ItemCreateRequest();
        itemData.setName("test item");
        itemData.setDescription("test item description");
        itemData.setAvailable(true);
        return itemData;
    }

    private CreateUserRequest getTestRequesterData() {
        CreateUserRequest userData = new CreateUserRequest();
        userData.setName("requester");
        userData.setEmail("requester@test.com");
        return userData;
    }

    private CreateUserRequest getTestUserData(String name, String email) {
        CreateUserRequest userData = new CreateUserRequest();
        userData.setName(name);
        userData.setEmail(email);
        return userData;
    }

    private CreateUserRequest getTestOwnerData() {
        CreateUserRequest userData = new CreateUserRequest();
        userData.setName("owner");
        userData.setEmail("owner@test.com");
        return userData;
    }

}
