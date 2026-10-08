package exceptions;

import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom unchecked exception that extends {@link RuntimeException} and carries an HTTP status code and the error message.
 * The exception is logged at error level when it is created.
 * Intended use is for missing entities related exceptions
 */
public class EntityException extends RuntimeException {
    private HttpStatus code;
    private static final Logger logger = LoggerFactory.getLogger(EntityException.class);

    /**
     * Creates a new EntityNotFoundException and logs it.
     * @param code the HTTP status code to return it
     * @param msg the error message
     */
    public EntityException(HttpStatus code, String msg){
        super(msg);
        this.code = code;
        logger.error("EntityNotFoundException (code={}): {}", code, msg);
    }
    public HttpStatus getCode(){
        return code;
    }
}
