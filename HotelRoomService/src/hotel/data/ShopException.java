package hotel.data;

/** A problem the user should be told about (not enough stock, empty room number, file problem...). */
public class ShopException extends RuntimeException {
    public ShopException(String message) { super(message); }
    public ShopException(String message, Throwable cause) { super(message, cause); }
}
