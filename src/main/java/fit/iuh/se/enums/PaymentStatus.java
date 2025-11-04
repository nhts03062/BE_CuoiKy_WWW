package fit.iuh.se.enums;

public enum PaymentStatus {
    PENDING("Pending"),
    SUCCESS("Success"),
    FAIL("Fail"),
    REFUNDED("Refunded"),
    FAILED("Failed");

    private String displayName;
    
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
