package exceptions;

import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom unchecked exception that extends {@link RuntimeException} and carries an HTTP status code and the error message.
 * The exception is logged at error level when it is created.
 * Intended use is for api related exceptions
 */
public class ValidationException extends RuntimeException {
    private HttpStatus code;
    private static final Logger logger = LoggerFactory.getLogger(ValidationException.class);

    /**
     * Creates a new MissingInputException and logs it.
     *
     * @param code the HTTP status code to return it
     * @param msg  the error message
     */
    public ValidationException(HttpStatus code, String msg) {
        super(msg);
        this.code = code;
        logger.error("MissingIDException (code={}): {}", code, msg);
    }

    public HttpStatus getCode() {
        return code;
    }

    /**
     * Checks that the given value is neither null nor blank.
     *
     * @param value     the value to check
     * @param fieldName the name of the field being checked, like "name" or "id"
     * @param inputType the name of the entity the field belongs, like "Host" or "User"
     * @throws ValidationException if the value is null or blank (status code 400)
     */

    public static void requireValue(String value, String fieldName, String inputType) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    inputType + " " + fieldName + " cannot be empty");
        }
    }

    /**
     * Overload of {@link #requireValue(String, String, String)} for Long values
     * Only checks for null.
     *
     * @param value     the value to check
     * @param fieldName the name of the field being checked, like "name" or "id"
     * @param inputType the name of the entity the field belongs, like "Host" or "User"
     * @throws ValidationException if the value is null (status code 400)
     */
    public static void requireValue(Long value, String fieldName, String inputType) {
        if (value == null) {
            throw new ValidationException(HttpStatus.BAD_REQUEST,
                    inputType + " " + fieldName + " cannot be empty");
        }
    }

    /**
     * Validate the email based on a regex expression
     *
     * @param email the email that needs validation
     * @return true if the email fits the regex rules
     * @see <a href="https://stackoverflow.com/questions/8204680/java-regex-email">Regex source</a>
     */
    private static boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }

    /**
     * Validates an email based on rules, if its null, blank or fits regex expression from {@link #isValidEmail(String)}
     *
     * @param email the email that needs validation
     * @throws ValidationException if the email is null, blank or not valid (status code 400)
     */
    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, "Email cannot be empty");
        } else if (!isValidEmail(email)) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, "Email is not valid");
        }
    }
}
