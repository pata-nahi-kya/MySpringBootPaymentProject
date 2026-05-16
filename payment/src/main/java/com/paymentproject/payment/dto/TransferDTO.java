package com.paymentproject.payment.dto;

/**
 * Data Transfer Object for money transfer requests.
 */
public class TransferDTO {
    private int senderId;
    private int receiverId;
    private double amount;

    public TransferDTO() {
    }

    public TransferDTO(int senderId, int receiverId, int amount) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
