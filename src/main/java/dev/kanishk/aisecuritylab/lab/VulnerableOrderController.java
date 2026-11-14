package dev.kanishk.aisecuritylab.lab;

import dev.kanishk.aisecuritylab.api.ApiController.Order;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

/** Intentionally vulnerable training example. Never deploy the lab-vulnerable profile. */
@RestController
@Profile("lab-vulnerable")
@RequestMapping("/lab-vulnerable")
public class VulnerableOrderController {
    private final Map<Long, Order> orders = Map.of(
        1001L, new Order(1001L, "lab-customer", List.of(1L), "CREATED"),
        1002L, new Order(1002L, "another-customer", List.of(2L), "CREATED"));

    @GetMapping("/orders/{id}")
    public Order getAnyOrder(@PathVariable long id) {
        // INTENTIONAL IDOR: authentication is required globally, but ownership is not checked.
        return orders.get(id);
    }
}
