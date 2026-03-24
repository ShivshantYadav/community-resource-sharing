package com.community.service;

import com.community.dto.BookingRequest;
import com.community.dto.BookingResponse;
import com.community.dto.ResourceResponse;
import com.community.entity.*;
import com.community.repository.BookingRepository;
import com.community.repository.ResourceRepository;
import com.community.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BookingService(
            BookingRepository bookingRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            EmailService emailService
    ) {
        this.bookingRepository = bookingRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // ================= CREATE BOOKING =================
    public BookingResponse createBooking(Long resourceId, Long userId, BookingRequest dto) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        User borrower = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = new Booking();
        booking.setResource(resource);
        booking.setBorrower(borrower);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.UNPAID);
        booking.setPaid(false);

        if (dto.getBorrowDates() != null) {
            booking.setBorrowDates(
                    dto.getBorrowDates().stream()
                            .map(LocalDate::parse)
                            .collect(Collectors.toList())
            );
        }

        booking.setQuantity(dto.getQuantity() > 0 ? dto.getQuantity() : 1);
        booking.setRentPerDay(resource.getRentPrice());
        booking.setDeposit(resource.getDeposit());
        booking.calculateTotalPayable();

        bookingRepository.save(booking);

        return mapToDTO(
                bookingRepository.findByIdWithDetails(booking.getBookingId()).get()
        );
    }
    
    
    

    public Booking getBookingEntity(Long bookingId) {
        return bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public Booking getBookingEntityByRazorpayOrderId(String razorpayOrderId) {
        return bookingRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException(
                        "Booking not found for Razorpay order ID: " + razorpayOrderId));
    }
    
 // ================= OWNER: GET BORROWS BY MULTIPLE STATUSES =================
    @Transactional(readOnly = true)
    public List<BookingResponse> getOwnerBookingsByStatuses(Long ownerId, List<BookingStatus> statuses) {
        return bookingRepository.findByResourceOwnerAndBookingStatusIn(ownerId, statuses)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    // Convenience method for frontend: show all active + returned bookings
    @Transactional(readOnly = true)
    public List<BookingResponse> getOwnerBorrows(Long ownerId) {
        List<BookingStatus> statuses = List.of(
                BookingStatus.BORROWED,
                BookingStatus.RETURN_REQUESTED,
                BookingStatus.RETURNED
        );
        return getOwnerBookingsByStatuses(ownerId, statuses);
    }


    // ================= GET BOOKING =================
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return mapToDTO(booking);
    }
    
    public List<Booking> getBorrowsForOwner(Long ownerId) {
        return bookingRepository.findAllByOwner(ownerId); // includes returned
    }


    // ================= OWNER: APPROVE / REJECT =================
    public void updateBookingStatusByOwner(Long bookingId, BookingStatus status, Long ownerId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getResource().getOwner().getUserId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized owner");
        }

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Booking already processed");
        }

        if (status != BookingStatus.APPROVED && status != BookingStatus.REJECTED) {
            throw new RuntimeException("Invalid status update");
        }

        booking.setBookingStatus(status);
        bookingRepository.save(booking);
    }

    // ================= USER UPDATE =================
    public void updateBookingUserDetails(Long bookingId, BookingRequest dto) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (dto.getBorrowDates() != null) {
            booking.setBorrowDates(
                    dto.getBorrowDates().stream()
                            .map(LocalDate::parse)
                            .collect(Collectors.toList())
            );
        }

        if (dto.getQuantity() > 0) {
            booking.setQuantity(dto.getQuantity());
        }

        booking.calculateTotalPayable();
        bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<String> getBorrowDates(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getBorrowDates() == null) return new ArrayList<>();

        return booking.getBorrowDates().stream()
                .map(d -> d.format(DATE_FORMAT))
                .collect(Collectors.toList());
    }

    // ================= CANCEL =================
    public void cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getBorrower().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized cancellation");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    // ================= PAYMENT =================
    public void payBooking(Long bookingId, String userEmail, PaymentMethod paymentMethod) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getBorrower().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized payment");
        }

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Already paid");
        }

        if (booking.getBookingStatus() != BookingStatus.APPROVED) {
            throw new RuntimeException("Booking not approved");
        }

        booking.setPaymentMethod(paymentMethod);
        booking.setPaymentStatus(PaymentStatus.UNPAID); // DO NOT mark PAID yet
        bookingRepository.save(booking);
    }

    // ================= RETURN REQUEST =================
    public void requestReturn(Long bookingId, String borrowerEmail) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Verify borrower
        if (!booking.getBorrower().getEmail().equals(borrowerEmail)) {
            throw new RuntimeException("Unauthorized return request");
        }

        // Allow request if booking is BORROWED or if an OTP is already pending
        if (booking.getBookingStatus() == BookingStatus.RETURNED) {
            throw new RuntimeException("Booking already returned");
        }

        // Generate new OTP for owner
        String otp = generateOtp();
        booking.setReturnOtp(otp);

        // Do NOT change booking status
        bookingRepository.save(booking);

        // Send OTP to owner
        emailService.sendReturnOtpToOwner(
                booking.getResource().getOwner().getEmail(),
                booking.getBorrower().getEmail(),
                booking.getResource().getTitle(),
                otp
        );
    }


    // ================= VERIFY RETURN =================
    public void verifyReturn(Long bookingId, String otp) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getReturnOtp() == null || !booking.getReturnOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        booking.setBookingStatus(BookingStatus.RETURNED);
        booking.setReturned(true);
        booking.setReturnedAt(java.time.LocalDateTime.now());
        booking.setReturnOtp(null);
        bookingRepository.save(booking);

        emailService.sendReturnConfirmationToBorrower(
                booking.getBorrower().getEmail(),
                booking.getResource().getTitle()
        );
    }



    // ================= OWNER BOOKINGS =================
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsForOwner(Long ownerId) {
        return bookingRepository.findByResourceOwner(ownerId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getOwnerBookingsByStatus(Long ownerId, BookingStatus status) {
        return bookingRepository.findByResourceOwnerAndStatus(ownerId, status)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUser(Long userId) {
        return bookingRepository.findByBorrowerWithResource(userId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // ================= OTP =================
    private String generateOtp() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    // ================= DTO MAPPER =================
    public BookingResponse mapToDTO(Booking booking) {
        BookingResponse dto = new BookingResponse();
        dto.setBookingId(booking.getBookingId());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setQuantity(booking.getQuantity());
        dto.setRentPerDay(booking.getRentPerDay());
        dto.setDeposit(booking.getDeposit());
        dto.setTotalPayable(booking.getTotalPayable());
        dto.setPaymentStatus(booking.getPaymentStatus());
        dto.setPaymentMethod(booking.getPaymentMethod());
        dto.setBorrowerEmail(booking.getBorrower().getEmail());

        dto.setBorrowDates(
                booking.getBorrowDates() == null ? new ArrayList<>() :
                        booking.getBorrowDates().stream()
                                .map(d -> d.format(DATE_FORMAT))
                                .collect(Collectors.toList())
        );

        Resource r = booking.getResource();
        ResourceResponse rr = new ResourceResponse();
        rr.setResourceId(r.getResourceId());
        rr.setTitle(r.getTitle());
        rr.setRentPrice(r.getRentPrice());
        rr.setDeposit(r.getDeposit());
        rr.setCity(r.getCity());
        rr.setArea(r.getArea());
        rr.setCategory(r.getCategory());
        rr.setImage(r.getImage());
        rr.setOwnerEmail(r.getOwner().getEmail());

        dto.setResource(rr);
        return dto;
    }
}
