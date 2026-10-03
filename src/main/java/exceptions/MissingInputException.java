package exceptions;

import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom unchecked exception that extends {@link RuntimeException} and carries an HTTP status code and the error message.
 * The exception is logged at error level when it is created.
 * Intended use is for api related exceptions
 */
public class MissingInputException extends RuntimeException {
    private HttpStatus code;
    private static final Logger logger = LoggerFactory.getLogger(MissingInputException.class);

    /**
     * Creates a new MissingInputException and logs it.
     * @param code the HTTP status code to return it
     * @param msg the error message
     */
    public MissingInputException(HttpStatus code, String msg){
        super(msg);
        this.code = code;
        logger.error("MissingIDException (code={}): {}", code, msg);
    }
    public HttpStatus getCode(){
        return code;
    }

    /**
     * Checks that the given value is neither null nor blank.
     * @param value the value to check
     * @param fieldName the name of the field being checked, like "name" or "id"
     * @param inputType the name of the entity the field belongs, like "Host" or "User"
     * @throws MissingInputException if the value is null or blank (status code 400)
     */

    public static void requireValue(String value, String fieldName, String inputType) {
        if (value == null || value.isBlank()) {
            throw new MissingInputException(HttpStatus.BAD_REQUEST,
                    inputType + " " + fieldName + " cannot be empty");
        }
    }

    /**
     * Overload of {@link #requireValue(String, String, String)} for Long values
     * Only checks for null.
     * @param value the value to check
     * @param fieldName the name of the field being checked, like "name" or "id"
     * @param inputType the name of the entity the field belongs, like "Host" or "User"
     * @throws MissingInputException if the value is null (status code 400)
     */
    public static void requireValue(Long value, String fieldName, String inputType) {
        if (value == null) {
            throw new MissingInputException(HttpStatus.BAD_REQUEST,
                    inputType + " " + fieldName + " cannot be empty");
        }
    }
}
