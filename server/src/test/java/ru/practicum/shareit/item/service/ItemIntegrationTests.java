package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBookingComments;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ItemIntegrationTests {

    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentService commentService;

    @Test
    @Transactional
    void add_whenInvokedAndItemHasRequest_thenItemWithRequestSavedToDB() {
        User itemOwner = saveRandomUser();
        ItemRequest request = savedRandomRequest();
        ItemDto itemCreateDto = ItemDto.builder()
                .name("name")
                .description("desc")
                .requestId(request.getId())
                .available(true)
                .build();

        Long savedItemId = itemService.addItem(itemOwner.getId(), itemCreateDto).getId();

        Optional<Item> optionalSavedItem = itemRepository.findById(savedItemId);
        assertTrue(optionalSavedItem.isPresent());

        optionalSavedItem.ifPresent(savedItem -> {
            assertThat(savedItem.getName(), equalTo(itemCreateDto.getName()));
            assertThat(savedItem.getDescription(), equalTo(itemCreateDto.getDescription()));
            assertThat(savedItem.getAvailable(), equalTo(itemCreateDto.getAvailable()));
        });
    }

    @Test
    @Transactional
    void add_whenInvokedAndItemHasNoRequest_thenItemWithoutRequestSavedToDB() {
        User itemOwner = saveRandomUser();
        ItemDto itemCreateDto = ItemDto.builder()
                .name("name")
                .description("desc")
                .available(true)
                .build();

        Long savedItemId = itemService.addItem(itemOwner.getId(), itemCreateDto).getId();

        Optional<Item> optionalSavedItem = itemRepository.findById(savedItemId);
        assertTrue(optionalSavedItem.isPresent());

        optionalSavedItem.ifPresent(savedItem -> {
            assertThat(savedItem.getName(), equalTo(itemCreateDto.getName()));
            assertThat(savedItem.getDescription(), equalTo(itemCreateDto.getDescription()));
            assertThat(savedItem.getAvailable(), equalTo(itemCreateDto.getAvailable()));
            assertNull(savedItem.getRequest());
        });
    }

    @Test
    void add_whenInvokedAndItemHasNoRequest_thenDtoWithoutRequestIdReturned() {
        User itemOwner = saveRandomUser();
        ItemDto itemCreateDto = ItemDto.builder()
                .name("name")
                .description("desc")
                .available(true)
                .build();

        ItemDto addedItemDto = itemService.addItem(itemOwner.getId(), itemCreateDto);

        assertThat(addedItemDto.getRequestId(), nullValue());
    }

    @Test
    void update_whenInvokedAndUpdateHasRequest_thenUpdatedItemDtoWithRequestIdReturned() {
        User itemOwner = saveRandomUser();
        ItemRequest itemRequest = savedRandomRequest();
        ItemRequest newItemRequest = savedRandomRequest();
        Item savedItem = itemRepository.save(Item.builder()
                .name("oldName")
                .description("oldDescription")
                .available(true)
                .owner(itemOwner)
                .request(itemRequest.getId())
                .build());
        ItemDto updateItemDto = ItemDto.builder()
                .name("newName")
                .description("newDescription")
                .available(false)
                .requestId(newItemRequest.getId())
                .build();

        ItemDto updatedItemDto = itemService.editItem(itemOwner.getId(), savedItem.getId(), updateItemDto);

        assertThat(updatedItemDto.getName(), equalTo(updateItemDto.getName()));
        assertThat(updatedItemDto.getDescription(), equalTo(updateItemDto.getDescription()));
        assertThat(updatedItemDto.getAvailable(), equalTo(updateItemDto.getAvailable()));
    }

    @Test
    void update_whenInvokedAndUpdateHasNoRequest_thenUpdatedItemDtoWithoutRequestIdReturned() {
        User itemOwner = saveRandomUser();
        Item savedItem = itemRepository.save(Item.builder()
                .name("oldName")
                .description("oldDescription")
                .available(true)
                .owner(itemOwner)
                .build());
        ItemDto updateItemDto = ItemDto.builder()
                .name("newName")
                .description("newDescription")
                .available(false)
                .build();

        ItemDto updatedItemDto = itemService.editItem(itemOwner.getId(), savedItem.getId(), updateItemDto);

        assertThat(updatedItemDto.getName(), equalTo(updateItemDto.getName()));
        assertThat(updatedItemDto.getDescription(), equalTo(updateItemDto.getDescription()));
        assertThat(updatedItemDto.getAvailable(), equalTo(updateItemDto.getAvailable()));
        assertThat(updatedItemDto.getRequestId(), nullValue());
    }

    @Test
    @Transactional
    void update_whenInvokedAndUpdateHasRequest_thenItemUpdatedInDBWithNewRequest() {
        User itemOwner = saveRandomUser();
        ItemRequest itemRequest = savedRandomRequest();
        ItemRequest newItemRequest = savedRandomRequest();
        Item savedItem = itemRepository.save(Item.builder()
                .name("oldName")
                .description("oldDescription")
                .available(true)
                .owner(itemOwner)
                .request(itemRequest.getId())
                .build());
        ItemDto updateItemDto = ItemDto.builder()
                .name("newName")
                .description("newDescription")
                .available(false)
                .requestId(newItemRequest.getId())
                .build();

        Long updatedItemId = itemService.editItem(itemOwner.getId(), savedItem.getId(), updateItemDto).getId();

        Optional<Item> optionalUpdatedItem = itemRepository.findById(updatedItemId);
        assertTrue(optionalUpdatedItem.isPresent());

        optionalUpdatedItem.ifPresent(updatedItem -> {
            assertThat(updatedItem.getName(), equalTo(updateItemDto.getName()));
            assertThat(updatedItem.getDescription(), equalTo(updateItemDto.getDescription()));
            assertThat(updatedItem.getAvailable(), equalTo(updateItemDto.getAvailable()));
            assertThat(updatedItem.getOwner(), equalTo(itemOwner));
        });
    }

    @Test
    @Transactional
    void update_whenSavedItemHasRequestAndUpdateHasNot_thenRequestRemovedFromItemInDb() {
        User itemOwner = saveRandomUser();
        ItemRequest itemRequest = savedRandomRequest();
        Item savedItem = itemRepository.save(Item.builder()
                .name("oldName")
                .description("oldDescription")
                .available(true)
                .owner(itemOwner)
                .request(itemRequest.getId())
                .build());
        ItemDto updateItemDto = ItemDto.builder()
                .name("newName")
                .description("newDescription")
                .available(false)
                .build();

        Long updatedItemId = itemService.editItem(itemOwner.getId(), savedItem.getId(), updateItemDto).getId();

        Optional<Item> optionalUpdatedItem = itemRepository.findById(updatedItemId);
        assertTrue(optionalUpdatedItem.isPresent(), "Item with ID " + updatedItemId + " should be present in the database");

        optionalUpdatedItem.ifPresent(updatedItem -> {
            assertThat(updatedItem.getName(), equalTo(updateItemDto.getName()));
            assertThat(updatedItem.getDescription(), equalTo(updateItemDto.getDescription()));
            assertThat(updatedItem.getAvailable(), equalTo(updateItemDto.getAvailable()));
            assertThat(updatedItem.getOwner(), equalTo(itemOwner));
        });
    }

    @Test
    @Transactional
    void update_whenSavedItemHasNoRequestAndAlsoUpdateHasNot_thenItemWithoutRequestSavedToDb() {
        User itemOwner = saveRandomUser();
        Item savedItem = itemRepository.save(Item.builder()
                .name("oldName")
                .description("oldDescription")
                .available(true)
                .owner(itemOwner)
                .build());
        ItemDto updateItemDto = ItemDto.builder()
                .name("newName")
                .description("newDescription")
                .available(false)
                .build();

        Long updatedItemId = itemService.editItem(itemOwner.getId(), savedItem.getId(), updateItemDto).getId();

        Optional<Item> optionalUpdatedItem = itemRepository.findById(updatedItemId);
        assertTrue(optionalUpdatedItem.isPresent(), "Item with ID " + updatedItemId + " should be present in the database");

        optionalUpdatedItem.ifPresent(updatedItem -> {
            assertThat(updatedItem.getName(), equalTo(updateItemDto.getName()));
            assertThat(updatedItem.getDescription(), equalTo(updateItemDto.getDescription()));
            assertThat(updatedItem.getAvailable(), equalTo(updateItemDto.getAvailable()));
            assertNull(updatedItem.getRequest());
            assertThat(updatedItem.getOwner(), equalTo(itemOwner));
        });
    }

    @Test
    @Transactional
    void getById_whenItemHasComments_thenDtoWithCommentsReturned() {
        User owner = saveRandomUser();
        Item requestedItem = itemRepository.save(Item.builder()
                .name("name")
                .description("desc")
                .owner(owner)
                .available(true)
                .build());
        Comment comment = commentRepository.save(Comment.builder()
                .item(requestedItem)
                .created(LocalDateTime.now())
                .author(saveRandomUser())
                .text("text")
                .build());

        ItemDtoBookingComments requestedItemDto = itemService.getItem(owner.getId(), requestedItem.getId());

        assertThat(requestedItemDto.getComments(), hasSize(1));
        assertThat(requestedItemDto.getComments().get(0).getId(), equalTo(comment.getId()));

    }

    @Test
    void searchItems_whenInvoked_thenListItemsReturnedContainedTextInNameOrDescription() {
        User owner = saveRandomUser();
        Long requesterId = saveRandomUser().getId();
        String requestedText = "randomize";
        itemRepository.save(Item.builder()
                .name("SomeRandomizeName")
                .description("desc")
                .owner(owner)
                .available(true)
                .build());
        itemRepository.save(Item.builder()
                .name("name")
                .description("randoMize Description")
                .owner(owner)
                .available(true)
                .build());

        List<ItemDto> foundItems = itemService.searchItem(requesterId, requestedText, 0, 5);

        assertThat(foundItems, hasSize(2));
        assertThat(foundItems.get(0).getName(), containsStringIgnoringCase(requestedText));
        assertThat(foundItems.get(1).getDescription(), containsStringIgnoringCase(requestedText));
    }

    @Test
    @Transactional
    void addComment_whenInvoked_thenCommentAddedToItem() {
        User owner = saveRandomUser();
        User author = saveRandomUser();
        Item item = itemRepository.save(Item.builder()
                .name("name")
                .description("desc")
                .owner(owner)
                .available(true)
                .build());
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(author)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1))
                .build());
        CommentDto commentDto = CommentDto.builder()
                .authorName("Author")
                .text("Some valid text")
                .build();

        CommentDto addedComment = commentService.addComment(author.getId(), item.getId(), commentDto);

        ItemDtoBookingComments itemDto = itemService.getItem(owner.getId(), item.getId());
        assertThat(itemDto.getComments(), hasSize(1));
        assertThat(itemDto.getComments().get(0).getText(), equalTo(commentDto.getText()));
    }

    private User saveRandomUser() {
        return userRepository.save(User.builder()
                .name("name")
                .email(String.format("%s%s@email.ru", "email", new Random(9999L)))
                .build());
    }

    private ItemRequest savedRandomRequest() {
        return requestRepository.save(ItemRequest.builder()
                .requester(saveRandomUser())
                .description("desc")
                .created(LocalDateTime.now())
                .build());
    }
}