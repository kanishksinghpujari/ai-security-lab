package dev.kanishk.aisecuritylab.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final AtomicLong productIds = new AtomicLong(2);
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final Map<Long, Order> orders = new ConcurrentHashMap<>();

    public ApiController() {
        products.put(1L, new Product(1L, "Mechanical Keyboard", 7999, 12));
        products.put(2L, new Product(2L, "USB-C Hub", 2499, 25));
        orders.put(1001L, new Order(1001L, "lab-customer", List.of(1L), "CREATED"));
        orders.put(1002L, new Order(1002L, "another-customer", List.of(2L), "CREATED"));
    }

    @GetMapping("/products")
    public List<Product> products() {
        return products.values().stream().sorted((a, b) -> Long.compare(a.id(), b.id())).toList();
    }

    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@Valid @RequestBody CreateProduct request) {
        long id = productIds.incrementAndGet();
        Product product = new Product(id, request.name(), request.priceInPaise(), request.stock());
        products.put(id, product);
        return product;
    }

    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable long id, Authentication authentication) {
        Order order = orders.get(id);
        if (order == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        // INTENTIONAL VULNERABILITY: authenticated users can read every order.
        // This deliberately omits the owner/admin authorization check.
        return order;
    }

    public record Product(long id, String name, int priceInPaise, int stock) {}
    public record Order(long id, String owner, List<Long> productIds, String status) {}
    public record CreateProduct(@NotBlank @Size(max = 100) String name,
        @Min(1) int priceInPaise, @Min(0) int stock) {}
}
