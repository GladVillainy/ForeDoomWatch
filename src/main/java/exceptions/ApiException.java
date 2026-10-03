package exceptions;

import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom unchecked exception that extends {@link RuntimeException} and carries an HTTP status code and the error message.
 * The exception is logged at error level when it is created.
 * Intended use is for api related exceptions
 */
public class ApiException extends RuntimeException {
    private HttpStatus code;
    private static final Logger logger = LoggerFactory.getLogger(ApiException.class);


    /**
     * Creates a new ApiException and logs it.
     * @param code the HTTP status code to return it
     * @param msg the error message
     */
    public ApiException(HttpStatus code, String msg){
        super(msg);
        this.code = code;
        logger.error("ApiException (code={}): {}", code, msg);
    }
    public HttpStatus getCode(){
        return code;
    }

    /**
     * Checks that the given key is neither null nor blank.
     * @param apiKey the key to check
     * @throws MissingInputException if the key is null or blank (status code 500)
     */

    public static void requireApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new MissingInputException(HttpStatus.INTERNAL_SERVER_ERROR, "Api key is missing");
        }
    }

}
