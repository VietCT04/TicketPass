package com.ticketpass.api.order;
import com.ticketpass.api.auth.AuthenticatedUser;
import com.ticketpass.api.common.ApiException;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/orders")
public class BuyerReceiptConfirmationController {
 private final BuyerReceiptConfirmationService service;
 public BuyerReceiptConfirmationController(BuyerReceiptConfirmationService service){this.service=service;}
 @PostMapping("/{orderId}/receipt-confirmation") public ResponseEntity<BuyerReceiptConfirmationResponse> confirm(@AuthenticationPrincipal AuthenticatedUser currentUser,@PathVariable String orderId,@RequestBody(required=false) byte[] body){if(body!=null&&body.length>0)throw new ApiException(HttpStatus.BAD_REQUEST,"Receipt confirmation does not accept a request body");BuyerReceiptConfirmationResponse response=service.confirm(currentUser.id(),orderId);return ResponseEntity.status(response.statusRefreshRequired()?HttpStatus.ACCEPTED:HttpStatus.OK).header(HttpHeaders.CACHE_CONTROL,"no-store").body(response);}
}
