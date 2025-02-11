package com.libraryman_api.borrowing;


import com.libraryman_api.exception.InvalidSortFieldException;
import com.libraryman_api.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowings, Integer> {

//    {
//        try {
//            Page<Borrowings> borrowings = borrowingRepository.findByMember_memberId(memberId, pageable);
//
//            if (borrowings.isEmpty()) {
//                throw new ResourceNotFoundException("Member didn't borrow any book");
//            }
//            return borrowings.map(this::EntityToDto);
//        } catch (PropertyReferenceException ex) {
//            throw new InvalidSortFieldException("The specified 'sortBy' value is invalid.");
//        }
//    }

    Page<Borrowings> findByMember_memberId(int memberId, Pageable pageable);

    @Query(value = "SELECT b.title AS bookTitle, COUNT(*) AS borrowCount " +
            "FROM borrowings br JOIN books b ON br.book_id = b.book_id " +
            "GROUP BY b.book_id ORDER BY borrowCount DESC LIMIT :limit", nativeQuery = true)
    List<Map<String, Object>> findMostBorrowedBooks(int limit);

    @Query("SELECT FUNCTION('DATE', b.borrowDate) as date, COUNT(*) as count " +
            "FROM Borrowings b " +
            "WHERE b.borrowDate BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE', b.borrowDate)")
    Map<String, Long> getBorrowingTrendsBetweenDates(LocalDate startDate, LocalDate endDate);
}
