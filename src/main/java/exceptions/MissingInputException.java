package exceptions;

import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MissingInputException extends RuntimeException {
    private HttpStatus code;
    private static final Logger logger = LoggerFactory.getLogger(MissingInputException.class);

    public MissingInputException(HttpStatus code, String msg){
        super(msg);
        this.code = code;
        logger.error("MissingIDException (code={}): {}", code, msg);
    }
    public HttpStatus getCode(){
        return code;
    }

    public static void requireValue(String value, String fieldName, String inputType) {
        if (value == null || value.isBlank()) {
            throw new MissingInputException(HttpStatus.BAD_REQUEST,
                    inputType + " " + fieldName + " cannot be empty");
        }
    }

    public static void requireValue(Long value, String fieldName, String inputType) {
        if (value == null) {
            throw new MissingInputException(HttpStatus.BAD_REQUEST,
                    inputType + " " + fieldName + " cannot be empty");
        }
    }
}
