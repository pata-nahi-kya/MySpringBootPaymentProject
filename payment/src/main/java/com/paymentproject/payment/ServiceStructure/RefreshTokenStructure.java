package com.paymentproject.payment.ServiceStructure;


public interface RefreshTokenStructure {
   

    public String createRefreshToken(String username);
    public String validateRefreshToken(String refreshToken);
    public void deleteRefreshToken(String refreshToken);
    public boolean isRefreshTokenExpired(String refreshToken);
   
}
