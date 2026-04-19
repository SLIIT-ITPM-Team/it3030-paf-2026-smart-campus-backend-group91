package com.smart_campus_hub.smart_campus_api.repository;

import com.smart_campus_hub.smart_campus_api.model.Ticket;
import com.smart_campus_hub.smart_campus_api.model.TicketPriority;
import com.smart_campus_hub.smart_campus_api.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByCategory(String category);
    List<Ticket> findByPriority(TicketPriority priority);
}
