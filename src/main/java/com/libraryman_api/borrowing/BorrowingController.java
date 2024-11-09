package com.libraryman_api.borrowing;

import com.libraryman_api.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing borrowings in the LibraryMan application.
 * This controller provides endpoints for performing operations related to borrowing and returning books,
 * paying fines, and retrieving borrowing records.
 */
@RestController
@RequestMapping("/api/borrowings")
public class BorrowingController {

    private final BorrowingService borrowingService;

    /**
     * Constructs a new {@code BorrowingController} with the specified {@link BorrowingService}.
     *
     * @param borrowingService the service used to handle borrowing-related operations.
     */
    public BorrowingController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    /**
     * Retrieves a paginated and sorted list of all borrowing records in the library.
     *
     * @param pageable contains pagination information (page number, size, and sorting).
     * @param sortBy   (optional) the field by which to sort the results.
     * @param sortDir  (optional) the direction of sorting (asc or desc). Defaults to ascending.
     * @return a {@link Page} of {@link Borrowings} representing all borrowings.
     * The results are sorted by borrow date by default and limited to 5 members per page.
     */
    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public Page<BorrowingsDto> getAllBorrowings(@PageableDefault(page = 0, size = 5, sort = "borrowDate") Pageable pageable,
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

        return borrowingService.getAllBorrowings(pageable);
    }

    /**
     * Records a new book borrowing.
     *
     * @param borrowingsDto the {@link Borrowings} object containing borrowing details.
     * @return the saved {@link Borrowings} object representing the borrowing record.
     */
    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or (hasRole('USER') and #borrowingsDto.member.memberId == authentication.principal.memberId)")
    public BorrowingsDto borrowBook(@Valid @RequestBody BorrowingsDto borrowingsDto) {
        return borrowingService.borrowBook(borrowingsDto);
    }

    /**
     * Marks a borrowed book as returned.
     *
     * @param id the ID of the borrowing record to update.
     */
    @PutMapping("/{id}/return")
    public BorrowingsDto returnBook(@PathVariable int id) {
        return borrowingService.returnBook(id);
    }

    /**
     * Pays the fine for an overdue book.
     *
     * @param id the ID of the borrowing record for which the fine is being paid.
     * @return a message indicating the payment status.
     */
    @PutMapping("/{id}/pay")
    public String payFine(@PathVariable int id) {
        System.out.println("Pay Fine Id: " + id);
        return borrowingService.payFine(id);
    }

    /**
     * Retrieves a paginated and sorted list of all borrowing records for a specific member.
     *
     * @param memberId the ID of the member whose borrowing records are to be retrieved.
     * @param pageable contains pagination information (page number, size, and sorting).
     * @param sortBy   (optional) the field by which to sort the results.
     * @param sortDir  (optional) the direction of sorting (asc or desc). Defaults to ascending.
     * @return a {@link Page} of {@link Borrowings} representing all borrowings for a specific member.
     * The results are sorted by borrow date by default and limited to 5 members per page.
     */
    @GetMapping("member/{memberId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or (hasRole('USER') and #memberId == authentication.principal.memberId)")
    public Page<BorrowingsDto> getAllBorrowingsOfAMember(@PathVariable int memberId,
                                                         @PageableDefault(page = 0, size = 5, sort = "borrowDate") Pageable pageable,
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

        return borrowingService.getAllBorrowingsOfMember(memberId, pageable);
    }

    /**
     * Retrieves a borrowing record by its ID.
     *
     * @param borrowingId the ID of the borrowing record to retrieve.
     * @return the {@link Borrowings} object representing the borrowing record.
     * @throws ResourceNotFoundException if the borrowing record with the specified ID is not found.
     */
    @GetMapping("{borrowingId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BorrowingsDto getBorrowingById(@PathVariable int borrowingId) {
        return borrowingService.getBorrowingById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found"));
    }
}
