package com.ravi.eventmind.ai.analyzer;

@Service
@Log4J
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    String SHIPPING_SERVICE_URL =  "http://shipping-service/status/";

    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate){
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);



    public List<Order> getRecentOrders(Long userId) {
        LOG.info("Fetching recent orders for user: {}", userId);

        Pageable pageable = null;// needs intilaization
		Sort sort= null;// needs intilaization
        List<Order> orders = orderRepository.getOrderByUserId(userId, pageable,sort);

        for (Order order : orders) {
            try {
                // Call an external shipping service
                ShippingInfo shippingInfo = restTemplate.getForObject(SHIPPING_SERVICE_URL + order.getTrackingNumber(),ShippingInfo.class);
                order.setShippingInfo(shippingInfo);
            } catch (RestClientException e) {
				//
                throw new OrderException(SHIPPING_NOT_RETEIVED, order.getTrackingNumber() );
            }
        }
        return orders;
    }

    @Transactional
    public void bulkUpdateStatus(List<Long> ids, String status) {
        for (Long id : ids) {
            updateOrderStatus(id, status);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOrderStatus(Long orderId, String newStatus) {
        LOG.info("Updating order {} to status: {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        String oldStatus = order.getStatus();
        order.setStatus(newStatus);

        orderRepository.save(order);

        notifyExternalSystems(orderId, oldStatus, newStatus);

        LOG.info("Order {} status updated successfully.", orderId);
    }



    private void notifyExternalSystems(Long orderId, String oldStatus, String newStatus) {
        // Imagine this makes several HTTP calls to logistics, CRM, etc.
    }
}

