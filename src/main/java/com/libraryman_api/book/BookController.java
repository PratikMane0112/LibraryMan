package com.libraryman_api.book;

import com.libraryman_api.exception.ResourceNotFoundException;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing books in the LibraryMan application.
 * This controller provides endpoints for performing CRUD operations on books,
 * including retrieving all books, getting a book by its ID, adding a new book,
 * updating an existing book, and deleting a book.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * Retrieves a paginated and sorted list of all books in the library.
     *
     * @param pageable contains pagination information (page number, size, and sorting).
     * @param sortBy   (optional) the field by which to sort the results.
     * @param sortDir  (optional) the direction of sorting (asc or desc). Defaults to ascending.
     * @return a {@link Page} of {@link BookDto} objects representing the books in the library.
     * The results are sorted by title by default and limited to 5 books per page.
     */
    @GetMapping
    public Page<BookDto> getAllBooks(@PageableDefault(page = 0, size = 5, sort = "title") Pageable pageable,
                                     @RequestParam(required = false) String sortBy,
                                     @RequestParam(required = false) String sortDir) {

        // Adjust the pageable based on dynamic sorting parameters
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = Sort.Direction.ASC; // Default direction

            if (sortDir != null && sortDir.equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            }

            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(direction, sortBy));
        }
        return bookService.getAllBooks(pageable);
    }

    /**
     * Retrieves a book by its ID.
     *
     * @param id the ID of the book to retrieve.
     * @return a {@link ResponseEntity} containing the {@link Book} object, if found.
     * @throws ResourceNotFoundException if the book with the specified ID is not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable int id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
    }

    /**
     * Adds a new book to the library.
     *
     * @param bookDto the {@link Book} object representing the new book to add.
     * @return the added {@link Book} object.
     */
    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BookDto addBook(@RequestBody BookDto bookDto) {
        return bookService.addBook(bookDto);
    }

    /**
     * Updates an existing book in the library.
     *
     * @param id             the ID of the book to update.
     * @param bookDtoDetails the {@link Book} object containing the updated book details.
     * @return the updated {@link Book} object.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BookDto updateBook(@PathVariable int id, @RequestBody BookDto bookDtoDetails) {
        return bookService.updateBook(id, bookDtoDetails);
    }

    /**
     * Deletes a book from the library by its ID.
     *
     * @param id the ID of the book to delete.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public void deleteBook(@PathVariable int id) {
        bookService.deleteBook(id);
    }
    
    /**
     * Searches book based on title, author, genre, etc.
     * It uses a keyword parameter to filter the books, and pagination is applied to the search results.
     * If no book is found it will return 204(No content found) http response.
     * If keyword is null then it will return all books.
     * @param keyword the Keyword to search Book
     * @param pageable
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBook(@RequestParam String keyword, @PageableDefault(page = 0, size = 5, sort = "title") Pageable pageable){
    	Page<Book> books=bookService.searchBook(keyword,pageable);
    	if(!books.isEmpty())
    		return ResponseEntity.ok(books);
    	return ResponseEntity.noContent().build();
    }
}