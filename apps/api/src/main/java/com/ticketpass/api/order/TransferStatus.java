package com.ticketpass.api.order;

public enum TransferStatus {
    AWAITING_SELLER_TRANSFER,
    SELLER_CONFIRMED_TRANSFER,
    BUYER_CONFIRMED_RECEIPT,
    TRANSFER_TIMED_OUT,
    REQUIRES_REVIEW
}
